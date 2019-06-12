package simulator
import simulator.model.{Action, TrainingData}

class TrainingGenerator(seed: Int) {

  def actualise(data: List[TrainingData]): Unit = {

    // TODO WIP: Plan out the implementation of the action generation (requires config which needs to be
    //  obtained through database not yet implemented)

    // we are making actions:
    // To make an action we need to know who the customer is going to be and the actions
    // List[customer] List[Action]
    // using each [customer] we can get each attribute for that customer and stack those attributes together
    // List[customer] -> Customer[List[Attribute], List[Attribute], List[Attribute]]... etc till end of attributes
    // for each attribute we need the action effects that "effect" it
    // [Customer.Attribute] => List[Action.Effect]
    // once we have this we can
    // need an algorithm to say: When customer is "this" action does "this"
    // actionEffects = foreach effect -> map (customer -> action.effect.value)
  }

}

object TrainingGenerator {
  val default = new TrainingGenerator(0)
}
