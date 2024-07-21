package org.wartremover

import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.plugins.PluginPhase

trait CompilerPluginCompat { self: Plugin =>
  override final def initialize(options: List[String])(using context: Context): List[PluginPhase] =
    initializeWartremoverPlugin(options, Option(context))
}
