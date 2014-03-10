# WartRemover

[![Build Status](https://travis-ci.org/typelevel/wartremover.png?branch=master)](https://travis-ci.org/typelevel/wartremover)

WartRemover is a flexible Scala code linting tool.

## Usage

WartRemover can be used in the following ways:

* As a command-line tool
* As a compiler plugin
* To derive macros

### Command-line

Compile the command-line tool via `sbt assembly`.

    $ ./wartremover -traverser org.brianmckenna.wartremover.warts.Unsafe src/main/scala/wartremover/Plugin.scala
    src/main/scala/wartremover/Plugin.scala:15: error: var is disabled
      private[this] var traversers: List[WartTraverser] = List.empty
                        ^

### Compiler plugin

Add the following to `build.sbt`:

```scala
resolvers += Resolver.sonatypeRepo("releases")

addCompilerPlugin("org.brianmckenna" %% "wartremover" % "0.7")

scalacOptions in (Compile, compile) += "-P:wartremover:traverser:org.brianmckenna.wartremover.warts.Unsafe"
```

### Macros

You can make any wart into a macro, like so:

    scala> import language.experimental.macros
    import language.experimental.macros

    scala> import org.brianmckenna.wartremover.warts.Unsafe
    import org.brianmckenna.wartremover.warts.Unsafe

    scala> def safe(expr: Any) = macro Unsafe.asMacro
    safe: (expr: Any)Any

    scala> safe { null }
    <console>:10: error: null is disabled
                  safe { null }
                         ^

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
