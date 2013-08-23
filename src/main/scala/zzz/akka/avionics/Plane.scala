package zzz.akka.avionics

import akka.actor.ActorInitializationException
import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import akka.pattern._
import akka.util.Timeout
import scala.concurrent.Await
import scala.concurrent.duration._
import zzz.akka.IsolatedStopSupervisor

import zzz.akka.{ IsolatedResumeSupervisor, OneForOneSupervisor }

object Plane {
  case object GiveMeControl
  case class Controls(controls: ActorRef)

  def apply() = new Plane with PilotProvider with AltimeterProvider with LeadFlightAttendantProvider
}

class Plane extends Actor with ActorLogging { this: PilotProvider with AltimeterProvider with LeadFlightAttendantProvider ⇒
  import EventSource._
  import Altimeter._
  import ControlSurfaces._
  import Plane._
  import Pilot._
  import zzz.akka.IsolatedLifeCycleSupervisor.{ Started, WaitForStart }

  lazy val configKey = "zzz.akka.avionics.flightcrew"
  lazy val config = context.system.settings.config
  lazy val pilotName = config.getString(s"$configKey.pilotName")
  lazy val copilotName = config.getString(s"$configKey.copilotName")

  implicit val askTimeout = Timeout(1.second)

  def actorForControls(name: String) = context.actorFor(s"Equipment/$name")

  def startEquipment = {
    val controls = context.actorOf(
      Props(
        new IsolatedResumeSupervisor with OneForOneSupervisor {
          def childStarter = {
            val alt = context.actorOf(Props(altimeter), "Altimeter")
            context.actorOf(Props(autopilot), "Autopilot")
            context.actorOf(Props(ControlSurfaces(alt)), "ControlSurfaces")
          }

        }), "Equipment")

    Await.result(controls ? WaitForStart, 1.second)
  }

  def actorForPilots(name: String) = context.actorFor(s"Pilots/$name")

  def startPeople = {
    val (plane, controls, autopilot, altimeter) = (
      self,
      actorForControls("ControlSurfaces"),
      actorForControls("Autopilot"),
      actorForControls("Altimeter"))

    val people = context.actorOf(
      Props(
        new IsolatedStopSupervisor with OneForOneSupervisor {
          def childStarter = {
            context
              .actorOf(
                Props(
                  pilot(
                    plane,
                    autopilot,
                    controls,
                    altimeter)),
                pilotName)
            context
              .actorOf(
                Props(copilot),
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

  }

  override def preStart() {
    startEquipment
    startPeople
    actorForControls("Altimeter") ! RegisterListener(self)
    actorForPilots(pilotName) :: actorForPilots(copilotName) :: Nil foreach { _ ! ReadyToGo }
  }

}

trait PlaneProviderComponent {
  def plane: ActorRef
}
