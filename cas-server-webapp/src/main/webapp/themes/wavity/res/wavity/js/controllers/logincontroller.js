define(
	[
		'backbone',
		'marionette',
		'js/login',
		'js/views/login/loginview'
	],
	function(
		Backbone,
		Marionette,
		Login,		
		LoginView
	) {
	return Backbone.Marionette.Controller.extend({
		initialize: function(options) {
			console.log("Wavity login controller initialize called");
		},
		// routes, events
		login: function() {
			console.log("Wavity login controller login route called");
		}
	});
});

// EOF
