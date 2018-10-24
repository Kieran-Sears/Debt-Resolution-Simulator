package simple.DebtVsAge

import java.util.UUID

import simple.DebtVsAge.model.Actions.{AddCustomers, Repeat}
import simple.DebtVsAge.model.{ActionQueue, State}

object Main extends App {

  val customerGenerationParameters =
    CustomerGeneratorParameters(10, DebtTimeVariance.None, 1, 10)

  val initQueue = ActionQueue
    .addNewAction(0,
                  AddCustomers(UUID.randomUUID(), Some(Repeat(10, 100))),
                  Map())

  val snapshotInterval = 50

  Simulation(State(actionQueue = initQueue))

}

case class CustomerGeneratorParameters(
    customerStartingDebt: Double,
    debtVarianceOverTime: DebtTimeVariance.Value,
    arrearsBias: Double,
    batchSize: Int,
)

object DebtTimeVariance extends Enumeration {
  val Increase, Decrease, None = Value
}
