package simple.DebtVsAge.model

case class SimulationConfig(
                             startState: State,
                             startTime: Int,
                             endTime: Option[Int] = None,
                             customerGenParams: CustomerGenConfig
)
