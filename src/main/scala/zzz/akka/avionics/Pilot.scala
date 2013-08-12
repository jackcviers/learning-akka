package zzz.akka.avionics

import akka.actor.{ Actor, ActorLogging }

class Pilot extends Actor { self: PlaneProviderComponent ⇒
  import Pilot._
  import Plane.{ Controls, GiveMeControl }


  val copilotName = context.system.settings.config.getString("zzz.akka.avionics.flightcrew.copilotName")

  var copilot = context.actorSelection("/deadLetters")
  var controls = context.actorSelection("/deadLetters")
  var autopilot = context.actorSelection("/deadLetters")
  def receive = {
    case ReadyToGo ⇒
      copilot = context.actorSelection("../" + copilotName)
      autopilot = context.actorSelection("../Autopilot")
      plane ! GiveMeControl
    case Controls(controlSurfaces) ⇒
      controls = context.actorSelection(controlSurfaces.path)
    case m ⇒ throw new Exception(s"Pilot doesn't understand: $m")
  }
}


object Pilot {
  case object ReadyToGo
  def apply() = new Pilot with PilotPlaneProvider
}

trait PilotPlaneProvider extends PlaneProviderComponent { self: Actor ⇒
  def plane = context.parent
}
