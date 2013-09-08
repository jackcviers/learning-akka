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
  import FlyingBehavior._
  import ControlSurfaces._

  case object ReadyToGo
  case object RelinquishControl

  val tipsyFactor = 1.03f
  val zaphodFactor = 1f

  val tipsyCalcElevator: Calculator = { (target, status) ⇒
    calcElevator(target, status) match {
      case StickForward(amt) ⇒ StickForward(amt * tipsyFactor)
      case StickBack(amt) ⇒ StickBack(amt * tipsyFactor)
      case m ⇒ m
    }
  }

  val tipsyCalcAilerons: Calculator = { (target, status) ⇒
    calcAilerons(target, status) match {
      case StickLeft(amt) ⇒ StickLeft(amt * tipsyFactor)
      case StickRight(amt) ⇒ StickRight(amt * tipsyFactor)
      case m ⇒ m
    }
  }

  val zaphodCalcElevator: Calculator = { (target, status) ⇒
    calcElevator(target, status) match {
      case StickForward(_) ⇒ StickForward(zaphodFactor)
      case StickBack(_) ⇒ StickBack(zaphodFactor)
      case m ⇒ m
    }
  }

  val zaphodCalcAilerons: Calculator = { (target, status) ⇒
    calcAilerons(target, status) match {
      case StickLeft(_) ⇒ StickLeft(zaphodFactor)
      case StickRight(_) ⇒ StickRight(zaphodFactor)
      case m ⇒ m
    }
  }

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
