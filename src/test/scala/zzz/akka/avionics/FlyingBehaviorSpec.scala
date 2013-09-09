package zzz.akka.avionics

import akka.actor.Identify
import akka.actor.{ ActorRef, ActorSelection, ActorSystem }
import akka.testkit.{ ImplicitSender, ScopedTestKit, TestFSMRef, TestKit, TestKitBase, TestProbe }
import org.specs2.mutable.Specification
import zzz.akka.avionics.Altimeter._
import zzz.akka.avionics.FlyingBehavior._
import zzz.akka.avionics.HeadingIndicator._
import zzz.akka.avionics.Plane._
import scala.language.implicitConversions

class FlyingBehaviorSpec extends Specification {
  isolated

  "FlyingBehavior".title

  "" should {
    "start in the Idle state and with Uninitialized data" in new flyingBehaviorContext(ActorSystem("TestFlyingBehaviorStart")) {
      val a = testFlyingBehavior()
      a.stateName must_== Idle
      a.stateData must_== Uninitialized
    }
  }

  "PreparingToFly state" should {
    "stay in PreparingToFly state when only a " +
      "HeadingUpdate is received" in new flyingBehaviorContext(ActorSystem("TestFlyingBehaviorPreparingToFly")) {
        val a = testFlyingBehavior()
        a ! Fly(target)
        a ! HeadingUpdate(20)
        a.stateName must_== PreparingToFly
        val sd = a.stateData
        sd.status.altitude must_== -1
        sd.status.heading must_== 20
      }

    "move to Flying state when all parts are received" in new flyingBehaviorContext(ActorSystem("TestFlyingBehaviorFlyingConditions")) {
      val a = testFlyingBehavior()
      a ! Fly(target)
      a ! HeadingUpdate(20)
      a ! AltitudeUpdate(20)
      a ! Controls(system.actorSelection(testActor.path))

      awaitCond(a.stateName != PreparingToFly)

      a.stateName must_== Flying
      val sd = a.stateData
      sd.controls must_== testActor
      sd.status.altitude must_== 20
      sd.status.heading must_== 20
    }
  }

  "transitioning te Flying state" should {
    "create the Adjustment timer" in new flyingBehaviorContext(ActorSystem("TestFlyingBehaviorAdjustmentTimer")) {
      val a = testFlyingBehavior()
      a.setState(PreparingToFly)
      a.setState(Flying)
      a.isTimerActive("Adjustment") must beTrue
    }
  }
}

class flyingBehaviorContext(val actorSystem: ActorSystem)
  extends TestKit(actorSystem) with ScopedTestKit with ImplicitSender {
  private[this] def nilActorSelection = system.actorSelection(TestProbe().ref.path)
  private[this] def nilActor = TestProbe().ref

  implicit def toFlightData(d: Data): FlightData = d.asInstanceOf[FlightData]

  def target = CourseTarget(20000.00, 240.0f, System.currentTimeMillis() + 20000L)

  def testFlyingBehavior(
    plane: ActorRef = nilActor,
    heading: ActorSelection = nilActorSelection,
    altimeter: ActorSelection = nilActorSelection) = TestFSMRef[State, Data, FlyingBehavior](new FlyingBehavior(plane, heading, altimeter))
}

