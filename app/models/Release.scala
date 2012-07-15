package models

case class Release(connectionId: String, correlationId: String) extends Soapable {

  def toEnvelope(replyTo: String) = inEnvelope(
    <int:releaseRequest>
      <int:correlationId>{ correlationId }</int:correlationId>
      <int:replyTo>{ replyTo }</int:replyTo>
      <type:release>
        { nsas }
        <connectionId>{ connectionId }</connectionId>
      </type:release>
    </int:releaseRequest>
  )
}
