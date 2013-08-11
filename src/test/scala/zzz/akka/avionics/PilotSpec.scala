package zzz.akka.avionics

import akka.actor.{ ActorSystem, Props }
import akka.testkit.{ ImplicitSender, ScopedTestKit, TestActorRef, TestKit }
import org.specs2.mutable.Specification

class PilotSpec extends Specification {
  isolated

  "Pilot".title

  "" should {
    "respond with GiveMeControl when sent ReadyToGo" in new pilotContext(ActorSystem("TestPilot")) {
      val a = TestActorRef(Props(TestPilot()))

      a ! Pilot.ReadyToGo
      fishForMessage() {
        case m ⇒ true
      }
    }

    "accept a Controls message" in new pilotContext(ActorSystem("TestPilot")) {
      val pilot = TestActorRef(Props(TestPilot()))
      val altimeter = TestActorRef(Props(Altimeter()))
      val controls = TestActorRef(Props(ControlSurfaces(altimeter)))

      pilot ! Plane.Controls(controls)
      expectNoMsg()
    }
  }
}

object TestPilot {
  def apply() = new Pilot
}

class pilotContext(val actorSystem: ActorSystem) extends TestKit(actorSystem) with ScopedTestKit with ImplicitSender

