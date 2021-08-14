import Settings.dockerSettings

lazy val akkaVersion              = "2.6.15"
lazy val akkaHttpVersion          = "10.2.6"
lazy val akkaHttpJsonVersion      = "1.37.0"
lazy val akkaPersistenceJdbc      = "5.0.1"
lazy val catsVersion              = "2.6.1"
lazy val circeVersion             = "0.14.1"
lazy val slickVersion             = "3.3.3"
lazy val postgresqlVersion        = "42.2.23"
lazy val tapirAkkaHttpVersion     = "0.19.0-M4"
lazy val logbackClassicVersion    = "1.2.5"
lazy val scalaCacheEhCacheVersion = "0.28.0"
lazy val scalaTestVersion         = "3.2.9"

ThisBuild / organization := "com.igobrilhante"
ThisBuild / scalaVersion := "2.13.4"
ThisBuild / version := "0.1.0"
ThisBuild / name := "github-challenge"

Global / onChangedBuildSource := IgnoreSourceChanges

lazy val root = (project in file(".")).aggregate(core, akkaProject)

lazy val core = (project in file("core"))
  .settings(
    version := "0.1.0"
  )
  .settings(
    libraryDependencies ++= Seq(
      // cats
      "org.typelevel" %% "cats-core" % catsVersion,
      // others
      "com.github.cb372" %% "scalacache-ehcache" % scalaCacheEhCacheVersion,
      // logs
      "ch.qos.logback" % "logback-classic" % logbackClassicVersion,
      // tests
      "org.scalatest" %% "scalatest" % scalaTestVersion % Test
    )
  )

lazy val adapters = (project in file("adapters"))
  .settings(
    libraryDependencies ++= Seq(
      // circle
      "io.circe" %% "circe-core"    % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser"  % circeVersion
    )
  )
  .dependsOn(core)

lazy val akkaProject = (project in file("akka-project"))
  .enablePlugins(JavaAppPackaging, DockerPlugin)
  .settings(
    libraryDependencies ++= Seq(
      // cats
      "org.typelevel" %% "cats-core" % catsVersion,
      // akka http
      "com.typesafe.akka" %% "akka-http"         % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-caching" % akkaHttpVersion,
      "de.heikoseeberger" %% "akka-http-circe"   % akkaHttpJsonVersion,
      // akka
      "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
      "com.typesafe.akka" %% "akka-stream"      % akkaVersion,
      // logs
      "ch.qos.logback" % "logback-classic" % logbackClassicVersion,
      // circle
      "io.circe" %% "circe-core"    % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser"  % circeVersion,
      // others
      "com.github.cb372" %% "scalacache-ehcache" % scalaCacheEhCacheVersion,
      // tests
      "com.typesafe.akka" %% "akka-http-testkit"        % akkaHttpVersion  % Test,
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion      % Test,
      "org.scalatest"     %% "scalatest"                % scalaTestVersion % Test
    )
  )
  .settings(dockerSettings: _*)
  .dependsOn(core, adapters)

lazy val zioProject = (project in file("zio-project"))
  .enablePlugins(JavaAppPackaging, DockerPlugin)
  .settings(
    libraryDependencies ++= Seq(
      // cats
      "org.typelevel" %% "cats-core"   % catsVersion,
      // zio
      "dev.zio"       %% "zio"         % "1.0.10",
      "dev.zio"       %% "zio-streams" % "1.0.10",
      //
      // zio http
      "io.d11"        %% "zhttp"      % "1.0.0.0-RC17",
      //
      "com.softwaremill.sttp.client3" %% "core"                          % "3.3.13",
      "com.softwaremill.sttp.client3" %% "async-http-client-backend-zio" % "3.3.13",
      "com.softwaremill.sttp.client3" %% "circe"                         % "3.3.13",
      // akka http
      "com.typesafe.akka" %% "akka-http"         % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-caching" % akkaHttpVersion,
      "de.heikoseeberger" %% "akka-http-circe"   % akkaHttpJsonVersion,
      // akka
      "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
      "com.typesafe.akka" %% "akka-stream"      % akkaVersion,
      // logs
      "ch.qos.logback" % "logback-classic" % logbackClassicVersion,
      // circle
      "io.circe" %% "circe-core"    % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser"  % circeVersion,
      // others
      "com.github.cb372" %% "scalacache-ehcache" % scalaCacheEhCacheVersion,
      // tests
      "io.d11"        %% "zhttp-test" % "1.0.0.0-RC17"      % Test,
      "org.scalatest" %% "scalatest"  % scalaTestVersion % Test
    )
  )
  .settings(dockerSettings: _*)
  .dependsOn(core, adapters)
