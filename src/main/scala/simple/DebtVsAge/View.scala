package simple.DebtVsAge

import simple.DebtVsAge.model.State

object View {

  def displaySimulationResults(state: State) = {
    println(getStats(state))
  }

  def getStats(currentState: State) = {
    (("batches",
      currentState.history
        .foldLeft[List[(String, Double)]](Nil)((acc, state: State) =>
          acc :+ (timeToString(state.time), state.stats.batchArrears))),
     ("totals",
      currentState.history
        .foldLeft[List[(String, Double)]](Nil)((acc, state: State) =>
          acc :+ (timeToString(state.time), state.stats.totalArrears))),
     ("aging",
      currentState.history
        .map(state => timeToString(state.time))
        .zip((currentState.history :+ currentState).reverse
          .map(ts => ts.stats.batchArrears))))
  }

  def timeToString(time: Int) = time.toString + "-" + (time + 10)
}
