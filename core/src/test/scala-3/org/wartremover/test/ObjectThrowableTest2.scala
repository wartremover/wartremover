package org.wartremover
package test

import org.wartremover.warts.ObjectThrowable
import org.scalatest.funsuite.AnyFunSuite
import scala.util.control.NoStackTrace

class ObjectThrowableTest2 extends AnyFunSuite with ResultAssertions {
  test("enum no param") {
    Seq(
      WartTestTraverser(ObjectThrowable) {
        enum A extends java.lang.Error {
          case B
        }
      },
      WartTestTraverser(ObjectThrowable) {
        enum A(message: String) extends RuntimeException(message) {
          case B extends A("foo")
        }
      },
    ).foreach { result =>
      assertError(result)("Don't use empty param enum case if extends Throwable")
    }
  }

  test("enum with param") {
    val result = WartTestTraverser(ObjectThrowable) {
      enum A extends java.lang.Error {
        case B(x: Int)
        case C()
      }
    }
    assertEmpty(result)
  }

  test("enum NoStackTrace") {
    val result = WartTestTraverser(ObjectThrowable) {
      enum A extends java.lang.Error with NoStackTrace {
        case B
      }
    }
    assertEmpty(result)
  }
}
