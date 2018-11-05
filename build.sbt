name := "Simulations"

version := "0.1"

scalaVersion := "2.12.6"

val akkaVersion = "2.5.16"
val akkaHttpVersion = "10.1.3"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor"       % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit"     % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
  "com.typesafe.akka" %% "akka-http"        % akkaHttpVersion,
  "org.iq80.leveldb"              % "leveldb"          % "0.7",
  "org.fusesource.leveldbjni"     % "leveldbjni-all"   % "1.8",
  "org.scalatest" %% "scalatest"  % "3.0.5"            % "test",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.5"
 
)

javaOptions in Test += s"-Dconfig.file=${sourceDirectory.value}/test/resources/application.test.conf"

fork in Test := true