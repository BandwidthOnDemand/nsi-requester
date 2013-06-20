package controllers

import org.joda.time.Period
import org.joda.time.format.PeriodFormatterBuilder

import models.Provider
import models.QueryNotificationOperation._
import models.QueryNotificationOperation
import models.QueryOperation._
import models.QueryOperation

import play.api.data.FormError
import play.api.data.Forms._
import play.api.data.Mapping
import play.api.data.format.Formats.stringFormat
import play.api.data.format.Formatter

object FormSupport {

  private val periodFormatter = new PeriodFormatterBuilder()
    .printZeroAlways()
    .appendDays().appendSuffix(":")
    .appendHours().appendSuffix(":")
    .appendMinutes()
    .toFormatter

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

  implicit def queryNotificationOperationFormat: Formatter[QueryNotificationOperation] = new Formatter[QueryNotificationOperation] {
    override val format = Some((s"Allowed values: ${QueryNotificationOperation.values.mkString(", ")}", Nil))

    def bind(key: String, data: Map[String, String]): Either[Seq[FormError], QueryNotificationOperation] =
      stringFormat.bind(key, data).right.flatMap { s =>
        scala.util.control.Exception.allCatch[QueryNotificationOperation]
          .either(QueryNotificationOperation.withName(s))
          .left.map(e => Seq(FormError(key, "error.queryNotificationOperation", Nil)))
      }

    def unbind(key: String, value: QueryNotificationOperation) = Map(key -> value.toString)
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
    "providerUrl" -> nonEmptyText,
    "nsiVersion" -> number,
    "username" -> optional(text),
    "password" -> optional(text),
    "accessToken" -> optional(text)
  ){ Provider.apply } { Provider.unapply }

}