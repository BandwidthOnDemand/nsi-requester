package controllers

import java.util.{Date, UUID}
import org.joda.time.{DateTime, Period}
import com.ning.http.client.Realm.AuthScheme
import models._
import FormSupport._
import Defaults._
import play.api.data.format.Formats._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.Mapping
import play.api.libs.ws.WS
import play.api.libs.ws.Response
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.{Response => _, _}
import support.JsonResponse
import play.api.libs.json.Json
import play.api.libs.json.Writes
import play.api.data.FormError
import play.api.libs.json.JsValue
import views.html.defaultpages.badRequest
import play.api.http.HeaderNames.CONTENT_TYPE
import play.api.http.MimeTypes

object Application extends Controller {

  implicit object FormErrorWrites extends Writes[FormError] {
    def writes(error: FormError) = Json.toJson(
      Map(
        "id" -> Json.toJson(error.key.replace('.', '_')),
        "message" -> Json.toJson(error.message)
      ))
  }

  def index = Action {
    Redirect(routes.Application.reserveForm)
  }

  def reserveForm = Action { implicit request =>
    val startDate = DateTime.now.plusMinutes(5)
    val endDate = startDate.plusMinutes(10)

    val defaultForm = reserveF(defaultProvider.nsiVersion).fill(
      Reserve(
        description = Some("A NSI reserve test"), startDate = Some(startDate.toDate), end = Left(endDate.toDate),
        connectionId = generateConnectionId, correlationId = generateCorrelationId,
        source = DefaultStpUriPrefix, destination = DefaultStpUriPrefix, bandwidth = 100, replyTo = defaultReplyToUrl, providerNsa = defaultProviderNsa
      )
    )

    Ok(views.html.reserve(defaultForm, defaultProvider))
  }

  def reserve = Action { implicit request =>
    reserveF(defaultProvider.nsiVersion).bindFromRequest.fold(
      formWithErrors => BadRequest(Json.toJson(formWithErrors.errors)),
      { case reservation => sendEnvelope(defaultProvider, reservation) }
    )
  }

  def reserveCommitForm = Action { implicit request =>
    val defaultForm = reserveCommitF.fill(
      ReserveCommit(connectionId = "", correlationId = generateCorrelationId, replyTo = defaultReplyToUrl, providerNsa = defaultProviderNsa)
    )

    Ok(views.html.reserveCommit(defaultForm, defaultProvider))
  }

  def reserveCommit = Action { implicit request =>
    reserveCommitF.bindFromRequest.fold(
      formWithErrors => BadRequest(Json.toJson(formWithErrors.errors)),
      { case reserveCommit => sendEnvelope(defaultProvider, reserveCommit) }
    )
  }

  def reserveAbortForm = Action { implicit request =>
    val defaultForm = reserveAbortF.fill(
      ReserveAbort(connectionId = "", correlationId = generateCorrelationId, replyTo = defaultReplyToUrl, providerNsa = defaultProviderNsa)
    )

    Ok(views.html.reserveAbort(defaultForm, defaultProvider))
  }

  def reserveAbort = Action { implicit request =>
    reserveAbortF.bindFromRequest.fold(
      formWithErrors => BadRequest(Json.toJson(formWithErrors.errors)),
      { case reserveAbort => sendEnvelope(defaultProvider, reserveAbort) }
    )
  }

  def provisionForm = Action { implicit request =>
    val defaultForm = provisionF.fill(
      Provision(connectionId = "", correlationId = generateCorrelationId, replyTo = defaultReplyToUrl, providerNsa = defaultProviderNsa)
    )

    Ok(views.html.provision(defaultForm, defaultProvider))
  }

  def provision = Action { implicit request =>
    provisionF.bindFromRequest.fold(
      formWithErrors => BadRequest(Json.toJson(formWithErrors.errors)),
      { case provision => sendEnvelope(defaultProvider, provision) }
    )
  }

  def terminateForm = Action { implicit request =>
    val defaultForm = terminateF.fill(
      Terminate(connectionId = "", correlationId = generateCorrelationId, replyTo = defaultReplyToUrl, providerNsa = defaultProviderNsa)
    )

    Ok(views.html.terminate(defaultForm, defaultProvider))
  }

  def terminate = Action { implicit request =>
    terminateF.bindFromRequest.fold(
      formWithErrors => BadRequest(Json.toJson(formWithErrors.errors)),
      { case terminate => sendEnvelope(defaultProvider, terminate) }
    )
  }

  def releaseForm = Action { implicit request =>
    val defaultForm = releaseF.fill(
      Release(connectionId = "", correlationId = generateCorrelationId, replyTo = defaultReplyToUrl, providerNsa = defaultProviderNsa)
    )

    Ok(views.html.release(defaultForm, defaultProvider))
  }

