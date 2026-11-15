package org.wartremover
package test

import org.scalatest.funsuite.AnyFunSuite
import org.wartremover.warts.CatchThrowable

class CatchThrowableTest extends AnyFunSuite with ResultAssertions {
  test("report error") {
    Seq(
      WartTestTraverser(CatchThrowable) {
        try 2
        catch {
          case _: Throwable => 3
        }
      },
      WartTestTraverser(CatchThrowable) {
        try 2
        catch {
          case _: Throwable => 3
        } finally {
          println("finally")
        }
      },
      WartTestTraverser(CatchThrowable) {
        try 2
        catch {
          case x: Throwable => 3
        }
      }
    ).foreach { result =>
      assertError(result)("Catching Throwable can lead to unexpected behavior")
    }
  }

  test("not Throwable") {
    val result = WartTestTraverser(CatchThrowable) {
      val x1 = try {
        2
      } catch {
        case _: RuntimeException => 3
      }
      val x2 = try {
        2
      } catch {
        case _: IndexOutOfBoundsException => 3
      }
    }
    assertEmpty(result)
  }

  test("scala.util.Try.recover") {
    val result = WartTestTraverser(CatchThrowable) {
      scala.util.Try(2).recover { case _: Throwable => 3 }
    }
    assertEmpty(result)
  }

  test("obeys SuppressWarnings") {
    val result = WartTestTraverser(CatchThrowable) {
      @SuppressWarnings(Array("org.wartremover.warts.CatchThrowable"))
      def f: Int = {
        try 2
        catch {
          case _: Throwable => 3
        }
      }
    }
    assertEmpty(result)
  }
}
