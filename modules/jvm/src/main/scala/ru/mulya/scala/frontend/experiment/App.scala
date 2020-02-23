package ru.mulya.scala.frontend.experiment

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.alpakka.slick.scaladsl.SlickSession
import com.typesafe.config.ConfigFactory
import ru.mulya.scala.frontend.experiment.http.Server
import ru.mulya.scala.frontend.experiment.migration.FlywayHelper
import ru.mulya.scala.frontend.experiment.service.TagService
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success}


object App extends App {

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val mat = Materializer.createMaterializer(system)
  implicit val session = SlickSession.forConfig("slick")

  system.registerOnTermination(() => session.close())

  private val logger =  LoggerFactory.getLogger(getClass)

  val config = ConfigFactory.load()

  val flywayHelper = new FlywayHelper(
    config.getString("slick.db.url"),
    config.getString("slick.db.user"),
    config.getString("slick.db.password"),
    config.getString("slick.db.schema")
  )

  val tagService = new TagService()

  val server = new Server(tagService)

  flywayHelper.migrate() match {
    case Success(count) =>
      logger.info(s"Successfully applied migrations: $count")
      server.startServer(config.getString("host"), config.getInt("port"))
    case Failure(exception) =>
      logger.error("Error while applying migrations", exception)
      system.terminate()
  }



}
