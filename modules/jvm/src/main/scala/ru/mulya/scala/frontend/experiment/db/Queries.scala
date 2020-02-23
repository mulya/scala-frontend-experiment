package ru.mulya.scala.frontend.experiment.db

import slick.jdbc.MySQLProfile.api.{Tag => _, _}

object Queries {

    val insertTagReturnTagWithIdQuery = Tables.tags.returning(Tables.tags.map(_.id)).into((tag, id) => tag.copy(id = id))

    def upsertTagQuery(tag: Models.Tag) = Tables.tags.insertOrUpdate(tag)

    def findTagsByTrackIdSet(trackIdSet: Set[Long]): StreamingDBIO[Seq[(Long, Models.Tag)], (Long, Models.Tag)] = {
      (for {
        tr <- Tables.trackRelations if tr.trackId inSet trackIdSet
        t <- tr.tag
      } yield (tr.trackId, (t.id, t.name).mapTo[Models.Tag])).result
    }

    def deleteTagQuery(tagId: Long) =
      Tables.tags.filter(_.id === tagId).delete

    def deleteTrackRelationQuery(trId: Long) =
      Tables.trackRelations.filter(_.trackId === trId).delete
  }