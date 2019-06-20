package simulator.db.training

import java.util.UUID
import cats.effect.IO
import doobie.implicits._
import simulator.model.Effect
import simulator.db.Storage
import doobie.postgres._
import doobie.postgres.implicits._

class EffectStorage(override val tableName: String) extends Storage {

  def init(): IO[Int] = {
    logger.info(s"Initiliasing Database Table $tableName at $dbUrl")
    for {
      queryResult <- (sql"""
      CREATE TABLE IF NOT EXISTS """ ++ tableNameFragment ++
        sql""" (
        id VARCHAR(36) PRIMARY KEY NOT NULL UNIQUE,
        actionID VARCHAR(36) NOT NULL,
        name text NOT NULL,
        type text NOT NULL,
        target text NOT NULL,
        value FLOAT,
        certainty FLOAT
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
      .query[Effect]
      .to[List]
      .transact(xa)

  def readAll() =
    (sql"SELECT * FROM " ++ tableNameFragment)
      .query[Effect]
      .to[List]
      .transact(xa)

  def write(model: Effect, actionId: UUID) =
    (sql"""INSERT INTO """ ++ tableNameFragment ++
      sql""" (id, name, actionId, type, target, value, certainty)
          VALUES (${model.id}, ${actionId.toString}, ${model.name}, ${model.`type`}, ${model.target}, ${model.value}, ${model.certainty})
          ON CONFLICT ON CONSTRAINT """ ++ indexName("pkey") ++
      sql""" DO NOTHING""").update.run
      .transact(xa)
}
