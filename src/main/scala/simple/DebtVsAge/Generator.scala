package simple.DebtVsAge

import simple.DebtVsAge.model.{Account, Customer}

object Generator {

  def customerGen(time: Int, params: CustomerGeneratorParameters) =
    (for (_ <- 1 to params.batchSize) yield {
      val arrears = params.debtVarianceOverTime match {
        case DebtTimeVariance.None =>
          params.customerStartingDebt
        case DebtTimeVariance.Increase =>
          params.customerStartingDebt + (params.arrearsBias * time)
        case DebtTimeVariance.Decrease =>
          params.customerStartingDebt - (params.arrearsBias * time)
      }
      Customer(Account(arrears))
    }).toList

}
