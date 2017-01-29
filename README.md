# WartRemover

[![Build Status](https://travis-ci.org/wartremover/wartremover.png?branch=master)](https://travis-ci.org/puffnfresh/wartremover)

WartRemover is a flexible Scala code linting tool.

## Usage

Add the following to your `project/plugins.sbt`:

```scala
addSbtPlugin("org.wartremover" % "sbt-wartremover" % "1.3.0")
```

**NOTE**: `sbt-wartremover` requires sbt version 0.13.5+.

Now, you can proceed to configure the linter in your `build.sbt`. By default, all errors and warnings are turned off. To turn on all checks that are currently considered stable, use:

```scala
wartremoverErrors ++= Warts.unsafe
```

To turn on *all* available errors (some have false positives), use:

```scala
wartremoverErrors ++= Warts.all
```

Similarly, to just issue warnings instead of errors for all built-in warts, you can use:

```scala
wartremoverWarnings ++= Warts.all    // or Warts.unsafe
```

You can also use scopes, e.g. to turn on all warts only for compilation (and not for the tests nor the `sbt console`), use:

```scala
wartremoverErrors in (Compile, compile) ++= Warts.all
```

To choose warts more selectively, use any of the following:

```scala
wartremoverErrors ++= Warts.allBut(Wart.Any, Wart.Nothing, Wart.Serializable)

wartremoverWarnings += Wart.Nothing

wartremoverWarnings ++= Seq(Wart.Any, Wart.Serializable)
```

To exclude a file from all checks, use:

```scala
wartremoverExcluded += baseDirectory.value / "src" / "main" / "scala" / "SomeFile.scala"
```

To exclude a specific piece of code from one or more checks, use the `SuppressWarnings` annotation:
```scala
@SuppressWarnings(Array("org.wartremover.warts.Var", "org.wartremover.warts.Null"))
var foo = null
```

Finally, if you want to add your custom `WartTraverser`, provide its classpath first:

```scala
wartremoverClasspaths += "some-url"

wartremoverErrors += Wart.custom("org.your.custom.WartTraverser")
```

See also [other ways of using WartRemover](/OTHER-WAYS.md) for information on how to use it as a command-line tool, in Maven builds, and as a macro or a compiler plugin, while providing all the `scalac` options manually.

* Note - the WartRemover SBT plugin sets scalac options - make sure you're not overwriting those by having a `scalacOptions := ...` setting in your SBT settings. Use `scalacOptions ++= ...` instead.


## Warts

Here is a list of built-in warts under the
`org.wartremover.warts` package.

### Any

`Any` is the top type; it is the supertype of every other type. The
Scala compiler loves to infer `Any` as a generic type, but that is
almost always incorrect. Explicit type arguments should be used
instead.

```scala
// Won't compile: Inferred type containing Any
val any = List(1, true, "three")
```

### Any2StringAdd

Deprecated, use **StringPlusAny**.

### AsInstanceOf

`asInstanceOf` is unsafe in isolation and violates parametricity when guarded by `isInstanceOf`. Refactor so that the desired type is proven statically.

```scala
// Won't compile: asInstanceOf is disabled
x.asInstanceOf[String]
```

### DefaultArguments

Scala allows methods to have default arguments, which make it hard to use methods as functions.

```scala
// Won't compile: Function has default arguments
def x(y: Int = 0)
```

### EitherProjectionPartial

`scala.util.Either.LeftProjection` and `scala.util.Either.RightProjection`
have a `get` method which will throw if the value doesn't match the
projection.  The program should be refactored to use `scala.util.Either.LeftProjection#toOption`
and `scala.util.Either.RightProjection#toOption` to explicitly handle both
the `Some` and `None` cases.

### Enumeration

Scala's `Enumeration` can cause performance problems due to its reliance on reflection. Additionally, the lack of exhaustive match checks and partial methods can lead to runtime errors. Instead of `Enumeration`, a sealed abstract class extended by case objects should be used instead.

### Equals

Scala's `Any` type provides an `==` method which is not type-safe. Using this method allows obviously incorrect code like `5 == "5"` to compile. A better version which forbids equality checks across types (which always fail) is easily defined:
```scala
@SuppressWarnings(Array("org.wartremover.warts.Equals"))
implicit final class AnyOps[A](self: A) {
   def ===(other: A): Boolean = self == other
}
```

### ExplicitImplicitTypes

Scala has trouble correctly resolving implicits when some of them lack explicit result types. To avoid this, all implicits should have explicit type ascriptions.

### FinalCaseClass

Scala's case classes provide a useful implementation of logicless data types. Extending a case class can break this functionality in surprising ways. This can be avoided by always making them final or sealed.

```scala
// Won't compile: case classes must be final
case class Foo()
```

### FinalVal

Value of a `final val` is inlined and can cause inconsistency during incremental compilation (see [sbt/sbt/issues/1543 ](https://github.com/sbt/sbt/issues/1543)).

```scala
file 1:
object c {
  // Won't compile: final val is disabled
  final val v = 1
}

file 2:
println(c.v)
```

### ImplicitConversion

Implicit conversions weaken type safety and always can be replaced by explicit conversions.

```scala
// Won't compile: implicit conversion is disabled
implicit def int2Array(i: Int) = Array.fill(i)("madness")
```

### ImplicitParameter

Implicit parameters aas configuration often lead to confusing interfaces and can result in surprising inconsistencies.

```scala
// Won't compile: Implicit parameters are disabled
def f()(implicit s: String) = ()

// Still compiles
def f[A: Ordering](a: A, other: A) = ...
def f(a: A, other: A)(implicit ordering: Ordering[A]) = ...
```

### IsInstanceOf

`isInstanceOf` violates parametricity. Refactor so that the type is established statically.

```scala
// Won't compile: isInstanceOf is disabled
x.isInstanceOf[String]
```

### JavaConversions

The standard library provides implicits conversions to and from Java types in `scala.collection.JavaConversions`. This can make code difficult to understand and read about. The explicit conversions provided by `scala.collection.JavaConverters` should be used instead.

```scala
// Won't compile: scala.collection.JavaConversions is disabled
import scala.collection.JavaConversions._
val scalaMap: Map[String, String] = Map()
val javaMap: java.util.Map[String, String] = scalaMap
```

### LeakingSealed

Descendants of a sealed type must be final or sealed. Otherwise this type can be extended in another file through its descendant.

```scala
file 1:
// Won't compile: Descendants of a sealed type must be final or sealed
sealed trait t
class c extends t

file 2:
class d extends c
```

### ListOps

Deprecated, use **TraversableOps**.

### MutableDataStructures

The standard library provides mutable collections. Mutation breaks equational reasoning.

```scala
// Won't compile: scala.collection.mutable package is disabled
import scala.collection.mutable.ListBuffer
val mutList = ListBuffer()
```

### NoNeedForMonad

Deprecated, this has been moved to [wartremover-contrib](https://github.com/wartremover/wartremover-contrib).

### NonUnitStatements

Scala allows statements to return any type. Statements should only
return `Unit` (this ensures that they're really intended to be
statements).

```scala
// Won't compile: Statements must return Unit
10
false
```

### Nothing

`Nothing` is a special bottom type; it is a subtype of every other
type. The Scala compiler loves to infer `Nothing` as a generic type but
that is almost always incorrect. Explicit type arguments should be
used instead.

```scala
// Won't compile: Inferred type containing Nothing
val nothing = ???
val nothingList = List.empty
```

### Null

`null` is a special value that inhabits all reference types. It breaks
type safety.

```scala
// Won't compile: null is disabled
val s: String = null
var s2: String = _
```

### Option2Iterable

Scala inserts an implicit conversion from `Option` to `Iterable`. This can hide bugs and creates surprising situations like `Some(1) zip Some(2)` returning an `Iterable[(Int, Int)]`.

### OptionPartial

`scala.Option` has a `get` method which will throw if the value is
`None`. The program should be refactored to use `scala.Option#fold` to
explicitly handle both the `Some` and `None` cases.

### Overloading

Method overloading may lead to confusion and usually can be avoided.

```scala
// Won't compile: Overloading is disabled
class c {
  def equals(x: Int) = {}
}
```

### Product

`Product` is a type common to many structures; it is the supertype of
case classes and tuples. The Scala compiler loves to infer `Product` as
a generic type, but that is almost always incorrect. Explicit type
arguments should be used instead.

```scala
// Won't compile: Inferred type containing Product
val any = List((1, 2, 3), (1, 2))
```

### PublicInference

Type inference of public members can expose extra type information, that can break encapsulation.

```scala
class c {
  // Won't compile: Public member must have an explicit type ascription
  def f() = new c with t
}
```

### Return

`return` breaks referential transparency. Refactor to terminate computations in a safe way.

```scala
// Won't compile: return is disabled
def foo(n:Int): Int = return n + 1
def foo(ns: List[Int]): Any = ns.map(n => return n + 1)
```

### Serializable

`Serializable` is a type common to many structures. The Scala compiler
loves to infer `Serializable` as a generic type, but that is almost
always incorrect. Explicit type arguments should be used instead.

```scala
// Won't compile: Inferred type containing Serializable
val any = List((1, 2, 3), (1, 2))
```

### StringPlusAny

Scala's `String` interface provides a `+` method that converts the operand to a `String` via its `toString` method. As mentioned in the documentation for the `ToString` wart, this method is unreliable and brittle.

```scala
// Won't compile: Implicit conversion to string is disabled
"foo" + {}
{} + "bar"
```

### Throw

`throw` implies partiality. Encode exceptions/errors as return
values instead using `Either`. 

### ToString

Scala creates a `toString` method automatically for all classes. Since `toString` is based on the class name, any rename can potentially introduce bugs. This is especially pernicious for case objects. `toString` should be explicitly overridden wherever used.
```scala
case object Foo { override val toString = "Foo" }
```

### TraversableOps

`scala.collection.Traversable` has:

* `head`,
* `tail`,
* `init`,
* `last`,
* `reduce`,
* `reduceLeft` and
* `reduceRight` methods,

all of which will throw if the collection is empty. The program should be refactored to use:

* `headOption`,
* `drop(1)`,
* `dropRight(1)`,
* `lastOption`,
* `reduceOption` or `fold`,
* `reduceLeftOption` or `foldLeft` and
* `reduceRightOption` or `foldRight` respectively,

to explicitly handle empty collections.

### TryPartial

`scala.util.Try` has a `get` method which will throw if the value is a
`Failure`. The program should be refactored to use `scala.util.Try#map` and `scala.util.Try#getOrElse` to
explicitly handle both the `Success` and `Failure` cases.

### Unsafe

Checks for the following warts:

* Any
* Any2StringAdd
* AsInstanceOf
* EitherProjectionPartial
* IsInstanceOf
* ListOps
* NonUnitStatements
* Null
* OptionPartial
* Product
* Return
* Serializable
* Throw
* TryPartial
* Var

### Var

Mutation breaks equational reasoning.

```scala
// Won't compile: var is disabled
var x = 100
```

### While

`while` loop usually indicates low-level code. If performance is not an issue, it can be replaced.

```scala
// Won't compile: while is disabled
while(i < 10) {
  i += 1
  ...
}
```

## Writing Wart Rules

A wart rule has to be an object that extends `WartTraverser`. The
object only needs an `apply` method which takes a `WartUniverse` and
returns a `WartUniverse#universe#Traverser`.

The `WartUniverse` has `error` and `warning` methods, which both take
`(WartUniverse#universe#Position, String)`. They are side-effecting
methods for adding errors and warnings.

Most traversers will want a `super.traverse` call to be able to
recursively continue.

```scala
import org.wartremover.{WartTraverser, WartUniverse}

object Unimplemented extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    import scala.reflect.NameTransformer

    val notImplementedName: TermName = NameTransformer.encode("???")
    val notImplemented: Symbol = typeOf[Predef.type].member(notImplementedName)
    require(notImplemented != NoSymbol)
    new Traverser {
      override def traverse(tree: Tree) {
        tree match {
          case rt: RefTree if rt.symbol == notImplemented =>
            u.error(tree.pos, "There was something left unimplemented")
          case _ =>
        }
        super.traverse(tree)
      }
    }
  }
}
```

## Reporting Issues

It's very useful to get the tree expanded by the Scala compiler,
rather than the original source. Adding the `-Xprint:typer` flag to
the Scala compiler will show code like the following:

```scala
// println("Hello world")
package $line4 {
  object $read extends scala.AnyRef {
    def <init>(): $line4.$read.type = {
      $read.super.<init>();
      ()
    };
    object $iw extends scala.AnyRef {
      def <init>(): type = {
        $iw.super.<init>();
        ()
      };
      object $iw extends scala.AnyRef {
        def <init>(): type = {
          $iw.super.<init>();
          ()
        };
        private[this] val res1: Unit = scala.this.Predef.println("Hello world");
        <stable> <accessor> def res1: Unit = $iw.this.res1
      }
    }
  }
}
```

Adding the generated code to an issue is very useful for debugging.

## License

[The Apache Software License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt)
