package zzz.akka.avionics

import akka.actor.{ Actor, ActorLogging }

class Copilot extends Actor { self: PlaneProviderComponent ⇒
  import Pilot.ReadyToGo

  val pilotName = context.system.settings.config.getString("zzz.akka.avionics.flightcrew.pilotName")

  var controls = context.actorSelection("/deadLetters")
  var pilot = context.actorSelection("/deadLetters")
  var autopilot = context.actorSelection("/deadLetters")

  def receive = {
    case ReadyToGo ⇒
      pilot = context.actorSelection("../" + pilotName)
      autopilot = context.actorSelection("../Autopilot")

    case m ⇒ throw new Exception(s"Copilot dosen't understand $m")
  }
}

object Copilot {
  def apply() = new Copilot with PilotPlaneProvider
}
