package simulator.spark

import java.util.regex.Pattern
import org.apache.spark.SparkConf
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.dstream.{DStream, ReceiverInputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}

class Cassandra {

  val conf = new SparkConf()
  conf set ("spark.cassandra.connection.host", "127.0.0.1")
  conf setMaster "local[*]" // TODO only for local dev, comment out for deployment
  conf setAppName "CassandraExample"

  val streamingServiceContext = new StreamingContext(conf, Seconds(10))

  val lines: ReceiverInputDStream[String] =
    streamingServiceContext.socketTextStream("127.0.0.1", 9999, StorageLevel.MEMORY_AND_DISK_SER)

  val requests: DStream[(String, String, Int, String)] = lines.map(x => {
    val matcher = apacheLogPattern().matcher(x)
    if (matcher.matches()) {

      val ip = matcher.group(1)
      val request = matcher.group(5)
      val requestFields = request.toString.split(" ")
      val url = requestFields(1)
      (ip, url, matcher.group(6).toInt, matcher.group(9))
    } else ("error", "error", 0, "error")
  })

  // val requests: DStream[(String, String, Int, String)] = lines.map(x => { (x._1, x._2, x._3) }) // TODO get state to be analysed and normalise it ready for processing

  requests.foreachRDD((rdd, time) => {
    rdd.cache()
    println("writing " + rdd.count() + " rows to cassandra")
    // rdd.saveToCassandra("frank", "LogTest", SomeColumns("IP", "URL", "STATUS", "USER_AGENT"))
  })

  streamingServiceContext.checkpoint(System.getProperty("user.dir") + "checkpoints/CassandraExample/")
  streamingServiceContext.start()
  streamingServiceContext.awaitTermination()

  /** Retrieves a regex Pattern for parsing Apache access logs. */
  def apacheLogPattern(): Pattern = {
    val ddd = "\\d{1,3}"
    val ip = s"($ddd\\.$ddd\\.$ddd\\.$ddd)?"
    val client = "(\\S+)"
    val user = "(\\S+)"
    val dateTime = "(\\[.+?\\])"
    val request = "\"(.*?)\""
    val status = "(\\d{3})"
    val bytes = "(\\S+)"
    val referer = "\"(.*?)\""
    val agent = "\"(.*?)\""
    val regex = s"$ip $client $user $dateTime $request $status $bytes $referer $agent"
    Pattern.compile(regex)
  }
}
