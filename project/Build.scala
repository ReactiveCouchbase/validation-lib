import sbt._
import Keys._

object ApplicationBuild extends Build {

  val appName         = "validation-lib"
  val appVersion      = "1.0-SNAPSHOT"

  val local: Project.Initialize[Option[sbt.Resolver]] = version { (version: String) =>
    val localPublishRepo = "./repository"
    if(version.trim.endsWith("SNAPSHOT"))
      Some(Resolver.file("snapshots", new File(localPublishRepo + "/snapshots")))
    else Some(Resolver.file("releases", new File(localPublishRepo + "/releases")))
  }

  lazy val baseSettings = Defaults.defaultSettings ++ Seq(
    autoScalaLibrary := false,
    crossPaths := false
  )

  lazy val root = Project("root", base = file("."))
    .settings(baseSettings: _*)
    .settings(
      publishLocal := {},
      publish := {}
    ).aggregate(
      validationLib
    )

  lazy val validationLib = Project(appName, base = file("validation-lib"))
    .settings(baseSettings: _*)
    .settings(
      resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
      libraryDependencies += "org.reactivecouchbase" % "common-lib" % "1.0-SNAPSHOT",
      libraryDependencies += "org.reactivecouchbase" % "json-lib" % "1.0-SNAPSHOT",
      libraryDependencies += "joda-time" % "joda-time" % "2.9.2",
      libraryDependencies += "org.assertj" % "assertj-core" % "3.3.0",
      libraryDependencies += "junit" % "junit" % "4.11" % "test",
      libraryDependencies += "com.novocode" % "junit-interface" % "0.9" % "test",
      organization := "org.reactivecouchbase",
      version := appVersion,
      publishTo <<= local,
      publishMavenStyle := true,
      publishArtifact in Test := false,
      pomIncludeRepository := { _ => false },
      publishArtifact in (Compile, packageDoc) := false,
      publishArtifact in packageDoc := false
      //testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-v")
    )
}
