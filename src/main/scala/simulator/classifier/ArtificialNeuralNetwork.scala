package simulator.classifier
import com.intel.analytics.bigdl.dlframes.{DLEstimator, DLModel}
import com.intel.analytics.bigdl.nn.keras.Model
import com.intel.analytics.bigdl.tensor.Tensor
import org.apache.spark.sql.{DataFrame, SparkSession}

trait ArtificialNeuralNetwork {

  def createGraph(
    input: Int,
    hidden: Array[Int],
    output: Int,
    weightsAndBiases: Option[Array[Tensor[Float]]] = None): Model[Float]

  def createEstimator(
    // spark: SparkSession,
    model: Model[Float],
    train: DataFrame,
    epochs: Int,
    learningRate: Double,
    decayRate: Double,
    batchSize: Int,
    input: Int,
    output: Int): DLEstimator[Float]

  def train(estimator: DLEstimator[Float], train: DataFrame): DLModel[Float]

  def test(estimatorModel: DLModel[Float], test: DataFrame)

}
