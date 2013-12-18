package models

import scala.xml.NodeSeq
import scala.xml.Elem
import scala.xml.NodeSeq

case class Port(stpId: String) {

  def xmlV1: NodeSeq =
    <stpId>{ stpId }</stpId>
}