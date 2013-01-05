package org.brianmckenna.wartremover

trait WartRemover {
  def undefined[A]: A = sys.error("undefined")

  // The following belong in the "someone thinks you're too stupid to
  // use Scala features" pile. Not dangerous features. Just tricky.
  implicit def existentials = language.existentials
  implicit def higherKinds = language.higherKinds
  implicit def implicitConversions = language.implicitConversions

  // any2stringadd allows anything to be added against String.
  class AmbiguousStringAdd {
    def +(b: String) = undefined
  }

  implicit def amb1any2stringadd(a: Any) = new AmbiguousStringAdd
  implicit def amb2any2stringadd(a: Any) = new AmbiguousStringAdd
}
