package zzz.akka.avionics

import akka.actor.{ ActorLogging, ActorRef, ActorSystem, Props, PoisonPill }
import akka.pattern.ask
import akka.testkit.{ ImplicitSender, ScopedTestKit, TestKit, TestProbe }
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import org.specs2.mutable.Specification
import scala.concurrent.Await
import scala.concurrent.duration._
import zzz.akka.{ IsolatedLifeCycleSupervisor, IsolatedResumeSupervisor, IsolatedStopSupervisor, OneForOneSupervisor }

class AutopilotSpec extends Specification {
  isolated

  "Autopilot".title

  "" should {
    "take control when the copilot dies" in new autopilotContext(ActorSystem("AutopilotSpec")) {

      Await.result(autopilotReadyToGo ? IsolatedLifeCycleSupervisor.WaitForStart, DurationInt(3).seconds)
      Await.result(copilotReadyToGo ? IsolatedLifeCycleSupervisor.WaitForStart, DurationInt(3).seconds)
      system.actorSelection(copilotPath) ! Pilot.ReadyToGo

      expectMsg(Plane.PilotIdentified)

      system.actorSelection(autopilotPath) ! Pilot.ReadyToGo

      expectMsg(Plane.RequestCopilot)

      lastSender.path.toString must_== s"akka://AutopilotSpec/user/TestAutopilot/Autopilot"

      system.actorSelection(autopilotPath) ! Autopilot.CopilotSelection(system.actorSelection(copilotPath))

      expectMsg(Plane.CopilotIdentified)

      lastSender.path.toString must_== s"akka://AutopilotSpec/user/TestAutopilot/Autopilot"

      system.actorSelection(copilotPath) ! PoisonPill

      expectMsg(Plane.GiveMeControl)

      lastSender.path.toString must_== s"akka://AutopilotSpec/user/TestAutopilot/Autopilot"
    }
  }
}

object TestAutopilot {
  def apply(plane: ActorRef) = new Autopilot(plane)
}

class autopilotContext(val actorSystem: ActorSystem) extends TestKit(actorSystem) with ScopedTestKit with ImplicitSender {
  import Plane._
  lazy val copilotName = ConfigFactory.load().getString("zzz.akka.avionics.flightcrew.copilotName")
  lazy val pilotName = ConfigFactory.load().getString("zzz.akka.avionics.flightcrew.pilotName")

  def nilActor = system.actorSelection(TestProbe().ref.path)
  lazy val copilotPath = s"/user/TestPilots/$copilotName"
  lazy val autopilotPath = "/user/TestAutopilot/Autopilot"
  lazy val pilotPath = "/user/TestPilots/$pilotName"
  implicit val askTimeout = Timeout(DurationInt(4).seconds)

  def autopilotReadyToGo = {
    system.actorOf(Props(new IsolatedResumeSupervisor with OneForOneSupervisor with ActorLogging {
      def childStarter {
        log.debug("StartingEquipment")
        context.actorOf(Props(TestAutopilot(testActor)), "Autopilot")
      }
    }), "TestAutopilot")
  }

  def copilotReadyToGo = {
    system.actorOf(Props(new IsolatedStopSupervisor with OneForOneSupervisor with ActorLogging {
      def childStarter {
        log.debug("StartingKids")
        context.actorOf(Props(TestPilot(testActor, nilActor, nilActor, nilActor)), pilotName)
        context.actorOf(Props(TestCopilot(testActor, nilActor, nilActor, nilActor)), copilotName)
      }
    }), "TestPilots")
  }
}
