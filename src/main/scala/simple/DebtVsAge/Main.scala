package simple.DebtVsAge

import scalafx.application.JFXApp

object Main extends JFXApp {

  // generate customers with accounts that have arrears
  val sampleSize = 1000
  val averageDebt = 100
  val debtVarianceOverTime = DebtTimeVariance.IncLin

  // start the timing control off
  val totalTime = 100
  val interval = 10
  val batchSize = 100

  val inputData =
    Generator.customerGen(sampleSize,
                          averageDebt,
                          debtVarianceOverTime,
                          totalTime,
                          interval,
                          batchSize)

  val batches = Timer.timingRoutine(totalTime, interval, batchSize, inputData)
  val timeSeries = EventProcessor.eventRoutine(batches, totalTime, interval)

  View.graphResults(timeSeries)

}

case class Customer(account: Account)

case class Account(arrears: Double, age: Int)
