import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "xChessTemp"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      // Add your project dependencies here,
        "org.mongodb" %% "casbah" % "2.4.1",
        "com.novus" %% "salat-core" % "1.9.2-SNAPSHOT"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
      // Add your own project settings here
	   resolvers += "typesafe" at "http://repo.typesafe.com/typesafe/releases/"

    )

}
