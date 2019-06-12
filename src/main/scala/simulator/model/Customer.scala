package simulator.model

import java.util.UUID

case class CustomerConfig(
  id: UUID = UUID.randomUUID(),
  name: String,
  arrears: Scalar,
  satisfaction: Scalar,
  attributeConfigurations: List[String] = Nil,
  proportion: Int = 100,
  kind: String = "customer")

case class Customer(
  id: UUID = UUID.randomUUID(),
  name: String,
  arrears: Double,
  satisfaction: Double,
  featureValues: List[Attribute] = Nil,
  difficulty: Option[Int] = None,
  assignedLabel: Option[Int] = None)

case class Attribute(
  id: UUID = UUID.randomUUID(),
  name: String,
  value: Double
)

case class AttributeConfig(
  id: UUID = UUID.randomUUID(),
  name: String,
  value: Value
)

trait Value

case class Scalar(
  variance: Variance.Value,
  min: Double,
  max: Double,
  kind: String = "scalar"
) extends Value

case class Categorical(
  options: List[String],
  kind: String = "categorical"
) extends Value

case class OptionConfig(
  id: UUID = UUID.randomUUID(),
  name: String,
  kind: String = "option",
  probability: Int
)
