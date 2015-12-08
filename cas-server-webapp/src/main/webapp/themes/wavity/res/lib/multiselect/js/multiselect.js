/*
 * @license
 *
 * Multiselect v2.1.4
 * http://crlcu.github.io/multiselect/
 *
 * Copyright (c) 2015 Adrian Crisan
 * Licensed under the MIT license (https://github.com/crlcu/multiselect/blob/master/LICENSE)
 */

if (typeof jQuery === 'undefined') {
	throw new Error('multiselect requires jQuery');
}

;(function ($) {
	'use strict';

	var version = $.fn.jquery.split(' ')[0].split('.');
	
	if (version[0] < 2 && version[1] < 7) {
		throw new Error('multiselect requires jQuery version 1.7 or higher');
	}
})(jQuery);

;(function (factory) {
	if (typeof define === 'function' && define.amd) {
		// AMD. Register as an anonymous module depending on jQuery.
		define(['jquery'], factory);
	} else {
		// No AMD. Register plugin with global jQuery object.
		factory(jQuery);
	}
}(function ($) {
	'use strict';

 	var Multiselect = (function($) {
		/**	Multiselect object constructor
		 *
		 *	@class Multiselect
		 *	@constructor
		**/
		function Multiselect( $select, settings ) {
			var id = $select.prop('id');
			this.left = $select;
			this.right = $( settings.right ).length ? $( settings.right ) : $('#' + id + '_to');
			this.actions = {
				leftAll: 		$( settings.leftAll ).length ? $( settings.leftAll ) : $('#' + id + '_leftAll'),
				rightAll: 		$( settings.rightAll ).length ? $( settings.rightAll ) : $('#' + id + '_rightAll'),
				leftSelected:	$( settings.leftSelected ).length ? $( settings.leftSelected ) : $('#' + id + '_leftSelected'),
				rightSelected:	$( settings.rightSelected ).length ? $( settings.rightSelected ) : $('#' + id + '_rightSelected'),

				undo:			$( settings.undo ).length ? $( settings.undo ) : $('#' + id + '_undo'),
				redo:			$( settings.redo ).length ? $( settings.redo ) : $('#' + id + '_redo')
			};
			
			delete settings.leftAll;
			delete settings.leftSelected;
			delete settings.right;
			delete settings.rightAll;
			delete settings.rightSelected;

			this.options = {
				keepRenderingSort:	settings.keepRenderingSort,
				submitAllLeft:		settings.submitAllLeft !== undefined ? settings.submitAllLeft : true,
				submitAllRight:		settings.submitAllRight !== undefined ? settings.submitAllLeft : true,
				search: 			settings.search,
			};

			delete settings.keepRenderingSort, settings.submitAllLeft, settings.submitAllRight, settings.search;

			this.callbacks = settings;
			
			this.init();
		}
		
		Multiselect.prototype = {
			// Vars
			undoStack: [],
			redoStack: [],

			// Functions
			init: function() {
				var self = this;

				if (self.options.keepRenderingSort) {
					self.skipInitSort = true;

					self.callbacks.sort = function(a, b) {
						return $(a).data('position') > $(b).data('position') ? 1 : -1;
					};

					self.left.find('option').each(function(index, option) {
						$(option).data('position', index);
					});

					self.right.find('option').each(function(index, option) {
						$(option).data('position', index);
					});
				}

				if ( typeof self.callbacks.startUp == 'function' ) {
					self.callbacks.startUp( self.left, self.right );
				}
				
				if ( !self.skipInitSort && typeof self.callbacks.sort == 'function' ) {
					self.left.find('option')
						.sort(self.callbacks.sort)
						.appendTo(self.left);
					
					self.right
						.each(function(i, select) {
						$(select).find('option')
							.sort(self.callbacks.sort)
							.appendTo(select);
					});
				}

				if (self.options.search && self.options.search.left) {
					self.options.search.left = $(self.options.search.left);
					self.left.before(self.options.search.left);
				}

				if (self.options.search && self.options.search.right) {
					self.options.search.right = $(self.options.search.right);
					self.right.before($(self.options.search.right));
				}
				
				self.events( self.actions );
			},
			
			events: function( actions ) {
				var self = this;
				
				self.left.on('dblclick', 'option', function(e) {
					e.preventDefault();
					self.moveToRight(this, e);
				});
				
				self.right.on('dblclick', 'option', function(e) {
					e.preventDefault();
					self.moveToLeft(this, e);
				});

				// append left filter
				if (self.options.search && self.options.search.left) {
					self.options.search.left.on('keyup', function(e) {
						var search = this.value;

						self.left.find('option').each(function(i, option) {
							if (option.text.indexOf(search) > -1) {
								$(option).show();
							} else {
								$(option).hide();
							}
						});
					});
				}

				// append right filter
				if (self.options.search && self.options.search.right) {
					self.options.search.right.on('keyup', function(e) {
						var search = this.value;

						self.right.find('option').each(function(i, option) {
							if (option.text.indexOf(search) > -1) {
								$(option).show();
							} else {
								$(option).hide();
							}
						});
					});
				}

				// select all the options from left and right side
				// when submiting the parent form
				self.right.closest('form').on('submit', function(e) {
					self.left.children().prop('selected', self.options.submitAllLeft);
					self.right.children().prop('selected', self.options.submitAllRight);
				});
				
				// dblclick support for IE
				if ( navigator.userAgent.match(/MSIE/i)  || navigator.userAgent.indexOf('Trident/') > 0 || navigator.userAgent.indexOf('Edge/') > 0) {
					self.left.dblclick(function(e) {
						actions.rightSelected.trigger('click');
					});
					
					self.right.dblclick(function(e) {
						actions.leftSelected.trigger('click');
					});
				}
				
				actions.rightSelected.on('click', function(e) {
					e.preventDefault();
					var options = self.left.find('option:selected');
					
					if ( options ) {
						self.moveToRight(options, e);
					}

					$(this).blur();
				});
				
				actions.leftSelected.on('click', function(e) {
					e.preventDefault();
					var options = self.right.find('option:selected');
					
					if ( options ) {
						self.moveToLeft(options, e);
					}

					$(this).blur();
				});
				
				actions.rightAll.on('click', function(e) {
					e.preventDefault();
					var options = self.left.find('option');
					
					if ( options ) {
						self.moveToRight(options, e);
					}

					$(this).blur();
				});
				
				actions.leftAll.on('click', function(e) {
					e.preventDefault();
					
					var options = self.right.find('option');
					
					if ( options ) {
						self.moveToLeft(options, e);
					}

					$(this).blur();
				});

				actions.undo.on('click', function(e) {
					e.preventDefault();

					self.undo(e);
				});

				actions.redo.on('click', function(e) {
					e.preventDefault();

					self.redo(e);
				});
			},
			
			moveToRight: function( options, event, silent, skipStack ) {
				var self = this;

				if ( typeof self.callbacks.moveToRight == 'function' ) {
					return self.callbacks.moveToRight( self, options, event, silent, skipStack );
				} else {
					if ( typeof self.callbacks.beforeMoveToRight == 'function' && !silent ) {
						if ( !self.callbacks.beforeMoveToRight( self.left, self.right, options ) ) {
							return false;
						}
					}
					
					self.right.append(options);

					if ( !skipStack ) {
						self.undoStack.push(['right', options ]);
						self.redoStack = [];
					}
					
					if ( typeof self.callbacks.sort == 'function' && !silent ) {
						self.right.find('option').sort(self.callbacks.sort).appendTo(self.right);
					}
					
					if ( typeof self.callbacks.afterMoveToRight == 'function' && !silent ){
						self.callbacks.afterMoveToRight( self.left, self.right, options );
					}
					
					return self;
				}
			},
			
			moveToLeft: function( options, event, silent, skipStack ) {
				var self = this;
				
				if ( typeof self.callbacks.moveToLeft == 'function' ) {
					return self.callbacks.moveToLeft( self, options, event, silent, skipStack );
				} else {
					if ( typeof self.callbacks.beforeMoveToLeft == 'function' && !silent ) {
						if ( !self.callbacks.beforeMoveToLeft( self.left, self.right, options ) ) {
							return false;
						}
					}
						
					self.left.append(options);
					
					if ( !skipStack ) {
						self.undoStack.push(['left', options ]);
						self.redoStack = [];
					}
					
					if ( typeof self.callbacks.sort == 'function' && !silent ) {
						self.left.find('option').sort(self.callbacks.sort).appendTo(self.left);		
					}
					
					if ( typeof self.callbacks.afterMoveToLeft == 'function' && !silent ) {
						self.callbacks.afterMoveToLeft( self.left, self.right, options );
					}
					
					return self;
				}
			},

			undo: function(event) {
				var self = this;
				var last = self.undoStack.pop();

				if ( last ) {
					self.redoStack.push(last);

					switch(last[0]) {
						case 'left':
							self.moveToRight(last[1], event, false, true);
							break;
						case 'right':
							self.moveToLeft(last[1], event, false, true);
							break;
					}
				}
			},
			redo: function(event) {
				var self = this;
				var last = self.redoStack.pop();

				if ( last ) {
					self.undoStack.push(last);

					switch(last[0]) {
						case 'left':
							self.moveToLeft(last[1], event, false, true);
							break;
						case 'right':
							self.moveToRight(last[1], event, false, true);
							break;
					}
				}
			}
		}
		
		return Multiselect;
	})($);
	
	$.multiselect = {
		defaults: {
			/**	will be executed once - remove from $left all options that are already in $right
			 *
			 *	@method startUp
			**/
			startUp: function( $left, $right ) {
				$right.find('option').each(function(index, option) {
					$left.find('option[value="' + option.value + '"]').remove();
				});
			},

			/**	will be executed each time before moving option[s] to right
			 *  
			 *	IMPORTANT : this method must return boolean value
			 *      true    : continue to moveToRight method
			 *      false   : stop
			 * 
			 *  @method beforeMoveToRight
			 *  @attribute $left jQuery object
			 *  @attribute $right jQuery object
			 *  @attribute options HTML object (the option[s] which was selected to be moved)
			 *  
			 *  @default true
			 *  @return {boolean}
			**/
			beforeMoveToRight: function($left, $right, options) { return true; },

			/*	will be executed each time after moving option[s] to right
			 * 
			 *  @method afterMoveToRight
			 *  @attribute $left jQuery object
			 *  @attribute $right jQuery object
			 *  @attribute options HTML object (the option[s] which was selected to be moved)
			**/
			afterMoveToRight: function($left, $right, options){},

			/**	will be executed each time before moving option[s] to left
			 *  
			 *	IMPORTANT : this method must return boolean value
			 *      true    : continue to moveToRight method
			 *      false   : stop
			 * 
			 *  @method beforeMoveToLeft
			 *  @attribute $left jQuery object
			 *  @attribute $right jQuery object
			 *  @attribute options HTML object (the option[s] which was selected to be moved)
			 *  
			 *  @default true
			 *  @return {boolean}
			**/
			beforeMoveToLeft: function($left, $right, option){ return true; },

			/*	will be executed each time after moving option[s] to left
			 * 
			 *  @method afterMoveToLeft
			 *  @attribute $left jQuery object
			 *  @attribute $right jQuery object
			 *  @attribute options HTML object (the option[s] which was selected to be moved)
			**/
			afterMoveToLeft: function($left, $right, option){},

			/**	sort options by option text
			 * 
			 *  @method sort
			 *  @attribute a HTML option
			 *  @attribute b HTML option
			 *
			 *  @return 1/-1
			**/
			sort: function(a, b) {
				if (a.innerHTML == 'NA') {
					return 1;   
				} else if (b.innerHTML == 'NA') {
					return -1;   
				}
				
				return (a.innerHTML > b.innerHTML) ? 1 : -1;
			},
		}
	};

	$.fn.multiselect = function( options ) {
		return this.each(function() {
			var $this = $(this),
				data = $this.data();
			
			var settings = $.extend({}, $.multiselect.defaults, data, options);
			
			return new Multiselect($this, settings);
		});
	};
}));
