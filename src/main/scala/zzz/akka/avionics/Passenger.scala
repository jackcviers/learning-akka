package zzz.akka.avionics

object Passenger {
  case object FastenSeatbelts
  case object UnfastenSeatbelts

  val SeatAssignment = """([\w\s_]+)-(\d+)-([A-Z])""".r
}



