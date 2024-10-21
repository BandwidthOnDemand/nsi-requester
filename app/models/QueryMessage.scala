/*
 * Copyright (c) 2012, 2013, 2014, 2015, 2016 SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *     disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package models

import java.net.URI
import scala.xml.NodeSeq.Empty
import scala.xml.Node

object QueryMessageMode extends Enumeration:
  type QueryMessageMode = Value
  val NotificationSync, NotificationAsync, ResultSync, ResultAsync = Value

import QueryMessageMode.*

case class QueryMessage(
    operation: QueryMessageMode,
    connectionId: String,
    startId: Option[Long],
    endId: Option[Long],
    correlationId: String,
    replyTo: Option[URI],
    requesterNsa: String,
    provider: Provider
) extends NsiRequest():

  override def soapActionSuffix: String = operation match
    case NotificationSync  => "queryNotificationSync"
    case NotificationAsync => "queryNotification"
    case ResultSync        => "queryResultSync"
    case ResultAsync       => "queryResult"

  override def nsiV2Body: Node =
    operation match
      case NotificationAsync =>
        <type:queryNotification>
           {queryBody}
         </type:queryNotification>
      case NotificationSync =>
        <type:queryNotificationSync>
           {queryBody}
        </type:queryNotificationSync>
      case ResultAsync =>
        <type:queryResult>
           {queryBody}
        </type:queryResult>
      case ResultSync =>
        <type:queryResultSync>
           {queryBody}
        </type:queryResultSync>

  private def queryBody = List(
    <connectionId>{connectionId}</connectionId>,
    { startIdTag },
    { endIdTag }
  )

  private def startIdTag = operation match
    case NotificationSync | NotificationAsync =>
      startId.map(id => <startNotificationId>{id}</startNotificationId>).getOrElse(Empty)
    case ResultSync | ResultAsync =>
      startId.map(id => <startResultId>{id}</startResultId>).getOrElse(Empty)

  private def endIdTag = operation match
    case NotificationSync | NotificationAsync =>
      endId.map(id => <endNotificationId>{id}</endNotificationId>).getOrElse(Empty)
    case ResultSync | ResultAsync =>
      endId.map(id => <endResultId>{id}</endResultId>).getOrElse(Empty)
end QueryMessage
