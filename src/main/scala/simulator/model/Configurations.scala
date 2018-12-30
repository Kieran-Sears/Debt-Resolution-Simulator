package simulator.model

case class Configurations (
 customer: CustomerGenConfig = CustomerGenConfig(),
 simulation: SimulationConfig = SimulationConfig()
)

case class CustomerGenConfig (
  debtVarianceOverTime: DebtTimeVariance.Value = DebtTimeVariance.None,
  arrearsBias: Double = 0,
  kind: String = "customer")

case class SimulationConfig(
    startState: Option[State] = None,
    startTime: Int = 0,
    endTime: Option[Int] = None,
    kind: String = "simulation")
