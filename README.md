# sbt-cmake
Sbt plugin for projects with [CMake](http://www.cmake.org/) sources

Automatically compiles CMake project from the _main/cpp_ folder of your project.

## Install

```scala
resolvers += Resolver.url("sbt-plugins", url("http://dl.bintray.com/volodymyr/sbt-plugins/"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.github.vorlov" % "sbt-cmake" % "0.1.0")
```

## Usage

Your CMake project will be compiled automatically before _compile_ step. To compile only your CMake project use _cmakeCompile_ task.
