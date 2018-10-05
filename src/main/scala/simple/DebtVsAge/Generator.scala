package simple.DebtVsAge

import scala.util.Random

object Generator {

  def customerGen(quantity: Int,
                  averageDebt: Double,
                  timeVariance: DebtTimeVariance.Value,
                  totalTime: Int,
                  interval: Int,
                  batchSize: Int): List[Customer] = {

    (for (i <- 1 to quantity) yield {
      val weight = i * (quantity / batchSize)
      val arrears = timeVariance match {
        case DebtTimeVariance.None =>
          Random
            .nextGaussian() * 10 + averageDebt
        case DebtTimeVariance.IncLin =>
          Random
            .nextGaussian() * 10 + averageDebt + weight
        case DebtTimeVariance.DecLin =>
          Random
            .nextGaussian() * 10 + averageDebt - weight
      }
      Customer(Account(arrears = arrears, age = 0))
    }).toList

  }

}

object DebtTimeVariance extends Enumeration {
  val IncLin, DecLin, None = Value
}
