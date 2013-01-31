package controllers

import play.api.data.format.Formatter
import org.joda.time.LocalDateTime
import org.joda.time.Period
import org.joda.time.format.PeriodFormatterBuilder
import play.api.data._
import play.api.data.format.Formats._

object FormSupport {

  private val periodFormatter = new PeriodFormatterBuilder()
    .printZeroAlways()
    .appendDays().appendSuffix(":")
    .appendHours().appendSuffix(":")
    .appendMinutes()
    .toFormatter

  implicit def periodFormat: Formatter[Period] = new Formatter[Period] {

    override val format = Some("Period ('days:hours:minuts')", Nil)

    def bind(key: String, data: Map[String, String]) = {
      stringFormat.bind(key, data).right.flatMap { s =>
        scala.util.control.Exception.allCatch[Period]
          .either(periodFormatter.parsePeriod(s))
          .left.map(e => Seq(FormError(key, "error.period", Nil)))
      }
    }

    def unbind(key: String, value: Period) = Map(key -> periodFormatter.print(value))
  }
}
