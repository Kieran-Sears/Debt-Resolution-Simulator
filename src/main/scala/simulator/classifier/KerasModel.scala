package simulator.classifier

import com.intel.analytics.bigdl._
import com.intel.analytics.bigdl.nn.{Contiguous, MSECriterion}
import com.intel.analytics.bigdl.numeric.NumericFloat
import com.intel.analytics.bigdl.dlframes.{DLEstimator, DLModel}
import com.intel.analytics.bigdl.tensor.Tensor
import com.intel.analytics.bigdl.utils.Shape
import com.intel.analytics.bigdl.nn.keras._
import com.intel.analytics.bigdl.optim._
import com.intel.analytics.bigdl.visualization.{TrainSummary, ValidationSummary}
import org.apache.spark.mllib.evaluation.MulticlassMetrics
import org.apache.spark.mllib.evaluation.MultilabelMetrics
import org.apache.spark.sql.{DataFrame, SparkSession}

/*
taken from the examples and guides provided by Intel's BigDL Library:
https://bigdl-project.github.io/0.7.0/#KerasStyleAPIGuide/keras-api-scala/
DLEstimator is a provided class that allows use of spark Dataframes / Datasets along with keras style model creation
https://bigdl-project.github.io/0.7.0/#ScalaUserGuide/examples/#image-classification-working-with-spark-dataframe-and-ml-pipeline
 */

class KerasModel(spark: SparkSession) extends ArtificialNeuralNetwork {

  override def createGraph(
    input: Int,
    hidden: Array[Int],
    output: Int,
    weightsAndBiases: Option[Array[Tensor[Float]]] = None): Model[Float] = {

    println(s"Input: $input, Output: $output, Hidden: ${hidden.toString}")

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

    val model = Model(in, out)

    // check predefined weights & bias shapes match graph structure
    weightsAndBiases.map(wab => {
      printWeightsAndBiasShapes(model.getWeightsBias())
      printWeightsAndBiasShapes(wab)
      model.setWeightsBias(wab)
    })

    // model.toGraph().saveGraphTopology("models/")

    model
  }

  override def createEstimator(
    //spark: SparkSession,
    model: Model[Float],
    train: DataFrame,
    epochs: Int,
    learningRate: Double,
    decayRate: Double,
    batchSize: Int,
    input: Int,
    output: Int): DLEstimator[Float] = {

    val criterion = MSECriterion() // 68%
    // val criterion = SmoothL1Criterion() // 65% (mean absolute error = good for outliers)
    // val criterion = BCECriterion() // 54% (practically random)
    // val criterion = AbsCriterion() // 54% (trained but with huge standard deviation)
    // val criterion = CategoricalCrossEntropy() // 54% (weird loss graph - not readable)
    // val criterion = DotProductCriterion() // 45% (started promising but then regressed)
    // val criterion = DistKLDivCriterion() // 45% (no discernible convergence)
    // val criterion = ClassNLLCriterion() // Doesnt work - Target should be 1D tensor after squeeze
    // val criterion = CrossEntropyCriterion() // Doesnt work - Target should be 1D tensor after squeeze
    // val criterion = DiceCoefficientCriterion() // Doesnt work - Null Pointer Error

    val logDir = "./reports/bigDL/"

    val trainSummary = TrainSummary(logDir, spark.sparkContext.appName)
    val validationSummary = ValidationSummary(logDir, spark.sparkContext.appName)

    trainSummary.readScalar("Loss")
    trainSummary.readScalar("accuracy")

    validationSummary.readScalar("Loss")
    validationSummary.readScalar("accuracy")

    val estimator =
      new DLEstimator(model = model, criterion = criterion, featureSize = Array(input), labelSize = Array(output))
        .setBatchSize(batchSize)
        .setMaxEpoch(epochs)
        .setLearningRate(learningRate)
        .setLearningRateDecay(decayRate)
        .setTrainSummary(trainSummary)
        .setValidationSummary(validationSummary)
        .setOptimMethod(new Adam(learningRate, decayRate))
        .setValidation(Trigger.severalIteration(30), train, Array(new MAE), batchSize)

    estimator
  }

  override def train(estimator: DLEstimator[Float], train: DataFrame): DLModel[Float] = {
    println(train.schema)

    val data = train.select("features", "label").toDF("features", "label")

    data.show(5, false)
    println(data.coalesce(2))
    estimator.fit(data)
  }

  override def test(estimatorModel: DLModel[Float], test: DataFrame) = {
    val data = test.select("features", "label").toDF("features", "label")
    val results = estimatorModel.transform(data)

    val raw: DataFrame = results.select("prediction", "label")
    raw.show(10, false)
    val predictionsAndLabels = raw.rdd.map(row => {
      val predictions = row.getAs[Seq[Double]]("prediction")
      val labels = row.getAs[Seq[Double]]("label")

      val prediction = predictions.indexOf(predictions.max)
      val label = labels.indexOf(labels.max)

      (prediction.toDouble, label.toDouble)
    })

    val metrics = new MulticlassMetrics(predictionsAndLabels)

    println("\nConfusion matrix:")
    println(metrics.confusionMatrix.asML)

    // Summary stats
    println("\nSummary Statistics")
    println(s"Accuracy = ${metrics.accuracy}")
    println(s"Weighted precision: ${metrics.weightedPrecision}")
    println(s"Weighted recall: ${metrics.weightedRecall}")
    println(s"Weighted F1 score: ${metrics.weightedFMeasure}")
    println(s"Weighted false positive rate: ${metrics.weightedFalsePositiveRate}")

    metrics.labels.foreach { l =>
      println(s"\nLabel $l:")
      println(s"Precision = ${metrics.precision(l)}")
      println(s"Recall = ${metrics.recall(l)}")
      println(s"False positive rate = ${metrics.falsePositiveRate(l)}")
      println(s"F1-Score = ${metrics.fMeasure(l)}")
    }
  }

  def testMultiLabel(estimatorModel: DLModel[Float], test: DataFrame): Unit = {

    val results = estimatorModel.transform(test.select("features", "label").toDF("features", "label"))

    val raw: DataFrame = results.select("prediction", "label")
    raw.show(10, false)
    val predictionsAndLabels = raw.rdd.map(row => {
      val predictions: Seq[Double] = row.getAs[Seq[Double]]("prediction")
      val labels: Seq[Double] = row.getAs[Seq[Double]]("label")
      (predictions.map(_.doubleValue()).toArray, labels.map(_.doubleValue()).toArray)
    })

    val metrics = new MultilabelMetrics(predictionsAndLabels)

    // Summary stats
    println("Summary Statistics")
    println(s"Recall = ${metrics.recall}")
    println(s"Precision = ${metrics.precision}")
    println(s"F1 measure = ${metrics.f1Measure}")
    println(s"Accuracy = ${metrics.accuracy}")
    metrics.labels.foreach(label => println(s"Class $label precision = ${metrics.precision(label)}"))
    metrics.labels.foreach(label => println(s"Class $label recall = ${metrics.recall(label)}"))
    metrics.labels.foreach(label => println(s"Class $label F1-score = ${metrics.f1Measure(label)}"))
    println(s"Micro recall = ${metrics.microRecall}")
    println(s"Micro precision = ${metrics.microPrecision}")
    println(s"Micro F1 measure = ${metrics.microF1Measure}")
    println(s"Hamming loss = ${metrics.hammingLoss}")
    println(s"Subset accuracy = ${metrics.subsetAccuracy}")

  }

}
