package simulator.db

import java.util.UUID

import cats.effect.IO
import simulator.db.ml._
import simulator.model._
import cats.implicits._
import simulator.db.model.{AttributeConfigData, CategoricalConfigData, OptionConfigData}

trait StorageError

trait StorageController {
  def initialiseStorageTables(): IO[Either[StorageError, Unit]]
  def initialiseTrainingTables(): IO[Either[StorageError, Unit]]
  def initialisePlayTables(attributes: List[AttributeConfig]): IO[Either[StorageError, Unit]]
  def storeConfiguration(username: String, config: Configurations): IO[Either[StorageError, Unit]]
  def getConfiguration(configId: UUID): IO[Either[StorageError, Configurations]]
  def storeTrainingData(data: TrainingData): IO[Either[StorageError, Unit]]
  def storePlayingData(
    attributes: List[AttributeConfig],
    data: List[(simulator.model.Customer, simulator.model.Action)],
    configurationId: UUID): IO[Either[StorageError, Unit]]
}

class StorageImpl extends StorageController {

  val simStorage: configuration.Simulation = new configuration.Simulation("configuration_simulation")
  val cusStorage: configuration.Customer = new configuration.Customer("configuration_customer")
  val actStorage: configuration.Action = new configuration.Action("configuration_action")
  val effStorage: configuration.Effect = new configuration.Effect("configuration_effect")
  val attStorage: configuration.Attribute = new configuration.Attribute("configuration_attribute")
  val scaStorage: configuration.Scalar = new configuration.Scalar("configuration_scalar")
  val catStorage: configuration.Categorical = new configuration.Categorical("configuration_categorical")
  val optStorage: configuration.Option = new configuration.Option("configuration_option")

  val attributeStorage: training.Attribute = new training.Attribute("training_attribute")
  val customerStorage: training.Customer = new training.Customer("training_customer")
  val actionStorage: training.Action = new training.Action("training_action")
  val effectStorage: training.Effect = new training.Effect("training_effect")

  val train: TrainStorage = new TrainStorage("playing_train")
  // val test: TestStorage = new TestStorage("playing_test")

  override def initialiseStorageTables(): IO[Either[StorageError, Unit]] = {
    println("initialiseStorageTables")

    for {
      _ <- simStorage.init()
      _ <- cusStorage.init()
      _ <- actStorage.init()
      _ <- effStorage.init()
      _ <- attStorage.init()
      _ <- scaStorage.init()
      _ <- catStorage.init()
      _ <- optStorage.init()
    } yield Right(Unit)

//      simStorage.init().unsafeRunSync()
//    cusStorage.init().unsafeRunSync()
//    actStorage.init().unsafeRunSync()
//    effStorage.init().unsafeRunSync()
//    attStorage.init().unsafeRunSync()
//    scaStorage.init().unsafeRunSync()
//    catStorage.init().unsafeRunSync()
//    optStorage.init().unsafeRunSync()
  }

  override def initialiseTrainingTables(): IO[Either[StorageError, Unit]] = {

    for {
      _ <- attributeStorage.init()
      _ <- customerStorage.init()
      _ <- actionStorage.init()
      _ <- effectStorage.init()
      send <- IO.apply(Unit)
    } yield Right(Unit)

//    println("initialiseTrainingTables")
//    attributeStorage.init().unsafeRunSync()
//    customerStorage.init().unsafeRunSync()
//    actionStorage.init().unsafeRunSync()
//    effectStorage.init().unsafeRunSync()
  }

  override def initialisePlayTables(attributes: List[AttributeConfig]): IO[Either[StorageError, Unit]] = {
    println("initialisePlayTables")
    for {
      _ <- train.init(attributes)
    } yield Right(Unit)
  }

  def storeAttributesByCustomer(customerId: UUID, attributes: List[UUID], config: Configurations): IO[List[Int]] = {
    val valueList = config.scalarConfigurations ++ config.categoricalConfigurations
    attributes
      .flatMap(a => config.attributeConfigurations.find(_.id == a))
      .map(attribute => {
        val value = valueList.filter(_.id == attribute.value)
        value.foreach {
          case scalar: ScalarConfig => scaStorage.write(scalar, config.id, attribute.id)
          case categorical: CategoricalConfig => {
            categorical.options.foreach(y => {
              val options = config.optionConfigurations.filter(_.id == y)
              options.foreach(option => optStorage.write(option, config.id, categorical.id))
            })
          }
        }
        attStorage.write(attribute, config.id, customerId)
      })
      .sequence
  }

  def storeEffectsByAction(config: Configurations): IO[List[Int]] = {
    config.actionConfigurations
      .map(action => {
        action.effectConfigurations.map(y => {
          val effects = config.effectConfigurations.filter(z => z.id == y)
          effects.map(effect => effStorage.write(effect, config.id, action.id))
        })
        actStorage.write(action, config.id)
      })
      .sequence
  }

