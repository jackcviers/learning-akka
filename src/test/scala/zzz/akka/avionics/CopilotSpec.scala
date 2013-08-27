package zzz.akka.avionics

import akka.actor.ActorIdentity
import akka.actor.ActorIdentity
import akka.actor.ActorLogging
import akka.actor.Identify
import akka.actor.Identify
import akka.actor.Identify
import akka.actor.PoisonPill
import akka.pattern.ask
import akka.actor.Actor
import akka.actor.ActorSelection
import akka.actor.ActorSystem
import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.testkit.TestProbe
import akka.testkit.{ ImplicitSender, ScopedTestKit, TestActorRef, TestKit }
import akka.util.Timeout
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import java.security.Identity
import scala.concurrent.Await
import scala.concurrent.duration._
import org.specs2.mutable.Specification
import zzz.akka.IsolatedLifeCycleSupervisor
import zzz.akka.IsolatedStopSupervisor
import zzz.akka.OneForOneSupervisor

class PilotsSpec extends Specification {
  isolated

  "Pilots".title

  "Copilot" should {
    "take control when the pilot dies" in new pilotsContext(ActorSystem("PilotsSpec")) {
      import PilotsSpec._
      import Plane._

      def nilActor = system.actorSelection(TestProbe().ref.path)

      val pilotPath = s"/user/TestPilots/$pilotName"
      val copilotPath = s"/user/TestPilots/$copilotName"
      implicit val askTimeout = Timeout(DurationInt(4).seconds)

      def pilotsReadyToGo() = {
        

        val a = system.actorOf(Props(new IsolatedStopSupervisor with OneForOneSupervisor with ActorLogging {
          def childStarter() {
            log.debug("StartingKids")
            context.actorOf(Props(TestPilot(testActor, nilActor, nilActor, nilActor)), pilotName)
            context.actorOf(Props(Copilot(testActor, nilActor, nilActor, nilActor)), copilotName)
          }
        }), "TestPilots")
        a
      }

      Await.result(pilotsReadyToGo() ? IsolatedLifeCycleSupervisor.WaitForStart, DurationInt(3).seconds)

      system.actorSelection(copilotPath) ! Pilot.ReadyToGo

      Thread.sleep(5000)
      
      system.actorSelection(pilotPath) ! PoisonPill

      expectMsg(FiniteDuration(14, "seconds"), Plane.GiveMeControl)
      lastSender.path.toString must_== s"akka://PilotsSpec$copilotPath"
    }
  }
}

object PilotsSpec {
  val copilotName = ConfigFactory.load().getString("zzz.akka.avionics.flightcrew.copilotName")
  val pilotName = ConfigFactory.load().getString("zzz.akka.avionics.flightcrew.pilotName")
}

class FakePilot extends Actor with ActorLogging {
  override def receive = {
    case "Hello" ⇒ log.debug("I got a message")
    case Identify(n) ⇒ ActorIdentity(n, Some(self))
    case _ ⇒
  }
}

object TestCopilot {
  def apply(
    plane: ActorRef,
    autopilot: ActorSelection,
    controls: ActorSelection,
    altimeter: ActorSelection) = new Copilot(
    plane,
    autopilot,
    controls,
    altimeter)
}

class pilotsContext(val actorSystem: ActorSystem) extends TestKit(actorSystem) with ScopedTestKit with ImplicitSender {

}

