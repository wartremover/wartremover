package org.wartremover

import argonaut.DecodeJson
import argonaut.EncodeJson
import java.net.URLClassLoader
import scala.quoted.Quotes
import scala.tasty.inspector.Inspector
import scala.tasty.inspector.Tasty
import scala.tasty.inspector.TastyInspector
import java.net.URL

final class WartRemoverInspector {
  private[this] implicit val inspectParamInstance: DecodeJson[InspectParam] =
    DecodeJson.derive[InspectParam]

  private[this] implicit val inspectResultInstance: EncodeJson[InspectResult] = {
    implicit val positionInstance: EncodeJson[Position] =
      EncodeJson.derive[Position]
    implicit val diagnosticInstance: EncodeJson[Diagnostic] =
      EncodeJson.derive[Diagnostic]

    EncodeJson.derive[InspectResult]
  }

  def runFromJson(json: String): String = {
    val param = argonaut.JsonParser
      .parse(json)
      .left
      .map(sys.error(_))
      .merge
      .as[InspectParam]
      .fold(
        (e, _) => sys.error(e),
        identity
      )
    val result = run(param)
    implicitly[EncodeJson[InspectResult]].encode(result).spaces2
  }

  def run(param: InspectParam): InspectResult = {
    if (param.tastyFiles.isEmpty) {
      println("tastyFiles is empty")
      InspectResult.empty
    } else {
      val classLoader = new URLClassLoader(param.wartClasspath.map(new URL(_)).toArray, getClass.getClassLoader)
      val (errorLoadFail, errorTraversers) = param.errorWarts.toList.partitionMap(Plugin.loadWart(_, classLoader))
      val (warnLoadFail, warningTraversers) = param.warningWarts.toList.partitionMap(Plugin.loadWart(_, classLoader))
      val loadFailed = errorLoadFail ++ warnLoadFail
      if (loadFailed.nonEmpty) {
        println("load fail warts = " + loadFailed.mkString(", "))
        if (param.failIfWartLoadError) {
          throw loadFailed.head._2
        }
      }
      if (errorTraversers.isEmpty && warningTraversers.isEmpty) {
        println("warts is empty")
        InspectResult.empty
      } else {
        run0(
          errorTraversers = errorTraversers,
          warningTraversers = warningTraversers,
          tastyFiles = param.tastyFiles.toList,
          dependenciesClasspath = param.dependenciesClasspath.toList,
          exclude = param.exclude,
          outputReporter = param.outputStandardReporter,
        )
      }
    }
  }

  private[this] def run0(
    errorTraversers: List[WartTraverser],
    warningTraversers: List[WartTraverser],
    tastyFiles: List[String],
    dependenciesClasspath: List[String],
    exclude: List[String],
    outputReporter: Boolean
  ): InspectResult = {
    val errors, warnings = List.newBuilder[Diagnostic]

    val inspector = new Inspector {
      def inspect(using q: Quotes)(tastys: List[Tasty[q.type]]): Unit = {
        import q.reflect.*
        def convertPos(p: Position): org.wartremover.Position = {
          org.wartremover.Position(
            start = p.start,
            startLine = p.startLine,
            startColumn = p.startColumn,
            end = p.end,
            endLine = p.endLine,
            endColumn = p.endColumn,
            path = p.sourceFile.path,
            sourceCode = p.sourceCode,
          )
        }

        def run(onlyWarning: Boolean, traverser: WartTraverser): Unit = {
          val universe: WartUniverse.Aux[q.type] =
            new WartUniverse(onlyWarning = onlyWarning, logLevel = LogLevel.Debug) {
              override type Q = q.type
              override val quotes: q.type = q
              override def onError(msg: String, pos: Position): Unit = {
                errors += Diagnostic(
                  message = msg,
                  wart = traverser.fullName,
                  position = convertPos(pos),
                )
                if (outputReporter) {
                  super.onError(msg = msg, pos = pos)
                }
              }
              override def onWarn(msg: String, pos: Position): Unit = {
                warnings += Diagnostic(
                  message = msg,
                  wart = traverser.fullName,
                  position = convertPos(pos),
                )
                if (outputReporter) {
                  super.onWarn(msg = msg, pos = pos)
                }
              }
            }

          val treeTraverser = traverser.apply(universe)
          tastys.foreach { tasty =>
            val tree = tasty.ast
            if (exclude.exists(tree.pos.sourceFile.path startsWith _)) {
              // skip
            } else {
              treeTraverser.traverseTree(tree)(tree.symbol)
            }
          }
        }

        errorTraversers.foreach(t => run(onlyWarning = false, traverser = t))
        warningTraversers.foreach(t => run(onlyWarning = true, traverser = t))
      }
    }
    TastyInspector.inspectAllTastyFiles(
      tastyFiles = tastyFiles,
      jars = Nil,
      dependenciesClasspath = dependenciesClasspath,
    )(inspector)

    implicit val ord: Ordering[Diagnostic] = Ordering.by { a =>
      val p = a.position
      (p.path, p.start, p.end, a.message)
    }

    InspectResult(
      errors = errors.result().sorted,
      warnings = warnings.result().sorted
    )
  }
}
