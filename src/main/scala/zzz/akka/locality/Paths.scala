package zzz.akka.locality

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props

object Paths {
  def main(args: Array[String]) {
    val system = ActorSystem("TheSystem")
    val a = system.actorOf(Props(new Actor {
      def receive = Actor.emptyBehavior
    }), "AnonymousActor")

    println(a.path)
    println(a.path.elements.mkString("/", "/", ""))
    println(a.path.name)
    system.shutdown()
  }
}
