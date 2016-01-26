package wartremover

import sbt._
import Keys._

object Warts {

  /** All predefined Warts in [[Wart]]. */
  lazy val all: Seq[Wart] = Wart.AllWarts

  /** All predefined Warts in [[Wart]], except the ones you list in `ws`. */
  def allBut(ws: Wart*): Seq[Wart] = all filterNot (w => ws exists (_.clazz == w.clazz))

  /** Warts known to be stable, defined in `Unsafe.scala`. */
  lazy val unsafe: Seq[Wart] = Wart.UnsafeWarts

}
