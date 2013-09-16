package org.brianmckenna.wartremover

import tools.nsc.plugins.PluginComponent
import tools.nsc.{Global, Phase}

import java.net.{URL, URLClassLoader}

class Plugin(val global: Global) extends tools.nsc.plugins.Plugin {
  import global._

  val name = "wartremover"
  val description = "Linting library and plugin. Allows rules to run inside a plugin or inside macros."
  val components = List[PluginComponent](Traverser)

  private[this] var traversers: List[WartTraverser] = List.empty

  def getTraverser(mirror: reflect.runtime.universe.Mirror)(name: String): WartTraverser = {
    val moduleSymbol = mirror.staticModule(name)
    val instance = mirror.reflectModule(moduleSymbol).instance
    instance.asInstanceOf[WartTraverser]
  }

  def prefixedOption(prefix: String)(option: String) =
    if (option.startsWith(s"$prefix:"))
      Some(option.substring(s"$prefix:".length))
    else
      None

  def filterOptions(prefix: String, options: List[String]) =
    options.map(prefixedOption(prefix)).flatten

  override def processOptions(options: List[String], error: String => Unit) {
    val classPathEntries = filterOptions("cp", options).map(new URL(_))
    val classLoader = new URLClassLoader(classPathEntries.toArray, getClass.getClassLoader)
    val mirror = reflect.runtime.universe.runtimeMirror(classLoader)

    val traverserNames = filterOptions("traverser", options)
    traversers = traverserNames.map(getTraverser(mirror))
  }

  object Traverser extends PluginComponent {
    import global._

    val global = Plugin.this.global

    override val runsAfter = List("typer")

    val phaseName = "wartremover-traverser"

    override def newPhase(prev: Phase) = new StdPhase(prev) {
      override def apply(unit: CompilationUnit) {
        val wartUniverse = new WartUniverse {
          val universe: global.type = global
          def error(pos: Position, message: String) = unit.error(pos, message)
          def warning(pos: Position, message: String) = unit.warning(pos, message)
        }

        traversers.foreach(_(wartUniverse).traverse(unit.body))
      }
    }
  }
}
