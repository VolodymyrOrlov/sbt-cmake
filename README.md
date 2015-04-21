# sbt-cmake
Sbt plugin for projects with [CMake](http://www.cmake.org/) sources

Automatically compiles CMake project from the *cmakeSource*, which defaults to _main/native_ folder within your project.

## Install

```scala
resolvers += Resolver.url("vorlov sbt-plugins", url("http://dl.bintray.com/vorlov/sbt-plugins/"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.github.vorlov" % "sbt-cmake" % "0.1.0")
```

## Usage

On compile, the *sbt-cmake::compile* task will be run. It will run _cmake_ followed by _make_. Compiled native binaries 
and libraries will end up in _target_. To install those run *sbt-cmake::install* task.
