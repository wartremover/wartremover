package org.wartremover
package warts

object ListAppend extends WartTraverser {
  private[wartremover] def message: String = "Don't use List `:+` method because too slow"
  override def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    val colonPlus = TermName(":+").encodedName

    new Traverser {
      override def traverse(tree: Tree): Unit = {
        val listType = rootMirror.staticClass("scala.collection.immutable.List").asType.toTypeConstructor

        tree match {
          case t if hasWartAnnotation(u)(t) =>
          // Ignore trees marked by SuppressWarnings
          case Apply(TypeApply(Select(receiver, method), typeArgs), args) if
            (receiver.tpe.typeConstructor <:< listType) &&
            (method == colonPlus) &&
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
