package db

class NoOpStorageImpl {}

import java.util.UUID

import cats.Monad
import cats.effect.IO
import simulator.db._
import simulator.model.{Config, Train}

class NoOpConfigurationStorageImpl
  extends PrimaryConfigurationStorage
  with SecondaryConfigurationStorage
  with PrimaryTrainingStorage
  with SecondaryTrainingStorage {
  val IO = Monad[IO]

  val tableName = "testTableName"

  def init(): IO[Int] = IO.pure(0)

  def readByOwnerId(id: UUID): IO[Any] = IO.pure(Nil)

  def write(model: Config, configurationId: UUID): IO[Int] = IO.pure(0)

  def write(config: Config, configurationId: UUID, owner: UUID): IO[Int] = IO.pure(0)

  def write(train: Train, configurationId: UUID): IO[Int] = IO.pure(0)

  def write(train: Train, configurationId: UUID, owner: UUID): IO[Int] = IO.pure(0)
}
