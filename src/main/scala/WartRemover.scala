package org.brianmckenna.wartremover

trait WartRemover {
  def undefined[A]: A = sys.error("undefined")

  // The following belong in the "someone thinks you're too stupid to
  // use Scala features" pile. Not dangerous features. Just tricky.
  implicit def existentials = language.existentials
  implicit def higherKinds = language.higherKinds
  implicit def implicitConversions = language.implicitConversions

  // Make the following bad features ambiguous so they don't
  // work. Also can't import them. A compiler flag won't even make them
  // available!
  implicit def amb1postfixops: languageFeature.postfixOps = undefined
  implicit def amb2postfixops: languageFeature.postfixOps = undefined

  // any2stringadd allows anything to be added against String.
  class AmbiguousStringAdd {
    def +(b: String) = undefined
  }

  implicit def amb1any2stringadd(a: Any) = new AmbiguousStringAdd
  implicit def amb2any2stringadd(a: Any) = new AmbiguousStringAdd
}
