package example

import org.wartremover.*

object DiableHoge extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case t: DefDef if t.name == "hoge" =>
            error(t.pos, "disable hoge")
          case _ =>
        }
        super.traverseTree(tree)(owner)
      }
    }
  }
}
