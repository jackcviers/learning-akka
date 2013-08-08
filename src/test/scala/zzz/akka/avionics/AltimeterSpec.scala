package zzz.akka.avionics

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import akka.testkit.TestActorRef
import akka.testkit.TestLatch
import akka.testkit.{ ScopedTestKit, TestKitBase }
import org.specs2.mutable.Specification

class AltimeterSpec extends Specification {
  isolated

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
