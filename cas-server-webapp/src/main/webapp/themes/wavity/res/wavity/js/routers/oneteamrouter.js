define(
		[
		 'backbone', 
		 'marionette'
		 ], 
		 function(
				 Backbone, 
				 Marionette
				 ) {
						return Backbone.Marionette.AppRouter.extend({
							initialize: function(options) {
								console.log("1-Team router initialize called");
							},
							// all routes need to be defined in controller
							appRoutes: {
								'': 'home',
								'tasks':'showTaskToDo',
								'showMain': 'showHome',
								'adminHome': 'showAdminHome',
								'widgetsHome': 'showWidgets',
								'taskToDo': 'showTaskToDo'
							}
						});
});

// EOF

