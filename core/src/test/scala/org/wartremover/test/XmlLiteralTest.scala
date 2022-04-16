package org.wartremover
package test

import org.wartremover.warts.Unsafe
import org.scalatest.funsuite.AnyFunSuite

class XmlLiteralTest extends AnyFunSuite with ResultAssertions {
  test("can use xml literals") {
    val result = WartTestTraverser(Unsafe) {
      val x = <foo />
      val x2 = <a>b</a>
      val x3 = <a>{List(2).sum}ccc</a>
      val x4 = <a>{List(2).sum}ccc{9}</a>
    }
    assertEmpty(result)
  }
  test("can use attributes in xml literals") {
    val result = WartTestTraverser(Unsafe) {
      <foo bar="baz" />
    }
    assertEmpty(result)
  }
  test("can use xmlns attrib in XML literals") {
    val result = WartTestTraverser(Unsafe) {
      <x xmlns="y"/> // this one has special meaning
    }
    assertEmpty(result)
  }
}
