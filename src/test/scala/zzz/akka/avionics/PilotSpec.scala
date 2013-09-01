package zzz.akka.avionics

import akka.actor.ActorSelection
import akka.actor.{ ActorLogging, ActorRef, ActorSystem, Props }
import akka.testkit.{ ImplicitSender, ScopedTestKit, TestActorRef, TestKit }
import org.specs2.mutable.Specification

class PilotSpec extends Specification {
  import Pilot.ReadyToGo
  isolated

  "Pilot".title

  "" should {
    "respond with GiveMeControl when sent ReadyToGo" in new pilotContext(ActorSystem("TestPilot")) {
      var fakeAltimeter = system.actorSelection("/Controls/Altimeter")
      var fakeControls = system.actorSelection("/Controls/ControlSurfaces")
      var fakeAutopilot = system.actorSelection("/Controls/Autopilot")

      val pilot = TestActorRef(Props(TestPilot(testActor, fakeAutopilot, fakeControls, fakeAltimeter)), "Pilot")
      

      pilot ! Pilot.ReadyToGo
      expectMsg(Plane.GiveMeControl)
    }

    "accept a Controls message" in new pilotContext(ActorSystem("TestPilot")) {
      var fakeAltimeter = system.actorSelection("/Controls/Altimeter")
      var fakeControls = system.actorSelection("/Controls/ControlSurfaces")
      var fakeAutopilot = system.actorSelection("/Controls/Autopilot")

      val pilot = TestActorRef(Props(TestPilot(testActor, fakeAutopilot, fakeControls, fakeAltimeter)), "Pilot")
      val copilot = TestActorRef(Props(Copilot(testActor, fakeAutopilot, fakeControls, fakeAltimeter)), "TestPilot")

      pilot receive Plane.Controls(fakeControls)
      expectNoMsg
    }
  }
}

object TestPilot {
  def apply(
    plane: ActorRef,
    autopilot: ActorSelection,
    heading: ActorSelection,
    altimeter: ActorSelection) = new Pilot(plane, autopilot, heading, altimeter) with DrinkingProvider with FlyingProvider 
}

class pilotContext(val actorSystem: ActorSystem) extends TestKit(actorSystem) with ScopedTestKit with ImplicitSender

