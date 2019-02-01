name := "Simulations"

version := "0.1"

scalaVersion := "2.11.12"

val sparkVersion = "2.3.1"
val akkaVersion = "2.5.16"
val akkaHttpVersion = "10.1.5"
val slf4jVersion = "1.7.25"
val circeVersion = "0.7.0"
val tensorflowVersion = "0.3.0"
val log4jVersion = "2.4.1"

val akkaDependencies = Seq(
  "com.typesafe.akka" %% "akka-actor"           % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit"         % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence"     % akkaVersion,
  "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-testkit"    % akkaHttpVersion
)

val sparkDependencies = Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-streaming" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "org.apache.spark" %% "spark-mllib" % sparkVersion,
  "org.apache.spark" %% "spark-hive" % sparkVersion
)

val circeDependencies = Seq(
  "io.circe"  %% "circe-core"     % circeVersion,
  "io.circe"  %% "circe-generic"  % circeVersion,
  "io.circe"  %% "circe-parser"   % circeVersion
)

val otherDependencies = Seq(
  "org.scalatest" %% "scalatest" % "3.0.5",
  "com.intel.analytics.bigdl" % "bigdl-SPARK_2.3" % "0.7.0",
  "org.iq80.leveldb"              % "leveldb"          % "0.7",
  "org.fusesource.leveldbjni"     % "leveldbjni-all"   % "1.8",
  "com.datastax.spark" %% "spark-cassandra-connector" % "2.0.6"
 // "org.platanios" %% "tensorflow" % tensorflowVersion classifier "linux-cpu-x86_64"
)

libraryDependencies ++= (akkaDependencies ++ sparkDependencies ++ circeDependencies ++ otherDependencies).map(_.exclude(org = "org.slf4j", "slf4j-log4j12"))

lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(
    Defaults.itSettings,
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "it,test"
    // other settings here
  )

fork := true

javaOptions in Test += s"-Dconfig.file=${sourceDirectory.value}/test/resources/application.test.conf"

