import java.util.UUID

import org.scalatest.{FlatSpec, Matchers}
import simulator.ConfigurationGenerator
import simulator.model._

class ConfigurationGeneratorSpec extends FlatSpec with Matchers {

  "Generator" should
    "when given a valid customer configuration return a customer with normalised values" in {

    val arrears = ScalarConfig(id = UUID.randomUUID(), Variance.None, 50, 500)
    val satisfaction = ScalarConfig(id = UUID.randomUUID(), Variance.None, 10, 50)

    val age = ScalarConfig(id = UUID.randomUUID(), Variance.None, 18, 85)
    val income = ScalarConfig(id = UUID.randomUUID(), Variance.None, 15000, 22000)

    val rent = OptionConfig(id = UUID.randomUUID(), "Rent", 50)
    val homeowner = OptionConfig(id = UUID.randomUUID(), "Homeowner", 20)
    val councilHousing = OptionConfig(id = UUID.randomUUID(), "Council housing", 30)
    val tenure = CategoricalConfig(id = UUID.randomUUID(), List(rent.id, homeowner.id, councilHousing.id))

    val ageAtt = AttributeConfig(id = UUID.randomUUID(), name = "Age", value = age.id)
    val incomeAtt = AttributeConfig(id = UUID.randomUUID(), name = "Income", value = income.id)
    val tenureAtt = AttributeConfig(id = UUID.randomUUID(), name = "Tenure", value = tenure.id)

    val customerConfig = CustomerConfig(
      id = UUID.randomUUID(),
      name = "Mary",
      arrears = arrears.id,
      satisfaction = satisfaction.id,
      attributeConfigurations = List(ageAtt.id, incomeAtt.id, tenureAtt.id),
      proportion = 20
    )

    val attributeConfigs = List(ageAtt, incomeAtt, tenureAtt)

    val scalarConfigs = List(arrears, satisfaction, age, income)
    val categoricalConfigs = List(tenure)
    val optionConfigs = List[OptionConfig](rent, homeowner, councilHousing)

    val customer =
      ConfigurationGenerator.default
        .generateCustomer(customerConfig, attributeConfigs, optionConfigs, scalarConfigs, categoricalConfigs)

    customer.id shouldEqual customerConfig.id
    customer.arrears shouldEqual 275.0 +- 225.0
    customer.satisfaction shouldEqual 30.0 +- 20.0
    customer.difficulty shouldEqual None

  }

}
