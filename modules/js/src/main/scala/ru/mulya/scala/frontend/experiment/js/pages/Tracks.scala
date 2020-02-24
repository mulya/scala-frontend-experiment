package ru.mulya.scala.frontend.experiment.js.pages

import ru.mulya.scala.frontend.experiment.js.util.URLSearchParams
import org.querki.jquery.{$, JQuery, JQueryAjaxSettings, JQueryEventObject, JQueryXHR}
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.{Element, document, window}
import scalatags.Text.all._

import scala.scalajs.js
import scala.scalajs.js.Dynamic

object Tracks {

  def initTracksPage() {
    val searchParams = URLSearchParams(window.location.search)
    if (searchParams.contains("query")) {
      $("#query").`val`(searchParams("query"))
      var page = 1
      if (searchParams.contains("page")) {
        page = searchParams("page").toInt
      }
      ajaxTrackSearch(searchParams("query"), page)
    }
  }

  $(document).on("keypress", "#query", js.undefined, (e: Element, ev: JQueryEventObject) => {
    if (ev.which == KeyCode.Enter) {
      ev.preventDefault()
      val query = $(e).valueString
      ajaxTrackSearch(query)
    }
  })

  def ajaxTrackSearch(query: String, page: Int = 1) {
    if (query != "") {
      var search = "?query=" + query
      if (page != 1) {
        search = search + "&page=" + page
      }
      window.history.pushState({}, null, search)

      $(".table-spinner-container").show()
      $(".table-responsive table tbody tr").remove()
      resetPagination()

      $.ajax(Dynamic.literal(
        url = "https://bejqkywxu2.execute-api.us-east-1.amazonaws.com/default/trackList",
        method = "GET",
        data = "query=" + query,
        cache = false,
        success = (data: js.Dynamic, status: String, jqXHR: JQueryXHR) => {
          ajaxTagSearch(data.tracks.asInstanceOf[js.Array[js.Dynamic]], data.page.toString.toInt, data.pagesCount.toString.toInt)
        },
        error = (jqXHR: JQueryXHR, textStatus: String, errorThrow: String) => {
          if (jqXHR.status == 404) {
            $(".table-spinner-container").hide()
          }
        }
      ).asInstanceOf[JQueryAjaxSettings])
    }
  }

  def ajaxTagSearch(tracksData: js.Array[js.Dynamic], page: Int, pagesCount: Int) {
    val idListStr = tracksData.map(_.id.toString).mkString(";")

    $.ajax(Dynamic.literal(
      url = "/api/v1/tracks/" + idListStr + "/tags",
      method = "GET",
      cache = false,
      success = (data: js.Dynamic, status: String, jqXHR: JQueryXHR) => {
        fillTableWithData(tracksData, data)
        updatePagination(page, pagesCount)
      }
    ).asInstanceOf[JQueryAjaxSettings])
  }

  def updatePagination(page: Int, pagesCount: Int) {
    $(".pagination-current a").text(page.toString)

    if (page > 1) {
      $(".pagination-back").removeClass("disabled")
    } else {
      $(".pagination-back").addClass("disabled")
    }

    if (page < pagesCount) {
      $(".pagination-forward").removeClass("disabled")
    } else {
      $(".pagination-forward").addClass("disabled")
    }
  }

  def resetPagination() {
    $(".pagination-current a").text("1")
    $(".pagination-back").addClass("disabled")
    $(".pagination-forward").addClass("disabled")
  }

  $(".page-link").on("click", (e: Element, ev: JQueryEventObject) => {
    ev.preventDefault()
    val a = $(e)

    val searchParams = URLSearchParams(window.location.search)
    var page = 1
    if (searchParams.contains("page")) {
      page = searchParams("page").toInt
    }

    val query = searchParams("query")

    if (a.parent().hasClass("pagination-back")) {
      page = page - 1
    } else {
      page = page + 1
    }

    ajaxTrackSearch(query, page)
  })

