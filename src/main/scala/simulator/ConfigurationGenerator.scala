package simulator

import java.util.UUID

import simulator.model._

import scala.util.Random

class ConfigurationGenerator(seed: Int) {

  val random = new Random(seed)

  def actualise(configurations: Configurations) = {
    println(s"Recieved configurations:\n$configurations")

    val customerProportions = configurations.customerConfigurations
      .map(customerConf => {
        val proportion = configurations.simulationConfiguration.numberOfCustomers / customerConf.proportion
        println(s"${configurations.simulationConfiguration.numberOfCustomers} ${customerConf.proportion} $proportion")
        (customerConf, proportion)
      })

    val customers = customerProportions.flatMap {
      case (customerConf: CustomerConfig, proportion: Int) => {
        (1 to proportion).map(
          _ =>
            generateCustomer(
              customerConf,
              configurations.attributeConfigurations,
              configurations.optionConfigurations,
              configurations.scalarConfigurations,
              configurations.categoricalConfigurations
          )
        )

      }
    }

    val actions: List[Action] = generateAction(configurations.actionConfigurations, configurations.effectConfigurations)

    TrainingData(customers, actions)

  }

  def generateCustomer(
    customerConf: CustomerConfig,
    attributeConfigs: List[AttributeConfig],
    optionConfigs: List[OptionConfig],
    scalarConfigs: List[ScalarConfig],
    categoricalConfigs: List[CategoricalConfig]): Customer = {
    val attributeConfs = attributeConfigs.filter(att => customerConf.attributeConfigurations.contains(att.id))
    val valueConfigs = scalarConfigs ++ categoricalConfigs
    val normAtts = attributeConfs.map(
      attributeConfig =>
        Attribute(
          attributeConfig.id,
          attributeConfig.name,
          actualiseFromConfig(attributeConfig, valueConfigs, optionConfigs)))
    val arr = normaliseScalar(
      valueConfigs
        .find(x => x.id == customerConf.arrears)
        .getOrElse(throw new Exception(s"Cant Find scalar ${customerConf.name} arrears"))
        .asInstanceOf[ScalarConfig])
    val sat = normaliseScalar(
      valueConfigs
        .find(x => x.id == customerConf.satisfaction)
        .getOrElse(throw new Exception(s"Cant Find scalar ${customerConf.name} satisfaction"))
        .asInstanceOf[ScalarConfig])

    val normAttsIds = normAtts.map(x => x.id)

    Customer(
      id = customerConf.id,
      name = customerConf.name,
      featureValues = normAttsIds,
      arrears = arr,
      satisfaction = sat,
      assignedLabel = None)
  }

  def actualiseFromConfig(attribute: AttributeConfig, values: List[Value], options: List[OptionConfig]) = {
    val v =
      values.find(x => x.id == attribute.value).getOrElse(throw new Exception(s"Cannot find value ${attribute.name}"))
    v match {
      case v: CategoricalConfig => normaliseCategorical(v, options)
      case v: ScalarConfig => normaliseScalar(v)
    }
  }

  def normaliseCategorical(c: CategoricalConfig, options: List[OptionConfig]) = {

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

  def normaliseScalar(scalar: ScalarConfig) = {
    val randInRange = scalar.min + (scalar.max - scalar.min) * random.nextDouble()
    BigDecimal(randInRange).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
  }

  def generateAction(actionConfigs: List[ActionConfig], effectConfigs: List[EffectConfig]): List[Action] = {
    actionConfigs.map(actionConf => {

      val effectConfs = effectConfigs.filter(effectConf => actionConf.effectConfigurations.contains(effectConf.id))
      val effects = effectConfs.map(effectConf => generateEffect(effectConf))

      Action(
        id = UUID.randomUUID(),
        name = actionConf.name,
        effects = effects,
        repeat = actionConf.repeat,
        kind = actionConf.kind)
    })

  }

  def generateEffect(effectConfig: EffectConfig): Effect =
    Effect(
      id = UUID.randomUUID(),
      name = effectConfig.name,
      `type` = effectConfig.`type`,
      target = effectConfig.target
    )

}

object ConfigurationGenerator {
  val default = new ConfigurationGenerator(0)
}
