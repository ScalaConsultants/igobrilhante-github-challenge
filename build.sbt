import Settings.dockerSettings

lazy val akkaVersion = "2.6.15"
lazy val akkaHttpVersion = "10.2.6"
lazy val akkaHttpJsonVersion = "1.37.0"
lazy val akkaPersistenceJdbc = "5.0.1"
lazy val catsVersion = "2.6.1"
lazy val circeVersion = "0.14.1"
lazy val slickVersion = "3.3.3"
lazy val postgresqlVersion = "42.2.23"
lazy val tapirAkkaHttpVersion = "0.19.0-M4"
lazy val logbackClassicVersion = "1.2.5"
lazy val scalaCacheEhCacheVersion = "0.28.0"
lazy val scalaTestVersion = "3.2.9"

Global / onChangedBuildSource := IgnoreSourceChanges

lazy val root = (project in file("."))
  .enablePlugins(JavaAppPackaging, DockerPlugin)
  .settings(
    organization := "com.igobrilhante",
    scalaVersion := "2.13.4",
    version := "0.1.0",
    name := "github-challenge",
    libraryDependencies ++= Seq(
      // cats
      "org.typelevel" %% "cats-core" % catsVersion,
      // akka http
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-caching" % akkaHttpVersion,
      "de.heikoseeberger" %% "akka-http-circe" % akkaHttpJsonVersion,
      // akka
      "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,
      // logs
      "ch.qos.logback" % "logback-classic" % logbackClassicVersion,
      // circle
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      // others
      "com.github.cb372" %% "scalacache-ehcache" % scalaCacheEhCacheVersion,
      // tests
      "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
      "org.scalatest" %% "scalatest" % scalaTestVersion % Test
    )
  )
  .settings(dockerSettings: _*)
