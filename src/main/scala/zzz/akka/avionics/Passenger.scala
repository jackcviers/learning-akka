package zzz.akka.avionics

import akka.actor.{ Actor, ActorLogging, ActorSelection }
import scala.concurrent.ExecutionContext
import scala.util.Random

class Passenger(callButton: ActorSelection) extends Actor with ActorLogging {
  this: DrinkRequestProbability ⇒
  import Passenger._
  import FlightAttendant.{ GetDrink, Drink }
  import scala.collection.JavaConverters._

  implicit val ec: ExecutionContext = this.context.dispatcher
  
  val SeatAssignment(myname, _, _) = self.path.name.replaceAllLiterally("_", " ")
  val drinks = context.system.settings.config.getStringList("zzz.akka.avionics.drinks").asScala.toIndexedSeq
  val scheduler = context.system.scheduler

  override def preStart() {
    self ! CallForDrink
  }

  def maybeSendDrinkRequest(): Unit = {
    if (Random.nextFloat() > askThreshold) callButton ! GetDrink(drinks(Random.nextInt(drinks.length)))
    scheduler.scheduleOnce(randomishTime(), self, CallForDrink)
  }

  def receive = {
    case CallForDrink ⇒ maybeSendDrinkRequest()
    case Drink(drinkName) ⇒ log.info(s"$myname received a $drinkName - Yum.")
    case FastenSeatbelts ⇒ log.info(s"$myname fastening seatbelt")
    case UnfastenSeatbelts ⇒ log.info(s"$myname unfastening seatbelt")
  }

}

object Passenger {
  case object FastenSeatbelts
  case object UnfastenSeatbelts
  case object CallForDrink

  val SeatAssignment = """([\w\s_]+)-(\d+)-([A-Z])""".r
}

trait PassengerProvider {
  def passenger(callButton: ActorSelection): Actor = new Passenger(callButton) with DrinkRequestProbability
}

