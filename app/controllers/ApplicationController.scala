/*
 * Copyright (c) 2012, 2013, 2014, 2015, 2016 SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *     disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package controllers

import java.util.UUID
import play.api.data.{Form, FormError, Mapping}
import play.api.data.Forms.*
import play.api.data.format.Formats.*
import play.api.libs.ws.{WSClient, WSRequest}
import play.api.libs.ws.XMLBodyWritables.*
import play.api.libs.json.*
import play.api.*
import play.api.mvc.*
import support.JsonResponse
import models.*
import FormSupport.*
import java.net.URI
import play.api.Logger
import scala.concurrent.Future
import org.joda.time.DateTime
import scala.concurrent.ExecutionContext
import javax.inject.Inject

@javax.inject.Singleton
class ApplicationController @Inject() (
    val controllerComponents: ControllerComponents,
    val configuration: Configuration,
    val environment: Environment,
    requesterSession: RequesterSession,
    ws: WSClient
)(implicit ec: ExecutionContext)
    extends BaseController
    with Soap11Controller
    with ViewContextSupport:
  import requesterSession.*

  private val logger = Logger(classOf[Application])

  implicit object FormErrorWrites extends Writes[FormError]:
    def writes(error: FormError): JsValue = Json.toJson(
      Map("id" -> Json.toJson(error.key.replace('.', '_')), "message" -> Json.toJson(error.message))
    )

  def index: Action[AnyContent] = Action {
    Redirect(routes.ApplicationController.reserveForm)
  }

  def reserveForm: Action[AnyContent] = Action { implicit request =>
    val startDate = DateTime.now.plusMinutes(5)
    val endDate = startDate.plusMinutes(15)

    val defaultForm = currentEndPoint.reserveF.fill(
      Reserve(
        description = Some("A NSI reserve test"),
        startDate = Some(startDate.toDate),
        endDate = endDate.toDate,
        correlationId = generateCorrelationId,
        serviceType = RequesterSession.ServiceType,
        source = Port(currentPortPrefix),
        destination = Port(currentPortPrefix),
        ero = List(),
        bandwidth = 100,
        replyTo = Some(ReplyToUrl),
        requesterNsa = RequesterNsa,
        provider = currentEndPoint.provider
      )
    )

    Ok(views.html.reserve(defaultForm))
  }

  def reserve = Action.async { implicit request =>
    currentEndPoint.reserveF
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(Json.toJson(formWithErrors.errors))),
        reservation => sendEnvelope(currentEndPoint, reservation)
      )
  }

  def reserveModifyForm: Action[AnyContent] = Action { implicit request =>
    val defaultForm = currentEndPoint.modifyF.fill(
      ReserveModify(
        connectionId = "",
        startDate = None,
        startNow = false,
        endDate = None,
        indefiniteEnd = false,
        bandwidth = None,
        correlationId = generateCorrelationId,
        replyTo = Some(ReplyToUrl),
        requesterNsa = RequesterNsa,
        provider = currentEndPoint.provider
      )
    )

    Ok(views.html.reserveModify(defaultForm))
  }

  def reserveModify = Action.async { implicit request =>
    currentEndPoint.modifyF
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(Json.toJson(formWithErrors.errors))),
        modify => sendEnvelope(currentEndPoint, modify)
      )
  }

  def reserveCommitForm: Action[AnyContent] = Action { implicit request =>
    val defaultForm = currentEndPoint.reserveCommitF.fill(
      ReserveCommit(
        connectionId = "",
        correlationId = generateCorrelationId,
        replyTo = Some(ReplyToUrl),
        requesterNsa = RequesterNsa,
        provider = currentEndPoint.provider
      )
    )

    Ok(views.html.reserveCommit(defaultForm))
  }

  def reserveCommit = Action.async { implicit request =>
    currentEndPoint.reserveCommitF
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(Json.toJson(formWithErrors.errors))),
        { case reserveCommit => sendEnvelope(currentEndPoint, reserveCommit) }
      )
  }

  def reserveAbortForm: Action[AnyContent] = Action { implicit request =>
    val defaultForm = currentEndPoint.reserveAbortF.fill(
      ReserveAbort(
        connectionId = "",
        correlationId = generateCorrelationId,
        replyTo = Some(ReplyToUrl),
        requesterNsa = RequesterNsa,
        provider = currentEndPoint.provider
      )
    )

    Ok(views.html.reserveAbort(defaultForm))
  }

  def reserveAbort = Action.async { implicit request =>
    currentEndPoint.reserveAbortF
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(Json.toJson(formWithErrors.errors))),
        { case reserveAbort => sendEnvelope(currentEndPoint, reserveAbort) }
      )
  }

  def provisionForm: Action[AnyContent] = Action { implicit request =>
    val defaultForm = currentEndPoint.provisionF.fill(
      Provision(
        connectionId = "",
        correlationId = generateCorrelationId,
        replyTo = Some(ReplyToUrl),
        requesterNsa = RequesterNsa,
        provider = currentEndPoint.provider
      )
    )

    Ok(views.html.provision(defaultForm))
  }

  def provision = Action.async { implicit request =>
    currentEndPoint.provisionF
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(Json.toJson(formWithErrors.errors))),
        { case provision => sendEnvelope(currentEndPoint, provision) }
      )
  }

  def terminateForm: Action[AnyContent] = Action { implicit request =>
    val defaultForm = currentEndPoint.terminateF.fill(
      Terminate(
        connectionId = "",
        correlationId = generateCorrelationId,
        replyTo = Some(ReplyToUrl),
        requesterNsa = RequesterNsa,
        provider = currentEndPoint.provider
      )
    )

    Ok(views.html.terminate(defaultForm))
  }

  def terminate = Action.async { implicit request =>
    currentEndPoint.terminateF
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(Json.toJson(formWithErrors.errors))),
        { case terminate => sendEnvelope(currentEndPoint, terminate) }
      )
  }

  def releaseForm: Action[AnyContent] = Action { implicit request =>
    val defaultForm = currentEndPoint.releaseF.fill(
      Release(
        connectionId = "",
        correlationId = generateCorrelationId,
        replyTo = Some(ReplyToUrl),
        requesterNsa = RequesterNsa,
        provider = currentEndPoint.provider
      )
    )

    Ok(views.html.release(defaultForm))
  }

  def release = Action.async { implicit request =>
    currentEndPoint.releaseF
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(Json.toJson(formWithErrors.errors))),
        { case release => sendEnvelope(currentEndPoint, release) }
      )
  }

  def queryForm: Action[AnyContent] = Action { implicit request =>
    val defaultModifiedSince = DateTime.now.minusMonths(1).toDate
    val defaultForm = currentEndPoint.queryF.fill(
      Query(
        QueryOperation.Summary,
        Nil,
        Nil,
        Some(defaultModifiedSince),
        generateCorrelationId,
        replyTo = Some(ReplyToUrl),
        requesterNsa = RequesterNsa,
        provider = currentEndPoint.provider
      )
    )

    Ok(views.html.query(defaultForm))
  }

  def query = Action.async { implicit request =>
    currentEndPoint.queryF
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(Json.toJson(formWithErrors.errors))),
        { case query => sendEnvelope(currentEndPoint, query) }
      )
  }

  def queryMessageForm: Action[AnyContent] = Action { implicit request =>
    val defaultForm = currentEndPoint.queryMessageF.fill(
      QueryMessage(
        QueryMessageMode.ResultAsync,
        "",
        None,
        None,
        generateCorrelationId,
        Some(ReplyToUrl),
        requesterNsa = RequesterNsa,
        provider = currentEndPoint.provider
      )
    )

    Ok(views.html.queryMessage(defaultForm))
  }

  def queryMessage: Action[AnyContent] = Action.async { implicit request =>
    currentEndPoint.queryMessageF
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(Json.toJson(formWithErrors.errors))),
        { case queryMessage => sendEnvelope(currentEndPoint, queryMessage) }
      )
  }

  def validateProvider: Action[JsValue] = Action.async(parse.json) { implicit request =>
    val wsdlValid =
      for
        nsaId <- (request.body \ "nsa-id").asOpt[String]
        provider <- findProvider(nsaId)
      yield
        val wsdlRequest = ws.url(s"${provider.providerUrl}?wsdl").withFollowRedirects(false)

        wsdlRequest.get().map { wsdlResponse =>
          val response =
            if wsdlResponse.status == 200 then Json.obj("valid" -> true)
            else Json.obj("valid" -> false, "message" -> wsdlResponse.status)
          Ok(response)
        }

    wsdlValid.getOrElse(Future.successful(BadRequest))
  }

  private def sendEnvelope(endPoint: EndPoint, nsiRequest: NsiRequest)(implicit
      r: Request[AnyContent]
  ): Future[Result] =
    val remoteUser = r.headers.get("X-REMOTE-USER")
    val soapRequest = nsiRequest.toNsiEnvelope(remoteUser, endPoint.accessTokens)
    val requestTime = DateTime.now()

    val addHeaders =
      addOauth2Header(endPoint.accessTokens) _ andThen addSoapActionHeader(nsiRequest.soapAction)
    val wsRequest = addHeaders(
      ws.url(endPoint.provider.providerUrl.toASCIIString()).withFollowRedirects(false)
    )

    wsRequest
      .post(soapRequest)
      .map { response =>
        logger.debug(
          s"Provider (${endPoint.provider.providerUrl}) response: ${response.status}, ${response.statusText}"
        )

        if response.header(CONTENT_TYPE).exists(_ contains ContentTypeSoap11) then
          val jsonResponse =
            JsonResponse.success(soapRequest, requestTime, response.xml, DateTime.now())
          Ok(jsonResponse)
        else
          val message =
            s"Failed: ${response.status} (${response.statusText}), ${response.header(CONTENT_TYPE).getOrElse("No content type header found")}"
          BadRequest(JsonResponse.failure(soapRequest, requestTime, message))
      }
      .recover { case e =>
        logger.info("Could not send soap request", e)
        BadRequest(Json.obj("message" -> e.getMessage))
      }
  end sendEnvelope

  private def addSoapActionHeader(action: String)(request: WSRequest): WSRequest =
    request.addHttpHeaders("SOAPAction" -> s""""$action"""")

  private def addOauth2Header(tokens: List[String])(request: WSRequest): WSRequest =
    if tokens.isEmpty
    then request
    else request.addHttpHeaders("Authorization" -> s"bearer ${tokens.head}")

  private def generateCorrelationId = UUID.randomUUID.toString

  private implicit class Mappings(endPoint: EndPoint)(implicit request: Request[AnyContent]):

    private def listWithoutEmptyStrings: Mapping[List[String]] =
      list(text).transform(_.filterNot(_.isEmpty), identity)

    def reserveF: Form[Reserve] = Form(
      "reservation" -> mapping(
        "description" -> optional(text),
        "startDate" -> optional(date("yyyy-MM-dd HH:mm")),
        "endDate" -> date("yyyy-MM-dd HH:mm"),
        "serviceType" -> nonEmptyText,
        "source" -> portMapping,
        "destination" -> portMapping,
        "ero" -> list(text),
        "bandwidth" -> longNumber(0, 100000),
        "version" -> number(min = 0),
        "correlationId" -> nonEmptyText,
        "globalReservationId" -> optional(text),
        "unprotected" -> boolean,
        "pathComputationAlgorithm" -> optional(nonEmptyText)
      )(
        (
            desc,
            start,
            end,
            serviceType,
            source,
            dest,
            ero,
            bandwidth,
            version,
            correlationId,
            globalReservationId,
            unProtected,
            pathComputationAlgorithm
        ) =>
          Reserve(
            desc,
            start,
            end,
            serviceType,
            source,
            dest,
            ero,
            bandwidth,
            version,
            correlationId,
            Some(ReplyToUrl),
            RequesterNsa,
            endPoint.provider,
            globalReservationId,
            unProtected,
            pathComputationAlgorithm
          )
      )(reserve =>
        Some(
          (
            reserve.description,
            reserve.startDate,
            reserve.endDate,
            reserve.serviceType,
            reserve.source,
            reserve.destination,
            reserve.ero,
            reserve.bandwidth,
            reserve.version,
            reserve.correlationId,
            reserve.globalReservationId,
            reserve.unprotected,
            reserve.pathComputationAlgorithm
          )
        )
      )
    )

    def modifyF: Form[ReserveModify] = Form(
      "reservation" -> mapping(
        "connectionId" -> text,
        "startDate" -> optional(date("yyyy-MM-dd HH:mm")),
        "startNow" -> boolean,
        "endDate" -> optional(date("yyyy-MM-dd HH:mm")),
        "indefiniteEnd" -> boolean,
        "bandwidth" -> optional(longNumber(0, 100000)),
        "version" -> number(min = 0),
        "correlationId" -> nonEmptyText
      )((connectionId, start, startNow, end, indefiniteEnd, bandwidth, version, correlationId) =>
        ReserveModify(
          connectionId,
          start,
          startNow,
          end,
          indefiniteEnd,
          bandwidth,
          version,
          correlationId,
          Some(ReplyToUrl),
          RequesterNsa,
          endPoint.provider
        )
      )(modify =>
        Some(
          (
            modify.connectionId,
            modify.startDate,
            modify.startNow,
            modify.endDate,
            modify.indefiniteEnd,
            modify.bandwidth,
            modify.version,
            modify.correlationId
          )
        )
      )
    )

    def reserveAbortF: Form[ReserveAbort] = Form(
      "reserveAbort" -> genericOperationMapping(ReserveAbort.apply)(Tuple.fromProductTyped)
    )

    def reserveCommitF: Form[ReserveCommit] = Form(
      "reserveCommit" -> genericOperationMapping(ReserveCommit.apply)(Tuple.fromProductTyped)
    )

    def provisionF: Form[Provision] = Form(
      "provision" -> genericOperationMapping(Provision.apply)(Tuple.fromProductTyped)
    )

    def terminateF: Form[Terminate] = Form(
      "terminate" -> genericOperationMapping(Terminate.apply)(Tuple.fromProductTyped)
    )

    def releaseF: Form[Release] = Form(
      "release" -> genericOperationMapping(Release.apply)(Tuple.fromProductTyped)
    )

    def portMapping: Mapping[Port] =
      mapping("stpId" -> nonEmptyText)(Port.apply)(p => Some(p.stpId))

    private def genericOperationMapping[R](
        apply: (String, String, Option[URI], String, Provider) => R
    )(unapply: R => (String, String, Option[URI], String, Provider)) =
      mapping(
        "connectionId" -> nonEmptyText,
        "correlationId" -> nonEmptyText
      )((connectionId, correlationId) =>
        apply(connectionId, correlationId, Some(ReplyToUrl), RequesterNsa, endPoint.provider)
      ) { go =>
        val tuple = unapply(go)
        Some((tuple._1, tuple._2))
      }

    def queryF: Form[Query] = Form(
      "query" -> mapping(
        "operation" -> of[QueryOperation.Value],
        "connectionIds" -> listWithoutEmptyStrings,
        "globalReservationIds" -> listWithoutEmptyStrings,
        "ifModifiedSince" -> optional(date("yyyy-MM-dd HH:mm")),
        "correlationId" -> nonEmptyText
      )((operation, connectionIds, globalReservationIds, ifModifiedSince, correlationId) =>
        Query(
          operation,
          connectionIds,
          globalReservationIds,
          ifModifiedSince,
          correlationId,
          Some(ReplyToUrl),
          RequesterNsa,
          endPoint.provider
        )
      ) { query =>
        Some(
          (
            query.operation,
            query.connectionIds,
            query.globalReservationIds,
            query.ifModifiedSince,
            query.correlationId
          )
        )
      }
    )

    def queryMessageF: Form[QueryMessage] = Form(
      "queryMessage" -> mapping(
        "operation" -> of[QueryMessageMode.Value],
        "connectionId" -> nonEmptyText,
        "startId" -> optional(of[Long]),
        "endId" -> optional(of[Long]),
        "correlationId" -> nonEmptyText
      )((operation, connectionId, startId, endId, correlationId) =>
        QueryMessage(
          operation,
          connectionId,
          startId,
          endId,
          correlationId,
          Some(ReplyToUrl),
          RequesterNsa,
          endPoint.provider
        )
      ) { queryMessage =>
        Some(
          (
            queryMessage.operation,
            queryMessage.connectionId,
            queryMessage.startId,
            queryMessage.endId,
            queryMessage.correlationId
          )
        )
      }
    )
  end Mappings
end ApplicationController
