package simulator.model.Actions

import java.util.UUID

import simulator.model.{Config, State}

case class Repeat(interval: Int, finishTime: Int)

abstract class Action {
  val actionId: UUID
  val repeat: Option[Repeat]
  val config: Config
  val kind: String
  def perform(state: State): (Int, State)
}
