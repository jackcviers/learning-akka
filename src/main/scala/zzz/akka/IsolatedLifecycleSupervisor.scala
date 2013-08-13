package zzz.akka

import akka.actor.Actor

trait IsolatedLifeCycleSupervisor extends Actor {
  import IsolatedLifeCycleSupervisor.{ Started, WaitForStart }

  def receive = {
    case WaitForStart ⇒ sender ! Started
    case m ⇒ throw new Exception(s"Don't call ${self.path.name} directly ($m).")
  }

  def childStarter(): Unit
  final override def preStart() { childStarter() }
  final override def postRestart(reason: Throwable) {}
  final override def preRestart(reason: Throwable, message: Option[Any]) {}
}

object IsolatedLifeCycleSupervisor {
  case object WaitForStart
  case object Started
}
