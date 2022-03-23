package org.wartremover.warts

import org.wartremover.WartTraverser
import org.wartremover.WartUniverse

object NoNeedImport extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case _ if hasWartAnnotation(tree) =>
          case Import(_, iss) =>
            iss.foldLeft(ImportTypeContainer.init) {
              case (acc, SimpleSelector("_")) =>
                acc.copy(existsWildCard = true)
              case (acc, OmitSelector(_)) =>
                acc.copy(existsNameToWildCard = true)
              case (acc, RenameSelector(_, _)) =>
                acc
              case (acc, _) =>
                acc.copy(existsRegular = true)
            } match {
              case ImportTypeContainer(true, true, _) =>
              case ImportTypeContainer(true, false, _) =>
                error(tree.pos, "Import into the wildcard(`something => _`) is meaningless. Remove it.")
              case ImportTypeContainer(false, true, true) =>
                error(tree.pos, "The wildcard import exists. Remove other explicitly names of the `import`.")
              case _ =>
            }
          case _ =>
            super.traverseTree(tree)(owner)
        }
      }
    }
  }
}
