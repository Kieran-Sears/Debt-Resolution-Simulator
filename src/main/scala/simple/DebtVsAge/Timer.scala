package simple.DebtVsAge

object Timer {

  def timingRoutine(totalTime: Int,
                    interval: Int,
                    batchSize: Int,
                    inputData: List[Customer]) = {

    for (time <- 0 to totalTime - interval if time % interval == 0)
      yield (time, inputData.slice(time, time + interval))

  }

}
