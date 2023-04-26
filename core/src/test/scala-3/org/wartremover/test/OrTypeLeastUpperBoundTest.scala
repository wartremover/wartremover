package org.wartremover
package test

import org.wartremover.warts.OrTypeLeastUpperBound
import org.scalatest.funsuite.AnyFunSuite

class OrTypeLeastUpperBoundTest extends AnyFunSuite with ResultAssertions {

  import OrTypeLeastUpperBoundTest.*

  test("All") {
    val results = Seq(
      WartTestTraverser(OrTypeLeastUpperBound.All) {
        List(IArray(1), false)
      },
      WartTestTraverser(OrTypeLeastUpperBound.All) {
        List(A1(1), B(2))
      },
      WartTestTraverser(OrTypeLeastUpperBound.All) {
        List("a", true)
      },
      WartTestTraverser(OrTypeLeastUpperBound.All) {
        List(Right[Int, Int](1), Option("a"))
      },
      WartTestTraverser(OrTypeLeastUpperBound.All) {
        Seq(Option(8), Option("V"))
      },
      WartTestTraverser(OrTypeLeastUpperBound.All) {
        def f[R: R1: R2](b: Boolean): Eff[R, Int] = {
          if (b) {
            eff1[R, String]("2")
          } else {
            eff2(Some(3))
          }
        }.map { x => 4 }
      },
    )
    results.foreach { result =>
      assert(result.errors.size == 1)
      assert(result.errors.forall(_.contains("least upper bound is")), result)
    }

    val mustEmpty = WartTestTraverser(OrTypeLeastUpperBound.All) {
      for {
        x1 <- Option(5)
        x2 <- x1 match {
          case 2 =>
            Option(L(3))
          case _ =>
            Option(R("a"))
        }
      } yield ()
      Tuple2(
        _2 = 3,
        _1 = if (true) None else Some(2)
      )
      List(Right(1), Left(2))
      List(Some(77), None)
      List(false, 1.5)
    }
    assertEmpty(mustEmpty)
  }

  test("Any") {
    val result = WartTestTraverser(OrTypeLeastUpperBound.Any) {
      List(IArray(1), false)
    }
    assertError(result)("least upper bound is `scala.Any`. `scala.IArray$package.IArray[scala.Int] | scala.Boolean`")
  }

  test("AnyRef") {
    val result = WartTestTraverser(OrTypeLeastUpperBound.AnyRef) {
      List(A1(1), B(2))
    }
    assertError(result)(
      "least upper bound is `java.lang.Object & scala.Product & java.io.Serializable`. `org.wartremover.test.OrTypeLeastUpperBoundTest.A1 | org.wartremover.test.OrTypeLeastUpperBoundTest.B`"
    )
    val mustEmpty = WartTestTraverser(OrTypeLeastUpperBound.AnyRef) {
      List(4L, 9)
    }
    assertEmpty(mustEmpty)
  }

  test("Matchable") {
    val result = WartTestTraverser(OrTypeLeastUpperBound.Matchable) {
      List("a", true)
    }
    assertError(result)("least upper bound is `scala.Matchable`. `java.lang.String | scala.Boolean`")
    val mustEmpty = WartTestTraverser(OrTypeLeastUpperBound.Matchable) {
      List(2L, 3.5)
    }
    assertEmpty(mustEmpty)
  }

  test("Product") {
    val mustError1 = WartTestTraverser(OrTypeLeastUpperBound.Product) {
      List(A1(1), B(2))
    }
    assertError(mustError1)(
      "least upper bound is `java.lang.Object & scala.Product & java.io.Serializable`. `org.wartremover.test.OrTypeLeastUpperBoundTest.A1 | org.wartremover.test.OrTypeLeastUpperBoundTest.B`"
    )

    val mustError2 = WartTestTraverser(OrTypeLeastUpperBound.Product) {
      List(Right[Int, Int](1), Option("a"))
    }
    assertError(mustError2)(
      "least upper bound is `java.lang.Object & scala.Product & java.io.Serializable`. `scala.util.Right[scala.Int, scala.Int] | scala.Option[java.lang.String]`"
    )

    val mustEmpty = WartTestTraverser(OrTypeLeastUpperBound.Product) {
      List(A1(1), A2(2))
    }
    assertEmpty(mustEmpty)
  }

  test("Serializable") {
    val result = WartTestTraverser(OrTypeLeastUpperBound.Serializable) {
      List(A1(1), B(2))
    }
    assertError(result)(
      "least upper bound is `java.lang.Object & scala.Product & java.io.Serializable`. `org.wartremover.test.OrTypeLeastUpperBoundTest.A1 | org.wartremover.test.OrTypeLeastUpperBoundTest.B`"
    )
  }
}

object OrTypeLeastUpperBoundTest {
  sealed abstract class A
  case class A1(x: Int) extends A
  case class A2(x: Int) extends A

  case class B(x: Int)

  sealed abstract class E[+A, +B] extends Product with Serializable
  final case class L[+A](a: A) extends E[A, Nothing]
  final case class R[+B](b: B) extends E[Nothing, B]

  trait Eff[R, A] {
    def map[B](f: A => B): Eff[R, B] = ???
  }

  trait R1[A]
  trait R2[A]

  def eff1[R: R1, A](a: A): Eff[R, A] = ???
  def eff2[R: R2, A](a: Option[A]): Eff[R, A] = ???
}
