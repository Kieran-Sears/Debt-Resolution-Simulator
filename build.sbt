name := "Simulations"

version := "0.1"

scalaVersion := "2.12.6"

val akkaVersion = "2.5.16"
val akkaHttpVersion = "10.1.3"
val slf4jVersion = "1.7.25"

lazy val scalatest = "org.scalatest" %% "scalatest" % "3.0.5"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor"       % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit"     % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
  "com.typesafe.akka" %% "akka-http"        % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % "10.1.5",
  "org.iq80.leveldb"              % "leveldb"          % "0.7",
  "org.fusesource.leveldbjni"     % "leveldbjni-all"   % "1.8",
  scalatest,
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.5",
  "io.spray" % "spray-httpx" % "1.3.1"
)

val sparkVersion = "2.4.0"
libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-streaming" % sparkVersion,
  "com.datastax.spark" %% "spark-cassandra-connector" % "2.0.6"
  // "org.apache.spark" %% "spark-streaming-kafka-0-10" % "2.0.0",
  // "com.datastax.spark" %% "spark-cassandra-connector" % "2.4.0"
)

lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(
    Defaults.itSettings,
    libraryDependencies += scalatest % "it,test"
    // other settings here
  )

fork := true

javaOptions in Test += s"-Dconfig.file=${sourceDirectory.value}/test/resources/application.test.conf"

