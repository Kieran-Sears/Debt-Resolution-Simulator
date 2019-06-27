package model
import org.apache.commons.math3.distribution.NormalDistribution
import org.scalatest.{FlatSpec, Matchers, OptionValues}
import util.MockData

class TrainingDataSpec extends FlatSpec with Matchers with OptionValues {

  val mockData = new MockData

  "processCustomer" should "process a customer when effects have been calibrated with standard deviation of zero" in {
    val customerResult = mockData.action1.processCustomer(mockData.customer1)
    customerResult should not equal mockData.customer1
    customerResult.getArrears.value shouldBe 0.0
  }

  it should "process a customer when effects have been calibrated with standard deviation of above zero" in {
    val customerResult = mockData.action1.processCustomer(mockData.customer1)
    customerResult should not equal mockData.customer1
    val satisfaction = mockData.customer1.getSatisfaction.value

    val newValue = customerResult.getSatisfaction.value
    // val percentageDifference = (newValue - satisfaction) / ((newValue + satisfaction) / 2) * 100
    newValue shouldBe 40.0 +- mockData.action1Effect2OnCustomer1.deviation.value
  }

  it should "report no such element when processing a customer given affects with no corresponding effect name" in {
    val alteredEffects = mockData.action1.effects.map(e => e.copy(target = "MalformedTarget"))
    assertThrows[NoSuchElementException] {
      mockData.action1.copy(effects = alteredEffects).processCustomer(mockData.customer1)
    }
  }
}
