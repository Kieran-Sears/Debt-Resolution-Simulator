package simulator.model

import akka.actor.ActorRef

case class RunSimulation(config: SimulationConfig)

case class UpdateState(state: State)

case class StateUpdated(state: State)

case class TickOnTime(previousTime: Int, newTime: Int, stopTime: Option[Int], originalSender: ActorRef)

case class TimeTickedOn(previousTime: Int, newTime: Int)

trait SimulationComplete

case class SimulationResults(batches: Map[String, Double], totals: Map[String, Double], aging: Map[String, Double]) extends SimulationComplete

case class SimulationError(reason: String) extends SimulationComplete