  def fillTableWithData(tracksData: js.Array[js.Dynamic], tagsData: js.Dynamic) {
    tracksData.foreach { track =>
      var tagListStr = ""

      if (!js.isUndefined(tagsData.selectDynamic(track.id.toString))) {
        val tags = tagsData.selectDynamic(track.id.toString).asInstanceOf[js.Array[js.Dynamic]]

        tagListStr = tags.map(_.name).mkString(", ")
      }

      $(".table-responsive table tbody").append(
        tr(
          td(
            a(href := "#", attr("data-toggle") := "modal", attr("data-target") := "#trackModal",
              attr("data-id") := track.id.toString, attr("data-artist") := track.artistName.toString, attr("data-track") := track.track.toString)(track.id.toString)
          ),
          td(track.artistName.toString),
          td(track.track.toString),
          td(tagListStr)
        )
          .toString
      )
    }

    $(".table-spinner-container").hide()
  }

  $("#trackModal").on("show.bs.modal", (e: Element, ev: JQueryEventObject) => {
    val button = $(ev.relatedTarget) // Button that triggered the modal
    val modal = $(e)
    val id = button.data("id").toString.toInt

    button.data("id").map(v => modal.find(".modal-body #staticId").`val`(v.toString))
    button.data("artist").map(v => modal.find(".modal-body #staticArtist").`val`(v.toString))
    button.data("track").map(v => modal.find(".modal-body #staticTrack").`val`(v.toString))

    val spinnerDiv = modal.find("#tags-spinner")
    val containerDiv = modal.find("#tags-container")

    spinnerDiv.show()
    containerDiv.hide()

    containerDiv.find("div").remove()

    $.when($.ajax("/api/v1/tags"), $.ajax("/api/v1/tracks/" + id + "/tags")).done((allTagsData: js.Array[js.Dynamic], tracksTagsData: js.Array[js.Dynamic]) => {
      allTagsData.head.asInstanceOf[js.Array[js.Dynamic]].foreach { tag =>
        var checked = false
        val tracksTagsMap = tracksTagsData.head
        if (!js.isUndefined(tracksTagsMap.selectDynamic(id.toString))) {
          val tracksTags = tracksTagsMap.selectDynamic(id.toString).asInstanceOf[js.Array[js.Dynamic]]
          tracksTags.foreach { tracksTag =>
            if (tracksTag.selectDynamic("id").toString == tag.selectDynamic("id").toString) {
              checked = true
            }
          }

        }

        containerDiv.append(
          div(`class` := "form-check")(
            if (checked) {
              input(`class` := "form-check-input", `type` := "checkbox", name := "tag", attr("id") := s"tag${tag.id.toString}",
                attr("value") := tag.id.toString, attr("checked") := 1)
            } else {
              input(`class` := "form-check-input", `type` := "checkbox", name := "tag", attr("id") := s"tag${tag.id.toString}",
                attr("value") := tag.id.toString)
            },
            label(`class` := "form-check-label", attr("for") := s"tag${tag.id.toString}")(tag.name.toString)
          ).toString
        )
      }

      spinnerDiv.hide()
      containerDiv.show()
    })
  })

  $("#modal-save-button").on("click", (e: Element, ev: JQueryEventObject) => {
    import js.JSConverters._
    ev.preventDefault()

    val id = $("#staticId").valueString.toInt
    val tagData = $("#modal-form input:checked").toArray().map(x => x.getAttribute("value").toInt)
    $.ajax(Dynamic.literal(
      url = "/api/v1/tracks/" + id + "/tags",
      data = js.JSON.stringify(tagData),
      method = "POST",
      contentType = "application/json",
      cache = false,
      success = (data: js.Dynamic, status: String, jqXHR: JQueryXHR) => {
        $("#trackModal").modal("hide")
        ajaxTrackSearch($("#query").valueString)
      }
    ).asInstanceOf[JQueryAjaxSettings])
  })

}
