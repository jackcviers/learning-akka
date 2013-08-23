package zzz.akka.avionics

import akka.actor.{ Actor, ActorLogging, ActorRef }

class Pilot(
  plane: ActorRef,
  autopilot: ActorRef,
  var controls: ActorRef,
  altimeter: ActorRef) extends Actor {
  import Pilot._
  import Plane.{ Controls, GiveMeControl }

  var copilot = context.system.actorFor("/deadLetters")

  val copilotName = context
    .system
    .settings
    .config
    .getString("zzz.akka.avionics.flightcrew.copilotName")

  def receive = {
    case ReadyToGo ⇒
      plane ! GiveMeControl
      copilot = context.actorFor("../" + copilotName)
    case Controls(controlSurfaces) ⇒
      controls = controlSurfaces
    case m ⇒ throw new Exception(s"Pilot doesn't understand: $m")
  }
}

object Pilot {
  case object ReadyToGo
  def apply(
    plane: ActorRef,
    autopilot: ActorRef,
    controls: ActorRef,
    altimeter: ActorRef) = new Pilot(plane, autopilot, controls, altimeter)
}

trait PilotProvider {
  def pilot(
    plane: ActorRef,
    autopilot: ActorRef,
    controls: ActorRef,
    altimeter: ActorRef): Actor = Pilot(plane, autopilot, controls, altimeter)
  def copilot: Actor = Copilot()
  def autopilot: Actor = Autopilot()
}
