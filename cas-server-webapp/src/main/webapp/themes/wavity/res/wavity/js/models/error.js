define(['backbone'], function(Backbone) {
	return Backbone.Model.extend({
		defaults: {
			code: 'RUNTIME_ERROR',
			context: 'application'
		}
	});
});

// EOF

