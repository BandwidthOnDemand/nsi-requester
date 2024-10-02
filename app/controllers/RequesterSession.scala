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

import com.typesafe.config.{ConfigList, ConfigObject, ConfigValue}
import java.net.URI
import models._
import play.api.Configuration
import play.api.mvc.AnyContent
import play.api.mvc.Request
import scala.jdk.CollectionConverters._

object RequesterSession {
  val ProviderNsaSessionField = "nsaId"
  val AccessTokensSessionField = "accessTokens"
  val ServiceType = "http://services.ogf.org/nsi/2013/12/descriptions/EVTS.A-GOLE"
}
@javax.inject.Singleton
class RequesterSession @javax.inject.Inject() (configuration: Configuration) {
  import RequesterSession._

  val RequesterNsa = configuration
    .getOptional[String]("requester.nsi.requesterNsa")
    .getOrElse(sys.error("Requester NSA is not configured (requester.nsi.requesterNsa)"))

  def currentPortPrefix(implicit request: Request[AnyContent]): String = currentProvider.portPrefix

  def currentProvider(implicit request: Request[AnyContent]): Provider =
    request.session.get(ProviderNsaSessionField) flatMap findProvider getOrElse allProviders.head

  def currentEndPoint(implicit request: Request[AnyContent]): EndPoint = {
    request.session.get("accessTokens") match {
      case None                 => EndPoint(currentProvider, List())
      case Some(commaSeparated) => EndPoint(currentProvider, commaSeparated.split(",").toList)
    }
  }

  def ReplyToUrl(implicit request: Request[AnyContent]) =
    URI.create(routes.ResponseController.reply.absoluteURL(isUsingSsl))

  private def isUsingSsl(implicit request: Request[AnyContent]) = (
    request.headers.get("X-Forwarded-Proto") == Some("https")
      || configuration.getOptional[Boolean]("requester.ssl") == Some(true)
  )

  // is not a lazy val, because some tests will break (object will only be initialized once during tests
  def allProviders: Seq[Provider] = {
    def toProvider(value: ConfigValue): Provider = value match {
      case providerObject: ConfigObject =>
        Provider(
          providerObject.get("id").unwrapped().asInstanceOf[String],
          URI.create(providerObject.get("url").unwrapped().asInstanceOf[String]),
          providerObject.get("portPrefix").unwrapped().asInstanceOf[String]
        )
      case _ =>
        sys.error(s"bad provider configuration $value")
    }

    configuration
      .getOptional[ConfigList]("requester.nsi.providers")
      .map(x => x.iterator.asScala.map(toProvider).toSeq)
      .getOrElse(sys.error("No NSI providers where configured (requester.ns.providers)"))
  }

  def findProvider(nsaId: String) = allProviders.find(_.nsaId == nsaId)
}
