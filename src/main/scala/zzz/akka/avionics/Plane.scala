package zzz.akka.avionics

import akka.actor.{ Actor, ActorLogging, ActorRef, Props }


object Plane {
  case object GiveMeControl
  case class Controls(controls: ActorRef)
}

class Plane extends Actor with ActorLogging {
  import EventSource._
  import Altimeter._
  import ControlSurfaces._
  import Plane._
  import Pilot._

  val configKey = "zzz.akka.avionics.flightcrew"
  val altimeter = context.actorOf(Props(Altimeter()), "Altimeter")
  val controls = context.actorOf(Props(ControlSurfaces(altimeter)), "ControlSurfaces")
  val config = context.system.settings.config
  val pilot = context.actorOf(Props(Pilot()), config.getString(s"$configKey.pilotName"))
  val copilot = context.actorOf(Props(Copilot()), config.getString(s"$configKey.copilotName"))
  val leadFlightAttendant = context.actorOf(Props(LeadFlightAttendant()), config.getString(s"$configKey.leadAttendantName"))
  val autopilot = context.actorOf(Props[Autopilot], "Autopilot")

  def receive = {
    case AltitudeUpdate(altitudeInFeet) ⇒ log debug (s"Altitude is now: $altitudeInFeet")
    case GiveMeControl ⇒
      log info ("Plane giving control.")
      sender ! Controls(controls)

  }

  override def preStart() {
    altimeter ! RegisterListener(self)
    pilot :: copilot :: Nil foreach { _ ! ReadyToGo }
  }

}

trait PlaneProviderComponent {
  def plane: ActorRef
}
