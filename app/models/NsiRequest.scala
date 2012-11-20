package models

abstract class NsiRequest(correlationId: String, replyTo: String, providerNsa: String) extends Soapable {

  protected def nsas = {
    <requesterNSA>urn:ogf:network:nsa:surfnet-nsi-requester</requesterNSA>
    <providerNSA>{ providerNsa }</providerNSA>
  }

  protected def nsiRequestFields = {
    <int:correlationId>{ correlationId }</int:correlationId>
    <int:replyTo>{ replyTo }</int:replyTo>
  }
}
