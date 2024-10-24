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

import javax.inject.Singleton
import models.Ack
import org.apache.pekko.stream.*
import org.apache.pekko.stream.scaladsl.*
import org.joda.time.DateTime
import play.api.*
import play.api.http.ContentTypes
import play.api.libs.EventSource
import play.api.libs.json.*
import play.api.mvc.*
import scala.concurrent.stm.TMap
import scala.xml.NodeSeq
import support.JsonResponse

@javax.inject.Singleton
class ResponseController @javax.inject.Inject() (
    val controllerComponents: ControllerComponents,
    requesterSession: RequesterSession
)(using Materializer)
    extends BaseController
    with Soap11Controller:
  private val logger = Logger(classOf[ResponseController])

  type Channel = (Sink[JsValue, ?], Source[JsValue, ?])

  private val channels: TMap.View[String, Channel] = TMap().single

  def register(id: String): Channel = channels.getOrElseUpdate(
    id,
    MergeHub.source[JsValue].toMat(BroadcastHub.sink)(Keep.both).run()
  )

  def reply: Action[NodeSeq] = Action(parse.xml) { request =>
    val correlationId = parseCorrelationId(request.body)
    val providerNsa = parseProviderNsa(request.body)
    val requesterNsa = parseRequesterNsa(request.body)

    correlationId.foreach { id =>
      val clients = channels.get(id).map(Seq(_)).getOrElse {
        logger.info(s"Could not find correlation id $id, sending reply to all clients")
        channels.values
      }

      clients foreach { client =>
        Source.single(JsonResponse.response(request.body, DateTime.now())).runWith(client._1)
      }
    }

    providerNsa
      .flatMap(requesterSession.findProvider)
      .fold(badRequest("Could not find provider nsa")) { provider =>
        correlationId.fold(badRequest("Could not find CorrelationId")) { id =>
          Ok(Ack(id, requesterNsa.getOrElse("not.found.in.request"), provider).toNsiEnvelope())
            .as(ContentTypeSoap11)
        }
      }
  }

  private def badRequest(message: String) = BadRequest(<badRequest>{message}</badRequest>)

  private val CorrelationId = "urn:uuid:(.*)".r

  private def parseCorrelationId(xml: NodeSeq): Option[String] =
    (xml \\ "correlationId").theSeq.headOption.flatMap { correlationId =>
      correlationId.text match
        case CorrelationId(id) => Some(id)
        case _                 => None
    }

  private def parseRequesterNsa(xml: NodeSeq): Option[String] =
    (xml \\ "requesterNSA").headOption.map(_.text)

  private def parseProviderNsa(xml: NodeSeq): Option[String] =
    (xml \\ "providerNSA").headOption.map(_.text)

  def eventSource(id: String): Action[AnyContent] = Action { implicit request =>
    val (_, source) = register(id)
    Ok.chunked(source via EventSource.flow).as(ContentTypes.EVENT_STREAM)
  }
end ResponseController
