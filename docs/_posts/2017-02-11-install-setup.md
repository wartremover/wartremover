---
layout: page
title: "Installation & Setup"
category: doc
date: 2017-02-11 0:00:00
order: 0
---
## sbt
Add the following to your `project/plugins.sbt`:

```scala
addSbtPlugin("org.wartremover" % "sbt-wartremover" % "2.4.16")
```

**NOTE**: `sbt-wartremover` requires sbt version 1.0+. [for sbt 0.13.x](https://github.com/wartremover/wartremover/blob/da1e629e3367c0ec/docs/_posts/2017-02-11-install-setup.md)

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

## Suppressing Errors & Warnings

To exclude a specific piece of code from one or more checks, use the `SuppressWarnings` annotation:

```scala
@SuppressWarnings(Array("org.wartremover.warts.Var", "org.wartremover.warts.Null"))
var foo = null

// suppress all warts
@SuppressWarnings(Array("org.wartremover.warts.All"))
var bar = null
```

To exclude a file or directory from all checks, use `wartremoverExcluded` in your `build.sbt` file:

```scala
wartremoverExcluded += baseDirectory.value / "src" / "main" / "scala" / "SomeFile.scala"
wartremoverExcluded += sourceManaged.value
```

## Other ways of using WartRemover

Apart from using the sbt plugin to set it up for your project, WartRemover can also be used in the following ways:

* as a command-line tool,
* as a compiler plugin with *manually* provided `scalac` options,
* to derive macros.

### Command-line

Compile the command-line tool via `sbt "++ 2.12.14" core/assembly` and then use the provided `wartremover` shell script:

    $ sbt "++ 2.12.14" core/assembly
    
      ...
    
    $ ./wartremover -traverser org.wartremover.warts.Unsafe core/src/main/scala/wartremover/Plugin.scala
    core/src/main/scala/wartremover/Plugin.scala:15: error: var is disabled
      private[this] var traversers: List[WartTraverser] = List.empty
                        ^

### Compiler plugin (manually)

Similarly to the sbt plugin, warts needed to be checked and whether to report failiure as error or 
warning can be defined as scalac option in the format -P:wartremover:_( traverser / only-warn-traverser / skip)_:
(semicolon
separated list of wart names). 

Built-in warts can be defined by their simple class name like.

`-P:wartremover:traverser:Any,Var`

#### checks reported as error
Add the following to `build.sbt`:

```scala
addCompilerPlugin("org.wartremover" %% "wartremover" % "2.4.16" cross CrossVersion.full)

scalacOptions += "-P:wartremover:traverser:org.wartremover.warts.Unsafe"
```

#### checks reported as warnings
By default, WartRemover generates compile-time errors. If you want to be warned only, use an `only-warn-traverser`:

```scala
scalacOptions += "-P:wartremover:only-warn-traverser:org.wartremover.warts.Unsafe"
```

#### checks to skip (both error and warning)
Since there are 39 [built-in warts](https://www.wartremover.org/doc/warts.html) specifying the 
needed ones can be cumbersome. Similarly to the sbt plugin's `Warts.allBut` both the checks defined 
as error and warning can be filtered by the option `skip`. It can be particularly useful when using 
the special warts `All` or `Unsafe` that are only containers of other warts.
```scala
scalacOptions += "-P:wartremover:only-warn-traverser:All"
scalacOptions += "-P:wartremover:skip:While,Return"
```

#### exclude files
If you don't want to perform the checks in some file(s), you can use:

```scala
scalacOptions += "-P:wartremover:excluded:PATH_TO_FILE:PATH_TO_OTHER_FILE"
```

The `excluded` option accepts a colon-separated list of paths to files to ignore.

#### custom wart
To use your custom `WartTraverser`, you'll need to provide a classpath where it can be found:

```scala
scalacOptions += "-P:wartremover:cp:someUrl"
```

### Gradle
Support for Gradle is provided by the [Gradle-Wartremover plugin](https://github.com/augi/gradle-wartremover):

```gradle
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
      classpath 'cz.augi:gradle-wartremover:putCurrentVersionHere'
    }
}
apply plugin: 'wartremover'
```

The plugin can be also applied using [the new Gradle syntax](https://plugins.gradle.org/plugin/cz.augi.gradle.wartremover):
```gradle
plugins {
    id 'cz.augi.gradle.wartremover' version 'putCurrentVersionHere'
}
```

It automatically uses the safe Warts for all Scala code.

### Apache Maven

You can use WartRemover in Maven by employing it as a compilerPlugin to scala-maven-plugin:

```xml
<build>
  <plugins>
    <plugin>
      <groupId>net.alchim31.maven</groupId>
      <artifactId>scala-maven-plugin</artifactId>
      <version>3.2.1</version>
      <configuration>
        <compilerPlugins>
          <compilerPlugin>
            <groupId>org.wartremover</groupId>
            <artifactId>wartremover_2.13</artifactId>
            <version>2.4.16</version>
          </compilerPlugin>
        </compilerPlugins>
        <args>
          <arg>-P:wartremover:only-warn-traverser:org.wartremover.warts.Unsafe</arg>
        </args>
      </configuration>
    </plugin>
  </plugins>
</build>
```

See the notes on the compiler plugin above for options to pass as `<arg>`s.

### Macros

You can make any wart into a macro, like so:

    scala> import language.experimental.macros
    import language.experimental.macros

    scala> import org.wartremover.warts.Unsafe
    import org.wartremover.warts.Unsafe

    scala> def safe(expr: Any) = macro Unsafe.asMacro
    safe: (expr: Any)Any

    scala> safe { null }
    <console>:10: error: null is disabled
                  safe { null }
                         ^
