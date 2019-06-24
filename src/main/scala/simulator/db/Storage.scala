package simulator.db
import java.util.UUID
import java.util.concurrent.Executors

import cats.effect.{ContextShift, IO}
import com.typesafe.scalalogging.LazyLogging
import doobie.util.ExecutionContexts
import doobie.util.fragment.Fragment
import doobie.util.transactor.Transactor
import doobie.implicits._
import simulator.Settings
import simulator.db.model.ConfigDbData
import simulator.model.{Config, Train}

import scala.concurrent.ExecutionContext

trait Storage extends MetaMapping with LazyLogging {

  val tableName: String

  val tableNameFragment = Fragment.const(s"$tableName")

  def indexName(name: String) = Fragment.const(s"${tableName}_$name")

  implicit def contextShift: ContextShift[IO] =
    IO.contextShift(ExecutionContext.fromExecutor(Executors.newFixedThreadPool(4)))

  val settings = Settings().DatabaseSettings

  val xa: Transactor[IO] = Transactor.fromDriverManager[IO](
    settings.driver,
    settings.simulatorUrl,
    settings.user,
    Settings().SecretSettings.dbSecret,
    ExecutionContexts.synchronous // just for testing
  )

  implicit val dbUrl: String = Settings().DatabaseSettings.simulatorUrl

}

trait ConventionalStorage extends Storage {
  def init(): IO[Int]

  def drop(): IO[Int] =
    for {
      queryResult <- (sql"DROP TABLE IF EXISTS " ++ tableNameFragment ++ sql";").update.run
        .transact(xa)
    } yield queryResult

  def readByOwnerId(id: UUID): IO[Any]
}

trait PrimaryConfigurationStorage extends ConventionalStorage {
  def write(model: Config, configurationId: UUID): IO[Int]
}

trait SecondaryConfigurationStorage extends ConventionalStorage {
  def write(config: Config, configurationId: UUID, owner: UUID): IO[Int]
}

trait PrimaryTrainingStorage extends ConventionalStorage {
  def write(train: Train, configurationId: UUID): IO[Int]
}

trait SecondaryTrainingStorage extends ConventionalStorage {
  def write(train: Train, configurationId: UUID, owner: UUID): IO[Int]
}
