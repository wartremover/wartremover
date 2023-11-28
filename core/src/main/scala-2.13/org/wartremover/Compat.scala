package org.wartremover

import scala.reflect.api.Universe

private[wartremover] class Compat[U <: Universe](val universe: U) {
  val NamedArg: universe.NamedArg.type = universe.NamedArg
}
