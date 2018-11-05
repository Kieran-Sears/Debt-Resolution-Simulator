package simple.DebtVsAge.model.Actions

import java.util.UUID

import simple.DebtVsAge.model._

case class AddCustomers(actionId: UUID, repeat: Option[Repeat], config: CustomerGenConfig) extends Action {

  override def perform(currentState: State) = {

    val newBatchOfCustomers = makeBatchOfCustomers(currentState.time)

    val batchArrears = calculateSumOfBatchArrears(newBatchOfCustomers)

    val newTotalArrears = batchArrears + currentState.stats.totalArrears

    val newStats = Statistics(batchArrears, newTotalArrears)

    val queueWithActionRemoved = ActionQueue
      .removeActionFromActionQueue(currentState.time,
                                   actionId,
                                   currentState.actionQueue)

    val updatedQueue = repeatAction(currentState.time, queueWithActionRemoved)

    val newState =
      State(currentState.time,
            newStats,
            updatedQueue,
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

  def repeatAction(currentTime: Int, queue: Map[Int, List[Action]]) = {
    repeat
      .map(repeatInstructions => {
        if (currentTime < repeatInstructions.finishTime) {
          ActionQueue.addNewAction(currentTime + repeatInstructions.interval,
                                   AddCustomers(UUID.randomUUID(),
                                                Some(repeatInstructions), config),
                                   queue)
        } else {
          queue
        }
      })
      .getOrElse(queue)
  }

}
