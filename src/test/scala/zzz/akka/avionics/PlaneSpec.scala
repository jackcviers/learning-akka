package zzz.akka.avionics

import akka.actor.ActorSystem
import akka.testkit.{ ImplicitSender, ScopedTestKit, TestActorRef, TestKit }
import org.specs2.mutable.Specification

class PlaneSpec extends Specification {
  isolated
  "Plane".title

  "" should {
    "relenquish controls when sent GiveMeControl" in new planeContext(ActorSystem("TestPlane")) {
      val a = TestActorRef[Plane]

      a ! Plane.GiveMeControl
      expectMsgClass(classOf[Plane.Controls])
    }

    "accept AltitudeUpdates" in new planeContext(ActorSystem("TestPlane")) {
      val a = TestActorRef[Plane]

      a.receive(Altimeter.AltitudeUpdate(10.0))
      expectNoMsg
    }

    "have a configKey of zzz.akka.avionics.flightcrew" in new planeContext(ActorSystem("TestPlane")) {
      val a = TestActorRef[Plane]

      a.underlyingActor.asInstanceOf[Plane].configKey must_== "zzz.akka.avionics.flightcrew"
    }

    "have a pilot with the name Harry" in new planeContext(ActorSystem("TestPlane")) {
      val a = TestActorRef[Plane]
      a.underlyingActor.asInstanceOf[Plane].pilotRef.path.name must contain("Harry")
    }

    "have a copilot with the name Joan" in new planeContext(ActorSystem("TestPlane")) {
      val a = TestActorRef[Plane]
      a.underlyingActor.asInstanceOf[Plane].copilotRef.path.name must contain("Joan")
    }

    "have an autopilot with the name Autopilot" in new planeContext(ActorSystem("TestPlane")) {
      val a = TestActorRef[Plane]
      a.underlyingActor.asInstanceOf[Plane].autopilotRef.path.name must contain("Autopilot")
    }

    "have a leadFlightAttendant with the name Gizalle" in new planeContext(ActorSystem("TestPlane")) {
      val a = TestActorRef[Plane]
      a.underlyingActor.asInstanceOf[Plane].leadFlightAttendantRef.path.name must contain("Gizalle")
    }
  }
}

class planeContext(val actorSystem: ActorSystem) extends TestKit(actorSystem) with ScopedTestKit with ImplicitSender
