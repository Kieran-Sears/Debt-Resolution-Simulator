package simulator.db.configuration

import java.util.UUID

import cats.effect.IO
import com.typesafe.scalalogging.LazyLogging
import doobie.implicits._
import doobie.util.fragment.Fragment
import simulator.model.{ActionConfig, CategoricalConfig}
import doobie.postgres._
import doobie.postgres.implicits._
import doobie.util.transactor.Transactor

class CategoricalConfigurationStorage(dbUrl: String, tableName: String, xa: Transactor[IO])
  extends MetaMapping
  with LazyLogging {

  val tableNameFragment = Fragment.const(s"${tableName}CategoricalConfig")

  def indexName(name: String) = Fragment.const(s"${tableName}_$name")

  def init(): IO[Int] = {
    logger.info(s"Initiliasing Database Table $tableName at $dbUrl")
    for {
      queryResult <- (sql"""
      CREATE TABLE IF NOT EXISTS """ ++ tableNameFragment ++
        sql""" (
        id SERIAL PRIMARY KEY NOT NULL UNIQUE,
        options VARCHAR[] NOT NULL
      );
      CREATE INDEX IF NOT EXISTS """ ++ indexName("to") ++ sql" ON " ++ tableNameFragment ++
        sql""" (id);
    """).update.run.transact(xa)
    } yield queryResult
  }

  def write(model: CategoricalConfig) =
    (sql"""INSERT INTO """ ++ tableNameFragment ++
      sql""" (id, name, type, repeat, effectConfigurations)
          VALUES (${model.id}, ${model.options})
          ON CONFLICT ON CONSTRAINT """ ++ indexName("pkey") ++
      sql""" DO NOTHING""").update.run
      .transact(xa)
}
