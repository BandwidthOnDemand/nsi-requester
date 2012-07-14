package models

case class Terminate(connectionId: String, correlationId: String) extends Soapable {

  def toEnvelope(replyTo: String) = { inEnvelope(
    <int:terminateRequest>
         <int:correlationId>{ correlationId }</int:correlationId>
         <int:replyTo>{ replyTo }</int:replyTo>
         <type:terminate>
            { nsas }
            <connectionId>{ connectionId }</connectionId>
         </type:terminate>
    </int:terminateRequest>)
  }
}
