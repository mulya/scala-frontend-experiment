package ru.mulya.scala.frontend.experiment.service

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.alpakka.slick.scaladsl.{Slick, SlickSession}
import akka.stream.scaladsl.{Flow, Sink, Source}
import ru.mulya.scala.frontend.experiment.db.Models.{Tag, TrackRelation}
import ru.mulya.scala.frontend.experiment.db.{Models, Queries, Tables}
import slick.dbio.StreamingDBIO
import slick.jdbc.GetResult
import slick.jdbc.MySQLProfile.api.{Tag => _, _}

import scala.concurrent.{ExecutionContext, Future}

trait TagServiceLike {

  def findTags(): Future[List[Models.Tag]]

  def findTagsStreaming(): Source[Models.Tag, NotUsed]

  def findTags(tagId: Long): Future[Models.Tag]

  def upsertTag(tag: Tag): Future[Boolean]

  def deleteTag(tagId: Long): Future[Boolean]

  def findTagsByTrackIdSet(trackIdSet: Set[Long]): Future[Map[Long, List[Models.Tag]]]

  /**
   * Streaming nature of this method doesn't allow to group data to return all tags of track in one collection "(trackId, tagList)".
   * Tags (actually Track-Tag relations) flowing in order natural for current DB and need to be groupe (if necessary) on receiving side.
   */
  def findTagsByTrackIdSetStreaming(trackIdSet: Set[Long]): Source[(Long, Models.Tag), NotUsed]

  def upsertTracksTags(trackId: Long, tagIdSet: Set[Long]): Future[Boolean]

  def deleteTracksTags(trackId: Long): Future[Boolean]

}

class TagService()(implicit ec: ExecutionContext, ss: SlickSession, mat: Materializer) extends TagServiceLike {

  implicit val getTagResult: GetResult[Tag] =
    GetResult(r => Tag(r.nextLong(), r.nextString()))
  implicit val getTrackRelationResult: GetResult[TrackRelation] =
    GetResult(r => TrackRelation(r.nextLong(), r.nextLong()))

  def findTags(): Future[List[Models.Tag]] = {
    findTagsStreaming()
      .runWith(
        Sink.foldAsync(List.empty[Models.Tag])((acc, next) => Future(acc :+ next))
      )
  }

  def findTagsStreaming(): Source[Models.Tag, NotUsed] = {
    Slick.source(Tables.tags.sortBy(_.id.desc).result)
  }

  def findTags(tagId: Long): Future[Models.Tag] = {
    Slick.source(
      Tables.tags
        .filter(_.id === tagId)
        .sortBy(_.id.desc)
        .result
    )
      .runWith(Sink.head)
  }

  def upsertTag(tag: Tag): Future[Boolean] = {
    Source.single(tag)
      .via(Slick.flow(t => Queries.upsertTagQuery(t)))
      .via(Flow.fromFunction(_ != 0))
      .runWith(Sink.head[Boolean])
  }

  def deleteTag(tagId: Long): Future[Boolean] =
    Source.single(tagId)
      .via(Slick.flow(Queries.deleteTagQuery))
      .via(Flow.fromFunction(_ != 0))
      .runWith(Sink.head)

  def findTagsByTrackIdSet(trackIdSet: Set[Long]): Future[Map[Long, List[Models.Tag]]] = {
    findTagsByTrackIdSetStreaming(trackIdSet)
      .runWith(
        Sink.foldAsync(List.empty[(Long, Models.Tag)])((acc, next) => Future(acc :+ next))
      )
      .map{ tupleList =>
        tupleList
          .groupBy(_._1)
          .view.mapValues(v => v.map(_._2))
          .toMap
      }
  }

  def findTagsByTrackIdSetStreaming(trackIdSet: Set[Long]): Source[(Long, Models.Tag), NotUsed] = {
    Slick.source(Queries.findTagsByTrackIdSet(trackIdSet))
  }

  def upsertTracksTags(trackId: Long, tagIdSet: Set[Long]): Future[Boolean] = {
    def combinedQuery(trackId: Long): DBIO[Int] = for {
      _ <- Queries.deleteTrackRelationQuery(trackId)
      track = tagIdSet.map(tagId => TrackRelation(trackId, tagId))
      i <- Tables.trackRelations ++= track
    } yield i.getOrElse(0)

    Source.single(trackId)
      .via(Slick.flow(tId => combinedQuery(tId).transactionally))
      .via(Flow.fromFunction(_ != 0))
      .runWith(Sink.head)
  }

  def deleteTracksTags(trackId: Long): Future[Boolean] =
    Source.single(trackId)
      .via(Slick.flow(Queries.deleteTrackRelationQuery))
      .via(Flow.fromFunction(_ != 0))
      .runWith(Sink.head)

}
