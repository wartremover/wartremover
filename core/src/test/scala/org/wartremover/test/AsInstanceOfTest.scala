package org.wartremover
package test

import org.wartremover.warts.AsInstanceOf
import org.scalatest.funsuite.AnyFunSuite

class AsInstanceOfTest extends AnyFunSuite with ResultAssertions {
  test("asInstanceOf is disabled") {
    val result = WartTestTraverser(AsInstanceOf) {
      "abc".asInstanceOf[String]
    }
    assertError(result)("asInstanceOf is disabled")
  }
  test("asInstanceOf is disabled in anonymous PartialFunction") {
    val result = WartTestTraverser(AsInstanceOf) {
      List(1).collect { case x if { x.asInstanceOf[Integer]; true } => x }
    }
    assertError(result)("asInstanceOf is disabled")
  }
  test("issue 266 GADTs") {
    /* scalac generate following AST

      c match {
        case (message: String)PrintLn(_) =>
          new Task[Unit](()).asInstanceOf[Task[A]]
      }
     */

    val result = WartTestTraverser(AsInstanceOf) {
      import AsInstanceOfTest.~>
      class Task[A](value: A)
      sealed trait ConsoleIO[A]
      case class PrintLn(message: String) extends ConsoleIO[Unit]

      new (ConsoleIO ~> Task) {
        def apply[A](c: ConsoleIO[A]): Task[A] = c match {
          case PrintLn(_) => new Task(())
        }
      }
    }
    assertEmpty(result)
  }
  test("asInstanceOf wart obeys SuppressWarnings") {
    val result = WartTestTraverser(AsInstanceOf) {
      @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
      val foo = "abc".asInstanceOf[String]
    }
    assertEmpty(result)
  }
}

object AsInstanceOfTest {
  private trait ~>[F[_], G[_]] { def apply[A](a: F[A]): G[A] }
}
