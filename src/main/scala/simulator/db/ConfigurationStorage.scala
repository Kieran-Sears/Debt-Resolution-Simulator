package simulator.db
import java.util.concurrent.Executors

import cats.effect.{ContextShift, IO}
import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor
import simulator.Settings
import simulator.db.configuration._
import simulator.model.Configurations

import scala.concurrent.ExecutionContext

object ConfigurationStorage {

  implicit def contextShift: ContextShift[IO] =
    IO.contextShift(ExecutionContext.fromExecutor(Executors.newFixedThreadPool(4)))

  implicit val xa: Transactor[IO] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver", // driver classname
    "jdbc:postgresql:postgres", // connect URL (driver-specific)
    "postgres", // user
    Settings().DatabaseSettings.password, // password
    ExecutionContexts.synchronous // just for testing
  )

  val dbUrl: String = Settings().DatabaseSettings.databaseUrl
  val tableName = s"configuration"

  private val simStorage: SimulationStorage = new SimulationStorage(dbUrl, tableName, xa)
  private val cusStorage: CustomerConfigurationStorage = new CustomerConfigurationStorage(dbUrl, tableName, xa)
  private val actStorage: ActionConfigurationStorage = new ActionConfigurationStorage(dbUrl, tableName, xa)
  private val effStorage: EffectConfigurationStorage = new EffectConfigurationStorage(dbUrl, tableName, xa)
  private val attStorage: AttributeConfigurationStorage = new AttributeConfigurationStorage(dbUrl, tableName, xa)
  private val scaStorage: ScalarConfigurationStorage = new ScalarConfigurationStorage(dbUrl, tableName, xa)
  private val catStorage: CategoricalConfigurationStorage = new CategoricalConfigurationStorage(dbUrl, tableName, xa)
  private val optStorage: OptionConfigurationStorage = new OptionConfigurationStorage(dbUrl, tableName, xa)
  private val repStorage: RepetitionConfigurationStorage = new RepetitionConfigurationStorage(dbUrl, tableName, xa)

  def initialiseTables(): Unit = {
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

  def storeConfiguration(config: Configurations): Unit = {
    simStorage.write(config.simulationConfiguration)
    config.customerConfigurations.foreach(x => cusStorage.write(x))
    config.actionConfigurations.foreach(x => actStorage.write(x))
    config.effectConfigurations.foreach(x => effStorage.write(x))
    config.attributeConfigurations.foreach(x => attStorage.write(x))
    config.scalarConfigurations.foreach(x => scaStorage.write(x))
    config.categoricalConfigurations.foreach(x => catStorage.write(x))
    config.optionConfigurations.foreach(x => optStorage.write(x))
    config.repeatConfigurations.foreach(x => repStorage.write(x))
    simStorage.write(config.simulationConfiguration)
  }

}
