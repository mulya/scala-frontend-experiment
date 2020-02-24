package ru.mulya.scala.frontend.experiment.js.util

object URLSearchParams {
  def apply(search: String): Map[String, String] = {
    search.length match {
      case 0 =>
        Map.empty
      case _ =>
        search
          .tail
          .split('&')
          .map(s => {
            val parts = s.split('=')
            parts(0) -> parts(1)
          })
        .toMap
    }
  }
}
