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

import java.net.URI
import models.QueryMessageMode.QueryMessageMode
import models.QueryOperation.QueryOperation
import models._
import org.joda.time.Period
import org.joda.time.format.PeriodFormatterBuilder
import play.api.data.FormError
import play.api.data.Forms.text
import play.api.data.Mapping
import play.api.data.format.Formats.stringFormat
import play.api.data.format.Formatter
import scala.util.Try

object FormSupport {

  private val periodFormatter = new PeriodFormatterBuilder()
    .printZeroAlways()
    .appendDays().appendSuffix(":")
    .appendHours().appendSuffix(":")
    .appendMinutes()
    .toFormatter

  def uri: Mapping[URI] = text.verifying("not a valid URI", s => Try(URI.create(s)).isSuccess).transform[URI](URI.create, _.toString).
    verifying("URI must be HTTP or HTTPS", uri => Option(uri.getScheme).getOrElse("").toLowerCase() match {
      case "http" | "https" => true
      case _                => false
    })


  implicit def periodFormat: Formatter[Period] = new Formatter[Period] {
    override val format = Some(("Period ('days:hours:minutes')", Nil))

    def bind(key: String, data: Map[String, String]) = {
      stringFormat.bind(key, data).flatMap { s =>
        scala.util.control.Exception.allCatch[Period]
          .either(periodFormatter.parsePeriod(s))
          .left.map(_ => Seq(FormError(key, "error.period", Nil)))
      }
    }

    def unbind(key: String, value: Period) = Map(key -> periodFormatter.print(value))
  }

  implicit def queryMessageModeFormat: Formatter[QueryMessageMode] = new Formatter[QueryMessageMode] {
    override val format = Some((s"Allowed values: ${QueryMessageMode.values.mkString(", ")}", Nil))

    def bind(key: String, data: Map[String, String]): Either[Seq[FormError], QueryMessageMode] =
      stringFormat.bind(key, data).flatMap { s =>
        scala.util.control.Exception.allCatch[QueryMessageMode]
          .either(QueryMessageMode.withName(s))
          .left.map(_ => Seq(FormError(key, "error.queryMessageMode", Nil)))
      }

    def unbind(key: String, value: QueryMessageMode) = Map(key -> value.toString)
  }

  implicit def queryOperationFormat: Formatter[QueryOperation] = new Formatter[QueryOperation] {
    override val format = Some((s"Allowed values: ${QueryOperation.values.mkString(", ")}", Nil))

    def bind(key: String, data: Map[String, String]): Either[Seq[FormError], QueryOperation] =
      stringFormat.bind(key, data).flatMap { s =>
        scala.util.control.Exception.allCatch[QueryOperation]
          .either(QueryOperation.withName(s))
          .left.map(_ => Seq(FormError(key, "error.queryOperation", Nil)))
      }

    def unbind(key: String, value: QueryOperation) = Map(key -> value.toString)
  }
}
