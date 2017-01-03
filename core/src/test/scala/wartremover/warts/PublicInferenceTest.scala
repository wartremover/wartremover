package org.wartremover
package test

import org.scalatest.FunSuite
import org.wartremover.warts.PublicInference

class PublicInferenceTest extends FunSuite with ResultAssertions {
  test("Public members without type ascription are disabled") {
    val result = WartTestTraverser(PublicInference) {
      class c {
        val v = 1
        def f() = ()
      }
    }
    assertErrors(result)("Public member must have an explicit type ascription", 2)
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
