package org.wartremover
package test

import org.scalatest.funsuite.AnyFunSuite
import org.wartremover.warts.ThreadSleep

class ThreadSleepTest2 extends AnyFunSuite with ResultAssertions {
  private def d: java.time.Duration = ???

  test("Thread.sleep is disabled") {
    val result = WartTestTraverser(ThreadSleep) {
      Thread.sleep(d)
    }
    assertError(result)("don't use Thread.sleep")
  }

}
