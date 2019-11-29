package org.wartremover
package test


import org.wartremover.warts.Null
import org.scalatest.funsuite.AnyFunSuite

class NullTest extends AnyFunSuite with ResultAssertions {
  test("can't use `null`") {
    val result = WartTestTraverser(Null) {
      println(null)
    }
    assertError(result)("null is disabled")
  }

  test("reference field placeholder is disabled") {
    val result = WartTestTraverser(Null) {
      class c {
        var s: String = _
      }
    }
    assertError(result)("null is disabled")

    val resultPrimitive = WartTestTraverser(Null) {
      class c {
        var i: Int = _
        var u: Unit = _
      }
    }
    assertEmpty(resultPrimitive)
  }

  test("can't use null in patterns") {
    val result = WartTestTraverser(Null) {
      val (a, b) = (1, null)
      println(a)
    }
    assertError(result)("null is disabled")
  }

  test("can't use null in default arguments") {
    val result = WartTestTraverser(Null) {
      class ClassWithArgs(val foo: String = null)
      case class CaseClassWithArgs(val foo: String = null)
    }
    assertErrors(result)("null is disabled", 2)
  }

  test("can't use null inside of Map#partition") {
    val result = WartTestTraverser(Null) {
      Map(1 -> "one", 2 -> "two").partition { case (k, v) => null.asInstanceOf[Boolean] }
    }
    assertError(result)("null is disabled")
  }

  test("can't use `Option#orNull`") {
    val result = WartTestTraverser(Null) {
      println(None.orNull)
    }
    assertError(result)("Option#orNull is disabled")
  }

  test("can use null in conditions") {
    val result = WartTestTraverser(Null) {
      null == ""
      null != ""
      "" == null
      "" != null
      null eq ""
      null ne ""
      "" eq null
      "" ne null
    }
    assertEmpty(result)
  }

  test("Null wart obeys SuppressWarnings") {
    val result = WartTestTraverser(Null) {
      @SuppressWarnings(Array("org.wartremover.warts.Null"))
      val foo = {
        println(null)
        println(None.orNull)
        val (a, b) = (1, null)
        println(a)
        Map(1 -> "one", 2 -> "two").partition { case (k, v) => null.asInstanceOf[Boolean] }
      }
    }
    assertEmpty(result)
  }

  test("Null wart obeys SuppressWarnings in classes with default arguments") {
    val result = WartTestTraverser(Null) {
      @SuppressWarnings(Array("org.wartremover.warts.Null"))
      class ClassWithArgs(val foo: String = null)
      @SuppressWarnings(Array("org.wartremover.warts.Null"))
      case class CaseClassWithArgs(val foo: String = null)
    }
    assertEmpty(result)
  }
}
