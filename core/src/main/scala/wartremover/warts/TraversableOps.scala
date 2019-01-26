package org.wartremover
package warts

object TraversableOps extends WartTraverser {

  class Op(name: String, error: String) extends WartTraverser {
    override lazy val className = "org.wartremover.warts.TraversableOps"

    def apply(u: WartUniverse): u.Traverser = {
      import u.universe._

      val symbol = try {
        rootMirror.staticClass("scala.collection.Traversable")
      } catch {
        case _: ScalaReflectionException =>
          // Traversable removed since Scala 2.13
          rootMirror.staticClass("scala.collection.Iterable")
      }
      val Name = TermName(name)
      new u.Traverser {
        override def traverse(tree: Tree): Unit = {
          tree match {
            // Ignore trees marked by SuppressWarnings
            case t if hasWartAnnotation(u)(t) =>
            case Select(left, Name) if left.tpe.baseType(symbol) != NoType =>
              error(u)(tree.pos, error)
            // TODO: This ignores a lot
            case LabelDef(_, _, rhs) if isSynthetic(u)(tree) =>
            case _ =>
              super.traverse(tree)
          }
        }
      }
    }
  }

  def apply(u: WartUniverse): u.Traverser =
    WartTraverser.sumList(u)(List(
      new Op("head", "head is disabled - use headOption instead"),
      new Op("tail", "tail is disabled - use drop(1) instead"),
      new Op("init", "init is disabled - use dropRight(1) instead"),
      new Op("last", "last is disabled - use lastOption instead"),
      new Op("reduce", "reduce is disabled - use reduceOption or fold instead"),
      new Op("reduceLeft", "reduceLeft is disabled - use reduceLeftOption or foldLeft instead"),
      new Op("reduceRight", "reduceRight is disabled - use reduceRightOption or foldRight instead"),
      new Op("maxBy", "maxBy is disabled - use foldLeft or foldRight instead"),
      new Op("max", "max is disabled - use foldLeft or foldRight instead"),
      new Op("minBy", "minBy is disabled - use foldLeft or foldRight instead"),
      new Op("min", "min is disabled - use foldLeft or foldRight instead")
    ))

}
