/*!
 * jQuery.extendext 0.1.1
 *
 * Copyright 2014 Damien "Mistic" Sorel (http://www.strangeplanet.fr)
 * Licensed under MIT (http://opensource.org/licenses/MIT)
 * 
 * Based on jQuery.extend by jQuery Foundation, Inc. and other contributors
 */

/*jshint -W083 */

(function(root, factory) {
    if (typeof define === 'function' && define.amd) {
        define(['jquery'], factory);
    }
    else {
        factory(root.jQuery);
    }
}(this, function($) {
  "use strict";

  $.extendext = function() {
    var options, name, src, copy, copyIsArray, clone,
      target = arguments[0] || {},
      i = 1,
      length = arguments.length,
      deep = false,
      arrayMode = 'default';

    // Handle a deep copy situation
    if ( typeof target === "boolean" ) {
      deep = target;

      // Skip the boolean and the target
      target = arguments[ i++ ] || {};
    }

    // Handle array mode parameter
    if ( typeof target === "string" ) {
      arrayMode = $([target.toLowerCase(), 'default']).filter(['default','concat','replace','extend'])[0];

      // Skip the string param
      target = arguments[ i++ ] || {};
    }

    // Handle case when target is a string or something (possible in deep copy)
    if ( typeof target !== "object" && !$.isFunction(target) ) {
      target = {};
    }

    // Extend jQuery itself if only one argument is passed
    if ( i === length ) {
      target = this;
      i--;
    }

    for ( ; i < length; i++ ) {
      // Only deal with non-null/undefined values
      if ( (options = arguments[ i ]) !== null ) {
        // Special operations for arrays
        if ($.isArray(options) && arrayMode !== 'default') {
          clone = target && $.isArray(target) ? target : [];

          switch (arrayMode) {
          case 'concat':
            target = clone.concat( $.extend( deep, [], options ) );
            break;

          case 'replace':
            target = $.extend( deep, [], options );
            break;

          case 'extend':
            options.forEach(function(e, i) {
              if (typeof e === 'object') {
                var type = $.isArray(e) ? [] : {};
                clone[i] = $.extendext( deep, arrayMode, clone[i] || type, e );

              } else if (clone.indexOf(e) === -1) {
                clone.push(e);
              }
            });

            target = clone;
            break;
          }

        } else {
          // Extend the base object
          for ( name in options ) {
            src = target[ name ];
            copy = options[ name ];

            // Prevent never-ending loop
            if ( target === copy ) {
              continue;
            }

            // Recurse if we're merging plain objects or arrays
            if ( deep && copy && ( $.isPlainObject(copy) ||
              (copyIsArray = $.isArray(copy)) ) ) {

              if ( copyIsArray ) {
                copyIsArray = false;
                clone = src && $.isArray(src) ? src : [];

              } else {
                clone = src && $.isPlainObject(src) ? src : {};
              }

              // Never move original objects, clone them
              target[ name ] = $.extendext( deep, arrayMode, clone, copy );

            // Don't bring in undefined values
            } else if ( copy !== undefined ) {
              target[ name ] = copy;
            }
          }
        }
      }
    }

    // Return the modified object
    return target;
  };
}));
