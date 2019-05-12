package simulator.model

import java.util.UUID

case class Customer(
  id: UUID = UUID.randomUUID(),
  arrears: Double,
  featureValues: List[Double] = Nil,
  difficulty: Option[Int] = None,
  assignedLabel: Int)

case class Attribute(
  id: String,
  value: Value
)

trait Value

case class Scalar(
  start: Double,
  variance: Variance.Value,
  min: Double,
  max: Double,
  kind: String = "scalar"
) extends Value

case class Categorical(
  startValue: Int = -1,
  options: List[String],
  kind: String = "categorical"
) extends Value
