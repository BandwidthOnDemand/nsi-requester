package support

import org.junit.runner.RunWith

@RunWith(classOf[org.specs2.runner.JUnitRunner])
class PrettyXmlSpec extends Specification {

  import PrettyXml.*

  "Pretty XML" should {

    "pretty print xml" in {
      val xml = <test><pretty><print/></pretty></test>

      xml.prettify must equalTo("""<test>
            |    <pretty>
            |        <print/>
            |    </pretty>
            |</test>""".stripMargin)
    }

    "pretty print xml node seq" in {
      val xml = <test><pretty><print1/></pretty><pretty><print2/></pretty></test> \\ "pretty"

      xml.prettify must equalTo("""<pretty>
            |    <print1/>
            |</pretty>
            |<pretty>
            |    <print2/>
            |</pretty>""".stripMargin)
    }
  }
}
