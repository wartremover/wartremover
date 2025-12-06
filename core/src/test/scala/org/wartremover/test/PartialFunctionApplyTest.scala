package org.wartremover
package test

import org.wartremover.warts.PartialFunctionApply
import org.scalatest.funsuite.AnyFunSuite

class PartialFunctionApplyTest extends AnyFunSuite with ResultAssertions {
  private def pf: PartialFunction[String, String] = { case a => a }

  test("can't use PartialFunction#apply") {
    Seq(
      WartTestTraverser(PartialFunctionApply) {
        pf("a")
      },
    ).foreach { result =>
      assertError(result)("PartialFunction#apply is disabled")
    }
  }

  test("can use Seq#apply and Map#apply") {
    val result = WartTestTraverser(PartialFunctionApply) {
      collection.Map("a" -> "b").apply("c")
      collection.Seq.empty[Int].apply(8)
      collection.immutable.Map("a" -> "b").apply("c")
      collection.immutable.Seq.empty[Int].apply(8)
      collection.mutable.Map("a" -> "b").apply("c")
      collection.mutable.Seq.empty[Int].apply(8)
      collection.immutable.List.empty[Int].apply(8)
    }
    assertEmpty(result)
  }

  test("can use try catch") {
    val pf2: PartialFunction[Throwable, Int] = { case _ => 3 }
    val result = WartTestTraverser(PartialFunctionApply) {
      try 2
      catch pf2
    }
    assertEmpty(result)
  }

  test("PartialFunctionApply wart obeys SuppressWarnings") {
    val result = WartTestTraverser(PartialFunctionApply) {
      @SuppressWarnings(Array("org.wartremover.warts.PartialFunctionApply"))
      val foo = pf("a")
    }
    assertEmpty(result)
  }
}
