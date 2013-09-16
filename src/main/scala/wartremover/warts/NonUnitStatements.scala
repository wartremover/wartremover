package org.brianmckenna.wartremover
package warts

object NonUnitStatements extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    val PrintName: TermName = "$print"

    def isIgnoredStatement(tree: Tree) = tree match {
      // Scala creates synthetic blocks with <init> calls on classes.
      // The calls return Object so we need to ignore them.
      case Apply(Select(_, nme.CONSTRUCTOR), _) => true
      case _ => false
    }

    def checkUnitLike(statements: List[Tree]) {
      statements.foreach { stat =>
        val unitLike = stat.isEmpty || stat.tpe == null || stat.tpe =:= typeOf[Unit] || stat.isDef || isIgnoredStatement(stat)
        if (!unitLike)
          u.error(stat.pos, "Statements must return Unit")
      }
    }

    new Traverser {
      override def traverse(tree: Tree) {
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
