package org.brianmckenna.wartremover
package warts

import org.brianmckenna.wartremover.{WartTraverser, WartUniverse}

/*
 Note: NumericRange is broken.
   That means, when using it (collection methods: map, foreach, zip), it depends
   on which method is used for constructing the new collections. You can go two paths on NumericRange:
   1) The "apply" path:   @see scala.collection.immutable.NumericRange.locationAfterN
   2) The "foreach" path: @see scala.collection.immutable.NumericRange.foreach

   The difference between these paths is that "foreach" accumulates values to obtain the current value
   (current += step), but "apply" will directly calculate the current value (start + (step * fromInt(n))).
   The apply path is more accurate. This means that the values on these path will diverge due to other
   rounding behavior.

   For example:

   val range = 0D to 1D by (1 / 7D)

   1) Apply path
   range.iterator.toVector // uses apply in range.iterator in the end
   Vector(
     0.0,
     0.14285714285714285,
     0.2857142857142857,
     0.42857142857142855,
     0.5714285714285714,
     0.7142857142857142,
     0.8571428571428571,
     1.0
   )

   2) Foreach path
   range.toVector // uses foreach at CanBuildFrom respectively ++= in the end
   Vector(
     0.0,
     0.14285714285714285,
     0.2857142857142857,
     0.42857142857142855,
     0.5714285714285714,
     0.7142857142857142,
     0.857142857142857,
     0.9999999999999998
   )

   As one can see the last two values differ and iterator/apply is more accurate.
*/
// TODO perhaps we can raise an error only if a numeric range isn't used by the apply path?!
object FloatingPointNumericRange extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    val By: TermName = scala.reflect.NameTransformer.encode("by")
    val partialRangeSymbol: Symbol = rootMirror.staticClass("scala.collection.immutable.Range.Partial")

    new Traverser {
      override def traverse(tree: Tree) {
        tree match {
          // Ignore trees marked by SuppressWarnings
          case t if hasWartAnnotation(u)(t) =>

          case Apply(Select(partialRangeTree, By), _) if partialRangeTree.tpe.widen contains partialRangeSymbol =>
            // Note: method by is only defined for floating point partial ranges, see scala.runtime.FractionalProxy,
            // so we don't have to check for floating point types here.
            u.error(tree.pos, "Do not use NumericRange for floating point Numbers, it's broken")

          case _ => super.traverse(tree)
        }
      }
    }
  }
}
