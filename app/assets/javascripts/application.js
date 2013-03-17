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

   var initNsiRequestSubmit = function() {

      var responseS = $("#responseS");

      if (!responseS.length) return;

      var queryS = $("#formS"),
          xmlTemplate = $("#xml-template"),
          responseContent = $("#responseS > div.content");

      queryS.find("form").submit(function(event) {

         var pusher = new Pusher(responseS.attr('data-pusher'));
         var channel = pusher.subscribe('response_channel');

         hideQueryForm();

         var correlationId = $(event.target).find('input[id$="correlationId"]').val();
         channel.bind(correlationId, function(data) {
            addXmlResponse("Callback response", data.id, data.response.xml, data.response.time);
         });

         $.ajax({
            data: $(event.target).serialize(),
            type: 'POST',
            url: event.target.action,
            success: function(data) {
               showResponses();
               addXmlBlock("Request", "request-0", data.request.xml, data.request.time)
               addXmlBlock("Response", "response-0", data.response.xml, data.response.time);
            },
            error: function(err) {
               showQueryForm();
               $('.control-group').removeClass('error');
               if (err.status === 400) {
                  var response = JSON.parse(err.responseText);

                  $.each(response, function(index, value) {
                     $("#" + value.id).closest('.control-group').addClass('error');
                  })
                  if (response.message) {
                     addErrorMessage(response.message);
                  }
               } else {
                  addErrorMessage('Error occurred ' + err.status + ', ' + err.statusText);
               }
            }
         });

         return false;
      });

      function addErrorMessage(message) {
         $("nav").after('<div class="alert alert-error"><button class="close" data-dismiss="alert">×</button>' + message + '</div>');
      }

      function addXmlBlock(name, id, xml, time) {
         var xmlBlock = xmlTemplate.clone().removeClass('template').attr("id", id);
         xmlBlock.find("h3").text(name)
         xmlBlock.find(".prettyprint").text(xml);
         xmlBlock.prependTo(responseContent);
         xmlBlock.find("h3").append($("<span/>", {"class": "time", text: time}));

         prettyPrint();
      }

      function addXmlResponse(name, id, xml, time) {
         var existing = $('#'+id)

         if (existing.length > 0) {
           existing.find(".prettyprint").text(existing.find(".prettyprint").text() + xml);
           prettyPrint();
         } else {
             addXmlBlock(name, id, xml, time);

             increaseResponses();
         }
      }

      function hideQueryForm() {
         queryS.css("display", "none");
      }

      function showQueryForm() {
        queryS.css("display", "block");
      }

      function showResponses() {
        responseS.css("display", "block");
      }

      function increaseResponses() {
         var number = $("#numberOfResponses");
         number.text(parseInt(number.text()) + 1);
      }

   }

   initExtraFields();
   initNsiRequestSubmit()

})