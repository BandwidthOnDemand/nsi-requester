package models

import scala.xml.NodeSeq
import scala.xml.Elem
import scala.xml.NodeSeq

case class Port(networkId: String, localId: String, vlan: Option[Int] = None, labels: Map[String, Seq[String]] = Map.empty) {
  def stpId = s"$networkId:$localId"

  def xmlV2: NodeSeq = {
    <networkId>{ networkId }</networkId>
    <localId>{ localId }</localId> ++ xmlLabels
  }

  private def xmlLabels = {
    val xml = labels.map {
      case (key, values) =>
        <attribute type={key}>
          { values.map(v => <value>{v}</value> ) }
        </attribute>
    }

    xml.toList match {
      case x::xs => <labels>{ xml }</labels>
      case Nil => NodeSeq.Empty
    }
  }

  def xmlV1: NodeSeq =
    <stpId>{ s"$networkId:$localId" }</stpId>
}