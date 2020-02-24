package ru.mulya.scala.frontend.experiment.js

import org.querki.jquery._
import org.scalajs.dom
import dom.window
import ru.mulya.scala.frontend.experiment.js.pages.{Tags, Tracks}

object App {

  def main(args: Array[String]): Unit = {
    $(() => init())
  }

  def init(): Unit = {
    window.location.pathname match {
      case "/" =>
        Tracks.initTracksPage()
      case "/tags" =>
        Tags.initTagsPage()
    }
  }

}
