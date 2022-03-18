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
  test("issue 264 TypeTag") {
    val result = WartTestTraverser(AsInstanceOf) {
      import scala.reflect.runtime.universe.TypeTag

      def takesTypeTag[A : TypeTag](a: A): String = {
        val tt = implicitly[TypeTag[A]]
        s"The tt of A is $tt"
      }

      def exerciseIt(): String = {
        takesTypeTag("Hello")
      }
    }
    assertEmpty(result)
  }
  test("issue 266 GADTs") {
    /* scalac generate following AST

      c match {
        case (message: String)PrintLn(_) =>
          new Task[Unit](()).asInstanceOf[Task[A]]
      }
    */

    val result = WartTestTraverser(AsInstanceOf) {
      class Task[A](value: A)
      trait ~>[F[_], G[_]] { def apply[A](a: F[A]): G[A] }
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
