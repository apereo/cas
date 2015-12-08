define(
	[
		'backbone',
		'marionette',
		'js/oneteam',
		'js/layouts/home/home',
		'js/views/admin/home',
		'js/views/tasktodo/tasktodo',
		'js/layouts/home/widgetsLayout'
	],
	function(
		Backbone,
		Marionette,
		OneTeam,
		ViewMainLayout,
		ViewAdminLayout,
		ViewTaskToDo,
		ViewWidgetsLayout
	) {
	return Backbone.Marionette.Controller.extend({
		initialize: function(options) {
			console.log("ONETEAM controller initialize called");
		},
		// routes, events
		home: function() {
			console.log("ONETEAM controller home route called");
		},
		showHome: function(){
			OneTeam.Layout.main.show(new ViewMainLayout());
		},
		showAdminHome: function(){
			OneTeam.Layout.main.show(new ViewAdminLayout());
		},
		showTaskToDo: function() {
			OneTeam.Layout.main.widgets.taskToDo.show(new ViewTaskToDo());
		},
		showWidgets: function() {
			OneTeam.Layout.main.widgets.show(new ViewWidgetsLayout());
		}
		
	});
});

// EOF
