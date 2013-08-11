package zzz.akka.avionics

import akka.actor.{ Actor, ActorLogging }

object Pilot {
  case object ReadyToGo
}

class Pilot extends Actor with ActorLogging {
  import Plane.GiveMeControl
  import Pilot.ReadyToGo

  val copilotName = context.system.settings.config.getString("zzz.akka.avionics.flightcrew.copilotName")

  var copilot = context.actorSelection("/deadLetters")
  def receive = {
    case ReadyToGo ⇒
      val parent = context.actorSelection("../")
      copilot = context.actorSelection("../" + copilotName)
      log info (s"parent: $parent")
      context.actorSelection("../") ! GiveMeControl

  }
}
