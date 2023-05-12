package org.wartremover
package test

import org.wartremover.warts.SeqApply
import org.scalatest.funsuite.AnyFunSuite

class SeqApplyTest extends AnyFunSuite with ResultAssertions {
  private def list: List[Int] = ???
  private def vector: Vector[String] = ???
  private def buffer: collection.mutable.Buffer[String] = ???
  private def seq: collection.Seq[Boolean] = ???
  private def set: Set[Int] = ???

  test("disable Seq.apply") {
    val results = List(
      WartTestTraverser(SeqApply) {
        list(1)
      },
      WartTestTraverser(SeqApply) {
        vector.apply(1)
      },
      WartTestTraverser(SeqApply) {
        buffer.apply(1)
      },
      WartTestTraverser(SeqApply) {
        seq(1)
      }
    )
    results.foreach { result =>
      assertError(result)("Seq.apply is disabled")
    }
  }

  test("Set.apply") {
    val result = WartTestTraverser(SeqApply) {
      set(2)
    }
    assertEmpty(result)
  }

  test("SuppressWarnings") {
    val result = WartTestTraverser(SeqApply) {
      @SuppressWarnings(Array("org.wartremover.warts.SeqApply"))
      def f =
        (
          list(1),
          vector(2),
          seq(3)
        )
    }
    assertEmpty(result)
  }
}
