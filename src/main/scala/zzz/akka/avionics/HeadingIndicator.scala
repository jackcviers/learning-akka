package zzz.akka.avionics

import akka.actor.Actor
import scala.concurrent.duration._

class HeadingIndicator extends Actor { self: EventSource ⇒
  import context._
  import HeadingIndicator._

  val maxRateOfBankChangeInDegreesPerSecond = 1f
  val minRateOfBankChangeInDegreesPerSecond = -1f
  val maxDegreesPerSecond = 5
  val ticker = system.scheduler.schedule(100.millis, 100.millis, self, Tick)

  var currentRateOfBankChangeInDegreesPerSecond = 0f
  var heading = 0f
  var lastTick = System.currentTimeMillis

  def receive = eventSourceReceive orElse calculateHeading

  def calculateHeading: Receive = {
    case BankChange(amount) ⇒
      currentRateOfBankChangeInDegreesPerSecond = amount
        .min(minRateOfBankChangeInDegreesPerSecond).max(maxRateOfBankChangeInDegreesPerSecond)
    case Tick ⇒
      val tick = System.currentTimeMillis
      val timeDelta = (tick - lastTick)
      val degrees = currentRateOfBankChangeInDegreesPerSecond * maxDegreesPerSecond
      heading = (heading + 360 * timeDelta * degrees) % 360
      lastTick = tick
      sendEvent(HeadingUpdate(heading))
  }

  override def postStop = ticker.cancel
}

object HeadingIndicator {
  case object Tick
  case class BankChange(amount: Float)
  case class HeadingUpdate(heading: Float)
}
