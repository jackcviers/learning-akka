package zzz.akka.avionics

import akka.actor.ActorSystem
import akka.actor.Props

object FlightAttendantPathChecker {
  def main(args: Array[String]) = {
    val system = ActorSystem("PlaneSimulation")
    val lead = system.actorOf(Props(new LeadFlightAttendant with AttendantCreationPolicy), system.settings.config.getString("zzz.akka.avionics.flightcrew.leadAttendantName"))

    Thread.sleep(2000)
    system.shutdown()
  }
}
