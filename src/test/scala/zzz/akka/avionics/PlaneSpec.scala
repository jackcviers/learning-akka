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
  }
}

class planeContext(val actorSystem: ActorSystem) extends TestKit(actorSystem) with ScopedTestKit with ImplicitSender
