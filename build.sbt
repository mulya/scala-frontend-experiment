// shadow sbt-scalajs' crossProject and CrossType from Scala.js 0.6.x
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport.dockerBaseImage
import sbtcrossproject.CrossPlugin.autoImport.{CrossType, crossProject}
import sbt.Keys.libraryDependencies

val akkaHttpVersion = "10.1.11"
val akkaStreamVersion = "2.6.1"

val sharedSettings = Seq(
  name := "tags",
  organization := "ru.mulya",
  version := "0.1-SNAPSHOT",
  scalaVersion := "2.12.10",
  resolvers += Classpaths.typesafeReleases
)

lazy val modules =
// select supported platforms
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Full)
    .settings(sharedSettings)
    .jsSettings(
      jsDependencies ++= Seq(
        "org.webjars" % "jquery" % "3.4.1" / "jquery.js" minified "jquery.min.js",
        "org.webjars" % "bootstrap" % "4.4.1" / "bootstrap.js" minified "bootstrap.min.js" dependsOn "jquery.js"
      ),
      libraryDependencies ++= Seq(
        "com.github.karasiq" %%% "scalajs-bootstrap-v4" % "2.3.5",
        "org.scala-js" %%% "scalajs-dom" % "0.9.2",
        "org.querki" %%% "jquery-facade" % "1.2",
        "com.lihaoyi" %% "scalatags" % "0.8.2"
      ),

      artifactPath in Compile in fastOptJS := (crossTarget in fastOptJS).value / ((moduleName in fastOptJS).value + ".js"),
      //      artifactPath in Compile in fullOptJS := (crossTarget in fullOptJS).value / ((moduleName in fullOptJS).value + ".js"),

      skip in packageJSDependencies := false,
      scalaJSUseMainModuleInitializer := true
    )
    .jvmSettings(
      libraryDependencies ++= Seq(
        "com.lihaoyi" %% "scalatags" % "0.8.2",
        "org.flywaydb" % "flyway-core" % "6.1.2",
        "mysql" % "mysql-connector-java" % "8.0.18",
        "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
        "com.typesafe.akka" %% "akka-stream" % akkaStreamVersion,
        "com.lightbend.akka" %% "akka-stream-alpakka-slick" % "1.1.2",
        "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
        "com.typesafe.akka" %% "akka-slf4j" % akkaStreamVersion,
        "ch.qos.logback" % "logback-classic" % "1.2.3",
        "com.typesafe.akka" %% "akka-http-caching" % akkaHttpVersion,
        "com.typesafe.akka" %% "akka-stream-testkit" % akkaStreamVersion % "test",
        "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % "test",
        "org.scalatest" %% "scalatest" % "3.1.0" % "test"
      ),
      mainClass in Compile := Some("ru.mulya.scala.frontend.experiment.App"),
      dockerBaseImage := "openjdk:jre-alpine",
      javaOptions in Universal ++= Seq(
        "-Dconfig.override_with_env_vars=true"
      )
    ).enablePlugins(AshScriptPlugin, DockerPlugin, JavaAppPackaging)

lazy val js = modules.js

def addJavaScriptToServerResources() = {
  (resources in Compile) += (fastOptJS in(js, Compile)).value.data
  //  (resources in Compile) += (fullOptJS in(js, Compile)).value.data
}

def addJSDependenciesToServerResources() = {
  (resources in Compile) += (packageMinifiedJSDependencies in(js, Compile)).value
  (resources in Compile) += (packageJSDependencies in(js, Compile)).value
}

def addSourceMapToServerResources() = {
  (resources in Compile) += Def.task {
    val fastOptJSFile = (fastOptJS in(js, Compile)).value.data
    fastOptJSFile.getParentFile / (fastOptJSFile.getName + ".map")
  }.value
  //  (resources in Compile) += Def.task {
  //    val fullOptJSFile = (fullOptJS in(js, Compile)).value.data
  //    fullOptJSFile.getParentFile / (fullOptJSFile.getName + ".map")
  //  }.value
}

lazy val jvm = modules.jvm.settings(
  addJavaScriptToServerResources(),
  addJSDependenciesToServerResources(),
  addSourceMapToServerResources()
)

scalacOptions += "-P:scalajs:sjsDefinedByDefault"