package com.github.jackcviers

import akka.actor.{Actor, ActorSystem, Props}

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

class Mine extends Actor {
  def receive = {
    case "Hello" ⇒ println("Him: Hi")
    case 42 ⇒ println("Him: I don't know the question. Go ask the Earth Mark II.")
    case s: String ⇒ println(s"Him: You sent me a string: $s.")
    case Alpha(Beta(b1, Gamma(g1)), Beta(b2, Gamma(g2))) ⇒ println(s"Him: beta1: $b1, beta2: $b2, gamma1: $g1, gamma2: $g2")
    case _ ⇒ println("Him: Huh?")
  }
}

object BadShakespeareanMain {
  val system = ActorSystem("BadShakespearean")
  val actor = system.actorOf(Props[BadShakespearean], "Shake")
  val wood = system.actorOf(Props[Wood], "Wood")
  val printing = system.actorOf(Props[Printing], "Printing")
  val mine = system.actorOf(Props[Mine], "Mine")
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
    for (a ← 1 to 10) {
      printing ! a
    }
  }
  def sendMine() {
    println("Me: Hello")
    mine ! "Hello"
    Thread.sleep(100)
    println("Me: 42")
    mine ! 42
    Thread.sleep(100)
    println("Me: Alpha!")
    mine ! Alpha(b1 = Beta(b = "A", g = Gamma(g = "Z")), b2 = Beta(b = "B", g = Gamma(g = "Y")))
    Thread.sleep(100)
    println("Me: Gamma(How much wood could a woodchuck chuck if a woodchuck could chuck wood.)")
    mine ! Gamma(g = "How much wood could a woodchuck chuck if a woodchuck could chuck wood.")
  }
  def main(args: Array[String]) {
    send("Good Morning")
    sendPrint()
    send("You're terrible")
    sendWood("If a tree falls in a forest, does it make a sound?")
    sendMine()
    system.shutdown()
  }
}
