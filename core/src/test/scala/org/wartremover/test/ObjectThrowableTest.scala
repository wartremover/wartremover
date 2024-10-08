package org.wartremover
package test

import org.wartremover.warts.ObjectThrowable
import org.scalatest.funsuite.AnyFunSuite

class ObjectThrowableTest extends AnyFunSuite with ResultAssertions {
  test("report error") {
    Seq(
      WartTestTraverser(ObjectThrowable) {
        object MyError extends Throwable
      },
      WartTestTraverser(ObjectThrowable) {
        object MyError extends RuntimeException
      },
      WartTestTraverser(ObjectThrowable) {
        object MyError extends java.lang.Error
      },
    ).foreach { result =>
      assertError(result)("use class if extends Throwable")
    }
  }

  trait MyNoStackTrace extends scala.util.control.NoStackTrace

  test("don't report error") {
    val result = WartTestTraverser(ObjectThrowable) {
      class MyError1 extends Throwable

      object MyError2 extends java.io.Serializable

      object MyError3 extends scala.util.control.NoStackTrace

      object MyError4 extends MyNoStackTrace
    }

    assertEmpty(result)
  }

  test("obeys SuppressWarnings") {
    val result = WartTestTraverser(ObjectThrowable) {
      @SuppressWarnings(Array("org.wartremover.warts.ObjectThrowable"))
      object MyError extends Throwable
    }
    assertEmpty(result)
  }
}
