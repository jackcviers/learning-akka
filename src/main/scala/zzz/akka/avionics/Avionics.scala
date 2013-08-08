package zzz.akka.avionics

import ControlSurfaces.StickBack
import Plane.GiveMeControl
import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

object Avionics {
  implicit val timeout = Timeout(5 seconds)

  val system = ActorSystem("PlaneSimulation")
  val plane = system.actorOf(Props[Plane], "Plane")

  def main(args: Array[String]) {
    val control = Await.result((plane ? GiveMeControl).mapTo[ActorRef], 5 seconds)

    system.scheduler.scheduleOnce(200 millis) {
      control ! StickBack(1f)
    }

    system.scheduler.scheduleOnce(1 seconds) {
      control ! StickBack(0f)
    }

    system.scheduler.scheduleOnce(3 seconds) {
      control ! StickBack(0.5f)
    }

    system.scheduler.scheduleOnce(4 seconds) {
      control ! StickBack(0f)
    }
    system.scheduler.scheduleOnce(5 seconds) {
      system.shutdown()
    }

  }
}

