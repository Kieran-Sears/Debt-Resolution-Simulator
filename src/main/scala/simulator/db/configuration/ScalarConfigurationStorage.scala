package simulator.db.configuration

import java.util.UUID

import cats.effect.IO
import doobie.implicits._
import simulator.model.ScalarConfig
import simulator.db.Storage
import doobie.postgres._
import doobie.postgres.implicits._

class ScalarConfigurationStorage(override val tableName: String) extends Storage {

  def init(): IO[Int] = {
    logger.info(s"Initiliasing Database Table $tableName at $dbUrl")
    for {
      queryResult <- (sql"""
      CREATE TABLE IF NOT EXISTS """ ++ tableNameFragment ++
        sql""" (
        id VARCHAR(36) PRIMARY KEY NOT NULL UNIQUE,
        attributeId  VARCHAR(36) NOT NUll,
        variance text NOT NULL,
        min INTEGER NOT NULL,
        max INTEGER NOT NULL
      );
      CREATE INDEX IF NOT EXISTS """ ++ indexName("to") ++ sql" ON " ++ tableNameFragment ++
        sql""" (id);
    """).update.run.transact(xa)
    } yield queryResult
  }

  def readById(id: UUID) =
    (sql"SELECT * FROM " ++ tableNameFragment ++ sql" WHERE id = ${id.toString}")
      .query[ScalarConfig]
      .to[List]
      .transact(xa)

  def write(model: ScalarConfig) =
    (sql"""INSERT INTO """ ++ tableNameFragment ++
      sql""" (id, variance, min, max)
          VALUES (${model.id}, ${model.variance}, ${model.min}, ${model.max})
          ON CONFLICT ON CONSTRAINT """ ++ indexName("pkey") ++
      sql""" DO NOTHING""").update.run
      .transact(xa)
}
