package org.wartremover
package test

import org.wartremover.warts.RedundantConversions
import org.scalatest.funsuite.AnyFunSuite

class RedundantConversionsTest extends AnyFunSuite with ResultAssertions {
  test("redundant toInt disabled") {
    val result = WartTestTraverser(RedundantConversions) {
      def int: Int = ???
      int.toInt
    }
    assertError(result)("redundant toInt conversion")
  }
  test("redundant toLong disabled") {
    val result = WartTestTraverser(RedundantConversions) {
      def long: Long = ???
      long.toLong
    }
    assertError(result)("redundant toLong conversion")
  }
  test("redundant toFloat disabled") {
    val result = WartTestTraverser(RedundantConversions) {
      def float: Float = ???
      float.toFloat
    }
    assertError(result)("redundant toFloat conversion")
  }
  test("redundant toDouble disabled") {
    val result = WartTestTraverser(RedundantConversions) {
      def double: Double = ???
      double.toDouble
    }
    assertError(result)("redundant toDouble conversion")
  }
  test("redundant toByte disabled") {
    val result = WartTestTraverser(RedundantConversions) {
      def byte: Byte = ???
      byte.toByte
    }
    assertError(result)("redundant toByte conversion")
  }
  test("redundant toShort disabled") {
    val result = WartTestTraverser(RedundantConversions) {
      def short: Short = 99
      short.toShort
    }
    assertError(result)("redundant toShort conversion")
  }
  test("redundant toChar disabled") {
    val result = WartTestTraverser(RedundantConversions) {
      def char: Char = ???
      char.toChar
    }
    assertError(result)("redundant toChar conversion")
  }
  test("no error another AnyVal type conversions") {
    def i: Int = ???
    def b: Byte = ???
    def s: Short = ???
    def c: Char = ???
    def l: Long = ???
    def f: Float = ???
    def d: Double = ???

    val result = WartTestTraverser(RedundantConversions) {
      // format: off
      Seq(()     , i.toByte, i.toShort, i.toChar, i.toLong, i.toFloat, i.toDouble)
      Seq(b.toInt, ()      , b.toShort, b.toChar, b.toLong, b.toFloat, b.toDouble)
      Seq(s.toInt, s.toByte, ()       , s.toChar, s.toLong, s.toFloat, s.toDouble)
      Seq(c.toInt, c.toByte, c.toShort, ()      , c.toLong, c.toFloat, c.toDouble)
      Seq(l.toInt, l.toByte, l.toShort, l.toChar, ()      , l.toFloat, l.toDouble)
      Seq(f.toInt, f.toByte, f.toShort, f.toChar, f.toLong, ()       , f.toDouble)
      Seq(d.toInt, d.toByte, d.toShort, d.toChar, d.toLong, d.toFloat, ()        )
      // format: on
    }
    assertEmpty(result)
  }

  test("redundant toString disabled") {
    val result = WartTestTraverser(RedundantConversions) {
      "x".toString
    }
    assertError(result)("redundant toString conversion")
  }
  test("no error if notStringType.toString") {
    val result = WartTestTraverser(RedundantConversions) {
      List(3).toString
      true.toString
      2.toString
    }
    assertEmpty(result)
  }

  test("redundant toList disabled") {
    val result = WartTestTraverser(RedundantConversions) {
      List(1).toList
    }
    assertError(result)("redundant toList conversion")
  }
  test("no error if notListType.toList") {
    val result = WartTestTraverser(RedundantConversions) {
      Seq(1).toList
      Vector("a").toList
      Array(false).toList
      Stream(1).toList
      Set(1).toList
    }
    assertEmpty(result)
  }

  test("redundant toVector disabled") {
    val result = WartTestTraverser(RedundantConversions) {
      Vector(1.5).toVector
    }
    assertError(result)("redundant toVector conversion")
  }
  test("no error if notVectorType.toVector") {
    val result = WartTestTraverser(RedundantConversions) {
      Seq(1).toVector
      List(1).toVector
      Array(1).toVector
      Stream(1).toVector
      Set(1).toVector
    }
    assertEmpty(result)
  }

  test("redundant toSet disabled") {
    val result = WartTestTraverser(RedundantConversions) {
      Set(None).toSet
    }
    assertError(result)("redundant toSet conversion")
  }
  test("no error if notSetType.toSet") {
    val result = WartTestTraverser(RedundantConversions) {
      Seq(1).toSet
      List(1).toSet
      Array(1).toSet
      Stream(1).toSet
      Vector(1).toSet
    }
    assertEmpty(result)
  }

  test("redundant toStream disabled") {
    val result = WartTestTraverser(RedundantConversions) {
      Stream(3).toStream
    }
    assertError(result)("redundant toStream conversion")
  }
  test("no error if notStreamType.toStream") {
    val result = WartTestTraverser(RedundantConversions) {
      Seq(1).toStream
      List(1).toStream
      Array(1).toStream
      Vector(1).toStream
      Set(1).toStream
    }
    assertEmpty(result)
  }

  test("redundant toSeq disabled") {
    val result = WartTestTraverser(RedundantConversions) {
      List(1).toSeq
      Vector(1).toSeq
      Stream(1).toSeq
    }
    assertErrors(result)("redundant toSeq conversion", 3)
  }
  test("no error if noSeqType.toSeq") {
    val result = WartTestTraverser(RedundantConversions) {
      Array(1).toSeq
      Set(1).toSeq
    }
    assertEmpty(result)
  }

  test("redundant toIndexedSeq disabled") {
    val result = WartTestTraverser(RedundantConversions) {
      Vector(1).toIndexedSeq
      collection.immutable.IndexedSeq("x").toIndexedSeq
    }
    assertErrors(result)("redundant toIndexedSeq conversion", 2)
  }
  test("no error if noIndexedSeqType.toIndexedSeq") {
    val result = WartTestTraverser(RedundantConversions) {
      Seq(1).toIndexedSeq
      List(1).toIndexedSeq
      Array(1).toIndexedSeq
      Stream(1).toIndexedSeq
      Set(1).toIndexedSeq
    }
    assertEmpty(result)
  }
}
