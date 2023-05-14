package org.wartremover
package test

import org.wartremover.warts.SeqUpdated
import org.scalatest.funsuite.AnyFunSuite

class SeqUpdatedTest extends AnyFunSuite with ResultAssertions {
  private def list: List[Int] = ???
  private def vector: Vector[String] = ???
  private def buffer: collection.mutable.Buffer[String] = ???
  private def seq: collection.Seq[Boolean] = ???
  private def map: Map[Int, Int] = ???

  test("disable Seq.updated") {
    val results = List(
      WartTestTraverser(SeqUpdated) {
        list.updated(1, 8)
      },
      WartTestTraverser(SeqUpdated) {
        vector.updated(1, "x")
      },
      WartTestTraverser(SeqUpdated) {
        buffer.updated(1, "y")
      },
      WartTestTraverser(SeqUpdated) {
        seq.updated(1, true)
      }
    )
    results.foreach { result =>
      assertError(result)("Seq.updated is disabled")
    }
  }

  test("Map.updated") {
    val result = WartTestTraverser(SeqUpdated) {
      map.updated(2, 3)
    }
    assertEmpty(result)
  }

  test("SuppressWarnings") {
    val result = WartTestTraverser(SeqUpdated) {
      @SuppressWarnings(Array("org.wartremover.warts.SeqUpdated"))
      def f =
        (
          list.updated(1, 1),
          vector.updated(2, "a"),
          seq.updated(3, false)
        )
    }
    assertEmpty(result)
  }
}
