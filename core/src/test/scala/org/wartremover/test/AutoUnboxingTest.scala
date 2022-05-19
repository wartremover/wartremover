package org.wartremover
package test

import org.wartremover.warts.AutoUnboxing
import org.scalatest.funsuite.AnyFunSuite

class AutoUnboxingTest extends AnyFunSuite with ResultAssertions {

  private def byte: java.lang.Byte = null
  private def short: java.lang.Short = null
  private def char: java.lang.Character = null
  private def int: java.lang.Integer = null
  private def long: java.lang.Long = null
  private def float: java.lang.Float = null
  private def double: java.lang.Double = null
  private def boolean: java.lang.Boolean = null

  test("Byte") {
    val result = WartTestTraverser(AutoUnboxing) {
      def x1: Byte = byte
      def x2: Option[Byte] = Some(byte)
    }
    assertErrors(result)("java.lang.Byte to scala.Byte auto unboxing", 2)
  }
  test("Short") {
    val result = WartTestTraverser(AutoUnboxing) {
      def x1: Short = short
    }
    assertError(result)("java.lang.Short to scala.Short auto unboxing")
  }
  test("Char") {
    val result = WartTestTraverser(AutoUnboxing) {
      def x1: Char = char
    }
    assertError(result)("java.lang.Character to scala.Char auto unboxing")
  }
  test("Int") {
    val result = WartTestTraverser(AutoUnboxing) {
      def x1: Int = int
    }
    assertError(result)("java.lang.Integer to scala.Int auto unboxing")
  }
  test("Long") {
    val result = WartTestTraverser(AutoUnboxing) {
      def x1: Long = long
    }
    assertError(result)("java.lang.Long to scala.Long auto unboxing")
  }
  test("Float") {
    val result = WartTestTraverser(AutoUnboxing) {
      def x1: Float = float
    }
    assertError(result)("java.lang.Float to scala.Float auto unboxing")
  }
  test("Double") {
    val result = WartTestTraverser(AutoUnboxing) {
      def x1: Double = double
    }
    assertError(result)("java.lang.Double to scala.Double auto unboxing")
  }
  test("Boolean") {
    val result = WartTestTraverser(AutoUnboxing) {
      def x1: Boolean = boolean
    }
    assertError(result)("java.lang.Boolean to scala.Boolean auto unboxing")
  }

  test("AutoUnboxing wart obeys SuppressWarnings") {
    val result = WartTestTraverser(AutoUnboxing) {
      @SuppressWarnings(Array("org.wartremover.warts.AutoUnboxing"))
      class X {
        def x1: Byte = byte
        def x2: Short = short
        def x3: Char = char
        def x4: Int = int
        def x5: Long = long
        def x6: Float = float
        def x7: Double = double
        def x8: Boolean = boolean
      }
    }
    assertEmpty(result)
  }
}
