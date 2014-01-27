package org.brianmckenna.wartremover
package test

import org.scalatest.FunSuite

import org.brianmckenna.wartremover.warts.Unsafe

class XmlLiteralTest extends FunSuite {
  test("can use xml literals") {
    val result = WartTestTraverser(Unsafe) {
      val x = <foo />
    }
    assert(result.errors == List.empty)
    assert(result.warnings == List.empty)
  }
  test("can use attributes in xml literals") {
    val result = WartTestTraverser(Unsafe) {
      <foo bar="baz" />
    }
    assert(result.errors == List.empty)
    assert(result.warnings == List.empty)
  }
  test("can use xmlns attrib in XML literals") {
    val result = WartTestTraverser(Unsafe) {
      <x xmlns="y"/> // this one has special meaning
    }
    assert(result.errors == List.empty)
    assert(result.warnings == List.empty)
  }
}
