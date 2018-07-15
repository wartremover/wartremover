---
layout: page
title: "Writing Your Own Warts"
category: doc
date: 2017-02-11 0:00:00
order: 2
---

A wart rule has to be an object that extends `WartTraverser`. The
object only needs an `apply` method which takes a `WartUniverse` and
returns a `WartUniverse#universe#Traverser`.

The `WartUniverse` has `error` and `warning` methods, which both take
`(WartUniverse#universe#Position, String)`. They are side-effecting
methods for adding errors and warnings.

Most traversers will want a `super.traverse` call to be able to
recursively continue.

```scala
package mywarts

import org.wartremover.{ WartTraverser, WartUniverse }

object Unimplemented extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    import scala.reflect.NameTransformer

    val notImplementedName: TermName = NameTransformer.encode("???")
    val notImplemented: Symbol = typeOf[Predef.type].member(notImplementedName)
    require(notImplemented != NoSymbol)
    new Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          case rt: RefTree if rt.symbol == notImplemented =>
            error(u)(tree.pos, "There was something left unimplemented")
          case _ =>
        }
        super.traverse(tree)
      }
    }
  }
}
```

Once you have your wart written you can add it to your config using `Wart.custom`:

```scala
wartremoverWarnings += Wart.custom("mywarts.Unimplemented")
```

You'll also need to add the package containing your wart to wartremover's classpath. The usual way to do this relies on adding your package as a library dependency:

```scala
libraryDependencies += "myOrg" %% "myWartPackage" % "1.0.0"
wartremoverClasspaths ++= {
  (dependencyClasspath in Compile).value.files
    .find(_.name.contains("myWartPackage"))
    .map(_.toURI.toString)
    .toList
}
```
