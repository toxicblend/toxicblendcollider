lazy val toxicblendcollider= project.in( file(".")).aggregate(Projects.jbulletd).dependsOn(Projects.jbulletd)

version := "0.1"

scalaVersion := "2.11.5"

libraryDependencies ++= Seq(
	"org.scalatest" % "scalatest_2.11" % "2.2.4" % "test",
	"com.typesafe.akka" %% "akka-actor" % "2.3.9",
	"com.typesafe.akka" %% "akka-remote" % "2.3.9"
)

publishMavenStyle := true

publishArtifact in Test := false

publishTo := Some(Resolver.file("file",  baseDirectory.value / "dist" ) )

net.virtualvoid.sbt.graph.Plugin.graphSettings
