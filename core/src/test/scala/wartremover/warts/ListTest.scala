package org.wartremover
package test

import org.scalatest.FunSuite

import org.wartremover.warts.ListOps

class ListTest extends FunSuite with ResultAssertions {
  test("can't use List#head on List") {
    val result = WartTestTraverser(ListOps) {
      println(List(1).head)
    }
    assertError(result)("[org.wartremover.warts.ListOps] List#head is disabled - use List#headOption instead")
  }

  test("can't use List#tail on List") {
    val result = WartTestTraverser(ListOps) {
      println(List().tail)
    }
    assertError(result)("[org.wartremover.warts.ListOps] List#tail is disabled - use List#drop(1) instead")
  }

  test("can't use List#init on List") {
    val result = WartTestTraverser(ListOps) {
      println(List().init)
    }
    assertError(result)("[org.wartremover.warts.ListOps] List#init is disabled - use List#dropRight(1) instead")
  }

  test("can't use List#last on List") {
    val result = WartTestTraverser(ListOps) {
      println(List().last)
    }
    assertError(result)("[org.wartremover.warts.ListOps] List#last is disabled - use List#lastOption instead")
  }

  test("can't use List#reduce on List") {
    val result = WartTestTraverser(ListOps) {
      println(List.empty[Int].reduce(_ + _))
    }
    assertError(result)("[org.wartremover.warts.ListOps] List#reduce is disabled - use List#reduceOption or List#fold instead")
  }

  test("can't use List#reduceLeft on List") {
    val result = WartTestTraverser(ListOps) {
      println(List.empty[Int].reduceLeft(_ + _))
    }
    assertError(result)("[org.wartremover.warts.ListOps] List#reduceLeft is disabled - use List#reduceLeftOption or List#foldLeft instead")
  }

  test("can't use List#reduceRight on List") {
    val result = WartTestTraverser(ListOps) {
      println(List.empty[Int].reduceRight(_ + _))
    }
    assertError(result)("[org.wartremover.warts.ListOps] List#reduceRight is disabled - use List#reduceRightOption or List#foldRight instead")
  }

  test("ListOps wart obeys SuppressWarnings") {
    val result = WartTestTraverser(ListOps) {
      @SuppressWarnings(Array("org.wartremover.warts.ListOps"))
      val foo = {
        println(List(1).head)
        println(List().tail)
        println(List().init)
        println(List().last)
        println(List.empty[Int].reduce(_ + _))
        println(List.empty[Int].reduceLeft(_ + _))
        println(List.empty[Int].reduceRight(_ + _))
      }
    }
    assertEmpty(result)
  }

}
