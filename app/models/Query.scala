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
import java.util.Date
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import scala.xml.NodeSeq.Empty

object QueryOperation extends Enumeration {
  type QueryOperation = Value
  val Summary, SummarySync, Details, Recursive = Value

  def operationsV1 = List(Summary, Details)
  def operationsV2 = List(Summary, SummarySync, Recursive)
}

import QueryOperation._

case class Query(
    operation: QueryOperation = Summary,
    connectionIds: List[String],
    globalReservationIds: List[String],
    ifModifiedSince: Option[Date],
    correlationId: String,
    replyTo: Option[URI],
    requesterNsa: String,
    provider: Provider) extends NsiRequest(correlationId, replyTo, requesterNsa, provider) {

  override def soapActionSuffix = operation match {
    case Summary     => "querySummary"
    case SummarySync => "querySummarySync"
    case Recursive   => "queryRecursive"
  }

  override def nsiV2Body = operation match {
    case Summary =>
      <type:querySummary>
        { connectionIdTags }
        { globalReservationIdTags }
        { ifModifiedSinceTag }
      </type:querySummary>
    case SummarySync =>
      <type:querySummarySync>
        { connectionIdTags }
        { globalReservationIdTags }
      </type:querySummarySync>
    case Recursive =>
      <type:queryRecursive>
        { connectionIdTags }
        { globalReservationIdTags }
      </type:queryRecursive>
    case _ =>
      sys.error(s"Unsupported NSI v2 query type '$operation'")
  }

  private def connectionIdTags =
    connectionIds.map(id => <connectionId>{ id }</connectionId>)

  private def globalReservationIdTags =
    globalReservationIds.map(id => <globalReservationId>{ id }</globalReservationId>)

  private def ifModifiedSinceTag = ifModifiedSince match {
    case Some(date) => <ifModifiedSince>{ ISODateTimeFormat.dateTime().print(new DateTime(date)) }</ifModifiedSince>
    case None => Empty
  }

}
