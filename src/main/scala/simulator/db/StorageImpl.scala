package simulator.db

import java.util.UUID

import cats.effect.IO
import simulator.db.configuration._
import simulator.db.ml.{TestStorage, TrainStorage}
import simulator.db.model.CategoricalConfigData
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

  val attributeStorage: AttributeStorage = new AttributeStorage("training_attribute")
  val customerStorage: CustomerStorage = new CustomerStorage("training_customer")
  val actionStorage: ActionStorage = new ActionStorage("training_action")
  val effectStorage: EffectStorage = new EffectStorage("training_effect")

  val train: TrainStorage = new TrainStorage("playing_train")
  val test: TestStorage = new TestStorage("playing_test")

  def initialiseStorageTables(): Unit = {
    println("initialiseStorageTables")
    simStorage.init().unsafeRunSync()
    cusStorage.init().unsafeRunSync()
    actStorage.init().unsafeRunSync()
    effStorage.init().unsafeRunSync()
    attStorage.init().unsafeRunSync()
    scaStorage.init().unsafeRunSync()
    catStorage.init().unsafeRunSync()
    optStorage.init().unsafeRunSync()
  }

  def initialiseTrainingTables(): Unit = {
    println("initialiseTrainingTables")
    attributeStorage.init().unsafeRunSync()
    customerStorage.init().unsafeRunSync()
    actionStorage.init().unsafeRunSync()
    effectStorage.init().unsafeRunSync()
  }

  def initialisePlayTables(attributes: List[AttributeConfig]): Unit = {
    println("initialisePlayTables")
    train.init(attributes)
  }

  def storeConfiguration(username: String, config: Configurations): Unit = {
    println("storeConfiguration")

    val id = config.id
    val valueList = config.scalarConfigurations ++ config.categoricalConfigurations
    simStorage.write(config.simulationConfiguration, config.id).unsafeRunSync()

    config.customerConfigurations.foreach(customer => {
      customer.attributeOverrides.foreach(y => {
        val attributes = config.attributeConfigurations.filter(z => z.id == y)
        attributes.foreach(attribute => {
          val value = valueList.filter(_.id == attribute.value)
          value.foreach {
            case scalar: ScalarConfig => scaStorage.write(scalar, id, attribute.id)
            case categorical: CategoricalConfig => {
              categorical.options.foreach(y => {
                val options = config.optionConfigurations.filter(_.id == y)
                options.foreach(option => optStorage.write(option, id, categorical.id))
              })
            }
          }
          attStorage.write(attribute, id, customer.id)
        })
      })
      cusStorage.write(customer, id).unsafeRunSync()
    })

    config.actionConfigurations.foreach(action => {
      action.effectConfigurations.foreach(y => {
        val effects = config.effectConfigurations.filter(z => z.id == y)
        effects.foreach(effect => effStorage.write(effect, id, action.id).unsafeRunSync())
      })
      actStorage.write(action, id).unsafeRunSync()
    })
  }

  def getConfiguration(configId: UUID): Configurations = {

    println("getConfiguration")

    val x: IO[(SimulationConfig, List[ActionConfig], List[CustomerConfig])] = for {
      simulation <- simStorage.readByConfigurationId(configId)
      _ = println(simulation)
      cd <- cusStorage.readByConfigurationId(configId)
      customers <- IO(cd.map(x => CustomerConfig(x.id, x.name, Nil, x.proportion)))
      _ = println(customers)
      ad <- actStorage.readByConfigurationId(configId)
      actions <- IO(ad.map(x => ActionConfig(x.id, x.name, x.actionType, Nil)))
      _ = println(actions)
    } yield (simulation, actions, customers)

    val (simulation, acts, custs) = x.unsafeRunSync()

    val effsAndActs = acts.map(action => {
      val es = effStorage.readByActionId(action.id).unsafeRunSync()
      println(es)
      val a = action.copy(effectConfigurations = es.map(_.id))
      (es, a)
    })

    val actions = effsAndActs.map(_._2)
    val effects = effsAndActs.flatMap(_._1)

    val attsAndCusts = custs.map(customer => {
      val as = attStorage.readByCustomerId(customer.id).unsafeRunSync()
      println(as)
      val c = customer.copy(attributeOverrides = as.map(_.id))
      (as, c)
    })

    val customers = attsAndCusts.map(_._2)
    val attributes = attsAndCusts.flatMap(_._1)

    val scalsAndAtts = attributes.map(attribute => {
      val scs = scaStorage.readByAttributeId(attribute.id).unsafeRunSync()
      val a = attribute.copy(value = scs.id)
      (scs, a)
    })

    val scalars = scalsAndAtts.map(x => {
      val b = x._1
      ScalarConfig(b.id, b.variance_type, b.min, b.max)
    })

    val catsAndAtts = attributes.map(attribute => {
      val cat = catStorage.readByAttributeId(attribute.id).unsafeRunSync()
      val a = attribute.copy(value = cat.id)
      (cat, a)
    })

    val catStore: List[CategoricalConfigData] = catsAndAtts.map(_._1)

    val optsAndCats: List[(List[OptionConfig], CategoricalConfig)] =
      catStore.map(categorical => {
        val opts = optStorage.readByCategoricalId(categorical.id).unsafeRunSync()
        val optio = opts.map(x => OptionConfig(x.id, x.name, x.probability))
        val o = CategoricalConfig(id = categorical.id, options = opts.map(_.id))
        (optio, o)
      })

    val ggg = (scalsAndAtts ++ catsAndAtts).map(x => {
      val b = x._2
      AttributeConfig(b.id, b.name, b.value, b.attributeType)
    })

    val categoricals = optsAndCats.map(_._2)
    val options: List[OptionConfig] = optsAndCats.flatMap(_._1)

    Configurations(configId, customers, actions, effects, ggg, scalars, categoricals, options, simulation)
  }

  def storeTrainingData(data: TrainingData) = {
    println("storeTrainingData")
    data.actions.foreach(action =>
      action.effects.foreach(effect => StorageImpl.effectStorage.write(effect, data.configurationId, action.id)))

    data.customers.foreach(customer =>
      customer.attributes.foreach(attribute =>
        StorageImpl.attributeStorage.write(attribute, data.configurationId, customer.id)))

    data.actions.foreach(x => actionStorage.write(x, data.configurationId))

    data.customers.foreach(x => customerStorage.write(x, data.configurationId))
  }

  def storePlayingData(attributes: List[AttributeConfig], data: List[(Customer, Action)], configurationId: UUID) = {
    println("storePlayingData")
    initialisePlayTables(attributes.filter(att => att.attributeType == AttributeEnum.Global))
    data.foreach { case (customer, action) => train.write(customer, action.name, configurationId) }
  }

}
