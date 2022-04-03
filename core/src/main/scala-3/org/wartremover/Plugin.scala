package org.wartremover

import dotty.tools.dotc.plugins.PluginPhase
import dotty.tools.dotc.plugins.StandardPlugin
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import java.util.concurrent.atomic.AtomicBoolean
import scala.reflect.NameTransformer

object Plugin {
  private[wartremover] def loadWart(
    name: String,
    classLoader: ClassLoader
  ): Either[(String, Throwable), WartTraverser] = {
    try {
      val clazz = classLoader.loadClass(name + NameTransformer.MODULE_SUFFIX_STRING)
      val field = clazz.getField(NameTransformer.MODULE_INSTANCE_NAME)
      val instance = field.get(null)
      Right(instance.asInstanceOf[WartTraverser])
    } catch {
      case e: Throwable => Left((name, e))
    }
  }
}

class Plugin extends StandardPlugin {
  override def name = "wartremover"

  override def description = "wartremover"

  private[this] val initialLog = new AtomicBoolean(true)

  override def init(options: List[String]): List[PluginPhase] = {
    val excluded = options.collect { case s"excluded:${path}" =>
      new File(path).getAbsolutePath
    }
    val classPathEntries = options.collect {
      case s"cp:file:${c}" =>
        val f = new File(c)
        f.getCanonicalFile.toURI.toURL
      case s"cp:${c}" =>
        new URL(c)
    }
    val classLoader = new URLClassLoader(classPathEntries.toArray, getClass.getClassLoader)
    val (errors1, errorWarts) = options.collect { case s"traverser:${name}" =>
      Plugin.loadWart(name, classLoader)
    }.partitionMap(identity)
    val (errors2, warningWarts) = options.collect { case s"only-warn-traverser:${name}" =>
      Plugin.loadWart(name, classLoader)
    }.partitionMap(identity)
    val loglevel = options.collect { case s"loglevel:${level}" =>
      LogLevel.map.get(level)
    }.flatten.headOption.getOrElse(LogLevel.Disable)
    val newPhase = new WartremoverPhase(
      errorWarts = errorWarts,
      warningWarts = warningWarts,
      loadFailureWarts = errors1 ++ errors2,
      excluded = excluded,
      logLevel = loglevel,
      initialLog = initialLog,
    )
    newPhase :: Nil
  }
}
