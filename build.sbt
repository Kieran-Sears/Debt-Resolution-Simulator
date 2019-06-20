name := "Simulations"
version := "0.1"
scalaVersion := "2.11.12"
scalacOptions += "-Ypartial-unification"

val sparkVersion = "2.3.1"
val akkaVersion = "2.5.16"
val akkaHttpVersion = "10.1.5"
val slf4jVersion = "1.7.25"
val tensorflowVersion = "0.3.0"
val log4jVersion = "2.4.1"
val doobieVersion   = "0.7.0"

val akkaDependencies = Seq(
  "com.typesafe.akka" %% "akka-actor"           % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit"         % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence"     % akkaVersion,
  "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-testkit"    % akkaHttpVersion
)

val sparkDependencies = Seq(
  "org.apache.spark" %% "spark-core"      % sparkVersion,
  "org.apache.spark" %% "spark-streaming" % sparkVersion,
  "org.apache.spark" %% "spark-sql"       % sparkVersion,
  "org.apache.spark" %% "spark-mllib"     % sparkVersion,
  "org.apache.spark" %% "spark-hive"      % sparkVersion
)

val doobieDependencies = Seq(
  "org.tpolecat" %% "doobie-core"               % doobieVersion,
  "org.tpolecat" %% "doobie-postgres"           % doobieVersion,
  "org.tpolecat" %% "doobie-specs2"             % doobieVersion
)

val otherDependencies = Seq(
  "org.scalatest" %% "scalatest" % "3.0.5",
  "com.intel.analytics.bigdl" % "bigdl-SPARK_2.3" % "0.7.0",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.4.0"
 // "org.platanios" %% "tensorflow" % tensorflowVersion classifier "linux-cpu-x86_64"
)

libraryDependencies ++= (akkaDependencies ++ sparkDependencies ++ doobieDependencies ++ otherDependencies)//.map(_.exclude(org = "org.slf4j", "slf4j-log4j12"))

lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(
    Defaults.itSettings,
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "it,test"
  )

fork := true

javaOptions in Test += s"-Dconfig.file=${sourceDirectory.value}/test/resources/application.test.conf"

