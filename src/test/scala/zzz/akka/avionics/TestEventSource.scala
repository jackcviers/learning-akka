package zzz.akka.avionics

import akka.actor.{ Actor, ActorSystem }
import akka.testkit.{ ScopedTestKit, TestActorRef, TestKitBase }
import org.specs2.mutable.Specification

class EventSourceSpec extends Specification {
  import EventSource._
  isolated

  override def is = {
    s2"""
  EventSource should
    allow us to register a listener $registerListener
    allow us to unregister a listener $unRegisterListener
    send the event to our test actor $sendEvent
"""
  }

  def registerListener = new eventSourceContext {
    val real = TestActorRef[TestEventSource].underlyingActor
    real.receive(RegisterListener(testActor))
    real.listeners must contain(testActor)
  }

  def unRegisterListener = new eventSourceContext {
    val real = TestActorRef[TestEventSource].underlyingActor
    real.receive(RegisterListener(testActor))
    real.receive(UnregisterListener(testActor))
    real.listeners.size must_== (0)
  }

  def sendEvent = new eventSourceContext {
    val testA = TestActorRef[TestEventSource]
    testA ! RegisterListener(testActor)
    testA.underlyingActor.sendEvent("Fibonacci")
    expectMsg("Fibonacci")
  }

}

class TestEventSource extends Actor with ProductionEventSource {
  def receive = eventSourceReceive
}

trait eventSourceContext extends ScopedTestKit with TestKitBase { scope ⇒

  implicit lazy val system = ActorSystem("EventSource")
}
