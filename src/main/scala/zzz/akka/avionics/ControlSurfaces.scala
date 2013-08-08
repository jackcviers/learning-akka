package zzz.akka.avionics

import akka.actor.{ Actor, ActorRef }

object ControlSurfaces {
  case class StickBack(amount: Float)
  case class StickForward(amount: Float)

  def apply(altimeter: ActorRef): ControlSurfaces = new ControlSurfaces(altimeter)
}

class ControlSurfaces(altimeter: ActorRef) extends Actor {
  import ControlSurfaces._
  import Altimeter._

  def receive = {
    case StickBack(amount) ⇒ altimeter ! RateChange(amount)
    case StickForward(amount) ⇒ altimeter ! RateChange(-amount)
  }
}
