define(
	[ 
		'jquery', 
		'underscore', 
		'backbone', 
		'marionette', 
		'js/login', 
		'i18n!assets/nls/login'
	 ], 
	 function(
		 $, 
		_, 
		Backbone, 
		Marionette, 
		Login, 
		LoginNLS
	 ) {
	return {
		type: 'loginhelper',
		getLoginNLS: function() {
			return LoginNLS;
		},
		getErrorMessage: function() {
			var msg = "";
			if(Login.hasOwnProperty('Error')) {
				var code = Login.Error.get('code');
				switch(code) {
				case "ENTRY_EXISTS":
					msg = LoginNLS.ENTRY_EXISTS;
					break;
				case "USER_ENTRY_PASSWORD_REQUIRES_RESET":
					msg = LoginNLS.USER_ENTRY_PASSWORD_REQUIRES_RESET;
					break;
				case "INVALID_CREDENTIALS":
					msg = LoginNLS.INVALID_CREDENTIALS;
					break;
				default:
					break;
				}
			}
			return msg;
		}
	};
});
// EOF
