/*
* One Team main RequireJS configuration
*/
require.config({
	baseUrl: 'themes/wavity/res/lib',
	paths: {
		/*
		 * Initial template loading 
		 */
		'js': '../wavity/js',
		'templates': '../wavity/templates',
		'assets': '../wavity',
		'assets-css': '../wavity/css',
		'jquery': 'jquery-2.1.4/jquery-2.1.4',
		'jquery.uploadfile': 'jquery.uploadfile-3.1.10/js/jquery.uploadfile',
		'jquery-ui': 'jquery-ui-1.11.4/js/jquery-ui-1.11.4.custom',
		'jquery-ui-assets': 'jquery-ui-1.11.4',
		'jquery-mobile': 'jquery.mobile-1.4.2/jquery.mobile-1.4.2',
		'jquery-mobile-assets': 'jquery.mobile-1.4.2',
		
		/*
		 * Java Script Framework Plug-ins
		 */
		'underscore': 'underscore-1.8.3/underscore',
		'backbone': 'backbone-1.2.0/backbone',
		'marionette': 'marionette-2.4.1/backbone.marionette',
		'text': 'require-2.1.20/text',
		'i18n': 'require-2.1.20/i18n',
		'domReady': 'require-2.1.20/domReady',
		'gridlocale-en': 'jquery-jqgrid-4.6.0/js/i18n/grid.locale-en',
		'require-css': 'require-css-0.1.2/css',
		'handlebars': 'handlebars-1.3.0/handlebars-v1.3.0',
		'knockout': 'knockout-3.0.0/knockout-3.0.0',
		
		/*
		 * HTML5 Framework Plug-ins
		 */
		'bootstrap': 'bootstrap-3.3.4/js/bootstrap',
		'bootstrap-multiselect': 'bootstrap-multiselect-2.0/js/bootstrap-multiselect',
		'jasny-bootstrap': 'jasny-bootstrap-3.1.0/js/jasny-bootstrap',
		'jasny-bootstrap-assets': 'jasny-bootstrap-3.1.0',		
		'font-awesome-assets': 'font-awesome-4.0.3',
		
		/*
		 * Template Custom Styles
		 */
		'CssCustom': 'custom/css',
		'CssForm': 'forms/css',
		'jsslider': 'custom/js/slider.min',
		'JsCustomCalendarActions': 'custom/js/calendaractions',
		'JsiCheckbox': 'custom/js/icheck',
		'JsFunctions': 'custom/js/functions',
		'JsAutoSize': 'custom/js/autosize.min',
		'JsMultiSelectChoosen': 'custom/js/chosen.min',
		'JsWYSIWYGeditor': 'custom/js/editor',
		'JsScroll': 'custom/js/scroll.min',
		'JsDateTimePicker': 'custom/js/datetimepicker.min',
		'JsFileUpload': 'custom/js/fileupload.min',
		'themeSettings': 'custom/js/themeSettings',
		'jquery-dateFormat': 'custom/js/jquery-dateFormat',
		
		/*
		 * Third Party jQuery Plug-ins
		 */
		'jquery-slimscroll': 'jquery-slimscroll-1.3.0/jquery.slimscroll',					// for Slim-Scroll
		'jquery-slimscrollhorizontal': 'jquery-slimscroll-1.3.0/slimscrollhorizontal',		// for Horizontal Slim-Scroll
		'jquery-jqgrid': 'jquery-jqgrid-4.6.0/js/jquery.jqGrid.src',						// for jqGrid
		'jquery-jqgrid-assets': 'jquery-jqgrid-4.6.0',										// for jqGrid Assets
		'JsJqueryKnob':'custom/js/jquery.knob.min',											// for Circular graph in task to do
		'jsSelectDropdownMenu': 'custom/js/select.min',										// for Bootstrap Dropdown
		'JsMoment': 'calendar/js/moment',													// for jQuery Full Calendar assets 													
		'JsCalendar': 'calendar/js/calendar.min',											// for jQuery Full Calendar
		'JsblockUI': 'custom/js/jquery.blockui',											// for block the operations
		'JsGridster' : 'jquery-gridster/js/jquery.gridster',								// drag and drop
		'CssProgress': 'jrange',															// for progress bar
		'progressRange': 'jrange/js/jquery.range',											// for progress bar range
		'JsBootSideMenu': 'bootstrap-bootsidemenu/js/BootSideMenu',							// for Side Menu
		'bootstrap-date': 'bootstrap-datepicker-2.0/js/bootstrap-datepicker',				// for Bootstrap Date entry
		'jquery-colorPicker':'Jquery-minicolor/js/jquery.minicolors',					    // for Bootstrap  color picker
		//'multiselect':'multiselect/js/multiselect',
		'JsBootstrapSwitch':'bootstrap-switch/js/bootstrap-switch',							// for Bootstrap switch
		'query-builder': 'jquery-builder/js/jquery-builder',								// for reports dynamic query builder
		'jquery-extendext': 'jquery-builder/js/jquery-extendext',							// for reports dynamic query builder
		'query-builder-moment': 'jquery-builder/js/moment',									// for reports dynamic query builder
		'CssJqueryBuilder':'jquery-builder/css/',											// for jquery-builder
		'JsflexSlider':'flexSlider/js/jquery.flexslider',									// for Flex slider for dash boards reports
		
		/*
		 * for Data Tables
		 */
		'datatables': 'dataTables-1.10.7/js/jquery.dataTables',
		'CssDataTables': 'dataTables-1.10.7/css/',
		'dTBootstrap': 'dataTables-1.10.7/js/dataTables.bootstrap',
		'jquery-treetable' : 'jquery-treetable/js/jquery.treetable',
		
		/*
		 * @ form level with proper validations 
		 */
		'jquery-form': 'jquery.form-3.51.0/jquery.form',
		'JsValidate':'custom/js/validate.min',
		'JsValidationEngine':'custom/js/validationEngine.min',
		'JsInputMask':'custom/js/input-mask.min',
		'JsMaskInput' :'jquery-inputmask/jquery.maskedinput',
		
		/*
		 * High Charts for Reports
		 */
		'JsHighcharts':'Highcharts-4.1.3/js/highcharts',
		'JsHighChartsExporting':'Highcharts-4.1.3/js/exporting',
		
		/*
		 * @ Space Tree
		 */
		'JsOrgChart': 'OrgChart/js/jquery.jOrgChart'
		
	},
	shim: {
		'jquery-ui': {
			deps: [ 'jquery' ]
		},
		'jquery.uploadfile': {
			deps: [ 'jquery' ]
		},
		'jquery-mobile': {
			deps: ['jquery']
		},
		
		'underscore': {
			/*exports: '_'*/
		},
		'backbone': {
			deps: [ 'underscore', 'jquery' ]
			/*exports: 'Backbone'*/
		},
		'gridlocale-en': {
			deps: [ 'jquery' ]
		},
		'handlebars': {
			deps: [ 'text' ]
		},
		'knockout': {
			deps: [ 'jquery' ]
		},
		'bootstrap': {
			deps: ['jquery', 'jquery-ui' ]
		},
		'bootstrap-multiselect':{
			deps: ['jquery']
		},
		'jasny-bootstrap': {
			deps: [ 'bootstrap' ]
		},		
		'themeSettings': {
			deps: [ 'jquery' ]
		},
		
		'jquery-slimscroll': {
			deps: [ 'jquery' ]
		},
		'jquery-slimscrollhorizontal': {
			deps: [ 'jquery' ]
		},
		'jquery-jqgrid': {
			deps: [ 'jquery' ]
		},
		'jsslider': {
			deps: [ 'jquery','jquery-ui','bootstrap' ]
		},
		'jsSelectDropdownMenu': {
			deps: [ 'jquery' ]
		},
		'JsCustomCalendarActions': {
			deps: [ 'jquery' ]
		},
		'JsiCheckbox': {
			deps: [ 'jquery' ]
		},
		'JsFunctions': {
			deps: [ 'jquery' ]
		},
		'JsAutoSize': {
			deps: [ 'jquery' ]
		},
		'JsMultiSelectChoosen': {
			deps: [ 'jquery' ]
		},
		'JsWYSIWYGeditor': {
			deps: [ 'jquery' ]
		},
		'JsScroll': {
			deps: [ 'jquery' ]
		},
		'JsDateTimePicker': {
			deps: [ 'jquery' ]
		},
		'JsJqueryKnob': {
			deps: [ 'jquery' ]
		},
		'JsblockUI': {
			deps: [ 'jquery', 'jquery-ui' ]
		},
		'JsGridster': {
			deps: [ 'jquery' ]
		},
		
		'JsMoment': {
			deps: [ 'jquery' ]
		},
		'JsCalendar': {
			deps: [ 'jquery', 'JsMoment' ]
		},
		'JsBootSideMenu': {
			deps: [ 'jquery', 'jquery-ui' ]
		},
		'datatables': {
			deps: [ 'jquery' ]
		},
		'dTBootstrap': {
			deps: [ 'jquery', 'bootstrap' ]
		},		
		
		'JsFileUpload': {
			deps: [ 'jquery' ]
		},
		'JsValidate': {
			deps: [ 'jquery' ]
		},
		'JsValidationEngine': {
			deps: [ 'jquery','JsValidate' ]
		},
		'JsInputMask': {
			deps: [ 'jquery' ]
		},
		'jquery-form': {
			deps: [ 'jquery' ]
		},
		'jquery-dateFormat': {
			deps: [ 'jquery' ]
		},
		'progressRange': {
			deps: ['jquery']
		},
		'JsMaskInput': {
			deps: [ 'jquery' ]
		},		
		'JsHighcharts':{
			deps: [ 'jquery' ]
		},
		'JsHighChartsExporting':{
			deps: ['jquery','JsHighcharts' ]
		},		
		'JsOrgChart': {
			deps: [ 'jquery' ]
		},		
		'bootstrap-date': {
			deps: [ 'jquery' ]
		},
		'jquery-treetable': {
			deps: [ 'jquery' ]
		},
		'jquery-colorPicker': {
			deps: [ 'jquery' ]
		},/*
		'multiselect' : {
			deps: [ 'jquery', 'bootstrap' ]
		},*/
		'JsBootstrapSwitch': {
			deps: ['jquery' ]
		},
		'query-builder': {
			deps: [ 'jquery', 'query-builder-moment', 'jquery-extendext']
		},
		'JsflexSlider' : {
			deps: [ 'jquery', 'jquery-ui' ]
		}
	},
	deps : [ 'jquery', 'underscore' ],
	map: {
		'*': {
			'css': 'require-css'
		}
	},
	urlArgs: 'bust=v1'
});
// EOF
