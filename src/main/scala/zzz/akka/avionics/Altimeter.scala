package zzz.akka.avionics

import akka.actor.{ Actor, ActorLogging }
import scala.concurrent.duration._

object Altimeter {
  case class AltitudeUpdate(altitude: Double)
  case class RateChange(amount: Float)
}

class Altimeter extends Actor with ActorLogging {
  import Altimeter._

  implicit val ec = context.dispatcher

  val ceilingInFeet = 43000 // in feet
  val maxRateOfClimbInFeetPerMinute = 5000 // in feet/min
  val ticker = context.system.scheduler.schedule(100.millis, 100.millis, self, Tick)

  var currentRateOfClimbInFeetPerMinute = 0f
  var currentAltitudeInFeet = 0d
  var timeSinceLastTick = System.currentTimeMillis

  case object Tick

  def receive = {
    case RateChange(amount) ⇒
      currentRateOfClimbInFeetPerMinute = amount.min(1.0f).max(-1.0f) * maxRateOfClimbInFeetPerMinute
      log info (s"Altimeter changed rate of climb to $currentRateOfClimbInFeetPerMinute.")
    case Tick ⇒
      val tick = System.currentTimeMillis
      currentAltitudeInFeet = currentAltitudeInFeet + ((tick - timeSinceLastTick) / 60000.0) * currentRateOfClimbInFeetPerMinute
      timeSinceLastTick = tick
  }

  override def postStop() = ticker.cancel
}
