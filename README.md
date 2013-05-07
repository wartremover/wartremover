# Wart Remover

This project aims to clean up some of Scala's warts by shadowing
things or making implicits ambiguous.

## Usage

Wart Remover contains two parts. A `WartRemover` trait and a `safe`
macro.

The `WartRemover` trait is meant to be extended by a `package object`
for your project's package (via the special `package.scala`). For
example:

    import org.brianmckenna.wartremover.WartRemover

    package object com.precog extends WartRemover

The `safe` macro takes any expression and performs extra checks on the
AST. For example, you can use it on a block:

    def main(args: Array[String]) = safe {
      def x[A](a: A) = a
      // Won't compile: Statements must return Unit
      // x(100)
      x(())
      println("Hello world")
    }

For extra safety, turn on all of Scala's warnings and make them
errors. You can do this in your project's `build.sbt`:

    scalaVersion := "2.10.0"

    // -Ywarn-adapted-args has a bug (see SI-6923). Need to use
    // -Yno-adapted-args for it to fully work.
    scalacOptions ++= Seq("-Yno-adapted-args", "-Ywarn-all", "-Xfatal-warnings")

## Package Object Warts

### Warnings for "advanced" features

SIP-18 introduced warnings for the following features:

* Existentials
* Higher-kinds
* Implicit conversions

They're "disabled" mostly because they're hard to understand. Let's
disable those warnings. These features are sane and we know what we're
doing.

### Postfix operators

Scala allowed postfix operators, like so:

    List(1, 2, 3) length

Notice the lack of a `.` before length? That means the operator is
"postfix". The problem is that postfix operators mess up semicolon
inference. Let's make postfix operators impossible to accidentally
use.

### Manifests

A Scala Manifest is a way of getting full type information at
runtime. Requiring runtime type information almost certainly means
dangerous stuff is happening:

    def makeMeA[T](implicit m: Manifest[T]) = m.erasure.newInstance
    makeMeA[List[Int]] // throws a nice Exception

The runtime of your program shouldn't rely on type information to
work. Get some type safety back by making this not compile.

### any2stringadd

What do you expect the following to do?

    println({} + "test")

Print `()test`, of course... Scala has an implicit which will convert
anything to a String if the right side of `+` is a String. Get some
type safety back by making this not compile.

## Macro Warts

### Non-unit statements

Scala allows statements to return any type. Statements should only
return `Unit` (this ensures that they're really intended to be
statements). The macro enforces that:

    safe {
      def x[A](a: A) = a
      // x(100)
      x(())

      100
    }

This is like a better `-Xfatal-warnings -Ywarn-value-discard`.

### null

null is a special value that inhabits all reference types. It breaks
type safety.

    safe {
      // Won't compile: null is disabled
      val s: String = null
    }

### var

Mutation breaks equational reasoning.

    safe {
      // Won't compile: var is disabled
      var x = 100
    }

### Nothing inference

Nothing is a special bottom type; it is a subtype of every other
type. The Scala compiler loves to infer Nothing as a generic type but
that almost always incorrect. Explicit type arguments should be used
instead.

    safe {
      // Won't compile: Inferred type containing Nothing from assignment
      val nothing = ???
      val nothingList = List.empty
    }
