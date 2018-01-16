import com.typesafe.sbt.web.Import.{WebKeys, pipelineStages}

scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation") //, "-feature")
name in ThisBuild := "Scalagna"

val http4sVersion = "0.15.3"

val scalaV = "2.11.8"
val scalaTagsV = "0.6.1"
val upickleV = "0.4.3"


lazy val commonSettings = Seq(
  scalaVersion := scalaV,
  addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
  libraryDependencies ++= Seq(
    "com.lihaoyi" %%% "upickle" % upickleV,
    "com.lihaoyi" %%% "scalatags" % scalaTagsV,
    "org.slf4j" % "slf4j-simple" % "1.6.4",
    "org.scala-lang" % "scala-reflect" % scalaV
  )
)

lazy val scalamt = (crossProject.crossType(CrossType.Pure) in file("scalamt"))
  .settings(commonSettings)
  .jvmSettings(
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-blaze-server" % http4sVersion
    )
  )
  .jsSettings(
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.1"
    )
  )
  .jsConfigure(_.enablePlugins (ScalaJSPlugin))

lazy val scalamtJs = scalamt.js
lazy val scalamtJvm = scalamt.jvm

lazy val example = (crossProject.crossType(CrossType.Pure) in file("example"))
  .settings(commonSettings)
  .jvmSettings(
    WebKeys.packagePrefix in Assets := "static/",
    pipelineStages in Assets := Seq(scalaJSPipeline),
    // triggers scalaJSPipeline when using compile or continuous compilation
    compile in Compile <<= (compile in Compile) dependsOn scalaJSPipeline,
    managedClasspath in Runtime += (packageBin in Assets).value
  )
  .jsSettings(
    scalaJSUseMainModuleInitializer := false,
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.1"
    )
  )
  .jvmConfigure(_.dependsOn(scalamtJvm))
  .jvmConfigure(_.enablePlugins(SbtWeb, JavaAppPackaging))
  .jsConfigure(_.dependsOn(scalamtJs))
  .jsConfigure(_.enablePlugins(ScalaJSPlugin, ScalaJSWeb))

val exampleJs = example.js
val exampleJvm = example.jvm.settings(
  scalaJSProjects := Seq(exampleJs)
)

// loads the server project at sbt startup
onLoad in Global := (Command.process("project exampleJVM", _: State)) compose (onLoad in Global).value
