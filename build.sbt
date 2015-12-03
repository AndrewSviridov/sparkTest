import sbtassembly.Plugin._
import sbtassembly.Plugin.AssemblyKeys._


name := "sparkTest"

version := "1.0.3.5"

scalaVersion := "2.10.4"

resolvers ++= Seq(
  "Akka Repository" at "http://repo.akka.io/releases/",
  "Cloudera Repository"  at "http://repository.cloudera.com/artifactory/cloudera-repos/"
)

javacOptions ++= Seq("-encoding", "UTF-8") 

libraryDependencies += "org.apache.spark" % "spark-streaming_2.10" % "1.3.1"

libraryDependencies += "org.apache.spark" % "spark-streaming-kafka_2.10" % "1.3.1"


libraryDependencies ++= Seq(
    "drools-compiler",
    "drools-core",
    "drools-jsr94",
    "knowledge-api"
).map("org.drools" % _ % "6.2.0.Final")

libraryDependencies += "redis.clients" % "jedis" % "2.7.2"

libraryDependencies += "commons-pool" % "commons-pool" % "1.6"

libraryDependencies += "net.sf.opencsv" % "opencsv" % "2.3"

libraryDependencies += "junit" % "junit" % "4.10"




assemblySettings

jarName in assembly := s"sparkTest-${version.value}.jar"

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
{
  case PathList("META-INF", "ECLIPSEF.RSA")         => MergeStrategy.discard
  case PathList("META-INF", "mailcap")         => MergeStrategy.discard
  case PathList("META-INF", "MANIFEST.MF")         => MergeStrategy.discard
  case x => MergeStrategy.last
}
}