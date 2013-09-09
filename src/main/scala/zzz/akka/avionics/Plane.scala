package zzz.akka.avionics

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSelection, Props }
import akka.pattern._
import akka.util.Timeout
import scala.concurrent.Await
import scala.concurrent.duration._
import zzz.akka.{ IsolatedResumeSupervisor, IsolatedStopSupervisor, OneForOneSupervisor }

class Plane
  extends Actor
  with ActorLogging { this: PilotProvider with AltimeterProvider with HeadingIndicatorProvider with LeadFlightAttendantProvider ⇒
  import EventSource._
  import Altimeter._
  import ControlSurfaces._
  import Plane._
  import Pilot._
  import Autopilot._
  import zzz.akka.IsolatedLifeCycleSupervisor.{ Started, WaitForStart }

  lazy val configKey = "zzz.akka.avionics.flightcrew"
  lazy val config = context.system.settings.config
  lazy val pilotName = config.getString(s"$configKey.pilotName")
  lazy val copilotName = config.getString(s"$configKey.copilotName")

  implicit val askTimeout = Timeout(1.second)
  val plane = context.self

  def actorForControls(name: String) = context
    .actorSelection(s"Equipment/$name")

  def startEquipment = {
    val controls = context.actorOf(
      Props(
        new IsolatedResumeSupervisor with OneForOneSupervisor {
          def childStarter = {
            val alt = context.actorOf(Props(altimeter), "Altimeter")
            val heading = context.actorOf(Props(headingIndicator), "HeadingIndicator")
            context.actorOf(Props(autopilot(plane)), "Autopilot")
            context.actorOf(Props(ControlSurfaces(plane, alt, heading)), "ControlSurfaces")
          }
        }), "Equipment")

    Await.result(controls ? WaitForStart, 1.second)
  }

  def actorForPilots(name: String) = context.actorSelection(s"Pilots/$name")

  def startPeople = {
    val (plane, controls, autopilot, altimeter, heading) = (
      self,
      actorForControls("ControlSurfaces"),
      actorForControls("Autopilot"),
      actorForControls("Altimeter"),
      actorForControls("HeadingIndicator"))

    val people = context.actorOf(
      Props(
        new IsolatedStopSupervisor with OneForOneSupervisor {
          def childStarter = {
            context
              .actorOf(
                Props(pilot(
                  plane,
                  autopilot,
                  heading,
                  altimeter)),
                pilotName)
            context
              .actorOf(
                Props(copilot(
                  plane,
                  autopilot,
                  controls,
                  altimeter)),
                copilotName)
          }
        }), "Pilots")

    context
      .actorOf(
        Props(leadFlightAttendant),
        config.getString(s"$configKey.leadAttendantName"))

    Await.result(people ? WaitForStart, 1.second)
  }

  def receive = {
    case AltitudeUpdate(altitudeInFeet) ⇒
      log debug (s"Altitude is now: $altitudeInFeet")
    case GiveMeControl ⇒
      log info ("Plane giving control.")
      sender ! Controls(actorForControls("ControlSurfaces"))
    case RequestCopilot ⇒ CopilotSelection(actorForPilots(s"$copilotName"))
    case CopilotIdentified ⇒ log debug s"Autopilot has identified the copilot"
    case PilotIdentified ⇒ log debug s"Copilot has identified the pilot"
    case LostControl ⇒ actorForControls("Autopilot") ! TakeControl
  }

  override def preStart() {
    startEquipment
    startPeople
    actorForControls("Altimeter") ! RegisterListener(self)
    actorForPilots(pilotName) :: actorForPilots(copilotName) :: Nil foreach {
      _ ! ReadyToGo
    }
  }

}

object Plane {
  case object GiveMeControl
  case object RequestCopilot
  case object CopilotIdentified
  case object PilotIdentified
  case object LostControl
  case class Controls(controls: ActorSelection)

  def apply() = new Plane with PilotProvider with AltimeterProvider with HeadingIndicatorProvider with LeadFlightAttendantProvider
}

trait PlaneProviderComponent {
  def plane: ActorRef
}
