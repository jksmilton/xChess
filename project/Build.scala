import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "xChessTemp"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      // Add your project dependencies here,
        "postgresql" % "postgresql" % "8.4-702.jdbc4",
        "com.typesafe" %% "play-plugins-mailer" % "2.0.4"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
      
    )

}
