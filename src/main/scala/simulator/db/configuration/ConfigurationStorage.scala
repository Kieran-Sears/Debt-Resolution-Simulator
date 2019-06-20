package simulator.db.configuration

import java.util.UUID

import cats.effect.IO
import doobie.implicits._
import simulator.db.Storage
import simulator.db.model.ConfigurationData
import doobie.postgres._
import doobie.postgres.implicits._

class ConfigurationStorage(override val tableName: String) extends Storage {

  def init(): IO[Int] = {
    logger.info(s"Initiliasing Database Table $tableName at $dbUrl")
    for {
      queryResult <- (sql"""
      CREATE TABLE IF NOT EXISTS """ ++ tableNameFragment ++
        sql""" (
        username VARCHAR(36) PRIMARY KEY NOT NULL UNIQUE,
        configurationId  VARCHAR(36) NOT NUll
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
      .query[ConfigurationData]
      .to[List]
      .transact(xa)

  def readAll() =
    (sql"SELECT * FROM " ++ tableNameFragment)
      .query[ConfigurationData]
      .to[List]
      .transact(xa)

  def write(model: ConfigurationData) =
    (sql"""INSERT INTO """ ++ tableNameFragment ++
      sql""" (username, configurationId)
          VALUES (${model.username}, ${model.ConfigurationId.toString})
          ON CONFLICT ON CONSTRAINT """ ++ indexName("pkey") ++
      sql""" DO NOTHING""").update.run.transact(xa)
}
