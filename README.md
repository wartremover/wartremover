# WartRemover

WartRemover is a flexible Scala code linting tool.

## Usage

WartRemover can be used in the following ways:

* As a command-line tool
* As a compiler plugin
* To derive macros

### Command-line

    $ ./wartremover -traverser org.brianmckenna.wartremover.warts.Unsafe src/main/scala/wartremover/Plugin.scala
    src/main/scala/wartremover/Plugin.scala:15: error: var is disabled
      private[this] var traversers: List[WartTraverser] = List.empty
                        ^

### Compiler plugin

    addCompilerPlugin("org.brianmckenna" % "wartremover" % "0.5" cross CrossVersion.full)

    scalacOptions += "-P:wartremover:traverser:org.brianmckenna.wartremover.warts.Unsafe"

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

## Warts

Here is a list of built-in warts under the
`org.brianmckenna.wartremover.warts` package.

### Any2StringAdd

Scala has an implicit which converts anything to a `String` if the
right hand side of `+` is a `String`.

    // Won't compile: Scala inserted an any2stringadd call
    println({} + "test")

### NonUnitStatements

Scala allows statements to return any type. Statements should only
return `Unit` (this ensures that they're really intended to be
statements).

    // Won't compile: Statements must return Unit
    10
    false

### Nothing

Nothing is a special bottom type; it is a subtype of every other
type. The Scala compiler loves to infer Nothing as a generic type but
that is almost always incorrect. Explicit type arguments should be
used instead.

    // Won't compile: Inferred type containing Nothing from assignment
    val nothing = ???
    val nothingList = List.empty

### Null

`null` is a special value that inhabits all reference types. It breaks
type safety.

    // Won't compile: null is disabled
    val s: String = null

### Unsafe

Checks for the following warts:

* Null
* NonUnitStatements
* Any2StringAdd
* Var

### Var

Mutation breaks equational reasoning.

    // Won't compile: var is disabled
    var x = 100

## Writing Wart Rules

A wart rule has to be an object which extends `WartTraverser`. The
object only needs an `apply` method which takes a `WartUniverse` and
returns a `WartUniverse#universe#Traverser`.

The `WartUniverse` has `error` and `warning` methods which both take
`(WartUniverse#universe#Position, String)`. They are side-effecting
methods for adding errors and warnings.

Most traversers will want a `super.traverse` call to be able to
recursively continue.

    import org.brianmckenna.wartremover.{WartTraverser, WartUniverse}

    object Unimplemented extends WartTraverser {
      def apply(u: WartUniverse): u.Traverser = {
        import u.universe._

        val NotImplementedName: TermName = "$qmark$qmark$qmark" // ???
        new Traverser {
          override def traverse(tree: Tree) {
            tree match {
              case Select(_, NotImplementedName) =>
                u.error(tree.pos, "There was something left unimplemented")
              case _ =>
            }
            super.traverse(tree)
          }
        }
      }
    }
