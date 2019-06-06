import java.util.UUID

import org.scalatest.{FlatSpec, Matchers}
import simulator.Generator
import simulator.model._

class GeneratorSpec extends FlatSpec with Matchers {

  "Generator" should
    "when given a valid customer configuration return a customer with normalised values" in {
    val customerConfig = CustomerConfig(
      id = "Mary",
      arrears = Scalar(Variance.None, 50, 500),
      satisfaction = Scalar(Variance.None, 10, 50),
      attributeConfigurations = List("Age", "Income", "Tenure")
    )
    val attributeConfig = List(
      AttributeConfig("Age", Scalar(Variance.None, 18, 85)),
      AttributeConfig("Income", Scalar(Variance.None, 15000, 22000)),
      AttributeConfig("Tenure", Categorical(List("Rent", "Homeowner", "Council housing")))
    )
    val optionConfigs = List[OptionConfig](
      OptionConfig(id = "Rent", probability = 60),
      OptionConfig(id = "Homeowner", probability = 30),
      OptionConfig(id = "Council housing", probability = 10)
    )

    val customer = Generator.default.generateCustomer(customerConfig, attributeConfig, optionConfigs)
    customer.id shouldEqual customerConfig.id
    customer.arrears shouldEqual 275.0 +- 225.0
    customer.satisfaction shouldEqual 30 +- 20
    customer.difficulty shouldEqual None

  }
  it should "remove the customer from the states customer list" in {}
}
