define(
	[
		'require', 
		'jquery', 
		'underscore', 
		'backbone', 
		'marionette', 
		'js/login', 
		'js/routers/loginrouter', 
		'js/controllers/logincontroller',
		'js/models/error',
		'js/layouts/login/loginLayout'
	], 
	function(
		require, 
		$, 
		_, 
		Backbone, 
		Marionette, 
		Login, 
		Router, 
		Controller,
		Error,
		LoginLayout
	) {
	return function(error) {
		console.log("OneTeam login starting initialization ...");
		if ( (typeof(error) !== "undefined") && (error !== null) && (error.length > 0) && (error !== "null") ) {
			Login.Error = new Error({code: error, context: 'authentication'});
		}
		Login.Controller = new Controller();
		Login.Router = new Router({ controller: Login.Controller });
		Login.Layout = new LoginLayout();
		Login.start();
		console.log("... finished OneTeam login initialization.");
	}
});

// EOF
