package simulator.db

import simulator.db.configuration._
import simulator.db.ml.{TestStorage, TrainStorage}
import simulator.db.model.ConfigurationData
import simulator.db.training._
import simulator.model._

object StorageImpl {

  val conStorage: ConfigurationStorage = new ConfigurationStorage("configuration")
  val simStorage: SimulationStorage = new SimulationStorage("configuration_simulation")
  val cusStorage: CustomerConfigurationStorage = new CustomerConfigurationStorage("configuration_customer")
  val actStorage: ActionConfigurationStorage = new ActionConfigurationStorage("configuration_action")
  val effStorage: EffectConfigurationStorage = new EffectConfigurationStorage("configuration_effect")
  val attStorage: AttributeConfigurationStorage = new AttributeConfigurationStorage("configuration_attribute")
  val oveStorage: AttributeConfigurationStorage = new AttributeConfigurationStorage("configuration_override")
  val scaStorage: ScalarConfigurationStorage = new ScalarConfigurationStorage("configuration_scalar")
  val catStorage: CategoricalConfigurationStorage = new CategoricalConfigurationStorage("configuration_categorical")
  val optStorage: OptionConfigurationStorage = new OptionConfigurationStorage("configuration_option")
  val repStorage: RepetitionConfigurationStorage = new RepetitionConfigurationStorage("configuration_repetition")

  val attributeStorage: AttributeStorage = new AttributeStorage("training_attribute")
  val customerStorage: CustomerStorage = new CustomerStorage("training_customer")
  val actionStorage: ActionStorage = new ActionStorage("training_action")
  val effectStorage: EffectStorage = new EffectStorage("training_effect")

  val train: TrainStorage = new TrainStorage("playing_train")
  val test: TestStorage = new TestStorage("playing_test")

  def initialiseTables(): Unit = {
    simStorage.init().unsafeRunSync()
    cusStorage.init().unsafeRunSync()
    actStorage.init().unsafeRunSync()
    effStorage.init().unsafeRunSync()
    attStorage.init().unsafeRunSync()
    oveStorage.init().unsafeRunSync()
    scaStorage.init().unsafeRunSync()
    catStorage.init().unsafeRunSync()
    optStorage.init().unsafeRunSync()
    repStorage.init().unsafeRunSync()

    attributeStorage.init().unsafeRunSync()
    customerStorage.init().unsafeRunSync()
    actionStorage.init().unsafeRunSync()
    effectStorage.init().unsafeRunSync()
  }

  def storeConfiguration(username: String, config: Configurations): Unit = {
    simStorage.write(config.simulationConfiguration)
    config.customerConfigurations.foreach(x => cusStorage.write(x))
    config.actionConfigurations.foreach(x => actStorage.write(x))
    config.effectConfigurations.foreach(x => effStorage.write(x))
    config.attributeConfigurations.foreach(x => attStorage.write(x))
    config.attributeOverrides.foreach(x => oveStorage.write(x))
    config.scalarConfigurations.foreach(x => scaStorage.write(x))
    config.categoricalConfigurations.foreach(x => catStorage.write(x))
    config.optionConfigurations.foreach(x => optStorage.write(x))
    config.repeatConfigurations.foreach(x => repStorage.write(x))
    conStorage.write(ConfigurationData(username, config.id))
  }

  def getConfiguration(username: String): Configurations = {
    ???
  }

  def storeTrainActions(data: List[Action]) = {
    // todo
  }

  def storeTrainingData(attributes: List[AttributeConfig], data: List[(Customer, Action)]) = {
    train.init(attributes)
    data.foreach { case (customer, action) => train.write(customer, action.name) }
  }

}
