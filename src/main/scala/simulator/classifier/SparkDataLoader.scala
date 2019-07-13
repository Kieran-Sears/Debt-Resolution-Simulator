package simulator.classifier

import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions.{array, col, when}
import simulator.model.ActionConfig

class SparkDataLoader(spark: SparkSession) extends DataLoader {

  override def getData(opts: Map[String, String], featureHeaders: Array[String]): DataFrame =
    spark.sqlContext.read
      .format("jdbc")
      .option("inferSchema", "true")
      .options(opts)
      .load
      .cache
      .toDF(featureHeaders: _*)

  override def getLabelIndexMap(actions: List[ActionConfig]): Map[String, Int] =
    actions.map(_.name).zipWithIndex.toMap

  override def assembleData(featureHeaders: Array[String], data: DataFrame): DataFrame = {

    val relabeledData = data
      .withColumn(
        "label",
        array(
          when(col("action_label") === 1.0, 1.0).otherwise(0.0),
          when(col("action_label") === 2.0, 1.0).otherwise(0.0)
        ))
      .drop("id", "configuration_id", "customer_id")

    new VectorAssembler()
      .setInputCols(featureHeaders.map(_.toLowerCase))
      .setOutputCol("features")
      .transform(relabeledData)

  }

  override def stop = {
    spark.stop()
  }
}
