package com.github.jackcviers

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props

class BadShakespearean extends Actor {
  def receive = {
    case "Good Morning" ⇒ println("Him: Forsooth 'tis the 'morn, but mourneth for thou doest I do!")
    case "You're terrible" ⇒ println("Him: Yup")
  }
}

object BadShakespeareanMain {
  val system = ActorSystem("BadShakespearean")
  val actor = system.actorOf(Props[BadShakespearean], "Shake")
  def send(msg: String) {
    println(s"Me: $msg")
    actor ! msg
    Thread.sleep(100)
  }
  def main(args: Array[String]) {
    send("Good Morning")
    send("You're terrible")
    system.shutdown()
  }
}
