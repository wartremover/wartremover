package org.wartremover

import tools.nsc.plugins.PluginComponent
import tools.nsc.{Global, Phase}
import java.io.File
import java.net.{URL, URLClassLoader}
import scala.collection.mutable

class Plugin(val global: Global) extends tools.nsc.plugins.Plugin {
  val name = "wartremover"
  val description = "Linting library and plugin. Allows rules to run inside a plugin or inside macros."
  val components = List[PluginComponent](Traverser)

  private[this] var _traversers: List[WartTraverser] = List.empty
  private[this] var _onlyWarnTraversers: List[WartTraverser] = List.empty
  private[this] var _excludedFiles: List[String] = List.empty

  def getTraverser(mirror: reflect.runtime.universe.Mirror)(name: String): List[WartTraverser] = {
    val moduleSymbol = mirror.staticModule(name)
    mirror.reflectModule(moduleSymbol).instance match {
      case c: Container => c.items
      case s: WartTraverser => List(s)
    }
   }

  def prefixedOption(prefix: String)(option: String) =
    if (option.startsWith(s"$prefix:"))
      Some(option.substring(s"$prefix:".length))
    else
      None

  def filterOptions(prefix: String, options: List[String]) =
    options.map(prefixedOption(prefix)).flatten

  override def init(options: List[String], error: String => Unit): Boolean = {
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

    def ts(p: String, skip: collection.Set[String]): List[String] = {
      filterOptions(p, options)
         .flatMap(_ split ";")
         .map { s =>
           (if ( s contains '.' ) "" else "org.wartremover.warts.") + s.trim
         }
         .filterNot(skip.contains)
    }

    def traversers(names: List[String], skip: collection.Set[String]) =
      names.flatMap(getTraverser(mirror)).distinct.filterNot(t => skip.contains(t.className))

    val skip = mutable.Set (ts("skip", Set()):_*)
    val errorNames = ts("traverser", skip)
    _traversers = traversers(errorNames, skip)
    skip ++= errorNames
    _onlyWarnTraversers = traversers(ts("only-warn-traverser", skip), skip)
    _excludedFiles = filterOptions("excluded", options) flatMap (_ split ":") map (_.trim) map (new java.io.File(_)
       .getAbsolutePath)
    true
  }

  def traversers = _traversers
  def onlyWarnTraversers = _onlyWarnTraversers
  def excludedFiles = _excludedFiles

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
