package simple.DebtVsAge

import java.util.UUID

import simple.DebtVsAge.model.Actions.{AddCustomers, Repeat}
import simple.DebtVsAge.model.{ActionQueue, State}

object Main extends App {

  val customerGenerationParameters =
    CustomerGeneratorParameters(10, DebtTimeVariance.None, 1, 10)

  val runtimeDuration = 100

  val addCustomersEvery10SecondsFor100Seconds = State(
    actionQueue = ActionQueue()
      .addNewAction(0, AddCustomers(UUID.randomUUID(), Some(Repeat(10, 100)))))

  val snapshotInterval = 50

  Simulation(addCustomersEvery10SecondsFor100Seconds)

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
