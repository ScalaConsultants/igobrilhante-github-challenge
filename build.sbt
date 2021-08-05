
lazy val akkaVersion = "2.6.15"
lazy val akkaHttpVersion = "10.2.5"
lazy val akkaHttpJsonVersion = "1.37.0"
lazy val akkaPersistenceJdbc = "5.0.1"
lazy val circeVersion = "0.14.1"
lazy val slickVersion = "3.3.3"
lazy val postgresqlVersion = "42.2.23"
lazy val tapirAkkaHttpVersion = "0.19.0-M4"
lazy val logbackClassicVersion = "1.2.5"

lazy val scalaTestVersion = "3.2.9"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "com.igobrilhante",
      scalaVersion    := "2.13.4"
    )),
    name := "github-challenge",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"                % akkaHttpVersion,
      "de.heikoseeberger" %% "akka-http-circe"          % akkaHttpJsonVersion,
      "com.typesafe.akka" %% "akka-actor-typed"         % akkaVersion,
      "com.typesafe.akka" %% "akka-stream"              % akkaVersion,
      "ch.qos.logback"    %  "logback-classic"          % logbackClassicVersion,

      "io.circe"          %% "circe-core"               % circeVersion,
      "io.circe"          %% "circe-generic"            % circeVersion,
      "io.circe"          %% "circe-parser"             % circeVersion,

      "com.typesafe.akka" %% "akka-http-testkit"        % akkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion     % Test,
      "org.scalatest"     %% "scalatest"                % "3.1.4"         % Test
    )
  )
