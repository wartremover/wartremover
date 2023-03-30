package org.wartremover
package test

import org.wartremover.warts.SizeToLength
import org.scalatest.funsuite.AnyFunSuite

class SizeToLengthTest extends AnyFunSuite with ResultAssertions {
  test("suggest length if Array or String") {
    def byte: Byte = 1
    def short: Short = 1
    def float: Float = 2.8f
    List(
      WartTestTraverser(SizeToLength) {
        def f[A](a: Array[A]): Int = a.size
      },
      WartTestTraverser(SizeToLength) {
        Array(byte).size
      },
      WartTestTraverser(SizeToLength) {
        Array(short).size
      },
      WartTestTraverser(SizeToLength) {
        Array(float).size
      },
      WartTestTraverser(SizeToLength) {
        "y".size
      },
      WartTestTraverser(SizeToLength) {
        Array(1).size
      },
      WartTestTraverser(SizeToLength) {
        Array("a").size
      },
      WartTestTraverser(SizeToLength) {
        Array(2L).size
      },
      WartTestTraverser(SizeToLength) {
        Array[Double](2.5).size
      },
      WartTestTraverser(SizeToLength) {
        Array.empty[Unit].size
      },
      WartTestTraverser(SizeToLength) {
        Array('c').size
      },
      WartTestTraverser(SizeToLength) {
        Array(true, false).size
      }
    ).foreach { result =>
      assert(result.errors.nonEmpty)
      assert(result.errors.forall(_.endsWith("Maybe you should use `length` instead of `size`")), result)
    }
  }

  test("don't suggest if another types") {
    val result = WartTestTraverser(SizeToLength) {
      List(1).size
      Option("a").size
    }
    assertEmpty(result)
  }

  test("SuppressWarnings") {
    val result = WartTestTraverser(SizeToLength) {
      @SuppressWarnings(Array("org.wartremover.warts.SizeToLength"))
      def x1 = List(
        Array(1).size,
        "x".size,
        Array(false).size,
      )
    }
    assertEmpty(result)
  }
}
