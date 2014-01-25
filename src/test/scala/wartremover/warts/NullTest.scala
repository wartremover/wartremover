package org.brianmckenna.wartremover
package test

import org.scalatest.FunSuite

import org.brianmckenna.wartremover.warts.Null

class NullTest extends FunSuite {
  test("can't use `null`") {
    val result = WartTestTraverser(Null) {
      println(null)
    }
    assert(result.errors == List("null is disabled"))
    assert(result.warnings == List.empty)
  }
  test("can't use null in patterns") {
    val result = WartTestTraverser(Null) {
      val (a, b) = (1, null)
      println(a)
    }
    assert(result.errors == List("null is disabled"))
    assert(result.warnings == List.empty)
  }
  test("can't use null inside of Map#partition") {
    val result = WartTestTraverser(Null) {
      Map(1 -> "one", 2 -> "two").partition { case (k, v) => null.asInstanceOf[Boolean] }
    }
    assert(result.errors == List("null is disabled"))
    assert(result.warnings == List.empty)
  }
  test("can use case classes") {
    val result = WartTestTraverser(Null) {
      case class A(b: Int)
    }
    assert(result.errors == List.empty)
    assert(result.warnings == List.empty)
  }
  test("can use xml literals") {
    val result = WartTestTraverser(Null) {
      val x = <foo />
    }
    assert(result.errors == List.empty)
    assert(result.warnings == List.empty)
  }
  test("can use xmlns attrib in XML literals") {
    val result = WartTestTraverser(Null) {
      <x xmlns="y"/>
    }
    assert(result.errors == List.empty)
    assert(result.warnings == List.empty)
  }
}