  override def storeConfiguration(username: String, config: Configurations): IO[Either[StorageError, Unit]] = {
    for {
      _ <- simStorage.write(config.simulationConfiguration, config.id)
      customers <- IO(config.customerConfigurations)
      _ <- customers.map(c => storeAttributesByCustomer(c.id, c.attributeOverrides, config)).sequence
      _ <- storeEffectsByAction(config)
    } yield Right(Unit)
  }

  def getPrimaryConfigurations(configId: UUID): IO[(SimulationConfig, List[ActionConfig], List[CustomerConfig])] = {
    for {
      simulation <- simStorage.readByOwnerId(configId)
      cd <- cusStorage.readByOwnerId(configId)
      customers <- IO(cd.map(x => CustomerConfig(x.id, x.name, Nil, x.proportion)))
      ad <- actStorage.readByOwnerId(configId)
      actions <- IO(ad.map(x => ActionConfig(x.id, x.name, x.actionType, Nil)))
    } yield (simulation, actions, customers)
  }

  def loadEffectIdsIntoAction(
    actions: List[ActionConfig],
    effects: List[List[EffectConfig]]): IO[List[ActionConfig]] = {
    IO(actions.zip(effects).map {
      case (action: ActionConfig, effects: List[EffectConfig]) => action.copy(effectConfigurations = effects.map(_.id))
    })
  }

  def loadAttributesIntoCustomer(
    customers: List[CustomerConfig],
    attributes: List[List[AttributeConfigData]]): IO[List[CustomerConfig]] = {
    IO(customers.zip(attributes).map {
      case (customer: CustomerConfig, attributes: List[AttributeConfigData]) =>
        customer.copy(attributeOverrides = attributes.map(_.id))
    })
  }

  def loadOptionsIntoCategorical(
    categoricals: List[CategoricalConfigData],
    options: List[List[OptionConfigData]]): IO[List[CategoricalConfig]] = {
    IO(categoricals.zip(options).map {
      case (categorical: CategoricalConfigData, optionList: List[OptionConfigData]) => {
        CategoricalConfig(categorical.id, optionList.map(_.id))
      }
    })
  }

  override def getConfiguration(configId: UUID): IO[Either[StorageError, Configurations]] = {
    val (simulation, rawActions, rawCustomers) = getPrimaryConfigurations(configId).unsafeRunSync()
    for {
      // (simulation, rawActions, rawCustomers) <- IO(primaryConfigs)
      effects <- rawActions.map(action => effStorage.readByOwnerId(action.id)).sequence
      actions <- loadEffectIdsIntoAction(rawActions, effects)
      rawAttributes <- rawCustomers
        .map(customer => attStorage.readByOwnerId(customer.id))
        .sequence
      customers <- loadAttributesIntoCustomer(rawCustomers, rawAttributes)
      rawScalars <- rawAttributes.flatten.map(attribute => scaStorage.readByOwnerId(attribute.id)).sequence
      scalars <- IO(rawScalars.map(s => ScalarConfig(s.id, s.variance_type, s.min, s.max)))
      rawCategoricals <- rawAttributes.flatten
        .map(attribute => catStorage.readByOwnerId(attribute.id))
        .sequence
      rawOptions <- rawCategoricals.map(categorical => optStorage.readByOwnerId(categorical.id)).sequence
      options <- IO(rawOptions.flatten.map(o => OptionConfig(o.id, o.name, o.probability)))
      categoricals <- loadOptionsIntoCategorical(rawCategoricals, rawOptions)
      attributes <- IO(rawAttributes.flatten.map(attribute =>
        AttributeConfig(attribute.id, attribute.name, attribute.value, attribute.attributeType)))
    } yield
      Right(
        Configurations(
          configId,
          customers,
          actions,
          effects.flatten,
          attributes,
          scalars,
          categoricals,
          options,
          simulation))
  }

  override def storeTrainingData(data: TrainingData): IO[Either[StorageError, Unit]] = {
    println("storeTrainingData")
    for {
      _ <- data.actions
        .flatMap(action => action.effects.map(effect => effectStorage.write(effect, data.configurationId, action.id)))
        .sequence
      _ <- data.customers
        .flatMap(customer =>
          customer.attributes.map(attribute => attributeStorage.write(attribute, data.configurationId, customer.id)))
        .sequence
      _ <- data.actions.map(x => actionStorage.write(x, data.configurationId)).sequence
      _ <- data.customers.map(x => customerStorage.write(x, data.configurationId)).sequence
    } yield Right(Unit)
  }

  override def storePlayingData(
    attributes: List[AttributeConfig],
    data: List[(simulator.model.Customer, simulator.model.Action)],
    configurationId: UUID): IO[Either[StorageError, Unit]] = {
    println("storePlayingData")
    for {
      _ <- initialisePlayTables(attributes.filter(att => att.attributeType == AttributeEnum.Global))
      _ <- data.map { case (customer, action) => train.write(customer, action.name, configurationId) }.sequence
    } yield Right(Unit)
  }

}
