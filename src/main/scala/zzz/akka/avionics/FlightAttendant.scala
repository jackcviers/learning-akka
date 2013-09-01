package zzz.akka.avionics

import akka.actor.{ Actor, ActorRef, Cancellable }
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Random

class FlightAttendant extends Actor { self: AttendantResponsiveness ⇒
  import FlightAttendant._
  implicit val ec = context.dispatcher

  def receive = assistInjuredPassenger orElse handleDrinkRequests

  case class DeliverDrink(drink: Drink)

  var pendingDelivery: Option[Cancellable] = None

  def scheduleDelivery(drinkName: String): Cancellable = {
    context.system.scheduler.scheduleOnce(responseDuration, context.self, DeliverDrink(Drink(drinkName)))
  }

  def assistInjuredPassenger: Receive = {
    case Assist(passenger) ⇒
      pendingDelivery foreach {_ cancel}
      pendingDelivery = None
      passenger ! Drink("Magic Healing Potion")
  }

  def handleDrinkRequests: Receive = {
    case GetDrink(drinkName) ⇒
      pendingDelivery = Some(scheduleDelivery(drinkName))
      context.become(assistInjuredPassenger orElse handleSpecificPerson(sender))
    case Busy_? ⇒ sender ! No
  }

  def handleSpecificPerson(person: ActorRef): Receive = {
    case GetDrink(drinkName) if sender == person ⇒
      pendingDelivery foreach {_ cancel}
      pendingDelivery = Some(scheduleDelivery(drinkName))
    case DeliverDrink(drink) ⇒
      person ! drink
      pendingDelivery = None
      context.become(assistInjuredPassenger orElse handleDrinkRequests)

    case m: GetDrink ⇒ context.parent forward m

    case Busy_? ⇒ sender ! Yes
  }



}

trait AttendantResponsiveness {
  val maxResponseTimeMS: Int
  def responseDuration = Random.nextInt(maxResponseTimeMS).millis
}

object FlightAttendant {
  case class GetDrink(drinkName: String)
  case class Drink(drinkName: String)
  case class Assist(passenger: ActorRef)
  case object Busy_?
  case object Yes
  case object No
  def apply() = new FlightAttendant with AttendantResponsiveness {
    val maxResponseTimeMS = 300000
  }
}
