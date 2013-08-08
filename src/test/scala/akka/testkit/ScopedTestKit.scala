package akka.testkit

import org.specs2.specification.{ After, Scope }
import org.specs2.time.Duration
import scala.concurrent.duration.{ Duration => ScalaDuration }

trait ScopedTestKit extends Scope with After { self: TestKitBase ⇒
  import scala.language.implicitConversions
  lazy val shutdown = system.shutdown()
  implicit def durationToScalaDuration(d: Duration): ScalaDuration = ScalaDuration(d.toMillis, "millis")
  implicit def scalaDurationToDuration(s: ScalaDuration): Duration = new Duration(s.toMillis)
  def after = shutdown
}
