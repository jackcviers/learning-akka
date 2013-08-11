package zzz.akka.avionics

import akka.actor.{ ActorLogging, ActorRef, ActorSystem, Props }
import akka.testkit.{ ImplicitSender, ScopedTestKit, TestActorRef, TestKit }
import org.specs2.mutable.Specification

class PilotSpec extends Specification {
  import Pilot.ReadyToGo
  isolated

  "Pilot".title

  "" should {
    "respond with GiveMeControl when sent ReadyToGo" in new pilotContext(ActorSystem("TestPilot")) {
      val a = TestActorRef(Props(TestPilot(testActor)))

      a ! Pilot.ReadyToGo
      expectMsg(Plane.GiveMeControl)
    }

    "accept a Controls message" in new pilotContext(ActorSystem("TestPilot")) {
      val pilot = TestActorRef(Props(TestPilot(testActor)))
      val altimeter = TestActorRef(Props(Altimeter()))
      val controls = TestActorRef(Props(ControlSurfaces(altimeter)))
      pilot receive Plane.Controls(controls)
      expectNoMsg
    }
  }
}

object TestPilot {
  def apply(test: ActorRef) = new Pilot with PilotPlaneProvider {
    override def plane = test
  }
}

class pilotContext(val actorSystem: ActorSystem) extends TestKit(actorSystem) with ScopedTestKit with ImplicitSender

