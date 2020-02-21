$(document).ready(function (e) {
    var searchParams = new URLSearchParams(window.location.search);
    if (searchParams.has("query")) {
        $("#query").val(searchParams.get("query"));
        var page = 1;
        if (searchParams.has("page")) {
            page = Number(searchParams.get("page"));
        }
        ajaxTrackSearch(searchParams.get("query"), page);
    }
});

$(document).on("keypress", "#query", function (e) {
  if (e.keyCode == 13){
      e.preventDefault();
      var query = $(this).val();
      ajaxTrackSearch(query);
  }
});

function ajaxTrackSearch(query, page){
    page = page || 1;
    if (query != '') {
        var search = '?query=' + query;
        if (page !== 1) {
            search = search + "&page=" + page;
        }
        window.history.pushState({}, null, search);

        $(".table-spinner-container").show();
        $(".table-responsive table tbody tr").remove();
        resetPagination();

        $.ajax({
            url: "https://bejqkywxu2.execute-api.us-east-1.amazonaws.com/default/trackList",
            method: "GET",
            cache: false,
            success: function (data){
               ajaxTagSearch(data.tracks, data.page, data.pagesCount);
            },
            error:function (xhr, ajaxOptions, thrownError){
                if(xhr.status==404) {
                    $(".table-spinner-container").hide()
                }
            }
        });
    }
}

function ajaxTagSearch(tracksData, page, pagesCount){
    var idListStr = ""
    for(var i = 0; i < tracksData.length; i++) {
         idListStr += tracksData[i].id;
         if (i + 1 < tracksData.length) {
            idListStr += ";"
         }
    }

    $.ajax({
        url: "/api/v1/tracks/" + idListStr + "/tags",
        method: "GET",
        cache: false,
        success: function (data) {
            fillTableWithData(tracksData, data);
            updatePagination(page, pagesCount);
        }
    });
}

function updatePagination(page, pagesCount) {
    $(".pagination-current a").text(page)

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

function resetPagination() {
    $(".pagination-current a").text("1")
    $(".pagination-back").addClass("disabled")
    $(".pagination-forward").addClass("disabled")
}

$('.page-link').on('click', function(event) {
    event.preventDefault();
    var a = $(this);

    var searchParams = new URLSearchParams(window.location.search);
    var page = 1;
    if (searchParams.has("page")) {
        page = Number(searchParams.get("page"));
    }

    var query = searchParams.get("query");

    if (a.parent().hasClass("pagination-back")) {
        page = page - 1;
    } else {
        page = page + 1;
    }

    ajaxTrackSearch(query, page)
});

function fillTableWithData(tracksData, tagsData) {
    var tagsData = new Map(Object.entries(tagsData));

    for(var i = 0; i < tracksData.length; i++) {
      var track = tracksData[i];
      var tagListStr = "";

      if (tagsData.has(track.id.toString())) {
      var tags = tagsData.get(track.id.toString())

      for(var j = 0; j < tags.length; j++) {
           var tag = tags[j];
           tagListStr += tag.name;
           if (j + 1 < tags.length) {
               tagListStr += ", ";
           }
       }
      }

      $(".table-responsive table tbody").append(
          "<tr><td><a href=\"#\" data-toggle=\"modal\" data-target=\"#trackModal\" data-id=\"" + track.id +
          "\" data-artist=\"" + track.artistName + "\" data-track=\"" + track.track + "\">" + track.id +
          "</a></td><td>" + track.artistName + "</td><td>" + track.track + "</td><td>" + tagListStr +
             "</td></tr>"
      )
    }

    $(".table-spinner-container").hide()
 }

$('#trackModal').on('show.bs.modal', function (event) {
  var button = $(event.relatedTarget) // Button that triggered the modal
  var modal = $(this)
  var id = button.data("id");

  modal.find('.modal-body #staticId').val(button.data("id"));
  modal.find('.modal-body #staticArtist').val(button.data("artist"));
  modal.find('.modal-body #staticTrack').val(button.data("track"));

  var spinnerDiv = modal.find('#tags-spinner');
  var containerDiv = modal.find('#tags-container');

  spinnerDiv.show();
  containerDiv.hide();

  containerDiv.find("div").remove();

   $.when(
       $.ajax({url: "/api/v1/tags", cache: false}),
       $.ajax({url: "/api/v1/tracks/" + id + "/tags", cache: false})
   )
    .done(function(allTagsData, tracksTagsData) {
        for(var i = 0; i < allTagsData[0].length; i++) {
            var tag = allTagsData[0][i];

            var checked = "";
            var tracksTagsMap = new Map(Object.entries(tracksTagsData[0]));
            if (tracksTagsMap.has(id.toString())) {
                var tracksTags = tracksTagsMap.get(id.toString());
                for(var k = 0; k < tracksTags.length; k++) {
                    var tracksTag = tracksTags[k];
                    if(tracksTag.id == tag.id) {
                        checked = "checked";
                        break;
                    }
                }
            }

            containerDiv.append(
                "<div class=\"form-check\">" +
                "   <input class=\"form-check-input\" type=\"checkbox\" name=\"tag\" id=\"tag" + tag.id + "\"" +
                "   value=\"" + tag.id + "\" " + checked + " >" +
                "   <label class=\"form-check-label\" for=\"tag" + tag.id + "\">" + tag.name + "</label>" +
                "</div>"
            )
        }

        spinnerDiv.hide();
        containerDiv.show();
   });
})

$('#modal-save-button').on('click', function(event) {
  event.preventDefault();

  var id = $("#staticId").val();
  var tagData = $('#modal-form').serializeArray().map(x => Number(x.value));
  $.ajax({
          url: "/api/v1/tracks/" + id + "/tags",
          data: JSON.stringify(tagData),
          method: "POST",
          contentType: "application/json",
          cache: false,
          success: function (data) {
              $('#trackModal').modal('hide');
              ajaxTrackSearch($("#query").val());
          }
      });
});