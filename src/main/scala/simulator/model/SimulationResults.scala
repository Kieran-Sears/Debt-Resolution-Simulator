package simulator.model

case class SimulationResults(batches: Map[Int, Double], totals: Map[Int, Double], aging: Map[Int, Double])