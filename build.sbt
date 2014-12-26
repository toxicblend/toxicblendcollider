lazy val toxicblendcollider= project.in( file(".")).aggregate(Projects.jbulletd).dependsOn(Projects.jbulletd)

version := "0.1"

scalaVersion := "2.11.4"

libraryDependencies ++= Seq(
	"org.scalatest" % "scalatest_2.11" % "2.2.1" % "test",
	"com.typesafe.akka" %% "akka-actor" % "2.3.8",
	"com.typesafe.akka" %% "akka-remote" % "2.3.8"
)

publishMavenStyle := true

publishArtifact in Test := false

publishTo := Some(Resolver.file("file",  baseDirectory.value / "dist" ) )

net.virtualvoid.sbt.graph.Plugin.graphSettings
