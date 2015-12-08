define(['backbone', 'marionette'], function(Backbone, Marionette) {
	return Backbone.Marionette.AppRouter.extend({
		initialize: function(options) {
			console.log("OneTeam login router initialize called");
		},
		// all routes need to be defined in controller
		appRoutes: {
			'': 'login'
		}
	});
});

// EOF

