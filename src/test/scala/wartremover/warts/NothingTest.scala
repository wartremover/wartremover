package org.brianmckenna.wartremover
package test

import org.scalatest.FunSuite

import org.brianmckenna.wartremover.warts.Nothing

class NothingTest extends FunSuite {
  test("Nothing can't be inferred") {
    val result = WartTestTraverser(Nothing) {
      val x = ???
      x
    }
    assert(result.errors == List("Inferred type containing Nothing from assignment"))
    assert(result.warnings == List.empty)
  }
}
