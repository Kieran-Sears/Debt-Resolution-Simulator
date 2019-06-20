package simulator.db.training

import java.util.UUID
import cats.effect.IO
import doobie.implicits._
import simulator.db.model.CustomerData
import simulator.db.Storage
import simulator.model.Customer
import doobie.postgres._
import doobie.postgres.implicits._

class CustomerStorage(override val tableName: String) extends Storage {

  def init(): IO[Int] = {
    logger.info(s"Initiliasing Database Table $tableName at $dbUrl")
    for {
      queryResult <- (sql"""
      CREATE TABLE IF NOT EXISTS """ ++ tableNameFragment ++
        sql""" (
        id UUID PRIMARY KEY NOT NULL UNIQUE,
        configuration_id UUID NOT NULL,
        name text NOT NULL,
        difficulty FLOAT,
        assigned_label text
      );
      CREATE INDEX IF NOT EXISTS """ ++ indexName("to") ++ sql" ON " ++ tableNameFragment ++
        sql""" (id);
    """).update.run.transact(xa)
    } yield queryResult
  }

  def drop(): IO[Int] =
    for {
      queryResult <- (sql"DROP TABLE IF EXISTS " ++ tableNameFragment ++ sql";").update.run
        .transact(xa)
    } yield queryResult

  def readByConfigurationId(id: UUID) =
    (sql"SELECT * FROM " ++ tableNameFragment ++ sql" WHERE configuration_id = $id")
      .query[CustomerData]
      .to[List]
      .transact(xa)

  def readAll() =
    (sql"SELECT * FROM " ++ tableNameFragment)
      .query[CustomerData]
      .to[List]
      .transact(xa)

  def write(model: Customer, configurationId: UUID) = {
    (sql"""INSERT INTO """ ++ tableNameFragment ++
      sql""" (id, configuration_id, name, difficulty, assigned_label)
          VALUES (${model.id}, $configurationId, ${model.name}, ${model.difficulty}, ${model.assignedLabel},)
          ON CONFLICT ON CONSTRAINT """ ++ indexName("pkey") ++
      sql""" DO NOTHING""").update.run
      .transact(xa)
  }
}
