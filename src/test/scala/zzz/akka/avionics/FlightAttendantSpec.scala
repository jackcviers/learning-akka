package zzz.akka.avionics

import akka.actor.{ ActorSystem, Props }
import akka.testkit.{ ImplicitSender, ScopedTestKit, TestActorRef, TestKit, TestKitBase }
import com.typesafe.config.ConfigFactory
import org.specs2.mutable.Specification

class FlightAttendantSpec extends Specification {
  import FlightAttendant._
  isolated
  override def is = s2"""
  FlightAttendant should
    get a drink when asked $getADrinkWhenAsked
    """
  def getADrinkWhenAsked = new flightAttendantContext(ActorSystem("FlightAttendantSpec", ConfigFactory.parseString("akka.scheduler.tick-duration = 1ms"))) {
    val a = TestActorRef(Props(TestFlightAttendant()))
    a ! GetDrink("Soda")
    expectMsg(Drink("Soda"))
  }
}

object TestFlightAttendant {
  def apply() = new FlightAttendant with AttendantResponsiveness {
    val maxResponseTimeMS = 1
  }
}

class flightAttendantContext(val actorSystem: ActorSystem) extends TestKit(actorSystem) with ScopedTestKit with ImplicitSender
