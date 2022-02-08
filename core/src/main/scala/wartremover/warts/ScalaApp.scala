package org.wartremover
package warts

object ScalaApp extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    new Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          case t if hasWartAnnotation(u)(t) =>
          case Template(parents, _, _) if parents.exists(_.symbol.fullName == "scala.App") =>
            error(u)(tree.pos, "Don't use scala.App. https://docs.scala-lang.org/scala3/book/methods-main-methods.html")
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
