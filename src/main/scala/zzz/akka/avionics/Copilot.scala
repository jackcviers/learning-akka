package zzz.akka.avionics

import akka.actor.ActorLogging
import akka.actor.PoisonPill
import akka.actor.{
  Actor,
  ActorIdentity,
  ActorLogging,
  ActorRef,
  ActorSelection
}
import akka.actor.{ Identify, Terminated }

class Copilot(
  plane: ActorRef,
  autopilot: ActorSelection,
  var controls: ActorSelection,
  altimeter: ActorSelection) extends Actor with ActorLogging {
  import Pilot.ReadyToGo
  import Plane.{ GiveMeControl, PilotIdentified }

  val pilotName = context
    .system
    .settings
    .config
    .getString("zzz.akka.avionics.flightcrew.pilotName")

  var pilot = context.actorSelection("/deadLetters")

  def receive = {
    case ReadyToGo ⇒
      pilot = context.actorSelection("../" + pilotName)
      log.debug(s"$pilot.path")
      log.debug(s"identifying pilot $pilotName")
      pilot ! Identify(None)
    case ActorIdentity(_, Some(r)) ⇒
      log.debug(s"identified $r")
      context.watch(r)
      plane ! PilotIdentified
    case ActorIdentity(_, None) ⇒
      log.debug("nobody home")
    case Terminated(_) ⇒
      log.debug("TERMINATE")
      plane ! GiveMeControl
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
