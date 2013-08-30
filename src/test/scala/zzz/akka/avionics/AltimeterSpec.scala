package zzz.akka.avionics

import akka.actor.{ Actor, ActorSystem, Props }
import akka.testkit.{ ScopedTestKit, TestActorRef, TestKitBase, TestLatch }
import org.specs2.mutable.Specification
import scala.concurrent.Await
import scala.concurrent.duration._

class AltimeterSpec extends Specification {
  isolated
  import Altimeter._
  override def is = {
    s2"""
  Altimeter should
    record rate of climb changes $recordRateOfClimb
    keep rate of climb changes within bounds $keepRateOfClimbChangesWithinBounds
"""
  }

  def recordRateOfClimb = new altimeterContext {
    val (_, real) = actor()
    real receive RateChange(1f)
    real.currentRateOfClimbInFeetPerMinute must_== real.maxRateOfClimbInFeetPerMinute
  }

  def keepRateOfClimbChangesWithinBounds = new altimeterContext {
    val (_, real) = actor()
    real receive RateChange(2f)
    real.currentRateOfClimbInFeetPerMinute must_== real.maxRateOfClimbInFeetPerMinute
  }
}

trait altimeterContext extends ScopedTestKit with TestKitBase { scope ⇒
  implicit lazy val system = ActorSystem("AltimeterSpec")

  object EventSourceSpy {
    val latch = TestLatch(1)
  }

  trait EventSourceSpy extends EventSource {
    def sendEvent[T](event: T): Unit = EventSourceSpy.latch.countDown()
    def eventSourceReceive = Actor.emptyBehavior
  }

  def slicedAltimeter = new Altimeter with EventSourceSpy

  def actor() = {
    val a = TestActorRef[Altimeter](Props(slicedAltimeter))
    (a, a.underlyingActor)
  }
}
