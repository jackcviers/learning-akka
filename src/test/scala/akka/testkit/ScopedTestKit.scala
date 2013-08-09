package akka.testkit

import org.specs2.specification.{ After, Scope }

trait ScopedTestKit extends Scope with After { self: TestKitBase ⇒
  import scala.language.implicitConversions
  import scala.concurrent.duration.DurationInt

  lazy val shutdown = system.shutdown()
  implicit def stringToDurationInt(i: String): DurationInt = new DurationInt(i.toInt)
  def after = shutdown
}

