name := "Spark.RestConsumer"

version := "1.0"

scalaVersion := "2.11.8"

mainClass := Some("CurrencyJob")

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", "services", _ @ _*) => MergeStrategy.first
  case PathList("META-INF", _ @ _*) => MergeStrategy.discard
  case _ => MergeStrategy.first
}

assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "2.4.1" % "provided",
  "org.apache.spark" %% "spark-sql" % "2.4.1" % "provided",
  "org.apache.spark" %% "spark-streaming" % "2.4.1" % "provided",

  "org.apache.spark" %% "spark-sql-kafka-0-10" % "2.4.1" exclude("org.slf4j", "slf4j-api"),
  "org.apache.spark" %% "spark-streaming-kafka-0-10" % "2.4.1" exclude("org.slf4j", "slf4j-api"),

  "org.scalaj" %% "scalaj-http" % "2.4.2",
  "net.liftweb" %% "lift-webkit" % "3.3.0",
  "com.typesafe" % "config" % "1.4.0",
  "org.scalactic" %% "scalactic" % "3.1.0",

  "org.scalatest" %% "scalatest" % "3.1.0" % "test"
)