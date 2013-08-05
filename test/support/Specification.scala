package support

import java.net.URI

abstract class Specification extends org.specs2.mutable.Specification {
  def uri(s: String): URI = URI.create(s)
}
