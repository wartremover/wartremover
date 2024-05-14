package org.wartremover
package warts

object CaseClassPrivateApply extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      private var outerObjectNames: List[String] = Nil

      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case t if hasWartAnnotation(t) =>
          case m: ClassDef =>
            val outer = (if (m.symbol.flags.is(Flags.Module)) m.symbol.companionClass else m.symbol).fullName
            outerObjectNames ::= outer
            super.traverseTree(tree)(owner)
            outerObjectNames = outerObjectNames.tail
          case Apply(Select(obj, "apply"), args)
              if obj.symbol.companionClass.primaryConstructor.flags.is(Flags.Private) &&
                !outerObjectNames.toSet.apply(obj.symbol.fullName) &&
                obj.symbol.moduleClass
                  .methodMember("apply")
                  .filter(_.paramSymss.headOption.forall(_.lengthCompare(args) == 0))
                  .forall(_.flags.is(Flags.Synthetic)) =>
            error(obj.pos, "disable apply because constructor is private")
            super.traverseTree(tree)(owner)
          case _ =>
            super.traverseTree(tree)(owner)
        }
      }
    }
  }
}
