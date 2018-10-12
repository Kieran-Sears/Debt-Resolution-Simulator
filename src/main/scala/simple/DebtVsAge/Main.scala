package simple.DebtVsAge

import scalafx.application.JFXApp

object Main extends JFXApp {

  val custGenParams =
    CustomerGeneratorParameters(10, DebtTimeVariance.None, 1, 10)

  val totalSimulationRunTime = 100
  val interval = 10

  var currentTime = 0
  var currentState = State(0, Stats(0, 0, totalSimulationRunTime), Nil)

  while (currentTime <= totalSimulationRunTime) {

    val newBatchOfCustomers = Generator.customerGen(currentTime, custGenParams)

    val batchArrears = newBatchOfCustomers.foldLeft(0d)((acc, customer) =>
      acc + customer.account.arrears)

    val totalArrears = batchArrears + currentState.stats.totalArrears

    val batchAge = currentTime

    val newStats = Stats(batchArrears, totalArrears, batchAge)

    currentState =
      State(currentTime, newStats, currentState.history :+ currentState)

    currentTime = currentTime + interval
  }

  View.initialiseView(currentState)
}

case class Customer(account: Account)

case class Account(arrears: Double)

case class State(time: Int, stats: Stats, history: List[State])

case class Stats(batchArrears: Double, totalArrears: Double, batchAge: Int)

case class CustomerGeneratorParameters(
    customerStartingDebt: Double,
    debtVarianceOverTime: DebtTimeVariance.Value,
    arrearsBias: Double,
    batchSize: Int,
)

object DebtTimeVariance extends Enumeration {
  val Increase, Decrease, None = Value
}
