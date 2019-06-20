package simulator

import java.util.UUID

import simulator.model._
import org.apache.commons.math3.distribution.NormalDistribution
import scala.util.Random

class Generator(seed: Int) {

  val random = new Random(seed)

  def trainingData(configurations: Configurations) = {

    val customers = configurations.customerConfigurations.map(
      customerConf =>
        idealCustomer(
          customerConf,
          configurations.attributeConfigurations,
          configurations.optionConfigurations,
          configurations.scalarConfigurations,
          configurations.categoricalConfigurations
      ))

    val actions: List[Action] =
      generateActions(configurations.actionConfigurations, configurations.effectConfigurations)

    TrainingData(configurations.id, customers, actions)
  }

  def playData(configurations: Configurations): List[Customer] = {
    val customerProportions = configurations.customerConfigurations
      .map(customerConf => {
        val proportion = configurations.simulationConfiguration.numberOfCustomers / customerConf.proportion
        println(s"${configurations.simulationConfiguration.numberOfCustomers} ${customerConf.proportion} $proportion")
        (customerConf, proportion)
      })

    customerProportions.flatMap {
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
  }

  def idealInfluences(actions: List[Action]) =
    actions.foldLeft(Map[(Customer, Action), List[Influence]]()) {
      case (acc, action) => {
        val customer = action.getTarget
        val influences = action.effects.map(effect => Influence(effect.target, actualiseInfluence(effect)))
        acc + ((customer, action) -> influences)
      }
    }

  def actualiseInfluence(effect: Effect) = {
    val mean = effect.value.getOrElse(
      throw new NoSuchElementException(s"Could not find value for effect ${effect.name} for ${effect.target}"))
    val standardDistribution = effect.certainty.getOrElse(
      throw new NoSuchElementException(s"Could not find certainty for effect ${effect.name} for ${effect.target}"))
    val dist = new NormalDistribution(mean, standardDistribution)
    dist.sample()
  }

  def customerProportions(configurations: Configurations) = {
    configurations.customerConfigurations
      .map(customerConf => {
        val proportion = configurations.simulationConfiguration.numberOfCustomers / customerConf.proportion
        println(s"${configurations.simulationConfiguration.numberOfCustomers} ${customerConf.proportion} $proportion")
        (customerConf, proportion)
      })
  }

  def idealCustomer(
    customerConf: CustomerConfig,
    attributeConfigs: List[AttributeConfig],
    optionConfigs: List[OptionConfig],
    scalarConfigs: List[ScalarConfig],
    categoricalConfigs: List[CategoricalConfig]) = {

    val customAttributes = attributeConfigs.filter(att => customerConf.attributeOverrides.contains(att.id))

    val normAtts = attributeConfigs.map(att =>
      customAttributes.find(x => x.name == att.name) match {
        case Some(attribute) => idealAttribute(attribute, scalarConfigs ++ categoricalConfigs, optionConfigs)
        case None => idealAttribute(att, scalarConfigs ++ categoricalConfigs, optionConfigs)
    })

    Customer(id = customerConf.id, name = customerConf.name, attributes = normAtts, assignedLabel = None)
  }

  def idealAttribute(attribute: AttributeConfig, values: List[Value], options: List[OptionConfig]) = {
    values
      .find(x => x.id == attribute.value)
      .getOrElse(throw new NoSuchElementException(s"Cannot find value ${attribute.name}")) match {
      case v: CategoricalConfig => Attribute(UUID.randomUUID(), attribute.name, idealCategorical(v, options))
      case v: ScalarConfig => Attribute(UUID.randomUUID(), attribute.name, idealScalar(v))
    }
  }

  def idealCategorical(c: CategoricalConfig, options: List[OptionConfig]) =
    options
      .indexOf(
        options.foldLeft(options.head) { case (option, x) => if (x.probability > option.probability) x else option })
      .toDouble

  def idealScalar(scalarConfig: ScalarConfig) =
    ((scalarConfig.max - scalarConfig.min) / 2) + scalarConfig.min

  def generateCustomer(
    customerConf: CustomerConfig,
    attributeConfigs: List[AttributeConfig],
    optionConfigs: List[OptionConfig],
    scalarConfigs: List[ScalarConfig],
    categoricalConfigs: List[CategoricalConfig]): Customer = {

    val customAttributes = attributeConfigs.filter(att => customerConf.attributeOverrides.contains(att.id))

    val normAtts = attributeConfigs.map(att =>
      customAttributes.find(x => x.name == att.name) match {
        case Some(attribute) => idealAttribute(attribute, scalarConfigs ++ categoricalConfigs, optionConfigs)
        case None => idealAttribute(att, scalarConfigs ++ categoricalConfigs, optionConfigs)
    })

    Customer(id = customerConf.id, name = customerConf.name, attributes = normAtts, assignedLabel = None)
  }

  def normaliseAttribute(attribute: AttributeConfig, values: List[Value], options: List[OptionConfig]) = {
    val v =
      values.find(x => x.id == attribute.value).getOrElse(throw new Exception(s"Cannot find value ${attribute.name}"))
    v match {
      case v: CategoricalConfig => Attribute(attribute.id, attribute.name, normaliseCategorical(v, options))
      case v: ScalarConfig => Attribute(attribute.id, attribute.name, normaliseScalar(v))
    }
  }

  def normaliseCategorical(c: CategoricalConfig, options: List[OptionConfig]) = {

    import org.apache.commons.math3.distribution.EnumeratedDistribution
    import scala.collection.JavaConversions._
    import scala.collection.mutable.ListBuffer

    val catOptions = options.filter(option => c.options.contains(option.id))
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

  def generateActions(actionConfigs: List[ActionConfig], effectConfigs: List[EffectConfig]): List[Action] = {
    actionConfigs.map(actionConf => {

      val effectConfs = effectConfigs.filter(effectConf => actionConf.effectConfigurations.contains(effectConf.id))
      val effects = effectConfs.map(effectConf => generateEffect(effectConf))

      Action(id = UUID.randomUUID(), name = actionConf.name, effects = effects, repeat = None, target = None)
    })

  }

  def generateEffect(effectConfig: EffectConfig): Effect =
    Effect(
      id = UUID.randomUUID(),
      name = effectConfig.name,
      effectType = effectConfig.effectType,
      target = effectConfig.target
    )

}

object Generator {
  val default = new Generator(0)
}
