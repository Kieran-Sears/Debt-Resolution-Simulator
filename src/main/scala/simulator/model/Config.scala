package simulator.model

abstract class Config

case class CustomerGenConfig(
    customerStartingDebt: Double,
    debtVarianceOverTime: DebtTimeVariance.Value,
    arrearsBias: Double,
    batchSize: Int
) extends Config
