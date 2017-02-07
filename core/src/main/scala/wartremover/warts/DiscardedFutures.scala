package org.wartremover
package warts

import scala.annotation.tailrec

object DiscardedFutures extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    import scala.reflect.NameTransformer

    val ReadName: TermName = "$read"
    val IwName: TermName = "$iw"
    val NodeBufferAddName: TermName = NameTransformer.encode("&+")

    @tailrec
    def isClassConstructor(tree: Tree): Boolean = tree match {
      case Select(_, nme.CONSTRUCTOR) => true
      case Apply(t, _) => isClassConstructor(t)
      case _ => false
    }

    def isIgnoredStatement(tree: Tree) = tree match {
      // scala.xml.NodeBuffer#&+ returns NodeBuffer instead of Unit, so
      // val x = <x>5</x> desugars to a non-Unit statement; ignore.
      case Apply(Select(qual, NodeBufferAddName), _)
        if qual.symbol.typeSignature =:= typeOf[scala.xml.NodeBuffer] => true
      // Scala creates synthetic blocks with <init> calls on classes.
      // The calls return Object so we need to ignore them.
      case t @ Apply(_, _) => isClassConstructor(t)
      // REPL needs this
      case Select(Select(Select(Ident(_), ReadName), IwName), IwName) => true
      case _ => false
    }

    def checkDiscardedFuture(statements: List[Tree]): Unit = {
      statements.foreach { stat =>
        val unitLike = stat.isEmpty || stat.tpe == null || stat.tpe =:= typeOf[Unit] || stat.isDef || isIgnoredStatement(stat)
        if (!unitLike && stat.tpe.typeConstructor.toString.contains("Future"))
          u.error(stat.pos, "Discarded Future")
      }
    }

    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          // Ignore trees marked by SuppressWarnings
          case t if hasWartAnnotation(u)(t) =>
          case Block(statements, _) =>
            checkDiscardedFuture(statements)
            super.traverse(tree)
          case ClassDef(_, _, _, Template((_, _, statements))) =>
            checkDiscardedFuture(statements)
            super.traverse(tree)
          case ModuleDef(_, _, Template((_, _, statements))) =>
            checkDiscardedFuture(statements)
            super.traverse(tree)
          case _ => super.traverse(tree)
        }
      }
    }
  }
}
