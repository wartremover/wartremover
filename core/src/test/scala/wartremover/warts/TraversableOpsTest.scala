package org.wartremover
package test

import org.scalatest.FunSuite
import org.wartremover.warts.TraversableOps

class TraversableOpsTest extends FunSuite with ResultAssertions {
  Seq[(String, Traversable[Any])]("List" -> List(1), "Seq" -> Seq(1), "Map" -> Map(1 -> 1)).foreach { case (name, x) =>
    test(s"can't use $name#head") {
      val result = WartTestTraverser(TraversableOps) {
        println(x.head)
      }
      assertError(result)("[org.wartremover.warts.TraversableOps] head is disabled - use headOption instead")
    }

    test(s"can't use $name#tail") {
      val result = WartTestTraverser(TraversableOps) {
        println(x.tail)
      }
      assertError(result)("[org.wartremover.warts.TraversableOps] tail is disabled - use drop(1) instead")
    }

    test(s"can't use $name#init") {
      val result = WartTestTraverser(TraversableOps) {
        println(x.init)
      }
      assertError(result)("[org.wartremover.warts.TraversableOps] init is disabled - use dropRight(1) instead")
    }

    test(s"can't use $name#last") {
      val result = WartTestTraverser(TraversableOps) {
        println(x.last)
      }
      assertError(result)("[org.wartremover.warts.TraversableOps] last is disabled - use lastOption instead")
    }

    test(s"can't use $name#reduce") {
      val result = WartTestTraverser(TraversableOps) {
        println(x.reduce(_.hashCode + _.hashCode))
      }
      assertError(result)("[org.wartremover.warts.TraversableOps] reduce is disabled - use reduceOption or fold instead")
    }

    test(s"can't use $name#reduceLeft") {
      val result = WartTestTraverser(TraversableOps) {
        println(x.reduceLeft(_.hashCode + _.hashCode))
      }
      assertError(result)("[org.wartremover.warts.TraversableOps] reduceLeft is disabled - use reduceLeftOption or foldLeft instead")
    }

    test(s"can't use $name#reduceRight") {
      val result = WartTestTraverser(TraversableOps) {
        println(x.reduceRight(_.hashCode + _.hashCode))
      }
      assertError(result)("[org.wartremover.warts.TraversableOps] reduceRight is disabled - use reduceRightOption or foldRight instead")
    }
  }

  test("TraversableOps wart obeys SuppressWarnings") {
    val result = WartTestTraverser(TraversableOps) {
      @SuppressWarnings(Array("org.wartremover.warts.TraversableOps"))
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
