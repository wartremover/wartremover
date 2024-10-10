package wartremover

import sbt.*
import sbt.Keys.*
import xsbti.FileConverter
import wartremover.WartRemover.CompileResult
import wartremover.InspectWart.Type
import java.lang.reflect.Modifier
import java.net.URLClassLoader
import scala.annotation.tailrec
import scala.util.Using

private[wartremover] trait WartRemoverCompat { self: WartRemover.type =>
  import self.autoImport.*

  private[wartremover] def convertToFile(x: File, fileConverter: FileConverter): File =
    x

  private[wartremover] def wartremoverTaskSetting(x: Configuration): Seq[Def.Setting[?]] = {
    x / wartremoverTask := Def.inputTaskDyn {
      val parsed0 = InspectArgsParser.get(file(".").getAbsoluteFile.toPath).parsed
      val parsed = InspectArgs.from(parsed0.collect { case x: InspectArg.Wart =>
        x
      })
      Def.taskDyn {
        val errResult = compileWartFromSources(parsed(Type.Err)).value
        val warnResult = compileWartFromSources(parsed(Type.Warn)).value
        createInspectTask(
          x = x,
          warningWartNames = parsed(Type.Warn).warts ++ warnResult.wartNames,
          errorWartNames = parsed(Type.Err).warts ++ errResult.wartNames,
          jarFiles = errResult.jarBinary.toSeq ++ warnResult.jarBinary.toSeq,
        )
      }
    }.evaluated
  }

  private[this] def compileWartFromSources(inspectArg: InspectArgs): Def.Initialize[Task[CompileResult]] = {
    Def.taskDyn {
      val log = streams.value.log
      val wartremoverJars = Def.taskDyn {
        val wartremoverCross = wartremoverCrossVersion.value
        getJarFiles(
          "org.wartremover" %% "wartremover" % Wart.PluginVersion cross wartremoverCross
        )
      }.value
      if (inspectArg.sources.nonEmpty) {
        val bytes: Seq[Byte] = getJar(inspectArg.sources.toSet).value
        IO.withTemporaryDirectory { tmpDir =>
          val compiledJar = tmpDir / "wartremover" / "warts.jar"
          IO.write(compiledJar, bytes.toArray)
          log.debug(s"compiled jar = $compiledJar")
          val classes = getAllClassNamesInJar(compiledJar)
          if (classes.isEmpty) {
            log.error("not found compiled classes")
            Def.task(CompileResult.empty)
          } else {
            log.info(s"compiled classes = ${classes.mkString(" ")}")

            val xs = Using.resource(
              new URLClassLoader(
                (compiledJar.toURI.toURL +: wartremoverJars.map(_.toURI.toURL)).toArray
              )
            ) { loader =>
              classes.filter { className =>
                val clazz = Class.forName(className, false, loader)
                val traverserName = "org.wartremover.WartTraverser"

                @tailrec
                def loop(c: Class[?]): Boolean = {
                  if (c == null) {
                    false
                  } else if (c.getName == traverserName) {
                    true
                  } else {
                    loop(c.getSuperclass)
                  }
                }

                if (Modifier.isAbstract(clazz.getModifiers)) {
                  false
                } else {
                  loop(clazz)
                }
              }.map { s =>
                if (s.endsWith("$")) s.dropRight(1) else s
              }
            }

            Def.task(
              CompileResult(jarBinary = Some(bytes), wartNames = xs.map(Wart.custom))
            )
          }
        }
      } else {
        Def.task(
          CompileResult.empty
        )
      }
    }
  }

}
