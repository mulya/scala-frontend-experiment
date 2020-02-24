package ru.mulya.scala.frontend.experiment.html

import scalatags.Text.all.{`class`, div, meta, _}
import scalatags.Text.tags2.{main, title}

object Templates {

  sealed trait Page {
    def name: String

    def link: String
  }

  case object TracksPage extends Page {
    override val name = "Треки"
    override val link = "/"
  }

  case object TagsPage extends Page {
    override val name = "Теги"
    override val link = "/tags"
  }

  object Page {
    val list = List(TracksPage, TagsPage)
  }

  def layout(page: Page)(bodyPart: scalatags.Text.Modifier*) = html(lang := "en")(
    head(
      meta(charset := "utf-8"),
      meta(name := "viewport", content := "width=device-width, initial-scale=1, shrink-to-fit=no"),
//      link(rel := "stylesheet", href := "assets/bootstrap/4.4.1/css/bootstrap.min.css"),
//      <link rel="stylesheet" href="target/scala-2.12/scalajs-bundler/main/node_modules/bootstrap/dist/css/bootstrap.min.css">
      link(rel := "stylesheet", href := "https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/css/bootstrap.min.css"),
      title("Scala Frontend Exp"),
      link(rel := "stylesheet", href := "assets/main.css")
    ),
    body(`class` := "d-flex flex-column h-100")(
      header(
        tag("nav")(`class` := "navbar navbar-expand-md navbar-dark fixed-top bg-dark")(
          a(`class` := "navbar-brand", href := "#")("Антимат"),
          button(`class` := "navbar-toggler", `type` := "button", attr("data-toggle") := "collapse", attr("data-target") := "#navbarCollapse", attr("aria-controls") := "navbarCollapse", attr("aria-expanded") := "false", attr("aria-label") := "Toggle navigation")(
            span(`class` := "navbar-toggler-icon")
          ),

          div(`class` := "collapse navbar-collapse", id := "navbarCollapse")(
            ul(`class` := "navbar-nav mr-auto")(
              for (p <- Page.list) yield {
                if (p == page) {
                  li(`class` := "nav-item active")(
                    a(`class` := "nav-link", href := p.link)(
                      p.name,
                      span(`class` := "sr-only")("(current)")
                    )
                  )
                } else {
                  li(`class` := "nav-item")(
                    a(`class` := "nav-link", href := p.link)(
                      p.name
                    )
                  )
                }
              }
            )
          )
        )
      ),

      main(role := "main", `class` := "flex-shrink-0")(
        div(`class` := "container")(
          div(`class` := "card")(
            div(`class` := "card-body")(bodyPart: _*)
          )
        )
      ),

      script(src := "assets/tags-jsdeps.js"),
      script(src := "assets/tags.js"),
    )
  )

  val tagsPage = layout(TagsPage)(
    div(`class` := "block-container")(
      //    New tag input
      div(`class` := "add-items d-flex")(
        form(`class` := "navbar-form navbar-right", `action` := "/")(
          input(`type` := "text", `class` := "form-control tag-list-input", placeholder := "Добавить тег", id := "tagName", name := "tagName")
        )
      ),
      div(`class` := "block-list")(
        ul(`class` := "list-group list-group-flush")()
      )
    ),
    //  Modal
    div(`class` := "modal fade", id := "tagConfirmationModal", tabindex := "-1", role := "dialog", attr("aria-labelledby") := "tagConfirmationModalLabel", attr("aria-hidden") := "true")(
      div(`class` := "modal-dialog", role := "document")(
        div(`class` := "modal-content")(
          div(`class` := "modal-header")(
            h5(`class` := "modal-title", id := "tagConfirmationModalLabel")("Уверены?"),
            button(`type` := "button", `class` := "close", attr("data-dismiss") := "modal", attr("aria-label") := "Close")(
              span(attr("aria-hidden") := "true")(raw("&times;"))
            )
          ),
          div(`class` := "modal-body")(
            p("")
          ),
          div(`class` := "modal-footer")(
            button(`type` := "button", `class` := "btn btn-secondary", attr("data-dismiss") := "modal")("Отменить"),
            button(id := "tag-delete-button", `type` := "button", `class` := "btn btn-danger")("Удалить")
          )
        )
      )
    )
  )

