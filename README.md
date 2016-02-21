# WartRemover

[![Build Status](https://travis-ci.org/puffnfresh/wartremover.png?branch=master)](https://travis-ci.org/puffnfresh/wartremover)

WartRemover is a flexible Scala code linting tool.

## Usage

Add the following to your `project/plugins.sbt`:

```scala
addSbtPlugin("org.brianmckenna" % "sbt-wartremover" % "0.14")
```

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
@SuppressWarnings(Array("org.brianmckenna.wartremover.warts.Var", "org.brianmckenna.wartremover.warts.Null"))
var foo = null
```

Finally, if you want to add your custom `WartTraverser`, provide its classpath first:

```scala
wartremoverClasspaths += "some-url"

wartremoverErrors += Wart.custom("org.your.custom.WartTraverser")
```

See also [other ways of using WartRemover](/OTHER-WAYS.md) for information on how to use it as a command-line tool, a macro or a compiler plugin, while providing all the `scalac` options manually.

* Note - the WartRemover SBT plugin sets scalac options - make sure you're not overwriting those by having a `scalacOptions := ...` setting in your SBT settings. Use `scalacOptions ++= ...` instead.


## Warts

Here is a list of built-in warts under the
`org.brianmckenna.wartremover.warts` package.

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

Scala has an implicit that converts anything to a `String` if the
right hand side of `+` is a `String`.

```scala
// Won't compile: Scala inserted an any2stringadd call
println({} + "test")
```

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

### ExplicitImplicitTypes

Scala has trouble correctly resolving implicits when some of them lack explicit result types. To avoid this, all implicits should have explicit type ascriptions.

### FinalCaseClass

Scala's case classes provide a useful implementation of logicless data types. Extending a case class can break this functionality in surprising ways. This can be avoided by always making them final.

```scala
// Won't compile: case classes must be final
case class Foo()
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

### ListOps

`scala.collection.immutable.List` has:

* `head`,
* `tail`,
* `init`,
* `last`,
* `reduce`,
* `reduceLeft` and
* `reduceRight` methods,

all of which will throw if the list is empty. The program should be refactored to use:

* `List#headOption`,
* `List#drop(1)`,
* `List#dropRight(1)`,
* `List#lastOption`,
* `List#reduceOption` or `List#fold`,
* `List#reduceLeftOption` or `List#foldLeft` and
* `List#reduceRightOption` or `List#foldRight` respectively,

to explicitly handle both the populated and empty `List`.

### MutableDataStructures

The standard library provides mutable collections. Mutation breaks equational reasoning.

```scala
// Won't compile: scala.collection.mutable package is disabled
import scala.collection.mutable.ListBuffer
val mutList = ListBuffer()
```

### NoNeedForMonad

Sometimes an additional power of `Monad` is not needed, and
`Applicative` is enough. This issues a warning in such cases
(not an error, since using a `Monad` instance might still be a conscious decision)

```scala
scala> for {
     | x <- List(1,2,3)
     | y <- List(2,3,4)
     | } yield x * y
<console>:19: warning: No need for Monad here (Applicative should suffice).
 > "If the extra power provided by Monad isn’t needed, it’s usually a good idea to use Applicative instead."
 Typeclassopedia (http://www.haskell.org/haskellwiki/Typeclassopedia)
 Apart from a cleaner code, using Applicatives instead of Monads can in general case result in a more parallel code.
 For more context, please refer to the aforementioned Typeclassopedia, http://comonad.com/reader/2012/abstracting-with-applicatives/, or http://www.serpentine.com/blog/2008/02/06/the-basics-of-applicative-functors-put-to-practical-work/
              x <- List(1,2,3)
                ^
res0: List[Int] = List(2, 3, 4, 4, 6, 8, 6, 9, 12)

scala> for {
     | x <- List(1,2,3)
     | y <- x to 3
     | } yield x * y
res1: List[Int] = List(1, 2, 3, 4, 6, 9)
```

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
```

### Option2Iterable

Scala inserts an implicit conversion from `Option` to `Iterable`. This can hide bugs and creates surprising situations like `Some(1) zip Some(2)` returning an `Iterable[(Int, Int)]`.

### OptionPartial

`scala.Option` has a `get` method which will throw if the value is
`None`. The program should be refactored to use `scala.Option#fold` to
explicitly handle both the `Some` and `None` cases.

### Product

`Product` is a type common to many structures; it is the supertype of
case classes and tuples. The Scala compiler loves to infer `Product` as
a generic type, but that is almost always incorrect. Explicit type
arguments should be used instead.

```scala
// Won't compile: Inferred type containing Product
val any = List((1, 2, 3), (1, 2))
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

### Throw

`throw` implies partiality. Encode exceptions/errors as return
values instead using `Either`. 

### ToString

Scala creates a `toString` method automatically for all classes. Since `toString` is based on the class name, any rename can potentially introduce bugs. This is especially pernicious for case objects. `toString` should be explicitly overridden wherever used.
```scala
case object Foo { override val toString = "Foo" }
```

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
import org.brianmckenna.wartremover.{WartTraverser, WartUniverse}

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
