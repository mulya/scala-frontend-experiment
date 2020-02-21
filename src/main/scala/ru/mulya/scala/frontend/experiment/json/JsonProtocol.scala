package ru.mulya.scala.frontend.experiment.json

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import ru.mulya.scala.frontend.experiment.db.Models
import spray.json._

import scala.util.{Failure, Success, Try}

trait JsonProtocols extends SprayJsonSupport with spray.json.DefaultJsonProtocol {
  implicit val tagFormat = jsonFormat2(Models.Tag.apply)
  implicit val trackRelationFormat = jsonFormat2(Models.TrackRelation.apply)

  //Workaround for https://github.com/spray/spray-json/issues/125
  implicit def mapAnyValKeyFormat[K <: AnyVal : JsonFormat, V : JsonFormat] = new RootJsonFormat[Map[K, V]] {
    def write(m: Map[K, V]) = JsObject {
      m.map { field =>
        field._1.toJson match {
          case JsNumber(x) if x.isValidLong => x.toString -> field._2.toJson
          case x => throw new SerializationException("Map key must be convertible to JsNumber, not '" + x + "'")
        }
      }
    }
    def read(value: JsValue) = value match {
      case x: JsObject => x.fields.map { field =>
        Try(JsNumber(BigDecimal(field._1))) match {
          case Success(k) => (k.convertTo[K], field._2.convertTo[V])
          case Failure(_) => deserializationError("Expected Map key to be deserializable to JsNumber, but got '" + field._1 + "'")
        }
      }
      case x => deserializationError("Expected Map as JsObject, but got " + x)
    }
  }
}