package wartremover

import sbt._
import Keys._

object Warts {

  /** All predefined Warts in [[Wart]]. */
  lazy val all: Seq[Wart] = {
    val ms = Wart.getClass.getDeclaredMethods filter
      (m => m.getParameterTypes.isEmpty && m.getReturnType == classOf[Wart])
    ms.map(_.invoke(Wart).asInstanceOf[Wart]).toSeq
  }

  /** All predefined Warts in [[Wart]], except the ones you list in `ws`. */
  def allBut(ws: Wart*): Seq[Wart] = all filterNot (w => ws exists (_.clazz == w.clazz))

}
