package ru.mulya.scala.frontend.experiment.migration

import org.flywaydb.core.Flyway
import org.slf4j.LoggerFactory

import scala.util.Try

class FlywayHelper(url: String, user: String, password: String, schema: String) {
  private val logger =  LoggerFactory.getLogger(getClass)

  def migrate(): Try[Int] = {
    Try(Flyway.configure
      .dataSource(url, user, password)
      .schemas(schema)
      .defaultSchema(schema)
      .load
      .migrate())
  }
}
