package simulator.db.configuration

import java.util.UUID

import cats.effect.IO
import com.typesafe.scalalogging.LazyLogging
import doobie.implicits._
import doobie.util.fragment.Fragment
import doobie.postgres._
import doobie.postgres.implicits._
import doobie.util.transactor.Transactor
import simulator.model.RepetitionConfig

class RepetitionConfigurationStorage(dbUrl: String, tableName: String, xa: Transactor[IO])
  extends MetaMapping
  with LazyLogging {

  val tableNameFragment = Fragment.const(s"${tableName}RepetitionConfig")

  def indexName(name: String) = Fragment.const(s"${tableName}_$name")

  def init(): IO[Int] = {
    logger.info(s"Initiliasing Database Table $tableName at $dbUrl")
    for {
      queryResult <- (sql"""
      CREATE TABLE IF NOT EXISTS """ ++ tableNameFragment ++
        sql""" (
        id SERIAL PRIMARY KEY NOT NULL UNIQUE,
        interval integer NOT NULL,
        repetitions integer NOT NULL
      );
      CREATE INDEX IF NOT EXISTS """ ++ indexName("to") ++ sql" ON " ++ tableNameFragment ++
        sql""" (id);
    """).update.run.transact(xa)
    } yield queryResult
  }

  def write(model: RepetitionConfig) =
    (sql"""INSERT INTO """ ++ tableNameFragment ++
      sql""" (id, name, type, repeat, effectConfigurations)
          VALUES (${model.id}, ${model.interval}, ${model.repetitions})
          ON CONFLICT ON CONSTRAINT """ ++ indexName("pkey") ++
      sql""" DO NOTHING""").update.run
      .transact(xa)
}
