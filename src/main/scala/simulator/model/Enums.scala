package simulator.model

object Variance extends Enumeration {
  val Increase, Decrease, None = Value
}

object EffectType extends Enumeration {
  val Effect, Affect = Value
}

object ActionType extends Enumeration {
  val Customer, Agent = Value
}
