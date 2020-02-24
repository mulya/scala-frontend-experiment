package ru.mulya.scala.frontend.experiment.js.pages

import org.querki.jquery.{$, JQueryAjaxSettings, JQueryEventObject, JQueryXHR}
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.{Element, document}

import scala.scalajs.js
import scala.scalajs.js.Dynamic

object Tags {
  def initTagsPage(): Unit = {
    ajaxTagsFetch()
  }

  def ajaxTagsFetch(): Unit = {
    $.ajax(Dynamic.literal(
      url = "api/v1/tags",
      method = "GET",
      cache = false,
      success = (data: js.Dynamic, status: String, jqXHR: JQueryXHR) => {
        val list = $(".block-list .list-group")

        list.find(".list-group-item").remove()

        data.asInstanceOf[js.Array[js.Dynamic]].foreach{ tag =>
          $(".block-list ul").append(
            "<li class=\"list-group-item\">" + tag.name + " " +
              "<a type=\"button\" class=\"text-danger tag-delete-link\" data-toggle=\"modal\" data-target=\"#tagConfirmationModal\" data-id=\"" + tag.id + "\" data-name=\"" + tag.name + "\">&times;</a>" +
              "</li>"
          )
        }
      }
    ).asInstanceOf[JQueryAjaxSettings])
  }

  $(document).on("keypress", "#tagName", js.undefined, (e: Element, ev: JQueryEventObject) => {
    if (ev.which == KeyCode.Enter){
      ev.preventDefault()
      val tagName = $(e).valueString
      ajaxCreateTag(tagName)
    }
  })

  def ajaxCreateTag(tagName: String): Unit = {
    $.ajax(Dynamic.literal(
      url = "api/v1/tags",
      method = "POST",
      data = js.JSON.stringify(js.Dynamic.literal(name = tagName, id = 0)),
      contentType = "application/json; charset=utf-8",
      cache = false,
      success = (data: js.Dynamic, status: String, jqXHR: JQueryXHR) => {
        $(".block-container .add-items .navbar-form input").`val`("")
        ajaxTagsFetch()
      }
    ).asInstanceOf[JQueryAjaxSettings])
  }

  $("#tagConfirmationModal").on("show.bs.modal", (e: Element, ev: JQueryEventObject) => {
    val button = $(ev.relatedTarget) // Button that triggered the modal
    val modal = $(e)
    val id: Int = button.data("id").get.asInstanceOf[Int]
    val name: String = button.data("name").asInstanceOf[String]
    val deleteButton = $("#tag-delete-button")

    modal.find(".modal-body p").text("Удаление тега \"" + name + "\" так же удалит все связи с треками")
    deleteButton.attr("data-id", id)
  })

  $("#tag-delete-button").on("click", (e: Element, ev: JQueryEventObject) => {
    val button = $(e)
    ajaxDeleteTag(button.attr("data-id").get.toLong)
  })

  def ajaxDeleteTag(tagId: Long): Unit = {
    $.ajax(Dynamic.literal(
      url = "api/v1/tags/" + tagId,
      method = "DELETE",
      cache = false,
      success = (data: js.Dynamic, status: String, jqXHR: JQueryXHR) => {
        $("#tagConfirmationModal").modal("hide")
        ajaxTagsFetch()
      }
    ).asInstanceOf[JQueryAjaxSettings])
  }
}
