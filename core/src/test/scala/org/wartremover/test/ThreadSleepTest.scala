package org.wartremover
package test

import org.wartremover.warts.ThreadSleep
import org.scalatest.funsuite.AnyFunSuite

class ThreadSleepTest extends AnyFunSuite with ResultAssertions {
  test("Thread.sleep is disabled") {
    val byte: Byte = 1
    val short: Short = 2
    val int: Int = 3
    val long: Long = 4

    Seq(
      WartTestTraverser(ThreadSleep) {
        Thread.sleep(1, 2)
      },
      WartTestTraverser(ThreadSleep) {
        Thread.sleep(byte)
      },
      WartTestTraverser(ThreadSleep) {
        Thread.sleep(short)
      },
      WartTestTraverser(ThreadSleep) {
        Thread.sleep(int)
      },
      WartTestTraverser(ThreadSleep) {
        Thread.sleep(long)
      },
      WartTestTraverser(ThreadSleep) {
        Thread.sleep(5)
      }
    ).foreach(result => assertError(result)("don't use Thread.sleep"))
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
