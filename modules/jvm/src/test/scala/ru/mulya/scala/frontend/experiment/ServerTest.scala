package ru.mulya.scala.frontend.experiment

import akka.NotUsed
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.alpakka.slick.scaladsl.SlickSession
import akka.stream.scaladsl.Source
import ru.mulya.scala.frontend.experiment.db.Models
import ru.mulya.scala.frontend.experiment.http.Server
import ru.mulya.scala.frontend.experiment.service.TagServiceLike
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.Future

class ServerTest extends AnyFunSuite with Matchers with ScalatestRouteTest with json.JsonProtocols {

  implicit val session = SlickSession.forConfig("slick")

  val tag = Models.Tag(1, "tag1")

  val tagService = new TagServiceLike {
    override def findTags(): Future[List[Models.Tag]] = Future.successful(List(tag))

    override def findTags(tagId: Long): Future[Models.Tag] = Future.successful(tag)

    override def upsertTag(tag: Models.Tag): Future[Boolean] = Future.successful(true)

    override def deleteTag(tagId: Long): Future[Boolean] = Future.successful(true)

    override def findTagsByTrackIdSet(trackIdSet: Set[Long]): Future[Map[Long, List[Models.Tag]]] = Future.successful(Map(1L -> List(tag)))

    override def upsertTracksTags(trackId: Long, tagIdSet: Set[Long]): Future[Boolean] = Future.successful(true)

    override def deleteTracksTags(trackId: Long): Future[Boolean] = Future.successful(true)

    override def findTagsStreaming(): Source[Models.Tag, NotUsed] = ???

    override def findTagsByTrackIdSetStreaming(trackIdSet: Set[Long]): Source[(Long, Models.Tag), NotUsed] = ???
  }
  val server = new Server(tagService)

  test("API Tag routes") {
    Get("/api/v1/tags") ~> server.routes ~> check {
      handled shouldEqual true
      entityAs[List[Models.Tag]] shouldEqual List(tag)
    }

    Post("/api/v1/tags", Models.Tag(id = 0, "newTag")) ~> server.routes ~> check {
      handled shouldEqual true
      status shouldEqual StatusCodes.OK
    }

    Delete("/api/v1/tags/123") ~> server.routes ~> check {
      handled shouldEqual true
      status shouldEqual StatusCodes.OK
    }
  }


}
