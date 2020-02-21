package ru.mulya.scala.frontend.experiment.db

object Models {

  final case class Tag(id: Long = 0, name: String)
  final case class TrackRelation(trackId: Long, tagId: Long)

}
