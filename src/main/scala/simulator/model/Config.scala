package simulator.model

abstract class Config

case class CustomerGenConfig(
    customerStartingDebt: Double,
    debtVarianceOverTime: DebtTimeVariance.Value,
    arrearsBias: Double,
    batchSize: Int
) extends Config

case class SimulationConfig(
    startState: State,
    startTime: Int,
    endTime: Option[Int] = None,
    customerGenParams: CustomerGenConfig
) extends Config
