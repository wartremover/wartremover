package org.wartremover

import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.plugins.PluginPhase
import dotty.tools.dotc.plugins.StandardPlugin
import dotty.tools.dotc.typer.TyperPhase
import java.io.File
import java.net.URI
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

class Plugin extends StandardPlugin with CompilerPluginCompat {
  override def name = "wartremover"

  override def description = "wartremover"

  private val initialLog = new AtomicBoolean(true)

  protected final def initializeWartremoverPlugin(
    options: List[String],
    context: Option[Context]
  ): List[PluginPhase] = {
    val excluded = options.collect { case s"excluded:${path}" =>
      new File(path).getAbsolutePath
    }
    val classPathEntries = options.collect {
      case s"cp:file:${c}" =>
        val f = new File(c)
        f.getCanonicalFile.toURI.toURL
      case s"cp:${c}" =>
        new URI(c).toURL
    }
    val classLoader = new URLClassLoader(classPathEntries.toArray, getClass.getClassLoader)
    val (errors1, errorWarts) = options.collect { case s"traverser:${name}" =>
      Plugin.loadWart(name, classLoader)
    }.partitionMap(identity)
    val (errors2, warningWarts) = options.collect { case s"only-warn-traverser:${name}" =>
      Plugin.loadWart(name, classLoader)
    }.partitionMap(identity)
    val profile = options.collectFirst { case s"profile:${filePath}" if filePath.trim.nonEmpty => filePath }
    val loglevel = options.collect { case s"loglevel:${level}" =>
      LogLevel.map.get(level)
    }.flatten.headOption.getOrElse(LogLevel.Disable)
    val optionsSet = options.toSet
    val throwIfLoadFail = optionsSet.contains("on-wart-load-error:failure")
    val separatePhase = optionsSet.contains("separate-phase")
    val loadFailureWarts = errors1 ++ errors2
    if (throwIfLoadFail && loadFailureWarts.nonEmpty) {
      println(loadFailureWarts.mkString("load failure warts = ", ", ", ""))
      throw loadFailureWarts.head._2
    }
    val allPhases: List[String] = (errorWarts ++ warningWarts).flatMap(_.runsAfter).distinct.toList.sorted
    allPhases.flatMap { phase =>
      def extractCompoisteTraverser(xs: List[WartTraverser]): List[WartTraverser] = xs.flatMap {
        case x: CompositeTraverser => x.traversers
        case x => x :: Nil
      }.distinct

      val errorWartsInThisPhase = extractCompoisteTraverser(errorWarts).filter(_.runsAfter(phase))
      val warningWartsInThisPhase = extractCompoisteTraverser(warningWarts).filter(_.runsAfter(phase))

      val duplicateNames: Set[String] = (errorWartsInThisPhase ++ warningWartsInThisPhase)
        .groupBy(_.simpleName)
        .collect { case (k, v) if v.sizeIs > 1 => k }
        .toSet

      if (separatePhase) {
        def phaseName(w: WartTraverser): String = {
          val wartName: String = if (duplicateNames(w.simpleName)) w.fullName else w.simpleName
          s"${WartremoverPhase.defaultWartremoverPhaseName}-${phase}-${wartName}"
        }

        List(
          errorWartsInThisPhase.map { w =>
            new WartremoverPhase(
              errorWarts = w :: Nil,
              warningWarts = Nil,
              loadFailureWarts = loadFailureWarts,
              excluded = excluded,
              logLevel = loglevel,
              initialLog = initialLog,
              runsAfter = Set(phase),
              phaseName = phaseName(w),
              profile = None,
            )
          },
          warningWartsInThisPhase.map { w =>
            new WartremoverPhase(
              errorWarts = Nil,
              warningWarts = w :: Nil,
              loadFailureWarts = loadFailureWarts,
              excluded = excluded,
              logLevel = loglevel,
              initialLog = initialLog,
              runsAfter = Set(phase),
              phaseName = phaseName(w),
              profile = None,
            )
          },
        ).flatten
      } else {
        new WartremoverPhase(
          errorWarts = errorWartsInThisPhase,
          warningWarts = warningWartsInThisPhase,
          loadFailureWarts = loadFailureWarts,
          excluded = excluded,
          logLevel = loglevel,
          initialLog = initialLog,
          runsAfter = Set(phase),
          phaseName = phase match {
            case TyperPhase.name =>
              WartremoverPhase.defaultWartremoverPhaseName
            case _ =>
              s"${WartremoverPhase.defaultWartremoverPhaseName}-${phase}"
          },
          profile = profile
        ) :: Nil
      }
    }
  }
}
