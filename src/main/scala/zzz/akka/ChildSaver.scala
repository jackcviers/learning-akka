package zzz.akka

import akka.actor.{ Actor, ActorRef, Terminated }

trait ChildSavingLifeCycle { self: Actor ⇒

  // non-child creating initialization
  def initialize()

  // creates and starts all children
  def startChildren()

  // recreates and restarts failed child
  def recreateChild(child: ActorRef): Unit

  override def preStart() {
    initialize()
    startChildren()
  }

  override def preRestart(reason: Throwable, message: Option[Any]) {
    postStop()
  }

  override def postRestart(reason: Throwable) {
    initialize()
  }

  def childTerminatedReceive: Receive = {
    case Terminated(child) ⇒ recreateChild(child)
  }

}
