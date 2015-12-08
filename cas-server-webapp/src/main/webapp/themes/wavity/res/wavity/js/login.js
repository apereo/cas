define(
	[
	 	'jquery', 
	 	'underscore', 
	 	'backbone', 
	 	'marionette'
	],
	function(
		$,
		_,
		Backbone,
		Marionette
	) {
	
		var LoginApplication = Marionette.Application.extend({
			initialize: function(options) {
				console.log("Wavity Login application initialize called");
			}
		});
		
		var Login = new LoginApplication();
		
		Login.on('initialize:before', function(options) {
			console.log("Wavity Login application initialize:before event called");
		});
		
		Login.on('initialize:after', function(options) {
			console.log("Wavity Login application initialize:after event called");
		});
		
		Login.on('start', function(options) {
			console.log("Wavity Login application start event called");
			if(Backbone.history){
				Backbone.history.start();
				console.log("Wavity Login application history tracking started");
			}
			this.Layout.render();
		});
		
		Login.addInitializer(function(options) {
			console.log("Wavity Login application add initializer called");
		});
		
		return Login;
});

/* EOF */