name := "AbhijaySandeepFinalProjectCS441"

version := "1.0"

//scalaVersion := "2.10.0"
scalaVersion := "2.11.0"

//mainClass in Compile := Some("chordMainMethod")

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-api" % "1.7.5",
  "org.slf4j" % "slf4j-simple" % "1.7.5",
  "com.typesafe.akka" % "akka-actor_2.11" % "2.4.12",
  "com.typesafe.akka" % "akka-http-experimental_2.11" % "2.4.11",
  "org.scalatest"  %% "scalatest"   % "2.2.4" % Test, //note 2.2.2 works too
  "org.mongodb" %% "casbah" % "3.1.1",
  "org.mongodb.scala" %% "mongo-scala-driver" % "1.2.1",
  "commons-io" % "commons-io" % "2.5"
)

scalacOptions += "-deprecation"
