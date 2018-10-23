package simple.DebtVsAge.model

case class Statistics(batchArrears: Double = 0, totalArrears: Double = 0) {

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
