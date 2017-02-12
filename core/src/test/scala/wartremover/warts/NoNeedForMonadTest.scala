package org.wartremover
package test

import org.scalatest.FunSuite

import org.wartremover.warts.NoNeedForMonad

class NoNeedForMonadTest extends FunSuite with ResultAssertions {
  test("Report cases where Applicative is enough") {
    val withWarnings = WartTestTraverser(NoNeedForMonad) {
      for {
        x <- List(1, 2, 3)
        y <- List(2, 3, 4)
      } yield x * y

      Option(1).flatMap(i => Option(2).map(j => i + j))
    }
    val noWarnings = WartTestTraverser(NoNeedForMonad) {
      for {
        x <- List(1,2,3)
        y <- x to 3
      } yield x * y

      Option(1).flatMap(i => Option(i + 1).map(j => i + j))
    }

    assertWarnings(withWarnings)("[wartremover:NoNeedForMonad] " + NoNeedForMonad.message, 2)

    assertEmpty(noWarnings)
  }

  test("Work properly with function literals, eta-expanded functions, objects with apply methods") {
    val etaExpanded = WartTestTraverser(NoNeedForMonad) {
      def fun(in: Int) = 14
      val xs = for {
        y <- Nil
        x <- Option(3) map fun
      } yield x

      Option(3).flatMap { case t => Some(t) }
    }

    val extendsFunction = WartTestTraverser(NoNeedForMonad) {
      object test extends Function1[Int, Option[Int]] {
        def apply(i: Int) = Option(i + 2)
      }
      object test2 {
        def apply(i: Int) = Option(i + 4)
      }

      for {
        x <- Option(1)
        res <- test(x)
      } yield res
      for {
        x <- Option(2)
        res <- test2(x)
      } yield res
    }


    assertWarnings(etaExpanded)("[wartremover:NoNeedForMonad] " + NoNeedForMonad.message, 1)

    assertEmpty(extendsFunction)
  }

  test("Handles unapply in for-comprehension") {
    val noWarnings = WartTestTraverser(NoNeedForMonad) {
      for {
        Some(x) <- List(Option(1), Option(2))
        (y, z)  <- (0 to x).zipWithIndex
      } yield x + y * z
    }

    assertEmpty(noWarnings)
  }

  test("NoNeedForMonad wart obeys SuppressWarnings") {
    val result = WartTestTraverser(NoNeedForMonad) {
      @SuppressWarnings(Array("org.wartremover.warts.NoNeedForMonad"))
      val foo = {
        for {
          x <- List(1, 2, 3)
          y <- List(2, 3, 4)
        } yield x * y

        Option(1).flatMap(i => Option(2).map(j => i + j))
      }
    }

    assertEmpty(result)
  }

  test("Does not produce false positives in one-level flatMaps") {
    val result = WartTestTraverser(NoNeedForMonad) {
      case class Group(singles: Seq[Int])
      val groups = Seq(Group(Seq(1, 2)), Group(Seq(3, 4)))
      groups flatMap (_.singles)
    }

    assertEmpty(result)
  }

  test("should not cause MatchError") {
    WartTestTraverser(NoNeedForMonad) {
      for {
        a <- List(1)
        b = a
      } yield b
    }
  }
}
