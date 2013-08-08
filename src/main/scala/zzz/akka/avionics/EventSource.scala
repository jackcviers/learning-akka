package zzz.akka.avionics

import akka.actor.{ Actor, ActorRef }

object EventSource {
  case class RegisterListener(listener: ActorRef)
  case class UnregisterListener(listener: ActorRef)
}

trait EventSource { self: Actor ⇒
  import EventSource._

  var listeners = Vector.empty[ActorRef]

  def sendEvent[T](event: T): Unit = listeners foreach {
    _ ! event
  }

  def eventSourceRecieve: Receive = {
    case RegisterListener(listener) ⇒ listeners :+ listener
    case UnregisterListener(listener) ⇒ listeners = listeners filterNot { _ == listener }
  }
}
