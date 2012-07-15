package controllers

import java.util.Date
import java.util.UUID
import scala.xml.Elem
import scala.xml.Node
import scala.xml.NodeSeq
import scala.xml.PrettyPrinter
import com.ning.http.client.Realm.AuthScheme
import play.api.data.Form
import play.api.data.Mapping
import play.api.data.Forms._
import play.api.libs.iteratee.Enumerator
import play.api.libs.iteratee.Iteratee
import play.api.libs.iteratee.PushEnumerator
import play.api.libs.ws.WS
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.WebSocket
import models._

object Application extends Controller {

  def index = Action {
    Redirect(routes.Application.reserve)
  }

  def reserveForm = Action {
    val defaultForm = reserveF.fill((
      defaultProvider,
      Reservation(
        description = "A NSI reserve test", startDate = new Date, endDate = new Date,
        connectionId = generateConnectionId, correlationId = generateCorrelationId,
        source = "First port", destination = "Second port", bandwidth = 1000)
    ))

    Ok(views.html.reserve(defaultForm))
  }

  def reserve = Action { implicit request =>
    reserveF.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.reserve(formWithErrors)),
      { case (provider, reservation) => sendEnvelope(provider, reservation) }
    )
  }

  def provisionForm = Action {
    val defaultForm = provisionF.fill(
      defaultProvider,
      Provision(connectionId = "", correlationId = generateCorrelationId)
    )
    Ok(views.html.provision(defaultForm))
  }

  def provision = Action { implicit request =>
    provisionF.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.provision(formWithErrors)),
      { case (provider, provision) => sendEnvelope(provider, provision) }
    )
  }

  def terminateForm = Action {
    val defaultForm = terminateF.fill(
      defaultProvider,
      Terminate(connectionId = "", correlationId = generateCorrelationId)
    )
    Ok(views.html.terminate(defaultForm))
  }

  def terminate = Action { implicit request =>
    terminateF.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.terminate(formWithErrors)),
      { case(provider, terminate) => sendEnvelope(provider, terminate) }
    )
  }

  def releaseForm = Action {
    Ok(views.html.release())
  }

  def queryForm = Action {
    Ok(views.html.query())
  }

  private def sendEnvelope(provider: Provider, request: Soapable) = Async {
    val soapRequest = request.toEnvelope(provider.replyToHost + routes.Application.reply)
    WS.url(provider.providerUrl)
      .withAuth(provider.username, provider.password, AuthScheme.BASIC)
      .post(soapRequest).map { response =>
         Ok(views.html.response(prettify(soapRequest), prettify(response.xml)))
    }
  }

  private def prettify(xml: Node): String = {
    val printer = new PrettyPrinter(80, 4)
    printer.format(xml)
  }

  private def prettify(xml: NodeSeq): String = {
    xml.foldLeft("")((a, b) => a + prettify(b))
  }

  private var clients: List[PushEnumerator[String]] = List()

  def reply = Action { request =>
    clients.foreach { client =>
      client.push(prettify(request.body.asXml.get))
    }
    Ok
  }

  def wsRequest = WebSocket.using[String] { request =>
    val in = Iteratee.foreach[String](println).mapDone { _ =>
      println("Disconnected")
    }

    lazy val out: PushEnumerator[String] = Enumerator.imperative[String]()

    clients = out :: clients

    (in, out)
  }

  private def generateConnectionId = "urn:uuid:%s".formatted(UUID.randomUUID.toString)

  private def generateCorrelationId = generateConnectionId

  private val defaultProvider = Provider("http://localhost:8082/bod/nsi/v1_sc/provider", "nsi", "nsi123", "http://localhost:9000")

  private val providerMapping: Mapping[Provider] = mapping(
    "providerUrl" -> nonEmptyText,
    "username" -> text,
    "password" -> text,
    "replyToHost" -> nonEmptyText){ Provider.apply } { Provider.unapply }

  private val reserveF: Form[(Provider, Reservation)] = Form(
    tuple(
      "provider" -> providerMapping,
      "reservation" -> mapping(
        "description" -> text,
        "startDate" -> date("yyyy-MM-dd HH:mm"),
        "endDate" -> date("yyyy-MM-dd HH:mm"),
        "connectionId" -> nonEmptyText,
        "correlationId" -> nonEmptyText,
        "source" -> nonEmptyText,
        "destination" -> nonEmptyText,
        "bandwidth" -> number(0, 100000)
      ){ Reservation.apply } { Reservation.unapply }
    )
  )

  private val provisionF: Form[(Provider, Provision)] = Form(
    tuple(
      "provider" -> providerMapping,
      "provision" -> mapping(
        "connectionId" -> nonEmptyText,
        "correlationId" -> nonEmptyText
      ){ Provision.apply }{ Provision.unapply }
    )
  )

  private val terminateF: Form[(Provider, Terminate)] = Form(
    tuple(
      "provider" -> providerMapping,
      "terminate" -> mapping(
        "connectionId" -> nonEmptyText,
        "correlationId" -> nonEmptyText
      ){ Terminate.apply }{ Terminate.unapply }
    )
  )
}
