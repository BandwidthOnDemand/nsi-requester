package controllers

import java.util.{Date, UUID}
import org.joda.time.{DateTime, Period}
import com.ning.http.client.Realm.AuthScheme
import models._
import FormSupport._
import play.api.data.format.Formats._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.Mapping
import play.api.libs.ws.WS
import play.api.libs.ws.Response
import play.api.mvc.{Response => _, _}
import support.PrettyXml.nodeToString
import org.apache.commons.codec.binary.Base64

object Application extends Controller {

  def index = Action {
    Redirect(routes.Application.reserveForm)
  }

  def reserveForm = Action { implicit request =>
    val startDate = DateTime.now.plusMinutes(5)
    val endDate = startDate.plusMinutes(10)

    val defaultForm = reserveF.fill((
      defaultProvider,
      Reserve(
        description = Some("A NSI reserve test"), startDate = Some(startDate.toDate), end = Left(endDate.toDate),
        connectionId = generateConnectionId, correlationId = generateCorrelationId,
        source = defaultStpUriPrefix, destination = defaultStpUriPrefix, bandwidth = 100, replyTo = defaultReplyToUrl, providerNsa = defaultProviderNsa)
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
      { case (provider, query) => sendEnvelope(provider, query) }
    )
  }

  def settingsForm = Action { implicit request =>
    Ok(views.html.settings(settingsF.fill((defaultProvider, (defaultReplyToUrl, defaultProviderNsa)))))
  }

  def settings = Action { implicit request =>
    settingsF.bindFromRequest.fold(
        formWithErrors => BadRequest(views.html.settings(formWithErrors)),
        { case (provider, (replyTo, providerNsa)) =>
            Redirect(routes.Application.reserveForm).flashing("success" -> "Settings changed for this session")
              .withSession(
                "providerUrl" -> provider.providerUrl,
                "username" -> provider.username.getOrElse(""),
                "password" -> provider.password.getOrElse(""),
                "accessToken" -> provider.accessToken.getOrElse(""),
                "replyTo" -> replyTo,
                "providerNsa" -> providerNsa)
        }
    )
  }

  def resetSettings = Action { implicit request =>
    Redirect(routes.Application.settingsForm).withNewSession.flashing("succes" -> "Settings have been reset")
  }

  private def sendEnvelope(provider: Provider, nsiRequest: Soapable)(implicit request: Request[AnyContent]) = Async {
    import support.PrettyXml._

    val soapRequest = nsiRequest.toEnvelope
    val wsRequest = WS.url(provider.providerUrl)
    val wsAuthRequest = if (provider.username.isDefined) {
      wsRequest.withAuth(provider.username.get, provider.password.getOrElse(""), AuthScheme.BASIC)
    } else if (provider.accessToken.isDefined) {
      val value = "bearer " + provider.accessToken.get
      wsRequest.withHeaders("Authorization" -> value)
    } else {
      wsRequest
    }

    wsAuthRequest.post(soapRequest)
      .map(response => {
        try {
          val prettyRequest = Some(soapRequest.prettify)
          val prettyResponse = Some(response.xml.prettify)
          val correlationId = Some((soapRequest \\ "correlationId").text)

          Ok(views.html.response(prettyRequest, prettyResponse, correlationId))
        } catch {
          case e: Throwable => InternalServerError(views.html.error(e, Some(response.body)))
        }
      })
      .recover {
        case e: Throwable => InternalServerError(views.html.error(e, None))
      }
  }

  private val defaultStpUriPrefix = "urn:ogf:network:stp:surfnet.nl:"
  private val defaultProviderUrl = "https://bod.surfnet.nl/nsi/v1_sc/provider"

  private def generateConnectionId = "urn:uuid:%s".formatted(UUID.randomUUID.toString)

  private def generateCorrelationId = generateConnectionId

  private def defaultProvider(implicit request: Request[AnyContent]) = {
    val url = request.session.get("providerUrl").getOrElse(defaultProviderUrl)
    val user = request.session.get("username")
    val pass = request.session.get("password")
    val token = request.session.get("accessToken")

    Provider(url, user, pass, token)
  }

  private def defaultProviderNsa(implicit request: Request[AnyContent]) =
    request.session.get("providerNsa").getOrElse("urn:ogf:network:nsa:surfnet.nl")

  private def defaultReplyToUrl(implicit request: Request[AnyContent]) =
    request.session.get("replyTo").getOrElse("http://" + request.host + routes.ResponseController.reply)

  private val providerMapping: Mapping[Provider] = mapping(
    "providerUrl" -> nonEmptyText,
    "username" -> optional(text),
    "password" -> optional(text),
    "accessToken" -> optional(text)
  ){ Provider.apply } { Provider.unapply }

  private val settingsF: Form[(Provider, (String, String))] = Form(
    tuple(
      "provider" -> providerMapping,
      "nsi" -> tuple(
        "replyTo" -> nonEmptyText,
        "providerNsa" -> nonEmptyText
      )
    )
  )

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

  private val reserveF: Form[(Provider, Reserve)] = Form(
    tuple(
      "provider" -> providerMapping,
      "reservation" -> mapping(
        "description" -> optional(text),
        "startDate" -> optional(date("yyyy-MM-dd HH:mm")),
        "end" -> endTuple,
        "connectionId" -> nonEmptyText,
        "source" -> nonEmptyText,
        "destination" -> nonEmptyText,
        "bandwidth" -> number(0, 100000),
        "correlationId" -> nonEmptyText,
        "replyTo" -> nonEmptyText,
        "providerNsa" -> nonEmptyText,
        "globalReservationId" ->  text,
        "unprotected" -> boolean
      ){ Reserve.apply } { Reserve.unapply }
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
