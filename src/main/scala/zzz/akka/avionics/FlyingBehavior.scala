package zzz.akka.avionics

import akka.actor.ActorContext
import akka.actor.ActorIdentity
import akka.actor.Identify
import akka.actor.Terminated
import akka.actor.{ Actor, ActorRef, ActorSelection, FSM }
import akka.actor.FSM._
import scala.concurrent.duration._
import scala.math.{ abs, signum }

class FlyingBehavior(plane: ActorRef, heading: ActorRef, altimeter: ActorRef)
  extends Actor
  with FSM[FlyingBehavior.State, FlyingBehavior.Data] {
  import FlyingBehavior._
  import Pilot._
  import Plane._
  import Altimeter._
  import HeadingIndicator._
  import EventSource._

  case object Adjust

  startWith(Idle, Uninitialized)
  when(Idle) {
    case Event(Fly(target), _) ⇒
      goto(PreparingToFly) using FlightData(
        context.system.deadLetters,
        calcElevator,
        calcAilerons,
        target,
        CourseStatus(-1, -1, 0, 0))
  }

  onTransition {
    case Idle -> PreparingToFly ⇒
      plane ! GiveMeControl
      registerInstruments()
      setTimer("PreparationStateTimeout", StateTimeout, 5.seconds, repeat = false)
    case PreparingToFly -> Flying ⇒
      cancelTimer("PreparationStateTimeout")
      setTimer("Adjustment", Adjust, 200.milliseconds, repeat = true)
    case PreparingToFly -> _ ⇒ cancelTimer("PreparationStateTimeout")
    case Flying -> _ ⇒ cancelTimer("Adjustment")
    case _ -> Idle ⇒ unregisterInstruments()
  }

  when(PreparingToFly)(transform {
    case Event(HeadingUpdate(head), d: FlightData) ⇒
      stay using d.copy(
        status = d.status.copy(
          heading = head, headingSinceMillis = currentMillis))
    case Event(AltitudeUpdate(alt), d: FlightData) ⇒
      stay using d.copy(
        status = d.status.copy(
          altitude = alt, altitudeSinceMillis = currentMillis))
    case Event(Controls(ctrls), d: FlightData) ⇒
      ctrls ! Identify('ctrls)
      stay using d
    case Event(ActorIdentity('ctrls, Some(ctrls)), d: FlightData) ⇒
      context watch ctrls
      stay using d.copy(controls = ctrls)
    case Event(ActorIdentity('ctrls, None), d: FlightData) ⇒
      plane ! GiveMeControl
      stay using d
    case Event(StateTimeout, _) ⇒
      plane ! LostControl
      goto(Idle)
    case Event(Terminated(_), d: FlightData) ⇒
      plane ! GiveMeControl
      stay using d.copy(controls = context.system.deadLetters)
  } using {
    case s if prepComplete(s.stateData) ⇒ s.copy(stateName = Flying)
  })

  when(Flying) {
    case Event(AltitudeUpdate(alt), d: FlightData) ⇒
      stay using d.copy(status = d
        .status.copy(altitude = alt, altitudeSinceMillis = currentMillis))
    case Event(HeadingUpdate(head), d: FlightData) ⇒
      stay using d.copy(status = d
        .status.copy(heading = head, headingSinceMillis = currentMillis))
    case Event(Adjust, flightData: FlightData) ⇒ stay using adjust(flightData)
    case Event(NewBankCalculator(f), d: FlightData) ⇒ stay using d.copy(bankCalc = f)
    case Event(NewElevatorCalculator(f), d: FlightData) ⇒ stay using d.copy(elevCalc = f)
    case Event(Terminated(_), d: FlightData) ⇒
      plane ! LostControl
      goto(Idle)
  }

  whenUnhandled {
    case Event(RelinquishControl, _) ⇒ goto(Idle)
  }

  def adjust(data: FlightData): FlightData = {
    val FlightData(c, elevCalc, bankCalc, t, s) = data
    c ! elevCalc(t, s)
    c ! bankCalc(t, s)
    data
  }

  def prepComplete: PartialFunction[Data, Boolean] = {
    case FlightData(c, _, _, _, s) if prepExecutionGaurd(c, s) ⇒ true
    case _ ⇒ false
  }

  def prepExecutionGaurd(controls: ActorRef, status: CourseStatus) = controls != context.system.deadLetters &&
    status.heading != -1f && status.altitude != -1f

  def registerInstruments() {
    instruments foreach { (_ ! RegisterListener(self)) }
  }

  def unregisterInstruments() {
    instruments foreach { _ ! UnregisterListener(self) }
  }

  private def instruments = List(heading, altimeter)

  initialize

}

object FlyingBehavior {
  import ControlSurfaces._

  sealed trait State
  case object Idle extends State
  case object Flying extends State
  case object PreparingToFly extends State
  case class NewElevatorCalculator(f: Calculator)
  case class NewBankCalculator(f: Calculator)

  case class CourseTarget(altitude: Double, heading: Float, byMillis: Long)
  case class CourseStatus(altitude: Double, heading: Float, headingSinceMillis: Long, altitudeSinceMillis: Long)

  type Calculator = (CourseTarget, CourseStatus) ⇒ Any

  sealed trait Data
  case object Uninitialized extends Data

  case class FlightData(
    controls: ActorRef,
    elevCalc: Calculator,
    bankCalc: Calculator,
    target: CourseTarget,
    status: CourseStatus) extends Data

  case class Fly(target: CourseTarget)

  def currentMillis = System.currentTimeMillis

  def calcElevator(target: CourseTarget, status: CourseStatus): Any = {
    val alt = (target.altitude - status.altitude).toFloat
    val duration = target.byMillis - status.altitudeSinceMillis
    if (alt < 0) StickForward(-(alt / duration)) else StickBack(alt / duration)
  }

  def calcAilerons(target: CourseTarget, status: CourseStatus): Any = {
    val diff = target.heading - status.heading
    val duration = target.byMillis - status.headingSinceMillis
    val amount = if (abs(diff) < 180) diff else signum(diff) * (abs(diff) - 360f)
    if (amount > 0) StickRight(amount / duration) else StickLeft(-(amount / duration))
  }

}
