package org.wartremover

import tools.nsc.plugins.PluginComponent
import tools.nsc.Global
import tools.nsc.Phase
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import scala.reflect.internal.util.NoPosition
import scala.util.control.NonFatal

class Plugin(val global: Global) extends tools.nsc.plugins.Plugin {

  val name = "wartremover"
  val description = "Linting library and plugin. Allows rules to run inside a plugin or inside macros."
  val components = List[PluginComponent](Traverser)

  private[this] var traversers: List[WartTraverser] = List.empty
  private[this] var onlyWarnTraversers: List[WartTraverser] = List.empty
  private[this] var excludedFiles: List[String] = List.empty
  private[this] var logLevel: LogLevel = LogLevel.Disable

  def getTraverser(mirror: reflect.runtime.universe.Mirror)(name: String): WartTraverser = {
    val moduleSymbol = mirror.staticModule(name)
    val instance = mirror.reflectModule(moduleSymbol).instance
    instance.asInstanceOf[WartTraverser]
  }

  def prefixedOption(prefix: String)(option: String): Option[String] =
    if (option.startsWith(s"$prefix:"))
      Some(option.substring(s"$prefix:".length))
    else
      None

  def filterOptions(prefix: String, options: List[String]): List[String] =
    options.flatMap(prefixedOption(prefix))

  override def init(options: List[String], error: String => Unit): Boolean = {
    val classPathEntries = filterOptions("cp", options).map { c =>
      val filePrefix = "file:"
      if (c startsWith filePrefix) {
        new File(c.drop(filePrefix.length)).getCanonicalFile.toURI.toURL
      } else {
        new URL(c)
      }
    }
    val classLoader = new URLClassLoader(classPathEntries.toArray, getClass.getClassLoader)
    val mirror = reflect.runtime.universe.runtimeMirror(classLoader)

    val optionsSet = options.toSet
    val throwIfLoadFail = optionsSet.contains("on-wart-load-error:failure")

    def ts(p: String): List[WartTraverser] = {
      val traverserNames = filterOptions(p, options)
      val success = List.newBuilder[WartTraverser]
      val failure = List.newBuilder[(String, Throwable)]
      traverserNames.foreach { name =>
        try {
          success += getTraverser(mirror)(name)
        } catch {
          case NonFatal(e) =>
            failure += ((name, e))
        }
      }
      val loadFail = failure.result()
      if (loadFail.nonEmpty) {
        global.reporter.warning(NoPosition, loadFail.mkString("load failure warts = ", ", ", ""))
        if (throwIfLoadFail) {
          throw loadFail.head._2
        }
      }
      success.result()
    }

    filterOptions("loglevel", options).flatMap(LogLevel.map.get).headOption.foreach { loglevel =>
      this.logLevel = loglevel
    }

    traversers = ts("traverser")
    onlyWarnTraversers = ts("only-warn-traverser")
    excludedFiles =
      filterOptions("excluded", options) flatMap (_ split ":") map (_.trim) map new java.io.File(_).getAbsolutePath

    logLevel match {
      case LogLevel.Debug | LogLevel.Info =>
        if (traversers.nonEmpty) {
          global.reporter.echo("error warts = " + traversers.map(_.getClass.getName.dropRight(1)).mkString(", "))
        }
        if (onlyWarnTraversers.nonEmpty) {
          global.reporter.echo(
            "warning warts = " + onlyWarnTraversers.map(_.getClass.getName.dropRight(1)).mkString(", ")
          )
        }
        if (excludedFiles.nonEmpty) {
          global.reporter.echo("exclude = " + excludedFiles.mkString(", "))
        }
      case LogLevel.Disable =>
    }

    true
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

        if (isExcluded) {
          logLevel match {
            case LogLevel.Debug =>
              reporter.echo("skip wartremover " + unit.source.path)
            case _ =>
          }
        } else {
          def wartUniverse(onlyWarn: Boolean) = new WartUniverse {
            val universe: global.type = global
            def error(pos: Position, message: String) =
              if (onlyWarn) global.reporter.warning(pos, message)
              else global.reporter.error(pos, message)
            def warning(pos: Position, message: String) = global.reporter.warning(pos, message)
            override val logLevel: LogLevel = Plugin.this.logLevel
          }

          def go(ts: List[WartTraverser], onlyWarn: Boolean): Unit = {
            ts.foreach { traverser =>
              try {
                traverser.apply(wartUniverse(onlyWarn)).traverse(unit.body)
              } catch {
                case e: Throwable =>
                  val message = s"error wartremover ${traverser.className} ${unit.source.path} '${e.getMessage}'"
                  global.reporter.error(unit.targetPos, message)
                  throw e
              }
            }
          }

          logLevel match {
            case LogLevel.Debug =>
              global.reporter.echo(s"run wartremover ${unit.source.path}")
            case _ =>
          }
          go(traversers, onlyWarn = false)
          go(onlyWarnTraversers, onlyWarn = true)
        }
      }
    }
  }
}
