package simulator

import simulator.model._
import scala.util.Random

class Generator(seed: Int) {

  val random = new Random(seed)

  def actualiseConfigs(configurations: Configurations) = {
    println(s"Recieved configurations:\n$configurations")
    val attMap = generateFeatureMap(configurations)

    val customers = configurations.customerConfigurations
      .flatMap(customerConf => {
        val proportion = configurations.simulationConfiguration.numberOfCustomers / 100 * customerConf.proportion
        (0 to proportion).map(_ =>
          generateCustomer(customerConf, configurations.attributeConfigurations, configurations.optionConfigurations))
      })

    val simConf = configurations.simulationConfiguration

    val state =
      simConf.startState.getOrElse(State(time = simConf.startTime, configs = configurations)).copy(featureMap = attMap)

    (customers, state)

  }

  def generateFeatureMap(configurations: Configurations) = {
    configurations.attributeConfigurations.toIndexedSeq
  }

  def generateCustomer(
    customerConf: CustomerConfig,
    attributeConfigs: List[AttributeConfig],
    optionConfigs: List[OptionConfig]): Customer = {
    val attributeConfs = attributeConfigs.filter(att => customerConf.attributeConfigurations.contains(att.id))
    val normAtts = attributeConfs.map(attributeConfig =>
      FeatureValue(attributeConfig.id, actualiseFromConfig(attributeConfig, optionConfigs)))
    val arr = normaliseScalar(customerConf.arrears)
    val sat = normaliseScalar(customerConf.satisfaction)
    Customer(featureValues = normAtts, arrears = arr, satisfaction = sat, assignedLabel = None)
  }

  def actualiseFromConfig(attribute: AttributeConfig, options: List[OptionConfig]) =
    attribute.value match {
      case v: Categorical => normaliseCategorical(v, options)
      case v: Scalar => normaliseScalar(v)
    }

  def normaliseCategorical(c: Categorical, options: List[OptionConfig]) = {

    import org.apache.commons.math3.distribution.EnumeratedDistribution
    import scala.collection.JavaConversions._
    import scala.collection.mutable.ListBuffer

    val catOptions = options.filter(option => c.options.contains(option.id)) // TODO make this ordered?
    val probabilities = catOptions.map(option =>
      new org.apache.commons.math3.util.Pair[OptionConfig, java.lang.Double](option, option.probability.toDouble))
    val mapping: java.util.List[org.apache.commons.math3.util.Pair[OptionConfig, java.lang.Double]] = ListBuffer(
      probabilities: _*)

    val distribution = new EnumeratedDistribution[OptionConfig](mapping)
    catOptions.indexOf(distribution.sample()).toDouble
  }

  def normaliseScalar(scalar: Scalar) =
    scalar.min + (scalar.max - scalar.min) * random.nextDouble()
//
//  def generateAction(actionConfigs: List[ActionConfig], effectConfigs: List[EffectConfig]): Unit = {
//    actionConfigs.map(actionConf =>{
//
//      actionConf.`type` match {
//        case ActionType.Customer => {
//          val actions = actionConf.effectConfigurations.map(effectConfName => effectConfigs.filter(e => e.id == effectConfName))
//          actions.
//        }
//        case ActionType.Agent => {}
//      }
//    })
//
//  }
}

object Generator {
  val default = new Generator(0)
}
