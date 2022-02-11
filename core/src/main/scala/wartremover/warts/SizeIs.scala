package org.wartremover
package warts

import scala.reflect.NameTransformer

object SizeIs extends WartTraverser {
  override def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    object Method {
      private[this] val values = Seq(
        "<", "<=", "==", ">", ">="
      ).map(NameTransformer.encode).map(TermName.apply(_))

      def unapply(t: Name): Boolean = values.contains(t)
    }
    object IsScalaCollection {
      def unapply(t: Tree): Boolean = t.tpe <:< typeOf[collection.Iterable[Any]]
    }
    new Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          case t if hasWartAnnotation(u)(t) =>
          case Apply(Select(Select(IsScalaCollection(), a @ (TermName("size") | TermName("length"))), Method()), List(_)) if !isSynthetic(u)(tree) =>
            error(u)(tree.pos, s"Maybe you can use `${a.decodedName}Is` instead of `${a.decodedName}`")
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
