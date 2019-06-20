package simulator.db.configuration

import java.util.UUID
import cats.effect.IO
import doobie.implicits._
import simulator.model.EffectConfig
import doobie.postgres._
import doobie.postgres.implicits._

import simulator.db.Storage

class EffectConfigurationStorage(override val tableName: String) extends Storage {

  def init(): IO[Int] = {
    logger.info(s"Initiliasing Database Table $tableName at $dbUrl")
    for {
      queryResult <- (sql"""
      CREATE TABLE IF NOT EXISTS """ ++ tableNameFragment ++
        sql""" (
        id VARCHAR(36) PRIMARY KEY NOT NULL UNIQUE,
        actionId  VARCHAR(36) NOT NUll,
        name text NOT NULL,
        type text NOT NULL,
        target text NOT NULL
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
      .query[EffectConfig]
      .to[List]
      .transact(xa)

  def readAll() =
    (sql"SELECT * FROM " ++ tableNameFragment)
      .query[EffectConfig]
      .to[List]
      .transact(xa)

  def write(model: EffectConfig) =
    (sql"""INSERT INTO """ ++ tableNameFragment ++
      sql""" (id, name, type, target)
          VALUES (${model.id}, ${model.name}, ${model.`type`}, ${model.target})
          ON CONFLICT ON CONSTRAINT """ ++ indexName("pkey") ++
      sql""" DO NOTHING""").update.run
      .transact(xa)
}
