package org.wartremover
package test

import org.wartremover.warts.ThreadSleep
import org.scalatest.funsuite.AnyFunSuite

class ThreadSleepTest extends AnyFunSuite with ResultAssertions {
  test("Thread.sleep is disabled") {
    val result = WartTestTraverser(ThreadSleep) {
      val byte: Byte = 1
      val short: Short = 2
      val int: Int = 3
      val long: Long = 4
      Thread.sleep(byte)
      Thread.sleep(short)
      Thread.sleep(int)
      Thread.sleep(long)
      Thread.sleep(5)
    }
    assertErrors(result)(
      "don't use Thread.sleep",
      5
    )
  }

  test("no warn if another Thread class") {
    object Thread {
      def sleep(millis: Long): Unit = ()
    }
    val result = WartTestTraverser(ThreadSleep) {
      Thread.sleep(1)
    }
    assertEmpty(result)
  }

  test("Thread.sleep wart obeys SuppressWarnings") {
    val result = WartTestTraverser(ThreadSleep) {
      @SuppressWarnings(Array("org.wartremover.warts.ThreadSleep"))
      val foo = Thread.sleep(1)
    }
    assertEmpty(result)
  }
}
