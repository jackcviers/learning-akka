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

  val altimeter = context.actorOf(Props(Altimeter()), "Altimeter")
  val controls = context.actorOf(Props(ControlSurfaces(altimeter)), "ControlSurfaces")

  def receive = {
    case AltitudeUpdate(altitudeInFeet) ⇒ log info (s"Altitude is now: $altitudeInFeet")
    case GiveMeControl ⇒
      log info ("Plane giving control.")
      sender ! controls

  }

  override def preStart() {
    altimeter ! RegisterListener(self)
  }

}

trait PlaneProviderComponent {
  def plane: ActorRef
}
