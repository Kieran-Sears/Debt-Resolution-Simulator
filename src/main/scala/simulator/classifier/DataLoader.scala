package simulator.classifier

import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{array, col, when}
import simulator.Settings

import scala.io.Source

class DataLoader(spark: SparkSession, dataPath: String, headersPath: String) {

  def getHeaders = {
    val src = Source.fromFile(headersPath)
    val cols = src.getLines.take(1).toList.head.split(",")
    src.close
    cols
  }

  val opts = Map("url" -> Settings().DatabaseSettings.simulatorUrl, "dbtable" -> "train")

  val rawData = spark.sqlContext.read
    .format("jdbc")
    .option("inferSchema", "true")
    .options(opts)
    .load

  val featureHeaders = getHeaders

  def getKerasData = {
    val relabeledData = rawData
      .withColumn(
        "label",
        array(
          when(col("InitiatedByCustomer") === 0.0, 1.0).otherwise(0.0),
          when(col("InitiatedByCustomer") === 1.0, 1.0).otherwise(0.0)
        ))
      .drop("CALCDATEDIFF", "InitiatedByCustomer")

    val assembler = new VectorAssembler()
      .setInputCols(featureHeaders)
      .setOutputCol("features")

    assembler.transform(relabeledData.drop("CALCDATEDIFF"))
  }

  def getData = {

    val relabeledData = rawData
      .withColumn(
        "label",
        when(col("InitiatedByCustomer") === -1.0, 0.0)
          .otherwise(col("InitiatedByCustomer")))
      .drop("InitiatedByCustomer")

    val assembler = new VectorAssembler()
      .setInputCols(featureHeaders)
      .setOutputCol("features")

    val data = assembler.transform(relabeledData.drop("CALCDATEDIFF"))
    data
  }

  def stop = {
    spark.stop()
  }
}
