package models

import SoapHelper._

case class Provision(connectionId: String, correlationId: String) {
  def inEnvelope = <test></test>

  def toEnvelope(replyTo: String) = {

    inEnveloppe(
      <int:provisionRequest>
         <int:correlationId>{ correlationId }</int:correlationId>
         <int:replyTo>{ replyTo }</int:replyTo>
         <type:provision>
            { nsas }
            <connectionId>{ connectionId }</connectionId>
         </type:provision>
      </int:provisionRequest>
    )
  }

}
