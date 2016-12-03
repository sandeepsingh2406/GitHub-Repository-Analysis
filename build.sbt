name := "AbhijaySandeepFinalProjectCS441"

version := "1.0"

scalaVersion := "2.11.8"

//mainClass in Compile := Some("chordMainMethod")

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-api" % "1.7.5",
  "org.slf4j" % "slf4j-simple" % "1.7.5",
  "com.typesafe.akka" % "akka-actor_2.11" % "2.4.12",
  "com.typesafe.akka" % "akka-http-experimental_2.11" % "2.4.11",
  "org.scalatest"  %% "scalatest"   % "2.2.4" % Test, //note 2.2.2 works too
  "org.mongodb" %% "casbah" % "3.1.1",
  "org.mongodb.scala" %% "mongo-scala-driver" % "1.2.1",
  "mysql" % "mysql-connector-java" % "5.1.40",
//  "mysql" % "mysql-connector-java" % "6.0.2",
  "commons-io" % "commons-io" % "2.5"
)
resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "com.typesafe.play" %% "play-json" % "2.3.4"
libraryDependencies += "commons-codec" % "commons-codec" % "1.9"


scalacOptions += "-deprecation"

resolvers += "jgit-repo" at "http://download.eclipse.org/jgit/maven"

libraryDependencies += "org.eclipse.jgit" % "org.eclipse.jgit" % "[2.1,)"
libraryDependencies += "commons-io" % "commons-io" % "2.5"

