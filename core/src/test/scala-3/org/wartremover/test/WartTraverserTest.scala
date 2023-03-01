package org.wartremover
package test

import org.wartremover.warts.AutoUnboxing
import org.wartremover.warts.OrTypeLeastUpperBound
import org.wartremover.warts.Return
import org.scalatest.funsuite.AnyFunSuite

class WartTraverserTest extends AnyFunSuite {
  test("simpleName") {
    assert(AutoUnboxing.simpleName == "AutoUnboxing")
    assert(Return.simpleName == "Return")
    assert(OrTypeLeastUpperBound.All.simpleName == "OrTypeLeastUpperBound.All")
    assert(OrTypeLeastUpperBound.Any.simpleName == "OrTypeLeastUpperBound.Any")
    assert(OrTypeLeastUpperBound.AnyRef.simpleName == "OrTypeLeastUpperBound.AnyRef")
    assert(OrTypeLeastUpperBound.Matchable.simpleName == "OrTypeLeastUpperBound.Matchable")
    assert(OrTypeLeastUpperBound.Product.simpleName == "OrTypeLeastUpperBound.Product")
    assert(OrTypeLeastUpperBound.Serializable.simpleName == "OrTypeLeastUpperBound.Serializable")
  }
}
