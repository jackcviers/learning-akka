package zzz.akka.avionics

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.testkit.{ ImplicitSender, ScopedTestKit, TestActorRef, TestKit }
import org.specs2.mutable.Specification

class CopilotSpec extends Specification {
  import Pilot.ReadyToGo

  isolated

  "Copilot".title

  "" should {
    "accept a ReadyToGo message and set the pilot accordingly" in new copilotContext(ActorSystem("TestCopilot")) {
      val pilot = TestActorRef(Props(TestPilot(testActor)))
      val copilot = TestActorRef(Props(TestCopilot(testActor)))
      val altimeter = TestActorRef(Props(Altimeter()))
      val controls = TestActorRef(Props(ControlSurfaces(altimeter)))

      copilot.receive(ReadyToGo)
      expectNoMsg
    }
  }
}

object TestCopilot {
  def apply(test: ActorRef) = new Copilot with PilotPlaneProvider {
    override def plane = test
  }
}

class copilotContext(val actorSystem: ActorSystem) extends TestKit(actorSystem) with ScopedTestKit with ImplicitSender
