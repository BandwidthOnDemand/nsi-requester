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
import play.api.mvc.Request
import models._
import org.joda.time.Period
import org.joda.time.DateTime
import play.api.mvc.AnyContent

object Application extends Controller {

  def index = Action {
    Redirect(routes.Application.reserve)
  }

  def reserveForm = Action { implicit request =>
    val startDate = DateTime.now.plusMinutes(5)
    val endDate = startDate.plusMinutes(10)

    val defaultForm = reserveF.fill((
      defaultProvider,
      Reservation(
        description = Some("A NSI reserve test"), startDate = startDate.toDate, end = Left(endDate.toDate),
        connectionId = generateConnectionId, correlationId = generateCorrelationId,
        source = defaultStpUriPrefix, destination = defaultStpUriPrefix, bandwidth = 1000, replyTo = defaultReplyToUrl, providerNsa = defaultProviderNsa)
    ))

    Ok(views.html.reserve(defaultForm))
  }

  def reserve = Action { implicit request =>
    reserveF.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.reserve(formWithErrors)),
      { case (provider, reservation) => sendEnvelope(provider, reservation) }
    )
  }

  def provisionForm = Action { implicit request =>
    val defaultForm = provisionF.fill(
      defaultProvider,
      Provision(connectionId = "", correlationId = generateCorrelationId, replyTo = defaultReplyToUrl, providerNsa = defaultProviderNsa)
    )
    Ok(views.html.provision(defaultForm))
  }

  def provision = Action { implicit request =>
    provisionF.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.provision(formWithErrors)),
      { case (provider, provision) => sendEnvelope(provider, provision) }
    )
  }

  def terminateForm = Action { implicit request =>
    val defaultForm = terminateF.fill(
      defaultProvider,
      Terminate(connectionId = "", correlationId = generateCorrelationId, replyTo = defaultReplyToUrl, providerNsa = defaultProviderNsa)
    )
    Ok(views.html.terminate(defaultForm))
  }

  def terminate = Action { implicit request =>
    terminateF.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.terminate(formWithErrors)),
      { case(provider, terminate) => sendEnvelope(provider, terminate) }
    )
  }

  def releaseForm = Action { implicit request =>
    val defaultForm = releaseF.fill(
      defaultProvider,
      Release(connectionId = "", correlationId = generateCorrelationId, replyTo = defaultReplyToUrl, providerNsa = defaultProviderNsa)
    )
    Ok(views.html.release(defaultForm))
  }

  def release = Action { implicit request =>
    releaseF.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.release(formWithErrors)),
      { case(provider, release) => sendEnvelope(provider, release) }
    )
  }

  def queryForm = Action { implicit request =>
    val defaultForm = queryF.fill(defaultProvider, Query(Nil, Nil, generateCorrelationId, defaultReplyToUrl, defaultProviderNsa))
    Ok(views.html.query(defaultForm))
  }

  def query = Action { implicit request =>
    queryF.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.query(formWithErrors)),
      { case(provider, query) => sendEnvelope(provider, query) }
    )
  }

  private def sendEnvelope(provider: Provider, nsiRequest: Soapable)(implicit request: Request[AnyContent]) = Async {
    val soapRequest = nsiRequest.toEnvelope
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
    val soapResponse = prettify(request.body.asXml.get)
    clients.foreach { client =>
      client.push(soapResponse)
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

  private def defaultStpUriPrefix = "urn:ogf:network:stp:surfnet.nl:"

  private def generateConnectionId = "urn:uuid:%s".formatted(UUID.randomUUID.toString)

  private def generateCorrelationId = generateConnectionId

  private def defaultProvider = Provider("http://localhost:8082/bod/nsi/v1_sc/provider", "nsi", "nsi123")

  private def defaultProviderNsa = "urn:ogf:network:nsa:surfnet.nl"

  private def defaultReplyToUrl(implicit request: Request[AnyContent]) = "http://" + request.host + routes.Application.reply

  private val providerMapping: Mapping[Provider] = mapping(
    "providerUrl" -> nonEmptyText,
    "username" -> text,
    "password" -> text){ Provider.apply } { Provider.unapply }

  private val reserveF: Form[(Provider, Reservation)] = Form(
    tuple(
      "provider" -> providerMapping,
      "reservation" -> mapping(
        "description" -> optional(text),
        "startDate" -> date("yyyy-MM-dd HH:mm"),
        "endDate" -> date("yyyy-MM-dd HH:mm").transform[Either[Date, Period]](d => Left(d), e => e.left.get),
        "connectionId" -> nonEmptyText,
        "source" -> nonEmptyText,
        "destination" -> nonEmptyText,
        "bandwidth" -> number(0, 100000),
        "correlationId" -> nonEmptyText,
        "replyTo" -> nonEmptyText,
        "providerNsa" -> nonEmptyText,
        "globalReservationId" -> optional(text)
      ){ Reservation.apply } { Reservation.unapply }
    )
  )

  private val provisionF: Form[(Provider, Provision)] = Form(
    tuple(
      "provider" -> providerMapping,
      "provision" -> mapping(
        "connectionId" -> nonEmptyText,
        "correlationId" -> nonEmptyText,
        "replyTo" -> nonEmptyText,
        "providerNsa" -> nonEmptyText
      ){ Provision.apply }{ Provision.unapply }
    )
  )

  private val terminateF: Form[(Provider, Terminate)] = Form(
    tuple(
      "provider" -> providerMapping,
      "terminate" -> mapping(
        "connectionId" -> nonEmptyText,
        "correlationId" -> nonEmptyText,
        "replyTo" -> nonEmptyText,
        "providerNsa" -> nonEmptyText
      ){ Terminate.apply }{ Terminate.unapply }
    )
  )

  private val releaseF: Form[(Provider, Release)] = Form(
    tuple(
      "provider" -> providerMapping,
      "release" -> mapping(
        "connectionId" -> nonEmptyText,
        "correlationId" -> nonEmptyText,
        "replyTo" -> nonEmptyText,
        "providerNsa" -> nonEmptyText
      ){ Release.apply }{ Release.unapply }
    )
  )

  private def listWithoutEmptyStrings: Mapping[List[String]] = list(text).transform(l => l.filterNot(_.isEmpty), identity)

  private val queryF: Form[(Provider, Query)] = Form(
    tuple(
      "provider" -> providerMapping,
      "query" -> mapping(
        "connectionIds" -> listWithoutEmptyStrings,
        "globalReservationIds" -> listWithoutEmptyStrings,
        "correlationId" -> nonEmptyText,
        "replyTo" -> nonEmptyText,
        "providerNsa" -> nonEmptyText
      ){ Query.apply }{ Query.unapply }
    )
  )

}
