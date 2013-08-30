package zzz.akka

import akka.actor.{ AllForOneStrategy, OneForOneStrategy, SupervisorStrategy }
import akka.actor.SupervisorStrategy.Decider
import scala.concurrent.duration.Duration

trait Supervisor {
  def strategy(maxNumberOfRetries: Int, within: Duration)(decider: Decider): SupervisorStrategy
}

trait OneForOneSupervisor extends Supervisor {
  def strategy(maxNumberOfRetries: Int, within: Duration)(decider: Decider) = OneForOneStrategy(maxNumberOfRetries, within)(decider)
}

trait AllForOneSupervisor extends Supervisor {
  def strategy(maxNumberOfRetries: Int, within: Duration)(decider: Decider) = AllForOneStrategy(maxNumberOfRetries, within)(decider)
}
