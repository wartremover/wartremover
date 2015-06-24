package org.brianmckenna.wartremover
package test

import org.scalatest.FunSuite

import org.brianmckenna.wartremover.warts.ToString

class ToStringTest extends FunSuite {
  test("can't use automatic toString method") {
    val result = WartTestTraverser(ToString) {
      class Foo(i: Int)
      val foo: Foo = new Foo(5)
		foo.toString
    }
    assertResult(List("class Foo does not override toString and automatic toString is disabled"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
  test("can't use automatic toString method of TypeRefs") {
    val result = WartTestTraverser(ToString) {
      def foo[A](a: A): String = a.toString
    }
    assertResult(List("class Any does not override toString and automatic toString is disabled"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
  test("can't use generated toString method of case classes") {
    val result = WartTestTraverser(ToString) {
      case class Foo(i: Int)
		Foo(5).toString
    }
    assertResult(List("class Foo does not override toString and automatic toString is disabled"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
  test("can't use generated toString method of case objects") {
    val result = WartTestTraverser(ToString) {
      case object Foo
		Foo.toString
    }
    assertResult(List("object Foo does not override toString and automatic toString is disabled"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
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
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
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
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
  test("ToString wart obeys SuppressWarnings") {
    val result = WartTestTraverser(ToString) {
      case class Foo(i: Int)
      @SuppressWarnings(Array("org.brianmckenna.wartremover.warts.ToString"))
      val i = Foo(5).toString
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
}
