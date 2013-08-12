package zzz.akka.avionics

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.testkit.{ ImplicitSender, ScopedTestKit, TestActorRef, TestKit, TestKitBase }
import org.specs2.mutable.Specification

class LeadFlightAttendantSpec extends Specification {
  isolated

  "LeadFlightAttendant".title

  "" should {
    "get a flight attendant when asked" in new leadFlightAttendantContext(ActorSystem("TestLeadFlightAttendant")) {
      val a = TestActorRef(Props(TestLeadFlightAttendant()))
      a ! LeadFlightAttendant.GetFlightAttendant
      expectMsgClass(classOf[LeadFlightAttendant.Attendant])
    }

    "forward a message to the attendant" in new leadFlightAttendantContext(ActorSystem("TestLeadFlightAttendant")) {
      val a = TestActorRef(Props(TestLeadFlightAttendant()))
      a ! FlightAttendant.GetDrink("Soda")
      expectMsg(FlightAttendant.Drink("Soda"))
    }
  }
}

object TestLeadFlightAttendant {
  def apply() = new LeadFlightAttendant with AttendantCreationPolicy {
    override val numberOfAttendants = 1
    override def createAttendant = TestFlightAttendant()
  }
}

class leadFlightAttendantContext(val actorSystem: ActorSystem) extends TestKit(actorSystem) with ScopedTestKit with ImplicitSender

