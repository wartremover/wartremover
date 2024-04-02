package org.wartremover
package warts

object CaseClassPrivateApply extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    new u.Traverser {
      private[this] var outerObjectNames: List[String] = Nil

      override def traverse(tree: Tree): Unit = {
        tree match {
          case _ if hasWartAnnotation(u)(tree) =>
          case m @ ModuleDef(_, _, _) if m.symbol != null =>
            val outer = m.symbol.fullName
            outerObjectNames ::= outer
            super.traverse(tree)
            outerObjectNames = outerObjectNames.tail
          case m @ ClassDef(_, _, _, _) if m.symbol != null =>
            val outer = m.symbol.fullName
            outerObjectNames ::= outer
            super.traverse(tree)
            outerObjectNames = outerObjectNames.tail
          case Apply(Select(obj, TermName("apply")), args)
              if (obj.symbol != null) && obj.symbol.isModule &&
                !outerObjectNames.toSet.apply(obj.symbol.fullName) &&
                obj.symbol.companion.isClass &&
                obj.symbol.companion.asClass.primaryConstructor.isPrivate &&
                (obj.symbol.companion.asClass.primaryConstructor.privateWithin == NoSymbol) &&
                obj.tpe.members.iterator
                  .filter(_.name.toString == "apply")
                  .collect { case m if m.isMethod => m.asMethod }
                  .filter(_.paramLists.head.lengthCompare(args.size) == 0)
                  .forall(_.isSynthetic) =>
            error(u)(obj.pos, "disable apply because constructor is private")
            super.traverse(tree)
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
