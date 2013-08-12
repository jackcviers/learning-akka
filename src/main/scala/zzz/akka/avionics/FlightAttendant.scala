package zzz.akka.avionics

import akka.actor.Actor
import scala.concurrent.duration._
import scala.util.Random

class FlightAttendant extends Actor { self: AttendantResponsiveness ⇒
  import FlightAttendant._
  implicit val ec = context.dispatcher

  def receive = {
    case GetDrink(drinkName) ⇒ context.system.scheduler.scheduleOnce(responseDuration, sender, Drink(drinkName))
  }
}

trait AttendantResponsiveness {
  val maxResponseTimeMS: Int
  def responseDuration = Random.nextInt(maxResponseTimeMS).millis
}

object FlightAttendant {
  case class GetDrink(drinkName: String)
  case class Drink(drinkName: String)
  def apply() = new FlightAttendant with AttendantResponsiveness {
    val maxResponseTimeMS = 300000
  }
}
