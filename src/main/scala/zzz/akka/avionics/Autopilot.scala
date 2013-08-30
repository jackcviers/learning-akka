package zzz.akka.avionics

import akka.actor._

class Autopilot(plane: ActorRef) extends Actor with ActorLogging {
  import Plane._
  import Pilot._
  import Autopilot._

  def receive = {
    case ReadyToGo ⇒ plane ! RequestCopilot
    case CopilotSelection(copilot) ⇒ copilot ! Identify(None)
    case ActorIdentity(_, Some(r)) ⇒
      context.watch(r)
      plane ! CopilotIdentified
    case ActorIdentity(_, None) ⇒ log.debug("No copilot to watch!")
    case Terminated(_) ⇒ plane ! GiveMeControl
  }
}

object Autopilot {
  case class CopilotSelection(copilot: ActorSelection)
  def apply(plane: ActorRef) = new Autopilot(plane: ActorRef)
}
