package zzz.akka.avionics

import akka.actor.{ Actor, ActorLogging, OneForOneStrategy, Props }
import akka.actor.SupervisorStrategy.Restart
import scala.concurrent.duration._

class Altimeter extends Actor with ActorLogging { self: EventSource ⇒
  import Altimeter._
  import AltitudeCalculator._

  implicit val ec = context.dispatcher

  val ceilingInFeet = 43000 // in feet
  val maxRateOfClimbInFeetPerMinute = 5000 // in feet/min
  val ticker = context
    .system
    .scheduler
    .schedule(100.millis, 100.millis, self, Tick)
  val altitudeCalculator = context
    .actorOf(Props(AltitudeCalculator()), "AltitudeCalculator")

  override val supervisorStrategy = OneForOneStrategy(-1, Duration.Inf) {
    case _ ⇒ Restart
  }

  var currentRateOfClimbInFeetPerMinute = 0f
  var currentAltitudeInFeet = 0d
  var timeSinceLastTick = System.currentTimeMillis

  case object Tick

  def altimeterReceive: Receive = {
    case RateChange(amount) ⇒
      currentRateOfClimbInFeetPerMinute = amount
        .min(1.0f)
        .max(-1.0f) * maxRateOfClimbInFeetPerMinute
      log info ("Altimeter changed rate of climb to " +
        s"$currentRateOfClimbInFeetPerMinute.")
    case Tick ⇒
      val tick = System.currentTimeMillis
      altitudeCalculator ! CalculateAltitude(
        timeSinceLastTick, tick, currentRateOfClimbInFeetPerMinute)
      timeSinceLastTick = tick
    case AltitudeCalculated(tick, altitudeDelta) ⇒
      currentAltitudeInFeet += altitudeDelta
      sendEvent(AltitudeUpdate(currentAltitudeInFeet))
  }

  def receive = eventSourceReceive orElse altimeterReceive

  override def postStop() = ticker.cancel
}

object Altimeter {
  case class AltitudeUpdate(altitude: Double)
  case class RateChange(amount: Float)
  def apply(): Actor = new Altimeter with ProductionEventSource
}

trait AltimeterProvider {
  def altimeter = Altimeter()
}
