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
import org.joda.time.DateTime
import play.api.data.{ Form, FormError, Mapping }
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.libs.ws.{WS, WSRequestHolder}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._
import play.api.mvc._
import play.api.Play.current
import support.JsonResponse
import models._
import FormSupport._
import RequesterSession._
import java.net.URI
import play.api.Logger
import scala.concurrent.Future

object Application extends Controller with Soap11Controller {

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
    val endDate = startDate.plusMinutes(15)

    val defaultForm = currentEndPoint.reserveF.fill(
      Reserve(
        description = Some("A NSI reserve test"),
        startDate = Some(startDate.toDate),
        endDate = endDate.toDate,
        correlationId = generateCorrelationId,
        serviceType = ServiceType,
        source = Port(currentPortPrefix),
        destination = Port(currentPortPrefix),
        ero = List(),
        bandwidth = 100,
        replyTo = Some(ReplyToUrl),
        requesterNsa = RequesterNsa,
        provider = currentEndPoint.provider)
    )

    Ok(views.html.reserve(defaultForm))
  }

  def reserve = Action.async { implicit request =>
    currentEndPoint.reserveF.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(Json.toJson(formWithErrors.errors))),
      reservation => sendEnvelope(currentEndPoint, reservation))
  }

  def reserveModifyForm = Action { implicit request =>
    val startDate = None
    val endDate = None

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
        provider = currentEndPoint.provider)
    )

    Ok(views.html.reserveModify(defaultForm))
  }

  def reserveModify = Action.async { implicit request =>
    currentEndPoint.modifyF.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(Json.toJson(formWithErrors.errors))),
      modify => sendEnvelope(currentEndPoint, modify))
  }

  def reserveCommitForm = Action { implicit request =>
    val defaultForm = currentEndPoint.reserveCommitF.fill(
      ReserveCommit(connectionId = "", correlationId = generateCorrelationId, replyTo = Some(ReplyToUrl), requesterNsa = RequesterNsa, provider = currentEndPoint.provider))

    Ok(views.html.reserveCommit(defaultForm))
  }

  def reserveCommit = Action.async { implicit request =>
    currentEndPoint.reserveCommitF.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(Json.toJson(formWithErrors.errors))),
      { case reserveCommit => sendEnvelope(currentEndPoint, reserveCommit) })
  }

  def reserveAbortForm = Action { implicit request =>
    val defaultForm = currentEndPoint.reserveAbortF.fill(
      ReserveAbort(connectionId = "", correlationId = generateCorrelationId, replyTo = Some(ReplyToUrl), requesterNsa = RequesterNsa, provider = currentEndPoint.provider))

    Ok(views.html.reserveAbort(defaultForm))
  }

  def reserveAbort = Action.async { implicit request =>
    currentEndPoint.reserveAbortF.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(Json.toJson(formWithErrors.errors))),
      { case reserveAbort => sendEnvelope(currentEndPoint, reserveAbort) })
  }

  def provisionForm = Action { implicit request =>
    val defaultForm = currentEndPoint.provisionF.fill(
      Provision(connectionId = "", correlationId = generateCorrelationId, replyTo = Some(ReplyToUrl), requesterNsa = RequesterNsa, provider = currentEndPoint.provider))

    Ok(views.html.provision(defaultForm))
  }

  def provision = Action.async { implicit request =>
    currentEndPoint.provisionF.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(Json.toJson(formWithErrors.errors))),
      { case provision => sendEnvelope(currentEndPoint, provision) })
  }

  def terminateForm = Action { implicit request =>
    val defaultForm = currentEndPoint.terminateF.fill(
      Terminate(connectionId = "", correlationId = generateCorrelationId, replyTo = Some(ReplyToUrl), requesterNsa = RequesterNsa, provider = currentEndPoint.provider))

    Ok(views.html.terminate(defaultForm))
  }

  def terminate = Action.async { implicit request =>
    currentEndPoint.terminateF.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(Json.toJson(formWithErrors.errors))),
      { case terminate => sendEnvelope(currentEndPoint, terminate) })
  }

  def releaseForm = Action { implicit request =>
    val defaultForm = currentEndPoint.releaseF.fill(
      Release(connectionId = "", correlationId = generateCorrelationId, replyTo = Some(ReplyToUrl), requesterNsa = RequesterNsa, provider = currentEndPoint.provider))

    Ok(views.html.release(defaultForm))
  }

  def release = Action.async { implicit request =>
    currentEndPoint.releaseF.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(Json.toJson(formWithErrors.errors))),
      { case release => sendEnvelope(currentEndPoint, release) })
  }

  def queryForm = Action { implicit request =>
    val defaultForm = currentEndPoint.queryF.fill(
      Query(QueryOperation.Summary, Nil, Nil, generateCorrelationId, replyTo = Some(ReplyToUrl), requesterNsa = RequesterNsa, provider = currentEndPoint.provider))

    Ok(views.html.query(defaultForm))
  }

  def query = Action.async { implicit request =>
    currentEndPoint.queryF.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(Json.toJson(formWithErrors.errors))),
      { case query => sendEnvelope(currentEndPoint, query) })
  }

  def queryMessageForm = Action { implicit request =>
    val defaultForm = currentEndPoint.queryMessageF.fill(
      QueryMessage(QueryMessageMode.ResultAsync, "", None, None, generateCorrelationId, Some(ReplyToUrl), requesterNsa = RequesterNsa, provider = currentEndPoint.provider))

    Ok(views.html.queryMessage(defaultForm))
  }

  def queryMessage = Action.async { implicit request =>
    currentEndPoint.queryMessageF.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(Json.toJson(formWithErrors.errors))),
      { case queryMessage => sendEnvelope(currentEndPoint, queryMessage) })
  }

  def validateProvider = Action.async(parse.json) { implicit request =>

    val wsdlValid =
      for {
        nsaId <- (request.body \ "nsa-id").asOpt[String]
        provider <- findProvider(nsaId)
      } yield {
        val wsdlRequest = WS.url(s"${provider.providerUrl}?wsdl").withFollowRedirects(false)

        wsdlRequest.get.map { wsdlResponse =>
          val response = if (wsdlResponse.status == 200) Json.obj("valid" -> true) else Json.obj("valid" -> false, "message" -> wsdlResponse.status)
          Ok(response)
        }
      }

    wsdlValid.getOrElse(Future.successful(BadRequest))
  }

  private def sendEnvelope(endPoint: EndPoint, nsiRequest: NsiRequest)(implicit r: Request[AnyContent]): Future[Result] = {
    val remoteUser = r.headers.get("X-REMOTE-USER")
    val soapRequest = nsiRequest.toNsiEnvelope(remoteUser, endPoint.accessTokens)
    val requestTime = DateTime.now()

    val addHeaders = addOauth2Header(endPoint.accessTokens) _ andThen addSoapActionHeader(nsiRequest.soapAction)
    val wsRequest = addHeaders(WS.url(endPoint.provider.providerUrl.toASCIIString()).withFollowRedirects(false))

    wsRequest.post(soapRequest).map { response =>
      Logger.debug(s"Provider (${endPoint.provider.providerUrl}) response: ${response.status}, ${response.statusText}")

      if (response.header(CONTENT_TYPE).exists(_ contains ContentTypeSoap11)) {
        val jsonResponse = JsonResponse.success(soapRequest, requestTime, response.xml, DateTime.now())
        Ok(jsonResponse)
      } else {
        val message = s"Failed: ${response.status} (${response.statusText}), ${response.header(CONTENT_TYPE).getOrElse("No content type header found")}"
        BadRequest(JsonResponse.failure(soapRequest, requestTime, message))
      }
    }.recover {
      case e =>
        Logger.info("Could not send soap request", e)
        BadRequest(Json.obj("message" -> e.getMessage))
    }
  }

  private def addSoapActionHeader(action: String)(request: WSRequestHolder): WSRequestHolder =
    request.withHeaders("SOAPAction" -> s""""$action"""")

  private def addOauth2Header(tokens: List[String])(request: WSRequestHolder): WSRequestHolder =
    if (tokens.isEmpty) request else request.withHeaders("Authorization" -> s"bearer ${tokens.head}")

  private def generateCorrelationId = UUID.randomUUID.toString

  private implicit class Mappings(endPoint: EndPoint)(implicit request: Request[AnyContent]) {

    private def replyTo: Mapping[Option[URI]] = optional(uri).verifying("replyTo address is required for NSIv1", uri => true)
    private def listWithoutEmptyStrings: Mapping[List[String]] = list(text).transform(_.filterNot(_.isEmpty), identity)

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
        "pathComputationAlgorithm" -> optional(nonEmptyText))
        ((desc, start, end, serviceType, source, dest, ero, bandwidth, version, correlationId, globalReservationId, unProtected, pathComputationAlgorithm) =>
          Reserve(desc, start, end, serviceType, source, dest, ero, bandwidth, version, correlationId, Some(ReplyToUrl), RequesterNsa, endPoint.provider, globalReservationId, unProtected, pathComputationAlgorithm))
        (reserve =>
          Some((reserve.description, reserve.startDate, reserve.endDate, reserve.serviceType, reserve.source, reserve.destination, reserve.ero, reserve.bandwidth, reserve.version, reserve.correlationId, reserve.globalReservationId, reserve.unprotected, reserve.pathComputationAlgorithm))))

    def modifyF: Form[ReserveModify] = Form(
      "reservation" -> mapping(
        "connectionId" -> text,
        "startDate" -> optional(date("yyyy-MM-dd HH:mm")),
        "startNow" -> boolean,
        "endDate" -> optional(date("yyyy-MM-dd HH:mm")),
        "indefiniteEnd" -> boolean,
        "bandwidth" -> optional(longNumber(0, 100000)),
        "version" -> number(min = 0),
        "correlationId" -> nonEmptyText)((connectionId, start, startNow, end, indefiniteEnd, bandwidth, version, correlationId) =>
          ReserveModify(connectionId, start, startNow, end, indefiniteEnd, bandwidth, version, correlationId, Some(ReplyToUrl), RequesterNsa, endPoint.provider))(modify =>
          Some((modify.connectionId, modify.startDate, modify.startNow, modify.endDate, modify.indefiniteEnd, modify.bandwidth, modify.version, modify.correlationId))))

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

    def portMapping = mapping("stpId" -> nonEmptyText)(Port.apply)(Port.unapply)

    private def portLabelsMap: Mapping[Map[String, Seq[String]]] = list(text).transform(ls => ls.map(_.trim).filter(_.nonEmpty).map { label =>
      val parts = label.split(":")
      parts.head -> parts.tail.mkString(":").split(",").map(_.trim).toSeq
    }.toMap, ls => ls.map { case (key, values) => s"$key: " + values.mkString(", ") }.toList)

    private def genericOperationMapping[R](apply: (String, String, Option[URI], String, Provider) => R)(unapply: R => Option[(String, String, Option[URI], String, Provider)]) =
      mapping(
        "connectionId" -> nonEmptyText,
        "correlationId" -> nonEmptyText)((connectionId, correlationId) => apply(connectionId, correlationId, Some(ReplyToUrl), RequesterNsa, endPoint.provider)){ go =>
          val tuple = unapply(go).get
          Some((tuple._1, tuple._2))
        }

    def queryF: Form[Query] = Form(
      "query" -> mapping(
        "operation" -> of[QueryOperation.Value],
        "connectionIds" -> listWithoutEmptyStrings,
        "globalReservationIds" -> listWithoutEmptyStrings,
        "correlationId" -> nonEmptyText)((operation, connectionIds, globalReservationIds, correlationId) => Query(operation, connectionIds, globalReservationIds, correlationId, Some(ReplyToUrl), RequesterNsa, endPoint.provider)){ query =>
          Some((query.operation, query.connectionIds, query.globalReservationIds, query.correlationId))
        }
      )

    def queryMessageF: Form[QueryMessage] = Form(
      "queryMessage" -> mapping(
        "operation" -> of[QueryMessageMode.Value],
        "connectionId" -> nonEmptyText,
        "startId" -> optional(of[Long]),
        "endId" -> optional(of[Long]),
        "correlationId" -> nonEmptyText)((operation, connectionId, startId, endId, correlationId) => QueryMessage(operation, connectionId, startId, endId, correlationId, Some(ReplyToUrl), RequesterNsa, endPoint.provider)){ queryMessage =>
          Some((queryMessage.operation, queryMessage.connectionId, queryMessage.startId, queryMessage.endId, queryMessage.correlationId))
        }
      )
  }
}
