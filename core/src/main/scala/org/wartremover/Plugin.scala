package org.wartremover

import tools.nsc.plugins.PluginComponent
import tools.nsc.{Global, Phase}
import java.io.File
import java.net.{URL, URLClassLoader}

class Plugin(val global: Global) extends tools.nsc.plugins.Plugin {
  import global._

  val name = "wartremover"
  val description = "Linting library and plugin. Allows rules to run inside a plugin or inside macros."
  val components = List[PluginComponent](Traverser)

  private[this] var traversers: List[WartTraverser] = List.empty
  private[this] var onlyWarnTraversers: List[WartTraverser] = List.empty
  private[this] var excludedFiles: List[String] = List.empty

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

  override def processOptions(options: List[String], error: String => Unit): Unit = {
    val classPathEntries = filterOptions("cp", options).map {
      c =>
        val filePrefix = "file:"
        if (c startsWith filePrefix) {
          new File(c.drop(filePrefix.length)).getCanonicalFile.toURI.toURL
        } else {
          new URL(c)
        }
    }
    val classLoader = new URLClassLoader(classPathEntries.toArray, getClass.getClassLoader)
    val mirror = reflect.runtime.universe.runtimeMirror(classLoader)

    def ts(p: String) = {
      val traverserNames = filterOptions(p, options)
      traverserNames.map(getTraverser(mirror))
    }

    traversers = ts("traverser")
    onlyWarnTraversers = ts("only-warn-traverser")
    excludedFiles = filterOptions("excluded", options) flatMap (_ split ":") map (_.trim) map (new java.io.File(_).getAbsolutePath)
  }

  object Traverser extends PluginComponent {
    import global._

    val global = Plugin.this.global

    override val runsAfter = List("typer")

    override val runsBefore = List("patmat")

    val phaseName = "wartremover-traverser"

    override def newPhase(prev: Phase) = new StdPhase(prev) {
      override def apply(unit: CompilationUnit) = {
        val isExcluded = excludedFiles exists unit.source.file.absolute.path.startsWith

        if (!isExcluded) {
          def wartUniverse(onlyWarn: Boolean) = new WartUniverse {
            val universe: global.type = global
            def error(pos: Position, message: String) =
              if (onlyWarn) global.reporter.warning(pos, message)
              else global.reporter.error(pos, message)
            def warning(pos: Position, message: String) = global.reporter.warning(pos, message)
          }

          def go(ts: List[WartTraverser], onlyWarn: Boolean) =
            ts.foreach(_(wartUniverse(onlyWarn)).traverse(unit.body))

          go(traversers, onlyWarn = false)
          go(onlyWarnTraversers, onlyWarn = true)
        }
      }
    }
  }
}
