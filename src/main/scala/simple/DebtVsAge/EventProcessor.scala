package simple.DebtVsAge

object EventProcessor {

  def eventRoutine(batches: Seq[(Int, List[Customer])],
                   totalTime: Int,
                   interval: Int) = {

    def processBatch(time: Int,
                     customers: List[Customer],
                     previousStates: Seq[State]) = {

      def sumBatchArrears =
        customers.foldLeft(0d)((acc, customer) => {
          acc + customer.account.arrears
        })

      def sumTotalArrears =
        if (previousStates.nonEmpty)
          previousStates.last.stats.totalArrears + sumBatchArrears
        else sumBatchArrears

      def advanceAges(batchArrears: Double) = {
        customers.map(customer => {
          val updatedAccount =
            Account(arrears = customer.account.arrears, age = totalTime - time)
          Customer(updatedAccount)
        })
      }

      val batchArrears = sumBatchArrears
      advanceAges(batchArrears)
      State(time + "-" + (time + interval),
            Stats(batchArrears, sumTotalArrears),
            batches)

    }

    batches
      .foldLeft[Seq[State]](Seq.empty)((timeSeries, batch) =>
        timeSeries :+ processBatch(batch._1, batch._2, timeSeries))
      .toList

  }

}
