package org.brianmckenna.wartremover
package test

import org.scalatest.FunSuite

import org.brianmckenna.wartremover.warts.Equal

class EqualTest extends FunSuite {
  test("can't compare non-conforming types (1)") {
    val result = WartTestTraverser(Equal) {
      List(1) == Set(1) // scala says false
    }
    expectResult(List("Non-conforming types List[Int] and scala.collection.immutable.Set[Int] cannot be compared"), "result.errors")(result.errors)
    expectResult(List.empty, "result.warnings")(result.warnings)
  }
  test("can't compare non-conforming types (2)") {
    val result = WartTestTraverser(Equal) {
      Vector(1,2,3) == List(1,2,3) // scala says true
    }
    expectResult(List("Non-conforming types scala.collection.immutable.Vector[Int] and List[Int] cannot be compared"), "result.errors")(result.errors)
    expectResult(List.empty, "result.warnings")(result.warnings)
  }
  test("can't compare non-conforming types (3)") {
    val result = WartTestTraverser(Equal) {
      Some(1) == None // scala warns and says false
    }
    expectResult(List("Non-conforming types Some[Int] and None.type cannot be compared"), "result.errors")(result.errors)
    expectResult(List.empty, "result.warnings")(result.warnings)
  }
  test("ok to compare conforming types (1)") {
    val result = WartTestTraverser(Equal) {
      Option(1) == None
    }
    expectResult(List.empty, "result.errors")(result.errors)
    expectResult(List.empty, "result.warnings")(result.warnings)
  }
  test("ok to compare conforming types (2)") {
    val result = WartTestTraverser(Equal) {
      Iterable(1) == List(1) // cringe, but this is ok
    }
    expectResult(List.empty, "result.errors")(result.errors)
    expectResult(List.empty, "result.warnings")(result.warnings)
  }
  test("implicit conversions don't screw us!") {
    val result = WartTestTraverser(Equal) {
      Iterable('a') == "abc" // nope! \o/
    }
    expectResult(List("Non-conforming types Iterable[Char] and String(\"abc\") cannot be compared"), "result.errors")(result.errors)
    expectResult(List.empty, "result.warnings")(result.warnings)
  }
  test("can't do anything about numeric widening, sorry") {
    val result = WartTestTraverser(Equal) {
      1 == 1.0 // sadly you can still do this
    }
    expectResult(List.empty, "result.errors")(result.errors)
    expectResult(List.empty, "result.warnings")(result.warnings)
  }
  test("works with complex exprs") {
    val result = WartTestTraverser(Equal) {
      (1 max 2) == List(3, 4).map(_ + 1)
    }
    expectResult(List("Non-conforming types Int and List[Int] cannot be compared"), "result.errors")(result.errors)
    expectResult(List.empty, "result.warnings")(result.warnings)
  }
  test("works when types differ only in type args") {
    val result = WartTestTraverser(Equal) {
      List(1,2) == List(true)
    }
    expectResult(List("Non-conforming types List[Int] and List[Boolean] cannot be compared"), "result.errors")(result.errors)
    expectResult(List.empty, "result.warnings")(result.warnings)
  }
}
