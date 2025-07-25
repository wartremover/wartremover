package org.wartremover
package test

import org.wartremover.warts.FinalVal
import org.scalatest.funsuite.AnyFunSuite

class FinalValTest extends AnyFunSuite with ResultAssertions {
  test("final val is disabled") {
    object X {
      final val v = 2
    }

    Seq(
      WartTestTraverser(FinalVal) {
        class c {
          final val v = X.v
        }
      },
      WartTestTraverser(FinalVal) {
        class c {
          final val v = 1
        }
      },
      WartTestTraverser(FinalVal) {
        class c {
          final val v = 2L
        }
      },
      WartTestTraverser(FinalVal) {
        class c {
          final val v = true
        }
      },
      WartTestTraverser(FinalVal) {
        class c {
          final val v = 'a'
        }
      },
      WartTestTraverser(FinalVal) {
        class c {
          final val v = "b"
        }
      },
      WartTestTraverser(FinalVal) {
        class c {
          final val v = 3.14d
        }
      },
      WartTestTraverser(FinalVal) {
        class c {
          final val v = 1.5f
        }
      },
      WartTestTraverser(FinalVal) {
        class c {
          final val v = classOf[Int]
        }
      }
    ).foreach { result =>
      assertError(result)("final val is disabled - use non-final val or final def or add type ascription")
    }
  }

  test("not contant types are enabled") {
    val result = WartTestTraverser(FinalVal) {
      object X {
        final val y: Int = 9
      }
      class c {
        final val v1 = Option(2)
        final val v2 = List(3)
        final val v3 = X.y
      }
    }
    assertEmpty(result)
  }

  test("final val alternatives are enabled") {
    val result = WartTestTraverser(FinalVal) {
      class c {
        val v = 1
        final def v2 = 1
        final val v3: Int = 1
      }
    }
    assertEmpty(result)
  }

  test("FinalVal wart obeys SuppressWarnings") {
    val result = WartTestTraverser(FinalVal) {
      class c {
        @SuppressWarnings(Array("org.wartremover.warts.FinalVal"))
        final val v = 1
      }
    }
    assertEmpty(result)
  }
}
