package simulator.model

object Stats {

  def getAllOutcomes(
    customers: List[Customer],
    labelledData: TrainingData): List[(Customer, List[(Action, Customer)])] =
    customers.map { c: Customer =>
      (c, labelledData.actions.foldLeft(List[(Action, Customer)]()) {
        case (acc, a) => if (a.getTarget.name == c.name) acc :+ (a, a.processCustomer(c)) else acc
      })
    }

  def getIdealMappings(allOutcomes: List[(Customer, List[(Action, Customer)])]): List[(Customer, Action)] =
    allOutcomes.map {
      case (customer, processedList) => {
        val bestActionCustomer = processedList.foldLeft(processedList.head) {
          case ((x: Action, y: Customer), (a: Action, c: Customer)) => {
            if (Stats.compareCustomers(y, c) == c) (a, c) else (x, y)
          }
        }
        (customer, bestActionCustomer._1)
      }
    }

  def compareCustomers(cus1: Customer, cus2: Customer): Customer = {
    // arrears is generally more important as decided by a ratio.
    // find out what percent each value is:
    // - add values together to make 100%, divide numbers by one percent
    // apply ratio to percentages
    // - combine ratios for denominator
    // - divide numerator by denominator to get multiplier
    // - multiply percentage by multiplier for weighted result

    // ratio 3 : 7 = satisfaction : arrears

    // swapped because we are looking for minima not maxima for arrears
    val arrearsPercent = percentages(cus1.getArrears.value, cus2.getArrears.value).swap
    val satisfactionPercent = percentages(cus1.getSatisfaction.value, cus2.getSatisfaction.value)

    val c1ArrearsRatio = ratio(arrearsPercent._1, 3, 10)
    val c2ArrearsRatio = ratio(arrearsPercent._2, 3, 10)

    val customer1Score = c1ArrearsRatio + ratio(satisfactionPercent._1, 7, 10)
    val customer2Score = c2ArrearsRatio + ratio(satisfactionPercent._2, 7, 10)

    def scoreTie(score1: Double, score2: Double) =
      if (score1 == score2) cus1 else bestScore(score1, score2)

    def bestScore(score1: Double, score2: Double) =
      if (score1 > score2) cus1 else cus2

    if (customer1Score == customer2Score)
      scoreTie(c1ArrearsRatio, c2ArrearsRatio)
    else
      bestScore(customer1Score, customer2Score)
  }

  def percentages(value1: Double, value2: Double) = {
    val total = value1 + value2
    val onePercent = total / 100
    val v1Percent = value1 / onePercent
    val v2Percent = value2 / onePercent
    (v1Percent, v2Percent)
  }

  def ratio(num: Double, numerator: Double, denominator: Double) = {
    num * (numerator / denominator)
  }

}

case class Statistics(batchArrears: Double = 0, totalArrears: Double = 0)
