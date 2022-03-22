package mywarts

import org.wartremover.{ WartTraverser, WartUniverse }

object Unimplemented extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    import scala.reflect.NameTransformer

    val notImplementedName: TermName = TermName(NameTransformer.encode("???"))
    val notImplemented: Symbol = typeOf[Predef.type].member(notImplementedName)
    require(notImplemented != NoSymbol)
    new Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          case rt: RefTree if rt.symbol == notImplemented =>
            error(u)(tree.pos, "There was something left unimplemented")
          case _ =>
        }
        super.traverse(tree)
      }
    }
  }
}
