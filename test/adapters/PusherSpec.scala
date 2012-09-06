package adapters;

import org.specs2.mutable.Specification
import play.api.test.Helpers._
import play.api.test.FakeApplication
import java.security.MessageDigest

@org.junit.runner.RunWith(classOf[org.specs2.runner.JUnitRunner])
class PusherSpec extends Specification {

  "SecurityString" should {
    import SecurityString._

    "have a valid md5 hash for empty string" in {
      "".md5 must beEqualTo("d41d8cd98f00b204e9800998ecf8427e")
    }
    "have a valid md5 hash for a string" in {
      "The quick brown fox jumps over the lazy dog".md5() must beEqualTo("9e107d9d372bb6826bd81d3542a419d6")
    }
    "have a valid sha256 hash for a string" in {
      "The quick brown fox jumps over the lazy dog".sha256("key") must beEqualTo("f7bc83f430538424b13298e6aa6fb143ef4d59a14946175997479dbc2d1a3cd8")
    }
  }
}
