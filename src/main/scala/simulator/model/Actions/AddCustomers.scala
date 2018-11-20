package simulator.model.Actions

import java.util.UUID

import simulator.model._

case class AddCustomers(actionId: UUID, repeat: Option[Repeat], config: CustomerGenConfig) extends Action {

  override def perform(currentState: State) = {

    val newBatchOfCustomers = makeBatchOfCustomers(currentState.time)

    val batchArrears = calculateSumOfBatchArrears(newBatchOfCustomers)

    val newTotalArrears = batchArrears + currentState.stats.totalArrears

    val newStats = Statistics(batchArrears, newTotalArrears)

    currentState.actionQueue.removeAction(currentState.time, actionId)

    prepareNextRepetition(currentState)

    val newState =
      State(currentState.time,
            newStats,
            currentState.actionQueue,
            currentState.history :+ currentState)

    val nextActionTime = repeat
      .map(rep => currentState.time + rep.interval)
      .getOrElse(-1)

    (nextActionTime, newState)
  }

  def makeBatchOfCustomers(currentTime: Int) = {
    (for (_ <- 1 to config.batchSize) yield {
      val arrears = config.debtVarianceOverTime match {
        case DebtTimeVariance.None =>
          config.customerStartingDebt
        case DebtTimeVariance.Increase =>
          config.customerStartingDebt + (config.arrearsBias * currentTime)
        case DebtTimeVariance.Decrease =>
          config.customerStartingDebt - (config.arrearsBias * currentTime)
      }
      Customer(Account(arrears))
    }).toList
  }

  def calculateSumOfBatchArrears(batchOfCustomers: List[Customer]) = {
    batchOfCustomers.foldLeft(0d)((acc, customer) =>
      acc + customer.account.arrears)
  }

  def prepareNextRepetition(state: State) = {
    repeat.foreach(repeatInstructions => {
        if (state.time < repeatInstructions.finishTime) {
          state.actionQueue.addAction(state.time + repeatInstructions.interval,
                                   AddCustomers(UUID.randomUUID(),
                                                Some(repeatInstructions), config))
        }
      })
  }

}
