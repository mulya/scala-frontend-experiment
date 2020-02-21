$(document).ready(function (e) {
    ajaxTagsFetch();
});

function ajaxTagsFetch(){
    $.ajax({
        url: "api/v1/tags",
        method: "GET",
        cache: false,
        success: function (data){
            var list = $(".block-list .list-group");

            list.find('.list-group-item').remove();

            for(var i = 0; i < data.length; i++) {
                 var tag = data[i];
                 $(".block-list ul").append(
                     "<li class=\"list-group-item\">" + tag.name + " " +
                     "<a type=\"button\" class=\"text-danger tag-delete-link\" data-toggle=\"modal\" data-target=\"#tagConfirmationModal\" data-id=\"" + tag.id + "\" data-name=\"" + tag.name + "\">&times;</a>" +
                     "</li>"
                 );
            }
        }
    });
}

$(document).on("keypress", "#tagName", function (e) {
  if (e.keyCode == 13){
      e.preventDefault();
      var tagName = $(this).val();
      ajaxCreateTag(tagName);
  }
});

function ajaxCreateTag(tagName){
    $.ajax({
        url: "api/v1/tags",
        method: "POST",
        data: JSON.stringify({"name": tagName, "id": 0}),
        contentType: "application/json; charset=utf-8",
        cache: false,
        success: function (data){
            $('.block-container .add-items .navbar-form input').val('');
            ajaxTagsFetch();
        }
    });
}

$('#tagConfirmationModal').on('show.bs.modal', function (event) {
  var button = $(event.relatedTarget); // Button that triggered the modal
  var modal = $(this);
  var id = button.data("id");
  var name = button.data("name");
  var deleteButton = $("#tag-delete-button");

  modal.find('.modal-body p').text("Удаление тега \"" + name + "\" так же удалит все связи с треками");
  deleteButton.attr("data-id", id);
});

$("#tag-delete-button").on('click', function(event) {
    var button = $(this);
    ajaxDeleteTag(button.attr("data-id"));
});

function ajaxDeleteTag(tagId){
    $.ajax({
        url: "api/v1/tags/" + tagId,
        method: "DELETE",
        cache: false,
        success: function (data){
            $("#tagConfirmationModal").modal('hide');
            ajaxTagsFetch();
        }
    });
}