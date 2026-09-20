package org.wartremover
package test

import org.wartremover.warts.SynchronizedValueBasedClasses
import org.scalatest.funsuite.AnyFunSuite

class SynchronizedValueBasedClassesTest extends AnyFunSuite with ResultAssertions {
  test("java.time") {
    Seq(
      WartTestTraverser(SynchronizedValueBasedClasses) {
        def f(x: java.time.Instant) = x.synchronized(x)
      },
      WartTestTraverser(SynchronizedValueBasedClasses) {
        def f(x: java.time.LocalDate) = x.synchronized(x)
      },
      WartTestTraverser(SynchronizedValueBasedClasses) {
        def f(x: java.time.LocalTime) = x.synchronized(x)
      },
      WartTestTraverser(SynchronizedValueBasedClasses) {
        def f(x: java.time.LocalDateTime) = x.synchronized(x)
      },
      WartTestTraverser(SynchronizedValueBasedClasses) {
        def f(x: java.time.ZonedDateTime) = x.synchronized(x)
      },
      WartTestTraverser(SynchronizedValueBasedClasses) {
        def f(x: java.time.ZoneId) = x.synchronized(x)
      },
      WartTestTraverser(SynchronizedValueBasedClasses) {
        def f(x: java.time.OffsetTime) = x.synchronized(x)
      },
      WartTestTraverser(SynchronizedValueBasedClasses) {
        def f(x: java.time.OffsetDateTime) = x.synchronized(x)
      },
      WartTestTraverser(SynchronizedValueBasedClasses) {
        def f(x: java.time.ZoneOffset) = x.synchronized(x)
      },
      WartTestTraverser(SynchronizedValueBasedClasses) {
        def f(x: java.time.Duration) = x.synchronized(x)
      },
      WartTestTraverser(SynchronizedValueBasedClasses) {
        def f(x: java.time.Period) = x.synchronized(x)
      },
      WartTestTraverser(SynchronizedValueBasedClasses) {
        def f(x: java.time.Year) = x.synchronized(x)
      },
      WartTestTraverser(SynchronizedValueBasedClasses) {
        def f(x: java.time.YearMonth) = x.synchronized(x)
      },
      WartTestTraverser(SynchronizedValueBasedClasses) {
        def f(x: java.time.MonthDay) = x.synchronized(x)
      },
      WartTestTraverser(SynchronizedValueBasedClasses) {
        def f(x: java.time.chrono.MinguoDate) = x.synchronized(x)
      },
      WartTestTraverser(SynchronizedValueBasedClasses) {
        def f(x: java.time.chrono.HijrahDate) = x.synchronized(x)
      },
      WartTestTraverser(SynchronizedValueBasedClasses) {
        def f(x: java.time.chrono.JapaneseDate) = x.synchronized(x)
      },
      WartTestTraverser(SynchronizedValueBasedClasses) {
        def f(x: java.time.chrono.ThaiBuddhistDate) = x.synchronized(x)
      },
    ).foreach { result =>
      assertError(result)("attempt to synchronize on an instance of a value-based class")
    }
  }

  test("Optional") {
    Seq(
      WartTestTraverser(SynchronizedValueBasedClasses) {
        def f(x: java.util.Optional[String]) = x.synchronized(x)
      },
      WartTestTraverser(SynchronizedValueBasedClasses) {
        def f(x: java.util.OptionalInt) = x.synchronized(x)
      },
      WartTestTraverser(SynchronizedValueBasedClasses) {
        def f(x: java.util.OptionalDouble) = x.synchronized(x)
      },
      WartTestTraverser(SynchronizedValueBasedClasses) {
        def f(x: java.util.OptionalLong) = x.synchronized(x)
      },
    ).foreach { result =>
      assertError(result)("attempt to synchronize on an instance of a value-based class")
    }
  }

  test("other") {
    Seq(
      WartTestTraverser(SynchronizedValueBasedClasses) {
        def f(x: java.lang.Runtime.Version) = x.synchronized(x)
      },
    ).foreach { result =>
      assertError(result)("attempt to synchronize on an instance of a value-based class")
    }
  }

  test("no value-based classes allowed") {
    val result = WartTestTraverser(SynchronizedValueBasedClasses) {
      def f1(x: java.util.Date) = x.synchronized(x)
      def f2(x: String) = x.synchronized(x)
      def f3[A](x: Option[A]) = x.synchronized(x)
      def f4[A](x: java.util.List[A]) = x.synchronized(x)
    }
    assertEmpty(result)
  }

  test("SynchronizedValueBasedClasses wart obeys SuppressWarnings") {
    val result = WartTestTraverser(SynchronizedValueBasedClasses) {
      @SuppressWarnings(Array("org.wartremover.warts.SynchronizedValueBasedClasses"))
      class A {
        def f1(x: java.time.Instant) = x.synchronized(x)
        def f2(x: java.util.OptionalInt) = x.synchronized(x)
      }
    }
    assertEmpty(result)
  }
}
