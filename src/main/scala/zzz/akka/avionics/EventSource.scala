package zzz.akka.avionics

import akka.actor.ActorRef

object EventSource {
  case class RegisterListener(listener: ActorRef)
  case class UnregisterListener(listener: ActorRef)
}

