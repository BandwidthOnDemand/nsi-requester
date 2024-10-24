$(function() {
   let eventSource;

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

      var lastHash = window.location.hash,
          responseS = $("#responseS");

      if (!responseS.length) return;

      var queryS = $("#formS"),
          xmlTemplate = $("#xml-template"),
          form = queryS.find("form"),
          responseContent = $("#responseS > div.content");

      form.submit(function(event) {
         var correlationId = $(event.target).find('input[id$="correlationId"]').val();
         hideQueryForm(correlationId);
         clearResponses();
         document.location.hash = "request";

         $.ajax({
            data: $(event.target).serialize(),
            type: 'POST',
            url: event.target.action,
            success: function(data) {
               addXmlBlock("Request", data.request.xml, data.request.time);
               addXmlBlock("Response", data.response.xml, data.response.time);
            },
            error: function(err) {
               showQueryForm();
               document.location.hash = "error";
               $('.control-group').removeClass('error');
               if (err.status === 400) { // BadRequest
                  var response = JSON.parse(err.responseText);

                  $.each(response, function(index, value) {
                     $("#" + value.id).closest('.control-group').addClass('error');
                  })
                  if (response.message) {
                     addErrorMessage(response.message);

                     responseS.css("display", "block");
                     addXmlBlock("Failed request", response.request.xml, response.request.time);
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

      function addXmlBlock(name, xml, time) {
         var xmlBlock = xmlTemplate.clone().removeClass('template').removeAttr("id");
         xmlBlock.find("h3").text(name)
         xmlBlock.find(".prettyprint").text(xml);
         xmlBlock.find("h3").append($("<span/>", {"class": "time", text: time}));

         xmlBlock.prependTo(responseContent);

         prettyPrint();
         selectConnectionId();
      }

      function selectConnectionId() {
          if (window.getSelection) {
              var connectionId = responseContent.find(".tag:contains('connectionId') ~ span")[0];
              if (connectionId) {
                  var range = document.createRange();
                  range.selectNode(connectionId);
                  window.getSelection().removeAllRanges();
                  window.getSelection().addRange(range);
              }
          }
      }

      function hideQueryForm(correlationId) {
         if (eventSource) eventSource.close();

          eventSource = new EventSource(`/events/${correlationId}`);
          eventSource.addEventListener('message', (message) => {
              let data = JSON.parse(message.data);
              addXmlBlock('Callback Response', data.response.xml, data.response.time);
              increaseResponses();
          });

         queryS.css("display", "none");
         responseS.css("display", "block");
      }

      function showQueryForm() {
         if (eventSource) eventSource.close();
         eventSource = null;

         queryS.css("display", "block");
         responseS.css("display", "none");
      }

      function clearResponses() {
         responseS.find('div.xml-content').remove();
      }

      function increaseResponses() {
         var number = $("#numberOfResponses");
         number.text(parseInt(number.text()) + 1);
      }

      function checkLocation() {
         if (window.location.hash != lastHash) {
            if (lastHash == "#request") {
               clearResponses();
               showQueryForm();
            }
            lastHash = window.location.hash;
         }
      }

      setInterval(checkLocation, 100);
   }

   var initValidateProvider = function() {
      var providerSelect = $("#provider_id"),
          validIndicator = $("#valid-indicator"),
          tokenInput = $("#accessToken");

      if (!providerSelect.length) return;

      providerSelect.on('change', function(event) {
          validateProviderUrl();
      })
      tokenInput.on('change', function(event) {
          validateProviderUrl();
      })

      function validateProviderUrl() {
          providerUrlChecking();

          var data = {
              "nsa-id": providerSelect.val(),
              "token": tokenInput.val()
          };

          $.ajax({
              url: providerSelect.attr("data-validate-url"),
              type: 'POST',
              data: JSON.stringify(data),
              dataType: 'json',
              contentType: 'application/json; charset=utf-8',
              success: function(data) {
                  validProviderUrl(data);
              },
              error: function(err) {
                  validProviderUrl(false, err.status);
              }
          })
      }

      function providerUrlChecking() {
          validIndicator.text("Checking.....");
      }

      function validProviderUrl(data) {
          if (data.valid) {
              validIndicator.addClass("valid");
              validIndicator.text("Provider is valid");
          } else {
              validIndicator.removeClass("valid");
              validIndicator.text("Could not verify the provider, " + data.message);
          }
      }

      validateProviderUrl();
   }

   var initSelectInput = function() {
       $('input[type!="hidden"]').first().focus();
   }

   var initKeymaster = function() {
       key('shift+r', function() { clickMenu("reserve") });
       key('shift+m', function() { clickMenu("modify") });
       key('c', function() { clickMenu("commit") });
       key('a', function() { clickMenu("abort") });
       key('p', function() { clickMenu("provision") });
       key('r', function() { clickMenu("release") });
       key('t', function() { clickMenu("terminate") });
       key('q', function() { clickMenu("query") });
       key('m', function() { clickMenu("queryMessage") });
       key('s', function() { clickMenu("settings") });

       function clickMenu(menuId) {
           $(".navbar #"+menuId+" > a")[0].click();
       }
   }

   initExtraFields();
   initNsiRequestSubmit();
   initValidateProvider();
   initSelectInput();
   initKeymaster();

})
