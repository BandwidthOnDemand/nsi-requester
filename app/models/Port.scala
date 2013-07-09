package models

case class Port(networkId: String, localId: String) {
  def stpId = s"$networkId:$localId"

  def xmlV2 = {
    <networkId>{ networkId }</networkId>
    <localId>{ localId }</localId>
  }

  def xmlV1 = {
    <stpId>{ s"$networkId:$localId" }</stpId>
  }

//  def xmlLabels = labels.map {
//    case (key, value) => <attribute type="{key}"><value>{value}</value></attribute>
//  }
}