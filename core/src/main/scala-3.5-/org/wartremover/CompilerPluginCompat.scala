package org.wartremover

import dotty.tools.dotc.plugins.PluginPhase

trait CompilerPluginCompat { self: Plugin =>
  override final def init(options: List[String]): List[PluginPhase] =
    initializeWartremoverPlugin(options, None)
}
