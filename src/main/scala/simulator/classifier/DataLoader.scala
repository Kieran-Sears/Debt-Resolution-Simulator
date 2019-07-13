package simulator.classifier
import org.apache.spark.sql.DataFrame
import simulator.model.ActionConfig

trait DataLoader {

  def getData(opts: Map[String, String], featureHeaders: Array[String]): DataFrame

  def getLabelIndexMap(actions: List[ActionConfig]): Map[String, Int]

  def assembleData(featureHeaders: Array[String], data: DataFrame): DataFrame

  def stop
}
