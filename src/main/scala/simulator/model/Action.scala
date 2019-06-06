package simulator.model

import java.util.UUID

case class Repeat(interval: Int, finishTime: Int)

trait SystemAction {
  val actionId: UUID
  val repeat: Option[Repeat]
  val kind: String
  def perform(state: State): State

}

trait CustomerAction {
  val actionId: UUID
  val customerId: UUID
  val repeat: Option[Repeat]
  val kind: String
  def perform(state: State): State

}

trait AgentAction {
  val actionId: UUID
  val repeat: Option[Repeat]
  val kind: String
  def perform(state: State): State

}

case class ActionConfig(
  id: String,
  `type`: ActionType.Value,
  effectConfigurations: List[String] = Nil,
  kind: String = "action"
)

case class EffectConfig(
  id: String,
  `type`: EffectType.Value,
  target: String,
  kind: String = "effect"
)
