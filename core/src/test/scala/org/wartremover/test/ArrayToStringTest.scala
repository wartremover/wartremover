package org.wartremover
package test

import org.wartremover.warts.ArrayToString
import org.scalatest.funsuite.AnyFunSuite

class ArrayToStringTest extends AnyFunSuite with ResultAssertions {
  private def arrayInt: Array[Int] = Array.empty
  private def arrayLong: ArrayLong = Array.empty
  private val arrayString: Array[String] = Array.empty
  private def arrayRaw: Array[?] = Array.empty[Int]
  private def arrayAny: Array[Any] = Array.empty
  type ArrayLong = Array[Long]

  test("Array.toString is disabled") {
    Seq(
      WartTestTraverser(ArrayToString) {
        arrayLong.toString
      },
      WartTestTraverser(ArrayToString) {
        arrayInt.toString
      },
      WartTestTraverser(ArrayToString) {
        arrayString.toString
      },
      WartTestTraverser(ArrayToString) {
        arrayRaw.toString
      },
      WartTestTraverser(ArrayToString) {
        arrayAny.toString
      }
    ).foreach(result => assertError(result)("Array.toString is disabled"))
  }

  test("not Array") {
    val result = WartTestTraverser(ArrayToString) {
      Nil.toString + 2.toString
    }
    assertEmpty(result)
  }

  test("SuppressWarnings") {
    val result = WartTestTraverser(ArrayToString) {
      @SuppressWarnings(Array("org.wartremover.warts.ArrayToString"))
      def f = Seq(
        arrayInt.toString,
        arrayString.toString,
        arrayRaw.toString,
        arrayAny.toString,
      )
    }
    assertEmpty(result)
  }
}
