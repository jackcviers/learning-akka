package zzz.akka.avionics

import akka.actor.Actor

class Autopilot extends Actor {
  def receive = Actor.emptyBehavior
}

object Autopilot {
  def apply() = new Autopilot()
}
