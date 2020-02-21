organization := "ru.mulya"

name := "scala-frontend-experiment"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.13.1"

val akkaHttpVersion = "10.1.11"
val akkaStreamVersion = "2.6.1"

resolvers += Classpaths.typesafeReleases

libraryDependencies ++= Seq(
  "com.lihaoyi" %% "scalatags" % "0.8.2",
  "org.webjars" % "popper.js" % "1.14.3",
  "org.webjars" % "jquery" % "3.4.1",
  "org.webjars" % "bootstrap" % "4.4.1",
  "org.flywaydb" % "flyway-core" % "6.1.2",
  "mysql" % "mysql-connector-java" % "8.0.18",
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaStreamVersion,
  "com.lightbend.akka" %% "akka-stream-alpakka-slick" % "1.1.2",
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaStreamVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.scalatest" %% "scalatest" % "3.1.0" % "test",
  "com.typesafe.akka" %% "akka-http-caching" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaStreamVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion
)

mainClass in Compile := Some("ru.mulya.App")
dockerBaseImage := "openjdk:jre-alpine"
javaOptions in Universal ++= Seq(
  "-Dconfig.override_with_env_vars=true"
)


enablePlugins(AshScriptPlugin)
enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)