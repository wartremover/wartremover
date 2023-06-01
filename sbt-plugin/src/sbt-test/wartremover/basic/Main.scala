package example

class A {
  def &+(i: Int): A = this
}

object Main {
  def foo: A = {
    val a = new A
    a &+ 42
    a
  }
}

case class Foo(n: Int)
object Foo

case class B private (x: Int) {
  def y: B = B(x + 3)
}
object B {
  val x = B(2)
}
