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
import org.joda.time.Period
import scala.xml.NodeSeq.Empty

case class ReserveModify(
    connectionId: String,
    startDate: Option[Date],
    startNow: Boolean,
    endDate: Option[Date],
    indefiniteEnd: Boolean,
    bandwidth: Option[Long],
    version: Int = 1,
    correlationId: String,
    replyTo: Option[URI],
    requesterNsa: String,
    provider: Provider) extends NsiRequest(correlationId, replyTo, requesterNsa, provider, addsTrace = true
) {

  import NsiRequest._

  override def soapActionSuffix = "reserve"

  override def nsiV2Body =
    <type:reserve>
      <connectionId>{ connectionId }</connectionId>
      <criteria version={ version.toString }>
        <schedule>
          { startTimeField }
          { endTimeField }
        </schedule>
        { capacity }
      </criteria>
    </type:reserve>

  private def startTimeField = startDate match {
    case _ if startNow => <startTime xsi:nil="true"/>
    case Some(date)    => <startTime>{ ISODateTimeFormat.dateTime().print(new DateTime(date)) }</startTime>
    case None          => Empty
  }

  private def endTimeField = endDate match {
    case _ if indefiniteEnd => <endTime xsi:nil="true"/>
    case Some(date)         => <endTime>{ ISODateTimeFormat.dateTime().print(new DateTime(date)) }</endTime>
    case None               => Empty
  }

  private def capacity = bandwidth match {
    case Some(capacity) => <p2p:capacity xmlns:p2p={ NsiV2Point2PointNamespace }>{ capacity }</p2p:capacity>
    case None           => Empty
  }
}
