package zzz.akka.avionics

import akka.actor.{ Actor, ActorIdentity, ActorLogging, ActorRef, ActorSelection, ActorSystem, Identify, PoisonPill, Props }
import akka.pattern.ask
import akka.testkit.{ ImplicitSender, ScopedTestKit, TestKit, TestProbe }
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import org.specs2.mutable.Specification
import scala.concurrent.Await
import scala.concurrent.duration._
import zzz.akka.{ IsolatedLifeCycleSupervisor, IsolatedStopSupervisor, OneForOneSupervisor }

class PilotsSpec extends Specification {
  isolated

  "Pilots".title

  "Copilot" should {
    "take control when the pilot dies" in new pilotsContext(ActorSystem("PilotsSpec")) {

      Await.result(pilotsReadyToGo() ? IsolatedLifeCycleSupervisor.WaitForStart, DurationInt(3).seconds)

      system.actorSelection(copilotPath) ! Pilot.ReadyToGo

      expectMsg(Plane.PilotIdentified)
      lastSender.path.toString must_== s"akka://PilotsSpec$copilotPath"

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
  import PilotsSpec._
  import Plane._

  def nilActor = system.actorSelection(TestProbe().ref.path)

  val pilotPath = s"/user/TestPilots/$pilotName"
  val copilotPath = s"/user/TestPilots/$copilotName"
  implicit val askTimeout = Timeout(DurationInt(4).seconds)

  def pilotsReadyToGo() = {

    system.actorOf(Props(new IsolatedStopSupervisor with OneForOneSupervisor with ActorLogging {
      def childStarter() {
        log.debug("StartingKids")
        context.actorOf(Props(TestPilot(testActor, nilActor, nilActor, nilActor)), pilotName)
        context.actorOf(Props(Copilot(testActor, nilActor, nilActor, nilActor)), copilotName)
      }
    }), "TestPilots")
  }
}

