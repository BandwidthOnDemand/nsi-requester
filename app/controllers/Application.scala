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
import play.api.libs.ws.Response
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.WebSocket
import play.api.mvc.Request
import models._
import org.joda.time.Period
import org.joda.time.DateTime
import play.api.mvc.AnyContent
import scala.Left
import FormSupport._

object Application extends Controller {

  def index = Action {
    Redirect(routes.Application.reserveForm)
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
    val defaultForm = queryF.fill(defaultProvider, Query("Summary", Nil, Nil, generateCorrelationId, defaultReplyToUrl, defaultProviderNsa))
    Ok(views.html.query(defaultForm))
  }

  def query = Action { implicit request =>
    queryF.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.query(formWithErrors)),
      { case(provider, query) => sendEnvelope(provider, query) }
    )
  }

  private def sendEnvelope(provider: Provider, nsiRequest: Soapable)(implicit request: Request[AnyContent]) = Async {
    import support.PrettyXml._

    val soapRequest = nsiRequest.toEnvelope
    WS.url(provider.providerUrl)
      .withAuth(provider.username, provider.password, AuthScheme.BASIC)
      .post(soapRequest)
      .recover { case e: Throwable => new Response(null) {
          override def status = 500
          override lazy val body = e.toString
        }
      }
      .map {
        case response if response.status == 500 => Ok("Server %s could not be reached:\n %s".format(provider.providerUrl, response.body))
        case response => Ok(views.html.response(Some(soapRequest.prettify), Some(response.xml.prettify)))
    }
  }

  private def defaultStpUriPrefix = "urn:ogf:network:stp:surfnet.nl:"

  private def generateConnectionId = "urn:uuid:%s".formatted(UUID.randomUUID.toString)

  private def generateCorrelationId = generateConnectionId

  private def defaultProvider = Provider("http://localhost:8082/bod/nsi/v1_sc/provider", "nsi", "nsi123")

  private def defaultProviderNsa = "urn:ogf:network:nsa:surfnet.nl"

  private def defaultReplyToUrl(implicit request: Request[AnyContent]) = "http://" + request.host + routes.ResponseController.reply

  private val providerMapping: Mapping[Provider] = mapping(
    "providerUrl" -> nonEmptyText,
    "username" -> text,
    "password" -> text){ Provider.apply } { Provider.unapply }

  private val endTuple = tuple(
    "date" -> optional(date("yyyy-MM-dd HH:mm")),
    "period" -> optional(of[Period])
  ).verifying("Either end date or period is required", t => t match {
      case (None, None) => false
      case _ => true
  }).transform[Either[Date, Period]](
      tuple => if (tuple._1.isDefined) Left(tuple._1.get) else Right(tuple._2.get),
      {
        case Left(date) => (Some(date), None)
        case Right(period) => (None, Some(period))
      }
  )

  private val reserveF: Form[(Provider, Reservation)] = Form(
    tuple(
      "provider" -> providerMapping,
      "reservation" -> mapping(
        "description" -> optional(text),
        "startDate" -> date("yyyy-MM-dd HH:mm"),
        "end" -> endTuple,
        "connectionId" -> nonEmptyText,
        "source" -> nonEmptyText,
        "destination" -> nonEmptyText,
        "bandwidth" -> number(0, 100000),
        "correlationId" -> nonEmptyText,
        "replyTo" -> nonEmptyText,
        "providerNsa" -> nonEmptyText,
        "globalReservationId" ->  text
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
        "operation" -> text,
        "connectionIds" -> listWithoutEmptyStrings,
        "globalReservationIds" -> listWithoutEmptyStrings,
        "correlationId" -> nonEmptyText,
        "replyTo" -> nonEmptyText,
        "providerNsa" -> nonEmptyText
      ){ Query.apply }{ Query.unapply }
  ))

}
