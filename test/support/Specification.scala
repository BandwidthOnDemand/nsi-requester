package support

import java.net.URI
import play.api.test.PlaySpecification

abstract class Specification extends org.specs2.mutable.Specification with PlaySpecification {
  def uri(s: String): URI = URI.create(s)
}
