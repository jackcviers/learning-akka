package zzz.akka.avionics

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSelection }

class Copilot(
  plane: ActorRef,
  autopilot: ActorSelection,
  var controls: ActorSelection,
  altimeter: ActorSelection) extends Actor {
  import Pilot.ReadyToGo

  val pilotName = context
    .system
    .settings
    .config
    .getString("zzz.akka.avionics.flightcrew.pilotName")

  var pilot = context.actorSelection("/deadLetters")

  def receive = {
    case ReadyToGo ⇒
      pilot = context.actorSelection("../" + pilotName)
    case m ⇒ throw new Exception(s"Copilot dosen't understand $m")
  }
}

object Copilot {
  def apply(
    plane: ActorRef,
    autopilot: ActorSelection,
    controls: ActorSelection,
    altimeter: ActorSelection) = new Copilot(
    plane,
    autopilot,
    controls,
    altimeter)
}
