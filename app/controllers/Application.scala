package controllers

import java.util.{ Date, UUID }
import scala.util.{ Success, Failure }
import org.joda.time.{ DateTime, Period }
import com.ning.http.client.Realm.AuthScheme
import play.api.data.{ Form, FormError, Mapping }
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.libs.ws.{ WS, Response }
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._
import play.api.mvc._
import play.api.http.HeaderNames.CONTENT_TYPE
import play.api.http.MimeTypes
import support.JsonResponse
import models._
import models.QueryOperation._
import FormSupport._
import Defaults._
import java.net.URI
import play.api.Logger
import scala.concurrent.Future
import play.api.http.ContentTypeOf

object Application extends Controller {

  implicit object FormErrorWrites extends Writes[FormError] {
    def writes(error: FormError) = Json.toJson(
      Map(
        "id" -> Json.toJson(error.key.replace('.', '_')),
        "message" -> Json.toJson(error.message)))
  }

  def index = Action {
    Redirect(routes.Application.reserveForm)
  }

  def reserveForm = Action { implicit request =>
    val startDate = DateTime.now.plusMinutes(5)
    val endDate = startDate.plusMinutes(10)

    val defaultForm = defaultProvider.reserveF.fill(
      Reserve(
        description = Some("A NSI reserve test"), startDate = Some(startDate.toDate), end = Left(endDate.toDate),
        connectionId = generateConnectionId, correlationId = generateCorrelationId,
        serviceType = defaultProvider.nsiVersion.fold(v1 = "", v2 = "http://services.ogf.org/nsi/2013/07/descriptions/EVTS.A-GOLE"),
        source = defaultProvider.nsiVersion.fold(v1 = DefaultPortV1, v2 = DefaultPortV2),
        destination = defaultProvider.nsiVersion.fold(v1 = DefaultPortV1, v2 = DefaultPortV2),
        bandwidth = 100, replyTo = defaultReplyToUrl, requesterNsa = defaultRequesterNsa, providerNsa = defaultProviderNsa)
    )

    Ok(views.html.reserve(defaultForm, defaultProvider))
  }

  def reserve = Action.async { implicit request =>
    defaultProvider.reserveF.bindFromRequest.fold(
      formWithErrors => { Future.successful(BadRequest(Json.toJson(formWithErrors.errors))) },
      reservation => sendEnvelope(defaultProvider, reservation))
  }

  def reserveCommitForm = Action { implicit request =>
    val defaultForm = defaultProvider.reserveCommitF.fill(
      ReserveCommit(connectionId = "", correlationId = generateCorrelationId, replyTo = defaultReplyToUrl, requesterNsa = defaultRequesterNsa, providerNsa = defaultProviderNsa))

    Ok(views.html.reserveCommit(defaultForm, defaultProvider))
  }

