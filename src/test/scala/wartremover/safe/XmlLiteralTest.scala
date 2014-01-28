package org.brianmckenna.wartremover
package test

import org.scalatest.FunSuite

import org.brianmckenna.wartremover.warts.Unsafe

class XmlLiteralTest extends FunSuite {
  test("can use xml literals") {
    val result = WartTestTraverser(Unsafe) {
      val x = <foo />
    }
    expectResult(List.empty, "result.errors")(result.errors)
    expectResult(List.empty, "result.warnings")(result.warnings)
  }
  test("can use attributes in xml literals") {
    val result = WartTestTraverser(Unsafe) {
      <foo bar="baz" />
    }
    expectResult(List.empty, "result.errors")(result.errors)
    expectResult(List.empty, "result.warnings")(result.warnings)
  }
  test("can use xmlns attrib in XML literals") {
    val result = WartTestTraverser(Unsafe) {
      <x xmlns="y"/> // this one has special meaning
    }
    expectResult(List.empty, "result.errors")(result.errors)
    expectResult(List.empty, "result.warnings")(result.warnings)
  }
}
