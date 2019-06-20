package simulator.db.configuration

import java.util.UUID
import cats.effect.IO
import doobie.implicits._
import doobie.util.fragment.Fragment
import simulator.model.CustomerConfig
import simulator.db.Storage
import doobie.postgres._
import doobie.postgres.implicits._

class CustomerConfigurationStorage(override val tableName: String) extends Storage {

  def init(): IO[Int] = {
    logger.info(s"Initiliasing Database Table $tableName at $dbUrl")
    for {
      queryResult <- (sql"""
      CREATE TABLE IF NOT EXISTS """ ++ tableNameFragment ++
        sql""" (
        id VARCHAR(36) PRIMARY KEY NOT NULL UNIQUE,
        configurationId  VARCHAR(36) NOT NUll,
        name text NOT NULL,
        attributeConfigurations VARCHAR[],
        proportion float NOT NULL
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

  def readById(id: UUID) =
    (sql"SELECT * FROM " ++ tableNameFragment ++ sql" WHERE id = ${id.toString}")
      .query[CustomerConfig]
      .to[List]
      .transact(xa)

  def readAll() =
    (sql"SELECT * FROM " ++ tableNameFragment)
      .query[CustomerConfig]
      .to[List]
      .transact(xa)

  def write(model: CustomerConfig) =
    (sql"""INSERT INTO """ ++ tableNameFragment ++
      sql""" (id, name, attributeConfigurations, proportion)
          VALUES (${model.id}, ${model.name}, ${model.attributeOverrides}, ${model.proportion})
          ON CONFLICT ON CONSTRAINT """ ++ indexName("pkey") ++
      sql""" DO NOTHING""").update.run
      .transact(xa)
}
