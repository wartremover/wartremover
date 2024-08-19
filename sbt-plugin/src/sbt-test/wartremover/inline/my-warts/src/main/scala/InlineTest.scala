package mywarts

import org.wartremover.WartTraverser
import org.wartremover.WartUniverse
import play.api.libs.json.Writes

object InlineTest extends WartTraverser {
  override val runsAfter = Set(dotty.tools.dotc.transform.Inlining.name)

  override def apply(u: WartUniverse): u.Traverser =
    new u.Traverser(this) {
      import q.reflect.*

      private var currentPosition: Option[Position] = None

      private def savePosition(p: Position): Unit = {
        if (p.sourceFile.getJPath.isEmpty || ((p.start == 0) && (p.end == 0))) {
          // skip. empty position
        } else if (p.sourceFile.path.contains("play-json/shared/src/main/scala-3")) {
          // skip. play-json internal
        } else {
          currentPosition = Option(p)
        }
      }

      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        savePosition(tree.pos)
        tree match {
          case _ if hasWartAnnotation(tree) =>
          case _ if tree.isExpr =>
            tree.asExpr match {
              case '{ Writes.DoubleWrites } =>
                val p = currentPosition.getOrElse(tree.pos)
                error(p, "don't use default Writes[Double] becasue throw error if NaN or Infinity")
              case _ =>
                super.traverseTree(tree)(owner)
            }
          case _ =>
            super.traverseTree(tree)(owner)
        }
      }
    }
}
