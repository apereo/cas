define(
	[
		'jquery', 
		'underscore', 
		'backbone', 
		'marionette',
		'bootstrap',
		'js/login',
		'js/views/login/loginviewhelper',
		'text!templates/login/loginHeader.html'
	], 
	function(
		$, 
		_, 
		Backbone, 
		Marionette, 
		Bootstrap,
		Login,
		LoginHelper,
		LoginHeaderViewTemplate
	)  {
	return Marionette.ItemView.extend({
		template: LoginHeaderViewTemplate,
		templateHelpers: LoginHelper,
		initialize: function() {
			
		},
		events: {
			
		},
		
		onShow: function(){					
		
		}
	});
});


/* EOF */
