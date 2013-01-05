package org.brianmckenna.wartremover

trait WartRemover {
  def undefined[A]: A = sys.error("undefined")

  // any2stringadd allows anything to be added against String.
  class AmbiguousStringAdd {
    def +(b: String) = undefined
  }

  implicit def amb1any2stringadd(a: Any) = new AmbiguousStringAdd
  implicit def amb2any2stringadd(a: Any) = new AmbiguousStringAdd
}
