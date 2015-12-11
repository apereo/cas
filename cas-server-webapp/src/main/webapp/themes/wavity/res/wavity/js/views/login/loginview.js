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
		
			$("#appIcon").attr("src", $('input[name=defaultAppLogo]').val());
			/*var appName = $('input[name=appName]').val().toLowerCase();
			var that = this;
			$.ajax({
				headers : {
					'Accept' : 'application/json',
					'Content-Type' : 'application/json'
				},
				url: 'http://accounts.wavity.com:8080/scim/v2/CloudServices/',
				type: 'GET',
				dataType: 'json',
				async: false,
				success: function(response) {
					$(response.Resources).each(function() {
						if(this.serviceName.split(" ").join("").toLowerCase() === appName && this.serviceThumbnails !== undefined) {
							$("#appIcon").attr("src", this.serviceThumbnails[0].value);
						}
					});
				},
				error: function(response) {
					console.log(response);
				}
			});*/
			
			
			$("#domainIcon").attr("src", $('input[name=defaultTenantLogo]').val());
			/*var tenantName = $('input[name=tenantName]').val();
			var that = this;
			$.ajax({
				headers : {
					'Accept' : 'application/json',
					'Content-Type' : 'application/json'
				},
				url: 'http://accounts.wavity.com:8080/scim/v2/Tenants/',
				type: 'GET',
				dataType: 'json',
				async: false,
				success: function(response) {
					$(response.Resources).each(function() {
						if(this.tenantName.toLowerCase() === tenantName && this.tenantThumbnails !== undefined) {
							$("#domainIcon").attr("src", this.tenantThumbnails[0].value);
						}
					});
				},
				error: function(response) {
					console.log(response);
				}
			});*/
			
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
