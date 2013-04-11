package support

import scala.xml.Node
import scala.xml.NodeSeq
import scala.xml.PrettyPrinter

object PrettyXml {

  implicit class nodeSeqToString(xml: NodeSeq) {

    val pp = new PrettyPrinter(80, 4)

    def prettify: String = xml match {
        case x: Node => pp.format(x);
        case x: NodeSeq => xml.foldLeft("") { (str, node) =>
          val sep = if (str.isEmpty) "" else "\n"
          str + sep + node.prettify
        }
      }
  }

}