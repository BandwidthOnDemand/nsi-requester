package models

import java.util.Date

case class Reservation(description: String, startDate: Date, endDate: Date, connectionId: String, source: String = "", destination: String = "")