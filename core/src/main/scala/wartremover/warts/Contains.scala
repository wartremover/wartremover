package org.wartremover
package warts

object Contains extends WartTraverser {
  private[warts] def message: String = "Don't use List or Option `contains` method because is not typesafe"
  override def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    val containsName = TermName("contains").encodedName

    new u.Traverser {

      override def traverse(tree: Tree): Unit = {

        val listType = rootMirror.staticClass("scala.collection.immutable.List").asType.toTypeConstructor
        val optionType = rootMirror.staticClass("scala.Option").asType.toTypeConstructor

        tree match {
          case t if hasWartAnnotation(u)(t) =>
          // Ignore trees marked by SuppressWarnings
          case Apply(TypeApply(Select(receiver, method), _), _) if
            ((receiver.tpe.typeConstructor <:< listType) || (receiver.tpe.typeConstructor <:< optionType))
              && (method == containsName)
          =>
            error(u)(tree.pos, message)
          case _ =>
            super.traverse(tree)
        }

      }

    }
  }
}
