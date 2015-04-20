package com.github.vorlov

import sbt.KeyRanks._
import sbt.Keys._
import sbt._

import scala.collection.mutable.ListBuffer

object SbtCmake extends AutoPlugin {

  object autoImport {

    val cmakeConfigurationFile = SettingKey[File]("cmake-configuration-file", "Location of CMake configuration file.", ASetting)

    lazy val cmakeCompile = TaskKey[Unit]("cmake-compile", "Compile CMake project")

    lazy val cmakeInstall = TaskKey[Unit]("cmake-install","Compile CMake project")

    lazy val baseObfuscateSettings: Seq[Def.Setting[_]] = Seq(
      cmakeCompile := cmakeCompileImpl(cmakeConfigurationFile.value, target.value, new BufferedStringLogger(streams.value.log.asInstanceOf[AbstractLogger])),
      cmakeInstall := cmakeInstallImpl(cmakeConfigurationFile.value, target.value, new BufferedStringLogger(streams.value.log.asInstanceOf[AbstractLogger])),
      cmakeConfigurationFile := (sourceDirectory in Compile).value / "cpp",
      compile <<= compile in Compile dependsOn cmakeCompile,
      initialize ~= { _ =>
        System.setProperty("java.library.path", "target/")
      }
    )

  }

  def cmakeCompileImpl(source: File, buildFolder: File, log: BufferedStringLogger) = execute(Seq("make"), source, buildFolder, log)

  def cmakeInstallImpl(source: File, buildFolder: File, log: BufferedStringLogger) = execute(Seq("make", "install"), source, buildFolder, log)

  def execute(cmd: Seq[String], source: File, buildFolder: File, log: BufferedStringLogger ): Unit = {

    val cmakeCmd = findCMake.getOrElse{
      throw new Exception("Please install CMake or add it to the PATH.")
    }

    println(cmakeCmd)

    val exitCode = sbt.Process(cmakeCmd.getAbsolutePath :: source.getAbsolutePath :: Nil, buildFolder) #&& sbt.Process(cmd, buildFolder) ! log

    if( 0 != exitCode) throw new Exception(log.lastMessage)

  }

  def findCMake: Option[File] = {
    println(Option("which cmake" !!).map(s => s.replaceAll("[^\\w/\\\\\\t _\\-]", "")).map(s => s.replaceAll("\n", "")))
    Option("which cmake" !!).map(s => s.replaceAll("[^\\w/\\\\\\t _\\-]", "")).map(s => s.replaceAll("\n", "")).map( path => new File(path)).filter(_.exists)
  }

  import autoImport._
  override def requires = sbt.plugins.JvmPlugin

  override def trigger = allRequirements

  override val projectSettings =
      inConfig(Compile)(baseObfuscateSettings)

}

class BufferedStringLogger(logger: AbstractLogger) extends BufferedLogger(logger) {

  private[this] var lastBufferedMessage: Option[String] = None

  override def log(level: Level.Value, message: => String): Unit = {
    lastBufferedMessage = Some(message)
    super.log(level, message)
  }

  def lastMessage = lastBufferedMessage.getOrElse("")

}
