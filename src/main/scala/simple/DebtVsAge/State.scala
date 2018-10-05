package simple.DebtVsAge

case class State(time: String,
                 stats: Stats,
                 batches: Seq[(Int, List[Customer])])

case class Stats(batchArrears: Double, totalArrears: Double)
