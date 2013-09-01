package zzz.akka.avionics

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, ScopedTestKit, TestActorRef, TestKit, TestKitBase, TestProbe}
import org.specs2.mutable.Specification

class FlightAttendantSpec extends Specification {
  import FlightAttendant._
  isolated

  "Filght Attendant".title

  "" should {
    "get a drink when asked" in new flightAttendantContext(ActorSystem("FilghtAttendantSpec")) {
      flightAttendant ! GetDrink("Soda")
      expectMsg(Drink("Soda"))
    }

    "assist an injured passenger" in new flightAttendantContext(ActorSystem("FlightAttendantSpec")) {
      (for (i ← 1 to 1000) yield i) foreach { _ ⇒ flightAttendant ! GetDrink("Soda") }
      flightAttendant ! Assist(testActor)
      expectMsg(Drink("Magic Healing Potion."))
    }.pendingUntilFixed

    "change drinks if the person we are currently servicing asks for a drink" in new flightAttendantContext(ActorSystem("FilghtAttendantSpec")) {
      flightAttendant ! GetDrink("Soda")
      flightAttendant ! GetDrink("Whiskey")
      expectMsg(Drink("Whiskey"))
    }.pendingUntilFixed

    "Refuse requests while serving other requests by responding to Busy_? with yes" in new flightAttendantContext(ActorSystem("FlightAttendantSpec")) {
      (for (i ← 1 to 1000) yield i) foreach { _ ⇒ flightAttendant ! GetDrink("Soda") }
      testSender.send(flightAttendant, Busy_?)
      testSender.expectMsg(Yes)
    }.pendingUntilFixed

    "Say it is not busy while not serving other requests by responding te Busy_? with No" in new flightAttendantContext(ActorSystem("FlightAttendantSpec")) {
      flightAttendant ! Busy_?
      expectMsg(No)
    }.pendingUntilFixed
  }
}

object TestFlightAttendant {
  def apply() = new FlightAttendant with AttendantResponsiveness {
    val maxResponseTimeMS = 1
  }
}

class flightAttendantContext(val actorSystem: ActorSystem) extends TestKit(actorSystem) with ScopedTestKit with ImplicitSender {
  val flightAttendant = TestActorRef(Props(TestFlightAttendant()))
  val testSender = TestProbe()
}
