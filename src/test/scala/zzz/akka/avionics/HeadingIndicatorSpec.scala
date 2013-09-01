package zzz.akka.avionics

import akka.actor.{ Actor, ActorSystem, Props }
import akka.testkit.{ ScopedTestKit, TestActorRef, TestKitBase, TestLatch }
import org.specs2.mutable.Specification

class HeadingIndicatorSpec extends Specification {
  import HeadingIndicator._

  isolated
  "HeadingIndicator".title

  "" should {
    "record the rate of heading change" in new headingIndicatorContext {
      val (_, real) = actor()
      real receive BankChange(1f)
      real.currentRateOfBankChangeInDegreesPerSecond must_== real.maxRateOfBankChangeInDegreesPerSecond
    }

    "keep the rate of bank change within bounds" in new headingIndicatorContext {
      val (_, real) = actor()

      real receive BankChange(2f)
      real.currentRateOfBankChangeInDegreesPerSecond must_== real.maxRateOfBankChangeInDegreesPerSecond
    }
  }
}

trait headingIndicatorContext extends ScopedTestKit with TestKitBase { scope ⇒
  implicit lazy val system = ActorSystem("HeadingIndicatorSpec")
  object EventSourceSpy {
    val latch = TestLatch(1)
  }

  trait EventSourceSpy extends EventSource {
    def sendEvent[T](event: T): Unit = EventSourceSpy.latch.countDown()
    def eventSourceReceive = Actor.emptyBehavior
  }

  def slicedHeadingIndicator = new HeadingIndicator with EventSourceSpy

  def actor() = {
    val a = TestActorRef[HeadingIndicator](Props(slicedHeadingIndicator))
    (a, a.underlyingActor)
  }

}