  val tracksPage = layout(TracksPage)(
    //    Track search
    form(`class` := "form-inline")(
      div(`class` := "form-group mx-sm-3 mb-2")(
        label(attr("for") := "query", `class` := "sr-only")("Поиск трека"),
        input(`type` := "text", `class` := "form-control", placeholder := "Поиск трека", id := "query", name := "query")
      )
    ),
    //    Table
    div(`class` := "table-responsive")(
      table(`class` := "table table-striped")(
        thead(
          tr(
            th(attr("scope") := "col")("#"),
            th(attr("scope") := "col")("Исполнитель"),
            th(attr("scope") := "col")("Трек"),
            th(attr("scope") := "col")("Теги"),
          )
        ),
        tbody()
      )
    ),

    div(`class` := "table-spinner-container", style := "display: none;")(
      div(`class` := "text-center", id := "tags-spinner")(
        div(`class` := "spinner-border", role := "status")(
          span(`class` := "sr-only")("Загрузка...")
        )
      )
    ),

    //    Pagination
    tag("nav")(attr("aria-label") := "Page navigation", `class` := "mt-3")(
      ul(`class` := "pagination justify-content-center")(
        li(`class` := "page-item pagination-back disabled")(
          a(`class` := "page-link", href := "#", tabindex := "-1")("Назад")
        ),
        li(`class` := "page-item pagination-current disabled")(
          a(`class` := "page-link", href := "#")("1")
        ),
        li(`class` := "page-item pagination-forward disabled")(
          a(`class` := "page-link", href := "#")("Вперед")
        )
      )
    ),

    //  Modal
    div(`class` := "modal fade", id := "trackModal", tabindex := "-1", role := "dialog", attr("aria-labelledby") := "trackModalLabel", attr("aria-hidden") := "true")(
      div(`class` := "modal-dialog", role := "document")(
        div(`class` := "modal-content")(
          div(`class` := "modal-header")(
            h5(`class` := "modal-title", id := "trackModalLabel")("Трек"),
            button(`type` := "button", `class` := "close", attr("data-dismiss") := "modal", attr("aria-label") := "Close")(
              span(attr("aria-hidden") := "true")(raw("&times;"))
            )
          ),

          div(`class` := "modal-body")(
            form(id := "modal-form")(
              div(`class` := "form-group row")(
                label(attr("for") := "staticId", `class` := "col-sm-4 col-form-label")("#"),
                div(`class` := "col-sm-6")(
                  input(`type` := "text", readonly := 1, `class` := "form-control-plaintext", id := "staticId", value := "2448511")
                )
              ),

              div(`class` := "form-group row")(
                label(attr("for") := "staticArtist", `class` := "col-sm-4 col-form-label")("Исполнитель"),
                div(`class` := "col-sm-6")(
                  input(`type` := "text", readonly := 1, `class` := "form-control-plaintext", id := "staticArtist", value := "Exo")
                )
              ),

              div(`class` := "form-group row")(
                label(attr("for") := "staticTrack", `class` := "col-sm-4 col-form-label")("Трек"),
                div(`class` := "col-sm-6")(
                  input(`type` := "text", readonly := 1, `class` := "form-control-plaintext", id := "staticTrack", value := "Metal")
                )
              ),

              fieldset(`class` := "form-group")(
                div(`class` := "row")(
                  legend(`class` := "col-form-label col-sm-4 pt-0")("Теги"),
                  div(`class` := "col-sm-6")(
                    div(`class` := "text-center", id := "tags-spinner")(
                      div(`class` := "spinner-border", role := "status")(
                        span(`class` := "sr-only")("Загрузка...")
                      )
                    ),
                    div(id := "tags-container")()
                  )
                )
              )
            )
          ),

          div(`class` := "modal-footer")(
            input(id := "modal-save-button", `type` := "submit", `class` := "btn btn-primary", attr("form") := "modal-form", value := "Сохранить")
          )
        )
      )
    )
  )
}
