define(
	[ 
	 	'jquery', 
	 	'underscore', 
	 	'backbone', 
	 	'marionette',
	 	'js/views/login/loginHeader',
	 	'js/views/login/loginview',
	 	'js/views/login/loginFooter'
	], 
	function(
		$,
		_,
		Backbone, 
		Marionette,
		LoginHeaderView,
		LoginLayout,
		LoginFooterView
	) {
		return Backbone.Marionette.LayoutView.extend({
			el: 'body',
			template: false,
			regions: {
				loginHeader: "#ot-header",
				main: "#ot-main",
				loginFooter: "#ot-footer"
			},
			onRender: function() {
				console.log("Wavity Login application layout onRender called");
				this.loginHeader.show(new LoginHeaderView());
				this.main.show(new LoginLayout());
				this.loginFooter.show(new LoginFooterView());
			},			
			onShow: function() {
				console.log("Wavity application layout onShow called");
			}
		});
});

// EOF
