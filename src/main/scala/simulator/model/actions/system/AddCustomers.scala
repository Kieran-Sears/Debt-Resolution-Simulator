package simulator.model.actions.system

import java.util.UUID

import simulator.model.actions.{Repeat, SystemAction}
import simulator.model.{Customer, DebtTimeVariance, State, Statistics}

case class AddCustomers(actionId: UUID = UUID.randomUUID(),
                        numberOfCustomers: Int,
                        startingDebt: Double,
                        repeat: Option[Repeat],
                        kind: String = "addCustomers")
    extends SystemAction {

  override def perform(state: State) = {
    val stateWithoutAction =
      state.removeSystemAction(state.time, actionId)
    val newQueue = prepareNextRepetition(stateWithoutAction)
    val newBatchOfCustomers = makeBatchOfCustomers(state)
    val newStats = updateStatistics(state.stats, newBatchOfCustomers)
    val updatedCustomerList = state.customers ++ newBatchOfCustomers
    state.copy(stats = newStats, systemActions = newQueue, customers = updatedCustomerList)
  }

  def updateStatistics(stats: Statistics, newBatchOfCustomers: List[Customer]) = {
    val batchArrears = calculateSumOfBatchArrears(newBatchOfCustomers)
    val newTotalArrears = batchArrears + stats.totalArrears
    Statistics(batchArrears, newTotalArrears)
  }

  def makeBatchOfCustomers(state: State) = {
    val currentTime = state.time
    val config = state.configs.customer

    (for (_ <- 1 to numberOfCustomers) yield {
      val arrears = config.debtVarianceOverTime match {
        case DebtTimeVariance.None =>
          startingDebt
        case DebtTimeVariance.Increase =>
          startingDebt + (config.arrearsBias * currentTime)
        case DebtTimeVariance.Decrease =>
          startingDebt - (config.arrearsBias * currentTime)
      }
      Customer(arrears = arrears)
    }).toList
  }

  def calculateSumOfBatchArrears(batchOfCustomers: List[Customer]) = {
    batchOfCustomers.foldLeft(0d)((acc, customer) => acc + customer.arrears)
  }

  def prepareNextRepetition(state: State) = {
    repeat
      .map(repeatInstructions => {
        if (state.time < repeatInstructions.finishTime) {
          state
            .addSystemAction(state.time + repeatInstructions.interval,
                             AddCustomers(UUID.randomUUID(),
                                          numberOfCustomers,
                                          startingDebt,
                                          Some(repeatInstructions)))
            .systemActions
        } else {
          state.systemActions
        }
      })
      .getOrElse(state.systemActions)
  }

}
