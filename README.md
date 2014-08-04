# WartRemover

[![Build Status](https://travis-ci.org/typelevel/wartremover.png?branch=master)](https://travis-ci.org/typelevel/wartremover)

WartRemover is a flexible Scala code linting tool.

## Usage

Add the following to your `project/plugins.sbt`:

```scala
resolvers += Resolver.sonatypeRepo("releases")

addSbtPlugin("org.brianmckenna" % "sbt-wartremover" % "0.11")
```

If you're using `sbt` ≥ 0.13.5, you'll be able to make use of the new auto plugin feature. If not, you'll probably want to add the following to your `build.sbt`:

```scala
import wartremover._

wartremoverSettings
```

Now, you can proceed to configure the linter in your `build.sbt`. By default, all errors and warnings are turned off. To turn on all errors, use:

```scala
wartremoverErrors ++= Warts.all
```

Similarly, to just issue warnings instead of errors for all built-in warts, you can use:

```scala
wartremoverWarnings ++= Warts.all
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

To exclude a package or a class from all checks:

```scala
wartremoverExcluded ++= Seq("org.some.package", "org.other.package.SomeClass")
```

Finally, if you want to add your custom `WartTraverser`, provide its classpath first:

```scala
wartremoverClasspaths += "some-url"

wartremoverErrors += Wart.custom("org.your.custom.WartTraverser")
```

See also [other ways of using WartRemover](/OTHER-WAYS.md) for information on how to use it as a command-line tool, a macro or a compiler plugin, while providing all the `scalac` options manually.


## Warts

Here is a list of built-in warts under the
`org.brianmckenna.wartremover.warts` package.

### Any

Any is the top type; it is the supertype of every other type. The
Scala compiler loves to infer Any as a generic type but that is
almost always incorrect. Explicit type arguments should be used
instead.

```scala
// Won't compile: Inferred type containing Any
val any = List(1, true, "three")
```

### Any2StringAdd

Scala has an implicit which converts anything to a `String` if the
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

### IsInstanceOf

`isInstanceOf` violates parametricity. Refactor so that the  type is established statically.

```scala
// Won't compile: isInstanceOf is disabled
x.isInstanceOf[String]
```

### ListOps

`scala.collection.immutable.List` has:

* `head`,
* `last` and
* `tail` methods,

all of which will throw if the list is empty. The program should be refactored to use:

* `List#headOption`,
* `List#lastOption` and
* `List#drop(1)` respectively,

to explicitly handle both the populated and empty `List`.

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

Nothing is a special bottom type; it is a subtype of every other
type. The Scala compiler loves to infer Nothing as a generic type but
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

### OptionPartial

`scala.Option` has a `get` method which will throw if the value is
`None`. The program should be refactored to use `scala.Option#fold` to
explicitly handle both the `Some` and `None` cases.

### Product

Product is a type common to many structures; it is the supertype of
case classes and tuples. The Scala compiler loves to infer Product as
a generic type but that is almost always incorrect. Explicit type
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

Serializable is a type common to many structures. The Scala compiler
loves to infer Serializable as a generic type but that is almost
always incorrect. Explicit type arguments should be used instead.

```scala
// Won't compile: Inferred type containing Serializable
val any = List((1, 2, 3), (1, 2))
```
### Unsafe

Checks for the following warts:

* Any
* Any2StringAdd
* AsInstanceOf
* EitherProjectionPartial
* IsInstanceOf
* NonUnitStatements
* Null
* OptionPartial
* Product
* Return
* Serializable
* Var
* ListOps

### Var

Mutation breaks equational reasoning.

```scala
// Won't compile: var is disabled
var x = 100
```

## Writing Wart Rules

A wart rule has to be an object which extends `WartTraverser`. The
object only needs an `apply` method which takes a `WartUniverse` and
returns a `WartUniverse#universe#Traverser`.

The `WartUniverse` has `error` and `warning` methods which both take
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
