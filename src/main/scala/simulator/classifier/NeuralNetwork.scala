package simulator.classifier


import com.intel.analytics.bigdl._
import com.intel.analytics.bigdl.nn.BCECriterion
import com.intel.analytics.bigdl.numeric.NumericFloat
import com.intel.analytics.bigdl.dlframes.DLEstimator
import com.intel.analytics.bigdl.tensor.Tensor
import com.intel.analytics.bigdl.utils.Shape
import com.intel.analytics.bigdl.nn.keras._
import org.apache.spark.sql.DataFrame

/*
taken from the examples and guides provided by Intel's BigDL Library:
https://bigdl-project.github.io/0.7.0/#KerasStyleAPIGuide/keras-api-scala/
DLEstimator is a provided class that allows use of spark Dataframes / Datasets along with keras style model creation
https://bigdl-project.github.io/0.7.0/#ScalaUserGuide/examples/#image-classification-working-with-spark-dataframe-and-ml-pipeline
 */

class NeuralNetwork {

  def kerasGraph(input: Int,
                 hidden: Array[Int],
                 output: Int,
                 weightsAndBiases: Array[Tensor[Float]]): Model[Float] = {

    def printWeightsAndBiasShapes(edges: Array[Tensor[Float]]) = {
      edges foreach (x => x.size foreach print)
      println
    }

    val in =
      Input(inputShape = Shape(input))
    val layer0 =
      Dense(hidden(0), activation = "relu").setName("h" + 0).inputs(in)
    val layer1 =
      Dense(hidden(1), activation = "relu").setName("h" + 1).inputs(layer0)
    val out =
      Dense(output, activation = "softmax").setName("out").inputs(layer1)

    Model(in, out)

    // check predefined weights & bias shapes match graph structure
    //    printWeightsAndBiasShapes(model.getWeightsBias())
    //    printWeightsAndBiasShapes(weightsAndBiases)
    //    model.setWeightsBias(weightsAndBiases)
  }

  def train(model: Model[Float],
            train: DataFrame,
            test: DataFrame,
            epochs: Int = 100,
            learningRate: Double = 0.001,
            batchSize: Int = 120,
            trainTestSplit: Array[Double] = Array(0.7, 0.3)
           ) = {

    val criterion = BCECriterion[Float]()

    println(model.getInputShape().toString)
    println(model.getOutputShape().toSingle().toArray)
    model.getOutputShape().toSingle().toArray foreach print

    // Todo work out the difference between the classifier and estimator

    // DLEstimator
    val estimator = new DLEstimator(model, criterion, Array(18), Array(2))
      .setBatchSize(batchSize)
      .setMaxEpoch(epochs)
      .setLearningRate(learningRate)

    estimator.fit(train)
  }
}