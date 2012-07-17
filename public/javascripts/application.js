$(function() {

  var initExtraFields = function() {
      var extraFields = $("fieldset.extra-fields");

      if (extraFields.length) {

          $(".message.extra-fields").on('click', function(event) {
              $self = $(this)
              var fieldset = $self.next();
              $(fieldset).toggle();
              $self.toggleClass('expanded');
          });

          extraFields.toggle();
      }
  };

  initExtraFields();

  window.prettyPrint && prettyPrint();

})
