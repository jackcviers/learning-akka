package zzz.akka.avionics

import akka.actor.ActorSelection
import akka.actor.{ Actor, ActorLogging, ActorRef }

class Pilot(
  plane: ActorRef,
  autopilot: ActorSelection,
  var controls: ActorSelection,
  altimeter: ActorSelection) extends Actor {
  import Pilot._
  import Plane.{ Controls, GiveMeControl }

  var copilot = context.system.actorSelection("/deadLetters")

  // def childStarter(): Unit {}

  // override def preStart(){
  //   childStarter()
  // }

  val copilotName = context
    .system
    .settings
    .config
    .getString("zzz.akka.avionics.flightcrew.copilotName")

  def receive = {
    case ReadyToGo ⇒
      plane ! GiveMeControl
      copilot = context.actorSelection("../" + copilotName)
    case Controls(controlSurfaces) ⇒
      controls = controlSurfaces
    case m ⇒ throw new Exception(s"Pilot doesn't understand: $m")
  }
}

object Pilot {
  case object ReadyToGo
  def apply(
    plane: ActorRef,
    autopilot: ActorSelection,
    controls: ActorSelection,
    altimeter: ActorSelection) = new Pilot(plane, autopilot, controls, altimeter)
}

trait PilotProvider {
  def pilot(
    plane: ActorRef,
    autopilot: ActorSelection,
    controls: ActorSelection,
    altimeter: ActorSelection): Actor = Pilot(plane, autopilot, controls, altimeter)
  def copilot(
    plane: ActorRef,
    autopilot: ActorSelection,
    controls: ActorSelection,
    altimeter: ActorSelection): Actor = Copilot(plane, autopilot, controls, altimeter)
  def autopilot: Actor = Autopilot()
}
