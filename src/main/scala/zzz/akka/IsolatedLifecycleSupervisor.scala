package zzz.akka

import akka.actor.{ Actor, ActorInitializationException, ActorKilledException }
import akka.actor.SupervisorStrategy.{ Escalate, Resume, Stop }
import scala.concurrent.duration.Duration

abstract class IsolatedResumeSupervisor(
  maxNumberOfRetries: Int = -1,
  within: Duration = Duration.Inf)
  extends IsolatedLifeCycleSupervisor { self: Supervisor ⇒
  override val supervisorStrategy = strategy(maxNumberOfRetries, within) {
    case _: ActorInitializationException ⇒ Stop
    case _: ActorKilledException ⇒ Stop
    case _: Exception ⇒ Resume
    case _ ⇒ Escalate
  }
}

abstract class IsolatedStopSupervisor(
  maxNumberOfRetries: Int = -1,
  within: Duration)
  extends IsolatedLifeCycleSupervisor { self: Supervisor ⇒
  override val supervisorStrategy = strategy(maxNumberOfRetries, within) {
    case _: ActorInitializationException ⇒ Stop
    case _: ActorKilledException ⇒ Stop
    case _: Exception ⇒ Stop
    case _ ⇒ Escalate
  }
}

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
