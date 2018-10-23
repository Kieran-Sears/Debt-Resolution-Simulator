package simple.DebtVsAge.model.Actions

import java.util.UUID

import simple.DebtVsAge.model._
import simple.DebtVsAge.{DebtTimeVariance, Main}

case class AddCustomers(actionId: UUID, repeat: Option[Repeat]) extends Action {

  override def perform(currentState: State) = {

    val newBatchOfCustomers = makeBatchOfCustomers(currentState.time)

    val batchArrears = calculateSumOfBatchArrears(newBatchOfCustomers)

    val newTotalArrears = batchArrears + currentState.stats.totalArrears

    val newStats = Statistics(batchArrears, newTotalArrears)

    val removedAction = currentState.actionQueue
      .removeActionFromActionQueue(currentState.time, actionId)

    val updatedActionQueue = repeatAction(currentState.time, removedAction)

    val newState = State(currentState.time,
                         newStats,
                         updatedActionQueue,
                         currentState.history :+ currentState)

    val nextActionTime = repeat
      .map(rep => currentState.time + rep.interval)
      .getOrElse(-1)

    (nextActionTime, newState)
  }

  def makeBatchOfCustomers(currentTime: Int) = {
    val params = Main.customerGenerationParameters
    (for (_ <- 1 to params.batchSize) yield {
      val arrears = params.debtVarianceOverTime match {
        case DebtTimeVariance.None =>
          params.customerStartingDebt
        case DebtTimeVariance.Increase =>
          params.customerStartingDebt + (params.arrearsBias * currentTime)
        case DebtTimeVariance.Decrease =>
          params.customerStartingDebt - (params.arrearsBias * currentTime)
      }
      Customer(Account(arrears))
    }).toList
  }

  def calculateSumOfBatchArrears(batchOfCustomers: List[Customer]) = {
    batchOfCustomers.foldLeft(0d)((acc, customer) =>
      acc + customer.account.arrears)
  }

  def repeatAction(currentTime: Int, actionQueue: ActionQueue): ActionQueue = {
    repeat
      .map(repeatInstructions => {
        if (currentTime < repeatInstructions.finishTime) {
          actionQueue.addNewAction(currentTime + repeatInstructions.interval,
                                   AddCustomers(UUID.randomUUID(),
                                                Some(repeatInstructions)))
        } else {
          actionQueue
        }
      })
      .getOrElse(actionQueue)
  }

}
