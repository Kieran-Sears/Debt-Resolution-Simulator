package simulator.db.model
import java.util.UUID

case class ActionData(id: UUID, name: String, effects: List[UUID], repeat: Option[UUID], target: Option[UUID]) {}

case class CustomerData(
  id: UUID,
  name: String,
  attributes: List[UUID],
  difficulty: Option[Int] = None,
  assignedLabel: Option[Int] = None)
