package support

import scala.xml.Node
import scala.xml.NodeSeq
import scala.xml.PrettyPrinter

object PrettyXml {

  implicit def nodeToString(xml: Node) = new {
    def prettify(): String = {
      new PrettyPrinter(80, 4).format(xml)
    }
  }

  implicit def nodeSeqToString(xml: NodeSeq) = new {
    def prettify: String = {
      xml.foldLeft("")((a, b) => a + b.prettify)
    }
  }


}