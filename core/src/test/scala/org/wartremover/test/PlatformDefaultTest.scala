package org.wartremover
package test

import java.nio.charset.StandardCharsets
import java.util.Locale

import org.scalatest.funsuite.AnyFunSuite
import org.wartremover.warts.PlatformDefault

import scala.io.Codec

class PlatformDefaultTest extends AnyFunSuite with ResultAssertions {
  test("getByte()") {
    val result = WartTestTraverser(PlatformDefault) {
      "".getBytes()
    }
    assertError(result)(PlatformDefault.getByte)
  }
  test("getByte(charset)") {
    val result = WartTestTraverser(PlatformDefault) {
      "".getBytes("UTF-8")
      "".getBytes(StandardCharsets.UTF_8)
    }
    assertEmpty(result)
  }
  test("toUpperCase(), toLowerCase()") {
    val result = WartTestTraverser(PlatformDefault) {
      "".toUpperCase()
      "".toLowerCase()
    }
    assertErrors(result)(PlatformDefault.upperLowerCase, 2)
  }
  test("toUpperCase(locale), toLowerCase(locale)") {
    val result = WartTestTraverser(PlatformDefault) {
      "".toUpperCase(Locale.ENGLISH)
      "".toLowerCase(Locale.ROOT)
    }
    assertEmpty(result)
  }
  test("new String(bytes)") {
    val result = WartTestTraverser(PlatformDefault) {
      new String(Array.empty[Byte])
    }
    assertError(result)(PlatformDefault.newString)
  }
  test("new String(bytes, charset)") {
    val result = WartTestTraverser(PlatformDefault) {
      new String(Array.empty[Byte], "UTF-8")
      new String(Array.empty[Byte], StandardCharsets.UTF_8)
    }
    assertEmpty(result)
  }
  test("scala.io.Codec.fallbackSystemCodec") {
    val result = WartTestTraverser(PlatformDefault) {
      scala.io.Source.fromBytes(Array.empty[Byte])
    }
    assertError(result)(PlatformDefault.fallbackSystemCodec)
  }
  test("explicit scala.io.Codec") {
    val result = WartTestTraverser(PlatformDefault) {
      implicit val myCodec: Codec = Codec.UTF8
      scala.io.Source.fromBytes(Array.empty[Byte])
    }
    assertEmpty(result)
  }

  test("wart obeys SuppressWarnings") {
    val result = WartTestTraverser(PlatformDefault) {
      @SuppressWarnings(Array("org.wartremover.warts.PlatformDefault"))
      val foo = {
        new String(Array.empty[Byte])
        scala.io.Source.fromBytes(Array.empty[Byte])
        "".getBytes
        "".toUpperCase()
        "".toLowerCase()
      }
    }
    assertEmpty(result)
  }
}

