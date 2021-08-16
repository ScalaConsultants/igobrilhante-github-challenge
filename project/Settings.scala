import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.docker.Cmd

object Settings {

  val dockerSettings = Seq(
    dockerBaseImage := "adoptopenjdk/openjdk11:x86_64-debianslim-jdk-11.0.10_9-slim",
    dockerRepository := Some("igobrilhante"),
    dockerExposedPorts ++= Seq(8080),
    dockerCommands := dockerCommands.value.flatMap {
      case cmd @ Cmd("FROM", _) =>
        List(
          cmd,
          Cmd("RUN", "apt update && apt -y install bash wget")
        )
      case other => List(other)
    }
  )

}
