package com.github.sbt

import sbt.KeyRanks._
import sbt.Keys._
import sbt._

object SbtCmake extends AutoPlugin {

  object autoImport {

    val cmakeConfigurationFile = SettingKey[File]("cmake-configuration-file", "Location of CMake configuration file.", ASetting)

    lazy val cmakeCompile = taskKey[Unit]("Compile CMake project")

    lazy val baseObfuscateSettings: Seq[Def.Setting[_]] = Seq(
      cmakeCompile := {
        val exitCode = sbt.Process("cmake" :: cmakeConfigurationFile.value.getAbsolutePath :: Nil, target.value) #&& sbt.Process("make" :: Nil, target.value) !

        if( 0 != exitCode) throw new Exception("Could not compile AdMatch library")
      },
      cmakeConfigurationFile := (sourceDirectory in Compile).value / "cpp",
      compile <<= compile in Compile dependsOn cmakeCompile,
      initialize ~= { _ =>
        System.setProperty("java.library.path", "target/")
      }
    )

  }

  import autoImport._
  override def requires = sbt.plugins.JvmPlugin

  // This plugin is automatically enabled for projects which are JvmPlugin.
  override def trigger = allRequirements

  // a group of settings that are automatically added to projects.
  override val projectSettings =
      inConfig(Compile)(baseObfuscateSettings)

}
