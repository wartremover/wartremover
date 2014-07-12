# Other ways of using WartRemover

Apart from [using the sbt plugin](/README.md) to set it up for your project, WartRemover can also be used in the following ways:

* as a command-line tool,
* as a compiler plugin with *manually* provided `scalac` options,
* to derive macros.

## Command-line

Compile the command-line tool via `sbt core/assembly` and then use the provided `wartremover` shell script:

    $ sbt core/assembly
    
      ...
    
    $ ./wartremover -traverser org.brianmckenna.wartremover.warts.Unsafe core/src/main/scala/wartremover/Plugin.scala
    core/src/main/scala/wartremover/Plugin.scala:15: error: var is disabled
      private[this] var traversers: List[WartTraverser] = List.empty
                        ^

## Compiler plugin (manually)

Add the following to `build.sbt`:

```scala
resolvers += Resolver.sonatypeRepo("releases")

addCompilerPlugin("org.brianmckenna" %% "wartremover" % "0.10")

scalacOptions += "-P:wartremover:traverser:org.brianmckenna.wartremover.warts.Unsafe"
```

By default, WartRemover generates compile-time errors. If you want to be warned only, use an `only-warn-traverser`:

```scala
scalacOptions += "-P:wartremover:only-warn-traverser:org.brianmckenna.wartremover.warts.Unsafe"
```

If you don't want to perform the checks in some class (or even in a whole package), you can use:

```scala
scalacOptions += "-P:wartremover:excluded:package1:package2.Clazz"
```

Here, the option will prevent WartRemover from being applied in `package1` package and in `package2.Clazz` class (`excluded` accepts a colon-separated list of prefixes that are going to be ignored).

To use your custom `WartTraverser`, you'll need to provide a classpath where it can be found:

```scala
scalacOptions += "-P:wartremover:cp:someUrl"
```

## Macros

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

