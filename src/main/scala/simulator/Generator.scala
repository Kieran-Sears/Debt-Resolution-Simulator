package simulator

import cats.implicits._
import simulator.model._
import scala.util.{Failure, Random, Success, Try}

class Generator(seed: Int) {

  val random = new Random(seed)

  def actualiseConfigs(configurations: Configurations) = {
    val attMap = generateFeatureMap(configurations)
    val customers = configurations.customerConfigurations
      .flatMap(customerConf => {
        val numberOfStereotype = configurations.simulationConfiguration.numberOfCustomers / 100 * customerConf
          .proportion
        (0 to numberOfStereotype).map(_ => generateCustomer(customerConf, attMap))
      })
      .sequence

    val simConf = configurations.simulationConfiguration
    val state =
      simConf.startState.getOrElse(State(time = simConf.startTime, configs = configurations)).copy(featureMap = attMap)

    (customers, state)

  }

  def generateFeatureMap(configurations: Configurations) = {
    configurations.customerConfigurations.foldLeft(IndexedSeq[Attribute]())((acc, customerConf) => {
      acc.union(customerConf.attributeConfigurations)
    })
  }

  def generateCustomer(config: CustomerConfig, attMap: IndexedSeq[Attribute]): Try[Customer] = {
    val x =
      config.attributeConfigurations.map(attributeConfig => actualiseFromConfig(attributeConfig)).sequence.toOption
    val y = attMap
      .find(x => x.id.toLowerCase == "arrears")
      .map(arrearsConf => actualiseFromConfig(arrearsConf))
      .sequence
      .toOption
      .flatten

    val optionalCustomer = for {
      featureValues <- x
      arrears <- y
    } yield Customer(featureValues = featureValues, arrears = arrears, assignedLabel = config.assignedLabel)

    optionalCustomer.fold[Try[Customer]](
      Failure(new Exception(s"Could not generate customer ${config.id} from config")))(Success(_))
  }

  def actualiseFromConfig(attribute: Attribute) =
    attribute.value match {
      case v: Categorical => normaliseCategorical(v)
      case v: Scalar => Success(normaliseScalar(v))
      case _ => Failure(new Exception(s"Not enough information to actualise attribute ${attribute.id}"))
    }

  def normaliseCategorical(c: Categorical): Try[Double] = {
    val index = c.options.indexOf(c.startValue)
    if (index == -1)
      Failure(new Exception(s"Could not find ${c.startValue} in ${c.options} for ${c.kind} attribute value"))
    else Success(index)
  }

  def normaliseScalar(scalar: Scalar) =
    scalar.min + (scalar.max - scalar.min) * random.nextDouble()

}

object Generator {
  val default = new Generator(0)
}
