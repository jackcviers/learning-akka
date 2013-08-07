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

class Wood extends Actor {
  def receive = {
    case _ ⇒ throw new Exception("Wood cannot hear you.")
  }
}

class Printing extends Actor {
  def receive = {
    case msg ⇒ println(msg)
  }
}

object BadShakespeareanMain {
  val system = ActorSystem("BadShakespearean")
  val actor = system.actorOf(Props[BadShakespearean], "Shake")
  val wood = system.actorOf(Props[Wood], "Wood")
  val printing = system.actorOf(Props[Printing], "Printing")
  def send(msg: String) {
    println(s"Me: $msg")
    actor ! msg
    Thread.sleep(100)
  }
  def sendWood(msg: String) {
    println(s"Me: $msg")
    wood ! msg
  }
  def sendPrint() {
    for(a ← 1 to 10) {
      printing ! a
    }
  }
  def main(args: Array[String]) {
    send("Good Morning")
    sendPrint()
    send("You're terrible")
    sendWood("If a tree falls in a forest, does it make a sound?")
    system.shutdown()
  }
}
