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
	
		var OneTeamApplication = Marionette.Application.extend({
		  initialize: function(options) {
			console.log("OneTeam application initialize called");
		  }
		});
		
		var OneTeam = new OneTeamApplication();
		
		OneTeam.on('initialize:before', function(options) {
			console.log("OneTeam application initialize:before event called");
		});
		OneTeam.on('initialize:after', function(options) {
			console.log("OneTeam application initialize:after event called");
		});
		OneTeam.on('start', function(options) {
			console.log("OneTeam application start event called");
			if(Backbone.history){
				Backbone.history.start();
				console.log("OneTeam application history tracking started");
			}
			this.Layout.render();
		});
		OneTeam.addInitializer(function(options) {
			console.log("OneTeam application add initializer called");
		});
		return OneTeam;
});

// EOF
