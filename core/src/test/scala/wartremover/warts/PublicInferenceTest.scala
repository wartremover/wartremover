package org.wartremover
package test

import org.wartremover.warts.PublicInference
import org.scalatest.funsuite.AnyFunSuite
import wartremover.test.PublicInferenceTestMacros

class PublicInferenceTest extends AnyFunSuite with ResultAssertions {
  test("Non-public fields and methods are allowed") {
    case class X(i: Int)
    val result = WartTestTraverser(PublicInference) {
      class Y {
        private[this] val a1 = X(2)
        private val a2 = X(2)
        protected[this] val a3 = X(2)
        protected val a4 = X(2)
        private[test] val a5 = X(2)

        private[this] def b1 = X(2)
        private def b2 = X(2)
        protected[this] def b3 = X(2)
        protected def b4 = X(2)
        private[test] def b5 = X(2)
      }
    }
    assertEmpty(result)
  }

  test("Public members without type ascription are disabled") {
    val result = WartTestTraverser(PublicInference) {
      class c {
        val v = 1
        def f() = ()
      }
    }
    assertErrors(result)("Public member must have an explicit type ascription", 2)
  }

  test("Inherited public members without type ascription are allowed") {
    val result = WartTestTraverser(PublicInference) {
      trait t {
        def m(): Unit
      }
      class c extends t {
        def m() = {}
      }
    }
    assertEmpty(result)
  }

  test("Public members with explicit types are enabled") {
    val result = WartTestTraverser(PublicInference) {
      class c {
        val v: Long = 1
        def f(): Unit = ()
      }
    }
    assertEmpty(result)
  }

  test("Non-public members without type ascription are enabled") {
    val result = WartTestTraverser(PublicInference) {
      class c {
        private val v = 1
        protected def f() = ()
      }
    }
    assertEmpty(result)
  }

  test("Explicitly typed multiline should work correctly") {
    val result = WartTestTraverser(PublicInference) {
      val a:
        Int = 1
    }
    assertEmpty(result)
  }

  test("Multiline should work correctly even if not explicitly typed") {
    val result = WartTestTraverser(PublicInference) {
      val a
        = 1
    }
    assertEmpty(result)
  }

  test("Multiline def should also work") {
    val res = WartTestTraverser(PublicInference) {
      object X {
        def f(
               a: Int
             ): Int = 1
      }
    }
    assertEmpty(res)
  }

  test("Even a very complex example should pass") {
    val result = WartTestTraverser(PublicInference) {
      class A {
        val a_val = 2 // fails #1
        var a_var = 2 // fails #2
        implicit val b_val = 2 // fails #3
        implicit var b_var = 2 // fails #4
        val c_var: Int = 3
        var c_val: Int = 3

        def a() = { // fails #5
          val a_val = 2
          var a_var = 2
          println {
            val k_val = 2
            var k_var = 2
            k_val + k_var
          }
        }
      }
    }
    assertErrors(result)("Public member must have an explicit type ascription", 5)
  }

  test("Macro expansions are ignored") {
    val result = WartTestTraverser(PublicInference) {
      val a = PublicInferenceTestMacros.define
    }
    assertEmpty(result)
  }

  test("Members of non-public classes are ignored") {
    val result = WartTestTraverser(PublicInference) {
      object o {
        private class c {
          val v = 1
          def f() = ()
        }
      }
    }
    assertEmpty(result)
  }

  test("Case classes are enabled") {
    val result = WartTestTraverser(PublicInference) {
      case class c(i: Int)
    }
    assertEmpty(result)
  }

  test("PublicInference should work with partial functions") {
    val result = WartTestTraverser(PublicInference) {
      Seq(1).collect { case 1 => 1 }
    }
    assertEmpty(result)
  }

  test("Public inference of string, char or boolean literals is allowed") {
    val result = WartTestTraverser(PublicInference) {
      class c {
        val s = ""
        val c = ' '
        val b = true
      }
    }
    assertEmpty(result)
  }

  test("Overridden getters are allowed") {
    val result = WartTestTraverser(PublicInference) {
      trait Foo {
        def bar: Int
      }

      object Foo {
        def apply(i: Int): Foo = new Foo {
          override val bar = i
        }
      }
    }
    assertEmpty(result)
  }

  test("Explicit self types are allowed") {
    val result = WartTestTraverser(PublicInference) {
      class c { self => }
    }
    assertEmpty(result)
  }

  test("PublicInference wart obeys SuppressWarnings") {
    val result = WartTestTraverser(PublicInference) {
      @SuppressWarnings(Array("org.wartremover.warts.PublicInference"))
      class c {
        val v = 1
        def f() = ()
      }
    }
    assertEmpty(result)
  }
}
