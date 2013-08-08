package zzz.akka.avionics


object ControlSurfaces {
  case class StickBack(amount: Float)
  case class StickForward(amount: Float)
}
