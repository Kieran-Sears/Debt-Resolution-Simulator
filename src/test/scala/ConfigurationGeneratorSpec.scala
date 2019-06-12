import org.scalatest.{FlatSpec, Matchers}
import simulator.ConfigurationGenerator
import simulator.model._

class ConfigurationGeneratorSpec extends FlatSpec with Matchers {

  "Generator" should
    "when given a valid customer configuration return a customer with normalised values" in {
    val customerConfig = CustomerConfig(
      name = "Mary",
      arrears = Scalar(Variance.None, 50, 500),
      satisfaction = Scalar(Variance.None, 10, 50),
      attributeConfigurations = List("Age", "Income", "Tenure")
    )
    val attributeConfig = List(
      AttributeConfig(name = "Age", value = Scalar(Variance.None, 18, 85)),
      AttributeConfig(name = "Income", value = Scalar(Variance.None, 15000, 22000)),
      AttributeConfig(name = "Tenure", value = Categorical(List("Rent", "Homeowner", "Council housing")))
    )
    val optionConfigs = List[OptionConfig](
      OptionConfig(name = "Rent", probability = 60),
      OptionConfig(name = "Homeowner", probability = 30),
      OptionConfig(name = "Council housing", probability = 10)
    )

    val customer = ConfigurationGenerator.default.generateCustomer(customerConfig, attributeConfig, optionConfigs)
    customer.id shouldEqual customerConfig.id
    customer.arrears shouldEqual 275.0 +- 225.0
    customer.satisfaction shouldEqual 30 +- 20
    customer.difficulty shouldEqual None

  }
  it should "remove the customer from the states customer list" in {}
}
