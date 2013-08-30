package zzz.akka.avionics

import akka.actor.ActorIdentity
import akka.actor.ActorSelection
import akka.actor.Identify
import akka.actor.{ Actor, ActorLogging, ActorRef }

class Pilot(
  plane: ActorRef,
  autopilot: ActorSelection,
  var controls: ActorSelection,
  altimeter: ActorSelection) extends Actor with ActorLogging {
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
  def autopilot(plane: ActorRef): Actor = Autopilot(plane)
}
