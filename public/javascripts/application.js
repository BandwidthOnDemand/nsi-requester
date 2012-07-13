$(function() {

  var initExtraFields = function() {
      var extraFields = $("fieldset.extra-fields");

      if (extraFields.length) {

          $(".message.extra-fields").on('click', function(event) {
              extraFields.toggle();
              $(this).toggleClass('expanded');
          });

          extraFields.toggle();
      }
  };

  initExtraFields();

  window.prettyPrint && prettyPrint();

})
