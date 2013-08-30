package zzz.akka.avionics

import akka.actor.Actor

class AltitudeCalculator extends Actor {
  import AltitudeCalculator._

  def receive = {
    case CalculateAltitude(_, _, roc) if (roc == 0) ⇒
      throw new ArithmeticException("Divide by zero")
    case CalculateAltitude(lastTick, tick, roc) ⇒
      sender ! AltitudeCalculated(tick,
        ((tick - lastTick) / 60000.0) * (roc * roc) / roc)
  }
}

object AltitudeCalculator {
  case class CalculateAltitude(lastTick: Long, tick: Long, roc: Double)
  case class AltitudeCalculated(newTick: Long, altitide: Double)

  def apply() = new AltitudeCalculator
}

