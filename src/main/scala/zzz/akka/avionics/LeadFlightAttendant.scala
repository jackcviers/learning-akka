package zzz.akka.avionics

import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import com.typesafe.config.ConfigFactory
import scala.language.postfixOps
import scala.util.Random

class LeadFlightAttendant extends Actor { self: AttendantCreationPolicy ⇒
  import LeadFlightAttendant._

  override def preStart() {
    import scala.collection.JavaConverters._

    val attendantNames = context.system.settings.config.getStringList("zzz.akka.avionics.flightcrew.attendantNames").asScala

    attendantNames take numberOfAttendants foreach { name ⇒
      context.actorOf(Props(createAttendant), name)
    }
  }

  def randomAttendant = context.children.take(Random.nextInt(numberOfAttendants) + 1).last

  def receive = {
    case GetFlightAttendant ⇒
      sender ! Attendant(randomAttendant)
    case m ⇒
      randomAttendant forward m
  }
}

trait AttendantCreationPolicy {
  val numberOfAttendants: Int = ConfigFactory.load().getInt("zzz.akka.avionics.flightcrew.numberOfAttendants")
  def createAttendant: Actor = FlightAttendant()
}

trait LeadFlightAttendantProvider {
  def newLeadFlightAttendant: Actor = LeadFlightAttendant()
}

object LeadFlightAttendant {
  case object GetFlightAttendant
  case class Attendant(a: ActorRef)

  def apply() = new LeadFlightAttendant with AttendantCreationPolicy
}

