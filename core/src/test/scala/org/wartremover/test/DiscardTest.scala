package org.wartremover
package test

import org.wartremover.warts.Discard
import org.scalatest.funsuite.AnyFunSuite
import scala.concurrent.Future
import scala.util.Try

class DiscardTest extends AnyFunSuite with ResultAssertions {
  private def x: Discard = ???

  test("Future") {
    val result = WartTestTraverser(Discard.Future) {
      val f: Unit = Future.successful(x)
    }
    assertError(result)("discard `scala.concurrent.Future[org.wartremover.warts.Discard]`")
  }

  test("Try") {
    val result = WartTestTraverser(Discard.Try) {
      val f: Unit = Try(x)
    }
    assertError(result)("discard `scala.util.Try[org.wartremover.warts.Discard]`")
  }

  test("class body") {
    val result = WartTestTraverser(Discard.Try) {
      class Foo(x: Try[Discard]) {
        x.map(y => y)
      }
    }
    assertError(result)("discard `scala.util.Try[org.wartremover.warts.Discard]`")
  }

  test("function param") {
    Seq(
      WartTestTraverser(Discard.Try) {
        def f[A](x: Option[Try[A]]): Option[String] = {
          x.map { _ =>
            "x"
          }
        }
      },
      WartTestTraverser(Discard.Try) {
        def f[A](x: Option[Try[A]]): Option[String] = {
          x.map { y =>
            "x"
          }
        }
      }
    ).foreach { result =>
      assertError(result)("discard `scala.util.Try[A]`")
    }
  }

  test("wart obeys SuppressWarnings") {
    Seq(
      WartTestTraverser(Discard.Try) {
        @SuppressWarnings(Array("org.wartremover.warts.Discard$Try"))
        def f: Unit = Try(x)
      },
      WartTestTraverser(Discard.Future) {
        @SuppressWarnings(Array("org.wartremover.warts.Discard$Future"))
        def f: Unit = Try(x)
      }
    ).foreach { result =>
      assertEmpty(result)
    }
  }
}
