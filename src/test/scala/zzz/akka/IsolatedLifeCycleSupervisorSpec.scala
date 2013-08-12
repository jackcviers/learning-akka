package zzz.akka

import akka.actor.{ Actor, ActorRef, ActorSystem, Props }
import akka.testkit.{ ImplicitSender, ScopedTestKit, TestActorRef, TestKit }
import org.specs2.mutable.Specification

class IsolatedLifeCycleSupervisorSpec extends Specification {
  isolated

  "IsolatedLifeCycleSupervisor".title

  "" should {
    "reply with Started message when sent WaitForStart" in new isolatedLifeCycleSupervisorContext(ActorSystem("TestIsolatedLifeCycleSupervisor")) {
      val a = TestActorRef(Props(testIsolatedLifeCycleSupervisor()))

      a ! IsolatedLifeCycleSupervisor.WaitForStart
      expectMsg(IsolatedLifeCycleSupervisor.Started)
    }

    "throw when any other messape is sent to it" in new isolatedLifeCycleSupervisorContext(ActorSystem("TestIsolatedLifeCycleSupervisor")) {
      val a = TestActorRef(Props(testIsolatedLifeCycleSupervisor()))

      (a receive 'test) must throwA[Exception]
    }
  }
}

object testIsolatedLifeCycleSupervisor {
  def apply() = new IsolatedLifeCycleSupervisor {

    def childStarter {
      context.actorOf(Props(new Actor {
        def receive = {
          case _ ⇒ sender ! "aha"
        }
      }), "child1")
    }
  }
}

class isolatedLifeCycleSupervisorContext(val actorSystem: ActorSystem) extends TestKit(actorSystem) with ScopedTestKit with ImplicitSender

