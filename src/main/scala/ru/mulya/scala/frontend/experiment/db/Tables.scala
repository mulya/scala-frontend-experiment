package ru.mulya.scala.frontend.experiment.db

import slick.jdbc.MySQLProfile.api.{Tag => SlickTag, _}

object Tables {

  final class TagTable(slickTag: SlickTag) extends Table[Models.Tag](slickTag, Some("tags"), "tags") {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc) // This is the primary key column
    def name = column[String]("NAME")
    def * = (id, name).mapTo[Models.Tag]
  }

  final class TrackRelationTable(slickTag: SlickTag) extends Table[Models.TrackRelation](slickTag, Some("tags"), "track_relations") {
    def trackId = column[Long]("TRACK_ID")
    def tagId = column[Long]("TAG_ID")
    def * = (trackId, tagId).mapTo[Models.TrackRelation]
    def tag = foreignKey("TAG_FK", tagId, tags)(_.id)
  }

  lazy val tags = TableQuery[TagTable]
  lazy val trackRelations = TableQuery[TrackRelationTable]

}
