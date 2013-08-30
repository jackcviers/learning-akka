package zzz.akka

import akka.actor.{ OneForOneStrategy, SupervisorStrategy, SupervisorStrategyConfigurator }
import akka.actor.SupervisorStrategy.Resume

class UsorGaurdianStrategy extends SupervisorStrategyConfigurator {
  def create = OneForOneStrategy() {
    case _ ⇒ akka.actor.SupervisorStrategy.Resume
  }
}
