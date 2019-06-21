package simulator.db

import java.util.UUID

import simulator.db.configuration._
import simulator.db.ml.{TestStorage, TrainStorage}
import simulator.db.training._
import simulator.model._

object StorageImpl {

  val simStorage: SimulationStorage = new SimulationStorage("configuration_simulation")
  val cusStorage: CustomerConfigurationStorage = new CustomerConfigurationStorage("configuration_customer")
  val actStorage: ActionConfigurationStorage = new ActionConfigurationStorage("configuration_action")
  val effStorage: EffectConfigurationStorage = new EffectConfigurationStorage("configuration_effect")
  val attStorage: AttributeConfigurationStorage = new AttributeConfigurationStorage("configuration_attribute")
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

  def initialiseStorageTables(): Unit = {
    simStorage.init().unsafeRunSync()
    cusStorage.init().unsafeRunSync()
    actStorage.init().unsafeRunSync()
    effStorage.init().unsafeRunSync()
    attStorage.init().unsafeRunSync()
    scaStorage.init().unsafeRunSync()
    catStorage.init().unsafeRunSync()
    optStorage.init().unsafeRunSync()
    repStorage.init().unsafeRunSync()
  }

  def initialiseTrainingTables(): Unit = {
    attributeStorage.init().unsafeRunSync()
    customerStorage.init().unsafeRunSync()
    actionStorage.init().unsafeRunSync()
    effectStorage.init().unsafeRunSync()
  }

  def initialisePlayTables(attributes: List[AttributeConfig]): Unit = {
    train.init(attributes)
  }

  def storeConfiguration(username: String, config: Configurations): Unit = {
    val id = config.id
    simStorage.write(config.simulationConfiguration, config.id).unsafeRunSync()
    config.customerConfigurations.foreach(x => cusStorage.write(x, id).unsafeRunSync())
    config.actionConfigurations.foreach(x => actStorage.write(x, id).unsafeRunSync())
    config.effectConfigurations.foreach(x => effStorage.write(x, id).unsafeRunSync())
    config.attributeConfigurations.foreach(x => attStorage.write(x, id).unsafeRunSync())
    config.scalarConfigurations.foreach(x => scaStorage.write(x, id).unsafeRunSync())
    config.categoricalConfigurations.foreach(x => catStorage.write(x, id).unsafeRunSync())
    config.optionConfigurations.foreach(x => optStorage.write(x, id).unsafeRunSync())
    config.repeatConfigurations.foreach(x => repStorage.write(x, id).unsafeRunSync())
  }

  def getConfiguration(configId: UUID): Configurations = {
    val x = for {
      simulation <- simStorage.readByConfigurationId(configId)
      customers <- cusStorage.readByConfigurationId(configId)
      actions <- actStorage.readByConfigurationId(configId)
      effects <- effStorage.readByConfigurationId(configId)
      attributes <- attStorage.readByConfigurationId(configId)
      scalars <- scaStorage.readByConfigurationId(configId)
      categoricals <- catStorage.readByConfigurationId(configId)
      options <- optStorage.readByConfigurationId(configId)
      repeats <- repStorage.readByConfigurationId(configId)
    } yield
      Configurations(
        configId,
        customers,
        actions,
        effects,
        attributes,
        scalars,
        categoricals,
        options,
        repeats,
        simulation.head
      )

    x.unsafeRunSync()
  }

  def storeTrainingData(data: TrainingData) = {
    val effectData = data.actions.flatMap(a => a.effects)
    val attributeData = data.customers.flatMap(c => c.attributes)
    data.actions.foreach(x => actionStorage.write(x, data.configurationId))
    data.customers.foreach(x => customerStorage.write(x, data.configurationId))
    effectData.foreach(StorageImpl.effectStorage.write(_, data.configurationId))
    attributeData.foreach(StorageImpl.attributeStorage.write(_, data.configurationId))
  }

  def storePlayingData(attributes: List[AttributeConfig], data: List[(Customer, Action)], configurationId: UUID) = {
    initialisePlayTables(attributes.filter(att => att.attributeType == AttributeEnum.Global))
    data.foreach { case (customer, action) => train.write(customer, action.name, configurationId) }
  }

}
