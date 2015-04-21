package com.github.vorlov

import sbt.KeyRanks._
import sbt.Keys._
import sbt._

import scala.collection.mutable.ListBuffer

object SbtCmake extends AutoPlugin {

  object autoImport {

    val sbtCmake = TaskKey[String]("sbt-cmake", "sbt-cmake is an interface for the sbt-cmake package service")

    val cmakeSource = SettingKey[File]("cmake-configuration-file", "Location of CMake configuration file.", ASetting)

    lazy val cmakeInstall = TaskKey[Unit]("install", "Compile CMake project")

    lazy val cmakeCompile = TaskKey[Unit]("sbt-cmake compile", "Compile CMake project")

    lazy val sbtCmakeSettings: Seq[Def.Setting[_]] = Seq(
      cmakeInstall in sbtCmake := cmakeInstallImpl(cmakeSource.value, target.value, streams.value.log),
      cmakeCompile in sbtCmake := cmakeCompileImpl(cmakeSource.value, target.value, streams.value.log),
      cmakeSource := (sourceDirectory in Compile).value / "native",
      compile <<= compile in Compile dependsOn (cmakeCompile in sbtCmake),
      initialize <<= (target) { target =>
        System.setProperty("java.library.path", target.getAbsolutePath)
      }
    )

  }

  def cmakeCompileImpl(source: File, buildFolder: File, log: Logger) = execute(Seq("make"), source, buildFolder, log)

  def cmakeInstallImpl(source: File, buildFolder: File, log: Logger) = execute(Seq("make", "install"), source, buildFolder, log)

  def execute(cmd: Seq[String], source: File, buildFolder: File, log: Logger ): Unit = {

    val bufferedLogger = new BufferedLogger(log.asInstanceOf[AbstractLogger])

    val cmakeCmd = findCMake.getOrElse{
      throw new Exception("Please install CMake or add it to the PATH.")
    }

    val exitCode = sbt.Process(cmakeCmd.getAbsolutePath :: source.getAbsolutePath :: Nil, buildFolder) #&& sbt.Process(cmd, buildFolder) ! bufferedLogger

    exitCode match {
      case 0 => bufferedLogger.playAsSuccess
      case errorCode => {
        bufferedLogger.playAsFailure
        throw new Exception(s"Could not execute [${cmd.mkString(" ")}], error code is [$errorCode]")
      }
    }

  }

  def findCMake: Option[File] = {
    Option("which cmake" !!).map(s => s.replaceAll("[^\\w/\\\\\\t _\\-]", "")).map(s => s.replaceAll("\n", "")).map( path => new File(path)).filter(_.exists)
  }

  import autoImport._
  override def requires = sbt.plugins.JvmPlugin

  override def trigger = allRequirements

  override val projectSettings =
      inConfig(Compile)(sbtCmakeSettings)

}

class BufferedLogger(logger: AbstractLogger) extends BasicLogger {

  private[this] val buffer = new ListBuffer[String]

  override def log(level: Level.Value, message: => String): Unit = {
    buffer += message
  }

  override def control(event: ControlEvent.Value, message: => String): Unit = logger.control(event, message)

  override def logAll(events: Seq[LogEvent]): Unit = logger.logAll(events)

  override def success(message: => String): Unit = logger.success(message)

  override def trace(t: => Throwable): Unit = logger.trace(t)

  def playAsSuccess = play(Level.Info)

  def playAsFailure = play(Level.Error)

  private def play(level: Level.Value) = logAll(buffer.map(new Log(level, _)))

}