  def release = Action { implicit request =>
    releaseF.bindFromRequest.fold(
      formWithErrors => BadRequest(Json.toJson(formWithErrors.errors)),
      { case release => sendEnvelope(defaultProvider, release) }
    )
  }

  def queryForm = Action { implicit request =>
    val defaultForm = queryF.fill(
      Query("Summary", Nil, Nil, generateCorrelationId, defaultReplyToUrl, defaultProviderNsa)
    )

    Ok(views.html.query(defaultForm, defaultProvider))
  }

  def query = Action { implicit request =>
    queryF.bindFromRequest.fold(
      formWithErrors => BadRequest(Json.toJson(formWithErrors.errors)),
      { case query => sendEnvelope(defaultProvider, query) }
    )
  }

  private def sendEnvelope(provider: Provider, nsiRequest: NsiRequest)(implicit r: Request[AnyContent]) = Async {

    val wsRequest = {
      val request = WS.url(provider.providerUrl).withFollowRedirects(false)

      if (provider.username.isDefined)
        request.withAuth(provider.username.get, provider.password.getOrElse(""), AuthScheme.BASIC)
      else if (provider.accessToken.isDefined)
        request.withHeaders("Authorization" -> s"bearer ${provider.accessToken.get}")
      else
        request
    }

    val soapRequest = nsiRequest.toNsiEnvelope(provider.nsiVersion)
    val requestTime = DateTime.now()

    wsRequest.post(soapRequest).map(response => {
      if (response.status == 200 && response.header(CONTENT_TYPE).map(_.contains(MimeTypes.XML)).getOrElse(false)) {
        val jsonResponse = JsonResponse.success(soapRequest, requestTime, response.xml, DateTime.now())
        Ok(jsonResponse)
      } else {
        val jsonResponse = JsonResponse.failure(soapRequest, requestTime, s"Failed: ${response.status} (${response.statusText}), ${response.header(CONTENT_TYPE).getOrElse("No content type header found")}")
        BadRequest(jsonResponse)
      }
    }).recover {
      case e => BadRequest(Json.obj("message" -> e.getMessage()))
    }
  }

  private def generateConnectionId = UUID.randomUUID.toString
  private def generateCorrelationId = generateConnectionId

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

  private def reserveF(version: Int): Form[Reserve] = Form(
    "reservation" -> mapping(
      "description" -> optional(text),
      "startDate" -> optional(date("yyyy-MM-dd HH:mm")),
      "end" -> endTuple,
      "connectionId" -> (if(version == 1) nonEmptyText else text),
      "source" -> nonEmptyText,
      "destination" -> nonEmptyText,
      "bandwidth" -> number(0, 100000),
      "correlationId" -> nonEmptyText,
      "replyTo" -> nonEmptyText,
      "providerNsa" -> nonEmptyText,
      "globalReservationId" -> optional(text),
      "unprotected" -> boolean
    ) { Reserve.apply } { Reserve.unapply })

  private val reserveAbortF: Form[ReserveAbort] = Form(
    "reserveAbort" -> genericOperationMapping(ReserveAbort.apply)(ReserveAbort.unapply)
  )

  private val reserveCommitF: Form[ReserveCommit] = Form(
    "reserveCommit" -> genericOperationMapping(ReserveCommit.apply)(ReserveCommit.unapply)
  )

  private val provisionF: Form[Provision] = Form(
    "provision" -> genericOperationMapping(Provision.apply)(Provision.unapply)
  )

  private val terminateF: Form[Terminate] = Form(
    "terminate" -> genericOperationMapping(Terminate.apply)(Terminate.unapply)
  )

  private val releaseF: Form[Release] = Form(
    "release" -> genericOperationMapping(Release.apply)(Release.unapply)
  )

  private def genericOperationMapping[R](apply: Function4[String, String, String, String, R])(unapply: Function1[R, Option[(String, String, String, String)]]) =
    mapping(
      "connectionId" -> nonEmptyText,
      "correlationId" -> nonEmptyText,
      "replyTo" -> nonEmptyText,
      "providerNsa" -> nonEmptyText
    )(apply)(unapply)

  private def listWithoutEmptyStrings: Mapping[List[String]] = list(text).transform(_.filterNot(_.isEmpty), identity)

  private val queryF: Form[Query] = Form(
    "query" -> mapping(
      "operation" -> text,
      "connectionIds" -> listWithoutEmptyStrings,
      "globalReservationIds" -> listWithoutEmptyStrings,
      "correlationId" -> nonEmptyText,
      "replyTo" -> nonEmptyText,
      "providerNsa" -> nonEmptyText
    ) { Query.apply } { Query.unapply })

}