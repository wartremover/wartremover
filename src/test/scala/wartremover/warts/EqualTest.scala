package org.brianmckenna.wartremover
package test

import org.scalatest.FunSuite

import org.brianmckenna.wartremover.warts.Equal

class EqualTest extends FunSuite {
  
  def twice[A](a: A): List[A] = List(a, a)
  
  test("can't compare non-conforming types (1)") {
    val result = WartTestTraverser(Equal) {
      List(1) == Set(1) // scala says false
      List(1) equals Set(1) 
    }
    expectResult(twice("Non-conforming types List[Int] and scala.collection.immutable.Set[Int] cannot be compared"), "result.errors")(result.errors)
    expectResult(List.empty, "result.warnings")(result.warnings)
  }
  test("can't compare non-conforming types (2)") {
    val result = WartTestTraverser(Equal) {
      Vector(1,2,3) == List(1,2,3) // scala says true
      Vector(1,2,3) equals List(1,2,3)
    }
    expectResult(twice("Non-conforming types scala.collection.immutable.Vector[Int] and List[Int] cannot be compared"), "result.errors")(result.errors)
    expectResult(List.empty, "result.warnings")(result.warnings)
  }
  test("can't compare non-conforming types (3)") {
    val result = WartTestTraverser(Equal) {
      Some(1) == None // scala warns and says false
      Some(1) equals None 
    }
    expectResult(twice("Non-conforming types Some[Int] and None.type cannot be compared"), "result.errors")(result.errors)
    expectResult(List.empty, "result.warnings")(result.warnings)
  }
  test("ok to compare conforming types (1)") {
    val result = WartTestTraverser(Equal) {
      Option(1) == None
      Option(1) equals None
    }
    expectResult(List.empty, "result.errors")(result.errors)
    expectResult(List.empty, "result.warnings")(result.warnings)
  }
  test("ok to compare conforming types (2)") {
    val result = WartTestTraverser(Equal) {
      Iterable(1) == List(1) 
      Iterable(1) equals List(1) 
    }
    expectResult(List.empty, "result.errors")(result.errors)
    expectResult(List.empty, "result.warnings")(result.warnings)
  }
  test("implicit conversions don't screw us!") {
    val result = WartTestTraverser(Equal) {
      Iterable('a') == "abc"     
      Iterable('a') equals "abc" 
    }
    expectResult(twice("Non-conforming types Iterable[Char] and String(\"abc\") cannot be compared"), "result.errors")(result.errors)
    expectResult(List.empty, "result.warnings")(result.warnings)
  }
  test("numeric widening sometimes, sorry") {
    val result = WartTestTraverser(Equal) {
      1 == 1.0     // can't do anything about widening here
      1 equals 1.0 // but we can catch it here
    }
    expectResult(List("Non-conforming types Int(1) and Double(1.0) cannot be compared"), "result.errors")(result.errors)
    expectResult(List.empty, "result.warnings")(result.warnings)
  }
  test("works with complex exprs") {
    val result = WartTestTraverser(Equal) {
      (1 max 2) == List(3, 4).map(_ + 1)
      (1 max 2) equals List(3, 4).map(_ + 1) // Here the RHS is inferred as `Any`
    }
    expectResult(List("Non-conforming types Int and List[Int] cannot be compared"), "result.errors")(result.errors)
    expectResult(List.empty, "result.warnings")(result.warnings)
  }
  test("works when types differ only in type args") {
    val result = WartTestTraverser(Equal) {
      List(1,2) == List(true)
      List(1,2) equals List(true)
    }
    expectResult(twice("Non-conforming types List[Int] and List[Boolean] cannot be compared"), "result.errors")(result.errors)
    expectResult(List.empty, "result.warnings")(result.warnings)
  }

}
