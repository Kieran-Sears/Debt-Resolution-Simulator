package simulator.classifier

import com.intel.analytics.bigdl._
import com.intel.analytics.bigdl.dataset.DataSet
import com.intel.analytics.bigdl.dataset.image.{BytesToGreyImg, GreyImgNormalizer, GreyImgToBatch}
import com.intel.analytics.bigdl.nn.ClassNLLCriterion
import com.intel.analytics.bigdl.numeric.NumericFloat
import com.intel.analytics.bigdl.optim._
import com.intel.analytics.bigdl.utils.Engine
import com.intel.analytics.bigdl.models.lenet.LeNet5
import org.apache.spark.SparkContext

object NeuralNetwork {
  import models.lenet.Utils._

  def main(args: Array[String]): Unit = {
    trainParser.parse(args, new TrainParams()).map(param => {
      val conf = Engine.createSparkConf()
        .setAppName("Train Lenet on MNIST")
        .set("spark.task.maxFailures", "1")
      val sc = new SparkContext(conf)
      Engine.init

      val trainData = param.folder + "/train-images-idx3-ubyte"
      val trainLabel = param.folder + "/train-labels-idx1-ubyte"
      val validationData = param.folder + "/t10k-images-idx3-ubyte"
      val validationLabel = param.folder + "/t10k-labels-idx1-ubyte"

      val model = if (param.graphModel) LeNet5.kerasGraph(classNum = 10)
      else LeNet5.keras(classNum = 10)

      val optimMethod = if (param.stateSnapshot.isDefined) {
        OptimMethod.load[Float](param.stateSnapshot.get)
      } else {
        new SGD[Float](learningRate = param.learningRate,
          learningRateDecay = param.learningRateDecay)
      }

      val trainSet = DataSet.array(load(trainData, trainLabel), sc) ->
        BytesToGreyImg(28, 28) -> GreyImgNormalizer(trainMean, trainStd) -> GreyImgToBatch(
        param.batchSize)

      val validationSet = DataSet.array(load(validationData, validationLabel), sc) ->
        BytesToGreyImg(28, 28) -> GreyImgNormalizer(testMean, testStd) -> GreyImgToBatch(
        param.batchSize)

      model.compile(optimizer = optimMethod,
        loss = ClassNLLCriterion[Float](logProbAsInput = false),
        metrics = Array(new Top1Accuracy[Float](), new Top5Accuracy[Float](), new Loss[Float]))
      model.fit(trainSet, nbEpoch = param.maxEpoch, validationData = validationSet)

      sc.stop()
    })
  }
}