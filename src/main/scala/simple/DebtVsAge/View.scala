package simple.DebtVsAge

import simple.DebtVsAge.model.State

object View {

  def displaySimulationResults(state: State) = {
    println(state.stats)
  }

}
