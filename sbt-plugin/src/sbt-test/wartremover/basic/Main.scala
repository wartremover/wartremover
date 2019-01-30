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
