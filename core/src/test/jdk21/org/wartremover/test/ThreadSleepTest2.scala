package org.wartremover
package test

import org.wartremover.warts.ThreadSleep
import org.scalatest.funsuite.AnyFunSuite

class ThreadSleepTest2 extends AnyFunSuite with ResultAssertions {
  private def d: java.time.Duration = ???

  test("Thread.sleep is disabled") {
    val result = WartTestTraverser(ThreadSleep) {
      Thread.sleep(d)
    }
    assertError(result)("don't use Thread.sleep")
  }

}
