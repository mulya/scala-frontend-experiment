package ru.mulya.scala.frontend.experiment.js

import org.querki.jquery.JQuery

import scala.scalajs.js

package object pages {

  // Monkey patching JQuery
  @js.native
  trait SemanticJQuery extends JQuery {
    def modal(params: js.Any*): SemanticJQuery = js.native
  }

  // Monkey patching JQuery with implicit conversion
  implicit def jq2semantic(jq: JQuery): SemanticJQuery = jq.asInstanceOf[SemanticJQuery]

}
