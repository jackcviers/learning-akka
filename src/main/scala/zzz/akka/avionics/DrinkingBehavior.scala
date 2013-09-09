package zzz.akka.avionics

import akka.actor.Actor
import akka.actor.ActorRef
import scala.concurrent.duration._
import scala.util.Random

class DrinkingBehavior(drinker: ActorRef) extends Actor { self: DrinkingResolution ⇒
  import DrinkingBehavior._

  implicit val ec = context.dispatcher

  var currentLevel = 0f
  val scheduler = context.system.scheduler
  val sobering = scheduler.schedule(initialSobering, soberingInterval, context.self, LevelChanged(-0.0001f))

  override def postStop() {
    sobering.cancel()
  }

  override def preStart() {
    drink()
  }

  def drink() = scheduler.scheduleOnce(drinkInterval, context.self, LevelChanged(0.005f))

  def receive = {
    case LevelChanged(amount) ⇒
      currentLevel = (currentLevel + amount).max(0f)
      drinker ! (if(currentLevel <= 0.01) {
        drink()
        FeelingSober
      } else if(currentLevel <= 0.03) {
        drink()
        FeelingTipsy
      } else FeelingLikeZaphod)
  }
}

object DrinkingBehavior {
  case class LevelChanged(level: Float)
  case object FeelingSober
  case object FeelingTipsy
  case object FeelingLikeZaphod

  def apply(drinker: ActorRef) = new DrinkingBehavior(drinker) with DrinkingResolution
}

trait DrinkingResolution {
  def initialSobering: FiniteDuration = 1.second
  def soberingInterval: FiniteDuration = 1.second
  def drinkInterval: FiniteDuration = Random.nextInt(300).seconds
}
