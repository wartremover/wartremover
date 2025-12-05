package org.wartremover
package test

import org.wartremover.warts.FutureFilter
import org.scalatest.funsuite.AnyFunSuite
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FutureFilterTest extends AnyFunSuite with ResultAssertions {
  private def f: Future[Int] = Future.successful(2)
  test("filter") {
    val result = WartTestTraverser(FutureFilter) {
      f.filter(_ => true)
    }
    assertError(result)("Future.filter is disabled")
  }

  test("withFilter") {
    Seq(
      WartTestTraverser(FutureFilter) {
        f.withFilter(_ => true)
      },
      WartTestTraverser(FutureFilter) {
        for {
          x <- f
          if x == 2
        } yield x
      },
      WartTestTraverser(FutureFilter) {
        for {
          (y1, y2) <- f.map(x => (x, x))
        } yield (y1 + y2)
      }
    ).foreach { result =>
      assertError(result)("Future.withFilter is disabled")
    }
  }

  test("FutureFilter wart obeys SuppressWarnings") {
    val result = WartTestTraverser(FutureFilter) {
      @SuppressWarnings(Array("org.wartremover.warts.FutureFilter"))
      class A {
        def x1 = f.filter(_ => true)
        def x2 = f.withFilter(_ => false)
      }
    }
    assertEmpty(result)
  }
}
