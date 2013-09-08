package zzz.akka.avionics

import akka.actor.ActorIdentity
import akka.actor.ActorSelection
import akka.actor.Identify
import akka.actor.Props
import akka.actor.{ Actor, ActorLogging, ActorRef }
import FlyingBehavior._
import DrinkingBehavior._
import ControlSurfaces._
import Pilot._
import Plane.{ Controls, GiveMeControl }
import akka.actor.FSM._

class Pilot(
  plane: ActorRef,
  autopilot: ActorSelection,
  heading: ActorSelection,
  altimeter: ActorSelection) extends Actor with ActorLogging { self: DrinkingProvider with FlyingProvider ⇒

  val copilotName = context
    .system
    .settings
    .config
    .getString("zzz.akka.avionics.flightcrew.copilotName")

  def receive = bootstrap


  def setCourse(flyer: ActorSelection) {
    flyer ! Fly(CourseTarget(20000, 250, System.currentTimeMillis + 30000))
  }

  override def preStart() {
    context.actorOf(drinkingBehavior(context.self), "DrinkingBehavior")
    context.actorOf(flyingBehavior(plane, heading, altimeter), "FlyingBehavior")
  }

  def bootstrap: Receive = {
    case ReadyToGo ⇒
      val copilot = context.actorSelection(s"../$copilotName")
      val flyer = context.actorSelection(s"FlyingBehavior")
      flyer ! SubscribeTransitionCallBack(context.self)
      setCourse(flyer)
      context.become(sober(copilot, flyer))
  }

  def sober(copilot: ActorSelection, flyer: ActorSelection): Receive = {
    case FeelingSober ⇒
    case FeelingTipsy ⇒ becomeTipsy(copilot, flyer)
    case FeelingLikeZaphod ⇒ becomeZaphod(copilot, flyer)
  }

  def tipsy(copilot: ActorSelection, flyer: ActorSelection): Receive = {
    case FeelingSober ⇒ becomeSober(copilot, flyer)
    case FeelingTipsy ⇒
    case FeelingLikeZaphod ⇒ becomeZaphod(copilot, flyer)
  }

  def zaphod(copilot: ActorSelection, flyer: ActorSelection): Receive = {
    case FeelingSober ⇒ becomeSober(copilot, flyer)
    case FeelingTipsy ⇒ becomeTipsy(copilot, flyer)
    case FeelingLikeZaphod ⇒
  }

  def idle: Receive = {
    case _ ⇒
  }

  def becomeSober(copilot: ActorSelection, flyer: ActorSelection) = {
    flyer ! NewElevatorCalculator(calcElevator)
    flyer ! NewBankCalculator(calcAilerons)
    context.become(sober(copilot, flyer))
  }

  def becomeTipsy(copilot: ActorSelection, flyer: ActorSelection) = {
    flyer ! NewElevatorCalculator(tipsyCalcElevator)
    flyer ! NewBankCalculator(tipsyCalcAilerons)
    context.become(tipsy(copilot, flyer))
  }

  def becomeZaphod(copilot: ActorSelection, flyer: ActorSelection) = {
    flyer ! NewElevatorCalculator(zaphodCalcElevator)
    flyer ! NewBankCalculator(zaphodCalcAilerons)
    context.become(zaphod(copilot, flyer))
  }

  override def unhandled(msg: Any) = {
    msg match {
      case Transition(_, _, Flying) ⇒ setCourse(context.system.actorSelection(sender.path))
      case Transition(_, _, Idle) ⇒ context.become(idle)
      case Transition(_, _, _) ⇒
      case CurrentState(_, _) ⇒
      case m ⇒ super.unhandled(m)
    }
  }

}

object Pilot {

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
    heading: ActorSelection,
    altimeter: ActorSelection) = new Pilot(plane, autopilot, heading, altimeter) with DrinkingProvider with FlyingProvider
}

trait PilotProvider {
  def pilot(
    plane: ActorRef,
    autopilot: ActorSelection,
    heading: ActorSelection,
    altimeter: ActorSelection): Actor = Pilot(plane, autopilot, heading, altimeter)
  def copilot(
    plane: ActorRef,
    autopilot: ActorSelection,
    controls: ActorSelection,
    altimeter: ActorSelection): Actor = Copilot(plane, autopilot, controls, altimeter)
  def autopilot(plane: ActorRef): Actor = Autopilot(plane)
}

trait DrinkingProvider {
  def drinkingBehavior(drinker: ActorRef) = Props(DrinkingBehavior(drinker))
}

trait FlyingProvider {
  def flyingBehavior(plane: ActorRef, heading: ActorSelection, altimeter: ActorSelection) = Props(new FlyingBehavior(plane, heading, altimeter))
}
