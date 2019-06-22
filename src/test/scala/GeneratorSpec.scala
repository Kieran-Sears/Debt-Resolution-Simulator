import java.util.UUID

import org.scalatest.{FlatSpec, Matchers}
import simulator.Generator
import simulator.model._

class GeneratorSpec extends FlatSpec with Matchers {

  val gen = Generator.default

  "Generator" should "transform a scalar configuration to a value within range" in {
    val config = ScalarConfig(UUID.randomUUID(), VarianceEnum.None, 30, 50)
    val value = gen.idealScalar(config)
    value shouldEqual 40
  }

  def getValidCustomerConfigurationData() = {
    val arrears = ScalarConfig(id = UUID.randomUUID(), VarianceEnum.None, 50, 500)
    val satisfaction = ScalarConfig(id = UUID.randomUUID(), VarianceEnum.None, 10, 50)

    val age = ScalarConfig(id = UUID.randomUUID(), VarianceEnum.None, 35, 85)
    val income = ScalarConfig(id = UUID.randomUUID(), VarianceEnum.None, 20000, 28000)

    val rent = OptionConfig(id = UUID.randomUUID(), "Rent", 50)
    val homeowner = OptionConfig(id = UUID.randomUUID(), "Homeowner", 20)
    val councilHousing = OptionConfig(id = UUID.randomUUID(), "Council housing", 30)
    val tenure = CategoricalConfig(id = UUID.randomUUID(), List(rent.id, homeowner.id, councilHousing.id))

    val arrearsAtt =
      AttributeConfig(id = UUID.randomUUID(), name = "Arrears", value = arrears.id, AttributeEnum.Override)
    val satisfactionAtt =
      AttributeConfig(id = UUID.randomUUID(), name = "Satisfaction", value = satisfaction.id, AttributeEnum.Override)
    val ageAtt = AttributeConfig(id = UUID.randomUUID(), name = "Age", value = age.id, AttributeEnum.Override)
    val incomeAtt = AttributeConfig(id = UUID.randomUUID(), name = "Income", value = income.id, AttributeEnum.Override)
    val tenureAtt = AttributeConfig(id = UUID.randomUUID(), name = "Tenure", value = tenure.id, AttributeEnum.Override)

    val customerConfig = CustomerConfig(
      id = UUID.randomUUID(),
      name = "Mary",
      attributeOverrides = List(arrearsAtt.id, ageAtt.id, satisfactionAtt.id, incomeAtt.id, tenureAtt.id),
      proportion = 20
    )

    val arrearsG = ScalarConfig(id = UUID.randomUUID(), VarianceEnum.None, 10, 50000)
    val satisfactionG = ScalarConfig(id = UUID.randomUUID(), VarianceEnum.None, 0, 100)

    val ageG = ScalarConfig(id = UUID.randomUUID(), VarianceEnum.None, 18, 85)
    val incomeG = ScalarConfig(id = UUID.randomUUID(), VarianceEnum.None, 15000, 45000)

    val rentG = OptionConfig(id = UUID.randomUUID(), "Rent", 33)
    val homeownerG = OptionConfig(id = UUID.randomUUID(), "Homeowner", 33)
    val councilHousingG = OptionConfig(id = UUID.randomUUID(), "Council housing", 34)
    val tenureG = CategoricalConfig(id = UUID.randomUUID(), List(rentG.id, homeownerG.id, councilHousingG.id))

    val arrearsAttG =
      AttributeConfig(id = UUID.randomUUID(), name = "Arrears", value = arrearsG.id, AttributeEnum.Global)
    val satisfactionAttG =
      AttributeConfig(id = UUID.randomUUID(), name = "Satisfaction", value = satisfactionG.id, AttributeEnum.Global)
    val ageAttG = AttributeConfig(id = UUID.randomUUID(), name = "Age", value = ageG.id, AttributeEnum.Global)
    val incomeAttG = AttributeConfig(id = UUID.randomUUID(), name = "Income", value = incomeG.id, AttributeEnum.Global)
    val tenureAttG = AttributeConfig(id = UUID.randomUUID(), name = "Tenure", value = tenureG.id, AttributeEnum.Global)

    val scalarConfigs = List(arrears, satisfaction, age, income, arrearsG, satisfactionG, ageG, incomeG)
    val categoricalConfigs = List(tenure, tenureG)
    val optionConfigs = List[OptionConfig](rent, homeowner, councilHousing)

    val attributeConfigs = List(
      arrearsAttG,
      satisfactionAttG,
      ageAttG,
      incomeAttG,
      tenureAttG,
      ageAtt,
      incomeAtt,
      tenureAtt,
      arrearsAtt,
      satisfactionAtt)

    (customerConfig, attributeConfigs, optionConfigs, scalarConfigs, categoricalConfigs)
  }

  it should "when given a valid customer configuration return a customer with normalised values" in {

    val d = getValidCustomerConfigurationData()

    val customer = (gen.generateCustomer _).tupled(d)

    customer.id shouldEqual d._1.id
    customer.getArrears.value shouldEqual 275.0 +- 225.0
    customer.getSatisfaction.value shouldEqual 30.0 +- 20.0
    customer.difficulty shouldEqual None

  }

  it should "when given valid configurations return average customers to train on" in {
    val d = getValidCustomerConfigurationData()
    val customer = (gen.idealCustomer _).tupled(d)

    customer.id shouldEqual d._1.id
    customer.getArrears.value shouldEqual 275.0 +- 225.0
    customer.getSatisfaction.value shouldEqual 30.0 +- 20.0
    customer.difficulty shouldEqual None
  }

}
