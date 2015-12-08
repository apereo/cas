define(
	[
		'jquery', 
		'underscore', 
		'backbone', 
		'marionette',
		'bootstrap',
		'js/login',
		'js/views/login/loginviewhelper',
		'text!templates/login/loginFooter.html'
	], 
	function(
		$, 
		_, 
		Backbone, 
		Marionette, 
		Bootstrap,
		Login,
		LoginHelper,
		LoginFooterViewTemplate
	)  {
	return Marionette.ItemView.extend({
		template: LoginFooterViewTemplate,
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
