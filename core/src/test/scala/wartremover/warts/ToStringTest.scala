package org.wartremover
package test

import org.scalatest.FunSuite

import org.wartremover.warts.ToString

class ToStringTest extends FunSuite with ResultAssertions {
  test("can't use automatic toString method") {
    val result = WartTestTraverser(ToString) {
      class Foo(i: Int)
      val foo: Foo = new Foo(5)
      foo.toString
    }
    assertError(result)("class Foo does not override toString and automatic toString is disabled")
  }
  test("can't use automatic toString method of TypeRefs") {
    val result = WartTestTraverser(ToString) {
      def foo[A](a: A): String = a.toString
    }
    assertError(result)("class Any does not override toString and automatic toString is disabled")
  }
  test("can't use generated toString method of case classes") {
    val result = WartTestTraverser(ToString) {
      case class Foo(i: Int)
      Foo(5).toString
    }
    assertError(result)("class Foo does not override toString and automatic toString is disabled")
  }
  test("can't use generated toString method of case objects") {
    val result = WartTestTraverser(ToString) {
      case object Foo
      Foo.toString
    }
    assertError(result)("object Foo does not override toString and automatic toString is disabled")
  }
  test("can use overridden toString method") {
    val result = WartTestTraverser(ToString) {
      case class Foo(i: Int) {
        override val toString = s"Foo($i)"
      }
      class Bar(i: Int) {
        override val toString = s"Bar($i)"
      }
      case object Baz { override val toString = "Baz" }
      Foo(5).toString
      (new Bar(5)).toString
      Baz.toString
    }
    assertEmpty(result)
  }
  test("can use toString method of primitives") {
    val result = WartTestTraverser(ToString) {
     1.toString
     1l.toString
     1f.toString
     1.0.toString
     true.toString
     'a'.toString
     "a".toString
    }
    assertEmpty(result)
  }
  test("ToString wart obeys SuppressWarnings") {
    val result = WartTestTraverser(ToString) {
      case class Foo(i: Int)
      @SuppressWarnings(Array("org.wartremover.warts.ToString"))
      val i = Foo(5).toString
    }
    assertEmpty(result)
  }
}
