package controllers

import java.net.URI
import models._
import models.QueryMessageMode.QueryMessageMode
import models.QueryOperation.QueryOperation
import org.joda.time.Period
import org.joda.time.format.PeriodFormatterBuilder
import play.api.data.FormError
import play.api.data.Forms.{mapping, number, optional, text}
import play.api.data.Mapping
import play.api.data.format.Formats.stringFormat
import play.api.data.format.Formatter
import scala.util.Try
import models.NsiVersion

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
      stringFormat.bind(key, data).right.flatMap { s =>
        scala.util.control.Exception.allCatch[Period]
          .either(periodFormatter.parsePeriod(s))
          .left.map(e => Seq(FormError(key, "error.period", Nil)))
      }
    }

    def unbind(key: String, value: Period) = Map(key -> periodFormatter.print(value))
  }

  implicit def queryMessageModeFormat: Formatter[QueryMessageMode] = new Formatter[QueryMessageMode] {
    override val format = Some((s"Allowed values: ${QueryMessageMode.values.mkString(", ")}", Nil))

    def bind(key: String, data: Map[String, String]): Either[Seq[FormError], QueryMessageMode] =
      stringFormat.bind(key, data).right.flatMap { s =>
        scala.util.control.Exception.allCatch[QueryMessageMode]
          .either(QueryMessageMode.withName(s))
          .left.map(e => Seq(FormError(key, "error.queryMessageMode", Nil)))
      }

    def unbind(key: String, value: QueryMessageMode) = Map(key -> value.toString)
  }

  implicit def queryOperationFormat: Formatter[QueryOperation] = new Formatter[QueryOperation] {
    override val format = Some((s"Allowed values: ${QueryOperation.values.mkString(", ")}", Nil))

    def bind(key: String, data: Map[String, String]): Either[Seq[FormError], QueryOperation] =
      stringFormat.bind(key, data).right.flatMap { s =>
        scala.util.control.Exception.allCatch[QueryOperation]
          .either(QueryOperation.withName(s))
          .left.map(e => Seq(FormError(key, "error.queryOperation", Nil)))
      }

    def unbind(key: String, value: QueryOperation) = Map(key -> value.toString)
  }

  val providerMapping: Mapping[Provider] = mapping(
    "providerUrl" -> uri,
    "nsiVersion" -> number.transform[NsiVersion](NsiVersion.fromInt, _.value),
    "username" -> optional(text),
    "password" -> optional(text),
    "accessToken" -> optional(text)
  ){ Provider.apply } { Provider.unapply }

}