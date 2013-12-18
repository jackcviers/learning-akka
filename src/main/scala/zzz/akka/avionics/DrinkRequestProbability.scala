package zzz.akka.avionics

import scala.concurrent.duration._
import scala.util.Random

trait DrinkRequestProbability {
  val askThreshold = 0.9f

  val requestMin = 20.minutes

  val requestUpper = 30.minutes

  def randomishTime(): FiniteDuration = {
    requestMin + Random.nextInt(requestUpper.toMillis.toInt).millis
  }
}