  def reserveCommit = Action.async { implicit request =>
    defaultProvider.reserveCommitF.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(Json.toJson(formWithErrors.errors))),
      { case reserveCommit => sendEnvelope(defaultProvider, reserveCommit) })
  }

  def reserveAbortForm = Action { implicit request =>
    val defaultForm = defaultProvider.reserveAbortF.fill(
      ReserveAbort(connectionId = "", correlationId = generateCorrelationId, replyTo = defaultReplyToUrl, requesterNsa = defaultRequesterNsa, providerNsa = defaultProviderNsa))

    Ok(views.html.reserveAbort(defaultForm, defaultProvider))
  }

  def reserveAbort = Action.async { implicit request =>
    defaultProvider.reserveAbortF.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(Json.toJson(formWithErrors.errors))),
      { case reserveAbort => sendEnvelope(defaultProvider, reserveAbort) })
  }

  def provisionForm = Action { implicit request =>
    val defaultForm = defaultProvider.provisionF.fill(
      Provision(connectionId = "", correlationId = generateCorrelationId, replyTo = defaultReplyToUrl, requesterNsa = defaultRequesterNsa, providerNsa = defaultProviderNsa))

    Ok(views.html.provision(defaultForm, defaultProvider))
  }

  def provision = Action.async { implicit request =>
    defaultProvider.provisionF.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(Json.toJson(formWithErrors.errors))),
      { case provision => sendEnvelope(defaultProvider, provision) })
  }

  def terminateForm = Action { implicit request =>
    val defaultForm = defaultProvider.terminateF.fill(
      Terminate(connectionId = "", correlationId = generateCorrelationId, replyTo = defaultReplyToUrl, requesterNsa = defaultRequesterNsa, providerNsa = defaultProviderNsa))

    Ok(views.html.terminate(defaultForm, defaultProvider))
  }

  def terminate = Action.async { implicit request =>
    defaultProvider.terminateF.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(Json.toJson(formWithErrors.errors))),
      { case terminate => sendEnvelope(defaultProvider, terminate) })
  }

  def releaseForm = Action { implicit request =>
    val defaultForm = defaultProvider.releaseF.fill(
      Release(connectionId = "", correlationId = generateCorrelationId, replyTo = defaultReplyToUrl, requesterNsa = defaultRequesterNsa, providerNsa = defaultProviderNsa))

    Ok(views.html.release(defaultForm, defaultProvider))
  }

  def release = Action.async { implicit request =>
    defaultProvider.releaseF.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(Json.toJson(formWithErrors.errors))),
      { case release => sendEnvelope(defaultProvider, release) })
  }

  def queryForm = Action { implicit request =>
    val defaultForm = defaultProvider.queryF.fill(
      Query(Summary, Nil, Nil, generateCorrelationId, defaultReplyToUrl, requesterNsa = defaultRequesterNsa, defaultProviderNsa))

    Ok(views.html.query(defaultForm, defaultProvider))
  }

  def query = Action.async { implicit request =>
    defaultProvider.queryF.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(Json.toJson(formWithErrors.errors))),
      { case query => sendEnvelope(defaultProvider, query) })
  }

  def queryNotificationForm = Action { implicit request =>
    val defaultForm = defaultProvider.queryNotificationF.fill(QueryNotification(QueryNotificationOperation.Async, "", None, None, generateCorrelationId, defaultReplyToUrl, requesterNsa = defaultRequesterNsa, defaultProviderNsa))

    Ok(views.html.queryNotification(defaultForm, defaultProvider))
  }

  def queryNotification = Action.async { implicit request =>
    defaultProvider.queryNotificationF.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(Json.toJson(formWithErrors.errors))),
      { case queryNotification => sendEnvelope(defaultProvider, queryNotification) })
  }

  def validateProvider = Action.async(parse.json) { implicit request =>

    def determineNsiVersion(wsdl: String): Option[NsiVersion] =
      if (wsdl.contains(NsiRequest.NsiV1ProviderNamespace))
        Some(NsiVersion.V1)
      else if (wsdl.contains(NsiRequest.NsiV2ProviderNamespace))
        Some(NsiVersion.V2)
      else
        None

    val wsdlValid =
      for {
        url <- (request.body \ "url").asOpt[String] if !url.isEmpty()
      } yield {
        val authenticated = addAuthenticationHeader(
          (request.body \ "username").asOpt[String],
          (request.body \ "password").asOpt[String],
          (request.body \ "token").asOpt[String])

        val wsdlRequest = authenticated(WS.url(s"$url?wsdl"))

        wsdlRequest.get.map { wsdlResponse =>
          if (wsdlResponse.status == 200) {
            val nsiVersion = determineNsiVersion(wsdlResponse.body)
            Ok(nsiVersion.map(v => Json.obj("valid" -> true, "version" -> v.value, "providerNsa" -> Defaults.DefaultProviderNsa(v))).getOrElse(Json.obj("valid" -> false, "message" -> "unknown NSI version")))
          } else
            Ok(Json.obj("valid" -> false, "message" -> wsdlResponse.status))
        }
      }

    wsdlValid.getOrElse(Future.successful(BadRequest))
  }

  private def sendEnvelope(provider: Provider, nsiRequest: NsiRequest)(implicit r: Request[AnyContent]): Future[SimpleResult] = {
    val soapRequest = nsiRequest.toNsiEnvelope(provider.nsiVersion)
    val requestTime = DateTime.now()

    val addHeaders = addAuthenticationHeader(provider.username, provider.password, provider.accessToken) andThen addSoapActionHeader(nsiRequest.soapAction(provider.nsiVersion))
    val wsRequest = addHeaders(WS.url(provider.providerUrl.toString).withFollowRedirects(false))

    implicit val soapContentType = ContentTypeOf[scala.xml.Node](Some(withCharset("text/xml")))

    wsRequest.post(soapRequest).map { response =>
      Logger.debug(s"Provider (${provider.providerUrl}) response: ${response.status}, ${response.statusText}")
      if (response.header(CONTENT_TYPE).map(_ contains "text/xml").getOrElse(false)) {
        val jsonResponse = JsonResponse.success(soapRequest, requestTime, response.xml, DateTime.now())
        Ok(jsonResponse)
      } else {
        val message = s"Failed: ${response.status} (${response.statusText}), ${response.header(CONTENT_TYPE).getOrElse("No content type header found")}"
        BadRequest(JsonResponse.failure(soapRequest, requestTime, message))
      }
    }.recover {
      case e =>
        Logger.info("Could not send soap request", e)
        BadRequest(Json.obj("message" -> e.getMessage()))
    }
  }

  private[controllers] def addAuthenticationHeader(username: Option[String], password: Option[String], token: Option[String]): WS.WSRequestHolder => WS.WSRequestHolder =
    addBasicAuth(username, password) _ andThen addOAuthToken(token) _

  private def addOAuthToken(token: Option[String])(request: WS.WSRequestHolder): WS.WSRequestHolder =
    token.filterNot(_.isEmpty).fold(request)(t => request.withHeaders("Authorization" -> s"bearer $t"))

  private def addBasicAuth(username: Option[String], password: Option[String])(request: WS.WSRequestHolder): WS.WSRequestHolder =
    username.filterNot(_.isEmpty).fold(request)(u => request.withAuth(u, password.getOrElse(""), AuthScheme.BASIC))

  private def addSoapActionHeader(action: String)(request: WS.WSRequestHolder): WS.WSRequestHolder = request.withHeaders("SOAPAction" -> s""""$action"""")

  private def generateConnectionId = UUID.randomUUID.toString
  private def generateCorrelationId = generateConnectionId

  private implicit class Mappings(provider: Provider) {
    private val nsiVersion = provider.nsiVersion

    private def replyTo: Mapping[Option[URI]] = optional(uri).verifying("replyTo address is required for NSIv1", uri => nsiVersion.fold(v1 = uri.isDefined, v2 = true))
    private def listWithoutEmptyStrings: Mapping[List[String]] = list(text).transform(_.filterNot(_.isEmpty), identity)

    private def endDateOrPeriod = tuple(
      "date" -> optional(date("yyyy-MM-dd HH:mm")),
      "period" -> optional(of[Period])).verifying("Either end date or period is required", t => t match {
        case (None, None) => false
        case _            => true
      }).transform[Either[Date, Period]](
        tuple => if (tuple._1.isDefined) Left(tuple._1.get) else Right(tuple._2.get),
        {
          case Left(date)    => (Some(date), None)
          case Right(period) => (None, Some(period))
        })

    def reserveF: Form[Reserve] = Form(
      "reservation" -> mapping(
        "description" -> optional(text),
        "startDate" -> optional(date("yyyy-MM-dd HH:mm")),
        "end" -> endDateOrPeriod,
        "connectionId" -> nsiVersion.fold(v1 = nonEmptyText, v2 = text),
        "serviceType" -> nsiVersion.fold(v1 = text, v2 = nonEmptyText),
        "source" -> portMapping,
        "destination" -> portMapping,
        "bandwidth" -> longNumber(0, 100000),
        "correlationId" -> nonEmptyText,
        "replyTo" -> replyTo,
        "requesterNsa" -> nonEmptyText,
        "providerNsa" -> nonEmptyText,
        "globalReservationId" -> optional(text),
        "unprotected" -> boolean)(Reserve.apply)(Reserve.unapply))

    def reserveAbortF: Form[ReserveAbort] = Form(
      "reserveAbort" -> genericOperationMapping(ReserveAbort.apply)(ReserveAbort.unapply))

    def reserveCommitF: Form[ReserveCommit] = Form(
      "reserveCommit" -> genericOperationMapping(ReserveCommit.apply)(ReserveCommit.unapply))

    def provisionF: Form[Provision] = Form(
      "provision" -> genericOperationMapping(Provision.apply)(Provision.unapply))

    def terminateF: Form[Terminate] = Form(
      "terminate" -> genericOperationMapping(Terminate.apply)(Terminate.unapply))

    def releaseF: Form[Release] = Form(
      "release" -> genericOperationMapping(Release.apply)(Release.unapply))

    def portMapping = mapping(
      "networkId" -> nonEmptyText,
      "localId" -> nonEmptyText,
      "vlan" -> optional(number(1, 4095)),
      "labels" -> portLabelsMap)(Port.apply)(Port.unapply)

    private def portLabelsMap: Mapping[Map[String, Seq[String]]] = list(text).transform(ls => ls.map(_.trim).filter(_.nonEmpty).map { label =>
      val parts = label.split(":")
      parts.head -> parts.tail.mkString(":").split(",").map(_.trim).toSeq
    }.toMap, ls => ls.map { case (key, values) => s"$key: " + values.mkString(", ") }.toList)

    private def genericOperationMapping[R](apply: (String, String, Option[URI], String, String) => R)(unapply: R => Option[(String, String, Option[URI], String, String)]) =
      mapping(
        "connectionId" -> nonEmptyText,
        "correlationId" -> nonEmptyText,
        "replyTo" -> replyTo,
        "requesterNsa" -> nonEmptyText,
        "providerNsa" -> nonEmptyText)(apply)(unapply)

    import QueryOperation._

    def queryF: Form[Query] = Form(
      "query" -> mapping(
        "operation" -> of[QueryOperation],
        "connectionIds" -> listWithoutEmptyStrings,
        "globalReservationIds" -> listWithoutEmptyStrings,
        "correlationId" -> nonEmptyText,
        "replyTo" -> replyTo,
        "requesterNsa" -> nonEmptyText,
        "providerNsa" -> nonEmptyText) { Query.apply } { Query.unapply })

    def queryNotificationF: Form[QueryNotification] = Form(
      "queryNotification" -> mapping(
        "operation" -> of[QueryNotificationOperation.Value],
        "connectionId" -> nonEmptyText,
        "startNotificationId" -> optional(number),
        "endNotificationId" -> optional(number),
        "correlationId" -> nonEmptyText,
        "replyTo" -> replyTo,
        "requesterNsa" -> nonEmptyText,
        "providerNsa" -> nonEmptyText) { QueryNotification.apply } { QueryNotification.unapply })
  }
}
