package models

case class Provision(connectionId: String, correlationId: String) extends Soapable {

  def toEnvelope(replyTo: String) = { inEnvelope(
    <int:provisionRequest>
       <int:correlationId>{ correlationId }</int:correlationId>
       <int:replyTo>{ replyTo }</int:replyTo>
       <type:provision>
          { nsas }
          <connectionId>{ connectionId }</connectionId>
       </type:provision>
    </int:provisionRequest>)
  }

}
