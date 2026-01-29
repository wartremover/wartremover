package org.wartremover
package warts

object ListAppend extends WartTraverser {
  private[wartremover] def message: String = "Don't use List `:+` or `appended` method because too slow"
  override def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    val ColonPlus = TermName(":+").encodedName
    val Appended = TermName("appended")
    val listType = rootMirror.staticClass("scala.collection.immutable.List").asType.toTypeConstructor

    new Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          case t if hasWartAnnotation(u)(t) =>
          // Ignore trees marked by SuppressWarnings
          case Apply(TypeApply(Select(receiver, ColonPlus | Appended), typeArgs), args)
              if (receiver.tpe.typeConstructor <:< listType) &&
                args.nonEmpty &&
                (args.lengthCompare(2) <= 0) &&
                typeArgs.nonEmpty &&
                (typeArgs.lengthCompare(2) <= 0) =>
            error(u)(tree.pos, message)
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
