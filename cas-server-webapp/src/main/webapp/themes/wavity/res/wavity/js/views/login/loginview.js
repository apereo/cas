define(
	[
		'jquery', 
		'underscore', 
		'backbone', 
		'marionette',
		'bootstrap',
		'js/login',
		'js/views/login/loginviewhelper', 
		'text!templates/login/login.html'
	], 
	function(
		$, 
		_, 
		Backbone, 
		Marionette, 
		Bootstrap,
		Login,
		LoginHelper,
		LoginViewTemplate
	)  {
	return Marionette.ItemView.extend({
		template: LoginViewTemplate,
		templateHelpers: LoginHelper,
		initialize: function() {
			console.log("Login View Initialize()");
		},
	
		events: {
			"click .notification-close"	:	"errormsgclose"
		},
		
		onShow: function(){
			$(".usertext").focus();
			$('#myCarousel').carousel({
				interval: 3000
			});		
			if(Login.hasOwnProperty('Error'))
			{
				$('.notification_inner').show(500);
	            $('.ot_username, .ot_password').addClass('has-error');
	            $('.ot_username, .ot_password').find('.help-inline').addClass('oneteam-error-msg');			            
			}		
		
			$("#appIcon").attr("src", $('input[name=appLogo]').val());
			$("#domainIcon").attr("src", $('input[name=tenantLogo]').val());
			
			$('form input[name=lt]').val($('input[name=loginTicket]').val());
			$('form input[name=execution]').val($('input[name=flowExecutionKey]').val());
			$('form input[name=service]').val($('input[name=serviceUrl]').val());
		},
		errormsgclose: function() {
    	   $('.notification_inner').hide(500);
    	   $('.ot_username, .ot_password').removeClass('has-error');
    	   $('.ot_username .help-inline, .ot_password .help-inline').removeClass('oneteam-error-msg').text('');
       }
	});
});


/* EOF */
