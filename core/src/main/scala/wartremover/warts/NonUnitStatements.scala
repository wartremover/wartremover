package org.brianmckenna.wartremover
package warts

object NonUnitStatements extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    import scala.reflect.NameTransformer

    val ReadName: TermName = TermName("$read")
    val IwName: TermName = TermName("$iw")
    val NodeBufferAddName: TermName = TermName(NameTransformer.encode("&+"))

    def isIgnoredStatement(tree: Tree) = tree match {
      // Scala creates synthetic blocks with <init> calls on classes.
      // The calls return Object so we need to ignore them.
      case Apply(Select(_, termNames.CONSTRUCTOR), _) => true
      // scala.xml.NodeBuffer#&+ returns NodeBuffer instead of Unit, so
      // val x = <x>5</x> desugars to a non-Unit statement; ignore.
      case Apply(Select(qual, NodeBufferAddName), _)
        if qual.symbol.typeSignature =:= typeOf[scala.xml.NodeBuffer] => true
      // REPL needs this
      case Select(Select(Select(Ident(_), ReadName), IwName), IwName) => true
      case _ => false
    }

    def checkUnitLike(statements: List[Tree]): Unit = {
      statements.foreach { stat =>
        val unitLike = stat.isEmpty || stat.tpe == null || stat.tpe =:= typeOf[Unit] || stat.isDef || isIgnoredStatement(stat)
        if (!unitLike)
          u.error(stat.pos, "Statements must return Unit")
      }
    }

    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          case Block(statements, _) =>
            checkUnitLike(statements)
          case ClassDef(_, _, _, Template((_, _, statements))) =>
            checkUnitLike(statements)
          case ModuleDef(_, _, Template((_, _, statements))) =>
            checkUnitLike(statements)
          case _ =>
        }
        super.traverse(tree)
      }
    }
  }
}
