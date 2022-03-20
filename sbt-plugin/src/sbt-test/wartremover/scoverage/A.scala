package example

class A(i: Int) {
  def this(d: Double) = {
    this(d.toInt)
  }

  val x1 = "1"
}

// https://github.com/wartremover/wartremover/issues/475
object B {
  val x2 = "2"
}
