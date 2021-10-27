/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
(function (window, undefined) {
	var document = window.document,
	navigator = window.navigator,
	location = window.location;
	var jQuery = (function () {
		var jQuery = function (selector, context) {
			return new jQuery.fn.init(selector, context, rootjQuery)
		},
		_jQuery = window.jQuery,
		_$ = window.$,
		rootjQuery,
		quickExpr = /^(?:[^<]*(<[\w\W]+>)[^>]*$|#([\w\-]*)$)/,
		rnotwhite = /\S/,
		trimLeft = /^\s+/,
		trimRight = /\s+$/,
		rdigit = /\d/,
		rsingleTag = /^<(\w+)\s*\/?>(?:<\/\1>)?$/,
		rvalidchars = /^[\],:{}\s]*$/,
		rvalidescape = /\\(?:["\\\/bfnrt]|u[0-9a-fA-F]{4})/g,
		rvalidtokens = /"[^"\\\n\r]*"|true|false|null|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?/g,
		rvalidbraces = /(?:^|:|,)(?:\s*\[)+/g,
		rwebkit = /(webkit)[ \/]([\w.]+)/,
		ropera = /(opera)(?:.*version)?[ \/]([\w.]+)/,
		rmsie = /(msie) ([\w.]+)/,
		rmozilla = /(mozilla)(?:.*? rv:([\w.]+))?/,
		userAgent = navigator.userAgent,
		browserMatch,
		readyList,
		DOMContentLoaded,
		toString = Object.prototype.toString,
		hasOwn = Object.prototype.hasOwnProperty,
		push = Array.prototype.push,
		slice = Array.prototype.slice,
		trim = String.prototype.trim,
		indexOf = Array.prototype.indexOf,
		class2type = {};
		jQuery.fn = jQuery.prototype = {
			constructor : jQuery,
			init : function (selector, context, rootjQuery) {
				var match,
				elem,
				ret,
				doc;
				if (!selector) {
					return this
				}
				if (selector.nodeType) {
					this.context = this[0] = selector;
					this.length = 1;
					return this
				}
				if (selector === "body" && !context && document.body) {
					this.context = document;
					this[0] = document.body;
					this.selector = selector;
					this.length = 1;
					return this
				}
				if (typeof selector === "string") {
					if (selector.charAt(0) === "<" && selector.charAt(selector.length - 1) === ">" && selector.length >= 3) {
						match = [null, selector, null]
					} else {
						match = quickExpr.exec(selector)
					}
					if (match && (match[1] || !context)) {
						if (match[1]) {
							context = context instanceof jQuery ? context[0] : context;
							doc = (context ? context.ownerDocument || context : document);
							ret = rsingleTag.exec(selector);
							if (ret) {
								if (jQuery.isPlainObject(context)) {
									selector = [document.createElement(ret[1])];
									jQuery.fn.attr.call(selector, context, true)
								} else {
									selector = [doc.createElement(ret[1])]
								}
							} else {
								ret = jQuery.buildFragment([match[1]], [doc]);
								selector = (ret.cacheable ? jQuery.clone(ret.fragment) : ret.fragment).childNodes
							}
							return jQuery.merge(this, selector)
						} else {
							elem = document.getElementById(match[2]);
							if (elem && elem.parentNode) {
								if (elem.id !== match[2]) {
									return rootjQuery.find(selector)
								}
								this.length = 1;
								this[0] = elem
							}
							this.context = document;
							this.selector = selector;
							return this
						}
					} else {
						if (!context || context.jquery) {
							return (context || rootjQuery).find(selector)
						} else {
							return this.constructor(context).find(selector)
						}
					}
				} else {
					if (jQuery.isFunction(selector)) {
						return rootjQuery.ready(selector)
					}
				}
				if (selector.selector !== undefined) {
					this.selector = selector.selector;
					this.context = selector.context
				}
				return jQuery.makeArray(selector, this)
			},
			selector : "",
			jquery : "1.6.1",
			length : 0,
			size : function () {
				return this.length
			},
			toArray : function () {
				return slice.call(this, 0)
			},
			get : function (num) {
				return num == null ? this.toArray() : (num < 0 ? this[this.length + num] : this[num])
			},
			pushStack : function (elems, name, selector) {
				var ret = this.constructor();
				if (jQuery.isArray(elems)) {
					push.apply(ret, elems)
				} else {
					jQuery.merge(ret, elems)
				}
				ret.prevObject = this;
				ret.context = this.context;
				if (name === "find") {
					ret.selector = this.selector + (this.selector ? " " : "") + selector
				} else {
					if (name) {
						ret.selector = this.selector + "." + name + "(" + selector + ")"
					}
				}
				return ret
			},
			each : function (callback, args) {
				return jQuery.each(this, callback, args)
			},
			ready : function (fn) {
				jQuery.bindReady();
				readyList.done(fn);
				return this
			},
			eq : function (i) {
				return i === -1 ? this.slice(i) : this.slice(i, +i + 1)
			},
			first : function () {
				return this.eq(0)
			},
			last : function () {
				return this.eq(-1)
			},
			slice : function () {
				return this.pushStack(slice.apply(this, arguments), "slice", slice.call(arguments).join(","))
			},
			map : function (callback) {
				return this.pushStack(jQuery.map(this, function (elem, i) {
						return callback.call(elem, i, elem)
					}))
			},
			end : function () {
				return this.prevObject || this.constructor(null)
			},
			push : push,
			sort : [].sort,
			splice : [].splice
		};
		jQuery.fn.init.prototype = jQuery.fn;
		jQuery.extend = jQuery.fn.extend = function () {
			var options,
			name,
			src,
			copy,
			copyIsArray,
			clone,
			target = arguments[0] || {},
			i = 1,
			length = arguments.length,
			deep = false;
			if (typeof target === "boolean") {
				deep = target;
				target = arguments[1] || {};
				i = 2
			}
			if (typeof target !== "object" && !jQuery.isFunction(target)) {
				target = {}
				
			}
			if (length === i) {
				target = this;
				--i
			}
			for (; i < length; i++) {
				if ((options = arguments[i]) != null) {
					for (name in options) {
						src = target[name];
						copy = options[name];
						if (target === copy) {
							continue
						}
						if (deep && copy && (jQuery.isPlainObject(copy) || (copyIsArray = jQuery.isArray(copy)))) {
							if (copyIsArray) {
								copyIsArray = false;
								clone = src && jQuery.isArray(src) ? src : []
							} else {
								clone = src && jQuery.isPlainObject(src) ? src : {}
								
							}
							target[name] = jQuery.extend(deep, clone, copy)
						} else {
							if (copy !== undefined) {
								target[name] = copy
							}
						}
					}
				}
			}
			return target
		};
		jQuery.extend({
			noConflict : function (deep) {
				if (window.$ === jQuery) {
					window.$ = _$
				}
				if (deep && window.jQuery === jQuery) {
					window.jQuery = _jQuery
				}
				return jQuery
			},
			isReady : false,
			readyWait : 1,
			holdReady : function (hold) {
				if (hold) {
					jQuery.readyWait++
				} else {
					jQuery.ready(true)
				}
			},
			ready : function (wait) {
				if ((wait === true && !--jQuery.readyWait) || (wait !== true && !jQuery.isReady)) {
					if (!document.body) {
						return setTimeout(jQuery.ready, 1)
					}
					jQuery.isReady = true;
					if (wait !== true && --jQuery.readyWait > 0) {
						return
					}
					readyList.resolveWith(document, [jQuery]);
					if (jQuery.fn.trigger) {
						jQuery(document).trigger("ready").unbind("ready")
					}
				}
			},
			bindReady : function () {
				if (readyList) {
					return
				}
				readyList = jQuery._Deferred();
				if (document.readyState === "complete") {
					return setTimeout(jQuery.ready, 1)
				}
				if (document.addEventListener) {
					document.addEventListener("DOMContentLoaded", DOMContentLoaded, false);
					window.addEventListener("load", jQuery.ready, false)
				} else {
					if (document.attachEvent) {
						document.attachEvent("onreadystatechange", DOMContentLoaded);
						window.attachEvent("onload", jQuery.ready);
						var toplevel = false;
						try {
							toplevel = window.frameElement == null
						} catch (e) {}
						
						if (document.documentElement.doScroll && toplevel) {
							doScrollCheck()
						}
					}
				}
			},
			isFunction : function (obj) {
				return jQuery.type(obj) === "function"
			},
			isArray : Array.isArray || function (obj) {
				return jQuery.type(obj) === "array"
			},
			isWindow : function (obj) {
				return obj && typeof obj === "object" && "setInterval" in obj
			},
			isNaN : function (obj) {
				return obj == null || !rdigit.test(obj) || isNaN(obj)
			},
			type : function (obj) {
				return obj == null ? String(obj) : class2type[toString.call(obj)] || "object"
			},
			isPlainObject : function (obj) {
				if (!obj || jQuery.type(obj) !== "object" || obj.nodeType || jQuery.isWindow(obj)) {
					return false
				}
				if (obj.constructor && !hasOwn.call(obj, "constructor") && !hasOwn.call(obj.constructor.prototype, "isPrototypeOf")) {
					return false
				}
				var key;
				for (key in obj) {}
				
				return key === undefined || hasOwn.call(obj, key)
			},
			isEmptyObject : function (obj) {
				for (var name in obj) {
					return false
				}
				return true
			},
			error : function (msg) {
				throw msg
			},
			parseJSON : function (data) {
				if (typeof data !== "string" || !data) {
					return null
				}
				data = jQuery.trim(data);
				if (window.JSON && window.JSON.parse) {
					return window.JSON.parse(data)
				}
				if (rvalidchars.test(data.replace(rvalidescape, "@").replace(rvalidtokens, "]").replace(rvalidbraces, ""))) {
					return (new Function("return " + data))()
				}
				jQuery.error("Invalid JSON: " + data)
			},
			parseXML : function (data, xml, tmp) {
				if (window.DOMParser) {
					tmp = new DOMParser();
					xml = tmp.parseFromString(data, "text/xml")
				} else {
					xml = new ActiveXObject("Microsoft.XMLDOM");
					xml.async = "false";
					xml.loadXML(data)
				}
				tmp = xml.documentElement;
				if (!tmp || !tmp.nodeName || tmp.nodeName === "parsererror") {
					jQuery.error("Invalid XML: " + data)
				}
				return xml
			},
			noop : function () {},
			globalEval : function (data) {
				if (data && rnotwhite.test(data)) {
					(window.execScript || function (data) {
						window.eval.call(window, data)
					})(data)
				}
			},
			nodeName : function (elem, name) {
				return elem.nodeName && elem.nodeName.toUpperCase() === name.toUpperCase()
			},
			each : function (object, callback, args) {
				var name,
				i = 0,
				length = object.length,
				isObj = length === undefined || jQuery.isFunction(object);
				if (args) {
					if (isObj) {
						for (name in object) {
							if (callback.apply(object[name], args) === false) {
								break
							}
						}
					} else {
						for (; i < length; ) {
							if (callback.apply(object[i++], args) === false) {
								break
							}
						}
					}
				} else {
					if (isObj) {
						for (name in object) {
							if (callback.call(object[name], name, object[name]) === false) {
								break
							}
						}
					} else {
						for (; i < length; ) {
							if (callback.call(object[i], i, object[i++]) === false) {
								break
							}
						}
					}
				}
				return object
			},
			trim : trim ? function (text) {
				return text == null ? "" : trim.call(text)
			}
			 : function (text) {
				return text == null ? "" : text.toString().replace(trimLeft, "").replace(trimRight, "")
			},
			makeArray : function (array, results) {
				var ret = results || [];
				if (array != null) {
					var type = jQuery.type(array);
					if (array.length == null || type === "string" || type === "function" || type === "regexp" || jQuery.isWindow(array)) {
						push.call(ret, array)
					} else {
						jQuery.merge(ret, array)
					}
				}
				return ret
			},
			inArray : function (elem, array) {
				if (indexOf) {
					return indexOf.call(array, elem)
				}
				for (var i = 0, length = array.length; i < length; i++) {
					if (array[i] === elem) {
						return i
					}
				}
				return -1
			},
			merge : function (first, second) {
				var i = first.length,
				j = 0;
				if (typeof second.length === "number") {
					for (var l = second.length; j < l; j++) {
						first[i++] = second[j]
					}
				} else {
					while (second[j] !== undefined) {
						first[i++] = second[j++]
					}
				}
				first.length = i;
				return first
			},
			grep : function (elems, callback, inv) {
				var ret = [],
				retVal;
				inv = !!inv;
				for (var i = 0, length = elems.length; i < length; i++) {
					retVal = !!callback(elems[i], i);
					if (inv !== retVal) {
						ret.push(elems[i])
					}
				}
				return ret
			},
			map : function (elems, callback, arg) {
				var value,
				key,
				ret = [],
				i = 0,
				length = elems.length,
				isArray = elems instanceof jQuery || length !== undefined && typeof length === "number" && ((length > 0 && elems[0] && elems[length - 1]) || length === 0 || jQuery.isArray(elems));
				if (isArray) {
					for (; i < length; i++) {
						value = callback(elems[i], i, arg);
						if (value != null) {
							ret[ret.length] = value
						}
					}
				} else {
					for (key in elems) {
						value = callback(elems[key], key, arg);
						if (value != null) {
							ret[ret.length] = value
						}
					}
				}
				return ret.concat.apply([], ret)
			},
			guid : 1,
			proxy : function (fn, context) {
				if (typeof context === "string") {
					var tmp = fn[context];
					context = fn;
					fn = tmp
				}
				if (!jQuery.isFunction(fn)) {
					return undefined
				}
				var args = slice.call(arguments, 2),
				proxy = function () {
					return fn.apply(context, args.concat(slice.call(arguments)))
				};
				proxy.guid = fn.guid = fn.guid || proxy.guid || jQuery.guid++;
				return proxy
			},
			access : function (elems, key, value, exec, fn, pass) {
				var length = elems.length;
				if (typeof key === "object") {
					for (var k in key) {
						jQuery.access(elems, k, key[k], exec, fn, value)
					}
					return elems
				}
				if (value !== undefined) {
					exec = !pass && exec && jQuery.isFunction(value);
					for (var i = 0; i < length; i++) {
						fn(elems[i], key, exec ? value.call(elems[i], i, fn(elems[i], key)) : value, pass)
					}
					return elems
				}
				return length ? fn(elems[0], key) : undefined
			},
			now : function () {
				return (new Date()).getTime()
			},
			uaMatch : function (ua) {
				ua = ua.toLowerCase();
				var match = rwebkit.exec(ua) || ropera.exec(ua) || rmsie.exec(ua) || ua.indexOf("compatible") < 0 && rmozilla.exec(ua) || [];
				return {
					browser : match[1] || "",
					version : match[2] || "0"
				}
			},
			sub : function () {
				function jQuerySub(selector, context) {
					return new jQuerySub.fn.init(selector, context)
				}
				jQuery.extend(true, jQuerySub, this);
				jQuerySub.superclass = this;
				jQuerySub.fn = jQuerySub.prototype = this();
				jQuerySub.fn.constructor = jQuerySub;
				jQuerySub.sub = this.sub;
				jQuerySub.fn.init = function init(selector, context) {
					if (context && context instanceof jQuery && !(context instanceof jQuerySub)) {
						context = jQuerySub(context)
					}
					return jQuery.fn.init.call(this, selector, context, rootjQuerySub)
				};
				jQuerySub.fn.init.prototype = jQuerySub.fn;
				var rootjQuerySub = jQuerySub(document);
				return jQuerySub
			},
			browser : {}
			
		});
		jQuery.each("Boolean Number String Function Array Date RegExp Object".split(" "), function (i, name) {
			class2type["[object " + name + "]"] = name.toLowerCase()
		});
		browserMatch = jQuery.uaMatch(userAgent);
		if (browserMatch.browser) {
			jQuery.browser[browserMatch.browser] = true;
			jQuery.browser.version = browserMatch.version
		}
		if (jQuery.browser.webkit) {
			jQuery.browser.safari = true
		}
		if (rnotwhite.test("\xA0")) {
			trimLeft = /^[\s\xA0]+/;
			trimRight = /[\s\xA0]+$/
		}
		rootjQuery = jQuery(document);
		if (document.addEventListener) {
			DOMContentLoaded = function () {
				document.removeEventListener("DOMContentLoaded", DOMContentLoaded, false);
				jQuery.ready()
			}
		} else {
			if (document.attachEvent) {
				DOMContentLoaded = function () {
					if (document.readyState === "complete") {
						document.detachEvent("onreadystatechange", DOMContentLoaded);
						jQuery.ready()
					}
				}
			}
		}
		function doScrollCheck() {
			if (jQuery.isReady) {
				return
			}
			try {
				document.documentElement.doScroll("left")
			} catch (e) {
				setTimeout(doScrollCheck, 1);
				return
			}
			jQuery.ready()
		}
		return jQuery
	})();
	var promiseMethods = "done fail isResolved isRejected promise then always pipe".split(" "),
	sliceDeferred = [].slice;
	jQuery.extend({
		_Deferred : function () {
			var callbacks = [],
			fired,
			firing,
			cancelled,
			deferred = {
				done : function () {
					if (!cancelled) {
						var args = arguments,
						i,
						length,
						elem,
						type,
						_fired;
						if (fired) {
							_fired = fired;
							fired = 0
						}
						for (i = 0, length = args.length; i < length; i++) {
							elem = args[i];
							type = jQuery.type(elem);
							if (type === "array") {
								deferred.done.apply(deferred, elem)
							} else {
								if (type === "function") {
									callbacks.push(elem)
								}
							}
						}
						if (_fired) {
							deferred.resolveWith(_fired[0], _fired[1])
						}
					}
					return this
				},
				resolveWith : function (context, args) {
					if (!cancelled && !fired && !firing) {
						args = args || [];
						firing = 1;
						try {
							while (callbacks[0]) {
								callbacks.shift().apply(context, args)
							}
						}
						finally {
							fired = [context, args];
							firing = 0
						}
					}
					return this
				},
				resolve : function () {
					deferred.resolveWith(this, arguments);
					return this
				},
				isResolved : function () {
					return !!(firing || fired)
				},
				cancel : function () {
					cancelled = 1;
					callbacks = [];
					return this
				}
			};
			return deferred
		},
		Deferred : function (func) {
			var deferred = jQuery._Deferred(),
			failDeferred = jQuery._Deferred(),
			promise;
			jQuery.extend(deferred, {
				then : function (doneCallbacks, failCallbacks) {
					deferred.done(doneCallbacks).fail(failCallbacks);
					return this
				},
				always : function () {
					return deferred.done.apply(deferred, arguments).fail.apply(this, arguments)
				},
				fail : failDeferred.done,
				rejectWith : failDeferred.resolveWith,
				reject : failDeferred.resolve,
				isRejected : failDeferred.isResolved,
				pipe : function (fnDone, fnFail) {
					return jQuery.Deferred(function (newDefer) {
						jQuery.each({
							done : [fnDone, "resolve"],
							fail : [fnFail, "reject"]
						}, function (handler, data) {
							var fn = data[0],
							action = data[1],
							returned;
							if (jQuery.isFunction(fn)) {
								deferred[handler](function () {
									returned = fn.apply(this, arguments);
									if (returned && jQuery.isFunction(returned.promise)) {
										returned.promise().then(newDefer.resolve, newDefer.reject)
									} else {
										newDefer[action](returned)
									}
								})
							} else {
								deferred[handler](newDefer[action])
							}
						})
					}).promise()
				},
				promise : function (obj) {
					if (obj == null) {
						if (promise) {
							return promise
						}
						promise = obj = {}
						
					}
					var i = promiseMethods.length;
					while (i--) {
						obj[promiseMethods[i]] = deferred[promiseMethods[i]]
					}
					return obj
				}
			});
			deferred.done(failDeferred.cancel).fail(deferred.cancel);
			delete deferred.cancel;
			if (func) {
				func.call(deferred, deferred)
			}
			return deferred
		},
		when : function (firstParam) {
			var args = arguments,
			i = 0,
			length = args.length,
			count = length,
			deferred = length <= 1 && firstParam && jQuery.isFunction(firstParam.promise) ? firstParam : jQuery.Deferred();
			function resolveFunc(i) {
				return function (value) {
					args[i] = arguments.length > 1 ? sliceDeferred.call(arguments, 0) : value;
					if (!(--count)) {
						deferred.resolveWith(deferred, sliceDeferred.call(args, 0))
					}
				}
			}
			if (length > 1) {
				for (; i < length; i++) {
					if (args[i] && jQuery.isFunction(args[i].promise)) {
						args[i].promise().then(resolveFunc(i), deferred.reject)
					} else {
						--count
					}
				}
				if (!count) {
					deferred.resolveWith(deferred, args)
				}
			} else {
				if (deferred !== firstParam) {
					deferred.resolveWith(deferred, length ? [firstParam] : [])
				}
			}
			return deferred.promise()
		}
	});
	jQuery.support = (function () {
		var div = document.createElement("div"),
		documentElement = document.documentElement,
		all,
		a,
		select,
		opt,
		input,
		marginDiv,
		support,
		fragment,
		body,
		bodyStyle,
		tds,
		events,
		eventName,
		i,
		isSupported;
		div.setAttribute("className", "t");
		div.innerHTML = "   <link/><table></table><a href='/a' style='top:1px;float:left;opacity:.55;'>a</a><input type='checkbox'/>";
		all = div.getElementsByTagName("*");
		a = div.getElementsByTagName("a")[0];
		if (!all || !all.length || !a) {
			return {}
			
		}
		select = document.createElement("select");
		opt = select.appendChild(document.createElement("option"));
		input = div.getElementsByTagName("input")[0];
		support = {
			leadingWhitespace : (div.firstChild.nodeType === 3),
			tbody : !div.getElementsByTagName("tbody").length,
			htmlSerialize : !!div.getElementsByTagName("link").length,
			style : /top/.test(a.getAttribute("style")),
			hrefNormalized : (a.getAttribute("href") === "/a"),
			opacity : /^0.55$/.test(a.style.opacity),
			cssFloat : !!a.style.cssFloat,
			checkOn : (input.value === "on"),
			optSelected : opt.selected,
			getSetAttribute : div.className !== "t",
			submitBubbles : true,
			changeBubbles : true,
			focusinBubbles : false,
			deleteExpando : true,
			noCloneEvent : true,
			inlineBlockNeedsLayout : false,
			shrinkWrapBlocks : false,
			reliableMarginRight : true
		};
		input.checked = true;
		support.noCloneChecked = input.cloneNode(true).checked;
		select.disabled = true;
		support.optDisabled = !opt.disabled;
		try {
			delete div.test
		} catch (e) {
			support.deleteExpando = false
		}
		if (!div.addEventListener && div.attachEvent && div.fireEvent) {
			div.attachEvent("onclick", function click() {
				support.noCloneEvent = false;
				div.detachEvent("onclick", click)
			});
			div.cloneNode(true).fireEvent("onclick")
		}
		input = document.createElement("input");
		input.value = "t";
		input.setAttribute("type", "radio");
		support.radioValue = input.value === "t";
		input.setAttribute("checked", "checked");
		div.appendChild(input);
		fragment = document.createDocumentFragment();
		fragment.appendChild(div.firstChild);
		support.checkClone = fragment.cloneNode(true).cloneNode(true).lastChild.checked;
		div.innerHTML = "";
		div.style.width = div.style.paddingLeft = "1px";
		body = document.createElement("body");
		bodyStyle = {
			visibility : "hidden",
			width : 0,
			height : 0,
			border : 0,
			margin : 0,
			background : "none"
		};
		for (i in bodyStyle) {
			body.style[i] = bodyStyle[i]
		}
		body.appendChild(div);
		documentElement.insertBefore(body, documentElement.firstChild);
		support.appendChecked = input.checked;
		support.boxModel = div.offsetWidth === 2;
		if ("zoom" in div.style) {
			div.style.display = "inline";
			div.style.zoom = 1;
			support.inlineBlockNeedsLayout = (div.offsetWidth === 2);
			div.style.display = "";
			div.innerHTML = "<div style='width:4px;'></div>";
			support.shrinkWrapBlocks = (div.offsetWidth !== 2)
		}
		div.innerHTML = "<table><tr><td style='padding:0;border:0;display:none'></td><td>t</td></tr></table>";
		tds = div.getElementsByTagName("td");
		isSupported = (tds[0].offsetHeight === 0);
		tds[0].style.display = "";
		tds[1].style.display = "none";
		support.reliableHiddenOffsets = isSupported && (tds[0].offsetHeight === 0);
		div.innerHTML = "";
		if (document.defaultView && document.defaultView.getComputedStyle) {
			marginDiv = document.createElement("div");
			marginDiv.style.width = "0";
			marginDiv.style.marginRight = "0";
			div.appendChild(marginDiv);
			support.reliableMarginRight = (parseInt((document.defaultView.getComputedStyle(marginDiv, null) || {
						marginRight : 0
					}).marginRight, 10) || 0) === 0
		}
		body.innerHTML = "";
		documentElement.removeChild(body);
		if (div.attachEvent) {
			for (i in {
				submit : 1,
				change : 1,
				focusin : 1
			}) {
				eventName = "on" + i;
				isSupported = (eventName in div);
				if (!isSupported) {
					div.setAttribute(eventName, "return;");
					isSupported = (typeof div[eventName] === "function")
				}
				support[i + "Bubbles"] = isSupported
			}
		}
		return support
	})();
	jQuery.boxModel = jQuery.support.boxModel;
	var rbrace = /^(?:\{.*\}|\[.*\])$/,
	rmultiDash = /([a-z])([A-Z])/g;
	jQuery.extend({
		cache : {},
		uuid : 0,
		expando : "jQuery" + (jQuery.fn.jquery + Math.random()).replace(/\D/g, ""),
		noData : {
			embed : true,
			object : "clsid:D27CDB6E-AE6D-11cf-96B8-444553540000",
			applet : true
		},
		hasData : function (elem) {
			elem = elem.nodeType ? jQuery.cache[elem[jQuery.expando]] : elem[jQuery.expando];
			return !!elem && !isEmptyDataObject(elem)
		},
		data : function (elem, name, data, pvt) {
			if (!jQuery.acceptData(elem)) {
				return
			}
			var internalKey = jQuery.expando,
			getByName = typeof name === "string",
			thisCache,
			isNode = elem.nodeType,
			cache = isNode ? jQuery.cache : elem,
			id = isNode ? elem[jQuery.expando] : elem[jQuery.expando] && jQuery.expando;
			if ((!id || (pvt && id && !cache[id][internalKey])) && getByName && data === undefined) {
				return
			}
			if (!id) {
				if (isNode) {
					elem[jQuery.expando] = id = ++jQuery.uuid
				} else {
					id = jQuery.expando
				}
			}
			if (!cache[id]) {
				cache[id] = {};
				if (!isNode) {
					cache[id].toJSON = jQuery.noop
				}
			}
			if (typeof name === "object" || typeof name === "function") {
				if (pvt) {
					cache[id][internalKey] = jQuery.extend(cache[id][internalKey], name)
				} else {
					cache[id] = jQuery.extend(cache[id], name)
				}
			}
			thisCache = cache[id];
			if (pvt) {
				if (!thisCache[internalKey]) {
					thisCache[internalKey] = {}
					
				}
				thisCache = thisCache[internalKey]
			}
			if (data !== undefined) {
				thisCache[jQuery.camelCase(name)] = data
			}
			if (name === "events" && !thisCache[name]) {
				return thisCache[internalKey] && thisCache[internalKey].events
			}
			return getByName ? thisCache[jQuery.camelCase(name)] : thisCache
		},
		removeData : function (elem, name, pvt) {
			if (!jQuery.acceptData(elem)) {
				return
			}
			var internalKey = jQuery.expando,
			isNode = elem.nodeType,
			cache = isNode ? jQuery.cache : elem,
			id = isNode ? elem[jQuery.expando] : jQuery.expando;
			if (!cache[id]) {
				return
			}
			if (name) {
				var thisCache = pvt ? cache[id][internalKey] : cache[id];
				if (thisCache) {
					delete thisCache[name];
					if (!isEmptyDataObject(thisCache)) {
						return
					}
				}
			}
			if (pvt) {
				delete cache[id][internalKey];
				if (!isEmptyDataObject(cache[id])) {
					return
				}
			}
			var internalCache = cache[id][internalKey];
			if (jQuery.support.deleteExpando || cache != window) {
				delete cache[id]
			} else {
				cache[id] = null
			}
			if (internalCache) {
				cache[id] = {};
				if (!isNode) {
					cache[id].toJSON = jQuery.noop
				}
				cache[id][internalKey] = internalCache
			} else {
				if (isNode) {
					if (jQuery.support.deleteExpando) {
						delete elem[jQuery.expando]
					} else {
						if (elem.removeAttribute) {
							elem.removeAttribute(jQuery.expando)
						} else {
							elem[jQuery.expando] = null
						}
					}
				}
			}
		},
		_data : function (elem, name, data) {
			return jQuery.data(elem, name, data, true)
		},
		acceptData : function (elem) {
			if (elem.nodeName) {
				var match = jQuery.noData[elem.nodeName.toLowerCase()];
				if (match) {
					return !(match === true || elem.getAttribute("classid") !== match)
				}
			}
			return true
		}
	});
	jQuery.fn.extend({
		data : function (key, value) {
			var data = null;
			if (typeof key === "undefined") {
				if (this.length) {
					data = jQuery.data(this[0]);
					if (this[0].nodeType === 1) {
						var attr = this[0].attributes,
						name;
						for (var i = 0, l = attr.length; i < l; i++) {
							name = attr[i].name;
							if (name.indexOf("data-") === 0) {
								name = jQuery.camelCase(name.substring(5));
								dataAttr(this[0], name, data[name])
							}
						}
					}
				}
				return data
			} else {
				if (typeof key === "object") {
					return this.each(function () {
						jQuery.data(this, key)
					})
				}
			}
			var parts = key.split(".");
			parts[1] = parts[1] ? "." + parts[1] : "";
			if (value === undefined) {
				data = this.triggerHandler("getData" + parts[1] + "!", [parts[0]]);
				if (data === undefined && this.length) {
					data = jQuery.data(this[0], key);
					data = dataAttr(this[0], key, data)
				}
				return data === undefined && parts[1] ? this.data(parts[0]) : data
			} else {
				return this.each(function () {
					var $this = jQuery(this),
					args = [parts[0], value];
					$this.triggerHandler("setData" + parts[1] + "!", args);
					jQuery.data(this, key, value);
					$this.triggerHandler("changeData" + parts[1] + "!", args)
				})
			}
		},
		removeData : function (key) {
			return this.each(function () {
				jQuery.removeData(this, key)
			})
		}
	});
	function dataAttr(elem, key, data) {
		if (data === undefined && elem.nodeType === 1) {
			var name = "data-" + key.replace(rmultiDash, "$1-$2").toLowerCase();
			data = elem.getAttribute(name);
			if (typeof data === "string") {
				try {
					data = data === "true" ? true : data === "false" ? false : data === "null" ? null : !jQuery.isNaN(data) ? parseFloat(data) : rbrace.test(data) ? jQuery.parseJSON(data) : data
				} catch (e) {}
				
				jQuery.data(elem, key, data)
			} else {
				data = undefined
			}
		}
		return data
	}
	function isEmptyDataObject(obj) {
		for (var name in obj) {
			if (name !== "toJSON") {
				return false
			}
		}
		return true
	}
	function handleQueueMarkDefer(elem, type, src) {
		var deferDataKey = type + "defer",
		queueDataKey = type + "queue",
		markDataKey = type + "mark",
		defer = jQuery.data(elem, deferDataKey, undefined, true);
		if (defer && (src === "queue" || !jQuery.data(elem, queueDataKey, undefined, true)) && (src === "mark" || !jQuery.data(elem, markDataKey, undefined, true))) {
			setTimeout(function () {
				if (!jQuery.data(elem, queueDataKey, undefined, true) && !jQuery.data(elem, markDataKey, undefined, true)) {
					jQuery.removeData(elem, deferDataKey, true);
					defer.resolve()
				}
			}, 0)
		}
	}
	jQuery.extend({
		_mark : function (elem, type) {
			if (elem) {
				type = (type || "fx") + "mark";
				jQuery.data(elem, type, (jQuery.data(elem, type, undefined, true) || 0) + 1, true)
			}
		},
		_unmark : function (force, elem, type) {
			if (force !== true) {
				type = elem;
				elem = force;
				force = false
			}
			if (elem) {
				type = type || "fx";
				var key = type + "mark",
				count = force ? 0 : ((jQuery.data(elem, key, undefined, true) || 1) - 1);
				if (count) {
					jQuery.data(elem, key, count, true)
				} else {
					jQuery.removeData(elem, key, true);
					handleQueueMarkDefer(elem, type, "mark")
				}
			}
		},
		queue : function (elem, type, data) {
			if (elem) {
				type = (type || "fx") + "queue";
				var q = jQuery.data(elem, type, undefined, true);
				if (data) {
					if (!q || jQuery.isArray(data)) {
						q = jQuery.data(elem, type, jQuery.makeArray(data), true)
					} else {
						q.push(data)
					}
				}
				return q || []
			}
		},
		dequeue : function (elem, type) {
			type = type || "fx";
			var queue = jQuery.queue(elem, type),
			fn = queue.shift(),
			defer;
			if (fn === "inprogress") {
				fn = queue.shift()
			}
			if (fn) {
				if (type === "fx") {
					queue.unshift("inprogress")
				}
				fn.call(elem, function () {
					jQuery.dequeue(elem, type)
				})
			}
			if (!queue.length) {
				jQuery.removeData(elem, type + "queue", true);
				handleQueueMarkDefer(elem, type, "queue")
			}
		}
	});
	jQuery.fn.extend({
		queue : function (type, data) {
			if (typeof type !== "string") {
				data = type;
				type = "fx"
			}
			if (data === undefined) {
				return jQuery.queue(this[0], type)
			}
			return this.each(function () {
				var queue = jQuery.queue(this, type, data);
				if (type === "fx" && queue[0] !== "inprogress") {
					jQuery.dequeue(this, type)
				}
			})
		},
		dequeue : function (type) {
			return this.each(function () {
				jQuery.dequeue(this, type)
			})
		},
		delay : function (time, type) {
			time = jQuery.fx ? jQuery.fx.speeds[time] || time : time;
			type = type || "fx";
			return this.queue(type, function () {
				var elem = this;
				setTimeout(function () {
					jQuery.dequeue(elem, type)
				}, time)
			})
		},
		clearQueue : function (type) {
			return this.queue(type || "fx", [])
		},
		promise : function (type, object) {
			if (typeof type !== "string") {
				object = type;
				type = undefined
			}
			type = type || "fx";
			var defer = jQuery.Deferred(),
			elements = this,
			i = elements.length,
			count = 1,
			deferDataKey = type + "defer",
			queueDataKey = type + "queue",
			markDataKey = type + "mark",
			tmp;
			function resolve() {
				if (!(--count)) {
					defer.resolveWith(elements, [elements])
				}
			}
			while (i--) {
				if ((tmp = jQuery.data(elements[i], deferDataKey, undefined, true) || (jQuery.data(elements[i], queueDataKey, undefined, true) || jQuery.data(elements[i], markDataKey, undefined, true)) && jQuery.data(elements[i], deferDataKey, jQuery._Deferred(), true))) {
					count++;
					tmp.done(resolve)
				}
			}
			resolve();
			return defer.promise()
		}
	});
	var rclass = /[\n\t\r]/g,
	rspace = /\s+/,
	rreturn = /\r/g,
	rtype = /^(?:button|input)$/i,
	rfocusable = /^(?:button|input|object|select|textarea)$/i,
	rclickable = /^a(?:rea)?$/i,
	rboolean = /^(?:autofocus|autoplay|async|checked|controls|defer|disabled|hidden|loop|multiple|open|readonly|required|scoped|selected)$/i,
	rinvalidChar = /\:/,
	formHook,
	boolHook;
	jQuery.fn.extend({
		attr : function (name, value) {
			return jQuery.access(this, name, value, true, jQuery.attr)
		},
		removeAttr : function (name) {
			return this.each(function () {
				jQuery.removeAttr(this, name)
			})
		},
		prop : function (name, value) {
			return jQuery.access(this, name, value, true, jQuery.prop)
		},
		removeProp : function (name) {
			name = jQuery.propFix[name] || name;
			return this.each(function () {
				try {
					this[name] = undefined;
					delete this[name]
				} catch (e) {}
				
			})
		},
		addClass : function (value) {
			if (jQuery.isFunction(value)) {
				return this.each(function (i) {
					var self = jQuery(this);
					self.addClass(value.call(this, i, self.attr("class") || ""))
				})
			}
			if (value && typeof value === "string") {
				var classNames = (value || "").split(rspace);
				for (var i = 0, l = this.length; i < l; i++) {
					var elem = this[i];
					if (elem.nodeType === 1) {
						if (!elem.className) {
							elem.className = value
						} else {
							var className = " " + elem.className + " ",
							setClass = elem.className;
							for (var c = 0, cl = classNames.length; c < cl; c++) {
								if (className.indexOf(" " + classNames[c] + " ") < 0) {
									setClass += " " + classNames[c]
								}
							}
							elem.className = jQuery.trim(setClass)
						}
					}
				}
			}
			return this
		},
		removeClass : function (value) {
			if (jQuery.isFunction(value)) {
				return this.each(function (i) {
					var self = jQuery(this);
					self.removeClass(value.call(this, i, self.attr("class")))
				})
			}
			if ((value && typeof value === "string") || value === undefined) {
				var classNames = (value || "").split(rspace);
				for (var i = 0, l = this.length; i < l; i++) {
					var elem = this[i];
					if (elem.nodeType === 1 && elem.className) {
						if (value) {
							var className = (" " + elem.className + " ").replace(rclass, " ");
							for (var c = 0, cl = classNames.length; c < cl; c++) {
								className = className.replace(" " + classNames[c] + " ", " ")
							}
							elem.className = jQuery.trim(className)
						} else {
							elem.className = ""
						}
					}
				}
			}
			return this
		},
		toggleClass : function (value, stateVal) {
			var type = typeof value,
			isBool = typeof stateVal === "boolean";
			if (jQuery.isFunction(value)) {
				return this.each(function (i) {
					var self = jQuery(this);
					self.toggleClass(value.call(this, i, self.attr("class"), stateVal), stateVal)
				})
			}
			return this.each(function () {
				if (type === "string") {
					var className,
					i = 0,
					self = jQuery(this),
					state = stateVal,
					classNames = value.split(rspace);
					while ((className = classNames[i++])) {
						state = isBool ? state : !self.hasClass(className);
						self[state ? "addClass" : "removeClass"](className)
					}
				} else {
					if (type === "undefined" || type === "boolean") {
						if (this.className) {
							jQuery._data(this, "__className__", this.className)
						}
						this.className = this.className || value === false ? "" : jQuery._data(this, "__className__") || ""
					}
				}
			})
		},
		hasClass : function (selector) {
			var className = " " + selector + " ";
			for (var i = 0, l = this.length; i < l; i++) {
				if ((" " + this[i].className + " ").replace(rclass, " ").indexOf(className) > -1) {
					return true
				}
			}
			return false
		},
		val : function (value) {
			var hooks,
			ret,
			elem = this[0];
			if (!arguments.length) {
				if (elem) {
					hooks = jQuery.valHooks[elem.nodeName.toLowerCase()] || jQuery.valHooks[elem.type];
					if (hooks && "get" in hooks && (ret = hooks.get(elem, "value")) !== undefined) {
						return ret
					}
					return (elem.value || "").replace(rreturn, "")
				}
				return undefined
			}
			var isFunction = jQuery.isFunction(value);
			return this.each(function (i) {
				var self = jQuery(this),
				val;
				if (this.nodeType !== 1) {
					return
				}
				if (isFunction) {
					val = value.call(this, i, self.val())
				} else {
					val = value
				}
				if (val == null) {
					val = ""
				} else {
					if (typeof val === "number") {
						val += ""
					} else {
						if (jQuery.isArray(val)) {
							val = jQuery.map(val, function (value) {
									return value == null ? "" : value + ""
								})
						}
					}
				}
				hooks = jQuery.valHooks[this.nodeName.toLowerCase()] || jQuery.valHooks[this.type];
				if (!hooks || !("set" in hooks) || hooks.set(this, val, "value") === undefined) {
					this.value = val
				}
			})
		}
	});
	jQuery.extend({
		valHooks : {
			option : {
				get : function (elem) {
					var val = elem.attributes.value;
					return !val || val.specified ? elem.value : elem.text
				}
			},
			select : {
				get : function (elem) {
					var value,
					index = elem.selectedIndex,
					values = [],
					options = elem.options,
					one = elem.type === "select-one";
					if (index < 0) {
						return null
					}
					for (var i = one ? index : 0, max = one ? index + 1 : options.length; i < max; i++) {
						var option = options[i];
						if (option.selected && (jQuery.support.optDisabled ? !option.disabled : option.getAttribute("disabled") === null) && (!option.parentNode.disabled || !jQuery.nodeName(option.parentNode, "optgroup"))) {
							value = jQuery(option).val();
							if (one) {
								return value
							}
							values.push(value)
						}
					}
					if (one && !values.length && options.length) {
						return jQuery(options[index]).val()
					}
					return values
				},
				set : function (elem, value) {
					var values = jQuery.makeArray(value);
					jQuery(elem).find("option").each(function () {
						this.selected = jQuery.inArray(jQuery(this).val(), values) >= 0
					});
					if (!values.length) {
						elem.selectedIndex = -1
					}
					return values
				}
			}
		},
		attrFn : {
			val : true,
			css : true,
			html : true,
			text : true,
			data : true,
			width : true,
			height : true,
			offset : true
		},
		attrFix : {
			tabindex : "tabIndex"
		},
		attr : function (elem, name, value, pass) {
			var nType = elem.nodeType;
			if (!elem || nType === 3 || nType === 8 || nType === 2) {
				return undefined
			}
			if (pass && name in jQuery.attrFn) {
				return jQuery(elem)[name](value)
			}
			if (!("getAttribute" in elem)) {
				return jQuery.prop(elem, name, value)
			}
			var ret,
			hooks,
			notxml = nType !== 1 || !jQuery.isXMLDoc(elem);
			name = notxml && jQuery.attrFix[name] || name;
			hooks = jQuery.attrHooks[name];
			if (!hooks) {
				if (rboolean.test(name) && (typeof value === "boolean" || value === undefined || value.toLowerCase() === name.toLowerCase())) {
					hooks = boolHook
				} else {
					if (formHook && (jQuery.nodeName(elem, "form") || rinvalidChar.test(name))) {
						hooks = formHook
					}
				}
			}
			if (value !== undefined) {
				if (value === null) {
					jQuery.removeAttr(elem, name);
					return undefined
				} else {
					if (hooks && "set" in hooks && notxml && (ret = hooks.set(elem, value, name)) !== undefined) {
						return ret
					} else {
						elem.setAttribute(name, "" + value);
						return value
					}
				}
			} else {
				if (hooks && "get" in hooks && notxml) {
					return hooks.get(elem, name)
				} else {
					ret = elem.getAttribute(name);
					return ret === null ? undefined : ret
				}
			}
		},
		removeAttr : function (elem, name) {
			var propName;
			if (elem.nodeType === 1) {
				name = jQuery.attrFix[name] || name;
				if (jQuery.support.getSetAttribute) {
					elem.removeAttribute(name)
				} else {
					jQuery.attr(elem, name, "");
					elem.removeAttributeNode(elem.getAttributeNode(name))
				}
				if (rboolean.test(name) && (propName = jQuery.propFix[name] || name)in elem) {
					elem[propName] = false
				}
			}
		},
		attrHooks : {
			type : {
				set : function (elem, value) {
					if (rtype.test(elem.nodeName) && elem.parentNode) {
						jQuery.error("type property can't be changed")
					} else {
						if (!jQuery.support.radioValue && value === "radio" && jQuery.nodeName(elem, "input")) {
							var val = elem.value;
							elem.setAttribute("type", value);
							if (val) {
								elem.value = val
							}
							return value
						}
					}
				}
			},
			tabIndex : {
				get : function (elem) {
					var attributeNode = elem.getAttributeNode("tabIndex");
					return attributeNode && attributeNode.specified ? parseInt(attributeNode.value, 10) : rfocusable.test(elem.nodeName) || rclickable.test(elem.nodeName) && elem.href ? 0 : undefined
				}
			}
		},
		propFix : {
			tabindex : "tabIndex",
			readonly : "readOnly",
			"for" : "htmlFor",
			"class" : "className",
			maxlength : "maxLength",
			cellspacing : "cellSpacing",
			cellpadding : "cellPadding",
			rowspan : "rowSpan",
			colspan : "colSpan",
			usemap : "useMap",
			frameborder : "frameBorder",
			contenteditable : "contentEditable"
		},
		prop : function (elem, name, value) {
			var nType = elem.nodeType;
			if (!elem || nType === 3 || nType === 8 || nType === 2) {
				return undefined
			}
			var ret,
			hooks,
			notxml = nType !== 1 || !jQuery.isXMLDoc(elem);
			name = notxml && jQuery.propFix[name] || name;
			hooks = jQuery.propHooks[name];
			if (value !== undefined) {
				if (hooks && "set" in hooks && (ret = hooks.set(elem, value, name)) !== undefined) {
					return ret
				} else {
					return (elem[name] = value)
				}
			} else {
				if (hooks && "get" in hooks && (ret = hooks.get(elem, name)) !== undefined) {
					return ret
				} else {
					return elem[name]
				}
			}
		},
		propHooks : {}
		
	});
	boolHook = {
		get : function (elem, name) {
			return elem[jQuery.propFix[name] || name] ? name.toLowerCase() : undefined
		},
		set : function (elem, value, name) {
			var propName;
			if (value === false) {
				jQuery.removeAttr(elem, name)
			} else {
				propName = jQuery.propFix[name] || name;
				if (propName in elem) {
					elem[propName] = value
				}
				elem.setAttribute(name, name.toLowerCase())
			}
			return name
		}
	};
	jQuery.attrHooks.value = {
		get : function (elem, name) {
			if (formHook && jQuery.nodeName(elem, "button")) {
				return formHook.get(elem, name)
			}
			return elem.value
		},
		set : function (elem, value, name) {
			if (formHook && jQuery.nodeName(elem, "button")) {
				return formHook.set(elem, value, name)
			}
			elem.value = value
		}
	};
	if (!jQuery.support.getSetAttribute) {
		jQuery.attrFix = jQuery.propFix;
		formHook = jQuery.attrHooks.name = jQuery.valHooks.button = {
			get : function (elem, name) {
				var ret;
				ret = elem.getAttributeNode(name);
				return ret && ret.nodeValue !== "" ? ret.nodeValue : undefined
			},
			set : function (elem, value, name) {
				var ret = elem.getAttributeNode(name);
				if (ret) {
					ret.nodeValue = value;
					return value
				}
			}
		};
		jQuery.each(["width", "height"], function (i, name) {
			jQuery.attrHooks[name] = jQuery.extend(jQuery.attrHooks[name], {
					set : function (elem, value) {
						if (value === "") {
							elem.setAttribute(name, "auto");
							return value
						}
					}
				})
		})
	}
	if (!jQuery.support.hrefNormalized) {
		jQuery.each(["href", "src", "width", "height"], function (i, name) {
			jQuery.attrHooks[name] = jQuery.extend(jQuery.attrHooks[name], {
					get : function (elem) {
						var ret = elem.getAttribute(name, 2);
						return ret === null ? undefined : ret
					}
				})
		})
	}
	if (!jQuery.support.style) {
		jQuery.attrHooks.style = {
			get : function (elem) {
				return elem.style.cssText.toLowerCase() || undefined
			},
			set : function (elem, value) {
				return (elem.style.cssText = "" + value)
			}
		}
	}
	if (!jQuery.support.optSelected) {
		jQuery.propHooks.selected = jQuery.extend(jQuery.propHooks.selected, {
				get : function (elem) {
					var parent = elem.parentNode;
					if (parent) {
						parent.selectedIndex;
						if (parent.parentNode) {
							parent.parentNode.selectedIndex
						}
					}
				}
			})
	}
	if (!jQuery.support.checkOn) {
		jQuery.each(["radio", "checkbox"], function () {
			jQuery.valHooks[this] = {
				get : function (elem) {
					return elem.getAttribute("value") === null ? "on" : elem.value
				}
			}
		})
	}
	jQuery.each(["radio", "checkbox"], function () {
		jQuery.valHooks[this] = jQuery.extend(jQuery.valHooks[this], {
				set : function (elem, value) {
					if (jQuery.isArray(value)) {
						return (elem.checked = jQuery.inArray(jQuery(elem).val(), value) >= 0)
					}
				}
			})
	});
	var hasOwn = Object.prototype.hasOwnProperty,
	rnamespaces = /\.(.*)$/,
	rformElems = /^(?:textarea|input|select)$/i,
	rperiod = /\./g,
	rspaces = / /g,
	rescape = /[^\w\s.|`]/g,
	fcleanup = function (nm) {
		return nm.replace(rescape, "\\$&")
	};
	jQuery.event = {
		add : function (elem, types, handler, data) {
			if (elem.nodeType === 3 || elem.nodeType === 8) {
				return
			}
			if (handler === false) {
				handler = returnFalse
			} else {
				if (!handler) {
					return
				}
			}
			var handleObjIn,
			handleObj;
			if (handler.handler) {
				handleObjIn = handler;
				handler = handleObjIn.handler
			}
			if (!handler.guid) {
				handler.guid = jQuery.guid++
			}
			var elemData = jQuery._data(elem);
			if (!elemData) {
				return
			}
			var events = elemData.events,
			eventHandle = elemData.handle;
			if (!events) {
				elemData.events = events = {}
				
			}
			if (!eventHandle) {
				elemData.handle = eventHandle = function (e) {
					return typeof jQuery !== "undefined" && (!e || jQuery.event.triggered !== e.type) ? jQuery.event.handle.apply(eventHandle.elem, arguments) : undefined
				}
			}
			eventHandle.elem = elem;
			types = types.split(" ");
			var type,
			i = 0,
			namespaces;
			while ((type = types[i++])) {
				handleObj = handleObjIn ? jQuery.extend({}, handleObjIn) : {
					handler : handler,
					data : data
				};
				if (type.indexOf(".") > -1) {
					namespaces = type.split(".");
					type = namespaces.shift();
					handleObj.namespace = namespaces.slice(0).sort().join(".")
				} else {
					namespaces = [];
					handleObj.namespace = ""
				}
				handleObj.type = type;
				if (!handleObj.guid) {
					handleObj.guid = handler.guid
				}
				var handlers = events[type],
				special = jQuery.event.special[type] || {};
				if (!handlers) {
					handlers = events[type] = [];
					if (!special.setup || special.setup.call(elem, data, namespaces, eventHandle) === false) {
						if (elem.addEventListener) {
							elem.addEventListener(type, eventHandle, false)
						} else {
							if (elem.attachEvent) {
								elem.attachEvent("on" + type, eventHandle)
							}
						}
					}
				}
				if (special.add) {
					special.add.call(elem, handleObj);
					if (!handleObj.handler.guid) {
						handleObj.handler.guid = handler.guid
					}
				}
				handlers.push(handleObj);
				jQuery.event.global[type] = true
			}
			elem = null
		},
		global : {},
		remove : function (elem, types, handler, pos) {
			if (elem.nodeType === 3 || elem.nodeType === 8) {
				return
			}
			if (handler === false) {
				handler = returnFalse
			}
			var ret,
			type,
			fn,
			j,
			i = 0,
			all,
			namespaces,
			namespace,
			special,
			eventType,
			handleObj,
			origType,
			elemData = jQuery.hasData(elem) && jQuery._data(elem),
			events = elemData && elemData.events;
			if (!elemData || !events) {
				return
			}
			if (types && types.type) {
				handler = types.handler;
				types = types.type
			}
			if (!types || typeof types === "string" && types.charAt(0) === ".") {
				types = types || "";
				for (type in events) {
					jQuery.event.remove(elem, type + types)
				}
				return
			}
			types = types.split(" ");
			while ((type = types[i++])) {
				origType = type;
				handleObj = null;
				all = type.indexOf(".") < 0;
				namespaces = [];
				if (!all) {
					namespaces = type.split(".");
					type = namespaces.shift();
					namespace = new RegExp("(^|\\.)" + jQuery.map(namespaces.slice(0).sort(), fcleanup).join("\\.(?:.*\\.)?") + "(\\.|$)")
				}
				eventType = events[type];
				if (!eventType) {
					continue
				}
				if (!handler) {
					for (j = 0; j < eventType.length; j++) {
						handleObj = eventType[j];
						if (all || namespace.test(handleObj.namespace)) {
							jQuery.event.remove(elem, origType, handleObj.handler, j);
							eventType.splice(j--, 1)
						}
					}
					continue
				}
				special = jQuery.event.special[type] || {};
				for (j = pos || 0; j < eventType.length; j++) {
					handleObj = eventType[j];
					if (handler.guid === handleObj.guid) {
						if (all || namespace.test(handleObj.namespace)) {
							if (pos == null) {
								eventType.splice(j--, 1)
							}
							if (special.remove) {
								special.remove.call(elem, handleObj)
							}
						}
						if (pos != null) {
							break
						}
					}
				}
				if (eventType.length === 0 || pos != null && eventType.length === 1) {
					if (!special.teardown || special.teardown.call(elem, namespaces) === false) {
						jQuery.removeEvent(elem, type, elemData.handle)
					}
					ret = null;
					delete events[type]
				}
			}
			if (jQuery.isEmptyObject(events)) {
				var handle = elemData.handle;
				if (handle) {
					handle.elem = null
				}
				delete elemData.events;
				delete elemData.handle;
				if (jQuery.isEmptyObject(elemData)) {
					jQuery.removeData(elem, undefined, true)
				}
			}
		},
		customEvent : {
			getData : true,
			setData : true,
			changeData : true
		},
		trigger : function (event, data, elem, onlyHandlers) {
			var type = event.type || event,
			namespaces = [],
			exclusive;
			if (type.indexOf("!") >= 0) {
				type = type.slice(0, -1);
				exclusive = true
			}
			if (type.indexOf(".") >= 0) {
				namespaces = type.split(".");
				type = namespaces.shift();
				namespaces.sort()
			}
			if ((!elem || jQuery.event.customEvent[type]) && !jQuery.event.global[type]) {
				return
			}
			event = typeof event === "object" ? event[jQuery.expando] ? event : new jQuery.Event(type, event) : new jQuery.Event(type);
			event.type = type;
			event.exclusive = exclusive;
			event.namespace = namespaces.join(".");
			event.namespace_re = new RegExp("(^|\\.)" + namespaces.join("\\.(?:.*\\.)?") + "(\\.|$)");
			if (onlyHandlers || !elem) {
				event.preventDefault();
				event.stopPropagation()
			}
			if (!elem) {
				jQuery.each(jQuery.cache, function () {
					var internalKey = jQuery.expando,
					internalCache = this[internalKey];
					if (internalCache && internalCache.events && internalCache.events[type]) {
						jQuery.event.trigger(event, data, internalCache.handle.elem)
					}
				});
				return
			}
			if (elem.nodeType === 3 || elem.nodeType === 8) {
				return
			}
			event.result = undefined;
			event.target = elem;
			data = data ? jQuery.makeArray(data) : [];
			data.unshift(event);
			var cur = elem,
			ontype = type.indexOf(":") < 0 ? "on" + type : "";
			do {
				var handle = jQuery._data(cur, "handle");
				event.currentTarget = cur;
				if (handle) {
					handle.apply(cur, data)
				}
				if (ontype && jQuery.acceptData(cur) && cur[ontype] && cur[ontype].apply(cur, data) === false) {
					event.result = false;
					event.preventDefault()
				}
				cur = cur.parentNode || cur.ownerDocument || cur === event.target.ownerDocument && window
			} while (cur && !event.isPropagationStopped());
			if (!event.isDefaultPrevented()) {
				var old,
				special = jQuery.event.special[type] || {};
				if ((!special._default || special._default.call(elem.ownerDocument, event) === false) && !(type === "click" && jQuery.nodeName(elem, "a")) && jQuery.acceptData(elem)) {
					try {
						if (ontype && elem[type]) {
							old = elem[ontype];
							if (old) {
								elem[ontype] = null
							}
							jQuery.event.triggered = type;
							elem[type]()
						}
					} catch (ieError) {}
					
					if (old) {
						elem[ontype] = old
					}
					jQuery.event.triggered = undefined
				}
			}
			return event.result
		},
		handle : function (event) {
			event = jQuery.event.fix(event || window.event);
			var handlers = ((jQuery._data(this, "events") || {})[event.type] || []).slice(0),
			run_all = !event.exclusive && !event.namespace,
			args = Array.prototype.slice.call(arguments, 0);
			args[0] = event;
			event.currentTarget = this;
			for (var j = 0, l = handlers.length; j < l; j++) {
				var handleObj = handlers[j];
				if (run_all || event.namespace_re.test(handleObj.namespace)) {
					event.handler = handleObj.handler;
					event.data = handleObj.data;
					event.handleObj = handleObj;
					var ret = handleObj.handler.apply(this, args);
					if (ret !== undefined) {
						event.result = ret;
						if (ret === false) {
							event.preventDefault();
							event.stopPropagation()
						}
					}
					if (event.isImmediatePropagationStopped()) {
						break
					}
				}
			}
			return event.result
		},
		props : "altKey attrChange attrName bubbles button cancelable charCode clientX clientY ctrlKey currentTarget data detail eventPhase fromElement handler keyCode layerX layerY metaKey newValue offsetX offsetY pageX pageY prevValue relatedNode relatedTarget screenX screenY shiftKey srcElement target toElement view wheelDelta which".split(" "),
		fix : function (event) {
			if (event[jQuery.expando]) {
				return event
			}
			var originalEvent = event;
			event = jQuery.Event(originalEvent);
			for (var i = this.props.length, prop; i; ) {
				prop = this.props[--i];
				event[prop] = originalEvent[prop]
			}
			if (!event.target) {
				event.target = event.srcElement || document
			}
			if (event.target.nodeType === 3) {
				event.target = event.target.parentNode
			}
			if (!event.relatedTarget && event.fromElement) {
				event.relatedTarget = event.fromElement === event.target ? event.toElement : event.fromElement
			}
			if (event.pageX == null && event.clientX != null) {
				var eventDocument = event.target.ownerDocument || document,
				doc = eventDocument.documentElement,
				body = eventDocument.body;
				event.pageX = event.clientX + (doc && doc.scrollLeft || body && body.scrollLeft || 0) - (doc && doc.clientLeft || body && body.clientLeft || 0);
				event.pageY = event.clientY + (doc && doc.scrollTop || body && body.scrollTop || 0) - (doc && doc.clientTop || body && body.clientTop || 0)
			}
			if (event.which == null && (event.charCode != null || event.keyCode != null)) {
				event.which = event.charCode != null ? event.charCode : event.keyCode
			}
			if (!event.metaKey && event.ctrlKey) {
				event.metaKey = event.ctrlKey
			}
			if (!event.which && event.button !== undefined) {
				event.which = (event.button & 1 ? 1 : (event.button & 2 ? 3 : (event.button & 4 ? 2 : 0)))
			}
			return event
		},
		guid : 100000000,
		proxy : jQuery.proxy,
		special : {
			ready : {
				setup : jQuery.bindReady,
				teardown : jQuery.noop
			},
			live : {
				add : function (handleObj) {
					jQuery.event.add(this, liveConvert(handleObj.origType, handleObj.selector), jQuery.extend({}, handleObj, {
							handler : liveHandler,
							guid : handleObj.handler.guid
						}))
				},
				remove : function (handleObj) {
					jQuery.event.remove(this, liveConvert(handleObj.origType, handleObj.selector), handleObj)
				}
			},
			beforeunload : {
				setup : function (data, namespaces, eventHandle) {
					if (jQuery.isWindow(this)) {
						this.onbeforeunload = eventHandle
					}
				},
				teardown : function (namespaces, eventHandle) {
					if (this.onbeforeunload === eventHandle) {
						this.onbeforeunload = null
					}
				}
			}
		}
	};
	jQuery.removeEvent = document.removeEventListener ? function (elem, type, handle) {
		if (elem.removeEventListener) {
			elem.removeEventListener(type, handle, false)
		}
	}
	 : function (elem, type, handle) {
		if (elem.detachEvent) {
			elem.detachEvent("on" + type, handle)
		}
	};
	jQuery.Event = function (src, props) {
		if (!this.preventDefault) {
			return new jQuery.Event(src, props)
		}
		if (src && src.type) {
			this.originalEvent = src;
			this.type = src.type;
			this.isDefaultPrevented = (src.defaultPrevented || src.returnValue === false || src.getPreventDefault && src.getPreventDefault()) ? returnTrue : returnFalse
		} else {
			this.type = src
		}
		if (props) {
			jQuery.extend(this, props)
		}
		this.timeStamp = jQuery.now();
		this[jQuery.expando] = true
	};
	function returnFalse() {
		return false
	}
	function returnTrue() {
		return true
	}
	jQuery.Event.prototype = {
		preventDefault : function () {
			this.isDefaultPrevented = returnTrue;
			var e = this.originalEvent;
			if (!e) {
				return
			}
			if (e.preventDefault) {
				e.preventDefault()
			} else {
				e.returnValue = false
			}
		},
		stopPropagation : function () {
			this.isPropagationStopped = returnTrue;
			var e = this.originalEvent;
			if (!e) {
				return
			}
			if (e.stopPropagation) {
				e.stopPropagation()
			}
			e.cancelBubble = true
		},
		stopImmediatePropagation : function () {
			this.isImmediatePropagationStopped = returnTrue;
			this.stopPropagation()
		},
		isDefaultPrevented : returnFalse,
		isPropagationStopped : returnFalse,
		isImmediatePropagationStopped : returnFalse
	};
	var withinElement = function (event) {
		var parent = event.relatedTarget;
		event.type = event.data;
		try {
			if (parent && parent !== document && !parent.parentNode) {
				return
			}
			while (parent && parent !== this) {
				parent = parent.parentNode
			}
			if (parent !== this) {
				jQuery.event.handle.apply(this, arguments)
			}
		} catch (e) {}
		
	},
	delegate = function (event) {
		event.type = event.data;
		jQuery.event.handle.apply(this, arguments)
	};
	jQuery.each({
		mouseenter : "mouseover",
		mouseleave : "mouseout"
	}, function (orig, fix) {
		jQuery.event.special[orig] = {
			setup : function (data) {
				jQuery.event.add(this, fix, data && data.selector ? delegate : withinElement, orig)
			},
			teardown : function (data) {
				jQuery.event.remove(this, fix, data && data.selector ? delegate : withinElement)
			}
		}
	});
	if (!jQuery.support.submitBubbles) {
		jQuery.event.special.submit = {
			setup : function (data, namespaces) {
				if (!jQuery.nodeName(this, "form")) {
					jQuery.event.add(this, "click.specialSubmit", function (e) {
						var elem = e.target,
						type = elem.type;
						if ((type === "submit" || type === "image") && jQuery(elem).closest("form").length) {
							trigger("submit", this, arguments)
						}
					});
					jQuery.event.add(this, "keypress.specialSubmit", function (e) {
						var elem = e.target,
						type = elem.type;
						if ((type === "text" || type === "password") && jQuery(elem).closest("form").length && e.keyCode === 13) {
							trigger("submit", this, arguments)
						}
					})
				} else {
					return false
				}
			},
			teardown : function (namespaces) {
				jQuery.event.remove(this, ".specialSubmit")
			}
		}
	}
	if (!jQuery.support.changeBubbles) {
		var changeFilters,
		getVal = function (elem) {
			var type = elem.type,
			val = elem.value;
			if (type === "radio" || type === "checkbox") {
				val = elem.checked
			} else {
				if (type === "select-multiple") {
					val = elem.selectedIndex > -1 ? jQuery.map(elem.options, function (elem) {
							return elem.selected
						}).join("-") : ""
				} else {
					if (jQuery.nodeName(elem, "select")) {
						val = elem.selectedIndex
					}
				}
			}
			return val
		},
		testChange = function testChange(e) {
			var elem = e.target,
			data,
			val;
			if (!rformElems.test(elem.nodeName) || elem.readOnly) {
				return
			}
			data = jQuery._data(elem, "_change_data");
			val = getVal(elem);
			if (e.type !== "focusout" || elem.type !== "radio") {
				jQuery._data(elem, "_change_data", val)
			}
			if (data === undefined || val === data) {
				return
			}
			if (data != null || val) {
				e.type = "change";
				e.liveFired = undefined;
				jQuery.event.trigger(e, arguments[1], elem)
			}
		};
		jQuery.event.special.change = {
			filters : {
				focusout : testChange,
				beforedeactivate : testChange,
				click : function (e) {
					var elem = e.target,
					type = jQuery.nodeName(elem, "input") ? elem.type : "";
					if (type === "radio" || type === "checkbox" || jQuery.nodeName(elem, "select")) {
						testChange.call(this, e)
					}
				},
				keydown : function (e) {
					var elem = e.target,
					type = jQuery.nodeName(elem, "input") ? elem.type : "";
					if ((e.keyCode === 13 && !jQuery.nodeName(elem, "textarea")) || (e.keyCode === 32 && (type === "checkbox" || type === "radio")) || type === "select-multiple") {
						testChange.call(this, e)
					}
				},
				beforeactivate : function (e) {
					var elem = e.target;
					jQuery._data(elem, "_change_data", getVal(elem))
				}
			},
			setup : function (data, namespaces) {
				if (this.type === "file") {
					return false
				}
				for (var type in changeFilters) {
					jQuery.event.add(this, type + ".specialChange", changeFilters[type])
				}
				return rformElems.test(this.nodeName)
			},
			teardown : function (namespaces) {
				jQuery.event.remove(this, ".specialChange");
				return rformElems.test(this.nodeName)
			}
		};
		changeFilters = jQuery.event.special.change.filters;
		changeFilters.focus = changeFilters.beforeactivate
	}
	function trigger(type, elem, args) {
		var event = jQuery.extend({}, args[0]);
		event.type = type;
		event.originalEvent = {};
		event.liveFired = undefined;
		jQuery.event.handle.call(elem, event);
		if (event.isDefaultPrevented()) {
			args[0].preventDefault()
		}
	}
	if (!jQuery.support.focusinBubbles) {
		jQuery.each({
			focus : "focusin",
			blur : "focusout"
		}, function (orig, fix) {
			var attaches = 0;
			jQuery.event.special[fix] = {
				setup : function () {
					if (attaches++ === 0) {
						document.addEventListener(orig, handler, true)
					}
				},
				teardown : function () {
					if (--attaches === 0) {
						document.removeEventListener(orig, handler, true)
					}
				}
			};
			function handler(donor) {
				var e = jQuery.event.fix(donor);
				e.type = fix;
				e.originalEvent = {};
				jQuery.event.trigger(e, null, e.target);
				if (e.isDefaultPrevented()) {
					donor.preventDefault()
				}
			}
		})
	}
	jQuery.each(["bind", "one"], function (i, name) {
		jQuery.fn[name] = function (type, data, fn) {
			var handler;
			if (typeof type === "object") {
				for (var key in type) {
					this[name](key, data, type[key], fn)
				}
				return this
			}
			if (arguments.length === 2 || data === false) {
				fn = data;
				data = undefined
			}
			if (name === "one") {
				handler = function (event) {
					jQuery(this).unbind(event, handler);
					return fn.apply(this, arguments)
				};
				handler.guid = fn.guid || jQuery.guid++
			} else {
				handler = fn
			}
			if (type === "unload" && name !== "one") {
				this.one(type, data, fn)
			} else {
				for (var i = 0, l = this.length; i < l; i++) {
					jQuery.event.add(this[i], type, handler, data)
				}
			}
			return this
		}
	});
	jQuery.fn.extend({
		unbind : function (type, fn) {
			if (typeof type === "object" && !type.preventDefault) {
				for (var key in type) {
					this.unbind(key, type[key])
				}
			} else {
				for (var i = 0, l = this.length; i < l; i++) {
					jQuery.event.remove(this[i], type, fn)
				}
			}
			return this
		},
		delegate : function (selector, types, data, fn) {
			return this.live(types, data, fn, selector)
		},
		undelegate : function (selector, types, fn) {
			if (arguments.length === 0) {
				return this.unbind("live")
			} else {
				return this.die(types, null, fn, selector)
			}
		},
		trigger : function (type, data) {
			return this.each(function () {
				jQuery.event.trigger(type, data, this)
			})
		},
		triggerHandler : function (type, data) {
			if (this[0]) {
				return jQuery.event.trigger(type, data, this[0], true)
			}
		},
		toggle : function (fn) {
			var args = arguments,
			guid = fn.guid || jQuery.guid++,
			i = 0,
			toggler = function (event) {
				var lastToggle = (jQuery.data(this, "lastToggle" + fn.guid) || 0) % i;
				jQuery.data(this, "lastToggle" + fn.guid, lastToggle + 1);
				event.preventDefault();
				return args[lastToggle].apply(this, arguments) || false
			};
			toggler.guid = guid;
			while (i < args.length) {
				args[i++].guid = guid
			}
			return this.click(toggler)
		},
		hover : function (fnOver, fnOut) {
			return this.mouseenter(fnOver).mouseleave(fnOut || fnOver)
		}
	});
	var liveMap = {
		focus : "focusin",
		blur : "focusout",
		mouseenter : "mouseover",
		mouseleave : "mouseout"
	};
	jQuery.each(["live", "die"], function (i, name) {
		jQuery.fn[name] = function (types, data, fn, origSelector) {
			var type,
			i = 0,
			match,
			namespaces,
			preType,
			selector = origSelector || this.selector,
			context = origSelector ? this : jQuery(this.context);
			if (typeof types === "object" && !types.preventDefault) {
				for (var key in types) {
					context[name](key, data, types[key], selector)
				}
				return this
			}
			if (name === "die" && !types && origSelector && origSelector.charAt(0) === ".") {
				context.unbind(origSelector);
				return this
			}
			if (data === false || jQuery.isFunction(data)) {
				fn = data || returnFalse;
				data = undefined
			}
			types = (types || "").split(" ");
			while ((type = types[i++]) != null) {
				match = rnamespaces.exec(type);
				namespaces = "";
				if (match) {
					namespaces = match[0];
					type = type.replace(rnamespaces, "")
				}
				if (type === "hover") {
					types.push("mouseenter" + namespaces, "mouseleave" + namespaces);
					continue
				}
				preType = type;
				if (liveMap[type]) {
					types.push(liveMap[type] + namespaces);
					type = type + namespaces
				} else {
					type = (liveMap[type] || type) + namespaces
				}
				if (name === "live") {
					for (var j = 0, l = context.length; j < l; j++) {
						jQuery.event.add(context[j], "live." + liveConvert(type, selector), {
							data : data,
							selector : selector,
							handler : fn,
							origType : type,
							origHandler : fn,
							preType : preType
						})
					}
				} else {
					context.unbind("live." + liveConvert(type, selector), fn)
				}
			}
			return this
		}
	});
	function liveHandler(event) {
		var stop,
		maxLevel,
		related,
		match,
		handleObj,
		elem,
		j,
		i,
		l,
		data,
		close,
		namespace,
		ret,
		elems = [],
		selectors = [],
		events = jQuery._data(this, "events");
		if (event.liveFired === this || !events || !events.live || event.target.disabled || event.button && event.type === "click") {
			return
		}
		if (event.namespace) {
			namespace = new RegExp("(^|\\.)" + event.namespace.split(".").join("\\.(?:.*\\.)?") + "(\\.|$)")
		}
		event.liveFired = this;
		var live = events.live.slice(0);
		for (j = 0; j < live.length; j++) {
			handleObj = live[j];
			if (handleObj.origType.replace(rnamespaces, "") === event.type) {
				selectors.push(handleObj.selector)
			} else {
				live.splice(j--, 1)
			}
		}
		match = jQuery(event.target).closest(selectors, event.currentTarget);
		for (i = 0, l = match.length; i < l; i++) {
			close = match[i];
			for (j = 0; j < live.length; j++) {
				handleObj = live[j];
				if (close.selector === handleObj.selector && (!namespace || namespace.test(handleObj.namespace)) && !close.elem.disabled) {
					elem = close.elem;
					related = null;
					if (handleObj.preType === "mouseenter" || handleObj.preType === "mouseleave") {
						event.type = handleObj.preType;
						related = jQuery(event.relatedTarget).closest(handleObj.selector)[0];
						if (related && jQuery.contains(elem, related)) {
							related = elem
						}
					}
					if (!related || related !== elem) {
						elems.push({
							elem : elem,
							handleObj : handleObj,
							level : close.level
						})
					}
				}
			}
		}
		for (i = 0, l = elems.length; i < l; i++) {
			match = elems[i];
			if (maxLevel && match.level > maxLevel) {
				break
			}
			event.currentTarget = match.elem;
			event.data = match.handleObj.data;
			event.handleObj = match.handleObj;
			ret = match.handleObj.origHandler.apply(match.elem, arguments);
			if (ret === false || event.isPropagationStopped()) {
				maxLevel = match.level;
				if (ret === false) {
					stop = false
				}
				if (event.isImmediatePropagationStopped()) {
					break
				}
			}
		}
		return stop
	}
	function liveConvert(type, selector) {
		return (type && type !== "*" ? type + "." : "") + selector.replace(rperiod, "`").replace(rspaces, "&")
	}
	jQuery.each(("blur focus focusin focusout load resize scroll unload click dblclick mousedown mouseup mousemove mouseover mouseout mouseenter mouseleave change select submit keydown keypress keyup error").split(" "), function (i, name) {
		jQuery.fn[name] = function (data, fn) {
			if (fn == null) {
				fn = data;
				data = null
			}
			return arguments.length > 0 ? this.bind(name, data, fn) : this.trigger(name)
		};
		if (jQuery.attrFn) {
			jQuery.attrFn[name] = true
		}
	});
	/*
	 * Sizzle CSS Selector Engine
	 *  Copyright 2011, The Dojo Foundation
	 *  Released under the MIT, BSD, and GPL Licenses.
	 *  More information: http://sizzlejs.com/
	 */
	(function () {
		var chunker = /((?:\((?:\([^()]+\)|[^()]+)+\)|\[(?:\[[^\[\]]*\]|['"][^'"]*['"]|[^\[\]'"]+)+\]|\\.|[^ >+~,(\[\\]+)+|[>+~])(\s*,\s*)?((?:.|\r|\n)*)/g,
		done = 0,
		toString = Object.prototype.toString,
		hasDuplicate = false,
		baseHasDuplicate = true,
		rBackslash = /\\/g,
		rNonWord = /\W/;
		[0, 0].sort(function () {
			baseHasDuplicate = false;
			return 0
		});
		var Sizzle = function (selector, context, results, seed) {
			results = results || [];
			context = context || document;
			var origContext = context;
			if (context.nodeType !== 1 && context.nodeType !== 9) {
				return []
			}
			if (!selector || typeof selector !== "string") {
				return results
			}
			var m,
			set,
			checkSet,
			extra,
			ret,
			cur,
			pop,
			i,
			prune = true,
			contextXML = Sizzle.isXML(context),
			parts = [],
			soFar = selector;
			do {
				chunker.exec("");
				m = chunker.exec(soFar);
				if (m) {
					soFar = m[3];
					parts.push(m[1]);
					if (m[2]) {
						extra = m[3];
						break
					}
				}
			} while (m);
			if (parts.length > 1 && origPOS.exec(selector)) {
				if (parts.length === 2 && Expr.relative[parts[0]]) {
					set = posProcess(parts[0] + parts[1], context)
				} else {
					set = Expr.relative[parts[0]] ? [context] : Sizzle(parts.shift(), context);
					while (parts.length) {
						selector = parts.shift();
						if (Expr.relative[selector]) {
							selector += parts.shift()
						}
						set = posProcess(selector, set)
					}
				}
			} else {
				if (!seed && parts.length > 1 && context.nodeType === 9 && !contextXML && Expr.match.ID.test(parts[0]) && !Expr.match.ID.test(parts[parts.length - 1])) {
					ret = Sizzle.find(parts.shift(), context, contextXML);
					context = ret.expr ? Sizzle.filter(ret.expr, ret.set)[0] : ret.set[0]
				}
				if (context) {
					ret = seed ? {
						expr : parts.pop(),
						set : makeArray(seed)
					}
					 : Sizzle.find(parts.pop(), parts.length === 1 && (parts[0] === "~" || parts[0] === "+") && context.parentNode ? context.parentNode : context, contextXML);
					set = ret.expr ? Sizzle.filter(ret.expr, ret.set) : ret.set;
					if (parts.length > 0) {
						checkSet = makeArray(set)
					} else {
						prune = false
					}
					while (parts.length) {
						cur = parts.pop();
						pop = cur;
						if (!Expr.relative[cur]) {
							cur = ""
						} else {
							pop = parts.pop()
						}
						if (pop == null) {
							pop = context
						}
						Expr.relative[cur](checkSet, pop, contextXML)
					}
				} else {
					checkSet = parts = []
				}
			}
			if (!checkSet) {
				checkSet = set
			}
			if (!checkSet) {
				Sizzle.error(cur || selector)
			}
			if (toString.call(checkSet) === "[object Array]") {
				if (!prune) {
					results.push.apply(results, checkSet)
				} else {
					if (context && context.nodeType === 1) {
						for (i = 0; checkSet[i] != null; i++) {
							if (checkSet[i] && (checkSet[i] === true || checkSet[i].nodeType === 1 && Sizzle.contains(context, checkSet[i]))) {
								results.push(set[i])
							}
						}
					} else {
						for (i = 0; checkSet[i] != null; i++) {
							if (checkSet[i] && checkSet[i].nodeType === 1) {
								results.push(set[i])
							}
						}
					}
				}
			} else {
				makeArray(checkSet, results)
			}
			if (extra) {
				Sizzle(extra, origContext, results, seed);
				Sizzle.uniqueSort(results)
			}
			return results
		};
		Sizzle.uniqueSort = function (results) {
			if (sortOrder) {
				hasDuplicate = baseHasDuplicate;
				results.sort(sortOrder);
				if (hasDuplicate) {
					for (var i = 1; i < results.length; i++) {
						if (results[i] === results[i - 1]) {
							results.splice(i--, 1)
						}
					}
				}
			}
			return results
		};
		Sizzle.matches = function (expr, set) {
			return Sizzle(expr, null, null, set)
		};
		Sizzle.matchesSelector = function (node, expr) {
			return Sizzle(expr, null, null, [node]).length > 0
		};
		Sizzle.find = function (expr, context, isXML) {
			var set;
			if (!expr) {
				return []
			}
			for (var i = 0, l = Expr.order.length; i < l; i++) {
				var match,
				type = Expr.order[i];
				if ((match = Expr.leftMatch[type].exec(expr))) {
					var left = match[1];
					match.splice(1, 1);
					if (left.substr(left.length - 1) !== "\\") {
						match[1] = (match[1] || "").replace(rBackslash, "");
						set = Expr.find[type](match, context, isXML);
						if (set != null) {
							expr = expr.replace(Expr.match[type], "");
							break
						}
					}
				}
			}
			if (!set) {
				set = typeof context.getElementsByTagName !== "undefined" ? context.getElementsByTagName("*") : []
			}
			return {
				set : set,
				expr : expr
			}
		};
		Sizzle.filter = function (expr, set, inplace, not) {
			var match,
			anyFound,
			old = expr,
			result = [],
			curLoop = set,
			isXMLFilter = set && set[0] && Sizzle.isXML(set[0]);
			while (expr && set.length) {
				for (var type in Expr.filter) {
					if ((match = Expr.leftMatch[type].exec(expr)) != null && match[2]) {
						var found,
						item,
						filter = Expr.filter[type],
						left = match[1];
						anyFound = false;
						match.splice(1, 1);
						if (left.substr(left.length - 1) === "\\") {
							continue
						}
						if (curLoop === result) {
							result = []
						}
						if (Expr.preFilter[type]) {
							match = Expr.preFilter[type](match, curLoop, inplace, result, not, isXMLFilter);
							if (!match) {
								anyFound = found = true
							} else {
								if (match === true) {
									continue
								}
							}
						}
						if (match) {
							for (var i = 0; (item = curLoop[i]) != null; i++) {
								if (item) {
									found = filter(item, match, i, curLoop);
									var pass = not^!!found;
									if (inplace && found != null) {
										if (pass) {
											anyFound = true
										} else {
											curLoop[i] = false
										}
									} else {
										if (pass) {
											result.push(item);
											anyFound = true
										}
									}
								}
							}
						}
						if (found !== undefined) {
							if (!inplace) {
								curLoop = result
							}
							expr = expr.replace(Expr.match[type], "");
							if (!anyFound) {
								return []
							}
							break
						}
					}
				}
				if (expr === old) {
					if (anyFound == null) {
						Sizzle.error(expr)
					} else {
						break
					}
				}
				old = expr
			}
			return curLoop
		};
		Sizzle.error = function (msg) {
			throw "Syntax error, unrecognized expression: " + msg
		};
		var Expr = Sizzle.selectors = {
			order : ["ID", "NAME", "TAG"],
			match : {
				ID : /#((?:[\w\u00c0-\uFFFF\-]|\\.)+)/,
				CLASS : /\.((?:[\w\u00c0-\uFFFF\-]|\\.)+)/,
				NAME : /\[name=['"]*((?:[\w\u00c0-\uFFFF\-]|\\.)+)['"]*\]/,
				ATTR : /\[\s*((?:[\w\u00c0-\uFFFF\-]|\\.)+)\s*(?:(\S?=)\s*(?:(['"])(.*?)\3|(#?(?:[\w\u00c0-\uFFFF\-]|\\.)*)|)|)\s*\]/,
				TAG : /^((?:[\w\u00c0-\uFFFF\*\-]|\\.)+)/,
				CHILD : /:(only|nth|last|first)-child(?:\(\s*(even|odd|(?:[+\-]?\d+|(?:[+\-]?\d*)?n\s*(?:[+\-]\s*\d+)?))\s*\))?/,
				POS : /:(nth|eq|gt|lt|first|last|even|odd)(?:\((\d*)\))?(?=[^\-]|$)/,
				PSEUDO : /:((?:[\w\u00c0-\uFFFF\-]|\\.)+)(?:\((['"]?)((?:\([^\)]+\)|[^\(\)]*)+)\2\))?/
			},
			leftMatch : {},
			attrMap : {
				"class" : "className",
				"for" : "htmlFor"
			},
			attrHandle : {
				href : function (elem) {
					return elem.getAttribute("href")
				},
				type : function (elem) {
					return elem.getAttribute("type")
				}
			},
			relative : {
				"+" : function (checkSet, part) {
					var isPartStr = typeof part === "string",
					isTag = isPartStr && !rNonWord.test(part),
					isPartStrNotTag = isPartStr && !isTag;
					if (isTag) {
						part = part.toLowerCase()
					}
					for (var i = 0, l = checkSet.length, elem; i < l; i++) {
						if ((elem = checkSet[i])) {
							while ((elem = elem.previousSibling) && elem.nodeType !== 1) {}
							
							checkSet[i] = isPartStrNotTag || elem && elem.nodeName.toLowerCase() === part ? elem || false : elem === part
						}
					}
					if (isPartStrNotTag) {
						Sizzle.filter(part, checkSet, true)
					}
				},
				">" : function (checkSet, part) {
					var elem,
					isPartStr = typeof part === "string",
					i = 0,
					l = checkSet.length;
					if (isPartStr && !rNonWord.test(part)) {
						part = part.toLowerCase();
						for (; i < l; i++) {
							elem = checkSet[i];
							if (elem) {
								var parent = elem.parentNode;
								checkSet[i] = parent.nodeName.toLowerCase() === part ? parent : false
							}
						}
					} else {
						for (; i < l; i++) {
							elem = checkSet[i];
							if (elem) {
								checkSet[i] = isPartStr ? elem.parentNode : elem.parentNode === part
							}
						}
						if (isPartStr) {
							Sizzle.filter(part, checkSet, true)
						}
					}
				},
				"" : function (checkSet, part, isXML) {
					var nodeCheck,
					doneName = done++,
					checkFn = dirCheck;
					if (typeof part === "string" && !rNonWord.test(part)) {
						part = part.toLowerCase();
						nodeCheck = part;
						checkFn = dirNodeCheck
					}
					checkFn("parentNode", part, doneName, checkSet, nodeCheck, isXML)
				},
				"~" : function (checkSet, part, isXML) {
					var nodeCheck,
					doneName = done++,
					checkFn = dirCheck;
					if (typeof part === "string" && !rNonWord.test(part)) {
						part = part.toLowerCase();
						nodeCheck = part;
						checkFn = dirNodeCheck
					}
					checkFn("previousSibling", part, doneName, checkSet, nodeCheck, isXML)
				}
			},
			find : {
				ID : function (match, context, isXML) {
					if (typeof context.getElementById !== "undefined" && !isXML) {
						var m = context.getElementById(match[1]);
						return m && m.parentNode ? [m] : []
					}
				},
				NAME : function (match, context) {
					if (typeof context.getElementsByName !== "undefined") {
						var ret = [],
						results = context.getElementsByName(match[1]);
						for (var i = 0, l = results.length; i < l; i++) {
							if (results[i].getAttribute("name") === match[1]) {
								ret.push(results[i])
							}
						}
						return ret.length === 0 ? null : ret
					}
				},
				TAG : function (match, context) {
					if (typeof context.getElementsByTagName !== "undefined") {
						return context.getElementsByTagName(match[1])
					}
				}
			},
			preFilter : {
				CLASS : function (match, curLoop, inplace, result, not, isXML) {
					match = " " + match[1].replace(rBackslash, "") + " ";
					if (isXML) {
						return match
					}
					for (var i = 0, elem; (elem = curLoop[i]) != null; i++) {
						if (elem) {
							if (not^(elem.className && (" " + elem.className + " ").replace(/[\t\n\r]/g, " ").indexOf(match) >= 0)) {
								if (!inplace) {
									result.push(elem)
								}
							} else {
								if (inplace) {
									curLoop[i] = false
								}
							}
						}
					}
					return false
				},
				ID : function (match) {
					return match[1].replace(rBackslash, "")
				},
				TAG : function (match, curLoop) {
					return match[1].replace(rBackslash, "").toLowerCase()
				},
				CHILD : function (match) {
					if (match[1] === "nth") {
						if (!match[2]) {
							Sizzle.error(match[0])
						}
						match[2] = match[2].replace(/^\+|\s*/g, "");
						var test = /(-?)(\d*)(?:n([+\-]?\d*))?/.exec(match[2] === "even" && "2n" || match[2] === "odd" && "2n+1" || !/\D/.test(match[2]) && "0n+" + match[2] || match[2]);
						match[2] = (test[1] + (test[2] || 1)) - 0;
						match[3] = test[3] - 0
					} else {
						if (match[2]) {
							Sizzle.error(match[0])
						}
					}
					match[0] = done++;
					return match
				},
				ATTR : function (match, curLoop, inplace, result, not, isXML) {
					var name = match[1] = match[1].replace(rBackslash, "");
					if (!isXML && Expr.attrMap[name]) {
						match[1] = Expr.attrMap[name]
					}
					match[4] = (match[4] || match[5] || "").replace(rBackslash, "");
					if (match[2] === "~=") {
						match[4] = " " + match[4] + " "
					}
					return match
				},
				PSEUDO : function (match, curLoop, inplace, result, not) {
					if (match[1] === "not") {
						if ((chunker.exec(match[3]) || "").length > 1 || /^\w/.test(match[3])) {
							match[3] = Sizzle(match[3], null, null, curLoop)
						} else {
							var ret = Sizzle.filter(match[3], curLoop, inplace, true^not);
							if (!inplace) {
								result.push.apply(result, ret)
							}
							return false
						}
					} else {
						if (Expr.match.POS.test(match[0]) || Expr.match.CHILD.test(match[0])) {
							return true
						}
					}
					return match
				},
				POS : function (match) {
					match.unshift(true);
					return match
				}
			},
			filters : {
				enabled : function (elem) {
					return elem.disabled === false && elem.type !== "hidden"
				},
				disabled : function (elem) {
					return elem.disabled === true
				},
				checked : function (elem) {
					return elem.checked === true
				},
				selected : function (elem) {
					if (elem.parentNode) {
						elem.parentNode.selectedIndex
					}
					return elem.selected === true
				},
				parent : function (elem) {
					return !!elem.firstChild
				},
				empty : function (elem) {
					return !elem.firstChild
				},
				has : function (elem, i, match) {
					return !!Sizzle(match[3], elem).length
				},
				header : function (elem) {
					return (/h\d/i).test(elem.nodeName)
				},
				text : function (elem) {
					var attr = elem.getAttribute("type"),
					type = elem.type;
					return elem.nodeName.toLowerCase() === "input" && "text" === type && (attr === type || attr === null)
				},
				radio : function (elem) {
					return elem.nodeName.toLowerCase() === "input" && "radio" === elem.type
				},
				checkbox : function (elem) {
					return elem.nodeName.toLowerCase() === "input" && "checkbox" === elem.type
				},
				file : function (elem) {
					return elem.nodeName.toLowerCase() === "input" && "file" === elem.type
				},
				password : function (elem) {
					return elem.nodeName.toLowerCase() === "input" && "password" === elem.type
				},
				submit : function (elem) {
					var name = elem.nodeName.toLowerCase();
					return (name === "input" || name === "button") && "submit" === elem.type
				},
				image : function (elem) {
					return elem.nodeName.toLowerCase() === "input" && "image" === elem.type
				},
				reset : function (elem) {
					var name = elem.nodeName.toLowerCase();
					return (name === "input" || name === "button") && "reset" === elem.type
				},
				button : function (elem) {
					var name = elem.nodeName.toLowerCase();
					return name === "input" && "button" === elem.type || name === "button"
				},
				input : function (elem) {
					return (/input|select|textarea|button/i).test(elem.nodeName)
				},
				focus : function (elem) {
					return elem === elem.ownerDocument.activeElement
				}
			},
			setFilters : {
				first : function (elem, i) {
					return i === 0
				},
				last : function (elem, i, match, array) {
					return i === array.length - 1
				},
				even : function (elem, i) {
					return i % 2 === 0
				},
				odd : function (elem, i) {
					return i % 2 === 1
				},
				lt : function (elem, i, match) {
					return i < match[3] - 0
				},
				gt : function (elem, i, match) {
					return i > match[3] - 0
				},
				nth : function (elem, i, match) {
					return match[3] - 0 === i
				},
				eq : function (elem, i, match) {
					return match[3] - 0 === i
				}
			},
			filter : {
				PSEUDO : function (elem, match, i, array) {
					var name = match[1],
					filter = Expr.filters[name];
					if (filter) {
						return filter(elem, i, match, array)
					} else {
						if (name === "contains") {
							return (elem.textContent || elem.innerText || Sizzle.getText([elem]) || "").indexOf(match[3]) >= 0
						} else {
							if (name === "not") {
								var not = match[3];
								for (var j = 0, l = not.length; j < l; j++) {
									if (not[j] === elem) {
										return false
									}
								}
								return true
							} else {
								Sizzle.error(name)
							}
						}
					}
				},
				CHILD : function (elem, match) {
					var type = match[1],
					node = elem;
					switch (type) {
					case "only":
					case "first":
						while ((node = node.previousSibling)) {
							if (node.nodeType === 1) {
								return false
							}
						}
						if (type === "first") {
							return true
						}
						node = elem;
					case "last":
						while ((node = node.nextSibling)) {
							if (node.nodeType === 1) {
								return false
							}
						}
						return true;
					case "nth":
						var first = match[2],
						last = match[3];
						if (first === 1 && last === 0) {
							return true
						}
						var doneName = match[0],
						parent = elem.parentNode;
						if (parent && (parent.sizcache !== doneName || !elem.nodeIndex)) {
							var count = 0;
							for (node = parent.firstChild; node; node = node.nextSibling) {
								if (node.nodeType === 1) {
									node.nodeIndex = ++count
								}
							}
							parent.sizcache = doneName
						}
						var diff = elem.nodeIndex - last;
						if (first === 0) {
							return diff === 0
						} else {
							return (diff % first === 0 && diff / first >= 0)
						}
					}
				},
				ID : function (elem, match) {
					return elem.nodeType === 1 && elem.getAttribute("id") === match
				},
				TAG : function (elem, match) {
					return (match === "*" && elem.nodeType === 1) || elem.nodeName.toLowerCase() === match
				},
				CLASS : function (elem, match) {
					return (" " + (elem.className || elem.getAttribute("class")) + " ").indexOf(match) > -1
				},
				ATTR : function (elem, match) {
					var name = match[1],
					result = Expr.attrHandle[name] ? Expr.attrHandle[name](elem) : elem[name] != null ? elem[name] : elem.getAttribute(name),
					value = result + "",
					type = match[2],
					check = match[4];
					return result == null ? type === "!=" : type === "=" ? value === check : type === "*=" ? value.indexOf(check) >= 0 : type === "~=" ? (" " + value + " ").indexOf(check) >= 0 : !check ? value && result !== false : type === "!=" ? value !== check : type === "^=" ? value.indexOf(check) === 0 : type === "$=" ? value.substr(value.length - check.length) === check : type === "|=" ? value === check || value.substr(0, check.length + 1) === check + "-" : false
				},
				POS : function (elem, match, i, array) {
					var name = match[2],
					filter = Expr.setFilters[name];
					if (filter) {
						return filter(elem, i, match, array)
					}
				}
			}
		};
		var origPOS = Expr.match.POS,
		fescape = function (all, num) {
			return "\\" + (num - 0 + 1)
		};
		for (var type in Expr.match) {
			Expr.match[type] = new RegExp(Expr.match[type].source + (/(?![^\[]*\])(?![^\(]*\))/.source));
			Expr.leftMatch[type] = new RegExp(/(^(?:.|\r|\n)*?)/.source + Expr.match[type].source.replace(/\\(\d+)/g, fescape))
		}
		var makeArray = function (array, results) {
			array = Array.prototype.slice.call(array, 0);
			if (results) {
				results.push.apply(results, array);
				return results
			}
			return array
		};
		try {
			Array.prototype.slice.call(document.documentElement.childNodes, 0)[0].nodeType
		} catch (e) {
			makeArray = function (array, results) {
				var i = 0,
				ret = results || [];
				if (toString.call(array) === "[object Array]") {
					Array.prototype.push.apply(ret, array)
				} else {
					if (typeof array.length === "number") {
						for (var l = array.length; i < l; i++) {
							ret.push(array[i])
						}
					} else {
						for (; array[i]; i++) {
							ret.push(array[i])
						}
					}
				}
				return ret
			}
		}
		var sortOrder,
		siblingCheck;
		if (document.documentElement.compareDocumentPosition) {
			sortOrder = function (a, b) {
				if (a === b) {
					hasDuplicate = true;
					return 0
				}
				if (!a.compareDocumentPosition || !b.compareDocumentPosition) {
					return a.compareDocumentPosition ? -1 : 1
				}
				return a.compareDocumentPosition(b) & 4 ? -1 : 1
			}
		} else {
			sortOrder = function (a, b) {
				if (a === b) {
					hasDuplicate = true;
					return 0
				} else {
					if (a.sourceIndex && b.sourceIndex) {
						return a.sourceIndex - b.sourceIndex
					}
				}
				var al,
				bl,
				ap = [],
				bp = [],
				aup = a.parentNode,
				bup = b.parentNode,
				cur = aup;
				if (aup === bup) {
					return siblingCheck(a, b)
				} else {
					if (!aup) {
						return -1
					} else {
						if (!bup) {
							return 1
						}
					}
				}
				while (cur) {
					ap.unshift(cur);
					cur = cur.parentNode
				}
				cur = bup;
				while (cur) {
					bp.unshift(cur);
					cur = cur.parentNode
				}
				al = ap.length;
				bl = bp.length;
				for (var i = 0; i < al && i < bl; i++) {
					if (ap[i] !== bp[i]) {
						return siblingCheck(ap[i], bp[i])
					}
				}
				return i === al ? siblingCheck(a, bp[i], -1) : siblingCheck(ap[i], b, 1)
			};
			siblingCheck = function (a, b, ret) {
				if (a === b) {
					return ret
				}
				var cur = a.nextSibling;
				while (cur) {
					if (cur === b) {
						return -1
					}
					cur = cur.nextSibling
				}
				return 1
			}
		}
		Sizzle.getText = function (elems) {
			var ret = "",
			elem;
			for (var i = 0; elems[i]; i++) {
				elem = elems[i];
				if (elem.nodeType === 3 || elem.nodeType === 4) {
					ret += elem.nodeValue
				} else {
					if (elem.nodeType !== 8) {
						ret += Sizzle.getText(elem.childNodes)
					}
				}
			}
			return ret
		};
		(function () {
			var form = document.createElement("div"),
			id = "script" + (new Date()).getTime(),
			root = document.documentElement;
			form.innerHTML = "<a name='" + id + "'/>";
			root.insertBefore(form, root.firstChild);
			if (document.getElementById(id)) {
				Expr.find.ID = function (match, context, isXML) {
					if (typeof context.getElementById !== "undefined" && !isXML) {
						var m = context.getElementById(match[1]);
						return m ? m.id === match[1] || typeof m.getAttributeNode !== "undefined" && m.getAttributeNode("id").nodeValue === match[1] ? [m] : undefined : []
					}
				};
				Expr.filter.ID = function (elem, match) {
					var node = typeof elem.getAttributeNode !== "undefined" && elem.getAttributeNode("id");
					return elem.nodeType === 1 && node && node.nodeValue === match
				}
			}
			root.removeChild(form);
			root = form = null
		})();
		(function () {
			var div = document.createElement("div");
			div.appendChild(document.createComment(""));
			if (div.getElementsByTagName("*").length > 0) {
				Expr.find.TAG = function (match, context) {
					var results = context.getElementsByTagName(match[1]);
					if (match[1] === "*") {
						var tmp = [];
						for (var i = 0; results[i]; i++) {
							if (results[i].nodeType === 1) {
								tmp.push(results[i])
							}
						}
						results = tmp
					}
					return results
				}
			}
			div.innerHTML = "<a href='#'></a>";
			if (div.firstChild && typeof div.firstChild.getAttribute !== "undefined" && div.firstChild.getAttribute("href") !== "#") {
				Expr.attrHandle.href = function (elem) {
					return elem.getAttribute("href", 2)
				}
			}
			div = null
		})();
		if (document.querySelectorAll) {
			(function () {
				var oldSizzle = Sizzle,
				div = document.createElement("div"),
				id = "__sizzle__";
				div.innerHTML = "<p class='TEST'></p>";
				if (div.querySelectorAll && div.querySelectorAll(".TEST").length === 0) {
					return
				}
				Sizzle = function (query, context, extra, seed) {
					context = context || document;
					if (!seed && !Sizzle.isXML(context)) {
						var match = /^(\w+$)|^\.([\w\-]+$)|^#([\w\-]+$)/.exec(query);
						if (match && (context.nodeType === 1 || context.nodeType === 9)) {
							if (match[1]) {
								return makeArray(context.getElementsByTagName(query), extra)
							} else {
								if (match[2] && Expr.find.CLASS && context.getElementsByClassName) {
									return makeArray(context.getElementsByClassName(match[2]), extra)
								}
							}
						}
						if (context.nodeType === 9) {
							if (query === "body" && context.body) {
								return makeArray([context.body], extra)
							} else {
								if (match && match[3]) {
									var elem = context.getElementById(match[3]);
									if (elem && elem.parentNode) {
										if (elem.id === match[3]) {
											return makeArray([elem], extra)
										}
									} else {
										return makeArray([], extra)
									}
								}
							}
							try {
								return makeArray(context.querySelectorAll(query), extra)
							} catch (qsaError) {}
							
						} else {
							if (context.nodeType === 1 && context.nodeName.toLowerCase() !== "object") {
								var oldContext = context,
								old = context.getAttribute("id"),
								nid = old || id,
								hasParent = context.parentNode,
								relativeHierarchySelector = /^\s*[+~]/.test(query);
								if (!old) {
									context.setAttribute("id", nid)
								} else {
									nid = nid.replace(/'/g, "\\$&")
								}
								if (relativeHierarchySelector && hasParent) {
									context = context.parentNode
								}
								try {
									if (!relativeHierarchySelector || hasParent) {
										return makeArray(context.querySelectorAll("[id='" + nid + "'] " + query), extra)
									}
								} catch (pseudoError) {}
								
								finally {
									if (!old) {
										oldContext.removeAttribute("id")
									}
								}
							}
						}
					}
					return oldSizzle(query, context, extra, seed)
				};
				for (var prop in oldSizzle) {
					Sizzle[prop] = oldSizzle[prop]
				}
				div = null
			})()
		}
		(function () {
			var html = document.documentElement,
			matches = html.matchesSelector || html.mozMatchesSelector || html.webkitMatchesSelector || html.msMatchesSelector;
			if (matches) {
				var disconnectedMatch = !matches.call(document.createElement("div"), "div"),
				pseudoWorks = false;
				try {
					matches.call(document.documentElement, "[test!='']:sizzle")
				} catch (pseudoError) {
					pseudoWorks = true
				}
				Sizzle.matchesSelector = function (node, expr) {
					expr = expr.replace(/\=\s*([^'"\]]*)\s*\]/g, "='$1']");
					if (!Sizzle.isXML(node)) {
						try {
							if (pseudoWorks || !Expr.match.PSEUDO.test(expr) && !/!=/.test(expr)) {
								var ret = matches.call(node, expr);
								if (ret || !disconnectedMatch || node.document && node.document.nodeType !== 11) {
									return ret
								}
							}
						} catch (e) {}
						
					}
					return Sizzle(expr, null, null, [node]).length > 0
				}
			}
		})();
		(function () {
			var div = document.createElement("div");
			div.innerHTML = "<div class='test e'></div><div class='test'></div>";
			if (!div.getElementsByClassName || div.getElementsByClassName("e").length === 0) {
				return
			}
			div.lastChild.className = "e";
			if (div.getElementsByClassName("e").length === 1) {
				return
			}
			Expr.order.splice(1, 0, "CLASS");
			Expr.find.CLASS = function (match, context, isXML) {
				if (typeof context.getElementsByClassName !== "undefined" && !isXML) {
					return context.getElementsByClassName(match[1])
				}
			};
			div = null
		})();
		function dirNodeCheck(dir, cur, doneName, checkSet, nodeCheck, isXML) {
			for (var i = 0, l = checkSet.length; i < l; i++) {
				var elem = checkSet[i];
				if (elem) {
					var match = false;
					elem = elem[dir];
					while (elem) {
						if (elem.sizcache === doneName) {
							match = checkSet[elem.sizset];
							break
						}
						if (elem.nodeType === 1 && !isXML) {
							elem.sizcache = doneName;
							elem.sizset = i
						}
						if (elem.nodeName.toLowerCase() === cur) {
							match = elem;
							break
						}
						elem = elem[dir]
					}
					checkSet[i] = match
				}
			}
		}
		function dirCheck(dir, cur, doneName, checkSet, nodeCheck, isXML) {
			for (var i = 0, l = checkSet.length; i < l; i++) {
				var elem = checkSet[i];
				if (elem) {
					var match = false;
					elem = elem[dir];
					while (elem) {
						if (elem.sizcache === doneName) {
							match = checkSet[elem.sizset];
							break
						}
						if (elem.nodeType === 1) {
							if (!isXML) {
								elem.sizcache = doneName;
								elem.sizset = i
							}
							if (typeof cur !== "string") {
								if (elem === cur) {
									match = true;
									break
								}
							} else {
								if (Sizzle.filter(cur, [elem]).length > 0) {
									match = elem;
									break
								}
							}
						}
						elem = elem[dir]
					}
					checkSet[i] = match
				}
			}
		}
		if (document.documentElement.contains) {
			Sizzle.contains = function (a, b) {
				return a !== b && (a.contains ? a.contains(b) : true)
			}
		} else {
			if (document.documentElement.compareDocumentPosition) {
				Sizzle.contains = function (a, b) {
					return !!(a.compareDocumentPosition(b) & 16)
				}
			} else {
				Sizzle.contains = function () {
					return false
				}
			}
		}
		Sizzle.isXML = function (elem) {
			var documentElement = (elem ? elem.ownerDocument || elem : 0).documentElement;
			return documentElement ? documentElement.nodeName !== "HTML" : false
		};
		var posProcess = function (selector, context) {
			var match,
			tmpSet = [],
			later = "",
			root = context.nodeType ? [context] : context;
			while ((match = Expr.match.PSEUDO.exec(selector))) {
				later += match[0];
				selector = selector.replace(Expr.match.PSEUDO, "")
			}
			selector = Expr.relative[selector] ? selector + "*" : selector;
			for (var i = 0, l = root.length; i < l; i++) {
				Sizzle(selector, root[i], tmpSet)
			}
			return Sizzle.filter(later, tmpSet)
		};
		jQuery.find = Sizzle;
		jQuery.expr = Sizzle.selectors;
		jQuery.expr[":"] = jQuery.expr.filters;
		jQuery.unique = Sizzle.uniqueSort;
		jQuery.text = Sizzle.getText;
		jQuery.isXMLDoc = Sizzle.isXML;
		jQuery.contains = Sizzle.contains
	})();
	var runtil = /Until$/,
	rparentsprev = /^(?:parents|prevUntil|prevAll)/,
	rmultiselector = /,/,
	isSimple = /^.[^:#\[\.,]*$/,
	slice = Array.prototype.slice,
	POS = jQuery.expr.match.POS,
	guaranteedUnique = {
		children : true,
		contents : true,
		next : true,
		prev : true
	};
	jQuery.fn.extend({
		find : function (selector) {
			var self = this,
			i,
			l;
			if (typeof selector !== "string") {
				return jQuery(selector).filter(function () {
					for (i = 0, l = self.length; i < l; i++) {
						if (jQuery.contains(self[i], this)) {
							return true
						}
					}
				})
			}
			var ret = this.pushStack("", "find", selector),
			length,
			n,
			r;
			for (i = 0, l = this.length; i < l; i++) {
				length = ret.length;
				jQuery.find(selector, this[i], ret);
				if (i > 0) {
					for (n = length; n < ret.length; n++) {
						for (r = 0; r < length; r++) {
							if (ret[r] === ret[n]) {
								ret.splice(n--, 1);
								break
							}
						}
					}
				}
			}
			return ret
		},
		has : function (target) {
			var targets = jQuery(target);
			return this.filter(function () {
				for (var i = 0, l = targets.length; i < l; i++) {
					if (jQuery.contains(this, targets[i])) {
						return true
					}
				}
			})
		},
		not : function (selector) {
			return this.pushStack(winnow(this, selector, false), "not", selector)
		},
		filter : function (selector) {
			return this.pushStack(winnow(this, selector, true), "filter", selector)
		},
		is : function (selector) {
			return !!selector && (typeof selector === "string" ? jQuery.filter(selector, this).length > 0 : this.filter(selector).length > 0)
		},
		closest : function (selectors, context) {
			var ret = [],
			i,
			l,
			cur = this[0];
			if (jQuery.isArray(selectors)) {
				var match,
				selector,
				matches = {},
				level = 1;
				if (cur && selectors.length) {
					for (i = 0, l = selectors.length; i < l; i++) {
						selector = selectors[i];
						if (!matches[selector]) {
							matches[selector] = POS.test(selector) ? jQuery(selector, context || this.context) : selector
						}
					}
					while (cur && cur.ownerDocument && cur !== context) {
						for (selector in matches) {
							match = matches[selector];
							if (match.jquery ? match.index(cur) > -1 : jQuery(cur).is(match)) {
								ret.push({
									selector : selector,
									elem : cur,
									level : level
								})
							}
						}
						cur = cur.parentNode;
						level++
					}
				}
				return ret
			}
			var pos = POS.test(selectors) || typeof selectors !== "string" ? jQuery(selectors, context || this.context) : 0;
			for (i = 0, l = this.length; i < l; i++) {
				cur = this[i];
				while (cur) {
					if (pos ? pos.index(cur) > -1 : jQuery.find.matchesSelector(cur, selectors)) {
						ret.push(cur);
						break
					} else {
						cur = cur.parentNode;
						if (!cur || !cur.ownerDocument || cur === context || cur.nodeType === 11) {
							break
						}
					}
				}
			}
			ret = ret.length > 1 ? jQuery.unique(ret) : ret;
			return this.pushStack(ret, "closest", selectors)
		},
		index : function (elem) {
			if (!elem || typeof elem === "string") {
				return jQuery.inArray(this[0], elem ? jQuery(elem) : this.parent().children())
			}
			return jQuery.inArray(elem.jquery ? elem[0] : elem, this)
		},
		add : function (selector, context) {
			var set = typeof selector === "string" ? jQuery(selector, context) : jQuery.makeArray(selector && selector.nodeType ? [selector] : selector),
			all = jQuery.merge(this.get(), set);
			return this.pushStack(isDisconnected(set[0]) || isDisconnected(all[0]) ? all : jQuery.unique(all))
		},
		andSelf : function () {
			return this.add(this.prevObject)
		}
	});
	function isDisconnected(node) {
		return !node || !node.parentNode || node.parentNode.nodeType === 11
	}
	jQuery.each({
		parent : function (elem) {
			var parent = elem.parentNode;
			return parent && parent.nodeType !== 11 ? parent : null
		},
		parents : function (elem) {
			return jQuery.dir(elem, "parentNode")
		},
		parentsUntil : function (elem, i, until) {
			return jQuery.dir(elem, "parentNode", until)
		},
		next : function (elem) {
			return jQuery.nth(elem, 2, "nextSibling")
		},
		prev : function (elem) {
			return jQuery.nth(elem, 2, "previousSibling")
		},
		nextAll : function (elem) {
			return jQuery.dir(elem, "nextSibling")
		},
		prevAll : function (elem) {
			return jQuery.dir(elem, "previousSibling")
		},
		nextUntil : function (elem, i, until) {
			return jQuery.dir(elem, "nextSibling", until)
		},
		prevUntil : function (elem, i, until) {
			return jQuery.dir(elem, "previousSibling", until)
		},
		siblings : function (elem) {
			return jQuery.sibling(elem.parentNode.firstChild, elem)
		},
		children : function (elem) {
			return jQuery.sibling(elem.firstChild)
		},
		contents : function (elem) {
			return jQuery.nodeName(elem, "iframe") ? elem.contentDocument || elem.contentWindow.document : jQuery.makeArray(elem.childNodes)
		}
	}, function (name, fn) {
		jQuery.fn[name] = function (until, selector) {
			var ret = jQuery.map(this, fn, until),
			args = slice.call(arguments);
			if (!runtil.test(name)) {
				selector = until
			}
			if (selector && typeof selector === "string") {
				ret = jQuery.filter(selector, ret)
			}
			ret = this.length > 1 && !guaranteedUnique[name] ? jQuery.unique(ret) : ret;
			if ((this.length > 1 || rmultiselector.test(selector)) && rparentsprev.test(name)) {
				ret = ret.reverse()
			}
			return this.pushStack(ret, name, args.join(","))
		}
	});
	jQuery.extend({
		filter : function (expr, elems, not) {
			if (not) {
				expr = ":not(" + expr + ")"
			}
			return elems.length === 1 ? jQuery.find.matchesSelector(elems[0], expr) ? [elems[0]] : [] : jQuery.find.matches(expr, elems)
		},
		dir : function (elem, dir, until) {
			var matched = [],
			cur = elem[dir];
			while (cur && cur.nodeType !== 9 && (until === undefined || cur.nodeType !== 1 || !jQuery(cur).is(until))) {
				if (cur.nodeType === 1) {
					matched.push(cur)
				}
				cur = cur[dir]
			}
			return matched
		},
		nth : function (cur, result, dir, elem) {
			result = result || 1;
			var num = 0;
			for (; cur; cur = cur[dir]) {
				if (cur.nodeType === 1 && ++num === result) {
					break
				}
			}
			return cur
		},
		sibling : function (n, elem) {
			var r = [];
			for (; n; n = n.nextSibling) {
				if (n.nodeType === 1 && n !== elem) {
					r.push(n)
				}
			}
			return r
		}
	});
	function winnow(elements, qualifier, keep) {
		qualifier = qualifier || 0;
		if (jQuery.isFunction(qualifier)) {
			return jQuery.grep(elements, function (elem, i) {
				var retVal = !!qualifier.call(elem, i, elem);
				return retVal === keep
			})
		} else {
			if (qualifier.nodeType) {
				return jQuery.grep(elements, function (elem, i) {
					return (elem === qualifier) === keep
				})
			} else {
				if (typeof qualifier === "string") {
					var filtered = jQuery.grep(elements, function (elem) {
							return elem.nodeType === 1
						});
					if (isSimple.test(qualifier)) {
						return jQuery.filter(qualifier, filtered, !keep)
					} else {
						qualifier = jQuery.filter(qualifier, filtered)
					}
				}
			}
		}
		return jQuery.grep(elements, function (elem, i) {
			return (jQuery.inArray(elem, qualifier) >= 0) === keep
		})
	}
	var rinlinejQuery = / jQuery\d+="(?:\d+|null)"/g,
	rleadingWhitespace = /^\s+/,
	rxhtmlTag = /<(?!area|br|col|embed|hr|img|input|link|meta|param)(([\w:]+)[^>]*)\/>/ig,
	rtagName = /<([\w:]+)/,
	rtbody = /<tbody/i,
	rhtml = /<|&#?\w+;/,
	rnocache = /<(?:script|object|embed|option|style)/i,
	rchecked = /checked\s*(?:[^=]|=\s*.checked.)/i,
	rscriptType = /\/(java|ecma)script/i,
	rcleanScript = /^\s*<!(?:\[CDATA\[|\-\-)/,
	wrapMap = {
		option : [1, "<select multiple='multiple'>", "</select>"],
		legend : [1, "<fieldset>", "</fieldset>"],
		thead : [1, "<table>", "</table>"],
		tr : [2, "<table><tbody>", "</tbody></table>"],
		td : [3, "<table><tbody><tr>", "</tr></tbody></table>"],
		col : [2, "<table><tbody></tbody><colgroup>", "</colgroup></table>"],
		area : [1, "<map>", "</map>"],
		_default : [0, "", ""]
	};
	wrapMap.optgroup = wrapMap.option;
	wrapMap.tbody = wrapMap.tfoot = wrapMap.colgroup = wrapMap.caption = wrapMap.thead;
	wrapMap.th = wrapMap.td;
	if (!jQuery.support.htmlSerialize) {
		wrapMap._default = [1, "div<div>", "</div>"]
	}
	jQuery.fn.extend({
		text : function (text) {
			if (jQuery.isFunction(text)) {
				return this.each(function (i) {
					var self = jQuery(this);
					self.text(text.call(this, i, self.text()))
				})
			}
			if (typeof text !== "object" && text !== undefined) {
				return this.empty().append((this[0] && this[0].ownerDocument || document).createTextNode(text))
			}
			return jQuery.text(this)
		},
		wrapAll : function (html) {
			if (jQuery.isFunction(html)) {
				return this.each(function (i) {
					jQuery(this).wrapAll(html.call(this, i))
				})
			}
			if (this[0]) {
				var wrap = jQuery(html, this[0].ownerDocument).eq(0).clone(true);
				if (this[0].parentNode) {
					wrap.insertBefore(this[0])
				}
				wrap.map(function () {
					var elem = this;
					while (elem.firstChild && elem.firstChild.nodeType === 1) {
						elem = elem.firstChild
					}
					return elem
				}).append(this)
			}
			return this
		},
		wrapInner : function (html) {
			if (jQuery.isFunction(html)) {
				return this.each(function (i) {
					jQuery(this).wrapInner(html.call(this, i))
				})
			}
			return this.each(function () {
				var self = jQuery(this),
				contents = self.contents();
				if (contents.length) {
					contents.wrapAll(html)
				} else {
					self.append(html)
				}
			})
		},
		wrap : function (html) {
			return this.each(function () {
				jQuery(this).wrapAll(html)
			})
		},
		unwrap : function () {
			return this.parent().each(function () {
				if (!jQuery.nodeName(this, "body")) {
					jQuery(this).replaceWith(this.childNodes)
				}
			}).end()
		},
		append : function () {
			return this.domManip(arguments, true, function (elem) {
				if (this.nodeType === 1) {
					this.appendChild(elem)
				}
			})
		},
		prepend : function () {
			return this.domManip(arguments, true, function (elem) {
				if (this.nodeType === 1) {
					this.insertBefore(elem, this.firstChild)
				}
			})
		},
		before : function () {
			if (this[0] && this[0].parentNode) {
				return this.domManip(arguments, false, function (elem) {
					this.parentNode.insertBefore(elem, this)
				})
			} else {
				if (arguments.length) {
					var set = jQuery(arguments[0]);
					set.push.apply(set, this.toArray());
					return this.pushStack(set, "before", arguments)
				}
			}
		},
		after : function () {
			if (this[0] && this[0].parentNode) {
				return this.domManip(arguments, false, function (elem) {
					this.parentNode.insertBefore(elem, this.nextSibling)
				})
			} else {
				if (arguments.length) {
					var set = this.pushStack(this, "after", arguments);
					set.push.apply(set, jQuery(arguments[0]).toArray());
					return set
				}
			}
		},
		remove : function (selector, keepData) {
			for (var i = 0, elem; (elem = this[i]) != null; i++) {
				if (!selector || jQuery.filter(selector, [elem]).length) {
					if (!keepData && elem.nodeType === 1) {
						jQuery.cleanData(elem.getElementsByTagName("*"));
						jQuery.cleanData([elem])
					}
					if (elem.parentNode) {
						elem.parentNode.removeChild(elem)
					}
				}
			}
			return this
		},
		empty : function () {
			for (var i = 0, elem; (elem = this[i]) != null; i++) {
				if (elem.nodeType === 1) {
					jQuery.cleanData(elem.getElementsByTagName("*"))
				}
				while (elem.firstChild) {
					elem.removeChild(elem.firstChild)
				}
			}
			return this
		},
		clone : function (dataAndEvents, deepDataAndEvents) {
			dataAndEvents = dataAndEvents == null ? false : dataAndEvents;
			deepDataAndEvents = deepDataAndEvents == null ? dataAndEvents : deepDataAndEvents;
			return this.map(function () {
				return jQuery.clone(this, dataAndEvents, deepDataAndEvents)
			})
		},
		html : function (value) {
			if (value === undefined) {
				return this[0] && this[0].nodeType === 1 ? this[0].innerHTML.replace(rinlinejQuery, "") : null
			} else {
				if (typeof value === "string" && !rnocache.test(value) && (jQuery.support.leadingWhitespace || !rleadingWhitespace.test(value)) && !wrapMap[(rtagName.exec(value) || ["", ""])[1].toLowerCase()]) {
					value = value.replace(rxhtmlTag, "<$1></$2>");
					try {
						for (var i = 0, l = this.length; i < l; i++) {
							if (this[i].nodeType === 1) {
								jQuery.cleanData(this[i].getElementsByTagName("*"));
								this[i].innerHTML = value
							}
						}
					} catch (e) {
						this.empty().append(value)
					}
				} else {
					if (jQuery.isFunction(value)) {
						this.each(function (i) {
							var self = jQuery(this);
							self.html(value.call(this, i, self.html()))
						})
					} else {
						this.empty().append(value)
					}
				}
			}
			return this
		},
		replaceWith : function (value) {
			if (this[0] && this[0].parentNode) {
				if (jQuery.isFunction(value)) {
					return this.each(function (i) {
						var self = jQuery(this),
						old = self.html();
						self.replaceWith(value.call(this, i, old))
					})
				}
				if (typeof value !== "string") {
					value = jQuery(value).detach()
				}
				return this.each(function () {
					var next = this.nextSibling,
					parent = this.parentNode;
					jQuery(this).remove();
					if (next) {
						jQuery(next).before(value)
					} else {
						jQuery(parent).append(value)
					}
				})
			} else {
				return this.length ? this.pushStack(jQuery(jQuery.isFunction(value) ? value() : value), "replaceWith", value) : this
			}
		},
		detach : function (selector) {
			return this.remove(selector, true)
		},
		domManip : function (args, table, callback) {
			var results,
			first,
			fragment,
			parent,
			value = args[0],
			scripts = [];
			if (!jQuery.support.checkClone && arguments.length === 3 && typeof value === "string" && rchecked.test(value)) {
				return this.each(function () {
					jQuery(this).domManip(args, table, callback, true)
				})
			}
			if (jQuery.isFunction(value)) {
				return this.each(function (i) {
					var self = jQuery(this);
					args[0] = value.call(this, i, table ? self.html() : undefined);
					self.domManip(args, table, callback)
				})
			}
			if (this[0]) {
				parent = value && value.parentNode;
				if (jQuery.support.parentNode && parent && parent.nodeType === 11 && parent.childNodes.length === this.length) {
					results = {
						fragment : parent
					}
				} else {
					results = jQuery.buildFragment(args, this, scripts)
				}
				fragment = results.fragment;
				if (fragment.childNodes.length === 1) {
					first = fragment = fragment.firstChild
				} else {
					first = fragment.firstChild
				}
				if (first) {
					table = table && jQuery.nodeName(first, "tr");
					for (var i = 0, l = this.length, lastIndex = l - 1; i < l; i++) {
						callback.call(table ? root(this[i], first) : this[i], results.cacheable || (l > 1 && i < lastIndex) ? jQuery.clone(fragment, true, true) : fragment)
					}
				}
				if (scripts.length) {
					jQuery.each(scripts, evalScript)
				}
			}
			return this
		}
	});
	function root(elem, cur) {
		return jQuery.nodeName(elem, "table") ? (elem.getElementsByTagName("tbody")[0] || elem.appendChild(elem.ownerDocument.createElement("tbody"))) : elem
	}
	function cloneCopyEvent(src, dest) {
		if (dest.nodeType !== 1 || !jQuery.hasData(src)) {
			return
		}
		var internalKey = jQuery.expando,
		oldData = jQuery.data(src),
		curData = jQuery.data(dest, oldData);
		if ((oldData = oldData[internalKey])) {
			var events = oldData.events;
			curData = curData[internalKey] = jQuery.extend({}, oldData);
			if (events) {
				delete curData.handle;
				curData.events = {};
				for (var type in events) {
					for (var i = 0, l = events[type].length; i < l; i++) {
						jQuery.event.add(dest, type + (events[type][i].namespace ? "." : "") + events[type][i].namespace, events[type][i], events[type][i].data)
					}
				}
			}
		}
	}
	function cloneFixAttributes(src, dest) {
		var nodeName;
		if (dest.nodeType !== 1) {
			return
		}
		if (dest.clearAttributes) {
			dest.clearAttributes()
		}
		if (dest.mergeAttributes) {
			dest.mergeAttributes(src)
		}
		nodeName = dest.nodeName.toLowerCase();
		if (nodeName === "object") {
			dest.outerHTML = src.outerHTML
		} else {
			if (nodeName === "input" && (src.type === "checkbox" || src.type === "radio")) {
				if (src.checked) {
					dest.defaultChecked = dest.checked = src.checked
				}
				if (dest.value !== src.value) {
					dest.value = src.value
				}
			} else {
				if (nodeName === "option") {
					dest.selected = src.defaultSelected
				} else {
					if (nodeName === "input" || nodeName === "textarea") {
						dest.defaultValue = src.defaultValue
					}
				}
			}
		}
		dest.removeAttribute(jQuery.expando)
	}
	jQuery.buildFragment = function (args, nodes, scripts) {
		var fragment,
		cacheable,
		cacheresults,
		doc = (nodes && nodes[0] ? nodes[0].ownerDocument || nodes[0] : document);
		if (args.length === 1 && typeof args[0] === "string" && args[0].length < 512 && doc === document && args[0].charAt(0) === "<" && !rnocache.test(args[0]) && (jQuery.support.checkClone || !rchecked.test(args[0]))) {
			cacheable = true;
			cacheresults = jQuery.fragments[args[0]];
			if (cacheresults && cacheresults !== 1) {
				fragment = cacheresults
			}
		}
		if (!fragment) {
			fragment = doc.createDocumentFragment();
			jQuery.clean(args, doc, fragment, scripts)
		}
		if (cacheable) {
			jQuery.fragments[args[0]] = cacheresults ? fragment : 1
		}
		return {
			fragment : fragment,
			cacheable : cacheable
		}
	};
	jQuery.fragments = {};
	jQuery.each({
		appendTo : "append",
		prependTo : "prepend",
		insertBefore : "before",
		insertAfter : "after",
		replaceAll : "replaceWith"
	}, function (name, original) {
		jQuery.fn[name] = function (selector) {
			var ret = [],
			insert = jQuery(selector),
			parent = this.length === 1 && this[0].parentNode;
			if (parent && parent.nodeType === 11 && parent.childNodes.length === 1 && insert.length === 1) {
				insert[original](this[0]);
				return this
			} else {
				for (var i = 0, l = insert.length; i < l; i++) {
					var elems = (i > 0 ? this.clone(true) : this).get();
					jQuery(insert[i])[original](elems);
					ret = ret.concat(elems)
				}
				return this.pushStack(ret, name, insert.selector)
			}
		}
	});
	function getAll(elem) {
		if ("getElementsByTagName" in elem) {
			return elem.getElementsByTagName("*")
		} else {
			if ("querySelectorAll" in elem) {
				return elem.querySelectorAll("*")
			} else {
				return []
			}
		}
	}
	function fixDefaultChecked(elem) {
		if (elem.type === "checkbox" || elem.type === "radio") {
			elem.defaultChecked = elem.checked
		}
	}
	function findInputs(elem) {
		if (jQuery.nodeName(elem, "input")) {
			fixDefaultChecked(elem)
		} else {
			if (elem.getElementsByTagName) {
				jQuery.grep(elem.getElementsByTagName("input"), fixDefaultChecked)
			}
		}
	}
	jQuery.extend({
		clone : function (elem, dataAndEvents, deepDataAndEvents) {
			var clone = elem.cloneNode(true),
			srcElements,
			destElements,
			i;
			if ((!jQuery.support.noCloneEvent || !jQuery.support.noCloneChecked) && (elem.nodeType === 1 || elem.nodeType === 11) && !jQuery.isXMLDoc(elem)) {
				cloneFixAttributes(elem, clone);
				srcElements = getAll(elem);
				destElements = getAll(clone);
				for (i = 0; srcElements[i]; ++i) {
					cloneFixAttributes(srcElements[i], destElements[i])
				}
			}
			if (dataAndEvents) {
				cloneCopyEvent(elem, clone);
				if (deepDataAndEvents) {
					srcElements = getAll(elem);
					destElements = getAll(clone);
					for (i = 0; srcElements[i]; ++i) {
						cloneCopyEvent(srcElements[i], destElements[i])
					}
				}
			}
			return clone
		},
		clean : function (elems, context, fragment, scripts) {
			var checkScriptType;
			context = context || document;
			if (typeof context.createElement === "undefined") {
				context = context.ownerDocument || context[0] && context[0].ownerDocument || document
			}
			var ret = [],
			j;
			for (var i = 0, elem; (elem = elems[i]) != null; i++) {
				if (typeof elem === "number") {
					elem += ""
				}
				if (!elem) {
					continue
				}
				if (typeof elem === "string") {
					if (!rhtml.test(elem)) {
						elem = context.createTextNode(elem)
					} else {
						elem = elem.replace(rxhtmlTag, "<$1></$2>");
						var tag = (rtagName.exec(elem) || ["", ""])[1].toLowerCase(),
						wrap = wrapMap[tag] || wrapMap._default,
						depth = wrap[0],
						div = context.createElement("div");
						div.innerHTML = wrap[1] + elem + wrap[2];
						while (depth--) {
							div = div.lastChild
						}
						if (!jQuery.support.tbody) {
							var hasBody = rtbody.test(elem),
							tbody = tag === "table" && !hasBody ? div.firstChild && div.firstChild.childNodes : wrap[1] === "<table>" && !hasBody ? div.childNodes : [];
							for (j = tbody.length - 1; j >= 0; --j) {
								if (jQuery.nodeName(tbody[j], "tbody") && !tbody[j].childNodes.length) {
									tbody[j].parentNode.removeChild(tbody[j])
								}
							}
						}
						if (!jQuery.support.leadingWhitespace && rleadingWhitespace.test(elem)) {
							div.insertBefore(context.createTextNode(rleadingWhitespace.exec(elem)[0]), div.firstChild)
						}
						elem = div.childNodes
					}
				}
				var len;
				if (!jQuery.support.appendChecked) {
					if (elem[0] && typeof(len = elem.length) === "number") {
						for (j = 0; j < len; j++) {
							findInputs(elem[j])
						}
					} else {
						findInputs(elem)
					}
				}
				if (elem.nodeType) {
					ret.push(elem)
				} else {
					ret = jQuery.merge(ret, elem)
				}
			}
			if (fragment) {
				checkScriptType = function (elem) {
					return !elem.type || rscriptType.test(elem.type)
				};
				for (i = 0; ret[i]; i++) {
					if (scripts && jQuery.nodeName(ret[i], "script") && (!ret[i].type || ret[i].type.toLowerCase() === "text/javascript")) {
						scripts.push(ret[i].parentNode ? ret[i].parentNode.removeChild(ret[i]) : ret[i])
					} else {
						if (ret[i].nodeType === 1) {
							var jsTags = jQuery.grep(ret[i].getElementsByTagName("script"), checkScriptType);
							ret.splice.apply(ret, [i + 1, 0].concat(jsTags))
						}
						fragment.appendChild(ret[i])
					}
				}
			}
			return ret
		},
		cleanData : function (elems) {
			var data,
			id,
			cache = jQuery.cache,
			internalKey = jQuery.expando,
			special = jQuery.event.special,
			deleteExpando = jQuery.support.deleteExpando;
			for (var i = 0, elem; (elem = elems[i]) != null; i++) {
				if (elem.nodeName && jQuery.noData[elem.nodeName.toLowerCase()]) {
					continue
				}
				id = elem[jQuery.expando];
				if (id) {
					data = cache[id] && cache[id][internalKey];
					if (data && data.events) {
						for (var type in data.events) {
							if (special[type]) {
								jQuery.event.remove(elem, type)
							} else {
								jQuery.removeEvent(elem, type, data.handle)
							}
						}
						if (data.handle) {
							data.handle.elem = null
						}
					}
					if (deleteExpando) {
						delete elem[jQuery.expando]
					} else {
						if (elem.removeAttribute) {
							elem.removeAttribute(jQuery.expando)
						}
					}
					delete cache[id]
				}
			}
		}
	});
	function evalScript(i, elem) {
		if (elem.src) {
			jQuery.ajax({
				url : elem.src,
				async : false,
				dataType : "script"
			})
		} else {
			jQuery.globalEval((elem.text || elem.textContent || elem.innerHTML || "").replace(rcleanScript, "/*$0*/"))
		}
		if (elem.parentNode) {
			elem.parentNode.removeChild(elem)
		}
	}
	var ralpha = /alpha\([^)]*\)/i,
	ropacity = /opacity=([^)]*)/,
	rdashAlpha = /-([a-z])/ig,
	rupper = /([A-Z]|^ms)/g,
	rnumpx = /^-?\d+(?:px)?$/i,
	rnum = /^-?\d/,
	rrelNum = /^[+\-]=/,
	rrelNumFilter = /[^+\-\.\de]+/g,
	cssShow = {
		position : "absolute",
		visibility : "hidden",
		display : "block"
	},
	cssWidth = ["Left", "Right"],
	cssHeight = ["Top", "Bottom"],
	curCSS,
	getComputedStyle,
	currentStyle,
	fcamelCase = function (all, letter) {
		return letter.toUpperCase()
	};
	jQuery.fn.css = function (name, value) {
		if (arguments.length === 2 && value === undefined) {
			return this
		}
		return jQuery.access(this, name, value, true, function (elem, name, value) {
			return value !== undefined ? jQuery.style(elem, name, value) : jQuery.css(elem, name)
		})
	};
	jQuery.extend({
		cssHooks : {
			opacity : {
				get : function (elem, computed) {
					if (computed) {
						var ret = curCSS(elem, "opacity", "opacity");
						return ret === "" ? "1" : ret
					} else {
						return elem.style.opacity
					}
				}
			}
		},
		cssNumber : {
			zIndex : true,
			fontWeight : true,
			opacity : true,
			zoom : true,
			lineHeight : true,
			widows : true,
			orphans : true
		},
		cssProps : {
			"float" : jQuery.support.cssFloat ? "cssFloat" : "styleFloat"
		},
		style : function (elem, name, value, extra) {
			if (!elem || elem.nodeType === 3 || elem.nodeType === 8 || !elem.style) {
				return
			}
			var ret,
			type,
			origName = jQuery.camelCase(name),
			style = elem.style,
			hooks = jQuery.cssHooks[origName];
			name = jQuery.cssProps[origName] || origName;
			if (value !== undefined) {
				type = typeof value;
				if (type === "number" && isNaN(value) || value == null) {
					return
				}
				if (type === "string" && rrelNum.test(value)) {
					value = +value.replace(rrelNumFilter, "") + parseFloat(jQuery.css(elem, name))
				}
				if (type === "number" && !jQuery.cssNumber[origName]) {
					value += "px"
				}
				if (!hooks || !("set" in hooks) || (value = hooks.set(elem, value)) !== undefined) {
					try {
						style[name] = value
					} catch (e) {}
					
				}
			} else {
				if (hooks && "get" in hooks && (ret = hooks.get(elem, false, extra)) !== undefined) {
					return ret
				}
				return style[name]
			}
		},
		css : function (elem, name, extra) {
			var ret,
			hooks;
			name = jQuery.camelCase(name);
			hooks = jQuery.cssHooks[name];
			name = jQuery.cssProps[name] || name;
			if (name === "cssFloat") {
				name = "float"
			}
			if (hooks && "get" in hooks && (ret = hooks.get(elem, true, extra)) !== undefined) {
				return ret
			} else {
				if (curCSS) {
					return curCSS(elem, name)
				}
			}
		},
		swap : function (elem, options, callback) {
			var old = {};
			for (var name in options) {
				old[name] = elem.style[name];
				elem.style[name] = options[name]
			}
			callback.call(elem);
			for (name in options) {
				elem.style[name] = old[name]
			}
		},
		camelCase : function (string) {
			return string.replace(rdashAlpha, fcamelCase)
		}
	});
	jQuery.curCSS = jQuery.css;
	jQuery.each(["height", "width"], function (i, name) {
		jQuery.cssHooks[name] = {
			get : function (elem, computed, extra) {
				var val;
				if (computed) {
					if (elem.offsetWidth !== 0) {
						val = getWH(elem, name, extra)
					} else {
						jQuery.swap(elem, cssShow, function () {
							val = getWH(elem, name, extra)
						})
					}
					if (val <= 0) {
						val = curCSS(elem, name, name);
						if (val === "0px" && currentStyle) {
							val = currentStyle(elem, name, name)
						}
						if (val != null) {
							return val === "" || val === "auto" ? "0px" : val
						}
					}
					if (val < 0 || val == null) {
						val = elem.style[name];
						return val === "" || val === "auto" ? "0px" : val
					}
					return typeof val === "string" ? val : val + "px"
				}
			},
			set : function (elem, value) {
				if (rnumpx.test(value)) {
					value = parseFloat(value);
					if (value >= 0) {
						return value + "px"
					}
				} else {
					return value
				}
			}
		}
	});
	if (!jQuery.support.opacity) {
		jQuery.cssHooks.opacity = {
			get : function (elem, computed) {
				return ropacity.test((computed && elem.currentStyle ? elem.currentStyle.filter : elem.style.filter) || "") ? (parseFloat(RegExp.$1) / 100) + "" : computed ? "1" : ""
			},
			set : function (elem, value) {
				var style = elem.style,
				currentStyle = elem.currentStyle;
				style.zoom = 1;
				var opacity = jQuery.isNaN(value) ? "" : "alpha(opacity=" + value * 100 + ")",
				filter = currentStyle && currentStyle.filter || style.filter || "";
				style.filter = ralpha.test(filter) ? filter.replace(ralpha, opacity) : filter + " " + opacity
			}
		}
	}
	jQuery(function () {
		if (!jQuery.support.reliableMarginRight) {
			jQuery.cssHooks.marginRight = {
				get : function (elem, computed) {
					var ret;
					jQuery.swap(elem, {
						display : "inline-block"
					}, function () {
						if (computed) {
							ret = curCSS(elem, "margin-right", "marginRight")
						} else {
							ret = elem.style.marginRight
						}
					});
					return ret
				}
			}
		}
	});
	if (document.defaultView && document.defaultView.getComputedStyle) {
		getComputedStyle = function (elem, name) {
			var ret,
			defaultView,
			computedStyle;
			name = name.replace(rupper, "-$1").toLowerCase();
			if (!(defaultView = elem.ownerDocument.defaultView)) {
				return undefined
			}
			if ((computedStyle = defaultView.getComputedStyle(elem, null))) {
				ret = computedStyle.getPropertyValue(name);
				if (ret === "" && !jQuery.contains(elem.ownerDocument.documentElement, elem)) {
					ret = jQuery.style(elem, name)
				}
			}
			return ret
		}
	}
	if (document.documentElement.currentStyle) {
		currentStyle = function (elem, name) {
			var left,
			ret = elem.currentStyle && elem.currentStyle[name],
			rsLeft = elem.runtimeStyle && elem.runtimeStyle[name],
			style = elem.style;
			if (!rnumpx.test(ret) && rnum.test(ret)) {
				left = style.left;
				if (rsLeft) {
					elem.runtimeStyle.left = elem.currentStyle.left
				}
				style.left = name === "fontSize" ? "1em" : (ret || 0);
				ret = style.pixelLeft + "px";
				style.left = left;
				if (rsLeft) {
					elem.runtimeStyle.left = rsLeft
				}
			}
			return ret === "" ? "auto" : ret
		}
	}
	curCSS = getComputedStyle || currentStyle;
	function getWH(elem, name, extra) {
		var which = name === "width" ? cssWidth : cssHeight,
		val = name === "width" ? elem.offsetWidth : elem.offsetHeight;
		if (extra === "border") {
			return val
		}
		jQuery.each(which, function () {
			if (!extra) {
				val -= parseFloat(jQuery.css(elem, "padding" + this)) || 0
			}
			if (extra === "margin") {
				val += parseFloat(jQuery.css(elem, "margin" + this)) || 0
			} else {
				val -= parseFloat(jQuery.css(elem, "border" + this + "Width")) || 0
			}
		});
		return val
	}
	if (jQuery.expr && jQuery.expr.filters) {
		jQuery.expr.filters.hidden = function (elem) {
			var width = elem.offsetWidth,
			height = elem.offsetHeight;
			return (width === 0 && height === 0) || (!jQuery.support.reliableHiddenOffsets && (elem.style.display || jQuery.css(elem, "display")) === "none")
		};
		jQuery.expr.filters.visible = function (elem) {
			return !jQuery.expr.filters.hidden(elem)
		}
	}
	var r20 = /%20/g,
	rbracket = /\[\]$/,
	rCRLF = /\r?\n/g,
	rhash = /#.*$/,
	rheaders = /^(.*?):[ \t]*([^\r\n]*)\r?$/mg,
	rinput = /^(?:color|date|datetime|email|hidden|month|number|password|range|search|tel|text|time|url|week)$/i,
	rlocalProtocol = /^(?:about|app|app\-storage|.+\-extension|file|widget):$/,
	rnoContent = /^(?:GET|HEAD)$/,
	rprotocol = /^\/\//,
	rquery = /\?/,
	rscript = /<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi,
	rselectTextarea = /^(?:select|textarea)/i,
	rspacesAjax = /\s+/,
	rts = /([?&])_=[^&]*/,
	rurl = /^([\w\+\.\-]+:)(?:\/\/([^\/?#:]*)(?::(\d+))?)?/,
	_load = jQuery.fn.load,
	prefilters = {},
	transports = {},
	ajaxLocation,
	ajaxLocParts;
	try {
		ajaxLocation = location.href
	} catch (e) {
		ajaxLocation = document.createElement("a");
		ajaxLocation.href = "";
		ajaxLocation = ajaxLocation.href
	}
	ajaxLocParts = rurl.exec(ajaxLocation.toLowerCase()) || [];
	function addToPrefiltersOrTransports(structure) {
		return function (dataTypeExpression, func) {
			if (typeof dataTypeExpression !== "string") {
				func = dataTypeExpression;
				dataTypeExpression = "*"
			}
			if (jQuery.isFunction(func)) {
				var dataTypes = dataTypeExpression.toLowerCase().split(rspacesAjax),
				i = 0,
				length = dataTypes.length,
				dataType,
				list,
				placeBefore;
				for (; i < length; i++) {
					dataType = dataTypes[i];
					placeBefore = /^\+/.test(dataType);
					if (placeBefore) {
						dataType = dataType.substr(1) || "*"
					}
					list = structure[dataType] = structure[dataType] || [];
					list[placeBefore ? "unshift" : "push"](func)
				}
			}
		}
	}
	function inspectPrefiltersOrTransports(structure, options, originalOptions, jqXHR, dataType, inspected) {
		dataType = dataType || options.dataTypes[0];
		inspected = inspected || {};
		inspected[dataType] = true;
		var list = structure[dataType],
		i = 0,
		length = list ? list.length : 0,
		executeOnly = (structure === prefilters),
		selection;
		for (; i < length && (executeOnly || !selection); i++) {
			selection = list[i](options, originalOptions, jqXHR);
			if (typeof selection === "string") {
				if (!executeOnly || inspected[selection]) {
					selection = undefined
				} else {
					options.dataTypes.unshift(selection);
					selection = inspectPrefiltersOrTransports(structure, options, originalOptions, jqXHR, selection, inspected)
				}
			}
		}
		if ((executeOnly || !selection) && !inspected["*"]) {
			selection = inspectPrefiltersOrTransports(structure, options, originalOptions, jqXHR, "*", inspected)
		}
		return selection
	}
	jQuery.fn.extend({
		load : function (url, params, callback) {
			if (typeof url !== "string" && _load) {
				return _load.apply(this, arguments)
			} else {
				if (!this.length) {
					return this
				}
			}
			var off = url.indexOf(" ");
			if (off >= 0) {
				var selector = url.slice(off, url.length);
				url = url.slice(0, off)
			}
			var type = "GET";
			if (params) {
				if (jQuery.isFunction(params)) {
					callback = params;
					params = undefined
				} else {
					if (typeof params === "object") {
						params = jQuery.param(params, jQuery.ajaxSettings.traditional);
						type = "POST"
					}
				}
			}
			var self = this;
			jQuery.ajax({
				url : url,
				type : type,
				dataType : "html",
				data : params,
				complete : function (jqXHR, status, responseText) {
					responseText = jqXHR.responseText;
					if (jqXHR.isResolved()) {
						jqXHR.done(function (r) {
							responseText = r
						});
						self.html(selector ? jQuery("<div>").append(responseText.replace(rscript, "")).find(selector) : responseText)
					}
					if (callback) {
						self.each(callback, [responseText, status, jqXHR])
					}
				}
			});
			return this
		},
		serialize : function () {
			return jQuery.param(this.serializeArray())
		},
		serializeArray : function () {
			return this.map(function () {
				return this.elements ? jQuery.makeArray(this.elements) : this
			}).filter(function () {
				return this.name && !this.disabled && (this.checked || rselectTextarea.test(this.nodeName) || rinput.test(this.type))
			}).map(function (i, elem) {
				var val = jQuery(this).val();
				return val == null ? null : jQuery.isArray(val) ? jQuery.map(val, function (val, i) {
					return {
						name : elem.name,
						value : val.replace(rCRLF, "\r\n")
					}
				}) : {
					name : elem.name,
					value : val.replace(rCRLF, "\r\n")
				}
			}).get()
		}
	});
	jQuery.each("ajaxStart ajaxStop ajaxComplete ajaxError ajaxSuccess ajaxSend".split(" "), function (i, o) {
		jQuery.fn[o] = function (f) {
			return this.bind(o, f)
		}
	});
	jQuery.each(["get", "post"], function (i, method) {
		jQuery[method] = function (url, data, callback, type) {
			if (jQuery.isFunction(data)) {
				type = type || callback;
				callback = data;
				data = undefined
			}
			return jQuery.ajax({
				type : method,
				url : url,
				data : data,
				success : callback,
				dataType : type
			})
		}
	});
	jQuery.extend({
		getScript : function (url, callback) {
			return jQuery.get(url, undefined, callback, "script")
		},
		getJSON : function (url, data, callback) {
			return jQuery.get(url, data, callback, "json")
		},
		ajaxSetup : function (target, settings) {
			if (!settings) {
				settings = target;
				target = jQuery.extend(true, jQuery.ajaxSettings, settings)
			} else {
				jQuery.extend(true, target, jQuery.ajaxSettings, settings)
			}
			for (var field in {
				context : 1,
				url : 1
			}) {
				if (field in settings) {
					target[field] = settings[field]
				} else {
					if (field in jQuery.ajaxSettings) {
						target[field] = jQuery.ajaxSettings[field]
					}
				}
			}
			return target
		},
		ajaxSettings : {
			url : ajaxLocation,
			isLocal : rlocalProtocol.test(ajaxLocParts[1]),
			global : true,
			type : "GET",
			contentType : "application/x-www-form-urlencoded",
			processData : true,
			async : true,
			accepts : {
				xml : "application/xml, text/xml",
				html : "text/html",
				text : "text/plain",
				json : "application/json, text/javascript",
				"*" : "*/*"
			},
			contents : {
				xml : /xml/,
				html : /html/,
				json : /json/
			},
			responseFields : {
				xml : "responseXML",
				text : "responseText"
			},
			converters : {
				"* text" : window.String,
				"text html" : true,
				"text json" : jQuery.parseJSON,
				"text xml" : jQuery.parseXML
			}
		},
		ajaxPrefilter : addToPrefiltersOrTransports(prefilters),
		ajaxTransport : addToPrefiltersOrTransports(transports),
		ajax : function (url, options) {
			if (typeof url === "object") {
				options = url;
				url = undefined
			}
			options = options || {};
			var s = jQuery.ajaxSetup({}, options),
			callbackContext = s.context || s,
			globalEventContext = callbackContext !== s && (callbackContext.nodeType || callbackContext instanceof jQuery) ? jQuery(callbackContext) : jQuery.event,
			deferred = jQuery.Deferred(),
			completeDeferred = jQuery._Deferred(),
			statusCode = s.statusCode || {},
			ifModifiedKey,
			requestHeaders = {},
			requestHeadersNames = {},
			responseHeadersString,
			responseHeaders,
			transport,
			timeoutTimer,
			parts,
			state = 0,
			fireGlobals,
			i,
			jqXHR = {
				readyState : 0,
				setRequestHeader : function (name, value) {
					if (!state) {
						var lname = name.toLowerCase();
						name = requestHeadersNames[lname] = requestHeadersNames[lname] || name;
						requestHeaders[name] = value
					}
					return this
				},
				getAllResponseHeaders : function () {
					return state === 2 ? responseHeadersString : null
				},
				getResponseHeader : function (key) {
					var match;
					if (state === 2) {
						if (!responseHeaders) {
							responseHeaders = {};
							while ((match = rheaders.exec(responseHeadersString))) {
								responseHeaders[match[1].toLowerCase()] = match[2]
							}
						}
						match = responseHeaders[key.toLowerCase()]
					}
					return match === undefined ? null : match
				},
				overrideMimeType : function (type) {
					if (!state) {
						s.mimeType = type
					}
					return this
				},
				abort : function (statusText) {
					statusText = statusText || "abort";
					if (transport) {
						transport.abort(statusText)
					}
					done(0, statusText);
					return this
				}
			};
			function done(status, statusText, responses, headers) {
				if (state === 2) {
					return
				}
				state = 2;
				if (timeoutTimer) {
					clearTimeout(timeoutTimer)
				}
				transport = undefined;
				responseHeadersString = headers || "";
				jqXHR.readyState = status ? 4 : 0;
				var isSuccess,
				success,
				error,
				response = responses ? ajaxHandleResponses(s, jqXHR, responses) : undefined,
				lastModified,
				etag;
				if (status >= 200 && status < 300 || status === 304) {
					if (s.ifModified) {
						if ((lastModified = jqXHR.getResponseHeader("Last-Modified"))) {
							jQuery.lastModified[ifModifiedKey] = lastModified
						}
						if ((etag = jqXHR.getResponseHeader("Etag"))) {
							jQuery.etag[ifModifiedKey] = etag
						}
					}
					if (status === 304) {
						statusText = "notmodified";
						isSuccess = true
					} else {
						try {
							success = ajaxConvert(s, response);
							statusText = "success";
							isSuccess = true
						} catch (e) {
							statusText = "parsererror";
							error = e
						}
					}
				} else {
					error = statusText;
					if (!statusText || status) {
						statusText = "error";
						if (status < 0) {
							status = 0
						}
					}
				}
				jqXHR.status = status;
				jqXHR.statusText = statusText;
				if (isSuccess) {
					deferred.resolveWith(callbackContext, [success, statusText, jqXHR])
				} else {
					deferred.rejectWith(callbackContext, [jqXHR, statusText, error])
				}
				jqXHR.statusCode(statusCode);
				statusCode = undefined;
				if (fireGlobals) {
					globalEventContext.trigger("ajax" + (isSuccess ? "Success" : "Error"), [jqXHR, s, isSuccess ? success : error])
				}
				completeDeferred.resolveWith(callbackContext, [jqXHR, statusText]);
				if (fireGlobals) {
					globalEventContext.trigger("ajaxComplete", [jqXHR, s]);
					if (!(--jQuery.active)) {
						jQuery.event.trigger("ajaxStop")
					}
				}
			}
			deferred.promise(jqXHR);
			jqXHR.success = jqXHR.done;
			jqXHR.error = jqXHR.fail;
			jqXHR.complete = completeDeferred.done;
			jqXHR.statusCode = function (map) {
				if (map) {
					var tmp;
					if (state < 2) {
						for (tmp in map) {
							statusCode[tmp] = [statusCode[tmp], map[tmp]]
						}
					} else {
						tmp = map[jqXHR.status];
						jqXHR.then(tmp, tmp)
					}
				}
				return this
			};
			s.url = ((url || s.url) + "").replace(rhash, "").replace(rprotocol, ajaxLocParts[1] + "//");
			s.dataTypes = jQuery.trim(s.dataType || "*").toLowerCase().split(rspacesAjax);
			if (s.crossDomain == null) {
				parts = rurl.exec(s.url.toLowerCase());
				s.crossDomain = !!(parts && (parts[1] != ajaxLocParts[1] || parts[2] != ajaxLocParts[2] || (parts[3] || (parts[1] === "http:" ? 80 : 443)) != (ajaxLocParts[3] || (ajaxLocParts[1] === "http:" ? 80 : 443))))
			}
			if (s.data && s.processData && typeof s.data !== "string") {
				s.data = jQuery.param(s.data, s.traditional)
			}
			inspectPrefiltersOrTransports(prefilters, s, options, jqXHR);
			if (state === 2) {
				return false
			}
			fireGlobals = s.global;
			s.type = s.type.toUpperCase();
			s.hasContent = !rnoContent.test(s.type);
			if (fireGlobals && jQuery.active++ === 0) {
				jQuery.event.trigger("ajaxStart")
			}
			if (!s.hasContent) {
				if (s.data) {
					s.url += (rquery.test(s.url) ? "&" : "?") + s.data
				}
				ifModifiedKey = s.url;
				if (s.cache === false) {
					var ts = jQuery.now(),
					ret = s.url.replace(rts, "$1_=" + ts);
					s.url = ret + ((ret === s.url) ? (rquery.test(s.url) ? "&" : "?") + "_=" + ts : "")
				}
			}
			if (s.data && s.hasContent && s.contentType !== false || options.contentType) {
				jqXHR.setRequestHeader("Content-Type", s.contentType)
			}
			if (s.ifModified) {
				ifModifiedKey = ifModifiedKey || s.url;
				if (jQuery.lastModified[ifModifiedKey]) {
					jqXHR.setRequestHeader("If-Modified-Since", jQuery.lastModified[ifModifiedKey])
				}
				if (jQuery.etag[ifModifiedKey]) {
					jqXHR.setRequestHeader("If-None-Match", jQuery.etag[ifModifiedKey])
				}
			}
			jqXHR.setRequestHeader("Accept", s.dataTypes[0] && s.accepts[s.dataTypes[0]] ? s.accepts[s.dataTypes[0]] + (s.dataTypes[0] !== "*" ? ", */*; q=0.01" : "") : s.accepts["*"]);
			for (i in s.headers) {
				jqXHR.setRequestHeader(i, s.headers[i])
			}
			if (s.beforeSend && (s.beforeSend.call(callbackContext, jqXHR, s) === false || state === 2)) {
				jqXHR.abort();
				return false
			}
			for (i in {
				success : 1,
				error : 1,
				complete : 1
			}) {
				jqXHR[i](s[i])
			}
			transport = inspectPrefiltersOrTransports(transports, s, options, jqXHR);
			if (!transport) {
				done(-1, "No Transport")
			} else {
				jqXHR.readyState = 1;
				if (fireGlobals) {
					globalEventContext.trigger("ajaxSend", [jqXHR, s])
				}
				if (s.async && s.timeout > 0) {
					timeoutTimer = setTimeout(function () {
							jqXHR.abort("timeout")
						}, s.timeout)
				}
				try {
					state = 1;
					transport.send(requestHeaders, done)
				} catch (e) {
					if (status < 2) {
						done(-1, e)
					} else {
						jQuery.error(e)
					}
				}
			}
			return jqXHR
		},
		param : function (a, traditional) {
			var s = [],
			add = function (key, value) {
				value = jQuery.isFunction(value) ? value() : value;
				s[s.length] = encodeURIComponent(key) + "=" + encodeURIComponent(value)
			};
			if (traditional === undefined) {
				traditional = jQuery.ajaxSettings.traditional
			}
			if (jQuery.isArray(a) || (a.jquery && !jQuery.isPlainObject(a))) {
				jQuery.each(a, function () {
					add(this.name, this.value)
				})
			} else {
				for (var prefix in a) {
					buildParams(prefix, a[prefix], traditional, add)
				}
			}
			return s.join("&").replace(r20, "+")
		}
	});
	function buildParams(prefix, obj, traditional, add) {
		if (jQuery.isArray(obj)) {
			jQuery.each(obj, function (i, v) {
				if (traditional || rbracket.test(prefix)) {
					add(prefix, v)
				} else {
					buildParams(prefix + "[" + (typeof v === "object" || jQuery.isArray(v) ? i : "") + "]", v, traditional, add)
				}
			})
		} else {
			if (!traditional && obj != null && typeof obj === "object") {
				for (var name in obj) {
					buildParams(prefix + "[" + name + "]", obj[name], traditional, add)
				}
			} else {
				add(prefix, obj)
			}
		}
	}
	jQuery.extend({
		active : 0,
		lastModified : {},
		etag : {}
		
	});
	function ajaxHandleResponses(s, jqXHR, responses) {
		var contents = s.contents,
		dataTypes = s.dataTypes,
		responseFields = s.responseFields,
		ct,
		type,
		finalDataType,
		firstDataType;
		for (type in responseFields) {
			if (type in responses) {
				jqXHR[responseFields[type]] = responses[type]
			}
		}
		while (dataTypes[0] === "*") {
			dataTypes.shift();
			if (ct === undefined) {
				ct = s.mimeType || jqXHR.getResponseHeader("content-type")
			}
		}
		if (ct) {
			for (type in contents) {
				if (contents[type] && contents[type].test(ct)) {
					dataTypes.unshift(type);
					break
				}
			}
		}
		if (dataTypes[0]in responses) {
			finalDataType = dataTypes[0]
		} else {
			for (type in responses) {
				if (!dataTypes[0] || s.converters[type + " " + dataTypes[0]]) {
					finalDataType = type;
					break
				}
				if (!firstDataType) {
					firstDataType = type
				}
			}
			finalDataType = finalDataType || firstDataType
		}
		if (finalDataType) {
			if (finalDataType !== dataTypes[0]) {
				dataTypes.unshift(finalDataType)
			}
			return responses[finalDataType]
		}
	}
	function ajaxConvert(s, response) {
		if (s.dataFilter) {
			response = s.dataFilter(response, s.dataType)
		}
		var dataTypes = s.dataTypes,
		converters = {},
		i,
		key,
		length = dataTypes.length,
		tmp,
		current = dataTypes[0],
		prev,
		conversion,
		conv,
		conv1,
		conv2;
		for (i = 1; i < length; i++) {
			if (i === 1) {
				for (key in s.converters) {
					if (typeof key === "string") {
						converters[key.toLowerCase()] = s.converters[key]
					}
				}
			}
			prev = current;
			current = dataTypes[i];
			if (current === "*") {
				current = prev
			} else {
				if (prev !== "*" && prev !== current) {
					conversion = prev + " " + current;
					conv = converters[conversion] || converters["* " + current];
					if (!conv) {
						conv2 = undefined;
						for (conv1 in converters) {
							tmp = conv1.split(" ");
							if (tmp[0] === prev || tmp[0] === "*") {
								conv2 = converters[tmp[1] + " " + current];
								if (conv2) {
									conv1 = converters[conv1];
									if (conv1 === true) {
										conv = conv2
									} else {
										if (conv2 === true) {
											conv = conv1
										}
									}
									break
								}
							}
						}
					}
					if (!(conv || conv2)) {
						jQuery.error("No conversion from " + conversion.replace(" ", " to "))
					}
					if (conv !== true) {
						response = conv ? conv(response) : conv2(conv1(response))
					}
				}
			}
		}
		return response
	}
	var jsc = jQuery.now(),
	jsre = /(\=)\?(&|$)|\?\?/i;
	jQuery.ajaxSetup({
		jsonp : "callback",
		jsonpCallback : function () {
			return jQuery.expando + "_" + (jsc++)
		}
	});
	jQuery.ajaxPrefilter("json jsonp", function (s, originalSettings, jqXHR) {
		var inspectData = s.contentType === "application/x-www-form-urlencoded" && (typeof s.data === "string");
		if (s.dataTypes[0] === "jsonp" || s.jsonp !== false && (jsre.test(s.url) || inspectData && jsre.test(s.data))) {
			var responseContainer,
			jsonpCallback = s.jsonpCallback = jQuery.isFunction(s.jsonpCallback) ? s.jsonpCallback() : s.jsonpCallback,
			previous = window[jsonpCallback],
			url = s.url,
			data = s.data,
			replace = "$1" + jsonpCallback + "$2";
			if (s.jsonp !== false) {
				url = url.replace(jsre, replace);
				if (s.url === url) {
					if (inspectData) {
						data = data.replace(jsre, replace)
					}
					if (s.data === data) {
						url += (/\?/.test(url) ? "&" : "?") + s.jsonp + "=" + jsonpCallback
					}
				}
			}
			s.url = url;
			s.data = data;
			window[jsonpCallback] = function (response) {
				responseContainer = [response]
			};
			jqXHR.always(function () {
				window[jsonpCallback] = previous;
				if (responseContainer && jQuery.isFunction(previous)) {
					window[jsonpCallback](responseContainer[0])
				}
			});
			s.converters["script json"] = function () {
				if (!responseContainer) {
					jQuery.error(jsonpCallback + " was not called")
				}
				return responseContainer[0]
			};
			s.dataTypes[0] = "json";
			return "script"
		}
	});
	jQuery.ajaxSetup({
		accepts : {
			script : "text/javascript, application/javascript, application/ecmascript, application/x-ecmascript"
		},
		contents : {
			script : /javascript|ecmascript/
		},
		converters : {
			"text script" : function (text) {
				jQuery.globalEval(text);
				return text
			}
		}
	});
	jQuery.ajaxPrefilter("script", function (s) {
		if (s.cache === undefined) {
			s.cache = false
		}
		if (s.crossDomain) {
			s.type = "GET";
			s.global = false
		}
	});
	jQuery.ajaxTransport("script", function (s) {
		if (s.crossDomain) {
			var script,
			head = document.head || document.getElementsByTagName("head")[0] || document.documentElement;
			return {
				send : function (_, callback) {
					script = document.createElement("script");
					script.async = "async";
					if (s.scriptCharset) {
						script.charset = s.scriptCharset
					}
					script.src = s.url;
					script.onload = script.onreadystatechange = function (_, isAbort) {
						if (isAbort || !script.readyState || /loaded|complete/.test(script.readyState)) {
							script.onload = script.onreadystatechange = null;
							if (head && script.parentNode) {
								head.removeChild(script)
							}
							script = undefined;
							if (!isAbort) {
								callback(200, "success")
							}
						}
					};
					head.insertBefore(script, head.firstChild)
				},
				abort : function () {
					if (script) {
						script.onload(0, 1)
					}
				}
			}
		}
	});
	var xhrOnUnloadAbort = window.ActiveXObject ? function () {
		for (var key in xhrCallbacks) {
			xhrCallbacks[key](0, 1)
		}
	}
	 : false,
	xhrId = 0,
	xhrCallbacks;
	function createStandardXHR() {
		try {
			return new window.XMLHttpRequest()
		} catch (e) {}
		
	}
	function createActiveXHR() {
		try {
			return new window.ActiveXObject("Microsoft.XMLHTTP")
		} catch (e) {}
		
	}
	jQuery.ajaxSettings.xhr = window.ActiveXObject ? function () {
		return !this.isLocal && createStandardXHR() || createActiveXHR()
	}
	 : createStandardXHR;
	(function (xhr) {
		jQuery.extend(jQuery.support, {
			ajax : !!xhr,
			cors : !!xhr && ("withCredentials" in xhr)
		})
	})(jQuery.ajaxSettings.xhr());
	if (jQuery.support.ajax) {
		jQuery.ajaxTransport(function (s) {
			if (!s.crossDomain || jQuery.support.cors) {
				var callback;
				return {
					send : function (headers, complete) {
						var xhr = s.xhr(),
						handle,
						i;
						if (s.username) {
							xhr.open(s.type, s.url, s.async, s.username, s.password)
						} else {
							xhr.open(s.type, s.url, s.async)
						}
						if (s.xhrFields) {
							for (i in s.xhrFields) {
								xhr[i] = s.xhrFields[i]
							}
						}
						if (s.mimeType && xhr.overrideMimeType) {
							xhr.overrideMimeType(s.mimeType)
						}
						if (!s.crossDomain && !headers["X-Requested-With"]) {
							headers["X-Requested-With"] = "XMLHttpRequest"
						}
						try {
							for (i in headers) {
								xhr.setRequestHeader(i, headers[i])
							}
						} catch (_) {}
						
						xhr.send((s.hasContent && s.data) || null);
						callback = function (_, isAbort) {
							var status,
							statusText,
							responseHeaders,
							responses,
							xml;
							try {
								if (callback && (isAbort || xhr.readyState === 4)) {
									callback = undefined;
									if (handle) {
										xhr.onreadystatechange = jQuery.noop;
										if (xhrOnUnloadAbort) {
											delete xhrCallbacks[handle]
										}
									}
									if (isAbort) {
										if (xhr.readyState !== 4) {
											xhr.abort()
										}
									} else {
										status = xhr.status;
										responseHeaders = xhr.getAllResponseHeaders();
										responses = {};
										xml = xhr.responseXML;
										if (xml && xml.documentElement) {
											responses.xml = xml
										}
										responses.text = xhr.responseText;
										try {
											statusText = xhr.statusText
										} catch (e) {
											statusText = ""
										}
										if (!status && s.isLocal && !s.crossDomain) {
											status = responses.text ? 200 : 404
										} else {
											if (status === 1223) {
												status = 204
											}
										}
									}
								}
							} catch (firefoxAccessException) {
								if (!isAbort) {
									complete(-1, firefoxAccessException)
								}
							}
							if (responses) {
								complete(status, statusText, responses, responseHeaders)
							}
						};
						if (!s.async || xhr.readyState === 4) {
							callback()
						} else {
							handle = ++xhrId;
							if (xhrOnUnloadAbort) {
								if (!xhrCallbacks) {
									xhrCallbacks = {};
									jQuery(window).unload(xhrOnUnloadAbort)
								}
								xhrCallbacks[handle] = callback
							}
							xhr.onreadystatechange = callback
						}
					},
					abort : function () {
						if (callback) {
							callback(0, 1)
						}
					}
				}
			}
		})
	}
	var elemdisplay = {},
	iframe,
	iframeDoc,
	rfxtypes = /^(?:toggle|show|hide)$/,
	rfxnum = /^([+\-]=)?([\d+.\-]+)([a-z%]*)$/i,
	timerId,
	fxAttrs = [["height", "marginTop", "marginBottom", "paddingTop", "paddingBottom"], ["width", "marginLeft", "marginRight", "paddingLeft", "paddingRight"], ["opacity"]],
	fxNow,
	requestAnimationFrame = window.webkitRequestAnimationFrame || window.mozRequestAnimationFrame || window.oRequestAnimationFrame;
	jQuery.fn.extend({
		show : function (speed, easing, callback) {
			var elem,
			display;
			if (speed || speed === 0) {
				return this.animate(genFx("show", 3), speed, easing, callback)
			} else {
				for (var i = 0, j = this.length; i < j; i++) {
					elem = this[i];
					if (elem.style) {
						display = elem.style.display;
						if (!jQuery._data(elem, "olddisplay") && display === "none") {
							display = elem.style.display = ""
						}
						if (display === "" && jQuery.css(elem, "display") === "none") {
							jQuery._data(elem, "olddisplay", defaultDisplay(elem.nodeName))
						}
					}
				}
				for (i = 0; i < j; i++) {
					elem = this[i];
					if (elem.style) {
						display = elem.style.display;
						if (display === "" || display === "none") {
							elem.style.display = jQuery._data(elem, "olddisplay") || ""
						}
					}
				}
				return this
			}
		},
		hide : function (speed, easing, callback) {
			if (speed || speed === 0) {
				return this.animate(genFx("hide", 3), speed, easing, callback)
			} else {
				for (var i = 0, j = this.length; i < j; i++) {
					if (this[i].style) {
						var display = jQuery.css(this[i], "display");
						if (display !== "none" && !jQuery._data(this[i], "olddisplay")) {
							jQuery._data(this[i], "olddisplay", display)
						}
					}
				}
				for (i = 0; i < j; i++) {
					if (this[i].style) {
						this[i].style.display = "none"
					}
				}
				return this
			}
		},
		_toggle : jQuery.fn.toggle,
		toggle : function (fn, fn2, callback) {
			var bool = typeof fn === "boolean";
			if (jQuery.isFunction(fn) && jQuery.isFunction(fn2)) {
				this._toggle.apply(this, arguments)
			} else {
				if (fn == null || bool) {
					this.each(function () {
						var state = bool ? fn : jQuery(this).is(":hidden");
						jQuery(this)[state ? "show" : "hide"]()
					})
				} else {
					this.animate(genFx("toggle", 3), fn, fn2, callback)
				}
			}
			return this
		},
		fadeTo : function (speed, to, easing, callback) {
			return this.filter(":hidden").css("opacity", 0).show().end().animate({
				opacity : to
			}, speed, easing, callback)
		},
		animate : function (prop, speed, easing, callback) {
			var optall = jQuery.speed(speed, easing, callback);
			if (jQuery.isEmptyObject(prop)) {
				return this.each(optall.complete, [false])
			}
			prop = jQuery.extend({}, prop);
			return this[optall.queue === false ? "each" : "queue"](function () {
				if (optall.queue === false) {
					jQuery._mark(this)
				}
				var opt = jQuery.extend({}, optall),
				isElement = this.nodeType === 1,
				hidden = isElement && jQuery(this).is(":hidden"),
				name,
				val,
				p,
				display,
				e,
				parts,
				start,
				end,
				unit;
				opt.animatedProperties = {};
				for (p in prop) {
					name = jQuery.camelCase(p);
					if (p !== name) {
						prop[name] = prop[p];
						delete prop[p]
					}
					val = prop[name];
					if (jQuery.isArray(val)) {
						opt.animatedProperties[name] = val[1];
						val = prop[name] = val[0]
					} else {
						opt.animatedProperties[name] = opt.specialEasing && opt.specialEasing[name] || opt.easing || "swing"
					}
					if (val === "hide" && hidden || val === "show" && !hidden) {
						return opt.complete.call(this)
					}
					if (isElement && (name === "height" || name === "width")) {
						opt.overflow = [this.style.overflow, this.style.overflowX, this.style.overflowY];
						if (jQuery.css(this, "display") === "inline" && jQuery.css(this, "float") === "none") {
							if (!jQuery.support.inlineBlockNeedsLayout) {
								this.style.display = "inline-block"
							} else {
								display = defaultDisplay(this.nodeName);
								if (display === "inline") {
									this.style.display = "inline-block"
								} else {
									this.style.display = "inline";
									this.style.zoom = 1
								}
							}
						}
					}
				}
				if (opt.overflow != null) {
					this.style.overflow = "hidden"
				}
				for (p in prop) {
					e = new jQuery.fx(this, opt, p);
					val = prop[p];
					if (rfxtypes.test(val)) {
						e[val === "toggle" ? hidden ? "show" : "hide" : val]()
					} else {
						parts = rfxnum.exec(val);
						start = e.cur();
						if (parts) {
							end = parseFloat(parts[2]);
							unit = parts[3] || (jQuery.cssNumber[p] ? "" : "px");
							if (unit !== "px") {
								jQuery.style(this, p, (end || 1) + unit);
								start = ((end || 1) / e.cur()) * start;
								jQuery.style(this, p, start + unit)
							}
							if (parts[1]) {
								end = ((parts[1] === "-=" ? -1 : 1) * end) + start
							}
							e.custom(start, end, unit)
						} else {
							e.custom(start, val, "")
						}
					}
				}
				return true
			})
		},
		stop : function (clearQueue, gotoEnd) {
			if (clearQueue) {
				this.queue([])
			}
			this.each(function () {
				var timers = jQuery.timers,
				i = timers.length;
				if (!gotoEnd) {
					jQuery._unmark(true, this)
				}
				while (i--) {
					if (timers[i].elem === this) {
						if (gotoEnd) {
							timers[i](true)
						}
						timers.splice(i, 1)
					}
				}
			});
			if (!gotoEnd) {
				this.dequeue()
			}
			return this
		}
	});
	function createFxNow() {
		setTimeout(clearFxNow, 0);
		return (fxNow = jQuery.now())
	}
	function clearFxNow() {
		fxNow = undefined
	}
	function genFx(type, num) {
		var obj = {};
		jQuery.each(fxAttrs.concat.apply([], fxAttrs.slice(0, num)), function () {
			obj[this] = type
		});
		return obj
	}
	jQuery.each({
		slideDown : genFx("show", 1),
		slideUp : genFx("hide", 1),
		slideToggle : genFx("toggle", 1),
		fadeIn : {
			opacity : "show"
		},
		fadeOut : {
			opacity : "hide"
		},
		fadeToggle : {
			opacity : "toggle"
		}
	}, function (name, props) {
		jQuery.fn[name] = function (speed, easing, callback) {
			return this.animate(props, speed, easing, callback)
		}
	});
	jQuery.extend({
		speed : function (speed, easing, fn) {
			var opt = speed && typeof speed === "object" ? jQuery.extend({}, speed) : {
				complete : fn || !fn && easing || jQuery.isFunction(speed) && speed,
				duration : speed,
				easing : fn && easing || easing && !jQuery.isFunction(easing) && easing
			};
			opt.duration = jQuery.fx.off ? 0 : typeof opt.duration === "number" ? opt.duration : opt.duration in jQuery.fx.speeds ? jQuery.fx.speeds[opt.duration] : jQuery.fx.speeds._default;
			opt.old = opt.complete;
			opt.complete = function (noUnmark) {
				if (opt.queue !== false) {
					jQuery.dequeue(this)
				} else {
					if (noUnmark !== false) {
						jQuery._unmark(this)
					}
				}
				if (jQuery.isFunction(opt.old)) {
					opt.old.call(this)
				}
			};
			return opt
		},
		easing : {
			linear : function (p, n, firstNum, diff) {
				return firstNum + diff * p
			},
			swing : function (p, n, firstNum, diff) {
				return ((-Math.cos(p * Math.PI) / 2) + 0.5) * diff + firstNum
			}
		},
		timers : [],
		fx : function (elem, options, prop) {
			this.options = options;
			this.elem = elem;
			this.prop = prop;
			options.orig = options.orig || {}
			
		}
	});
	jQuery.fx.prototype = {
		update : function () {
			if (this.options.step) {
				this.options.step.call(this.elem, this.now, this)
			}
			(jQuery.fx.step[this.prop] || jQuery.fx.step._default)(this)
		},
		cur : function () {
			if (this.elem[this.prop] != null && (!this.elem.style || this.elem.style[this.prop] == null)) {
				return this.elem[this.prop]
			}
			var parsed,
			r = jQuery.css(this.elem, this.prop);
			return isNaN(parsed = parseFloat(r)) ? !r || r === "auto" ? 0 : r : parsed
		},
		custom : function (from, to, unit) {
			var self = this,
			fx = jQuery.fx,
			raf;
			this.startTime = fxNow || createFxNow();
			this.start = from;
			this.end = to;
			this.unit = unit || this.unit || (jQuery.cssNumber[this.prop] ? "" : "px");
			this.now = this.start;
			this.pos = this.state = 0;
			function t(gotoEnd) {
				return self.step(gotoEnd)
			}
			t.elem = this.elem;
			if (t() && jQuery.timers.push(t) && !timerId) {
				if (requestAnimationFrame) {
					timerId = 1;
					raf = function () {
						if (timerId) {
							requestAnimationFrame(raf);
							fx.tick()
						}
					};
					requestAnimationFrame(raf)
				} else {
					timerId = setInterval(fx.tick, fx.interval)
				}
			}
		},
		show : function () {
			this.options.orig[this.prop] = jQuery.style(this.elem, this.prop);
			this.options.show = true;
			this.custom(this.prop === "width" || this.prop === "height" ? 1 : 0, this.cur());
			jQuery(this.elem).show()
		},
		hide : function () {
			this.options.orig[this.prop] = jQuery.style(this.elem, this.prop);
			this.options.hide = true;
			this.custom(this.cur(), 0)
		},
		step : function (gotoEnd) {
			var t = fxNow || createFxNow(),
			done = true,
			elem = this.elem,
			options = this.options,
			i,
			n;
			if (gotoEnd || t >= options.duration + this.startTime) {
				this.now = this.end;
				this.pos = this.state = 1;
				this.update();
				options.animatedProperties[this.prop] = true;
				for (i in options.animatedProperties) {
					if (options.animatedProperties[i] !== true) {
						done = false
					}
				}
				if (done) {
					if (options.overflow != null && !jQuery.support.shrinkWrapBlocks) {
						jQuery.each(["", "X", "Y"], function (index, value) {
							elem.style["overflow" + value] = options.overflow[index]
						})
					}
					if (options.hide) {
						jQuery(elem).hide()
					}
					if (options.hide || options.show) {
						for (var p in options.animatedProperties) {
							jQuery.style(elem, p, options.orig[p])
						}
					}
					options.complete.call(elem)
				}
				return false
			} else {
				if (options.duration == Infinity) {
					this.now = t
				} else {
					n = t - this.startTime;
					this.state = n / options.duration;
					this.pos = jQuery.easing[options.animatedProperties[this.prop]](this.state, n, 0, 1, options.duration);
					this.now = this.start + ((this.end - this.start) * this.pos)
				}
				this.update()
			}
			return true
		}
	};
	jQuery.extend(jQuery.fx, {
		tick : function () {
			for (var timers = jQuery.timers, i = 0; i < timers.length; ++i) {
				if (!timers[i]()) {
					timers.splice(i--, 1)
				}
			}
			if (!timers.length) {
				jQuery.fx.stop()
			}
		},
		interval : 13,
		stop : function () {
			clearInterval(timerId);
			timerId = null
		},
		speeds : {
			slow : 600,
			fast : 200,
			_default : 400
		},
		step : {
			opacity : function (fx) {
				jQuery.style(fx.elem, "opacity", fx.now)
			},
			_default : function (fx) {
				if (fx.elem.style && fx.elem.style[fx.prop] != null) {
					fx.elem.style[fx.prop] = (fx.prop === "width" || fx.prop === "height" ? Math.max(0, fx.now) : fx.now) + fx.unit
				} else {
					fx.elem[fx.prop] = fx.now
				}
			}
		}
	});
	if (jQuery.expr && jQuery.expr.filters) {
		jQuery.expr.filters.animated = function (elem) {
			return jQuery.grep(jQuery.timers, function (fn) {
				return elem === fn.elem
			}).length
		}
	}
	function defaultDisplay(nodeName) {
		if (!elemdisplay[nodeName]) {
			var elem = jQuery("<" + nodeName + ">").appendTo("body"),
			display = elem.css("display");
			elem.remove();
			if (display === "none" || display === "") {
				if (!iframe) {
					iframe = document.createElement("iframe");
					iframe.frameBorder = iframe.width = iframe.height = 0
				}
				document.body.appendChild(iframe);
				if (!iframeDoc || !iframe.createElement) {
					iframeDoc = (iframe.contentWindow || iframe.contentDocument).document;
					iframeDoc.write("<!doctype><html><body></body></html>")
				}
				elem = iframeDoc.createElement(nodeName);
				iframeDoc.body.appendChild(elem);
				display = jQuery.css(elem, "display");
				document.body.removeChild(iframe)
			}
			elemdisplay[nodeName] = display
		}
		return elemdisplay[nodeName]
	}
	var rtable = /^t(?:able|d|h)$/i,
	rroot = /^(?:body|html)$/i;
	if ("getBoundingClientRect" in document.documentElement) {
		jQuery.fn.offset = function (options) {
			var elem = this[0],
			box;
			if (options) {
				return this.each(function (i) {
					jQuery.offset.setOffset(this, options, i)
				})
			}
			if (!elem || !elem.ownerDocument) {
				return null
			}
			if (elem === elem.ownerDocument.body) {
				return jQuery.offset.bodyOffset(elem)
			}
			try {
				box = elem.getBoundingClientRect()
			} catch (e) {}
			
			var doc = elem.ownerDocument,
			docElem = doc.documentElement;
			if (!box || !jQuery.contains(docElem, elem)) {
				return box ? {
					top : box.top,
					left : box.left
				}
				 : {
					top : 0,
					left : 0
				}
			}
			var body = doc.body,
			win = getWindow(doc),
			clientTop = docElem.clientTop || body.clientTop || 0,
			clientLeft = docElem.clientLeft || body.clientLeft || 0,
			scrollTop = win.pageYOffset || jQuery.support.boxModel && docElem.scrollTop || body.scrollTop,
			scrollLeft = win.pageXOffset || jQuery.support.boxModel && docElem.scrollLeft || body.scrollLeft,
			top = box.top + scrollTop - clientTop,
			left = box.left + scrollLeft - clientLeft;
			return {
				top : top,
				left : left
			}
		}
	} else {
		jQuery.fn.offset = function (options) {
			var elem = this[0];
			if (options) {
				return this.each(function (i) {
					jQuery.offset.setOffset(this, options, i)
				})
			}
			if (!elem || !elem.ownerDocument) {
				return null
			}
			if (elem === elem.ownerDocument.body) {
				return jQuery.offset.bodyOffset(elem)
			}
			jQuery.offset.initialize();
			var computedStyle,
			offsetParent = elem.offsetParent,
			prevOffsetParent = elem,
			doc = elem.ownerDocument,
			docElem = doc.documentElement,
			body = doc.body,
			defaultView = doc.defaultView,
			prevComputedStyle = defaultView ? defaultView.getComputedStyle(elem, null) : elem.currentStyle,
			top = elem.offsetTop,
			left = elem.offsetLeft;
			while ((elem = elem.parentNode) && elem !== body && elem !== docElem) {
				if (jQuery.offset.supportsFixedPosition && prevComputedStyle.position === "fixed") {
					break
				}
				computedStyle = defaultView ? defaultView.getComputedStyle(elem, null) : elem.currentStyle;
				top -= elem.scrollTop;
				left -= elem.scrollLeft;
				if (elem === offsetParent) {
					top += elem.offsetTop;
					left += elem.offsetLeft;
					if (jQuery.offset.doesNotAddBorder && !(jQuery.offset.doesAddBorderForTableAndCells && rtable.test(elem.nodeName))) {
						top += parseFloat(computedStyle.borderTopWidth) || 0;
						left += parseFloat(computedStyle.borderLeftWidth) || 0
					}
					prevOffsetParent = offsetParent;
					offsetParent = elem.offsetParent
				}
				if (jQuery.offset.subtractsBorderForOverflowNotVisible && computedStyle.overflow !== "visible") {
					top += parseFloat(computedStyle.borderTopWidth) || 0;
					left += parseFloat(computedStyle.borderLeftWidth) || 0
				}
				prevComputedStyle = computedStyle
			}
			if (prevComputedStyle.position === "relative" || prevComputedStyle.position === "static") {
				top += body.offsetTop;
				left += body.offsetLeft
			}
			if (jQuery.offset.supportsFixedPosition && prevComputedStyle.position === "fixed") {
				top += Math.max(docElem.scrollTop, body.scrollTop);
				left += Math.max(docElem.scrollLeft, body.scrollLeft)
			}
			return {
				top : top,
				left : left
			}
		}
	}
	jQuery.offset = {
		initialize : function () {
			var body = document.body,
			container = document.createElement("div"),
			innerDiv,
			checkDiv,
			table,
			td,
			bodyMarginTop = parseFloat(jQuery.css(body, "marginTop")) || 0,
			html = "<div style='position:absolute;top:0;left:0;margin:0;border:5px solid #000;padding:0;width:1px;height:1px;'><div></div></div><table style='position:absolute;top:0;left:0;margin:0;border:5px solid #000;padding:0;width:1px;height:1px;' cellpadding='0' cellspacing='0'><tr><td></td></tr></table>";
			jQuery.extend(container.style, {
				position : "absolute",
				top : 0,
				left : 0,
				margin : 0,
				border : 0,
				width : "1px",
				height : "1px",
				visibility : "hidden"
			});
			container.innerHTML = html;
			body.insertBefore(container, body.firstChild);
			innerDiv = container.firstChild;
			checkDiv = innerDiv.firstChild;
			td = innerDiv.nextSibling.firstChild.firstChild;
			this.doesNotAddBorder = (checkDiv.offsetTop !== 5);
			this.doesAddBorderForTableAndCells = (td.offsetTop === 5);
			checkDiv.style.position = "fixed";
			checkDiv.style.top = "20px";
			this.supportsFixedPosition = (checkDiv.offsetTop === 20 || checkDiv.offsetTop === 15);
			checkDiv.style.position = checkDiv.style.top = "";
			innerDiv.style.overflow = "hidden";
			innerDiv.style.position = "relative";
			this.subtractsBorderForOverflowNotVisible = (checkDiv.offsetTop === -5);
			this.doesNotIncludeMarginInBodyOffset = (body.offsetTop !== bodyMarginTop);
			body.removeChild(container);
			jQuery.offset.initialize = jQuery.noop
		},
		bodyOffset : function (body) {
			var top = body.offsetTop,
			left = body.offsetLeft;
			jQuery.offset.initialize();
			if (jQuery.offset.doesNotIncludeMarginInBodyOffset) {
				top += parseFloat(jQuery.css(body, "marginTop")) || 0;
				left += parseFloat(jQuery.css(body, "marginLeft")) || 0
			}
			return {
				top : top,
				left : left
			}
		},
		setOffset : function (elem, options, i) {
			var position = jQuery.css(elem, "position");
			if (position === "static") {
				elem.style.position = "relative"
			}
			var curElem = jQuery(elem),
			curOffset = curElem.offset(),
			curCSSTop = jQuery.css(elem, "top"),
			curCSSLeft = jQuery.css(elem, "left"),
			calculatePosition = (position === "absolute" || position === "fixed") && jQuery.inArray("auto", [curCSSTop, curCSSLeft]) > -1,
			props = {},
			curPosition = {},
			curTop,
			curLeft;
			if (calculatePosition) {
				curPosition = curElem.position();
				curTop = curPosition.top;
				curLeft = curPosition.left
			} else {
				curTop = parseFloat(curCSSTop) || 0;
				curLeft = parseFloat(curCSSLeft) || 0
			}
			if (jQuery.isFunction(options)) {
				options = options.call(elem, i, curOffset)
			}
			if (options.top != null) {
				props.top = (options.top - curOffset.top) + curTop
			}
			if (options.left != null) {
				props.left = (options.left - curOffset.left) + curLeft
			}
			if ("using" in options) {
				options.using.call(elem, props)
			} else {
				curElem.css(props)
			}
		}
	};
	jQuery.fn.extend({
		position : function () {
			if (!this[0]) {
				return null
			}
			var elem = this[0],
			offsetParent = this.offsetParent(),
			offset = this.offset(),
			parentOffset = rroot.test(offsetParent[0].nodeName) ? {
				top : 0,
				left : 0
			}
			 : offsetParent.offset();
			offset.top -= parseFloat(jQuery.css(elem, "marginTop")) || 0;
			offset.left -= parseFloat(jQuery.css(elem, "marginLeft")) || 0;
			parentOffset.top += parseFloat(jQuery.css(offsetParent[0], "borderTopWidth")) || 0;
			parentOffset.left += parseFloat(jQuery.css(offsetParent[0], "borderLeftWidth")) || 0;
			return {
				top : offset.top - parentOffset.top,
				left : offset.left - parentOffset.left
			}
		},
		offsetParent : function () {
			return this.map(function () {
				var offsetParent = this.offsetParent || document.body;
				while (offsetParent && (!rroot.test(offsetParent.nodeName) && jQuery.css(offsetParent, "position") === "static")) {
					offsetParent = offsetParent.offsetParent
				}
				return offsetParent
			})
		}
	});
	jQuery.each(["Left", "Top"], function (i, name) {
		var method = "scroll" + name;
		jQuery.fn[method] = function (val) {
			var elem,
			win;
			if (val === undefined) {
				elem = this[0];
				if (!elem) {
					return null
				}
				win = getWindow(elem);
				return win ? ("pageXOffset" in win) ? win[i ? "pageYOffset" : "pageXOffset"] : jQuery.support.boxModel && win.document.documentElement[method] || win.document.body[method] : elem[method]
			}
			return this.each(function () {
				win = getWindow(this);
				if (win) {
					win.scrollTo(!i ? val : jQuery(win).scrollLeft(), i ? val : jQuery(win).scrollTop())
				} else {
					this[method] = val
				}
			})
		}
	});
	function getWindow(elem) {
		return jQuery.isWindow(elem) ? elem : elem.nodeType === 9 ? elem.defaultView || elem.parentWindow : false
	}
	jQuery.each(["Height", "Width"], function (i, name) {
		var type = name.toLowerCase();
		jQuery.fn["inner" + name] = function () {
			return this[0] ? parseFloat(jQuery.css(this[0], type, "padding")) : null
		};
		jQuery.fn["outer" + name] = function (margin) {
			return this[0] ? parseFloat(jQuery.css(this[0], type, margin ? "margin" : "border")) : null
		};
		jQuery.fn[type] = function (size) {
			var elem = this[0];
			if (!elem) {
				return size == null ? null : this
			}
			if (jQuery.isFunction(size)) {
				return this.each(function (i) {
					var self = jQuery(this);
					self[type](size.call(this, i, self[type]()))
				})
			}
			if (jQuery.isWindow(elem)) {
				var docElemProp = elem.document.documentElement["client" + name];
				return elem.document.compatMode === "CSS1Compat" && docElemProp || elem.document.body["client" + name] || docElemProp
			} else {
				if (elem.nodeType === 9) {
					return Math.max(elem.documentElement["client" + name], elem.body["scroll" + name], elem.documentElement["scroll" + name], elem.body["offset" + name], elem.documentElement["offset" + name])
				} else {
					if (size === undefined) {
						var orig = jQuery.css(elem, type),
						ret = parseFloat(orig);
						return jQuery.isNaN(ret) ? orig : ret
					} else {
						return this.css(type, typeof size === "string" ? size : size + "px")
					}
				}
			}
		}
	});
	window.jQuery = window.$ = jQuery
})(window);
/*
 * jQuery UI 1.8.12
 *
 * Copyright 2011, AUTHORS.txt (http://jqueryui.com/about)
 * Dual licensed under the MIT or GPL Version 2 licenses.
 * http://jquery.org/license
 *
 * http://docs.jquery.com/UI
 */
(function ($, undefined) {
	$.ui = $.ui || {};
	if ($.ui.version) {
		return
	}
	$.extend($.ui, {
		version : "1.8.12",
		keyCode : {
			ALT : 18,
			BACKSPACE : 8,
			CAPS_LOCK : 20,
			COMMA : 188,
			COMMAND : 91,
			COMMAND_LEFT : 91,
			COMMAND_RIGHT : 93,
			CONTROL : 17,
			DELETE : 46,
			DOWN : 40,
			END : 35,
			ENTER : 13,
			ESCAPE : 27,
			HOME : 36,
			INSERT : 45,
			LEFT : 37,
			MENU : 93,
			NUMPAD_ADD : 107,
			NUMPAD_DECIMAL : 110,
			NUMPAD_DIVIDE : 111,
			NUMPAD_ENTER : 108,
			NUMPAD_MULTIPLY : 106,
			NUMPAD_SUBTRACT : 109,
			PAGE_DOWN : 34,
			PAGE_UP : 33,
			PERIOD : 190,
			RIGHT : 39,
			SHIFT : 16,
			SPACE : 32,
			TAB : 9,
			UP : 38,
			WINDOWS : 91
		}
	});
	$.fn.extend({
		_focus : $.fn.focus,
		focus : function (delay, fn) {
			return typeof delay === "number" ? this.each(function () {
				var elem = this;
				setTimeout(function () {
					$(elem).focus();
					if (fn) {
						fn.call(elem)
					}
				}, delay)
			}) : this._focus.apply(this, arguments)
		},
		scrollParent : function () {
			var scrollParent;
			if (($.browser.msie && (/(static|relative)/).test(this.css("position"))) || (/absolute/).test(this.css("position"))) {
				scrollParent = this.parents().filter(function () {
						return (/(relative|absolute|fixed)/).test($.curCSS(this, "position", 1)) && (/(auto|scroll)/).test($.curCSS(this, "overflow", 1) + $.curCSS(this, "overflow-y", 1) + $.curCSS(this, "overflow-x", 1))
					}).eq(0)
			} else {
				scrollParent = this.parents().filter(function () {
						return (/(auto|scroll)/).test($.curCSS(this, "overflow", 1) + $.curCSS(this, "overflow-y", 1) + $.curCSS(this, "overflow-x", 1))
					}).eq(0)
			}
			return (/fixed/).test(this.css("position")) || !scrollParent.length ? $(document) : scrollParent
		},
		zIndex : function (zIndex) {
			if (zIndex !== undefined) {
				return this.css("zIndex", zIndex)
			}
			if (this.length) {
				var elem = $(this[0]),
				position,
				value;
				while (elem.length && elem[0] !== document) {
					position = elem.css("position");
					if (position === "absolute" || position === "relative" || position === "fixed") {
						value = parseInt(elem.css("zIndex"), 10);
						if (!isNaN(value) && value !== 0) {
							return value
						}
					}
					elem = elem.parent()
				}
			}
			return 0
		},
		disableSelection : function () {
			return this.bind(($.support.selectstart ? "selectstart" : "mousedown") + ".ui-disableSelection", function (event) {
				event.preventDefault()
			})
		},
		enableSelection : function () {
			return this.unbind(".ui-disableSelection")
		}
	});
	$.each(["Width", "Height"], function (i, name) {
		var side = name === "Width" ? ["Left", "Right"] : ["Top", "Bottom"],
		type = name.toLowerCase(),
		orig = {
			innerWidth : $.fn.innerWidth,
			innerHeight : $.fn.innerHeight,
			outerWidth : $.fn.outerWidth,
			outerHeight : $.fn.outerHeight
		};
		function reduce(elem, size, border, margin) {
			$.each(side, function () {
				size -= parseFloat($.curCSS(elem, "padding" + this, true)) || 0;
				if (border) {
					size -= parseFloat($.curCSS(elem, "border" + this + "Width", true)) || 0
				}
				if (margin) {
					size -= parseFloat($.curCSS(elem, "margin" + this, true)) || 0
				}
			});
			return size
		}
		$.fn["inner" + name] = function (size) {
			if (size === undefined) {
				return orig["inner" + name].call(this)
			}
			return this.each(function () {
				$(this).css(type, reduce(this, size) + "px")
			})
		};
		$.fn["outer" + name] = function (size, margin) {
			if (typeof size !== "number") {
				return orig["outer" + name].call(this, size)
			}
			return this.each(function () {
				$(this).css(type, reduce(this, size, true, margin) + "px")
			})
		}
	});
	function visible(element) {
		return !$(element).parents().andSelf().filter(function () {
			return $.curCSS(this, "visibility") === "hidden" || $.expr.filters.hidden(this)
		}).length
	}
	$.extend($.expr[":"], {
		data : function (elem, i, match) {
			return !!$.data(elem, match[3])
		},
		focusable : function (element) {
			var nodeName = element.nodeName.toLowerCase(),
			tabIndex = $.attr(element, "tabindex");
			if ("area" === nodeName) {
				var map = element.parentNode,
				mapName = map.name,
				img;
				if (!element.href || !mapName || map.nodeName.toLowerCase() !== "map") {
					return false
				}
				img = $("img[usemap=#" + mapName + "]")[0];
				return !!img && visible(img)
			}
			return (/input|select|textarea|button|object/.test(nodeName) ? !element.disabled : "a" == nodeName ? element.href || !isNaN(tabIndex) : !isNaN(tabIndex)) && visible(element)
		},
		tabbable : function (element) {
			var tabIndex = $.attr(element, "tabindex");
			return (isNaN(tabIndex) || tabIndex >= 0) && $(element).is(":focusable")
		}
	});
	$(function () {
		var body = document.body,
		div = body.appendChild(div = document.createElement("div"));
		$.extend(div.style, {
			minHeight : "100px",
			height : "auto",
			padding : 0,
			borderWidth : 0
		});
		$.support.minHeight = div.offsetHeight === 100;
		$.support.selectstart = "onselectstart" in div;
		body.removeChild(div).style.display = "none"
	});
	$.extend($.ui, {
		plugin : {
			add : function (module, option, set) {
				var proto = $.ui[module].prototype;
				for (var i in set) {
					proto.plugins[i] = proto.plugins[i] || [];
					proto.plugins[i].push([option, set[i]])
				}
			},
			call : function (instance, name, args) {
				var set = instance.plugins[name];
				if (!set || !instance.element[0].parentNode) {
					return
				}
				for (var i = 0; i < set.length; i++) {
					if (instance.options[set[i][0]]) {
						set[i][1].apply(instance.element, args)
					}
				}
			}
		},
		contains : function (a, b) {
			return document.compareDocumentPosition ? a.compareDocumentPosition(b) & 16 : a !== b && a.contains(b)
		},
		hasScroll : function (el, a) {
			if ($(el).css("overflow") === "hidden") {
				return false
			}
			var scroll = (a && a === "left") ? "scrollLeft" : "scrollTop",
			has = false;
			if (el[scroll] > 0) {
				return true
			}
			el[scroll] = 1;
			has = (el[scroll] > 0);
			el[scroll] = 0;
			return has
		},
		isOverAxis : function (x, reference, size) {
			return (x > reference) && (x < (reference + size))
		},
		isOver : function (y, x, top, left, height, width) {
			return $.ui.isOverAxis(y, top, height) && $.ui.isOverAxis(x, left, width)
		}
	})
})(jQuery);
/*
 * jQuery UI Widget 1.8.12
 *
 * Copyright 2011, AUTHORS.txt (http://jqueryui.com/about)
 * Dual licensed under the MIT or GPL Version 2 licenses.
 * http://jquery.org/license
 *
 * http://docs.jquery.com/UI/Widget
 */
(function ($, undefined) {
	if ($.cleanData) {
		var _cleanData = $.cleanData;
		$.cleanData = function (elems) {
			for (var i = 0, elem; (elem = elems[i]) != null; i++) {
				$(elem).triggerHandler("remove")
			}
			_cleanData(elems)
		}
	} else {
		var _remove = $.fn.remove;
		$.fn.remove = function (selector, keepData) {
			return this.each(function () {
				if (!keepData) {
					if (!selector || $.filter(selector, [this]).length) {
						$("*", this).add([this]).each(function () {
							$(this).triggerHandler("remove")
						})
					}
				}
				return _remove.call($(this), selector, keepData)
			})
		}
	}
	$.widget = function (name, base, prototype) {
		var namespace = name.split(".")[0],
		fullName;
		name = name.split(".")[1];
		fullName = namespace + "-" + name;
		if (!prototype) {
			prototype = base;
			base = $.Widget
		}
		$.expr[":"][fullName] = function (elem) {
			return !!$.data(elem, name)
		};
		$[namespace] = $[namespace] || {};
		$[namespace][name] = function (options, element) {
			if (arguments.length) {
				this._createWidget(options, element)
			}
		};
		var basePrototype = new base();
		basePrototype.options = $.extend(true, {}, basePrototype.options);
		$[namespace][name].prototype = $.extend(true, basePrototype, {
				namespace : namespace,
				widgetName : name,
				widgetEventPrefix : $[namespace][name].prototype.widgetEventPrefix || name,
				widgetBaseClass : fullName
			}, prototype);
		$.widget.bridge(name, $[namespace][name])
	};
	$.widget.bridge = function (name, object) {
		$.fn[name] = function (options) {
			var isMethodCall = typeof options === "string",
			args = Array.prototype.slice.call(arguments, 1),
			returnValue = this;
			options = !isMethodCall && args.length ? $.extend.apply(null, [true, options].concat(args)) : options;
			if (isMethodCall && options.charAt(0) === "_") {
				return returnValue
			}
			if (isMethodCall) {
				this.each(function () {
					var instance = $.data(this, name),
					methodValue = instance && $.isFunction(instance[options]) ? instance[options].apply(instance, args) : instance;
					if (methodValue !== instance && methodValue !== undefined) {
						returnValue = methodValue;
						return false
					}
				})
			} else {
				this.each(function () {
					var instance = $.data(this, name);
					if (instance) {
						instance.option(options || {})._init()
					} else {
						$.data(this, name, new object(options, this))
					}
				})
			}
			return returnValue
		}
	};
	$.Widget = function (options, element) {
		if (arguments.length) {
			this._createWidget(options, element)
		}
	};
	$.Widget.prototype = {
		widgetName : "widget",
		widgetEventPrefix : "",
		options : {
			disabled : false
		},
		_createWidget : function (options, element) {
			$.data(element, this.widgetName, this);
			this.element = $(element);
			this.options = $.extend(true, {}, this.options, this._getCreateOptions(), options);
			var self = this;
			this.element.bind("remove." + this.widgetName, function () {
				self.destroy()
			});
			this._create();
			this._trigger("create");
			this._init()
		},
		_getCreateOptions : function () {
			return $.metadata && $.metadata.get(this.element[0])[this.widgetName]
		},
		_create : function () {},
		_init : function () {},
		destroy : function () {
			this.element.unbind("." + this.widgetName).removeData(this.widgetName);
			this.widget().unbind("." + this.widgetName).removeAttr("aria-disabled").removeClass(this.widgetBaseClass + "-disabled ui-state-disabled")
		},
		widget : function () {
			return this.element
		},
		option : function (key, value) {
			var options = key;
			if (arguments.length === 0) {
				return $.extend({}, this.options)
			}
			if (typeof key === "string") {
				if (value === undefined) {
					return this.options[key]
				}
				options = {};
				options[key] = value
			}
			this._setOptions(options);
			return this
		},
		_setOptions : function (options) {
			var self = this;
			$.each(options, function (key, value) {
				self._setOption(key, value)
			});
			return this
		},
		_setOption : function (key, value) {
			this.options[key] = value;
			if (key === "disabled") {
				this.widget()[value ? "addClass" : "removeClass"](this.widgetBaseClass + "-disabled ui-state-disabled").attr("aria-disabled", value)
			}
			return this
		},
		enable : function () {
			return this._setOption("disabled", false)
		},
		disable : function () {
			return this._setOption("disabled", true)
		},
		_trigger : function (type, event, data) {
			var callback = this.options[type];
			event = $.Event(event);
			event.type = (type === this.widgetEventPrefix ? type : this.widgetEventPrefix + type).toLowerCase();
			data = data || {};
			if (event.originalEvent) {
				for (var i = $.event.props.length, prop; i; ) {
					prop = $.event.props[--i];
					event[prop] = event.originalEvent[prop]
				}
			}
			this.element.trigger(event, data);
			return !($.isFunction(callback) && callback.call(this.element[0], event, data) === false || event.isDefaultPrevented())
		}
	}
})(jQuery);
/*
 * jQuery UI Mouse 1.8.12
 *
 * Copyright 2011, AUTHORS.txt (http://jqueryui.com/about)
 * Dual licensed under the MIT or GPL Version 2 licenses.
 * http://jquery.org/license
 *
 * http://docs.jquery.com/UI/Mouse
 *
 * Depends:
 *	jquery.ui.widget.js
 */
(function ($, undefined) {
	$.widget("ui.mouse", {
		options : {
			cancel : ":input,option",
			distance : 1,
			delay : 0
		},
		_mouseInit : function () {
			var self = this;
			this.element.bind("mousedown." + this.widgetName, function (event) {
				return self._mouseDown(event)
			}).bind("click." + this.widgetName, function (event) {
				if (true === $.data(event.target, self.widgetName + ".preventClickEvent")) {
					$.removeData(event.target, self.widgetName + ".preventClickEvent");
					event.stopImmediatePropagation();
					return false
				}
			});
			this.started = false
		},
		_mouseDestroy : function () {
			this.element.unbind("." + this.widgetName)
		},
		_mouseDown : function (event) {
			event.originalEvent = event.originalEvent || {};
			if (event.originalEvent.mouseHandled) {
				return
			}
			(this._mouseStarted && this._mouseUp(event));
			this._mouseDownEvent = event;
			var self = this,
			btnIsLeft = (event.which == 1),
			elIsCancel = (typeof this.options.cancel == "string" ? $(event.target).parents().add(event.target).filter(this.options.cancel).length : false);
			if (!btnIsLeft || elIsCancel || !this._mouseCapture(event)) {
				return true
			}
			this.mouseDelayMet = !this.options.delay;
			if (!this.mouseDelayMet) {
				this._mouseDelayTimer = setTimeout(function () {
						self.mouseDelayMet = true
					}, this.options.delay)
			}
			if (this._mouseDistanceMet(event) && this._mouseDelayMet(event)) {
				this._mouseStarted = (this._mouseStart(event) !== false);
				if (!this._mouseStarted) {
					event.preventDefault();
					return true
				}
			}
			if (true === $.data(event.target, this.widgetName + ".preventClickEvent")) {
				$.removeData(event.target, this.widgetName + ".preventClickEvent")
			}
			this._mouseMoveDelegate = function (event) {
				return self._mouseMove(event)
			};
			this._mouseUpDelegate = function (event) {
				return self._mouseUp(event)
			};
			$(document).bind("mousemove." + this.widgetName, this._mouseMoveDelegate).bind("mouseup." + this.widgetName, this._mouseUpDelegate);
			event.preventDefault();
			event.originalEvent.mouseHandled = true;
			return true
		},
		_mouseMove : function (event) {
			if ($.browser.msie && !(document.documentMode >= 9) && !event.button) {
				return this._mouseUp(event)
			}
			if (this._mouseStarted) {
				this._mouseDrag(event);
				return event.preventDefault()
			}
			if (this._mouseDistanceMet(event) && this._mouseDelayMet(event)) {
				this._mouseStarted = (this._mouseStart(this._mouseDownEvent, event) !== false);
				(this._mouseStarted ? this._mouseDrag(event) : this._mouseUp(event))
			}
			return !this._mouseStarted
		},
		_mouseUp : function (event) {
			$(document).unbind("mousemove." + this.widgetName, this._mouseMoveDelegate).unbind("mouseup." + this.widgetName, this._mouseUpDelegate);
			if (this._mouseStarted) {
				this._mouseStarted = false;
				if (event.target == this._mouseDownEvent.target) {
					$.data(event.target, this.widgetName + ".preventClickEvent", true)
				}
				this._mouseStop(event)
			}
			return false
		},
		_mouseDistanceMet : function (event) {
			return (Math.max(Math.abs(this._mouseDownEvent.pageX - event.pageX), Math.abs(this._mouseDownEvent.pageY - event.pageY)) >= this.options.distance)
		},
		_mouseDelayMet : function (event) {
			return this.mouseDelayMet
		},
		_mouseStart : function (event) {},
		_mouseDrag : function (event) {},
		_mouseStop : function (event) {},
		_mouseCapture : function (event) {
			return true
		}
	})
})(jQuery);
(function ($, undefined) {
	$.ui = $.ui || {};
	var horizontalPositions = /left|center|right/,
	verticalPositions = /top|center|bottom/,
	center = "center",
	_position = $.fn.position,
	_offset = $.fn.offset;
	$.fn.position = function (options) {
		if (!options || !options.of) {
			return _position.apply(this, arguments)
		}
		options = $.extend({}, options);
		var target = $(options.of),
		targetElem = target[0],
		collision = (options.collision || "flip").split(" "),
		offset = options.offset ? options.offset.split(" ") : [0, 0],
		targetWidth,
		targetHeight,
		basePosition;
		if (targetElem.nodeType === 9) {
			targetWidth = target.width();
			targetHeight = target.height();
			basePosition = {
				top : 0,
				left : 0
			}
		} else {
			if (targetElem.setTimeout) {
				targetWidth = target.width();
				targetHeight = target.height();
				basePosition = {
					top : target.scrollTop(),
					left : target.scrollLeft()
				}
			} else {
				if (targetElem.preventDefault) {
					options.at = "left top";
					targetWidth = targetHeight = 0;
					basePosition = {
						top : options.of.pageY,
						left : options.of.pageX
					}
				} else {
					targetWidth = target.outerWidth();
					targetHeight = target.outerHeight();
					basePosition = target.offset()
				}
			}
		}
		$.each(["my", "at"], function () {
			var pos = (options[this] || "").split(" ");
			if (pos.length === 1) {
				pos = horizontalPositions.test(pos[0]) ? pos.concat([center]) : verticalPositions.test(pos[0]) ? [center].concat(pos) : [center, center]
			}
			pos[0] = horizontalPositions.test(pos[0]) ? pos[0] : center;
			pos[1] = verticalPositions.test(pos[1]) ? pos[1] : center;
			options[this] = pos
		});
		if (collision.length === 1) {
			collision[1] = collision[0]
		}
		offset[0] = parseInt(offset[0], 10) || 0;
		if (offset.length === 1) {
			offset[1] = offset[0]
		}
		offset[1] = parseInt(offset[1], 10) || 0;
		if (options.at[0] === "right") {
			basePosition.left += targetWidth
		} else {
			if (options.at[0] === center) {
				basePosition.left += targetWidth / 2
			}
		}
		if (options.at[1] === "bottom") {
			basePosition.top += targetHeight
		} else {
			if (options.at[1] === center) {
				basePosition.top += targetHeight / 2
			}
		}
		basePosition.left += offset[0];
		basePosition.top += offset[1];
		return this.each(function () {
			var elem = $(this),
			elemWidth = elem.outerWidth(),
			elemHeight = elem.outerHeight(),
			marginLeft = parseInt($.curCSS(this, "marginLeft", true)) || 0,
			marginTop = parseInt($.curCSS(this, "marginTop", true)) || 0,
			collisionWidth = elemWidth + marginLeft + (parseInt($.curCSS(this, "marginRight", true)) || 0),
			collisionHeight = elemHeight + marginTop + (parseInt($.curCSS(this, "marginBottom", true)) || 0),
			position = $.extend({}, basePosition),
			collisionPosition;
			if (options.my[0] === "right") {
				position.left -= elemWidth
			} else {
				if (options.my[0] === center) {
					position.left -= elemWidth / 2
				}
			}
			if (options.my[1] === "bottom") {
				position.top -= elemHeight
			} else {
				if (options.my[1] === center) {
					position.top -= elemHeight / 2
				}
			}
			position.left = Math.round(position.left);
			position.top = Math.round(position.top);
			collisionPosition = {
				left : position.left - marginLeft,
				top : position.top - marginTop
			};
			$.each(["left", "top"], function (i, dir) {
				if ($.ui.position[collision[i]]) {
					$.ui.position[collision[i]][dir](position, {
						targetWidth : targetWidth,
						targetHeight : targetHeight,
						elemWidth : elemWidth,
						elemHeight : elemHeight,
						collisionPosition : collisionPosition,
						collisionWidth : collisionWidth,
						collisionHeight : collisionHeight,
						offset : offset,
						my : options.my,
						at : options.at
					})
				}
			});
			if ($.fn.bgiframe) {
				elem.bgiframe()
			}
			elem.offset($.extend(position, {
					using : options.using
				}))
		})
	};
	$.ui.position = {
		fit : {
			left : function (position, data) {
				var win = $(window),
				over = data.collisionPosition.left + data.collisionWidth - win.width() - win.scrollLeft();
				position.left = over > 0 ? position.left - over : Math.max(position.left - data.collisionPosition.left, position.left)
			},
			top : function (position, data) {
				var win = $(window),
				over = data.collisionPosition.top + data.collisionHeight - win.height() - win.scrollTop();
				position.top = over > 0 ? position.top - over : Math.max(position.top - data.collisionPosition.top, position.top)
			}
		},
		flip : {
			left : function (position, data) {
				if (data.at[0] === center) {
					return
				}
				var win = $(window),
				over = data.collisionPosition.left + data.collisionWidth - win.width() - win.scrollLeft(),
				myOffset = data.my[0] === "left" ? -data.elemWidth : data.my[0] === "right" ? data.elemWidth : 0,
				atOffset = data.at[0] === "left" ? data.targetWidth : -data.targetWidth,
				offset = -2 * data.offset[0];
				position.left += data.collisionPosition.left < 0 ? myOffset + atOffset + offset : over > 0 ? myOffset + atOffset + offset : 0
			},
			top : function (position, data) {
				if (data.at[1] === center) {
					return
				}
				var win = $(window),
				over = data.collisionPosition.top + data.collisionHeight - win.height() - win.scrollTop(),
				myOffset = data.my[1] === "top" ? -data.elemHeight : data.my[1] === "bottom" ? data.elemHeight : 0,
				atOffset = data.at[1] === "top" ? data.targetHeight : -data.targetHeight,
				offset = -2 * data.offset[1];
				position.top += data.collisionPosition.top < 0 ? myOffset + atOffset + offset : over > 0 ? myOffset + atOffset + offset : 0
			}
		}
	};
	if (!$.offset.setOffset) {
		$.offset.setOffset = function (elem, options) {
			if (/static/.test($.curCSS(elem, "position"))) {
				elem.style.position = "relative"
			}
			var curElem = $(elem),
			curOffset = curElem.offset(),
			curTop = parseInt($.curCSS(elem, "top", true), 10) || 0,
			curLeft = parseInt($.curCSS(elem, "left", true), 10) || 0,
			props = {
				top : (options.top - curOffset.top) + curTop,
				left : (options.left - curOffset.left) + curLeft
			};
			if ("using" in options) {
				options.using.call(elem, props)
			} else {
				curElem.css(props)
			}
		};
		$.fn.offset = function (options) {
			var elem = this[0];
			if (!elem || !elem.ownerDocument) {
				return null
			}
			if (options) {
				return this.each(function () {
					$.offset.setOffset(this, options)
				})
			}
			return _offset.call(this)
		}
	}
}
	(jQuery));
/*
 * Fluid Infusion v1.4
 *
 * Infusion is distributed under the Educational Community License 2.0 and new BSD licenses:
 * http://wiki.fluidproject.org/display/fluid/Fluid+Licensing
 *
 * For information on copyright, see the individual Infusion source code files:
 * https://github.com/fluid-project/infusion/
 */
var fluid_1_4 = fluid_1_4 || {};
var fluid = fluid || fluid_1_4;
(function ($, fluid) {
	fluid.version = "Infusion 1.4";
	fluid.environment = {
		fluid : fluid
	};
	var globalObject = window || {};
	var softFailure = [true];
	fluid.describeActivity = function () {
		return []
	};
	fluid.fail = function (message) {
		fluid.setLogging(true);
		fluid.log.apply(null, ["ASSERTION FAILED: "].concat(fluid.makeArray(arguments)).concat(fluid.describeActivity()));
		if (softFailure[0]) {
			throw new Error(message)
		} else {
			message.fail()
		}
	};
	fluid.pushSoftFailure = function (condition) {
		if (typeof(condition) === "boolean") {
			softFailure.unshift(condition)
		} else {
			if (condition === -1) {
				softFailure.shift()
			}
		}
	};
	fluid.notrycatch = false;
	fluid.tryCatch = function (tryfun, catchfun, finallyfun) {
		finallyfun = finallyfun || fluid.identity;
		if (fluid.notrycatch) {
			var togo = tryfun();
			finallyfun();
			return togo
		} else {
			try {
				return tryfun()
			} catch (e) {
				if (catchfun) {
					catchfun(e)
				} else {
					throw(e)
				}
			}
			finally {
				finallyfun()
			}
		}
	};
	fluid.expect = function (name, members, target) {
		fluid.transform(fluid.makeArray(members), function (key) {
			if (typeof target[key] === "undefined") {
				fluid.fail(name + " missing required parameter " + key)
			}
		})
	};
	var logging;
	fluid.isLogging = function () {
		return logging
	};
	fluid.setLogging = function (enabled) {
		if (typeof enabled === "boolean") {
			logging = enabled
		} else {
			logging = false
		}
	};
	fluid.applyHostFunction = function (obj, func, args) {
		if (func.apply) {
			func.apply(obj, args)
		} else {
			var applier = Function.prototype.bind.call(func, obj);
			applier.apply(obj, args)
		}
	};
	fluid.log = function (message) {
		if (logging) {
			var arg0 = fluid.renderTimestamp(new Date()) + ":  ";
			var args = [arg0].concat(fluid.makeArray(arguments));
			var str = args.join("");
			if (typeof(console) !== "undefined") {
				if (console.debug) {
					fluid.applyHostFunction(console, console.debug, args)
				} else {
					if (typeof(console.log) === "function") {
						fluid.applyHostFunction(console, console.log, args)
					} else {
						console.log(str)
					}
				}
			} else {
				if (typeof(YAHOO) !== "undefined") {
					YAHOO.log(str)
				} else {
					if (typeof(opera) !== "undefined") {
						opera.postError(str)
					}
				}
			}
		}
	};
	fluid.wrap = function (obj) {
		return ((!obj || obj.jquery) ? obj : $(obj))
	};
	fluid.unwrap = function (obj) {
		return obj && obj.jquery && obj.length === 1 ? obj[0] : obj
	};
	fluid.identity = function (arg) {
		return arg
	};
	fluid.isValue = function (value) {
		return value !== undefined && value !== null
	};
	fluid.isPrimitive = function (value) {
		var valueType = typeof(value);
		return !value || valueType === "string" || valueType === "boolean" || valueType === "number" || valueType === "function"
	};
	fluid.isDOMNode = function (obj) {
		return obj && typeof(obj.nodeType) === "number"
	};
	fluid.isArrayable = function (totest) {
		return totest && !fluid.isPrimitive(totest) && typeof(totest.length) === "number"
	};
	fluid.freshContainer = function (tocopy) {
		return fluid.isArrayable(tocopy) ? [] : {}
		
	};
	fluid.copy = function (tocopy) {
		if (fluid.isPrimitive(tocopy)) {
			return tocopy
		}
		return $.extend(true, fluid.freshContainer(tocopy), tocopy)
	};
	fluid.makeArray = function (arg) {
		if (arg === null || arg === undefined) {
			return []
		} else {
			return $.makeArray(arg)
		}
	};
	function transformInternal(source, togo, key, args) {
		var transit = source[key];
		for (var j = 0; j < args.length - 1; ++j) {
			transit = args[j + 1](transit, key)
		}
		togo[key] = transit
	}
	fluid.transform = function (source) {
		var togo = fluid.freshContainer(source);
		if (fluid.isArrayable(source)) {
			for (var i = 0; i < source.length; ++i) {
				transformInternal(source, togo, i, arguments)
			}
		} else {
			for (var key in source) {
				transformInternal(source, togo, key, arguments)
			}
		}
		return togo
	};
	fluid.each = function (source, func) {
		if (fluid.isArrayable(source)) {
			for (var i = 0; i < source.length; ++i) {
				func(source[i], i)
			}
		} else {
			for (var key in source) {
				func(source[key], key)
			}
		}
	};
	fluid.find = function (source, func, deflt) {
		var disp;
		if (fluid.isArrayable(source)) {
			for (var i = 0; i < source.length; ++i) {
				disp = func(source[i], i);
				if (disp !== undefined) {
					return disp
				}
			}
		} else {
			for (var key in source) {
				disp = func(source[key], key);
				if (disp !== undefined) {
					return disp
				}
			}
		}
		return deflt
	};
	fluid.accumulate = function (list, fn, arg) {
		for (var i = 0; i < list.length; ++i) {
			arg = fn(list[i], arg, i)
		}
		return arg
	};
	fluid.remove_if = function (source, fn) {
		if (fluid.isArrayable(source)) {
			for (var i = 0; i < source.length; ++i) {
				if (fn(source[i], i)) {
					source.splice(i, 1);
					--i
				}
			}
		} else {
			for (var key in source) {
				if (fn(source[key], key)) {
					delete source[key]
				}
			}
		}
		return source
	};
	fluid.filterKeys = function (toFilter, keys, exclude) {
		return fluid.remove_if($.extend({}, toFilter), function (value, key) {
			return exclude^($.inArray(key, keys) === -1)
		})
	};
	fluid.censorKeys = function (toCensor, keys) {
		return fluid.filterKeys(toCensor, keys, true)
	};
	fluid.keys = function (obj) {
		var togo = [];
		fluid.each(obj, function (value, key) {
			togo.push(key)
		});
		return togo
	};
	fluid.contains = function (obj, value) {
		return obj ? fluid.find(obj, function (thisValue, key) {
			if (value === thisValue) {
				return true
			}
		}) : undefined
	};
	fluid.keyForValue = function (obj, value) {
		return fluid.find(obj, function (thisValue, key) {
			if (value === thisValue) {
				return key
			}
		})
	};
	fluid.findKeyInObject = fluid.keyForValue;
	fluid.arrayToHash = function (array) {
		var togo = {};
		fluid.each(array, function (el) {
			togo[el] = true
		});
		return togo
	};
	fluid.clear = function (target) {
		if (fluid.isArrayable(target)) {
			target.length = 0
		} else {
			for (var i in target) {
				delete target[i]
			}
		}
	};
	fluid.model = {};
	fluid.VALUE = {
		type : "fluid.marker",
		value : "VALUE"
	};
	fluid.NO_VALUE = {
		type : "fluid.marker",
		value : "NO_VALUE"
	};
	fluid.EXPAND = {
		type : "fluid.marker",
		value : "EXPAND"
	};
	fluid.EXPAND_NOW = {
		type : "fluid.marker",
		value : "EXPAND_NOW"
	};
	fluid.isMarker = function (totest, type) {
		if (!totest || typeof(totest) !== "object" || totest.type !== "fluid.marker") {
			return false
		}
		if (!type) {
			return true
		}
		return totest === type
	};
	fluid.model.copyModel = function (target, source) {
		fluid.clear(target);
		$.extend(true, target, source)
	};
	fluid.model.parseEL = function (EL) {
		return EL === "" ? [] : String(EL).split(".")
	};
	fluid.model.composePath = function (prefix, suffix) {
		return prefix === "" ? suffix : (suffix === "" ? prefix : prefix + "." + suffix)
	};
	fluid.model.composeSegments = function () {
		return $.makeArray(arguments).join(".")
	};
	fluid.path = fluid.model.composeSegments;
	fluid.composePath = fluid.model.composePath;
	fluid.model.environmentStrategy = function (initEnvironment) {
		return {
			init : function () {
				var environment = initEnvironment;
				return function (root, segment, index) {
					var togo;
					if (environment && environment[segment]) {
						togo = environment[segment]
					}
					environment = null;
					return togo
				}
			}
		}
	};
	fluid.model.defaultCreatorStrategy = function (root, segment) {
		if (root[segment] === undefined) {
			root[segment] = {};
			return root[segment]
		}
	};
	fluid.model.defaultFetchStrategy = function (root, segment) {
		return segment === "" ? root : root[segment]
	};
	fluid.model.funcResolverStrategy = function (root, segment) {
		if (root.resolvePathSegment) {
			return root.resolvePathSegment(segment)
		}
	};
	fluid.model.applyStrategy = function (strategy, root, segment, index) {
		if (typeof(strategy) === "function") {
			return strategy(root, segment, index)
		} else {
			if (strategy && strategy.next) {
				return strategy.next(root, segment, index)
			}
		}
	};
	fluid.model.initStrategy = function (baseStrategy, index, oldStrategies) {
		return baseStrategy.init ? baseStrategy.init(oldStrategies ? oldStrategies[index] : undefined) : baseStrategy
	};
	fluid.model.makeTrundler = function (root, config, oldStrategies) {
		var that = {
			root : root,
			strategies : fluid.isArrayable(config) ? config : fluid.transform(config.strategies, function (strategy, index) {
				return fluid.model.initStrategy(strategy, index, oldStrategies)
			})
		};
		that.trundle = function (EL, uncess) {
			uncess = uncess || 0;
			var newThat = fluid.model.makeTrundler(that.root, config, that.strategies);
			newThat.segs = fluid.model.parseEL(EL);
			newThat.index = 0;
			newThat.step(newThat.segs.length - uncess);
			return newThat
		};
		that.next = function () {
			if (!that.root) {
				return
			}
			var accepted;
			for (var i = 0; i < that.strategies.length; ++i) {
				var value = fluid.model.applyStrategy(that.strategies[i], that.root, that.segs[that.index], that.index);
				if (accepted === undefined) {
					accepted = value
				}
			}
			if (accepted === fluid.NO_VALUE) {
				accepted = undefined
			}
			that.root = accepted;
			++that.index
		};
		that.step = function (limit) {
			for (var i = 0; i < limit; ++i) {
				that.next()
			}
			that.last = that.segs[that.index]
		};
		return that
	};
	fluid.model.defaultSetConfig = {
		strategies : [fluid.model.funcResolverStrategy, fluid.model.defaultFetchStrategy, fluid.model.defaultCreatorStrategy]
	};
	fluid.model.trundleImpl = function (trundler, EL, config, uncess) {
		if (typeof(EL) === "string") {
			trundler = trundler.trundle(EL, uncess)
		} else {
			var key = EL.type || "default";
			var resolver = config.resolvers[key];
			if (!resolver) {
				fluid.fail("Unable to find resolver of type " + key)
			}
			trundler = resolver(EL, trundler) || {};
			if (EL.path && trundler.trundle && trundler.root !== undefined) {
				trundler = fluid.model.trundleImpl(trundler, EL.path, config, uncess)
			}
		}
		return trundler
	};
	fluid.model.trundle = function (root, EL, config, uncess) {
		EL = EL || "";
		config = config || fluid.model.defaultGetConfig;
		var trundler = fluid.model.makeTrundler(root, config);
		return fluid.model.trundleImpl(trundler, EL, config, uncess)
	};
	fluid.model.getPenultimate = function (root, EL, config) {
		return fluid.model.trundle(root, EL, config, 1)
	};
	fluid.set = function (root, EL, newValue, config) {
		config = config || fluid.model.defaultSetConfig;
		var trundler = fluid.model.getPenultimate(root, EL, config);
		trundler.root[trundler.last] = newValue
	};
	fluid.model.defaultGetConfig = {
		strategies : [fluid.model.funcResolverStrategy, fluid.model.defaultFetchStrategy]
	};
	fluid.get = function (root, EL, config) {
		return fluid.model.trundle(root, EL, config).root
	};
	fluid.model.setBeanValue = fluid.set;
	fluid.model.getBeanValue = fluid.get;
	fluid.getGlobalValue = function (path, env) {
		if (path) {
			env = env || fluid.environment;
			var envFetcher = fluid.model.environmentStrategy(env);
			return fluid.get(globalObject, path, {
				strategies : [envFetcher].concat(fluid.model.defaultGetConfig.strategies)
			})
		}
	};
	fluid.invokeGlobalFunction = function (functionPath, args, environment) {
		var func = fluid.getGlobalValue(functionPath, environment);
		if (!func) {
			fluid.fail("Error invoking global function: " + functionPath + " could not be located")
		} else {
			return func.apply(null, args)
		}
	};
	fluid.registerGlobalFunction = function (functionPath, func, env) {
		env = env || fluid.environment;
		var envFetcher = fluid.model.environmentStrategy(env);
		fluid.set(globalObject, functionPath, func, {
			strategies : [envFetcher].concat(fluid.model.defaultSetConfig.strategies)
		})
	};
	fluid.setGlobalValue = fluid.registerGlobalFunction;
	fluid.registerNamespace = function (naimspace, env) {
		env = env || fluid.environment;
		var existing = fluid.getGlobalValue(naimspace, env);
		if (!existing) {
			existing = {};
			fluid.setGlobalValue(naimspace, existing, env)
		}
		return existing
	};
	fluid.dumpEl = fluid.identity;
	fluid.renderTimestamp = fluid.identity;
	fluid.registerNamespace("fluid.event");
	fluid.generateUniquePrefix = function () {
		return (Math.floor(Math.random() * 1000000000000)).toString(36) + "-"
	};
	var fluid_prefix = fluid.generateUniquePrefix();
	var fluid_guid = 1;
	fluid.allocateGuid = function () {
		return fluid_prefix + (fluid_guid++)
	};
	fluid.event.identifyListener = function (listener) {
		if (typeof(listener) === "string") {
			return listener
		}
		if (!listener.$$guid) {
			listener.$$guid = fluid.allocateGuid()
		}
		return listener.$$guid
	};
	fluid.event.mapPriority = function (priority, count) {
		return (priority === null || priority === undefined ? -count : (priority === "last" ? -Number.MAX_VALUE : (priority === "first" ? Number.MAX_VALUE : priority)))
	};
	fluid.event.listenerComparator = function (recA, recB) {
		return recB.priority - recA.priority
	};
	fluid.event.sortListeners = function (listeners) {
		var togo = [];
		fluid.each(listeners, function (listener) {
			togo.push(listener)
		});
		return togo.sort(fluid.event.listenerComparator)
	};
	fluid.event.getEventFirer = function (unicast, preventable) {
		var listeners = {};
		var sortedListeners = [];
		function fireToListeners(listeners, args, wrapper) {
			for (var i in listeners) {
				var lisrec = listeners[i];
				var listener = lisrec.listener;
				if (typeof(listener) === "string") {
					var listenerFunc = fluid.getGlobalValue(listener);
					if (!listenerFunc) {
						fluid.fail("Unable to look up name " + listener + " as a global function")
					} else {
						listener = lisrec.listener = listenerFunc
					}
				}
				if (lisrec.predicate && !lisrec.predicate(listener, args)) {
					continue
				}
				var value = fluid.tryCatch(function () {
						var ret = (wrapper ? wrapper(listener) : listener).apply(null, args);
						if (preventable && ret === false) {
							return false
						}
						if (unicast) {
							return ret
						}
					}, function (e) {
						fluid.log("FireEvent received exception " + e.message + " e " + e + " firing to listener " + i);
						throw(e)
					});
				if (value !== undefined) {
					return value
				}
			}
		}
		return {
			addListener : function (listener, namespace, predicate, priority) {
				if (!listener) {
					return
				}
				if (unicast) {
					namespace = "unicast"
				}
				if (!namespace) {
					namespace = fluid.event.identifyListener(listener)
				}
				listeners[namespace] = {
					listener : listener,
					predicate : predicate,
					priority : fluid.event.mapPriority(priority, sortedListeners.length)
				};
				sortedListeners = fluid.event.sortListeners(listeners)
			},
			removeListener : function (listener) {
				if (typeof(listener) === "string") {
					delete listeners[listener]
				} else {
					if (listener.$$guid) {
						delete listeners[listener.$$guid]
					}
				}
				sortedListeners = fluid.event.sortListeners(listeners)
			},
			fireToListeners : function (listeners, args, wrapper) {
				return fireToListeners(listeners, args, wrapper)
			},
			fire : function () {
				return fireToListeners(sortedListeners, arguments)
			}
		}
	};
	fluid.event.addListenerToFirer = function (firer, value, namespace) {
		if (fluid.isArrayable(value)) {
			for (var i = 0; i < value.length; ++i) {
				fluid.event.addListenerToFirer(firer, value[i], namespace)
			}
		} else {
			if (typeof(value) === "function" || typeof(value) === "string") {
				firer.addListener(value, namespace)
			} else {
				if (value && typeof(value) === "object") {
					firer.addListener(value.listener, namespace || value.namespace, value.predicate, value.priority)
				}
			}
		}
	};
	fluid.mergeListeners = function (that, events, listeners) {
		fluid.each(listeners, function (value, key) {
			var firer,
			namespace;
			if (key.charAt(0) === "{") {
				if (!fluid.expandOptions) {
					fluid.fail("fluid.expandOptions could not be loaded - please include FluidIoC.js in order to operate IoC-driven event with descriptor " + key)
				}
				firer = fluid.expandOptions(key, that)
			} else {
				var keydot = key.indexOf(".");
				if (keydot !== -1) {
					namespace = key.substring(keydot + 1);
					key = key.substring(0, keydot)
				}
				if (!events[key]) {
					fluid.fail("Listener registered for event " + key + " which is not defined for this component");
					events[key] = fluid.event.getEventFirer()
				}
				firer = events[key]
			}
			fluid.event.addListenerToFirer(firer, value, namespace)
		})
	};
	function initEvents(that, events, pass) {
		fluid.each(events, function (eventSpec, eventKey) {
			var isIoCEvent = eventSpec && (typeof(eventSpec) !== "string" || eventSpec.charAt(0) === "{");
			var event;
			if (isIoCEvent && pass === "IoC") {
				if (!fluid.event.resolveEvent) {
					fluid.fail("fluid.event.resolveEvent could not be loaded - please include FluidIoC.js in order to operate IoC-driven event with descriptor ", eventSpec)
				} else {
					event = fluid.event.resolveEvent(that, eventKey, eventSpec)
				}
			} else {
				if (pass === "flat") {
					event = fluid.event.getEventFirer(eventSpec === "unicast", eventSpec === "preventable")
				}
			}
			if (event) {
				that.events[eventKey] = event
			}
		})
	}
	fluid.instantiateFirers = function (that, options) {
		that.events = {};
		initEvents(that, options.events, "flat");
		initEvents(that, options.events, "IoC");
		var listeners = fluid.expandOptions ? fluid.expandOptions(options.listeners, that) : options.listeners;
		fluid.mergeListeners(that, that.events, listeners)
	};
	fluid.mergeListenersPolicy = function (target, source) {
		var togo = target || {};
		fluid.each(source, function (listeners, key) {
			togo[key] = fluid.makeArray(source[key]).concat(fluid.makeArray(listeners))
		});
		return togo
	};
	var defaultsStore = {};
	var resolveGradesImpl = function (gs, gradeNames) {
		gradeNames = fluid.makeArray(gradeNames);
		fluid.each(gradeNames, function (gradeName) {
			var options = fluid.rawDefaults(gradeName) || {};
			gs.gradeHash[gradeName] = true;
			gs.gradeChain.push(gradeName);
			gs.optionsChain.push(options);
			fluid.each(options.gradeNames, function (parent) {
				if (!gs.gradeHash[parent]) {
					resolveGradesImpl(gs, parent)
				}
			})
		});
		return gs
	};
	fluid.resolveGradeStructure = function (gradeNames) {
		var gradeStruct = {
			gradeChain : [],
			gradeHash : {},
			optionsChain : []
		};
		return resolveGradesImpl(gradeStruct, gradeNames)
	};
	fluid.lifecycleFunctions = {
		preInitFunction : true,
		postInitFunction : true,
		finalInitFunction : true
	};
	fluid.mergeLifecycleFunction = function (target, source) {
		fluid.event.addListenerToFirer(target, source);
		return target
	};
	fluid.rootMergePolicy = fluid.transform(fluid.lifecycleFunctions, function () {
			return fluid.mergeLifecycleFunction
		});
	fluid.makeLifecycleFirers = function () {
		return fluid.transform(fluid.lifecycleFunctions, function () {
			return fluid.event.getEventFirer()
		})
	};
	fluid.resolveGrade = function (defaults, gradeNames) {
		var mergeArgs = [defaults];
		if (gradeNames) {
			var gradeStruct = fluid.resolveGradeStructure(gradeNames);
			mergeArgs = gradeStruct.optionsChain.reverse().concat(mergeArgs).concat({
					gradeNames : gradeStruct.gradeChain
				})
		}
		mergeArgs = [fluid.rootMergePolicy, fluid.makeLifecycleFirers()].concat(mergeArgs);
		var mergedDefaults = fluid.merge.apply(null, mergeArgs);
		return mergedDefaults
	};
	fluid.resolveGradedOptions = function (componentName) {
		var defaults = fluid.rawDefaults(componentName);
		if (!defaults) {
			return defaults
		} else {
			return fluid.resolveGrade(defaults, defaults.gradeNames)
		}
	};
	fluid.rawDefaults = function (componentName, options) {
		if (options === undefined) {
			return defaultsStore[componentName]
		} else {
			defaultsStore[componentName] = options
		}
	};
	fluid.hasGrade = function (options, gradeName) {
		return !options || !options.gradeNames ? false : fluid.contains(options.gradeNames, gradeName)
	};
	fluid.defaults = function () {
		var offset = 0;
		if (typeof arguments[0] === "boolean") {
			offset = 1
		}
		var componentName = (offset === 0 ? "" : "*.global-") + arguments[offset];
		var options = arguments[offset + 1];
		if (options === undefined) {
			return fluid.resolveGradedOptions(componentName)
		} else {
			if (options && options.options) {
				fluid.fail('Probable error in options structure with option named "options" - perhaps you meant to write these options at top level in fluid.defaults?')
			}
			fluid.rawDefaults(componentName, options);
			if (fluid.hasGrade(options, "autoInit")) {
				fluid.makeComponent(componentName, fluid.resolveGradedOptions(componentName))
			}
		}
	};
	fluid.makeComponent = function (componentName, options) {
		if (!options.initFunction || !options.gradeNames) {
			fluid.fail("Cannot autoInit component " + componentName + " which does not have an initFunction and gradeName defined")
		}
		var creator = function () {
			return fluid.initComponent(componentName, arguments)
		};
		var existing = fluid.getGlobalValue(componentName);
		if (existing) {
			$.extend(creator, existing)
		}
		fluid.setGlobalValue(componentName, creator)
	};
	fluid.makeComponents = function (components, env) {
		fluid.each(components, function (value, key) {
			var options = {
				gradeNames : fluid.makeArray(value).concat(["autoInit"])
			};
			fluid.defaults(key, options)
		})
	};
	fluid.defaults("fluid.littleComponent", {
		initFunction : "fluid.initLittleComponent",
		argumentMap : {
			options : 0
		}
	});
	fluid.defaults("fluid.eventedComponent", {
		gradeNames : ["fluid.littleComponent"],
		mergePolicy : {
			listeners : "fluid.mergeListenersPolicy"
		}
	});
	fluid.preInitModelComponent = function (that) {
		that.model = that.options.model || {};
		that.applier = that.options.applier || fluid.makeChangeApplier(that.model, that.options.changeApplierOptions)
	};
	fluid.defaults("fluid.modelComponent", {
		gradeNames : ["fluid.littleComponent"],
		preInitFunction : {
			namespace : "preInitModelComponent",
			listener : "fluid.preInitModelComponent"
		},
		mergePolicy : {
			model : "preserve",
			applier : "nomerge"
		}
	});
	fluid.defaults("fluid.viewComponent", {
		gradeNames : ["fluid.littleComponent", "fluid.modelComponent", "fluid.eventedComponent"],
		initFunction : "fluid.initView",
		argumentMap : {
			container : 0,
			options : 1
		}
	});
	fluid.guardCircularity = function (seenIds, source, message1, message2) {
		if (source && source.id) {
			if (!seenIds[source.id]) {
				seenIds[source.id] = source
			} else {
				if (seenIds[source.id] === source) {
					fluid.fail("Circularity in options " + message1 + " - component with typename " + source.typeName + " and id " + source.id + " has already been seen" + message2)
				}
			}
		}
	};
	fluid.mergePolicyIs = function (policy, test) {
		return typeof(policy) === "string" && $.inArray(test, policy.split(/\s*,\s*/)) !== -1
	};
	function mergeImpl(policy, basePath, target, source, thisPolicy, rec) {
		if (typeof(thisPolicy) === "function") {
			thisPolicy.call(null, target, source);
			return target
		}
		if (fluid.mergePolicyIs(thisPolicy, "replace")) {
			fluid.clear(target)
		}
		fluid.guardCircularity(rec.seenIds, source, "merging", " when evaluating path " + basePath + ' - please protect components from merging using the "nomerge" merge policy');
		for (var name in source) {
			var path = (basePath ? basePath + "." : "") + name;
			var newPolicy = policy && typeof(policy) !== "string" ? policy[path] : policy;
			var thisTarget = target[name];
			var thisSource = source[name];
			var primitiveTarget = fluid.isPrimitive(thisTarget);
			if (thisSource !== undefined) {
				if (thisSource !== null && typeof(thisSource) === "object" && !fluid.isDOMNode(thisSource) && !thisSource.jquery && thisSource !== fluid.VALUE && !fluid.mergePolicyIs(newPolicy, "preserve") && !fluid.mergePolicyIs(newPolicy, "nomerge") && !fluid.mergePolicyIs(newPolicy, "noexpand")) {
					if (primitiveTarget) {
						target[name] = thisTarget = fluid.freshContainer(thisSource)
					}
					mergeImpl(policy, path, thisTarget, thisSource, newPolicy, rec)
				} else {
					if (typeof(newPolicy) === "function") {
						target[name] = newPolicy.call(null, thisTarget, thisSource, name)
					} else {
						if (!fluid.isValue(thisTarget) || !fluid.mergePolicyIs(newPolicy, "reverse")) {
							target[name] = fluid.isValue(thisTarget) && fluid.mergePolicyIs(newPolicy, "preserve") ? fluid.model.mergeModel(thisTarget, thisSource) : thisSource
						}
					}
				}
			}
		}
		return target
	}
	fluid.merge = function (policy, target) {
		var path = "";
		for (var i = 2; i < arguments.length; ++i) {
			var source = arguments[i];
			if (source !== null && source !== undefined) {
				mergeImpl(policy, path, target, source, policy ? policy[""] : null, {
					seenIds : {}
					
				})
			}
		}
		if (policy && typeof(policy) !== "string") {
			for (var key in policy) {
				var elrh = policy[key];
				if (typeof(elrh) === "string" && elrh !== "replace" && elrh !== "preserve") {
					var oldValue = fluid.get(target, key);
					if (oldValue === null || oldValue === undefined) {
						var value = fluid.get(target, elrh);
						fluid.set(target, key, value)
					}
				}
			}
		}
		return target
	};
	fluid.transformOptions = function (mergeArgs, transRec) {
		fluid.expect("Options transformation record", ["transformer", "config"], transRec);
		var transFunc = fluid.getGlobalValue(transRec.transformer);
		var togo = fluid.transform(mergeArgs, function (value, key) {
				return key === 0 ? value : transFunc.call(null, value, transRec.config)
			});
		return togo
	};
	fluid.lastTransformationRecord = function (extraArgs) {
		for (var i = extraArgs.length - 1; i >= 0; --i) {
			if (extraArgs[i] && extraArgs[i].transformOptions) {
				return extraArgs[i].transformOptions
			}
		}
	};
	fluid.mergeComponentOptions = function (that, componentName, userOptions, localOptions) {
		var defaults = fluid.defaults(componentName);
		var mergePolicy = $.extend({}, fluid.rootMergePolicy, defaults ? defaults.mergePolicy : {});
		var defaultGrades = defaults && defaults.gradeNames;
		var mergeArgs;
		if (!defaultGrades) {
			defaults = fluid.censorKeys(defaults, fluid.keys(fluid.lifecycleFunctions));
			mergeArgs = [mergePolicy, localOptions]
		} else {
			mergeArgs = [mergePolicy]
		}
		var extraArgs;
		if (fluid.expandComponentOptions) {
			extraArgs = fluid.expandComponentOptions(defaults, userOptions, that)
		} else {
			extraArgs = [defaults, userOptions]
		}
		var transRec = fluid.lastTransformationRecord(extraArgs);
		if (transRec) {
			extraArgs = fluid.transformOptions(extraArgs, transRec)
		}
		mergeArgs = mergeArgs.concat(extraArgs);
		that.options = fluid.merge.apply(null, mergeArgs)
	};
	fluid.COMPONENT_OPTIONS = {
		type : "fluid.marker",
		value : "COMPONENT_OPTIONS"
	};
	fluid.emptySubcomponent = function (options) {
		var that = {};
		options = $.makeArray(options);
		var empty = function () {};
		for (var i = 0; i < options.length; ++i) {
			that[options[i]] = empty
		}
		return that
	};
	fluid.computeNickName = function (typeName) {
		var segs = fluid.model.parseEL(typeName);
		return segs[segs.length - 1]
	};
	fluid.typeTag = function (name) {
		return name ? {
			typeName : name,
			id : fluid.allocateGuid()
		}
		 : null
	};
	fluid.typeFount = function (options) {
		var that = fluid.initLittleComponent("fluid.typeFount", options);
		return fluid.typeTag(that.options.targetTypeName)
	};
	fluid.initLittleComponent = function (name, options, localOptions) {
		var that = fluid.typeTag(name);
		that.nickName = options && options.nickName ? options.nickName : fluid.computeNickName(that.typeName);
		localOptions = localOptions || {
			gradeNames : "fluid.littleComponent"
		};
		localOptions = fluid.resolveGrade({}, localOptions.gradeNames);
		fluid.mergeComponentOptions(that, name, options, localOptions);
		that.options.preInitFunction.fire(that);
		if (fluid.hasGrade(that.options, "fluid.eventedComponent")) {
			fluid.instantiateFirers(that, that.options)
		}
		if (!fluid.hasGrade(that.options, "autoInit")) {
			fluid.clearLifecycleFunctions(that.options)
		}
		return that
	};
	fluid.clearLifecycleFunctions = function (options) {
		fluid.each(fluid.lifecycleFunctions, function (value, key) {
			delete options[key]
		});
		delete options.initFunction
	};
	fluid.diagnoseFailedView = function (componentName, that, options, args) {
		if (!that && fluid.hasGrade(options, "fluid.viewComponent")) {
			var container = fluid.wrap(args[1]);
			var message1 = "Instantiation of autoInit component with type " + componentName + " failed, since ";
			if (container.length === 0) {
				fluid.fail(message1 + 'selector "', args[1], '" did not match any markup in the document')
			} else {
				fluid.fail(message1 + " component creator function did not return a value")
			}
		}
	};
	fluid.initComponent = function (componentName, initArgs) {
		var options = fluid.defaults(componentName);
		if (!options.gradeNames) {
			fluid.fail("Cannot initialise component " + componentName + " which has no gradeName registered")
		}
		var args = [componentName].concat(fluid.makeArray(initArgs));
		var that = fluid.invokeGlobalFunction(options.initFunction, args);
		fluid.diagnoseFailedView(componentName, that, options, args);
		that.options.postInitFunction.fire(that);
		if (fluid.initDependents) {
			fluid.initDependents(that)
		}
		that.options.finalInitFunction.fire(that);
		fluid.clearLifecycleFunctions(that.options);
		return that.options.returnedPath ? fluid.get(that, that.options.returnedPath) : that
	};
	fluid.initSubcomponentImpl = function (that, entry, args) {
		var togo;
		if (typeof(entry) !== "function") {
			var entryType = typeof(entry) === "string" ? entry : entry.type;
			var globDef = fluid.defaults(true, entryType);
			fluid.merge("reverse", that.options, globDef);
			togo = entryType === "fluid.emptySubcomponent" ? fluid.emptySubcomponent(entry.options) : fluid.invokeGlobalFunction(entryType, args)
		} else {
			togo = entry.apply(null, args)
		}
		var returnedOptions = togo ? togo.returnedOptions : null;
		if (returnedOptions) {
			fluid.merge(that.options.mergePolicy, that.options, returnedOptions);
			if (returnedOptions.listeners) {
				fluid.mergeListeners(that, that.events, returnedOptions.listeners)
			}
		}
		return togo
	};
	fluid.initSubcomponents = function (that, className, args) {
		var entry = that.options[className];
		if (!entry) {
			return
		}
		var entries = $.makeArray(entry);
		var optindex = -1;
		var togo = [];
		args = $.makeArray(args);
		for (var i = 0; i < args.length; ++i) {
			if (args[i] === fluid.COMPONENT_OPTIONS) {
				optindex = i
			}
		}
		for (i = 0; i < entries.length; ++i) {
			entry = entries[i];
			if (optindex !== -1) {
				args[optindex] = entry.options
			}
			togo[i] = fluid.initSubcomponentImpl(that, entry, args)
		}
		return togo
	};
	fluid.initSubcomponent = function (that, className, args) {
		return fluid.initSubcomponents(that, className, args)[0]
	};
	fluid.checkTryCatchParameter = function () {
		var location = window.location || {
			search : "",
			protocol : "file:"
		};
		var GETParams = location.search.slice(1).split("&");
		return fluid.contains(GETParams, "notrycatch")
	};
	fluid.notrycatch = fluid.checkTryCatchParameter();
	fluid.container = function (containerSpec, fallible) {
		var container = fluid.wrap(containerSpec);
		if (fallible && (!container || container.length === 0)) {
			return null
		}
		if (!container || !container.jquery || container.length !== 1) {
			if (typeof(containerSpec) !== "string") {
				containerSpec = container.selector
			}
			var count = container.length !== undefined ? container.length : 0;
			fluid.fail((count > 1 ? "More than one (" + count + ") container elements were" : "No container element was") + " found for selector " + containerSpec)
		}
		if (!fluid.isDOMNode(container[0])) {
			fluid.fail("fluid.container was supplied a non-jQueryable element")
		}
		return container
	};
	fluid.createDomBinder = function (container, selectors) {
		var cache = {},
		that = {};
		function cacheKey(name, thisContainer) {
			return fluid.allocateSimpleId(thisContainer) + "-" + name
		}
		function record(name, thisContainer, result) {
			cache[cacheKey(name, thisContainer)] = result
		}
		that.locate = function (name, localContainer) {
			var selector,
			thisContainer,
			togo;
			selector = selectors[name];
			thisContainer = localContainer ? localContainer : container;
			if (!thisContainer) {
				fluid.fail("DOM binder invoked for selector " + name + " without container")
			}
			if (!selector) {
				return thisContainer
			}
			if (typeof(selector) === "function") {
				togo = $(selector.call(null, fluid.unwrap(thisContainer)))
			} else {
				togo = $(selector, thisContainer)
			}
			if (togo.get(0) === document) {
				togo = []
			}
			if (!togo.selector) {
				togo.selector = selector;
				togo.context = thisContainer
			}
			togo.selectorName = name;
			record(name, thisContainer, togo);
			return togo
		};
		that.fastLocate = function (name, localContainer) {
			var thisContainer = localContainer ? localContainer : container;
			var key = cacheKey(name, thisContainer);
			var togo = cache[key];
			return togo ? togo : that.locate(name, localContainer)
		};
		that.clear = function () {
			cache = {}
			
		};
		that.refresh = function (names, localContainer) {
			var thisContainer = localContainer ? localContainer : container;
			if (typeof names === "string") {
				names = [names]
			}
			if (thisContainer.length === undefined) {
				thisContainer = [thisContainer]
			}
			for (var i = 0; i < names.length; ++i) {
				for (var j = 0; j < thisContainer.length; ++j) {
					that.locate(names[i], thisContainer[j])
				}
			}
		};
		that.resolvePathSegment = that.locate;
		return that
	};
	fluid.expectFilledSelector = function (result, message) {
		if (result && result.length === 0 && result.jquery) {
			fluid.fail(message + ': selector "' + result.selector + '" with name ' + result.selectorName + " returned no results in context " + fluid.dumpEl(result.context))
		}
	};
	fluid.initView = function (componentName, container, userOptions, localOptions) {
		fluid.expectFilledSelector(container, 'Error instantiating component with name "' + componentName);
		container = fluid.container(container, true);
		if (!container) {
			return null
		}
		var that = fluid.initLittleComponent(componentName, userOptions, localOptions || {
				gradeNames : ["fluid.viewComponent"]
			});
		that.container = container;
		fluid.initDomBinder(that);
		return that
	};
	fluid.initDomBinder = function (that) {
		that.dom = fluid.createDomBinder(that.container, that.options.selectors);
		that.locate = that.dom.locate
	};
	fluid.findAncestor = function (element, test) {
		element = fluid.unwrap(element);
		while (element) {
			if (test(element)) {
				return element
			}
			element = element.parentNode
		}
	};
	fluid.jById = function (id, dokkument) {
		dokkument = dokkument && dokkument.nodeType === 9 ? dokkument : document;
		var element = fluid.byId(id, dokkument);
		var togo = element ? $(element) : [];
		togo.selector = "#" + id;
		togo.context = dokkument;
		return togo
	};
	fluid.byId = function (id, dokkument) {
		dokkument = dokkument && dokkument.nodeType === 9 ? dokkument : document;
		var el = dokkument.getElementById(id);
		if (el) {
			if (el.id !== id) {
				fluid.fail("Problem in document structure - picked up element " + fluid.dumpEl(el) + " for id " + id + " without this id - most likely the element has a name which conflicts with this id")
			}
			return el
		} else {
			return null
		}
	};
	fluid.getId = function (element) {
		return fluid.unwrap(element).id
	};
	fluid.allocateSimpleId = function (element) {
		var simpleId = "fluid-id-" + fluid.allocateGuid();
		if (!element) {
			return simpleId
		}
		element = fluid.unwrap(element);
		if (!element.id) {
			element.id = simpleId
		}
		return element.id
	};
	fluid.stringToRegExp = function (str, flags) {
		return new RegExp(str.replace(/[\-\[\]{}()*+?.,\\\^$|#\s]/g, "\\$&"), flags)
	};
	fluid.stringTemplate = function (template, values) {
		var newString = template;
		for (var key in values) {
			var re = fluid.stringToRegExp("%" + key, "g");
			newString = newString.replace(re, values[key])
		}
		return newString
	};
	fluid.messageResolver = function (options) {
		var that = fluid.initLittleComponent("fluid.messageResolver", options);
		that.messageBase = that.options.parseFunc(that.options.messageBase);
		that.lookup = function (messagecodes) {
			var resolved = fluid.messageResolver.resolveOne(that.messageBase, messagecodes);
			if (resolved === undefined) {
				return fluid.find(that.options.parents, function (parent) {
					return parent.lookup(messagecodes)
				})
			} else {
				return {
					template : resolved,
					resolveFunc : that.options.resolveFunc
				}
			}
		};
		that.resolve = function (messagecodes, args) {
			if (!messagecodes) {
				return "[No messagecodes provided]"
			}
			messagecodes = fluid.makeArray(messagecodes);
			var looked = that.lookup(messagecodes);
			return looked ? looked.resolveFunc(looked.template, args) : "[Message string for key " + messagecodes[0] + " not found]"
		};
		return that
	};
	fluid.defaults("fluid.messageResolver", {
		mergePolicy : {
			messageBase : "preserve"
		},
		resolveFunc : fluid.stringTemplate,
		parseFunc : fluid.identity,
		messageBase : {},
		parents : []
	});
	fluid.messageResolver.resolveOne = function (messageBase, messagecodes) {
		for (var i = 0; i < messagecodes.length; ++i) {
			var code = messagecodes[i];
			var message = messageBase[code];
			if (message !== undefined) {
				return message
			}
		}
	};
	fluid.messageLocator = function (messageBase, resolveFunc) {
		var resolver = fluid.messageResolver({
				messageBase : messageBase,
				resolveFunc : resolveFunc
			});
		return function (messagecodes, args) {
			return resolver.resolve(messagecodes, args)
		}
	}
})(jQuery, fluid_1_4);
var fluid_1_4 = fluid_1_4 || {};
(function ($, fluid) {
	var NAMESPACE_KEY = "fluid-scoped-data";
	fluid.getScopedData = function (target, key) {
		var data = $(target).data(NAMESPACE_KEY);
		return data ? data[key] : undefined
	};
	fluid.setScopedData = function (target, key, value) {
		$(target).each(function () {
			var data = $.data(this, NAMESPACE_KEY) || {};
			data[key] = value;
			$.data(this, NAMESPACE_KEY, data)
		})
	};
	var lastFocusedElement = null;
	$(document).bind("focusin", function (event) {
		lastFocusedElement = event.target
	});
	fluid.getLastFocusedElement = function () {
		return lastFocusedElement
	};
	var ENABLEMENT_KEY = "enablement";
	fluid.enabled = function (target, state) {
		target = $(target);
		if (state === undefined) {
			return fluid.getScopedData(target, ENABLEMENT_KEY) !== false
		} else {
			$("*", target).add(target).each(function () {
				if (fluid.getScopedData(this, ENABLEMENT_KEY) !== undefined) {
					fluid.setScopedData(this, ENABLEMENT_KEY, state)
				} else {
					if (/select|textarea|input/i.test(this.nodeName)) {
						$(this).prop("disabled", !state)
					}
				}
			});
			fluid.setScopedData(target, ENABLEMENT_KEY, state)
		}
	};
	fluid.initEnablement = function (target) {
		fluid.setScopedData(target, ENABLEMENT_KEY, true)
	};
	function applyOp(node, func) {
		node = $(node);
		node.trigger("fluid-" + func);
		node[func]()
	}
	$.each(["focus", "blur"], function (i, name) {
		fluid[name] = function (elem) {
			applyOp(elem, name)
		}
	})
})(jQuery, fluid_1_4);
var fluid_1_4 = fluid_1_4 || {};
(function ($, fluid) {
	fluid.dom = fluid.dom || {};
	var getNextNode = function (iterator) {
		if (iterator.node.firstChild) {
			iterator.node = iterator.node.firstChild;
			iterator.depth += 1;
			return iterator
		}
		while (iterator.node) {
			if (iterator.node.nextSibling) {
				iterator.node = iterator.node.nextSibling;
				return iterator
			}
			iterator.node = iterator.node.parentNode;
			iterator.depth -= 1
		}
		return iterator
	};
	fluid.dom.iterateDom = function (node, acceptor, allNodes) {
		var currentNode = {
			node : node,
			depth : 0
		};
		var prevNode = node;
		var condition;
		while (currentNode.node !== null && currentNode.depth >= 0 && currentNode.depth < fluid.dom.iterateDom.DOM_BAIL_DEPTH) {
			condition = null;
			if (currentNode.node.nodeType === 1 || allNodes) {
				condition = acceptor(currentNode.node, currentNode.depth)
			}
			if (condition) {
				if (condition === "delete") {
					currentNode.node.parentNode.removeChild(currentNode.node);
					currentNode.node = prevNode
				} else {
					if (condition === "stop") {
						return currentNode.node
					}
				}
			}
			prevNode = currentNode.node;
			currentNode = getNextNode(currentNode)
		}
	};
	fluid.dom.iterateDom.DOM_BAIL_DEPTH = 256;
	fluid.dom.isContainer = function (container, containee) {
		for (; containee; containee = containee.parentNode) {
			if (container === containee) {
				return true
			}
		}
		return false
	};
	fluid.dom.getElementText = function (element) {
		var nodes = element.childNodes;
		var text = "";
		for (var i = 0; i < nodes.length; ++i) {
			var child = nodes[i];
			if (child.nodeType === 3) {
				text = text + child.nodeValue
			}
		}
		return text
	}
})(jQuery, fluid_1_4);
fluid_1_4 = fluid_1_4 || {};
(function ($, fluid) {
	var unUnicode = /(\\u[\dabcdef]{4}|\\x[\dabcdef]{2})/g;
	fluid.unescapeProperties = function (string) {
		string = string.replace(unUnicode, function (match) {
				var code = match.substring(2);
				var parsed = parseInt(code, 16);
				return String.fromCharCode(parsed)
			});
		var pos = 0;
		while (true) {
			var backpos = string.indexOf("\\", pos);
			if (backpos === -1) {
				break
			}
			if (backpos === string.length - 1) {
				return [string.substring(0, string.length - 1), true]
			}
			var replace = string.charAt(backpos + 1);
			if (replace === "n") {
				replace = "\n"
			}
			if (replace === "r") {
				replace = "\r"
			}
			if (replace === "t") {
				replace = "\t"
			}
			string = string.substring(0, backpos) + replace + string.substring(backpos + 2);
			pos = backpos + 1
		}
		return [string, false]
	};
	var breakPos = /[^\\][\s:=]/;
	fluid.parseJavaProperties = function (text) {
		var togo = {};
		text = text.replace(/\r\n/g, "\n");
		text = text.replace(/\r/g, "\n");
		lines = text.split("\n");
		var contin,
		key,
		valueComp,
		valueRaw,
		valueEsc;
		for (var i = 0; i < lines.length; ++i) {
			var line = $.trim(lines[i]);
			if (!line || line.charAt(0) === "#" || line.charAt(0) === "!") {
				continue
			}
			if (!contin) {
				valueComp = "";
				var breakpos = line.search(breakPos);
				if (breakpos === -1) {
					key = line;
					valueRaw = ""
				} else {
					key = $.trim(line.substring(0, breakpos + 1));
					valueRaw = $.trim(line.substring(breakpos + 2));
					if (valueRaw.charAt(0) === ":" || valueRaw.charAt(0) === "=") {
						valueRaw = $.trim(valueRaw.substring(1))
					}
				}
				key = fluid.unescapeProperties(key)[0];
				valueEsc = fluid.unescapeProperties(valueRaw)
			} else {
				valueEsc = fluid.unescapeProperties(line)
			}
			contin = valueEsc[1];
			if (!valueEsc[1]) {
				togo[key] = valueComp + valueEsc[0]
			} else {
				valueComp += valueEsc[0]
			}
		}
		return togo
	};
	fluid.formatMessage = function (messageString, args) {
		if (!args) {
			return messageString
		}
		if (typeof(args) === "string") {
			args = [args]
		}
		for (var i = 0; i < args.length; ++i) {
			messageString = messageString.replace("{" + i + "}", args[i])
		}
		return messageString
	}
})(jQuery, fluid_1_4);
var fluid_1_4 = fluid_1_4 || {};
var fluid = fluid || fluid_1_4;
(function ($, fluid) {
	fluid.renderTimestamp = function (date) {
		var zeropad = function (num, width) {
			if (!width) {
				width = 2
			}
			var numstr = (num == undefined ? "" : num.toString());
			return "00000".substring(5 - width + numstr.length) + numstr
		};
		return zeropad(date.getHours()) + ":" + zeropad(date.getMinutes()) + ":" + zeropad(date.getSeconds()) + "." + zeropad(date.getMilliseconds(), 3)
	};
	fluid.detectStackStyle = function (e) {
		var style = "other";
		var stackStyle = {
			offset : 0
		};
		if (e.arguments) {
			style = "chrome"
		} else {
			if (typeof window !== "undefined" && window.opera && e.stacktrace) {
				style = "opera10"
			} else {
				if (e.stack) {
					style = "firefox";
					stackStyle.offset = e.stack.indexOf("Trace exception") === -1 ? 1 : 0
				} else {
					if (typeof window !== "undefined" && window.opera && !("stacktrace" in e)) {
						style = "opera"
					}
				}
			}
		}
		stackStyle.style = style;
		return stackStyle
	};
	fluid.obtainException = function () {
		try {
			throw new Error("Trace exception")
		} catch (e) {
			return e
		}
	};
	var stackStyle = fluid.detectStackStyle(fluid.obtainException());
	fluid.registerNamespace("fluid.exceptionDecoders");
	fluid.decodeStack = function () {
		if (stackStyle.style !== "firefox") {
			return null
		}
		var e = fluid.obtainException();
		return fluid.exceptionDecoders[stackStyle.style](e)
	};
	fluid.exceptionDecoders.firefox = function (e) {
		var lines = e.stack.replace(/(?:\n@:0)?\s+$/m, "").replace(/^\(/gm, "{anonymous}(").split("\n");
		return fluid.transform(lines, function (line) {
			var atind = line.indexOf("@");
			return atind === -1 ? [line] : [line.substring(atind + 1), line.substring(0, atind)]
		})
	};
	fluid.getCallerInfo = function (atDepth) {
		atDepth = (atDepth || 3) - stackStyle.offset;
		var stack = fluid.decodeStack();
		return stack ? stack[atDepth][0] : null
	};
	function generate(c, count) {
		var togo = "";
		for (var i = 0; i < count; ++i) {
			togo += c
		}
		return togo
	}
	function printImpl(obj, small, options) {
		var big = small + options.indentChars;
		if (obj === null) {
			return "null"
		} else {
			if (fluid.isPrimitive(obj)) {
				return JSON.stringify(obj)
			} else {
				var j = [];
				if (fluid.isArrayable(obj)) {
					if (obj.length === 0) {
						return "[]"
					}
					for (var i = 0; i < obj.length; ++i) {
						j[i] = printImpl(obj[i], big, options)
					}
					return "[\n" + big + j.join(",\n" + big) + "\n" + small + "]"
				} else {
					var i = 0;
					fluid.each(obj, function (value, key) {
						j[i++] = JSON.stringify(key) + ": " + printImpl(value, big, options)
					});
					return "{\n" + big + j.join(",\n" + big) + "\n" + small + "}"
				}
			}
		}
	}
	fluid.prettyPrintJSON = function (obj, options) {
		options = $.extend({
				indent : 4
			}, options);
		options.indentChars = generate(" ", options.indent);
		return printImpl(obj, "", options)
	};
	fluid.dumpEl = function (element) {
		var togo;
		if (!element) {
			return "null"
		}
		if (element.nodeType === 3 || element.nodeType === 8) {
			return "[data: " + element.data + "]"
		}
		if (element.nodeType === 9) {
			return "[document: location " + element.location + "]"
		}
		if (!element.nodeType && fluid.isArrayable(element)) {
			togo = "[";
			for (var i = 0; i < element.length; ++i) {
				togo += fluid.dumpEl(element[i]);
				if (i < element.length - 1) {
					togo += ", "
				}
			}
			return togo + "]"
		}
		element = $(element);
		togo = element.get(0).tagName;
		if (element.id) {
			togo += "#" + element.id
		}
		if (element.attr("class")) {
			togo += "." + element.attr("class")
		}
		return togo
	}
})(jQuery, fluid_1_4);
var fluid_1_4 = fluid_1_4 || {};
(function ($, fluid) {
	fluid.BINDING_ROOT_KEY = "fluid-binding-root";
	fluid.findData = function (elem, name) {
		while (elem) {
			var data = $.data(elem, name);
			if (data) {
				return data
			}
			elem = elem.parentNode
		}
	};
	fluid.bindFossils = function (node, data, fossils) {
		$.data(node, fluid.BINDING_ROOT_KEY, {
			data : data,
			fossils : fossils
		})
	};
	fluid.boundPathForNode = function (node, fossils) {
		node = fluid.unwrap(node);
		var key = node.name || node.id;
		var record = fossils[key];
		return record ? record.EL : null
	};
	fluid.findForm = function (node) {
		return fluid.findAncestor(node, function (element) {
			return element.nodeName.toLowerCase() === "form"
		})
	};
	fluid.value = function (nodeIn, newValue) {
		var node = fluid.unwrap(nodeIn);
		var multiple = false;
		if (node.nodeType === undefined && node.length > 1) {
			node = node[0];
			multiple = true
		}
		if ("input" !== node.nodeName.toLowerCase() || !/radio|checkbox/.test(node.type)) {
			return newValue === undefined ? $(node).val() : $(node).val(newValue)
		}
		var name = node.name;
		if (name === undefined) {
			fluid.fail("Cannot acquire value from node " + fluid.dumpEl(node) + " which does not have name attribute set")
		}
		var elements;
		if (multiple) {
			elements = nodeIn
		} else {
			elements = document.getElementsByName(name);
			var scope = fluid.findForm(node);
			elements = $.grep(elements, function (element) {
					if (element.name !== name) {
						return false
					}
					return !scope || fluid.dom.isContainer(scope, element)
				})
		}
		if (newValue !== undefined) {
			if (typeof(newValue) === "boolean") {
				newValue = (newValue ? "true" : "false")
			}
			$.each(elements, function () {
				this.checked = (newValue instanceof Array ? $.inArray(this.value, newValue) !== -1 : newValue === this.value)
			})
		} else {
			var checked = $.map(elements, function (element) {
					return element.checked ? element.value : null
				});
			return node.type === "radio" ? checked[0] : checked
		}
	};
	fluid.applyChange = function (node, newValue, applier) {
		node = fluid.unwrap(node);
		if (newValue === undefined) {
			newValue = fluid.value(node)
		}
		if (node.nodeType === undefined && node.length > 0) {
			node = node[0]
		}
		var root = fluid.findData(node, fluid.BINDING_ROOT_KEY);
		if (!root) {
			fluid.fail("Bound data could not be discovered in any node above " + fluid.dumpEl(node))
		}
		var name = node.name;
		var fossil = root.fossils[name];
		if (!fossil) {
			fluid.fail("No fossil discovered for name " + name + " in fossil record above " + fluid.dumpEl(node))
		}
		if (typeof(fossil.oldvalue) === "boolean") {
			newValue = newValue[0] ? true : false
		}
		var EL = root.fossils[name].EL;
		if (applier) {
			applier.fireChangeRequest({
				path : EL,
				value : newValue,
				source : node.id
			})
		} else {
			fluid.set(root.data, EL, newValue)
		}
	};
	fluid.pathUtil = {};
	var getPathSegmentImpl = function (accept, path, i) {
		var segment = null;
		if (accept) {
			segment = ""
		}
		var escaped = false;
		var limit = path.length;
		for (; i < limit; ++i) {
			var c = path.charAt(i);
			if (!escaped) {
				if (c === ".") {
					break
				} else {
					if (c === "\\") {
						escaped = true
					} else {
						if (segment !== null) {
							segment += c
						}
					}
				}
			} else {
				escaped = false;
				if (segment !== null) {
					accept += c
				}
			}
		}
		if (segment !== null) {
			accept[0] = segment
		}
		return i
	};
	var globalAccept = [];
	fluid.pathUtil.getPathSegment = function (path, i) {
		getPathSegmentImpl(globalAccept, path, i);
		return globalAccept[0]
	};
	fluid.pathUtil.getHeadPath = function (path) {
		return fluid.pathUtil.getPathSegment(path, 0)
	};
	fluid.pathUtil.getFromHeadPath = function (path) {
		var firstdot = getPathSegmentImpl(null, path, 0);
		return firstdot === path.length ? null : path.substring(firstdot + 1)
	};
	function lastDotIndex(path) {
		return path.lastIndexOf(".")
	}
	fluid.pathUtil.getToTailPath = function (path) {
		var lastdot = lastDotIndex(path);
		return lastdot === -1 ? null : path.substring(0, lastdot)
	};
	fluid.pathUtil.getTailPath = function (path) {
		var lastdot = lastDotIndex(path);
		return fluid.pathUtil.getPathSegment(path, lastdot + 1)
	};
	var composeSegment = function (prefix, toappend) {
		for (var i = 0; i < toappend.length; ++i) {
			var c = toappend.charAt(i);
			if (c === "." || c === "\\" || c === "}") {
				prefix += "\\"
			}
			prefix += c
		}
		return prefix
	};
	fluid.pathUtil.composePath = function (prefix, suffix) {
		if (prefix.length !== 0) {
			prefix += "."
		}
		return composeSegment(prefix, suffix)
	};
	fluid.pathUtil.matchPath = function (spec, path) {
		var togo = "";
		while (true) {
			if (!spec || path === "") {
				break
			}
			if (!path) {
				return null
			}
			var spechead = fluid.pathUtil.getHeadPath(spec);
			var pathhead = fluid.pathUtil.getHeadPath(path);
			if (spechead !== "*" && spechead !== pathhead) {
				return null
			}
			togo = fluid.pathUtil.composePath(togo, pathhead);
			spec = fluid.pathUtil.getFromHeadPath(spec);
			path = fluid.pathUtil.getFromHeadPath(path)
		}
		return togo
	};
	fluid.model.mergeModel = function (target, source, applier) {
		var copySource = fluid.copy(source);
		applier = applier || fluid.makeChangeApplier(source);
		if (!fluid.isPrimitive(target)) {
			applier.fireChangeRequest({
				type : "ADD",
				path : "",
				value : target
			})
		}
		applier.fireChangeRequest({
			type : "MERGE",
			path : "",
			value : copySource
		});
		return source
	};
	fluid.model.isNullChange = function (model, request, resolverGetConfig) {
		if (request.type === "ADD") {
			var existing = fluid.get(model, request.path, resolverGetConfig);
			if (existing === request.value) {
				return true
			}
		}
	};
	fluid.model.applyChangeRequest = function (model, request, resolverSetConfig) {
		var pen = fluid.model.getPenultimate(model, request.path, resolverSetConfig || fluid.model.defaultSetConfig);
		if (request.type === "ADD" || request.type === "MERGE") {
			if (request.path === "" || request.type === "MERGE") {
				if (request.type === "ADD") {
					fluid.clear(pen.root)
				}
				$.extend(true, request.path === "" ? pen.root : pen.root[pen.last], request.value)
			} else {
				pen.root[pen.last] = request.value
			}
		} else {
			if (request.type === "DELETE") {
				if (request.path === "") {
					fluid.clear(pen.root)
				} else {
					delete pen.root[pen.last]
				}
			}
		}
	};
	function bindRequestChange(that) {
		that.requestChange = function (path, value, type) {
			var changeRequest = {
				path : path,
				value : value,
				type : type
			};
			that.fireChangeRequest(changeRequest)
		}
	}
	fluid.makeChangeApplier = function (model, options) {
		options = options || {};
		var baseEvents = {
			guards : fluid.event.getEventFirer(false, true),
			postGuards : fluid.event.getEventFirer(false, true),
			modelChanged : fluid.event.getEventFirer(false, false)
		};
		var that = {
			model : model
		};
		function makeGuardWrapper(cullUnchanged) {
			if (!cullUnchanged) {
				return null
			}
			var togo = function (guard) {
				return function (model, changeRequest, internalApplier) {
					var oldRet = guard(model, changeRequest, internalApplier);
					if (oldRet === false) {
						return false
					} else {
						if (fluid.model.isNullChange(model, changeRequest)) {
							togo.culled = true;
							return false
						}
					}
				}
			};
			return togo
		}
		function wrapListener(listener, spec) {
			var pathSpec = spec;
			var transactional = false;
			var priority = Number.MAX_VALUE;
			if (typeof(spec) !== "string") {
				pathSpec = spec.path;
				transactional = spec.transactional;
				if (spec.priority !== undefined) {
					priority = spec.priority
				}
			} else {
				if (pathSpec.charAt(0) === "!") {
					transactional = true;
					pathSpec = pathSpec.substring(1)
				}
			}
			return function (changePath, fireSpec, accum) {
				var guid = fluid.event.identifyListener(listener);
				var exist = fireSpec.guids[guid];
				if (!exist) {
					var match = fluid.pathUtil.matchPath(pathSpec, changePath);
					if (match !== null) {
						var record = {
							changePath : changePath,
							pathSpec : pathSpec,
							listener : listener,
							priority : priority,
							transactional : transactional
						};
						if (accum) {
							record.accumulate = [accum]
						}
						fireSpec.guids[guid] = record;
						var collection = transactional ? "transListeners" : "listeners";
						fireSpec[collection].push(record);
						fireSpec.all.push(record)
					}
				} else {
					if (accum) {
						if (!exist.accumulate) {
							exist.accumulate = []
						}
						exist.accumulate.push(accum)
					}
				}
			}
		}
		function fireFromSpec(name, fireSpec, args, category, wrapper) {
			return baseEvents[name].fireToListeners(fireSpec[category], args, wrapper)
		}
		function fireComparator(recA, recB) {
			return recA.priority - recB.priority
		}
		function prepareFireEvent(name, changePath, fireSpec, accum) {
			baseEvents[name].fire(changePath, fireSpec, accum);
			fireSpec.all.sort(fireComparator);
			fireSpec.listeners.sort(fireComparator);
			fireSpec.transListeners.sort(fireComparator)
		}
		function makeFireSpec() {
			return {
				guids : {},
				all : [],
				listeners : [],
				transListeners : []
			}
		}
		function getFireSpec(name, changePath) {
			var fireSpec = makeFireSpec();
			prepareFireEvent(name, changePath, fireSpec);
			return fireSpec
		}
		function fireEvent(name, changePath, args, wrapper) {
			var fireSpec = getFireSpec(name, changePath);
			return fireFromSpec(name, fireSpec, args, "all", wrapper)
		}
		function adaptListener(that, name) {
			that[name] = {
				addListener : function (spec, listener, namespace) {
					baseEvents[name].addListener(wrapListener(listener, spec), namespace)
				},
				removeListener : function (listener) {
					baseEvents[name].removeListener(listener)
				}
			}
		}
		adaptListener(that, "guards");
		adaptListener(that, "postGuards");
		adaptListener(that, "modelChanged");
		function preFireChangeRequest(changeRequest) {
			if (!changeRequest.type) {
				changeRequest.type = "ADD"
			}
		}
		var bareApplier = {
			fireChangeRequest : function (changeRequest) {
				that.fireChangeRequest(changeRequest, true)
			}
		};
		bindRequestChange(bareApplier);
		that.fireChangeRequest = function (changeRequest, defeatGuards) {
			preFireChangeRequest(changeRequest);
			var guardFireSpec = defeatGuards ? null : getFireSpec("guards", changeRequest.path);
			if (guardFireSpec && guardFireSpec.transListeners.length > 0) {
				var ation = that.initiate();
				ation.fireChangeRequest(changeRequest, guardFireSpec);
				ation.commit()
			} else {
				if (!defeatGuards) {
					var prevent = fireFromSpec("guards", guardFireSpec, [model, changeRequest, bareApplier], "listeners");
					if (prevent === false) {
						return false
					}
				}
				var oldModel = model;
				if (!options.thin) {
					oldModel = {};
					fluid.model.copyModel(oldModel, model)
				}
				fluid.model.applyChangeRequest(model, changeRequest, options.resolverSetConfig);
				fireEvent("modelChanged", changeRequest.path, [model, oldModel, [changeRequest]])
			}
		};
		bindRequestChange(that);
		function fireAgglomerated(eventName, formName, changes, args, accpos) {
			var fireSpec = makeFireSpec();
			for (var i = 0; i < changes.length; ++i) {
				prepareFireEvent(eventName, changes[i].path, fireSpec, changes[i])
			}
			for (var j = 0; j < fireSpec[formName].length; ++j) {
				var spec = fireSpec[formName][j];
				if (accpos) {
					args[accpos] = spec.accumulate
				}
				var ret = spec.listener.apply(null, args);
				if (ret === false) {
					return false
				}
			}
		}
		that.initiate = function (newModel) {
			var cancelled = false;
			var changes = [];
			if (options.thin) {
				newModel = model
			} else {
				newModel = newModel || {};
				fluid.model.copyModel(newModel, model)
			}
			var internalApplier = {
				fireChangeRequest : function (changeRequest) {
					preFireChangeRequest(changeRequest);
					fluid.model.applyChangeRequest(newModel, changeRequest, options.resolverSetConfig);
					changes.push(changeRequest)
				}
			};
			bindRequestChange(internalApplier);
			var ation = {
				commit : function () {
					var oldModel;
					if (cancelled) {
						return false
					}
					var ret = fireAgglomerated("postGuards", "transListeners", changes, [newModel, null, internalApplier], 1);
					if (ret === false) {
						return false
					}
					if (options.thin) {
						oldModel = model
					} else {
						oldModel = {};
						fluid.model.copyModel(oldModel, model);
						fluid.clear(model);
						fluid.model.copyModel(model, newModel)
					}
					fireAgglomerated("modelChanged", "all", changes, [model, oldModel, null], 2)
				},
				fireChangeRequest : function (changeRequest) {
					preFireChangeRequest(changeRequest);
					if (options.cullUnchanged && fluid.model.isNullChange(model, changeRequest, options.resolverGetConfig)) {
						return
					}
					var wrapper = makeGuardWrapper(options.cullUnchanged);
					var prevent = fireEvent("guards", changeRequest.path, [newModel, changeRequest, internalApplier], wrapper);
					if (prevent === false && !(wrapper && wrapper.culled)) {
						cancelled = true
					}
					if (!cancelled) {
						if (!(wrapper && wrapper.culled)) {
							fluid.model.applyChangeRequest(newModel, changeRequest, options.resolverSetConfig);
							changes.push(changeRequest)
						}
					}
				}
			};
			bindRequestChange(ation);
			return ation
		};
		return that
	};
	fluid.makeSuperApplier = function () {
		var subAppliers = [];
		var that = {};
		that.addSubApplier = function (path, subApplier) {
			subAppliers.push({
				path : path,
				subApplier : subApplier
			})
		};
		that.fireChangeRequest = function (request) {
			for (var i = 0; i < subAppliers.length; ++i) {
				var path = subAppliers[i].path;
				if (request.path.indexOf(path) === 0) {
					var subpath = request.path.substring(path.length + 1);
					var subRequest = fluid.copy(request);
					subRequest.path = subpath;
					subAppliers[i].subApplier.fireChangeRequest(subRequest)
				}
			}
		};
		bindRequestChange(that);
		return that
	};
	fluid.attachModel = function (baseModel, path, model) {
		var segs = fluid.model.parseEL(path);
		for (var i = 0; i < segs.length - 1; ++i) {
			var seg = segs[i];
			var subModel = baseModel[seg];
			if (!subModel) {
				baseModel[seg] = subModel = {}
				
			}
			baseModel = subModel
		}
		baseModel[segs[segs.length - 1]] = model
	};
	fluid.assembleModel = function (modelSpec) {
		var model = {};
		var superApplier = fluid.makeSuperApplier();
		var togo = {
			model : model,
			applier : superApplier
		};
		for (var path in modelSpec) {
			var rec = modelSpec[path];
			fluid.attachModel(model, path, rec.model);
			if (rec.applier) {
				superApplier.addSubApplier(path, rec.applier)
			}
		}
		return togo
	}
})(jQuery, fluid_1_4);
var fluid_1_4 = fluid_1_4 || {};
var fluid = fluid || fluid_1_4;
(function ($, fluid) {
	fluid.thatistBridge = function (name, peer) {
		var togo = function (funcname) {
			var segs = funcname.split(".");
			var move = peer;
			for (var i = 0; i < segs.length; ++i) {
				move = move[segs[i]]
			}
			var args = [this];
			if (arguments.length === 2) {
				args = args.concat($.makeArray(arguments[1]))
			}
			var ret = move.apply(null, args);
			this.that = function () {
				return ret
			};
			var type = typeof(ret);
			return !ret || type === "string" || type === "number" || type === "boolean" || ret && ret.length !== undefined ? ret : this
		};
		$.fn[name] = togo;
		return togo
	};
	fluid.thatistBridge("fluid", fluid);
	fluid.thatistBridge("fluid_1_4", fluid_1_4);
	var normalizeTabindexName = function () {
		return $.browser.msie ? "tabIndex" : "tabindex"
	};
	var canHaveDefaultTabindex = function (elements) {
		if (elements.length <= 0) {
			return false
		}
		return $(elements[0]).is("a, input, button, select, area, textarea, object")
	};
	var getValue = function (elements) {
		if (elements.length <= 0) {
			return undefined
		}
		if (!fluid.tabindex.hasAttr(elements)) {
			return canHaveDefaultTabindex(elements) ? Number(0) : undefined
		}
		var value = elements.attr(normalizeTabindexName());
		return Number(value)
	};
	var setValue = function (elements, toIndex) {
		return elements.each(function (i, item) {
			$(item).attr(normalizeTabindexName(), toIndex)
		})
	};
	fluid.tabindex = function (target, toIndex) {
		target = $(target);
		if (toIndex !== null && toIndex !== undefined) {
			return setValue(target, toIndex)
		} else {
			return getValue(target)
		}
	};
	fluid.tabindex.remove = function (target) {
		target = $(target);
		return target.each(function (i, item) {
			$(item).removeAttr(normalizeTabindexName())
		})
	};
	fluid.tabindex.hasAttr = function (target) {
		target = $(target);
		if (target.length <= 0) {
			return false
		}
		var togo = target.map(function () {
				var attributeNode = this.getAttributeNode(normalizeTabindexName());
				return attributeNode ? attributeNode.specified : false
			});
		return togo.length === 1 ? togo[0] : togo
	};
	fluid.tabindex.has = function (target) {
		target = $(target);
		return fluid.tabindex.hasAttr(target) || canHaveDefaultTabindex(target)
	};
	fluid.a11y = $.a11y || {};
	fluid.a11y.orientation = {
		HORIZONTAL : 0,
		VERTICAL : 1,
		BOTH : 2
	};
	var UP_DOWN_KEYMAP = {
		next : $.ui.keyCode.DOWN,
		previous : $.ui.keyCode.UP
	};
	var LEFT_RIGHT_KEYMAP = {
		next : $.ui.keyCode.RIGHT,
		previous : $.ui.keyCode.LEFT
	};
	var unwrap = function (element) {
		return element.jquery ? element[0] : element
	};
	var makeElementsTabFocussable = function (elements) {
		elements.each(function (idx, item) {
			item = $(item);
			if (!item.fluid("tabindex.has") || item.fluid("tabindex") < 0) {
				item.fluid("tabindex", 0)
			}
		})
	};
	fluid.tabbable = function (target) {
		target = $(target);
		makeElementsTabFocussable(target)
	};
	var CONTEXT_KEY = "selectionContext";
	var NO_SELECTION = -32768;
	var cleanUpWhenLeavingContainer = function (selectionContext) {
		if (selectionContext.activeItemIndex !== NO_SELECTION) {
			if (selectionContext.options.onLeaveContainer) {
				selectionContext.options.onLeaveContainer(selectionContext.selectables[selectionContext.activeItemIndex])
			} else {
				if (selectionContext.options.onUnselect) {
					selectionContext.options.onUnselect(selectionContext.selectables[selectionContext.activeItemIndex])
				}
			}
		}
		if (!selectionContext.options.rememberSelectionState) {
			selectionContext.activeItemIndex = NO_SELECTION
		}
	};
	var drawSelection = function (elementToSelect, handler) {
		if (handler) {
			handler(elementToSelect)
		}
	};
	var eraseSelection = function (selectedElement, handler) {
		if (handler && selectedElement) {
			handler(selectedElement)
		}
	};
	var unselectElement = function (selectedElement, selectionContext) {
		eraseSelection(selectedElement, selectionContext.options.onUnselect)
	};
	var selectElement = function (elementToSelect, selectionContext) {
		unselectElement(selectionContext.selectedElement(), selectionContext);
		elementToSelect = unwrap(elementToSelect);
		var newIndex = selectionContext.selectables.index(elementToSelect);
		if (newIndex === -1) {
			return
		}
		selectionContext.activeItemIndex = newIndex;
		drawSelection(elementToSelect, selectionContext.options.onSelect)
	};
	var selectableFocusHandler = function (selectionContext) {
		return function (evt) {
			$(evt.target).fluid("tabindex", 0);
			selectElement(evt.target, selectionContext);
			return evt.stopPropagation()
		}
	};
	var selectableBlurHandler = function (selectionContext) {
		return function (evt) {
			$(evt.target).fluid("tabindex", selectionContext.options.selectablesTabindex);
			unselectElement(evt.target, selectionContext);
			return evt.stopPropagation()
		}
	};
	var reifyIndex = function (sc_that) {
		var elements = sc_that.selectables;
		if (sc_that.activeItemIndex >= elements.length) {
			sc_that.activeItemIndex = 0
		}
		if (sc_that.activeItemIndex < 0 && sc_that.activeItemIndex !== NO_SELECTION) {
			sc_that.activeItemIndex = elements.length - 1
		}
		if (sc_that.activeItemIndex >= 0) {
			fluid.focus(elements[sc_that.activeItemIndex])
		}
	};
	var prepareShift = function (selectionContext) {
		var selElm = selectionContext.selectedElement();
		if (selElm) {
			fluid.blur(selElm)
		}
		unselectElement(selectionContext.selectedElement(), selectionContext);
		if (selectionContext.activeItemIndex === NO_SELECTION) {
			selectionContext.activeItemIndex = -1
		}
	};
	var focusNextElement = function (selectionContext) {
		prepareShift(selectionContext);
		++selectionContext.activeItemIndex;
		reifyIndex(selectionContext)
	};
	var focusPreviousElement = function (selectionContext) {
		prepareShift(selectionContext);
		--selectionContext.activeItemIndex;
		reifyIndex(selectionContext)
	};
	var arrowKeyHandler = function (selectionContext, keyMap, userHandlers) {
		return function (evt) {
			if (evt.which === keyMap.next) {
				focusNextElement(selectionContext);
				evt.preventDefault()
			} else {
				if (evt.which === keyMap.previous) {
					focusPreviousElement(selectionContext);
					evt.preventDefault()
				}
			}
		}
	};
	var getKeyMapForDirection = function (direction) {
		var keyMap;
		if (direction === fluid.a11y.orientation.HORIZONTAL) {
			keyMap = LEFT_RIGHT_KEYMAP
		} else {
			if (direction === fluid.a11y.orientation.VERTICAL) {
				keyMap = UP_DOWN_KEYMAP
			}
		}
		return keyMap
	};
	var tabKeyHandler = function (selectionContext) {
		return function (evt) {
			if (evt.which !== $.ui.keyCode.TAB) {
				return
			}
			cleanUpWhenLeavingContainer(selectionContext);
			if (evt.shiftKey) {
				selectionContext.focusIsLeavingContainer = true
			}
		}
	};
	var containerFocusHandler = function (selectionContext) {
		return function (evt) {
			var shouldOrig = selectionContext.options.autoSelectFirstItem;
			var shouldSelect = typeof(shouldOrig) === "function" ? shouldOrig() : shouldOrig;
			if (selectionContext.focusIsLeavingContainer) {
				shouldSelect = false
			}
			if (shouldSelect && evt.target === selectionContext.container.get(0)) {
				if (selectionContext.activeItemIndex === NO_SELECTION) {
					selectionContext.activeItemIndex = 0
				}
				fluid.focus(selectionContext.selectables[selectionContext.activeItemIndex])
			}
			return evt.stopPropagation()
		}
	};
	var containerBlurHandler = function (selectionContext) {
		return function (evt) {
			selectionContext.focusIsLeavingContainer = false;
			return evt.stopPropagation()
		}
	};
	var makeElementsSelectable = function (container, defaults, userOptions) {
		var options = $.extend(true, {}, defaults, userOptions);
		var keyMap = getKeyMapForDirection(options.direction);
		var selectableElements = options.selectableElements ? options.selectableElements : container.find(options.selectableSelector);
		var that = {
			container : container,
			activeItemIndex : NO_SELECTION,
			selectables : selectableElements,
			focusIsLeavingContainer : false,
			options : options
		};
		that.selectablesUpdated = function (focusedItem) {
			if (typeof(that.options.selectablesTabindex) === "number") {
				that.selectables.fluid("tabindex", that.options.selectablesTabindex)
			}
			that.selectables.unbind("focus." + CONTEXT_KEY);
			that.selectables.unbind("blur." + CONTEXT_KEY);
			that.selectables.bind("focus." + CONTEXT_KEY, selectableFocusHandler(that));
			that.selectables.bind("blur." + CONTEXT_KEY, selectableBlurHandler(that));
			if (keyMap && that.options.noBubbleListeners) {
				that.selectables.unbind("keydown." + CONTEXT_KEY);
				that.selectables.bind("keydown." + CONTEXT_KEY, arrowKeyHandler(that, keyMap))
			}
			if (focusedItem) {
				selectElement(focusedItem, that)
			} else {
				reifyIndex(that)
			}
		};
		that.refresh = function () {
			if (!that.options.selectableSelector) {
				throw("Cannot refresh selectable context which was not initialised by a selector")
			}
			that.selectables = container.find(options.selectableSelector);
			that.selectablesUpdated()
		};
		that.selectedElement = function () {
			return that.activeItemIndex < 0 ? null : that.selectables[that.activeItemIndex]
		};
		if (keyMap && !that.options.noBubbleListeners) {
			container.keydown(arrowKeyHandler(that, keyMap))
		}
		container.keydown(tabKeyHandler(that));
		container.focus(containerFocusHandler(that));
		container.blur(containerBlurHandler(that));
		that.selectablesUpdated();
		return that
	};
	fluid.selectable = function (target, options) {
		target = $(target);
		var that = makeElementsSelectable(target, fluid.selectable.defaults, options);
		fluid.setScopedData(target, CONTEXT_KEY, that);
		return that
	};
	fluid.selectable.select = function (target, toSelect) {
		fluid.focus(toSelect)
	};
	fluid.selectable.selectNext = function (target) {
		target = $(target);
		focusNextElement(fluid.getScopedData(target, CONTEXT_KEY))
	};
	fluid.selectable.selectPrevious = function (target) {
		target = $(target);
		focusPreviousElement(fluid.getScopedData(target, CONTEXT_KEY))
	};
	fluid.selectable.currentSelection = function (target) {
		target = $(target);
		var that = fluid.getScopedData(target, CONTEXT_KEY);
		return $(that.selectedElement())
	};
	fluid.selectable.defaults = {
		direction : fluid.a11y.orientation.VERTICAL,
		selectablesTabindex : -1,
		autoSelectFirstItem : true,
		rememberSelectionState : true,
		selectableSelector : ".selectable",
		selectableElements : null,
		onSelect : null,
		onUnselect : null,
		onLeaveContainer : null
	};
	var checkForModifier = function (binding, evt) {
		if (!binding.modifier) {
			return true
		}
		var modifierKey = binding.modifier;
		var isCtrlKeyPresent = modifierKey && evt.ctrlKey;
		var isAltKeyPresent = modifierKey && evt.altKey;
		var isShiftKeyPresent = modifierKey && evt.shiftKey;
		return isCtrlKeyPresent || isAltKeyPresent || isShiftKeyPresent
	};
	var makeActivationHandler = function (binding) {
		return function (evt) {
			var target = evt.target;
			if (!fluid.enabled(evt.target)) {
				return
			}
			var code = evt.which ? evt.which : evt.keyCode;
			if (code === binding.key && binding.activateHandler && checkForModifier(binding, evt)) {
				var event = $.Event("fluid-activate");
				$(evt.target).trigger(event, [binding.activateHandler]);
				if (event.isDefaultPrevented()) {
					evt.preventDefault()
				}
			}
		}
	};
	var makeElementsActivatable = function (elements, onActivateHandler, defaultKeys, options) {
		var bindings = [];
		$(defaultKeys).each(function (index, key) {
			bindings.push({
				modifier : null,
				key : key,
				activateHandler : onActivateHandler
			})
		});
		if (options && options.additionalBindings) {
			bindings = bindings.concat(options.additionalBindings)
		}
		fluid.initEnablement(elements);
		for (var i = 0; i < bindings.length; ++i) {
			var binding = bindings[i];
			elements.keydown(makeActivationHandler(binding))
		}
		elements.bind("fluid-activate", function (evt, handler) {
			handler = handler || onActivateHandler;
			return handler ? handler(evt) : null
		})
	};
	fluid.activatable = function (target, fn, options) {
		target = $(target);
		makeElementsActivatable(target, fn, fluid.activatable.defaults.keys, options)
	};
	fluid.activate = function (target) {
		$(target).trigger("fluid-activate")
	};
	fluid.activatable.defaults = {
		keys : [$.ui.keyCode.ENTER, $.ui.keyCode.SPACE]
	}
})(jQuery, fluid_1_4);
var fluid_1_4 = fluid_1_4 || {};
(function ($, fluid) {
	fluid.defaults("fluid.ariaLabeller", {
		labelAttribute : "aria-label",
		liveRegionMarkup : '<div class="liveRegion fl-offScreen-hidden" aria-live="polite"></div>',
		liveRegionId : "fluid-ariaLabeller-liveRegion",
		invokers : {
			generateLiveElement : {
				funcName : "fluid.ariaLabeller.generateLiveElement",
				args : ["{ariaLabeller}"]
			}
		}
	});
	fluid.ariaLabeller = function (element, options) {
		var that = fluid.initView("fluid.ariaLabeller", element, options);
		fluid.initDependents(that);
		that.update = function (newOptions) {
			newOptions = newOptions || that.options;
			that.container.attr(that.options.labelAttribute, newOptions.text);
			if (newOptions.dynamicLabel) {
				var live = fluid.jById(that.options.liveRegionId);
				if (live.length === 0) {
					live = that.generateLiveElement()
				}
				live.text(newOptions.text)
			}
		};
		that.update();
		return that
	};
	fluid.ariaLabeller.generateLiveElement = function (that) {
		var liveEl = $(that.options.liveRegionMarkup);
		liveEl.prop("id", that.options.liveRegionId);
		$("body").append(liveEl);
		return liveEl
	};
	var LABEL_KEY = "aria-labelling";
	fluid.getAriaLabeller = function (element) {
		element = $(element);
		var that = fluid.getScopedData(element, LABEL_KEY);
		return that
	};
	fluid.updateAriaLabel = function (element, text, options) {
		options = $.extend({}, options || {}, {
				text : text
			});
		var that = fluid.getAriaLabeller(element);
		if (!that) {
			that = fluid.ariaLabeller(element, options);
			fluid.setScopedData(element, LABEL_KEY, that)
		} else {
			that.update(options)
		}
		return that
	};
	fluid.deadMansBlur = function (control, options) {
		var that = fluid.initLittleComponent("fluid.deadMansBlur", options);
		that.blurPending = false;
		that.lastCancel = 0;
		$(control).bind("focusout", function (event) {
			fluid.log("Starting blur timer for element " + fluid.dumpEl(event.target));
			var now = new Date().getTime();
			fluid.log("back delay: " + (now - that.lastCancel));
			if (now - that.lastCancel > that.options.backDelay) {
				that.blurPending = true
			}
			setTimeout(function () {
				if (that.blurPending) {
					that.options.handler(control)
				}
			}, that.options.delay)
		});
		that.canceller = function (event) {
			fluid.log("Cancellation through " + event.type + " on " + fluid.dumpEl(event.target));
			that.lastCancel = new Date().getTime();
			that.blurPending = false
		};
		fluid.each(that.options.exclusions, function (exclusion) {
			exclusion = $(exclusion);
			fluid.each(exclusion, function (excludeEl) {
				$(excludeEl).bind("focusin", that.canceller).bind("fluid-focus", that.canceller).click(that.canceller).mousedown(that.canceller)
			})
		});
		return that
	};
	fluid.defaults("fluid.deadMansBlur", {
		delay : 150,
		backDelay : 100
	})
})(jQuery, fluid_1_4);
var fluid_1_4 = fluid_1_4 || {};
(function ($, fluid) {
	var inCreationMarker = "__CURRENTLY_IN_CREATION__";
	fluid.isFireBreak = function (component) {
		return component.options && component.options["fluid.visitComponents.fireBreak"]
	};
	fluid.visitComponentChildren = function (that, visitor, options, up, down) {
		options = options || {};
		for (var name in that) {
			var component = that[name];
			if (!component || !component.typeName || (component.id && options.visited && options.visited[component.id])) {
				continue
			}
			if (options.visited) {
				options.visited[component.id] = true
			}
			if (visitor(component, name, options, up, down)) {
				return true
			}
			if (!fluid.isFireBreak(component) && !options.flat) {
				fluid.visitComponentChildren(component, visitor, options, up, down + 1)
			}
		}
	};
	var visitComponents = function (thatStack, visitor, options) {
		options = options || {
			visited : {},
			flat : true
		};
		var up = 0;
		for (var i = thatStack.length - 1; i >= 0; --i) {
			var that = thatStack[i];
			if (fluid.isFireBreak(that)) {
				return
			}
			if (that.typeName) {
				options.visited[that.id] = true;
				if (visitor(that, "", options, 0, 0)) {
					return
				}
			}
			if (fluid.visitComponentChildren(that, visitor, options, up, 1)) {
				return
			}
			++up
		}
	};
	function makeGingerStrategy(instantiator, that, thatStack) {
		return function (component, thisSeg) {
			var atval = component[thisSeg];
			if (atval === undefined) {
				var parentPath = instantiator.idToPath[component.id];
				atval = instantiator.pathToComponent[fluid.composePath(parentPath, thisSeg)];
				if (atval) {
					atval[inCreationMarker] = true
				}
			}
			if (atval !== undefined) {
				if (atval[inCreationMarker]) {
					fluid.fail("Component " + fluid.dumpThat(atval) + ' at path "' + thisSeg + '" of parent ' + fluid.dumpThat(component) + " cannot be used for lookup since it is still in creation. Please reorganise your dependencies so that they no longer contain circular references")
				}
			} else {
				if (fluid.get(component, fluid.path("options", "components", thisSeg, "type"))) {
					fluid.initDependent(component, thisSeg);
					atval = component[thisSeg]
				}
			}
			return atval
		}
	}
	fluid.dumpThat = function (that, instantiator) {
		return '{ typeName: "' + that.typeName + '" id: ' + that.id + "}"
	};
	fluid.dumpThatStack = function (thatStack, instantiator) {
		var togo = fluid.transform(thatStack, function (that) {
				var path = instantiator.idToPath[that.id];
				return fluid.dumpThat(that) + (path ? (" - path: " + path) : "")
			});
		return togo.join("\n")
	};
	fluid.describeActivity = function () {
		return fluid.threadLocal().activityStack || []
	};
	fluid.pushActivity = function (func, message) {
		if (!message) {
			return func()
		}
		var root = fluid.threadLocal();
		if (!root.activityStack) {
			root.activityStack = []
		}
		var frames = fluid.makeArray(message);
		frames.push("\n");
		frames.unshift("\n");
		root.activityStack = frames.concat(root.activityStack);
		return fluid.tryCatch(func, null, function () {
			root.activityStack = root.activityStack.slice(frames.length)
		})
	};
	fluid.wrapActivity = function (func, messageSpec) {
		return function () {
			var args = fluid.makeArray(arguments);
			var message = fluid.transform(fluid.makeArray(messageSpec), function (specEl) {
					if (specEl.indexOf("arguments.") === 0) {
						var el = specEl.substring("arguments.".length);
						return fluid.get(args, el)
					} else {
						return specEl
					}
				});
			return fluid.pushActivity(function () {
				return func.apply(null, args)
			}, message)
		}
	};
	var localRecordExpected = /arguments|options|container/;
	function makeStackFetcher(instantiator, parentThat, localRecord, expandOptions) {
		expandOptions = expandOptions || {};
		var thatStack = instantiator.getFullStack(parentThat);
		var fetchStrategies = [fluid.model.funcResolverStrategy, makeGingerStrategy(instantiator, parentThat, thatStack)];
		var fetcher = function (parsed) {
			var context = parsed.context;
			if (localRecord && localRecordExpected.test(context)) {
				var fetched = fluid.get(localRecord[context], parsed.path);
				return (context === "arguments" || expandOptions.direct) ? fetched : {
					marker : context === "options" ? fluid.EXPAND : fluid.EXPAND_NOW,
					value : fetched
				}
			}
			var foundComponent;
			visitComponents(thatStack, function (component, name, options, up, down) {
				if (context === name || context === component.typeName || context === component.nickName) {
					foundComponent = component;
					if (down > 1) {
						fluid.log("***WARNING: value resolution for context " + context + " found at depth " + down + ": this may not be supported in future")
					}
					return true
				}
				if (fluid.get(component, fluid.path("options", "components", context, "type")) && !component[context]) {
					foundComponent = fluid.get(component, context, {
							strategies : fetchStrategies
						});
					return true
				}
			});
			if (!foundComponent && parsed.path !== "") {
				var ref = fluid.renderContextReference(parsed);
				fluid.log("Failed to resolve reference " + ref + ": thatStack contains\n" + fluid.dumpThatStack(thatStack, instantiator));
				fluid.fail("Failed to resolve reference " + ref + " - could not match context with name " + context + " from component root of type " + thatStack[0].typeName, "\ninstantiator contents: ", instantiator)
			}
			return fluid.get(foundComponent, parsed.path, fetchStrategies)
		};
		return fetcher
	}
	function makeStackResolverOptions(instantiator, parentThat, localRecord, expandOptions) {
		return $.extend({}, fluid.defaults("fluid.resolveEnvironment"), {
			fetcher : makeStackFetcher(instantiator, parentThat, localRecord, expandOptions)
		})
	}
	fluid.instantiator = function (freeInstantiator) {
		var preThat = {
			options : {
				"fluid.visitComponents.fireBreak" : true
			},
			idToPath : {},
			pathToComponent : {},
			stackCount : 0,
			nickName : "instantiator"
		};
		var that = fluid.typeTag("fluid.instantiator");
		that = $.extend(that, preThat);
		that.stack = function (count) {
			return that.stackCount += count
		};
		that.getThatStack = function (component) {
			var path = that.idToPath[component.id] || "";
			var parsed = fluid.model.parseEL(path);
			var togo = fluid.transform(parsed, function (value, i) {
					var parentPath = fluid.model.composeSegments.apply(null, parsed.slice(0, i + 1));
					return that.pathToComponent[parentPath]
				});
			var root = that.pathToComponent[""];
			if (root) {
				togo.unshift(root)
			}
			return togo
		};
		that.getEnvironmentalStack = function () {
			var togo = [fluid.staticEnvironment];
			if (!freeInstantiator) {
				togo.push(fluid.threadLocal())
			}
			return togo
		};
		that.getFullStack = function (component) {
			var thatStack = component ? that.getThatStack(component) : [];
			return that.getEnvironmentalStack().concat(thatStack)
		};
		function recordComponent(component, path) {
			that.idToPath[component.id] = path;
			if (that.pathToComponent[path]) {
				fluid.fail("Error during instantiation - path " + path + " which has just created component " + fluid.dumpThat(component) + " has already been used for component " + fluid.dumpThat(that.pathToComponent[path]) + " - this is a circular instantiation or other oversight. Please clear the component using instantiator.clearComponent() before reusing the path.")
			}
			that.pathToComponent[path] = component
		}
		that.recordRoot = function (component) {
			if (component && component.id && !that.pathToComponent[""]) {
				recordComponent(component, "")
			}
		};
		that.pushUpcomingInstantiation = function (parent, name) {
			that.expectedParent = parent;
			that.expectedName = name
		};
		that.recordComponent = function (component) {
			if (that.expectedName) {
				that.recordKnownComponent(that.expectedParent, component, that.expectedName);
				delete that.expectedName;
				delete that.expectedParent
			} else {
				that.recordRoot(component)
			}
		};
		that.clearComponent = function (component, name, child, options, noModTree) {
			options = options || {
				visited : {},
				flat : true
			};
			child = child || component[name];
			fluid.visitComponentChildren(child, function (gchild, gchildname) {
				that.clearComponent(child, gchildname, null, options, noModTree)
			}, options);
			var path = that.idToPath[child.id];
			delete that.idToPath[child.id];
			delete that.pathToComponent[path];
			if (!noModTree) {
				delete component[name]
			}
		};
		that.recordKnownComponent = function (parent, component, name) {
			var parentPath = that.idToPath[parent.id] || "";
			var path = fluid.model.composePath(parentPath, name);
			recordComponent(component, path)
		};
		return that
	};
	fluid.freeInstantiator = fluid.instantiator(true);
	fluid.argMapToDemands = function (argMap) {
		var togo = [];
		fluid.each(argMap, function (value, key) {
			togo[value] = "{" + key + "}"
		});
		return togo
	};
	fluid.makePassArgsSpec = function (initArgs) {
		return fluid.transform(initArgs, function (arg, index) {
			return "{arguments}." + index
		})
	};
	function mergeToMergeAll(options) {
		if (options && options.mergeOptions) {
			options.mergeAllOptions = ["{options}"].concat(fluid.makeArray(options.mergeOptions))
		}
	}
	function upgradeMergeOptions(demandspec) {
		mergeToMergeAll(demandspec);
		if (demandspec.mergeAllOptions) {
			if (demandspec.options) {
				fluid.fail("demandspec ", demandspec, " is invalid - cannot specify literal options together with mergeOptions or mergeAllOptions")
			}
			demandspec.options = {
				mergeAllOptions : demandspec.mergeAllOptions
			}
		}
		if (demandspec.options) {
			delete demandspec.options.mergeOptions
		}
	}
	fluid.embodyDemands = function (instantiator, parentThat, demandspec, initArgs, options) {
		options = options || {};
		upgradeMergeOptions(demandspec);
		var oldOptions = fluid.get(options, "componentRecord.options");
		options.componentRecord = $.extend(true, {}, options.componentRecord, fluid.censorKeys(demandspec, ["args", "funcName", "registeredFrom"]));
		var mergeAllZero = fluid.get(options, "componentRecord.options.mergeAllOptions.0");
		if (mergeAllZero === "{options}") {
			fluid.set(options, "componentRecord.options.mergeAllOptions.0", oldOptions)
		}
		var demands = $.makeArray(demandspec.args);
		var upDefaults = fluid.defaults(demandspec.funcName);
		var argMap = upDefaults ? upDefaults.argumentMap : null;
		var inferMap = false;
		if (!argMap && (upDefaults || (options && options.componentRecord)) && !options.passArgs) {
			inferMap = true;
			if (demands.length < 2) {
				argMap = fluid.rawDefaults("fluid.littleComponent").argumentMap
			} else {
				argMap = {
					options : demands.length - 1
				}
			}
		}
		options = options || {};
		if (demands.length === 0) {
			if (options.componentRecord && argMap) {
				demands = fluid.argMapToDemands(argMap)
			} else {
				if (options.passArgs) {
					demands = fluid.makePassArgsSpec(initArgs)
				}
			}
		}
		var localRecord = $.extend({
				arguments : initArgs
			}, fluid.censorKeys(options.componentRecord, ["type"]));
		fluid.each(argMap, function (index, name) {
			if (initArgs.length > 0) {
				localRecord[name] = localRecord.arguments[index]
			}
			if (demandspec[name] !== undefined && localRecord[name] === undefined) {
				localRecord[name] = demandspec[name]
			}
		});
		mergeToMergeAll(localRecord.options);
		mergeToMergeAll(argMap && demands[argMap.options]);
		var upstreamLocalRecord = $.extend({}, localRecord);
		if (options.componentRecord.options !== undefined) {
			upstreamLocalRecord.options = options.componentRecord.options
		}
		var expandOptions = makeStackResolverOptions(instantiator, parentThat, localRecord);
		var args = [];
		if (demands) {
			for (var i = 0; i < demands.length; ++i) {
				var arg = demands[i];
				if (fluid.isMarker(arg) && arg.value === fluid.COMPONENT_OPTIONS.value) {
					arg = "{options}";
					if (inferMap) {
						argMap = {
							options : i
						}
					}
				}
				if (typeof(arg) === "string") {
					if (arg.charAt(0) === "@") {
						var argpos = arg.substring(1);
						arg = "{arguments}." + argpos
					}
				}
				if (!argMap || argMap.options !== i) {
					args[i] = fluid.expander.expandLight(arg, expandOptions)
				} else {
					if (arg && typeof(arg) === "object" && !arg.targetTypeName) {
						arg.targetTypeName = demandspec.funcName
					}
					args[i] = {
						marker : fluid.EXPAND,
						value : fluid.copy(arg),
						localRecord : upstreamLocalRecord
					}
				}
				if (args[i] && fluid.isMarker(args[i].marker, fluid.EXPAND_NOW)) {
					args[i] = fluid.expander.expandLight(args[i].value, expandOptions)
				}
			}
		} else {
			args = initArgs ? initArgs : []
		}
		var togo = {
			args : args,
			funcName : demandspec.funcName
		};
		return togo
	};
	var aliasTable = {};
	fluid.alias = function (demandingName, aliasName) {
		if (aliasName) {
			aliasTable[demandingName] = aliasName
		} else {
			return aliasTable[demandingName]
		}
	};
	var dependentStore = {};
	function searchDemands(demandingName, contextNames) {
		var exist = dependentStore[demandingName] || [];
		outer : for (var i = 0; i < exist.length; ++i) {
			var rec = exist[i];
			for (var j = 0; j < contextNames.length; ++j) {
				if (rec.contexts[j] !== contextNames[j]) {
					continue outer
				}
			}
			return rec.spec
		}
	}
	fluid.demands = function (demandingName, contextName, spec) {
		var contextNames = $.makeArray(contextName).sort();
		if (!spec) {
			return searchDemands(demandingName, contextNames)
		} else {
			if (spec.length) {
				spec = {
					args : spec
				}
			}
		}
		if (fluid.getCallerInfo) {
			var callerInfo = fluid.getCallerInfo(5);
			if (callerInfo) {
				spec.registeredFrom = callerInfo
			}
		}
		var exist = dependentStore[demandingName];
		if (!exist) {
			exist = [];
			dependentStore[demandingName] = exist
		}
		exist.push({
			contexts : contextNames,
			spec : spec
		})
	};
	fluid.compareDemands = function (speca, specb) {
		var p1 = speca.uncess - specb.uncess;
		return p1 === 0 ? specb.intersect - speca.intersect : p1
	};
	fluid.isDemandLogging = function (demandingNames) {
		return fluid.isLogging() && demandingNames[0] !== "fluid.threadLocal"
	};
	fluid.locateAllDemands = function (instantiator, parentThat, demandingNames) {
		var demandLogging = fluid.isDemandLogging(demandingNames);
		if (demandLogging) {
			fluid.log("Resolving demands for function names ", demandingNames, " in context of " + (parentThat ? "component " + parentThat.typeName : "no component"))
		}
		var contextNames = {};
		var visited = [];
		var thatStack = instantiator.getFullStack(parentThat);
		visitComponents(thatStack, function (component, xname, options, up, down) {
			contextNames[component.typeName] = true;
			visited.push(component)
		});
		if (demandLogging) {
			fluid.log("Components in scope for resolution:\n" + fluid.dumpThatStack(visited, instantiator))
		}
		var matches = [];
		for (var i = 0; i < demandingNames.length; ++i) {
			var rec = dependentStore[demandingNames[i]] || [];
			for (var j = 0; j < rec.length; ++j) {
				var spec = rec[j];
				var record = {
					spec : spec,
					intersect : 0,
					uncess : 0
				};
				for (var k = 0; k < spec.contexts.length; ++k) {
					record[contextNames[spec.contexts[k]] ? "intersect" : "uncess"] += 2
				}
				if (spec.contexts.length === 0) {
					record.intersect++
				}
				matches.push(record)
			}
		}
		matches.sort(fluid.compareDemands);
		return matches
	};
	fluid.locateDemands = function (instantiator, parentThat, demandingNames) {
		var matches = fluid.locateAllDemands(instantiator, parentThat, demandingNames);
		var demandspec = matches.length === 0 || matches[0].intersect === 0 ? null : matches[0].spec.spec;
		if (fluid.isDemandLogging(demandingNames)) {
			if (demandspec) {
				fluid.log("Located " + matches.length + " potential match" + (matches.length === 1 ? "" : "es") + ", selected best match with " + matches[0].intersect + " matched context names: ", demandspec)
			} else {
				fluid.log("No matches found for demands, using direct implementation")
			}
		}
		return demandspec
	};
	fluid.determineDemands = function (instantiator, parentThat, funcNames) {
		funcNames = $.makeArray(funcNames);
		var newFuncName = funcNames[0];
		var demandspec = fluid.locateDemands(instantiator, parentThat, funcNames) || {};
		if (demandspec.funcName) {
			newFuncName = demandspec.funcName
		}
		var aliasTo = fluid.alias(newFuncName);
		if (aliasTo) {
			newFuncName = aliasTo;
			fluid.log("Following redirect from function name " + newFuncName + " to " + aliasTo);
			var demandspec2 = fluid.locateDemands(instantiator, parentThat, [aliasTo]);
			if (demandspec2) {
				fluid.each(demandspec2, function (value, key) {
					if (localRecordExpected.test(key)) {
						fluid.fail("Error in demands block ", demandspec2, ' - content with key "' + key + '" is not supported since this demands block was resolved via an alias from "' + newFuncName + '"')
					}
				});
				if (demandspec2.funcName) {
					newFuncName = demandspec2.funcName;
					fluid.log('Followed final inner demands to function name "' + newFuncName + '"')
				}
			}
		}
		return fluid.merge(null, {
			funcName : newFuncName,
			args : fluid.makeArray(demandspec.args)
		}, fluid.censorKeys(demandspec, ["funcName", "args"]))
	};
	fluid.resolveDemands = function (instantiator, parentThat, funcNames, initArgs, options) {
		var demandspec = fluid.determineDemands(instantiator, parentThat, funcNames);
		return fluid.embodyDemands(instantiator, parentThat, demandspec, initArgs, options)
	};
	fluid.invoke = function (functionName, args, that, environment) {
		args = fluid.makeArray(args);
		return fluid.withInstantiator(that, function (instantiator) {
			var invokeSpec = fluid.resolveDemands(instantiator, that, functionName, args, {
					passArgs : true
				});
			return fluid.invokeGlobalFunction(invokeSpec.funcName, invokeSpec.args, environment)
		})
	};
	fluid.invoke = fluid.wrapActivity(fluid.invoke, ['    while invoking function with name "', "arguments.0", '" from component', "arguments.2"]);
	fluid.makeFreeInvoker = function (functionName, environment) {
		var demandSpec = fluid.determineDemands(fluid.freeInstantiator, null, functionName);
		return function () {
			var invokeSpec = fluid.embodyDemands(fluid.freeInstantiator, null, demandSpec, arguments, {
					passArgs : true
				});
			return fluid.invokeGlobalFunction(invokeSpec.funcName, invokeSpec.args, environment)
		}
	};
	fluid.makeInvoker = function (instantiator, that, demandspec, functionName, environment) {
		demandspec = demandspec || fluid.determineDemands(instantiator, that, functionName);
		return function () {
			var args = arguments;
			return fluid.pushActivity(function () {
				var invokeSpec = fluid.embodyDemands(instantiator, that, demandspec, args, {
						passArgs : true
					});
				return fluid.invokeGlobalFunction(invokeSpec.funcName, invokeSpec.args, environment)
			}, ["    while invoking invoker with name " + functionName + " on component", that])
		}
	};
	fluid.event.dispatchListener = function (instantiator, that, listener, eventName, eventSpec) {
		return function () {
			var demandspec = fluid.determineDemands(instantiator, that, eventName);
			if (demandspec.args.length === 0 && eventSpec.args) {
				demandspec.args = eventSpec.args
			}
			var resolved = fluid.embodyDemands(instantiator, that, demandspec, arguments, {
					passArgs : true,
					componentOptions : eventSpec
				});
			listener.apply(null, resolved.args)
		}
	};
	fluid.event.resolveEvent = function (that, eventName, eventSpec) {
		return fluid.withInstantiator(that, function (instantiator) {
			if (typeof(eventSpec) === "string") {
				var firer = fluid.expandOptions(eventSpec, that);
				if (!firer) {
					fluid.fail("Error in fluid.event.resolveEvent - context path " + eventSpec + " could not be looked up to a valid event firer")
				}
				return firer
			} else {
				var event = eventSpec.event;
				var origin;
				if (!event) {
					fluid.fail("Event specification for event with name " + eventName + " does not include a base event specification")
				}
				if (event.charAt(0) === "{") {
					origin = fluid.expandOptions(event, that)
				} else {
					origin = that.events[event]
				}
				if (!origin) {
					fluid.fail("Error in event specification - could not resolve base event reference " + event + " to an event firer")
				}
				var firer = {};
				fluid.each(["fire", "removeListener"], function (method) {
					firer[method] = function () {
						origin[method].apply(null, arguments)
					}
				});
				firer.addListener = function (listener, namespace, predicate, priority) {
					origin.addListener(fluid.event.dispatchListener(instantiator, that, listener, eventName, eventSpec), namespace, predicate, priority)
				};
				return firer
			}
		})
	};
	fluid.registerNamespace("fluid.expander");
	fluid.expander.preserveFromExpansion = function (options) {
		var preserve = {};
		var preserveList = fluid.arrayToHash(["mergePolicy", "mergeAllOptions", "components", "invokers", "events", "listeners", "transformOptions"]);
		fluid.each(options.mergePolicy, function (value, key) {
			if (fluid.mergePolicyIs(value, "noexpand")) {
				preserveList[key] = true
			}
		});
		fluid.each(preserveList, function (xvalue, path) {
			var pen = fluid.model.getPenultimate(options, path);
			var value = pen.root[pen.last];
			delete pen.root[pen.last];
			fluid.set(preserve, path, value)
		});
		return {
			restore : function (target) {
				fluid.each(preserveList, function (xvalue, path) {
					var preserved = fluid.get(preserve, path);
					if (preserved !== undefined) {
						fluid.set(target, path, preserved)
					}
				})
			}
		}
	};
	fluid.expandOptions = function (args, that, localRecord, outerExpandOptions) {
		if (!args) {
			return args
		}
		return fluid.withInstantiator(that, function (instantiator) {
			var expandOptions = makeStackResolverOptions(instantiator, that, localRecord, outerExpandOptions);
			expandOptions.noCopy = true;
			var pres;
			if (!fluid.isArrayable(args) && !fluid.isPrimitive(args)) {
				pres = fluid.expander.preserveFromExpansion(args)
			}
			var expanded = fluid.expander.expandLight(args, expandOptions);
			if (pres) {
				pres.restore(expanded)
			}
			return expanded
		})
	};
	fluid.locateTransformationRecord = function (that) {
		return fluid.withInstantiator(that, function (instantiator) {
			var matches = fluid.locateAllDemands(instantiator, that, ["fluid.transformOptions"]);
			return fluid.find(matches, function (match) {
				return match.uncess === 0 && fluid.contains(match.spec.contexts, that.typeName) ? match.spec.spec : undefined
			})
		})
	};
	fluid.hashToArray = function (hash) {
		var togo = [];
		fluid.each(hash, function (value, key) {
			togo.push(key)
		});
		return togo
	};
	fluid.localRecordExpected = ["type", "options", "arguments", "mergeOptions", "mergeAllOptions", "createOnEvent", "priority"];
	fluid.checkComponentRecord = function (defaults, localRecord) {
		var expected = fluid.arrayToHash(fluid.localRecordExpected);
		fluid.each(defaults.argumentMap, function (value, key) {
			expected[key] = true
		});
		fluid.each(localRecord, function (value, key) {
			if (!expected[key]) {
				fluid.fail('Probable error in subcomponent record - key "' + key + '" found, where the only legal options are ' + fluid.hashToArray(expected).join(", "))
			}
		})
	};
	fluid.expandComponentOptions = function (defaults, userOptions, that) {
		if (userOptions && userOptions.localRecord) {
			fluid.checkComponentRecord(defaults, userOptions.localRecord)
		}
		defaults = fluid.expandOptions(fluid.copy(defaults), that);
		var localRecord = {};
		if (userOptions && userOptions.marker === fluid.EXPAND) {
			var localOptions = fluid.get(userOptions, "localRecord.options");
			if (localOptions) {
				if (defaults && defaults.mergePolicy) {
					localOptions.mergePolicy = defaults.mergePolicy
				}
				localRecord.options = fluid.expandOptions(localOptions, that)
			}
			localRecord.arguments = fluid.get(userOptions, "localRecord.arguments");
			var toExpand = userOptions.value;
			userOptions = fluid.expandOptions(toExpand, that, localRecord, {
					direct : true
				})
		}
		localRecord.directOptions = userOptions;
		if (!localRecord.options) {
			localRecord.options = userOptions
		}
		var mergeOptions = (userOptions && userOptions.mergeAllOptions) || ["{directOptions}"];
		var togo = fluid.transform(mergeOptions, function (path) {
				return path === "{directOptions}" ? localRecord.directOptions : fluid.expandOptions(path, that, localRecord, {
					direct : true
				})
			});
		var transRec = fluid.locateTransformationRecord(that);
		if (transRec) {
			togo[0].transformOptions = transRec.options
		}
		return [defaults].concat(togo)
	};
	fluid.expandComponentOptions = fluid.wrapActivity(fluid.expandComponentOptions, ["    while expanding component options ", "arguments.1.value", " with record ", "arguments.1", " for component ", "arguments.2"]);
	fluid.initDependent = function (that, name, userInstantiator, directArgs) {
		if (!that || that[name]) {
			return
		}
		fluid.log('Beginning instantiation of component with name "' + name + '" as child of ' + fluid.dumpThat(that));
		directArgs = directArgs || [];
		var root = fluid.threadLocal();
		if (userInstantiator) {
			var existing = root["fluid.instantiator"];
			if (existing && existing !== userInstantiator) {
				fluid.fail("Error in initDependent: user instantiator supplied with id " + userInstantiator.id + " which differs from that for currently active instantiation with id " + existing.id)
			} else {
				root["fluid.instantiator"] = userInstantiator
			}
		}
		var component = that.options.components[name];
		fluid.withInstantiator(that, function (instantiator) {
			if (typeof(component) === "string") {
				that[name] = fluid.expandOptions([component], that)[0]
			} else {
				if (component.type) {
					var invokeSpec = fluid.resolveDemands(instantiator, that, [component.type, name], directArgs, {
							componentRecord : component
						});
					instantiator.pushUpcomingInstantiation(that, name);
					fluid.tryCatch(function () {
						that[inCreationMarker] = true;
						var instance = fluid.initSubcomponentImpl(that, {
								type : invokeSpec.funcName
							}, invokeSpec.args);
						var path = fluid.composePath(instantiator.idToPath[that.id] || "", name);
						var existing = instantiator.pathToComponent[path];
						if (existing && existing !== instance) {
							instantiator.clearComponent(that, name, existing, null, true)
						}
						if (instance && instance.typeName && instance.id && instance !== existing) {
							instantiator.recordKnownComponent(that, instance, name)
						}
						that[name] = instance
					}, null, function () {
						delete that[inCreationMarker];
						instantiator.pushUpcomingInstantiation()
					})
				} else {
					that[name] = component
				}
			}
		}, ['    while instantiating dependent component with name "' + name + '" with record ', component, " as child of ", that]);
		fluid.log('Finished instantiation of component with name "' + name + '" as child of ' + fluid.dumpThat(that))
	};
	fluid.withInstantiator = function (that, func, message) {
		var root = fluid.threadLocal();
		var instantiator = root["fluid.instantiator"];
		if (!instantiator) {
			instantiator = root["fluid.instantiator"] = fluid.instantiator()
		}
		return fluid.pushActivity(function () {
			return fluid.tryCatch(function () {
				if (that) {
					instantiator.recordComponent(that)
				}
				instantiator.stack(1);
				return func(instantiator)
			}, null, function () {
				var count = instantiator.stack(-1);
				if (count === 0) {
					delete root["fluid.instantiator"]
				}
			})
		}, message)
	};
	fluid.bindDeferredComponent = function (that, componentName, component, instantiator) {
		var events = fluid.makeArray(component.createOnEvent);
		fluid.each(events, function (eventName) {
			that.events[eventName].addListener(function () {
				if (that[componentName]) {
					instantiator.clearComponent(that, componentName)
				}
				fluid.initDependent(that, componentName, instantiator)
			}, null, null, component.priority)
		})
	};
	fluid.priorityForComponent = function (component) {
		return component.priority ? component.priority : (component.type === "fluid.typeFount" || fluid.hasGrade(fluid.defaults(component.type), "fluid.typeFount")) ? "first" : undefined
	};
	fluid.initDependents = function (that) {
		var options = that.options;
		var components = options.components || {};
		var componentSort = {};
		fluid.withInstantiator(that, function (instantiator) {
			fluid.each(components, function (component, name) {
				if (!component.createOnEvent) {
					var priority = fluid.priorityForComponent(component);
					componentSort[name] = {
						key : name,
						priority : fluid.event.mapPriority(priority, 0)
					}
				} else {
					fluid.bindDeferredComponent(that, name, component, instantiator)
				}
			});
			var componentList = fluid.event.sortListeners(componentSort);
			fluid.each(componentList, function (entry) {
				fluid.initDependent(that, entry.key)
			});
			var invokers = options.invokers || {};
			for (var name in invokers) {
				var invokerec = invokers[name];
				var funcName = typeof(invokerec) === "string" ? invokerec : null;
				that[name] = fluid.withInstantiator(that, function (instantiator) {
						fluid.log('Beginning instantiation of invoker with name "' + name + '" as child of ' + fluid.dumpThat(that));
						return fluid.makeInvoker(instantiator, that, funcName ? null : invokerec, funcName)
					}, ['    while instantiating invoker with name "' + name + '" with record ', invokerec, " as child of ", that]);
				fluid.log('Finished instantiation of invoker with name "' + name + '" as child of ' + fluid.dumpThat(that))
			}
		})
	};
	fluid.staticEnvironment = fluid.typeTag("fluid.staticEnvironment");
	fluid.staticEnvironment.environmentClass = fluid.typeTag("fluid.browser");
	fluid.demands("fluid.threadLocal", "fluid.browser", {
		funcName : "fluid.singleThreadLocal"
	});
	var singleThreadLocal = fluid.typeTag("fluid.dynamicEnvironment");
	fluid.singleThreadLocal = function () {
		return singleThreadLocal
	};
	fluid.threadLocal = function () {
		var demands = fluid.locateDemands(fluid.freeInstantiator, null, ["fluid.threadLocal"]);
		return fluid.invokeGlobalFunction(demands.funcName, arguments)
	};
	function applyLocalChange(applier, type, path, value) {
		var change = {
			type : type,
			path : path,
			value : value
		};
		applier.fireChangeRequest(change)
	}
	fluid.withEnvironment = function (envAdd, func, prefix) {
		prefix = prefix || "";
		var root = fluid.threadLocal();
		var applier = fluid.makeChangeApplier(root, {
				thin : true
			});
		return fluid.tryCatch(function () {
			for (var key in envAdd) {
				applyLocalChange(applier, "ADD", fluid.model.composePath(prefix, key), envAdd[key])
			}
			$.extend(root, envAdd);
			return func()
		}, null, function () {
			for (var key in envAdd) {
				applyLocalChange(applier, "DELETE", fluid.model.composePath(prefix, key))
			}
		})
	};
	fluid.makeEnvironmentFetcher = function (prefix, directModel) {
		return function (parsed) {
			var env = fluid.get(fluid.threadLocal(), prefix);
			return fluid.fetchContextReference(parsed, directModel, env)
		}
	};
	fluid.extractEL = function (string, options) {
		if (options.ELstyle === "ALL") {
			return string
		} else {
			if (options.ELstyle.length === 1) {
				if (string.charAt(0) === options.ELstyle) {
					return string.substring(1)
				}
			} else {
				if (options.ELstyle === "${}") {
					var i1 = string.indexOf("${");
					var i2 = string.lastIndexOf("}");
					if (i1 === 0 && i2 !== -1) {
						return string.substring(2, i2)
					}
				}
			}
		}
	};
	fluid.extractELWithContext = function (string, options) {
		var EL = fluid.extractEL(string, options);
		if (EL && EL.charAt(0) === "{") {
			return fluid.parseContextReference(EL, 0)
		}
		return EL ? {
			path : EL
		}
		 : EL
	};
	fluid.parseContextReference = function (reference, index, delimiter) {
		var endcpos = reference.indexOf("}", index + 1);
		if (endcpos === -1) {
			fluid.fail('Cannot parse context reference "' + reference + '": Malformed context reference without }')
		}
		var context = reference.substring(index + 1, endcpos);
		var endpos = delimiter ? reference.indexOf(delimiter, endcpos + 1) : reference.length;
		var path = reference.substring(endcpos + 1, endpos);
		if (path.charAt(0) === ".") {
			path = path.substring(1)
		}
		return {
			context : context,
			path : path,
			endpos : endpos
		}
	};
	fluid.renderContextReference = function (parsed) {
		return "{" + parsed.context + "}" + parsed.path
	};
	fluid.fetchContextReference = function (parsed, directModel, env) {
		var base = parsed.context ? env[parsed.context] : directModel;
		if (!base) {
			return base
		}
		return fluid.get(base, parsed.path)
	};
	fluid.resolveContextValue = function (string, options) {
		if (options.bareContextRefs && string.charAt(0) === "{") {
			var parsed = fluid.parseContextReference(string, 0);
			return options.fetcher(parsed)
		} else {
			if (options.ELstyle && options.ELstyle !== "${}") {
				var parsed = fluid.extractELWithContext(string, options);
				if (parsed) {
					return options.fetcher(parsed)
				}
			}
		}
		while (typeof(string) === "string") {
			var i1 = string.indexOf("${");
			var i2 = string.indexOf("}", i1 + 2);
			if (i1 !== -1 && i2 !== -1) {
				var parsed;
				if (string.charAt(i1 + 2) === "{") {
					parsed = fluid.parseContextReference(string, i1 + 2, "}");
					i2 = parsed.endpos
				} else {
					parsed = {
						path : string.substring(i1 + 2, i2)
					}
				}
				var subs = options.fetcher(parsed);
				var all = (i1 === 0 && i2 === string.length - 1);
				if (subs === undefined || subs === null) {
					return subs
				}
				string = all ? subs : string.substring(0, i1) + subs + string.substring(i2 + 1)
			} else {
				break
			}
		}
		return string
	};
	fluid.resolveContextValue = fluid.wrapActivity(fluid.resolveContextValue, ["    while resolving context value ", "arguments.0"]);
	function resolveEnvironmentImpl(obj, options) {
		fluid.guardCircularity(options.seenIds, obj, "expansion", ' - please ensure options are not circularly connected, or protect from expansion using the "noexpand" policy or expander');
		function recurse(arg) {
			return resolveEnvironmentImpl(arg, options)
		}
		if (typeof(obj) === "string" && !options.noValue) {
			return fluid.resolveContextValue(obj, options)
		} else {
			if (fluid.isPrimitive(obj) || obj.nodeType !== undefined || obj.jquery) {
				return obj
			} else {
				if (options.filter) {
					return options.filter(obj, recurse, options)
				} else {
					return (options.noCopy ? fluid.each : fluid.transform)(obj, function (value, key) {
						return resolveEnvironmentImpl(value, options)
					})
				}
			}
		}
	}
	fluid.defaults("fluid.resolveEnvironment", {
		ELstyle : "${}",
		seenIds : {},
		bareContextRefs : true
	});
	fluid.resolveEnvironment = function (obj, options) {
		options = $.extend(true, {}, fluid.rawDefaults("fluid.resolveEnvironment"), options);
		return resolveEnvironmentImpl(obj, options)
	};
	fluid.expander.deferredCall = function (target, source, recurse) {
		var expander = source.expander;
		var args = (!expander.args || fluid.isArrayable(expander.args)) ? expander.args : $.makeArray(expander.args);
		args = recurse(args);
		return fluid.invokeGlobalFunction(expander.func, args)
	};
	fluid.deferredCall = fluid.expander.deferredCall;
	fluid.deferredInvokeCall = function (target, source, recurse) {
		var expander = source.expander;
		var args = (!expander.args || fluid.isArrayable(expander.args)) ? expander.args : $.makeArray(expander.args);
		args = recurse(args);
		return fluid.invoke(expander.func, args)
	};
	fluid.expander.noexpand = function (target, source) {
		return $.extend(target, source.expander.tree)
	};
	fluid.noexpand = fluid.expander.noexpand;
	fluid.expander.lightFilter = function (obj, recurse, options) {
		var togo;
		if (fluid.isArrayable(obj)) {
			togo = options.noCopy ? obj : [];
			fluid.each(obj, function (value, key) {
				togo[key] = recurse(value)
			})
		} else {
			togo = options.noCopy ? obj : {};
			for (var key in obj) {
				var value = obj[key];
				var expander;
				if (key === "expander" && !(options.expandOnly && options.expandOnly[value.type])) {
					expander = fluid.getGlobalValue(value.type);
					if (expander) {
						return expander.call(null, togo, obj, recurse, options)
					}
				}
				if (key !== "expander" || !expander) {
					togo[key] = recurse(value)
				}
			}
		}
		return options.noCopy ? obj : togo
	};
	fluid.expander.expandLight = function (source, expandOptions) {
		var options = $.extend({}, expandOptions);
		options.filter = fluid.expander.lightFilter;
		return fluid.resolveEnvironment(source, options)
	}
})(jQuery, fluid_1_4);
var fluid_1_4 = fluid_1_4 || {};
(function ($, fluid) {
	var resourceCache = {};
	var pendingClass = {};
	fluid.fetchResources = function (resourceSpecs, callback, options) {
		var that = fluid.initLittleComponent("fluid.fetchResources", options);
		that.resourceSpecs = resourceSpecs;
		that.callback = callback;
		that.operate = function () {
			fluid.fetchResources.fetchResourcesImpl(that)
		};
		fluid.each(resourceSpecs, function (resourceSpec) {
			resourceSpec.recurseFirer = fluid.event.getEventFirer();
			resourceSpec.recurseFirer.addListener(that.operate);
			if (resourceSpec.url && !resourceSpec.href) {
				resourceSpec.href = resourceSpec.url
			}
		});
		if (that.options.amalgamateClasses) {
			fluid.fetchResources.amalgamateClasses(resourceSpecs, that.options.amalgamateClasses, that.operate)
		}
		that.operate();
		return that
	};
	fluid.fetchResources.amalgamateClasses = function (specs, classes, operator) {
		fluid.each(classes, function (clazz) {
			var pending = pendingClass[clazz];
			fluid.each(pending, function (pendingrec, canon) {
				specs[clazz + "!" + canon] = pendingrec;
				pendingrec.recurseFirer.addListener(operator)
			})
		})
	};
	fluid.fetchResources.timeSuccessCallback = function (resourceSpec) {
		if (resourceSpec.timeSuccess && resourceSpec.options && resourceSpec.options.success) {
			var success = resourceSpec.options.success;
			resourceSpec.options.success = function () {
				var startTime = new Date();
				var ret = success.apply(null, arguments);
				fluid.log("External callback for URL " + resourceSpec.href + " completed - callback time: " + (new Date().getTime() - startTime.getTime()) + "ms");
				return ret
			}
		}
	};
	function canonUrl(url) {
		return url
	}
	fluid.fetchResources.clearResourceCache = function (url) {
		if (url) {
			delete resourceCache[canonUrl(url)]
		} else {
			fluid.clear(resourceCache)
		}
	};
	fluid.fetchResources.handleCachedRequest = function (resourceSpec, response) {
		var canon = canonUrl(resourceSpec.href);
		var cached = resourceCache[canon];
		if (cached.$$firer$$) {
			fluid.log("Handling request for " + canon + " from cache");
			var fetchClass = resourceSpec.fetchClass;
			if (fetchClass && pendingClass[fetchClass]) {
				fluid.log("Clearing pendingClass entry for class " + fetchClass);
				delete pendingClass[fetchClass][canon]
			}
			resourceCache[canon] = response;
			cached.fire(response)
		}
	};
	fluid.fetchResources.completeRequest = function (thisSpec, recurseCall) {
		thisSpec.queued = false;
		thisSpec.completeTime = new Date();
		fluid.log("Request to URL " + thisSpec.href + " completed - total elapsed time: " + (thisSpec.completeTime.getTime() - thisSpec.initTime.getTime()) + "ms");
		thisSpec.recurseFirer.fire()
	};
	fluid.fetchResources.makeResourceCallback = function (thisSpec) {
		return {
			success : function (response) {
				thisSpec.resourceText = response;
				thisSpec.resourceKey = thisSpec.href;
				if (thisSpec.forceCache) {
					fluid.fetchResources.handleCachedRequest(thisSpec, response)
				}
				fluid.fetchResources.completeRequest(thisSpec)
			},
			error : function (response, textStatus, errorThrown) {
				thisSpec.fetchError = {
					status : response.status,
					textStatus : response.textStatus,
					errorThrown : errorThrown
				};
				fluid.fetchResources.completeRequest(thisSpec)
			}
		}
	};
	fluid.fetchResources.issueCachedRequest = function (resourceSpec, options) {
		var canon = canonUrl(resourceSpec.href);
		var cached = resourceCache[canon];
		if (!cached) {
			fluid.log("First request for cached resource with url " + canon);
			cached = fluid.event.getEventFirer();
			cached.$$firer$$ = true;
			resourceCache[canon] = cached;
			var fetchClass = resourceSpec.fetchClass;
			if (fetchClass) {
				if (!pendingClass[fetchClass]) {
					pendingClass[fetchClass] = {}
					
				}
				pendingClass[fetchClass][canon] = resourceSpec
			}
			options.cache = false;
			$.ajax(options)
		} else {
			if (!cached.$$firer$$) {
				options.success(cached)
			} else {
				fluid.log("Request for cached resource which is in flight: url " + canon);
				cached.addListener(function (response) {
					options.success(response)
				})
			}
		}
	};
	fluid.fetchResources.composeCallbacks = function (internal, external) {
		return external ? function () {
			try {
				external.apply(null, arguments)
			} catch (e) {
				fluid.log("Exception applying external fetchResources callback: " + e)
			}
			internal.apply(null, arguments)
		}
		 : internal
	};
	fluid.fetchResources.composePolicy = function (target, source, key) {
		return fluid.fetchResources.composeCallbacks(target, source)
	};
	fluid.defaults("fluid.fetchResources.issueRequest", {
		mergePolicy : {
			success : fluid.fetchResources.composePolicy,
			error : fluid.fetchResources.composePolicy,
			url : "reverse"
		}
	});
	fluid.fetchResources.issueRequest = function (resourceSpec, key) {
		var thisCallback = fluid.fetchResources.makeResourceCallback(resourceSpec);
		var options = {
			url : resourceSpec.href,
			success : thisCallback.success,
			error : thisCallback.error,
			dataType : "text"
		};
		fluid.fetchResources.timeSuccessCallback(resourceSpec);
		fluid.merge(fluid.defaults("fluid.fetchResources.issueRequest").mergePolicy, options, resourceSpec.options);
		resourceSpec.queued = true;
		resourceSpec.initTime = new Date();
		fluid.log("Request with key " + key + " queued for " + resourceSpec.href);
		if (resourceSpec.forceCache) {
			fluid.fetchResources.issueCachedRequest(resourceSpec, options)
		} else {
			$.ajax(options)
		}
	};
	fluid.fetchResources.fetchResourcesImpl = function (that) {
		var complete = true;
		var allSync = true;
		var resourceSpecs = that.resourceSpecs;
		for (var key in resourceSpecs) {
			var resourceSpec = resourceSpecs[key];
			if (!resourceSpec.options || resourceSpec.options.async) {
				allSync = false
			}
			if (resourceSpec.href && !resourceSpec.completeTime) {
				if (!resourceSpec.queued) {
					fluid.fetchResources.issueRequest(resourceSpec, key)
				}
				if (resourceSpec.queued) {
					complete = false
				}
			} else {
				if (resourceSpec.nodeId && !resourceSpec.resourceText) {
					var node = document.getElementById(resourceSpec.nodeId);
					resourceSpec.resourceText = fluid.dom.getElementText(node);
					resourceSpec.resourceKey = resourceSpec.nodeId
				}
			}
		}
		if (complete && that.callback && !that.callbackCalled) {
			that.callbackCalled = true;
			if ($.browser.mozilla && !allSync) {
				setTimeout(function () {
					that.callback(resourceSpecs)
				}, 1)
			} else {
				that.callback(resourceSpecs)
			}
		}
	};
	fluid.fetchResources.primeCacheFromResources = function (componentName) {
		var resources = fluid.defaults(componentName).resources;
		var that = {
			typeName : "fluid.fetchResources.primeCacheFromResources"
		};
		var expanded = (fluid.expandOptions ? fluid.expandOptions : fluid.identity)(fluid.copy(resources), that);
		fluid.fetchResources(expanded)
	};
	fluid.registerNamespace("fluid.expander");
	fluid.expander.makeDefaultFetchOptions = function (successdisposer, failid, options) {
		return $.extend(true, {
			dataType : "text"
		}, options, {
			success : function (response, environmentdisposer) {
				var json = JSON.parse(response);
				environmentdisposer(successdisposer(json))
			},
			error : function (response, textStatus) {
				fluid.log("Error fetching " + failid + ": " + textStatus)
			}
		})
	};
	fluid.expander.makeFetchExpander = function (options) {
		return {
			expander : {
				type : "fluid.expander.deferredFetcher",
				href : options.url,
				options : fluid.expander.makeDefaultFetchOptions(options.disposer, options.url, options.options),
				resourceSpecCollector : "{resourceSpecCollector}",
				fetchKey : options.fetchKey
			}
		}
	};
	fluid.expander.deferredFetcher = function (target, source, recurse, expandOptions) {
		var expander = source.expander;
		var spec = fluid.copy(expander);
		var collector = fluid.resolveEnvironment(expander.resourceSpecCollector, expandOptions);
		delete spec.type;
		delete spec.resourceSpecCollector;
		delete spec.fetchKey;
		var environmentdisposer = function (disposed) {
			$.extend(target, disposed)
		};
		spec.options.success = function (response) {
			expander.options.success(response, environmentdisposer)
		};
		var key = expander.fetchKey || fluid.allocateGuid();
		collector[key] = spec;
		return target
	}
})(jQuery, fluid_1_4);
var fluid_1_4 = fluid_1_4 || {};
(function ($, fluid) {
	fluid.XMLP = function (strXML) {
		return fluid.XMLP.XMLPImpl(strXML)
	};
	fluid.XMLP.closedTags = {
		abbr : true,
		br : true,
		col : true,
		img : true,
		input : true,
		link : true,
		meta : true,
		param : true,
		hr : true,
		area : true,
		embed : true
	};
	fluid.XMLP._NONE = 0;
	fluid.XMLP._ELM_B = 1;
	fluid.XMLP._ELM_E = 2;
	fluid.XMLP._ELM_EMP = 3;
	fluid.XMLP._ATT = 4;
	fluid.XMLP._TEXT = 5;
	fluid.XMLP._ENTITY = 6;
	fluid.XMLP._PI = 7;
	fluid.XMLP._CDATA = 8;
	fluid.XMLP._COMMENT = 9;
	fluid.XMLP._DTD = 10;
	fluid.XMLP._ERROR = 11;
	fluid.XMLP._CONT_XML = 0;
	fluid.XMLP._CONT_ALT = 1;
	fluid.XMLP._ATT_NAME = 0;
	fluid.XMLP._ATT_VAL = 1;
	fluid.XMLP._STATE_PROLOG = 1;
	fluid.XMLP._STATE_DOCUMENT = 2;
	fluid.XMLP._STATE_MISC = 3;
	fluid.XMLP._errs = [];
	fluid.XMLP._errs[fluid.XMLP.ERR_CLOSE_PI = 0] = "PI: missing closing sequence";
	fluid.XMLP._errs[fluid.XMLP.ERR_CLOSE_DTD = 1] = "DTD: missing closing sequence";
	fluid.XMLP._errs[fluid.XMLP.ERR_CLOSE_COMMENT = 2] = "Comment: missing closing sequence";
	fluid.XMLP._errs[fluid.XMLP.ERR_CLOSE_CDATA = 3] = "CDATA: missing closing sequence";
	fluid.XMLP._errs[fluid.XMLP.ERR_CLOSE_ELM = 4] = "Element: missing closing sequence";
	fluid.XMLP._errs[fluid.XMLP.ERR_CLOSE_ENTITY = 5] = "Entity: missing closing sequence";
	fluid.XMLP._errs[fluid.XMLP.ERR_PI_TARGET = 6] = "PI: target is required";
	fluid.XMLP._errs[fluid.XMLP.ERR_ELM_EMPTY = 7] = "Element: cannot be both empty and closing";
	fluid.XMLP._errs[fluid.XMLP.ERR_ELM_NAME = 8] = 'Element: name must immediatly follow "<"';
	fluid.XMLP._errs[fluid.XMLP.ERR_ELM_LT_NAME = 9] = 'Element: "<" not allowed in element names';
	fluid.XMLP._errs[fluid.XMLP.ERR_ATT_VALUES = 10] = "Attribute: values are required and must be in quotes";
	fluid.XMLP._errs[fluid.XMLP.ERR_ATT_LT_NAME = 11] = 'Element: "<" not allowed in attribute names';
	fluid.XMLP._errs[fluid.XMLP.ERR_ATT_LT_VALUE = 12] = 'Attribute: "<" not allowed in attribute values';
	fluid.XMLP._errs[fluid.XMLP.ERR_ATT_DUP = 13] = "Attribute: duplicate attributes not allowed";
	fluid.XMLP._errs[fluid.XMLP.ERR_ENTITY_UNKNOWN = 14] = "Entity: unknown entity";
	fluid.XMLP._errs[fluid.XMLP.ERR_INFINITELOOP = 15] = "Infinite loop";
	fluid.XMLP._errs[fluid.XMLP.ERR_DOC_STRUCTURE = 16] = "Document: only comments, processing instructions, or whitespace allowed outside of document element";
	fluid.XMLP._errs[fluid.XMLP.ERR_ELM_NESTING = 17] = "Element: must be nested correctly";
	fluid.XMLP._checkStructure = function (that, iEvent) {
		var stack = that.m_stack;
		if (fluid.XMLP._STATE_PROLOG == that.m_iState) {
			that.m_iState = fluid.XMLP._STATE_DOCUMENT
		}
		if (fluid.XMLP._STATE_DOCUMENT === that.m_iState) {
			if ((fluid.XMLP._ELM_B == iEvent) || (fluid.XMLP._ELM_EMP == iEvent)) {
				that.m_stack[stack.length] = that.getName()
			}
			if ((fluid.XMLP._ELM_E == iEvent) || (fluid.XMLP._ELM_EMP == iEvent)) {
				if (stack.length === 0) {
					return fluid.XMLP._NONE
				}
				var strTop = stack[stack.length - 1];
				that.m_stack.length--;
				if (strTop === null || strTop !== that.getName()) {
					return that._setErr(that, fluid.XMLP.ERR_ELM_NESTING)
				}
			}
		}
		return iEvent
	};
	fluid.XMLP._parseCDATA = function (that, iB) {
		var iE = that.m_xml.indexOf("]]>", iB);
		if (iE == -1) {
			return fluid.XMLP._setErr(that, fluid.XMLP.ERR_CLOSE_CDATA)
		}
		fluid.XMLP._setContent(that, fluid.XMLP._CONT_XML, iB, iE);
		that.m_iP = iE + 3;
		return fluid.XMLP._CDATA
	};
	fluid.XMLP._parseComment = function (that, iB) {
		var iE = that.m_xml.indexOf("-->", iB);
		if (iE == -1) {
			return fluid.XMLP._setErr(that, fluid.XMLP.ERR_CLOSE_COMMENT)
		}
		fluid.XMLP._setContent(that, fluid.XMLP._CONT_XML, iB - 4, iE + 3);
		that.m_iP = iE + 3;
		return fluid.XMLP._COMMENT
	};
	fluid.XMLP._parseDTD = function (that, iB) {
		var iE,
		strClose,
		iInt,
		iLast;
		iE = that.m_xml.indexOf(">", iB);
		if (iE == -1) {
			return fluid.XMLP._setErr(that, fluid.XMLP.ERR_CLOSE_DTD)
		}
		iInt = that.m_xml.indexOf("[", iB);
		strClose = ((iInt != -1) && (iInt < iE)) ? "]>" : ">";
		while (true) {
			if (iE == iLast) {
				return fluid.XMLP._setErr(that, fluid.XMLP.ERR_INFINITELOOP)
			}
			iLast = iE;
			iE = that.m_xml.indexOf(strClose, iB);
			if (iE == -1) {
				return fluid.XMLP._setErr(that, fluid.XMLP.ERR_CLOSE_DTD)
			}
			if (that.m_xml.substring(iE - 1, iE + 2) != "]]>") {
				break
			}
		}
		that.m_iP = iE + strClose.length;
		return fluid.XMLP._DTD
	};
	fluid.XMLP._parsePI = function (that, iB) {
		var iE,
		iTB,
		iTE,
		iCB,
		iCE;
		iE = that.m_xml.indexOf("?>", iB);
		if (iE == -1) {
			return fluid.XMLP._setErr(that, fluid.XMLP.ERR_CLOSE_PI)
		}
		iTB = fluid.SAXStrings.indexOfNonWhitespace(that.m_xml, iB, iE);
		if (iTB == -1) {
			return fluid.XMLP._setErr(that, fluid.XMLP.ERR_PI_TARGET)
		}
		iTE = fluid.SAXStrings.indexOfWhitespace(that.m_xml, iTB, iE);
		if (iTE == -1) {
			iTE = iE
		}
		iCB = fluid.SAXStrings.indexOfNonWhitespace(that.m_xml, iTE, iE);
		if (iCB == -1) {
			iCB = iE
		}
		iCE = fluid.SAXStrings.lastIndexOfNonWhitespace(that.m_xml, iCB, iE);
		if (iCE == -1) {
			iCE = iE - 1
		}
		that.m_name = that.m_xml.substring(iTB, iTE);
		fluid.XMLP._setContent(that, fluid.XMLP._CONT_XML, iCB, iCE + 1);
		that.m_iP = iE + 2;
		return fluid.XMLP._PI
	};
	fluid.XMLP._parseText = function (that, iB) {
		var iE = that.m_xml.indexOf("<", iB);
		if (iE == -1) {
			iE = that.m_xml.length
		}
		fluid.XMLP._setContent(that, fluid.XMLP._CONT_XML, iB, iE);
		that.m_iP = iE;
		return fluid.XMLP._TEXT
	};
	fluid.XMLP._setContent = function (that, iSrc) {
		var args = arguments;
		if (fluid.XMLP._CONT_XML == iSrc) {
			that.m_cAlt = null;
			that.m_cB = args[2];
			that.m_cE = args[3]
		} else {
			that.m_cAlt = args[2];
			that.m_cB = 0;
			that.m_cE = args[2].length
		}
		that.m_cSrc = iSrc
	};
	fluid.XMLP._setErr = function (that, iErr) {
		var strErr = fluid.XMLP._errs[iErr];
		that.m_cAlt = strErr;
		that.m_cB = 0;
		that.m_cE = strErr.length;
		that.m_cSrc = fluid.XMLP._CONT_ALT;
		return fluid.XMLP._ERROR
	};
	fluid.XMLP._parseElement = function (that, iB) {
		var iE,
		iDE,
		iRet;
		var iType,
		strN,
		iLast;
		iDE = iE = that.m_xml.indexOf(">", iB);
		if (iE == -1) {
			return that._setErr(that, fluid.XMLP.ERR_CLOSE_ELM)
		}
		if (that.m_xml.charAt(iB) == "/") {
			iType = fluid.XMLP._ELM_E;
			iB++
		} else {
			iType = fluid.XMLP._ELM_B
		}
		if (that.m_xml.charAt(iE - 1) == "/") {
			if (iType == fluid.XMLP._ELM_E) {
				return fluid.XMLP._setErr(that, fluid.XMLP.ERR_ELM_EMPTY)
			}
			iType = fluid.XMLP._ELM_EMP;
			iDE--
		}
		that.nameRegex.lastIndex = iB;
		var nameMatch = that.nameRegex.exec(that.m_xml);
		if (!nameMatch) {
			return fluid.XMLP._setErr(that, fluid.XMLP.ERR_ELM_NAME)
		}
		strN = nameMatch[1].toLowerCase();
		if ("li" === strN && iType !== fluid.XMLP._ELM_E && that.m_stack.length > 0 && that.m_stack[that.m_stack.length - 1] === "li" && !that.m_emitSynthetic) {
			that.m_name = "li";
			that.m_emitSynthetic = true;
			return fluid.XMLP._ELM_E
		}
		that.m_attributes = {};
		that.m_cAlt = "";
		if (that.nameRegex.lastIndex < iDE) {
			that.m_iP = that.nameRegex.lastIndex;
			while (that.m_iP < iDE) {
				that.attrStartRegex.lastIndex = that.m_iP;
				var attrMatch = that.attrStartRegex.exec(that.m_xml);
				if (!attrMatch) {
					return fluid.XMLP._setErr(that, fluid.XMLP.ERR_ATT_VALUES)
				}
				var attrname = attrMatch[1].toLowerCase();
				var attrval;
				if (that.m_xml.charCodeAt(that.attrStartRegex.lastIndex) === 61) {
					var valRegex = that.m_xml.charCodeAt(that.attrStartRegex.lastIndex + 1) === 34 ? that.attrValRegex : that.attrValIERegex;
					valRegex.lastIndex = that.attrStartRegex.lastIndex + 1;
					attrMatch = valRegex.exec(that.m_xml);
					if (!attrMatch) {
						return fluid.XMLP._setErr(that, fluid.XMLP.ERR_ATT_VALUES)
					}
					attrval = attrMatch[1]
				} else {
					attrval = attrname;
					valRegex = that.attrStartRegex
				}
				if (!that.m_attributes[attrname]) {
					that.m_attributes[attrname] = attrval
				} else {
					return fluid.XMLP._setErr(that, fluid.XMLP.ERR_ATT_DUP)
				}
				that.m_iP = valRegex.lastIndex
			}
		}
		if (strN.indexOf("<") != -1) {
			return fluid.XMLP._setErr(that, fluid.XMLP.ERR_ELM_LT_NAME)
		}
		that.m_name = strN;
		that.m_iP = iE + 1;
		if (fluid.XMLP.closedTags[strN]) {
			that.closeRegex.lastIndex = iE + 1;
			var closeMatch = that.closeRegex.exec;
			if (closeMatch) {
				var matchclose = that.m_xml.indexOf(strN, closeMatch.lastIndex);
				if (matchclose === closeMatch.lastIndex) {
					return iType
				} else {
					return fluid.XMLP._ELM_EMP
				}
			}
		}
		that.m_emitSynthetic = false;
		return iType
	};
	fluid.XMLP._parse = function (that) {
		var iP = that.m_iP;
		var xml = that.m_xml;
		if (iP === xml.length) {
			return fluid.XMLP._NONE
		}
		var c = xml.charAt(iP);
		if (c === "<") {
			var c2 = xml.charAt(iP + 1);
			if (c2 === "?") {
				return fluid.XMLP._parsePI(that, iP + 2)
			} else {
				if (c2 === "!") {
					if (iP === xml.indexOf("<!DOCTYPE", iP)) {
						return fluid.XMLP._parseDTD(that, iP + 9)
					} else {
						if (iP === xml.indexOf("<!--", iP)) {
							return fluid.XMLP._parseComment(that, iP + 4)
						} else {
							if (iP === xml.indexOf("<![CDATA[", iP)) {
								return fluid.XMLP._parseCDATA(that, iP + 9)
							}
						}
					}
				} else {
					return fluid.XMLP._parseElement(that, iP + 1)
				}
			}
		} else {
			return fluid.XMLP._parseText(that, iP)
		}
	};
	fluid.XMLP.XMLPImpl = function (strXML) {
		var that = {};
		that.m_xml = strXML;
		that.m_iP = 0;
		that.m_iState = fluid.XMLP._STATE_PROLOG;
		that.m_stack = [];
		that.m_attributes = {};
		that.m_emitSynthetic = false;
		that.getColumnNumber = function () {
			return fluid.SAXStrings.getColumnNumber(that.m_xml, that.m_iP)
		};
		that.getContent = function () {
			return (that.m_cSrc == fluid.XMLP._CONT_XML) ? that.m_xml : that.m_cAlt
		};
		that.getContentBegin = function () {
			return that.m_cB
		};
		that.getContentEnd = function () {
			return that.m_cE
		};
		that.getLineNumber = function () {
			return fluid.SAXStrings.getLineNumber(that.m_xml, that.m_iP)
		};
		that.getName = function () {
			return that.m_name
		};
		that.next = function () {
			return fluid.XMLP._checkStructure(that, fluid.XMLP._parse(that))
		};
		that.nameRegex = /([^\s\/>]+)/g;
		that.attrStartRegex = /\s*([\w:_][\w:_\-\.]*)/gm;
		that.attrValRegex = /\"([^\"]*)\"\s*/gm;
		that.attrValIERegex = /([^\>\s]+)\s*/gm;
		that.closeRegex = /\s*<\//g;
		return that
	};
	fluid.SAXStrings = {};
	fluid.SAXStrings.WHITESPACE = " \t\n\r";
	fluid.SAXStrings.QUOTES = "\"'";
	fluid.SAXStrings.getColumnNumber = function (strD, iP) {
		if (!strD) {
			return -1
		}
		iP = iP || strD.length;
		var arrD = strD.substring(0, iP).split("\n");
		arrD.length--;
		var iLinePos = arrD.join("\n").length;
		return iP - iLinePos
	};
	fluid.SAXStrings.getLineNumber = function (strD, iP) {
		if (!strD) {
			return -1
		}
		iP = iP || strD.length;
		return strD.substring(0, iP).split("\n").length
	};
	fluid.SAXStrings.indexOfNonWhitespace = function (strD, iB, iE) {
		if (!strD) {
			return -1
		}
		iB = iB || 0;
		iE = iE || strD.length;
		for (var i = iB; i < iE; ++i) {
			var c = strD.charAt(i);
			if (c !== " " && c !== "\t" && c !== "\n" && c !== "\r") {
				return i
			}
		}
		return -1
	};
	fluid.SAXStrings.indexOfWhitespace = function (strD, iB, iE) {
		if (!strD) {
			return -1
		}
		iB = iB || 0;
		iE = iE || strD.length;
		for (var i = iB; i < iE; i++) {
			if (fluid.SAXStrings.WHITESPACE.indexOf(strD.charAt(i)) != -1) {
				return i
			}
		}
		return -1
	};
	fluid.SAXStrings.lastIndexOfNonWhitespace = function (strD, iB, iE) {
		if (!strD) {
			return -1
		}
		iB = iB || 0;
		iE = iE || strD.length;
		for (var i = iE - 1; i >= iB; i--) {
			if (fluid.SAXStrings.WHITESPACE.indexOf(strD.charAt(i)) == -1) {
				return i
			}
		}
		return -1
	};
	fluid.SAXStrings.replace = function (strD, iB, iE, strF, strR) {
		if (!strD) {
			return ""
		}
		iB = iB || 0;
		iE = iE || strD.length;
		return strD.substring(iB, iE).split(strF).join(strR)
	}
})(jQuery, fluid_1_4);
fluid_1_4 = fluid_1_4 || {};
(function ($, fluid) {
	fluid.parseTemplate = function (template, baseURL, scanStart, cutpoints_in, opts) {
		opts = opts || {};
		if (!template) {
			fluid.fail("empty template supplied to fluid.parseTemplate")
		}
		var t;
		var parser;
		var tagstack;
		var lumpindex = 0;
		var nestingdepth = 0;
		var justended = false;
		var defstart = -1;
		var defend = -1;
		var debugMode = false;
		var cutpoints = [];
		var simpleClassCutpoints = {};
		var cutstatus = [];
		var XMLLump = function (lumpindex, nestingdepth) {
			return {
				nestingdepth : nestingdepth,
				lumpindex : lumpindex,
				parent : t
			}
		};
		function isSimpleClassCutpoint(tree) {
			return tree.length === 1 && tree[0].predList.length === 1 && tree[0].predList[0].clazz
		}
		function init(baseURLin, debugModeIn, cutpointsIn) {
			t.rootlump = XMLLump(0, -1);
			tagstack = [t.rootlump];
			lumpindex = 0;
			nestingdepth = 0;
			justended = false;
			defstart = -1;
			defend = -1;
			baseURL = baseURLin;
			debugMode = debugModeIn;
			if (cutpointsIn) {
				for (var i = 0; i < cutpointsIn.length; ++i) {
					var tree = fluid.parseSelector(cutpointsIn[i].selector);
					var clazz = isSimpleClassCutpoint(tree);
					if (clazz) {
						simpleClassCutpoints[clazz] = cutpointsIn[i].id
					} else {
						cutstatus.push([]);
						cutpoints.push($.extend({}, cutpointsIn[i], {
								tree : tree
							}))
					}
				}
			}
		}
		function findTopContainer() {
			for (var i = tagstack.length - 1; i >= 0; --i) {
				var lump = tagstack[i];
				if (lump.rsfID !== undefined) {
					return lump
				}
			}
			return t.rootlump
		}
		function newLump() {
			var togo = XMLLump(lumpindex, nestingdepth);
			if (debugMode) {
				togo.line = parser.getLineNumber();
				togo.column = parser.getColumnNumber()
			}
			t.lumps[lumpindex] = togo;
			++lumpindex;
			return togo
		}
		function addLump(mmap, ID, lump) {
			var list = mmap[ID];
			if (!list) {
				list = [];
				mmap[ID] = list
			}
			list[list.length] = lump
		}
		function checkContribute(ID, lump) {
			if (ID.indexOf("scr=contribute-") !== -1) {
				var scr = ID.substring("scr=contribute-".length);
				addLump(t.collectmap, scr, lump)
			}
		}
		function debugLump(lump) {
			return "<" + lump.tagname + ">"
		}
		function hasCssClass(clazz, totest) {
			if (!totest) {
				return false
			}
			return (" " + totest + " ").indexOf(" " + clazz + " ") !== -1
		}
		function matchNode(term, headlump, headclazz) {
			if (term.predList) {
				for (var i = 0; i < term.predList.length; ++i) {
					var pred = term.predList[i];
					if (pred.id && headlump.attributemap.id !== pred.id) {
						return false
					}
					if (pred.clazz && !hasCssClass(pred.clazz, headclazz)) {
						return false
					}
					if (pred.tag && headlump.tagname !== pred.tag) {
						return false
					}
				}
				return true
			}
		}
		function tagStartCut(headlump) {
			var togo;
			var headclazz = headlump.attributemap["class"];
			if (headclazz) {
				var split = headclazz.split(" ");
				for (var i = 0; i < split.length; ++i) {
					var simpleCut = simpleClassCutpoints[$.trim(split[i])];
					if (simpleCut) {
						return simpleCut
					}
				}
			}
			for (var i = 0; i < cutpoints.length; ++i) {
				var cut = cutpoints[i];
				var cutstat = cutstatus[i];
				var nextterm = cutstat.length;
				if (nextterm < cut.tree.length) {
					var term = cut.tree[nextterm];
					if (nextterm > 0) {
						if (cut.tree[nextterm - 1].child && cutstat[nextterm - 1] !== headlump.nestingdepth - 1) {
							continue
						}
					}
					var isMatch = matchNode(term, headlump, headclazz);
					if (isMatch) {
						cutstat[cutstat.length] = headlump.nestingdepth;
						if (cutstat.length === cut.tree.length) {
							if (togo !== undefined) {
								fluid.fail("Cutpoint specification error - node " + debugLump(headlump) + " has already matched with rsf:id of " + togo)
							}
							if (cut.id === undefined || cut.id === null) {
								fluid.fail("Error in cutpoints list - entry at position " + i + " does not have an id set")
							}
							togo = cut.id
						}
					}
				}
			}
			return togo
		}
		function tagEndCut() {
			if (cutpoints) {
				for (var i = 0; i < cutpoints.length; ++i) {
					var cutstat = cutstatus[i];
					if (cutstat.length > 0 && cutstat[cutstat.length - 1] === nestingdepth) {
						cutstat.length--
					}
				}
			}
		}
		function processTagEnd() {
			tagEndCut();
			var endlump = newLump();
			--nestingdepth;
			endlump.text = "</" + parser.getName() + ">";
			var oldtop = tagstack[tagstack.length - 1];
			oldtop.close_tag = t.lumps[lumpindex - 1];
			tagstack.length--;
			justended = true
		}
		function processTagStart(isempty, text) {
			++nestingdepth;
			if (justended) {
				justended = false;
				var backlump = newLump();
				backlump.nestingdepth--
			}
			if (t.firstdocumentindex === -1) {
				t.firstdocumentindex = lumpindex
			}
			var headlump = newLump();
			var stacktop = tagstack[tagstack.length - 1];
			headlump.uplump = stacktop;
			var tagname = parser.getName();
			headlump.tagname = tagname;
			var attrs = headlump.attributemap = parser.m_attributes;
			var ID = attrs[fluid.ID_ATTRIBUTE];
			if (ID === undefined) {
				ID = tagStartCut(headlump)
			}
			for (var attrname in attrs) {
				if (ID === undefined) {
					if (/href|src|codebase|action/.test(attrname)) {
						ID = "scr=rewrite-url"
					} else {
						if (ID === undefined && /for|headers/.test(attrname)) {
							ID = "scr=null"
						}
					}
				}
			}
			if (ID) {
				if (ID.charCodeAt(0) === 126) {
					ID = ID.substring(1);
					headlump.elide = true
				}
				checkContribute(ID, headlump);
				headlump.rsfID = ID;
				var downreg = findTopContainer();
				if (!downreg.downmap) {
					downreg.downmap = {}
					
				}
				while (downreg) {
					if (downreg.downmap) {
						addLump(downreg.downmap, ID, headlump)
					}
					downreg = downreg.uplump
				}
				addLump(t.globalmap, ID, headlump);
				var colpos = ID.indexOf(":");
				if (colpos !== -1) {
					var prefix = ID.substring(0, colpos);
					if (!stacktop.finallump) {
						stacktop.finallump = {}
						
					}
					stacktop.finallump[prefix] = headlump
				}
			}
			headlump.text = "<" + tagname + fluid.dumpAttributes(attrs) + (isempty && !ID ? "/>" : ">");
			tagstack[tagstack.length] = headlump;
			if (isempty) {
				if (ID) {
					processTagEnd()
				} else {
					--nestingdepth;
					tagstack.length--
				}
			}
		}
		function processDefaultTag() {
			if (defstart !== -1) {
				if (t.firstdocumentindex === -1) {
					t.firstdocumentindex = lumpindex
				}
				var text = parser.getContent().substr(defstart, defend - defstart);
				justended = false;
				var newlump = newLump();
				newlump.text = text;
				defstart = -1
			}
		}
		t = fluid.XMLViewTemplate();
		init(baseURL, opts.debugMode, cutpoints_in);
		var idpos = template.indexOf(fluid.ID_ATTRIBUTE);
		if (scanStart) {
			var brackpos = template.indexOf(">", idpos);
			parser = fluid.XMLP(template.substring(brackpos + 1))
		} else {
			parser = fluid.XMLP(template)
		}
		parseloop : while (true) {
			var iEvent = parser.next();
			switch (iEvent) {
			case fluid.XMLP._ELM_B:
				processDefaultTag();
				processTagStart(false, "");
				break;
			case fluid.XMLP._ELM_E:
				processDefaultTag();
				processTagEnd();
				break;
			case fluid.XMLP._ELM_EMP:
				processDefaultTag();
				processTagStart(true, "");
				break;
			case fluid.XMLP._PI:
			case fluid.XMLP._DTD:
				defstart = -1;
				continue;
			case fluid.XMLP._TEXT:
			case fluid.XMLP._ENTITY:
			case fluid.XMLP._CDATA:
			case fluid.XMLP._COMMENT:
				if (defstart === -1) {
					defstart = parser.m_cB
				}
				defend = parser.m_cE;
				break;
			case fluid.XMLP._ERROR:
				fluid.setLogging(true);
				var message = "Error parsing template: " + parser.m_cAlt + " at line " + parser.getLineNumber();
				fluid.log(message);
				fluid.log("Just read: " + parser.m_xml.substring(parser.m_iP - 30, parser.m_iP));
				fluid.log("Still to read: " + parser.m_xml.substring(parser.m_iP, parser.m_iP + 30));
				fluid.fail(message);
				break parseloop;
			case fluid.XMLP._NONE:
				break parseloop
			}
		}
		processDefaultTag();
		var excess = tagstack.length - 1;
		if (excess) {
			fluid.fail("Error parsing template - unclosed tag(s) of depth " + (excess) + ": " + fluid.transform(tagstack.splice(1, excess), function (lump) {
					return debugLump(lump)
				}).join(", "))
		}
		return t
	};
	fluid.debugLump = function (lump) {
		var togo = lump.text;
		togo += " at ";
		togo += "lump line " + lump.line + " column " + lump.column + " index " + lump.lumpindex;
		togo += lump.parent.href === null ? "" : " in file " + lump.parent.href;
		return togo
	};
	fluid.ID_ATTRIBUTE = "rsf:id";
	fluid.getPrefix = function (id) {
		var colpos = id.indexOf(":");
		return colpos === -1 ? id : id.substring(0, colpos)
	};
	fluid.SplitID = function (id) {
		var that = {};
		var colpos = id.indexOf(":");
		if (colpos === -1) {
			that.prefix = id
		} else {
			that.prefix = id.substring(0, colpos);
			that.suffix = id.substring(colpos + 1)
		}
		return that
	};
	fluid.XMLViewTemplate = function () {
		return {
			globalmap : {},
			collectmap : {},
			lumps : [],
			firstdocumentindex : -1
		}
	};
	fluid.XMLEncode = function (text) {
		return text.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/\"/g, "&quot;")
	};
	fluid.dumpAttributes = function (attrcopy) {
		var togo = "";
		for (var attrname in attrcopy) {
			var attrvalue = attrcopy[attrname];
			if (attrvalue !== null && attrvalue !== undefined) {
				togo += " " + attrname + '="' + attrvalue + '"'
			}
		}
		return togo
	};
	fluid.aggregateMMap = function (target, source) {
		for (var key in source) {
			var targhas = target[key];
			if (!targhas) {
				target[key] = []
			}
			target[key] = target[key].concat(source[key])
		}
	};
	fluid.parseTemplates = function (resourceSpec, templateList, opts) {
		var togo = [];
		opts = opts || {};
		togo.globalmap = {};
		for (var i = 0; i < templateList.length; ++i) {
			var resource = resourceSpec[templateList[i]];
			var lastslash = resource.href.lastIndexOf("/");
			var baseURL = lastslash === -1 ? "" : resource.href.substring(0, lastslash + 1);
			var template = fluid.parseTemplate(resource.resourceText, baseURL, opts.scanStart && i === 0, resource.cutpoints, opts);
			if (i === 0) {
				fluid.aggregateMMap(togo.globalmap, template.globalmap)
			}
			template.href = resource.href;
			template.baseURL = baseURL;
			template.resourceKey = resource.resourceKey;
			togo[i] = template;
			fluid.aggregateMMap(togo.globalmap, template.rootlump.downmap)
		}
		return togo
	};
	var chars = "(?:[\\w\u0128-\uFFFF*_-]|\\\\.)";
	var quickClass = new RegExp("([#.]?)(" + chars + "+)", "g");
	var childSeg = new RegExp("\\s*(>)?\\s*", "g");
	fluid.parseSelector = function (selstring) {
		var togo = [];
		selstring = $.trim(selstring);
		quickClass.lastIndex = 0;
		var lastIndex = 0;
		while (true) {
			var atNode = [];
			while (true) {
				var segMatch = quickClass.exec(selstring);
				if (!segMatch || segMatch.index !== lastIndex) {
					break
				}
				var thisNode = {};
				var text = segMatch[2];
				if (segMatch[1] === "") {
					thisNode.tag = text
				} else {
					if (segMatch[1] === "#") {
						thisNode.id = text
					} else {
						if (segMatch[1] === ".") {
							thisNode.clazz = text
						}
					}
				}
				atNode[atNode.length] = thisNode;
				lastIndex = quickClass.lastIndex
			}
			childSeg.lastIndex = lastIndex;
			var fullAtNode = {
				predList : atNode
			};
			var childMatch = childSeg.exec(selstring);
			if (!childMatch || childMatch.index !== lastIndex) {
				var remainder = selstring.substring(lastIndex);
				fluid.fail("Error in selector string - can not match child selector expression at " + remainder)
			}
			if (childMatch[1] === ">") {
				fullAtNode.child = true
			}
			togo[togo.length] = fullAtNode;
			if (childSeg.lastIndex >= selstring.length) {
				break
			}
			lastIndex = childSeg.lastIndex;
			quickClass.lastIndex = childSeg.lastIndex
		}
		return togo
	}
})(jQuery, fluid_1_4);
fluid_1_4 = fluid_1_4 || {};
(function ($, fluid) {
	function debugPosition(component) {
		return "as child of " + (component.parent.fullID ? "component with full ID " + component.parent.fullID : "root")
	}
	function computeFullID(component) {
		var togo = "";
		var move = component;
		if (component.children === undefined) {
			togo = component.ID + (component.localID !== undefined ? component.localID : "");
			move = component.parent
		}
		while (move.parent) {
			var parent = move.parent;
			if (move.fullID !== undefined) {
				togo = move.fullID + togo;
				return togo
			}
			if (move.noID === undefined) {
				var ID = move.ID;
				if (ID === undefined) {
					fluid.fail("Error in component tree - component found with no ID " + debugPosition(parent) + ": please check structure")
				}
				var colpos = ID.indexOf(":");
				var prefix = colpos === -1 ? ID : ID.substring(0, colpos);
				togo = prefix + ":" + (move.localID === undefined ? "" : move.localID) + ":" + togo
			}
			move = parent
		}
		return togo
	}
	var renderer = {};
	renderer.isBoundPrimitive = function (value) {
		return fluid.isPrimitive(value) || value instanceof Array && (value.length === 0 || typeof(value[0]) === "string")
	};
	var unzipComponent;
	function processChild(value, key) {
		if (renderer.isBoundPrimitive(value)) {
			return {
				componentType : "UIBound",
				value : value,
				ID : key
			}
		} else {
			var unzip = unzipComponent(value);
			if (unzip.ID) {
				return {
					ID : key,
					componentType : "UIContainer",
					children : [unzip]
				}
			} else {
				unzip.ID = key;
				return unzip
			}
		}
	}
	function fixChildren(children) {
		if (!(children instanceof Array)) {
			var togo = [];
			for (var key in children) {
				var value = children[key];
				if (value instanceof Array) {
					for (var i = 0; i < value.length; ++i) {
						var processed = processChild(value[i], key);
						togo[togo.length] = processed
					}
				} else {
					togo[togo.length] = processChild(value, key)
				}
			}
			return togo
		} else {
			return children
		}
	}
	function fixupValue(uibound, model, resolverGetConfig) {
		if (uibound.value === undefined && uibound.valuebinding !== undefined) {
			if (!model) {
				fluid.fail("Cannot perform value fixup for valuebinding " + uibound.valuebinding + " since no model was supplied to rendering")
			}
			uibound.value = fluid.get(model, uibound.valuebinding, resolverGetConfig)
		}
	}
	function upgradeBound(holder, property, model, resolverGetConfig) {
		if (holder[property] !== undefined) {
			if (renderer.isBoundPrimitive(holder[property])) {
				holder[property] = {
					value : holder[property]
				}
			} else {
				if (holder[property].messagekey) {
					holder[property].componentType = "UIMessage"
				}
			}
		} else {
			holder[property] = {
				value : null
			}
		}
		fixupValue(holder[property], model, resolverGetConfig)
	}
	renderer.duckMap = {
		children : "UIContainer",
		value : "UIBound",
		valuebinding : "UIBound",
		messagekey : "UIMessage",
		markup : "UIVerbatim",
		selection : "UISelect",
		target : "UILink",
		choiceindex : "UISelectChoice",
		functionname : "UIInitBlock"
	};
	var boundMap = {
		UISelect : ["selection", "optionlist", "optionnames"],
		UILink : ["target", "linktext"],
		UIVerbatim : ["markup"],
		UIMessage : ["messagekey"]
	};
	renderer.boundMap = fluid.transform(boundMap, fluid.arrayToHash);
	renderer.inferComponentType = function (component) {
		for (var key in renderer.duckMap) {
			if (component[key] !== undefined) {
				return renderer.duckMap[key]
			}
		}
	};
	renderer.applyComponentType = function (component) {
		component.componentType = renderer.inferComponentType(component);
		if (component.componentType === undefined && component.ID !== undefined) {
			component.componentType = "UIBound"
		}
	};
	unzipComponent = function (component, model, resolverGetConfig) {
		if (component) {
			renderer.applyComponentType(component)
		}
		if (!component || component.componentType === undefined) {
			var decorators = component.decorators;
			if (decorators) {
				delete component.decorators
			}
			component = {
				componentType : "UIContainer",
				children : component
			};
			component.decorators = decorators
		}
		var cType = component.componentType;
		if (cType === "UIContainer") {
			component.children = fixChildren(component.children)
		} else {
			var map = renderer.boundMap[cType];
			if (map) {
				fluid.each(map, function (value, key) {
					upgradeBound(component, key, model, resolverGetConfig)
				})
			}
		}
		return component
	};
	function fixupTree(tree, model, resolverGetConfig) {
		if (tree.componentType === undefined) {
			tree = unzipComponent(tree, model, resolverGetConfig)
		}
		if (tree.componentType !== "UIContainer" && !tree.parent) {
			tree = {
				children : [tree]
			}
		}
		if (tree.children) {
			tree.childmap = {};
			for (var i = 0; i < tree.children.length; ++i) {
				var child = tree.children[i];
				if (child.componentType === undefined) {
					child = unzipComponent(child, model, resolverGetConfig);
					tree.children[i] = child
				}
				child.parent = tree;
				if (child.ID === undefined) {
					fluid.fail("Error in component tree: component found with no ID " + debugPosition(child))
				}
				tree.childmap[child.ID] = child;
				var colpos = child.ID.indexOf(":");
				if (colpos === -1) {}
				else {
					var prefix = child.ID.substring(0, colpos);
					var childlist = tree.childmap[prefix];
					if (!childlist) {
						childlist = [];
						tree.childmap[prefix] = childlist
					}
					if (child.localID === undefined && childlist.length !== 0) {
						child.localID = childlist.length
					}
					childlist[childlist.length] = child
				}
				child.fullID = computeFullID(child);
				var componentType = child.componentType;
				if (componentType === "UISelect") {
					child.selection.fullID = child.fullID + "-selection"
				} else {
					if (componentType === "UIInitBlock") {
						var call = child.functionname + "(";
						for (var j = 0; j < child.arguments.length; ++j) {
							if (child.arguments[j]instanceof fluid.ComponentReference) {
								child.arguments[j] = child.parent.fullID + child.arguments[j].reference
							}
							call += JSON.stringify(child.arguments[j]);
							if (j < child.arguments.length - 1) {
								call += ", "
							}
						}
						child.markup = {
							value : call + ")\n"
						};
						child.componentType = "UIVerbatim"
					} else {
						if (componentType === "UIBound") {
							fixupValue(child, model, resolverGetConfig)
						}
					}
				}
				fixupTree(child, model, resolverGetConfig)
			}
		}
		return tree
	}
	fluid.NULL_STRING = "\u25a9null\u25a9";
	var LINK_ATTRIBUTES = {
		a : "href",
		link : "href",
		img : "src",
		frame : "src",
		script : "src",
		style : "src",
		input : "src",
		embed : "src",
		form : "action",
		applet : "codebase",
		object : "codebase"
	};
	renderer.decoratorComponentPrefix = "**-renderer-";
	renderer.IDtoComponentName = function (ID, num) {
		return renderer.decoratorComponentPrefix + ID.replace(/\./g, "") + "-" + num
	};
	renderer.invokeFluidDecorator = function (func, args, ID, num, options) {
		var that;
		if (options.instantiator && options.parentComponent) {
			var parent = options.parentComponent;
			var name = renderer.IDtoComponentName(ID, num);
			fluid.set(parent, fluid.path("options", "components", name), {
				type : func
			});
			that = fluid.initDependent(options.parentComponent, name, options.instantiator, args)
		} else {
			that = fluid.invokeGlobalFunction(func, args)
		}
		return that
	};
	fluid.renderer = function (templates, tree, options, fossilsIn) {
		options = options || {};
		tree = tree || {};
		var debugMode = options.debugMode;
		if (!options.messageLocator && options.messageSource) {
			options.messageLocator = fluid.resolveMessageSource(options.messageSource)
		}
		options.document = options.document || document;
		var directFossils = fossilsIn || {};
		var globalmap = {};
		var branchmap = {};
		var rewritemap = {};
		var seenset = {};
		var collected = {};
		var out = "";
		var renderOptions = options;
		var decoratorQueue = [];
		var renderedbindings = {};
		var usedIDs = {};
		var that = {};
		function getRewriteKey(template, parent, id) {
			return template.resourceKey + parent.fullID + id
		}
		function resolveInScope(searchID, defprefix, scope, child) {
			var deflump;
			var scopelook = scope ? scope[searchID] : null;
			if (scopelook) {
				for (var i = 0; i < scopelook.length; ++i) {
					var scopelump = scopelook[i];
					if (!deflump && scopelump.rsfID === defprefix) {
						deflump = scopelump
					}
					if (scopelump.rsfID === searchID) {
						return scopelump
					}
				}
			}
			return deflump
		}
		function resolveCall(sourcescope, child) {
			var searchID = child.jointID ? child.jointID : child.ID;
			var split = fluid.SplitID(searchID);
			var defprefix = split.prefix + ":";
			var match = resolveInScope(searchID, defprefix, sourcescope.downmap, child);
			if (match) {
				return match
			}
			if (child.children) {
				match = resolveInScope(searchID, defprefix, globalmap, child);
				if (match) {
					return match
				}
			}
			return null
		}
		function noteCollected(template) {
			if (!seenset[template.href]) {
				fluid.aggregateMMap(collected, template.collectmap);
				seenset[template.href] = true
			}
		}
		var fetchComponent;
		function resolveRecurse(basecontainer, parentlump) {
			for (var i = 0; i < basecontainer.children.length; ++i) {
				var branch = basecontainer.children[i];
				if (branch.children) {
					var resolved = resolveCall(parentlump, branch);
					if (resolved) {
						branchmap[branch.fullID] = resolved;
						var id = resolved.attributemap.id;
						if (id !== undefined) {
							rewritemap[getRewriteKey(parentlump.parent, basecontainer, id)] = branch.fullID
						}
						noteCollected(resolved.parent);
						resolveRecurse(branch, resolved)
					}
				}
			}
			if (parentlump.downmap) {
				for (var id in parentlump.downmap) {
					var lumps = parentlump.downmap[id];
					for (var i = 0; i < lumps.length; ++i) {
						var lump = lumps[i];
						var lumpid = lump.attributemap.id;
						if (lumpid !== undefined && lump.rsfID !== undefined) {
							var resolved = fetchComponent(basecontainer, lump.rsfID);
							if (resolved !== null) {
								var resolveID = resolved.fullID;
								if (resolved.componentType === "UISelect") {
									resolveID = resolveID + "-selection"
								}
								rewritemap[getRewriteKey(parentlump.parent, basecontainer, lumpid)] = resolveID
							}
						}
					}
				}
			}
		}
		function resolveBranches(globalmapp, basecontainer, parentlump) {
			branchmap = {};
			rewritemap = {};
			seenset = {};
			collected = {};
			globalmap = globalmapp;
			branchmap[basecontainer.fullID] = parentlump;
			resolveRecurse(basecontainer, parentlump)
		}
		function dumpTillLump(lumps, start, limit) {
			for (; start < limit; ++start) {
				var text = lumps[start].text;
				if (text) {
					out += lumps[start].text
				}
			}
		}
		function dumpScan(lumps, renderindex, basedepth, closeparent, insideleaf) {
			var start = renderindex;
			while (true) {
				if (renderindex === lumps.length) {
					break
				}
				var lump = lumps[renderindex];
				if (lump.nestingdepth < basedepth) {
					break
				}
				if (lump.rsfID !== undefined) {
					if (!insideleaf) {
						break
					}
					if (insideleaf && lump.nestingdepth > basedepth + (closeparent ? 0 : 1)) {
						fluid.log("Error in component tree - leaf component found to contain further components - at " + lump.toString())
					} else {
						break
					}
				}
				++renderindex
			}
			if (!closeparent && (renderindex === lumps.length || !lumps[renderindex].rsfID)) {
				--renderindex
			}
			dumpTillLump(lumps, start, renderindex);
			return renderindex
		}
		function isPlaceholder(value) {
			return false
		}
		function isValue(value) {
			return value !== null && value !== undefined && !isPlaceholder(value)
		}
		var trc = {};
		function openTag() {
			if (!trc.iselide) {
				out += "<" + trc.uselump.tagname
			}
		}
		function closeTag() {
			if (!trc.iselide) {
				out += "</" + trc.uselump.tagname + ">"
			}
		}
		function renderUnchanged() {
			dumpTillLump(trc.uselump.parent.lumps, trc.uselump.lumpindex + 1, trc.close.lumpindex + (trc.iselide ? 0 : 1))
		}
		function isSelfClose() {
			return trc.endopen.lumpindex === trc.close.lumpindex && fluid.XMLP.closedTags[trc.uselump.tagname]
		}
		function dumpTemplateBody() {
			if (isSelfClose()) {
				if (!trc.iselide) {
					out += "/>"
				}
			} else {
				if (!trc.iselide) {
					out += ">"
				}
				dumpTillLump(trc.uselump.parent.lumps, trc.endopen.lumpindex, trc.close.lumpindex + (trc.iselide ? 0 : 1))
			}
		}
		function replaceAttributes() {
			if (!trc.iselide) {
				out += fluid.dumpAttributes(trc.attrcopy)
			}
			dumpTemplateBody()
		}
		function replaceAttributesOpen() {
			if (trc.iselide) {
				replaceAttributes()
			} else {
				out += fluid.dumpAttributes(trc.attrcopy);
				var selfClose = isSelfClose();
				out += selfClose ? "/>" : ">";
				trc.nextpos = selfClose ? trc.close.lumpindex + 1 : trc.endopen.lumpindex
			}
		}
		function replaceBody(value) {
			out += fluid.dumpAttributes(trc.attrcopy);
			if (!trc.iselide) {
				out += ">"
			}
			out += fluid.XMLEncode(value.toString());
			closeTag()
		}
		function rewriteLeaf(value) {
			if (isValue(value)) {
				replaceBody(value)
			} else {
				replaceAttributes()
			}
		}
		function rewriteLeafOpen(value) {
			if (trc.iselide) {
				rewriteLeaf(trc.value)
			} else {
				if (isValue(value)) {
					replaceBody(value)
				} else {
					replaceAttributesOpen()
				}
			}
		}
		function rewriteUrl(template, url) {
			if (renderOptions.urlRewriter) {
				var rewritten = renderOptions.urlRewriter(url);
				if (rewritten) {
					return rewritten
				}
			}
			if (!renderOptions.rebaseURLs) {
				return url
			}
			var protpos = url.indexOf(":/");
			if (url.charAt(0) === "/" || protpos !== -1 && protpos < 7) {
				return url
			} else {
				return renderOptions.baseURL + url
			}
		}
		function dumpHiddenField(todump) {
			out += '<input type="hidden" ';
			var isvirtual = todump.virtual;
			var outattrs = {};
			outattrs[isvirtual ? "id" : "name"] = todump.name;
			outattrs.value = todump.value;
			out += fluid.dumpAttributes(outattrs);
			out += " />\n"
		}
		var outDecoratorsImpl;
		function applyAutoBind(torender, finalID) {
			if (!finalID) {
				return
			}
			var tagname = trc.uselump.tagname;
			var applier = renderOptions.applier;
			function applyFunc() {
				fluid.applyChange(fluid.byId(finalID), undefined, applier)
			}
			if (renderOptions.autoBind && /input|select|textarea/.test(tagname) && !renderedbindings[finalID]) {
				var decorators = [{
						jQuery : ["change", applyFunc]
					}
				];
				if ($.browser.msie && tagname === "input" && /radio|checkbox/.test(trc.attrcopy.type)) {
					decorators.push({
						jQuery : ["click", applyFunc]
					})
				}
				if ($.browser.safari && tagname === "input" && trc.attrcopy.type === "radio") {
					decorators.push({
						jQuery : ["keyup", applyFunc]
					})
				}
				outDecoratorsImpl(torender, decorators, trc.attrcopy, finalID)
			}
		}
		function dumpBoundFields(torender, parent) {
			if (torender) {
				var holder = parent ? parent : torender;
				if (directFossils && holder.valuebinding) {
					var fossilKey = holder.submittingname || torender.finalID;
					directFossils[fossilKey] = {
						name : fossilKey,
						EL : holder.valuebinding,
						oldvalue : holder.value
					};
					applyAutoBind(torender, torender.finalID)
				}
				if (torender.fossilizedbinding) {
					dumpHiddenField(torender.fossilizedbinding)
				}
				if (torender.fossilizedshaper) {
					dumpHiddenField(torender.fossilizedshaper)
				}
			}
		}
		function dumpSelectionBindings(uiselect) {
			if (!renderedbindings[uiselect.selection.fullID]) {
				renderedbindings[uiselect.selection.fullID] = true;
				dumpBoundFields(uiselect.selection);
				dumpBoundFields(uiselect.optionlist);
				dumpBoundFields(uiselect.optionnames)
			}
		}
		function isSelectedValue(torender, value) {
			var selection = torender.selection;
			return selection.value && typeof(selection.value) !== "string" && typeof(selection.value.length) === "number" ? $.inArray(value, selection.value, value) !== -1 : selection.value === value
		}
		function getRelativeComponent(component, relativeID) {
			component = component.parent;
			while (relativeID.indexOf("..::") === 0) {
				relativeID = relativeID.substring(4);
				component = component.parent
			}
			return component.childmap[relativeID]
		}
		function adjustForID(attrcopy, component, late, forceID) {
			if (!late) {
				delete attrcopy["rsf:id"]
			}
			if (component.finalID !== undefined) {
				attrcopy.id = component.finalID
			} else {
				if (forceID !== undefined) {
					attrcopy.id = forceID
				} else {
					if (attrcopy.id || late) {
						attrcopy.id = component.fullID
					}
				}
			}
			var count = 1;
			var baseid = attrcopy.id;
			while (renderOptions.document.getElementById(attrcopy.id) || usedIDs[attrcopy.id]) {
				attrcopy.id = baseid + "-" + (count++)
			}
			component.finalID = attrcopy.id;
			return attrcopy.id
		}
		function assignSubmittingName(attrcopy, component, parent) {
			var submitting = parent || component;
			adjustForID(attrcopy, component, true, component.fullID);
			if (submitting.submittingname === undefined && submitting.willinput !== false) {
				submitting.submittingname = submitting.finalID || submitting.fullID
			}
			return submitting.submittingname
		}
		function explodeDecorators(decorators) {
			var togo = [];
			if (decorators.type) {
				togo[0] = decorators
			} else {
				for (var key in decorators) {
					if (key === "$") {
						key = "jQuery"
					}
					var value = decorators[key];
					var decorator = {
						type : key
					};
					if (key === "jQuery") {
						decorator.func = value[0];
						decorator.args = value.slice(1)
					} else {
						if (key === "addClass" || key === "removeClass") {
							decorator.classes = value
						} else {
							if (key === "attrs") {
								decorator.attributes = value
							} else {
								if (key === "identify") {
									decorator.key = value
								}
							}
						}
					}
					togo[togo.length] = decorator
				}
			}
			return togo
		}
		outDecoratorsImpl = function (torender, decorators, attrcopy, finalID) {
			renderOptions.idMap = renderOptions.idMap || {};
			for (var i = 0; i < decorators.length; ++i) {
				var decorator = decorators[i];
				var type = decorator.type;
				if (!type) {
					var explodedDecorators = explodeDecorators(decorator);
					outDecoratorsImpl(torender, explodedDecorators, attrcopy, finalID);
					continue
				}
				if (type === "$") {
					type = decorator.type = "jQuery"
				}
				if (type === "jQuery" || type === "event" || type === "fluid") {
					var id = adjustForID(attrcopy, torender, true, finalID);
					if (decorator.ids === undefined) {
						decorator.ids = [];
						decoratorQueue[decoratorQueue.length] = decorator
					}
					decorator.ids.push(id)
				} else {
					if (type === "attrs") {
						fluid.each(decorator.attributes, function (value, key) {
							if (value === null || value === undefined) {
								delete attrcopy[key]
							} else {
								attrcopy[key] = fluid.XMLEncode(value)
							}
						})
					} else {
						if (type === "addClass" || type === "removeClass") {
							var fakeNode = {
								nodeType : 1,
								className : attrcopy["class"] || ""
							};
							$(fakeNode)[type](decorator.classes);
							attrcopy["class"] = fakeNode.className
						} else {
							if (type === "identify") {
								var id = adjustForID(attrcopy, torender, true, finalID);
								renderOptions.idMap[decorator.key] = id
							} else {
								if (type !== "null") {
									fluid.log("Unrecognised decorator of type " + type + " found at component of ID " + finalID)
								}
							}
						}
					}
				}
			}
		};
		function outDecorators(torender, attrcopy) {
			if (!torender.decorators) {
				return
			}
			if (torender.decorators.length === undefined) {
				torender.decorators = explodeDecorators(torender.decorators)
			}
			outDecoratorsImpl(torender, torender.decorators, attrcopy)
		}
		function dumpBranchHead(branch, targetlump) {
			if (targetlump.elide) {
				return
			}
			var attrcopy = {};
			$.extend(true, attrcopy, targetlump.attributemap);
			adjustForID(attrcopy, branch);
			outDecorators(branch, attrcopy);
			out += "<" + targetlump.tagname + " ";
			out += fluid.dumpAttributes(attrcopy);
			out += ">"
		}
		function resolveArgs(args) {
			if (!args) {
				return args
			}
			return fluid.transform(args, function (arg, index) {
				upgradeBound(args, index, renderOptions.model, renderOptions.resolverGetConfig);
				return args[index].value
			})
		}
		function degradeMessage(torender) {
			if (torender.componentType === "UIMessage") {
				torender.componentType = "UIBound";
				if (!renderOptions.messageLocator) {
					torender.value = "[No messageLocator is configured in options - please consult documentation on options.messageSource]"
				} else {
					upgradeBound(torender, "messagekey", renderOptions.model, renderOptions.resolverGetConfig);
					var resArgs = resolveArgs(torender.args);
					torender.value = renderOptions.messageLocator(torender.messagekey.value, resArgs)
				}
			}
		}
		function renderComponent(torender) {
			var attrcopy = trc.attrcopy;
			degradeMessage(torender);
			var componentType = torender.componentType;
			var tagname = trc.uselump.tagname;
			outDecorators(torender, attrcopy);
			function makeFail(torender, end) {
				fluid.fail("Error in component tree - UISelectChoice with id " + torender.fullID + end)
			}
			if (componentType === "UIBound" || componentType === "UISelectChoice") {
				var parent;
				if (torender.choiceindex !== undefined) {
					if (torender.parentRelativeID !== undefined) {
						parent = getRelativeComponent(torender, torender.parentRelativeID);
						if (!parent) {
							makeFail(torender, " has parentRelativeID of " + torender.parentRelativeID + " which cannot be resolved")
						}
					} else {
						makeFail(torender, " does not have parentRelativeID set")
					}
					assignSubmittingName(attrcopy, torender, parent.selection);
					dumpSelectionBindings(parent)
				}
				var submittingname = parent ? parent.selection.submittingname : torender.submittingname;
				if (!parent && torender.valuebinding) {
					submittingname = assignSubmittingName(attrcopy, torender)
				}
				if (tagname === "input" || tagname === "textarea") {
					if (submittingname !== undefined) {
						attrcopy.name = submittingname
					}
				}
				dumpBoundFields(torender, parent ? parent.selection : null);
				if (typeof(torender.value) === "boolean" || attrcopy.type === "radio" || attrcopy.type === "checkbox") {
					var underlyingValue;
					var directValue = torender.value;
					if (torender.choiceindex !== undefined) {
						if (!parent.optionlist.value) {
							fluid.fail("Error in component tree - selection control with full ID " + parent.fullID + " has no values")
						}
						underlyingValue = parent.optionlist.value[torender.choiceindex];
						directValue = isSelectedValue(parent, underlyingValue)
					}
					if (isValue(directValue)) {
						if (directValue) {
							attrcopy.checked = "checked"
						} else {
							delete attrcopy.checked
						}
					}
					attrcopy.value = fluid.XMLEncode(underlyingValue ? underlyingValue : "true");
					rewriteLeaf(null)
				} else {
					if (torender.value instanceof Array) {
						renderUnchanged()
					} else {
						var value = parent ? parent[tagname === "textarea" || tagname === "input" ? "optionlist" : "optionnames"].value[torender.choiceindex] : torender.value;
						if (tagname === "textarea") {
							if (isPlaceholder(value) && torender.willinput) {
								value = ""
							}
							rewriteLeaf(value)
						} else {
							if (tagname === "input") {
								if (torender.willinput || isValue(value)) {
									attrcopy.value = fluid.XMLEncode(String(value))
								}
								rewriteLeaf(null)
							} else {
								delete attrcopy.name;
								rewriteLeafOpen(value)
							}
						}
					}
				}
			} else {
				if (componentType === "UISelect") {
					var ishtmlselect = tagname === "select";
					var ismultiple = false;
					if (torender.selection.value instanceof Array) {
						ismultiple = true;
						if (ishtmlselect) {
							attrcopy.multiple = "multiple"
						}
					}
					var oldid = attrcopy.id;
					assignSubmittingName(attrcopy, torender.selection);
					if (oldid !== undefined) {
						attrcopy.id = oldid
					}
					if (ishtmlselect) {
						if (torender.selection.willinput !== false) {
							attrcopy.name = torender.selection.submittingname
						}
						applyAutoBind(torender, attrcopy.id)
					}
					out += fluid.dumpAttributes(attrcopy);
					if (ishtmlselect) {
						out += ">";
						var values = torender.optionlist.value;
						var names = torender.optionnames === null || torender.optionnames === undefined || !torender.optionnames.value ? values : torender.optionnames.value;
						if (!names || !names.length) {
							fluid.fail("Error in component tree - UISelect component with fullID " + torender.fullID + " does not have optionnames set")
						}
						for (var i = 0; i < names.length; ++i) {
							out += '<option value="';
							var value = values[i];
							if (value === null) {
								value = fluid.NULL_STRING
							}
							out += fluid.XMLEncode(value);
							if (isSelectedValue(torender, value)) {
								out += '" selected="selected'
							}
							out += '">';
							out += fluid.XMLEncode(names[i]);
							out += "</option>\n"
						}
						closeTag()
					} else {
						dumpTemplateBody()
					}
					dumpSelectionBindings(torender)
				} else {
					if (componentType === "UILink") {
						var attrname = LINK_ATTRIBUTES[tagname];
						if (attrname) {
							degradeMessage(torender.target);
							var target = torender.target.value;
							if (!isValue(target)) {
								target = attrcopy[attrname]
							}
							target = rewriteUrl(trc.uselump.parent, target);
							attrcopy[attrname] = fluid.XMLEncode(target)
						}
						var value;
						if (torender.linktext) {
							degradeMessage(torender.linktext);
							value = torender.linktext.value
						}
						if (!isValue(value)) {
							replaceAttributesOpen()
						} else {
							rewriteLeaf(value)
						}
					} else {
						if (torender.markup !== undefined) {
							degradeMessage(torender.markup);
							var rendered = torender.markup.value;
							if (rendered === null) {
								out += fluid.dumpAttributes(attrcopy);
								out += ">";
								renderUnchanged()
							} else {
								if (!trc.iselide) {
									out += fluid.dumpAttributes(attrcopy);
									out += ">"
								}
								out += rendered;
								closeTag()
							}
						}
					}
				}
			}
			if (attrcopy.id !== undefined) {
				usedIDs[attrcopy.id] = true
			}
		}
		function rewriteIDRelation(context) {
			var attrname;
			var attrval = trc.attrcopy["for"];
			if (attrval !== undefined) {
				attrname = "for"
			} else {
				attrval = trc.attrcopy.headers;
				if (attrval !== undefined) {
					attrname = "headers"
				}
			}
			if (!attrname) {
				return
			}
			var tagname = trc.uselump.tagname;
			if (attrname === "for" && tagname !== "label") {
				return
			}
			if (attrname === "headers" && tagname !== "td" && tagname !== "th") {
				return
			}
			var rewritten = rewritemap[getRewriteKey(trc.uselump.parent, context, attrval)];
			if (rewritten !== undefined) {
				trc.attrcopy[attrname] = rewritten
			}
		}
		function renderComment(message) {
			out += ("<!-- " + fluid.XMLEncode(message) + "-->")
		}
		function renderDebugMessage(message) {
			out += '<span style="background-color:#FF466B;color:white;padding:1px;">';
			out += message;
			out += "</span><br/>"
		}
		function reportPath(branch) {
			var path = branch.fullID;
			return !path ? "component tree root" : "full path " + path
		}
		function renderComponentSystem(context, torendero, lump) {
			var lumpindex = lump.lumpindex;
			var lumps = lump.parent.lumps;
			var nextpos = -1;
			var outerendopen = lumps[lumpindex + 1];
			var outerclose = lump.close_tag;
			nextpos = outerclose.lumpindex + 1;
			var payloadlist = lump.downmap ? lump.downmap["payload-component"] : null;
			var payload = payloadlist ? payloadlist[0] : null;
			var iselide = lump.rsfID.charCodeAt(0) === 126;
			var endopen = outerendopen;
			var close = outerclose;
			var uselump = lump;
			var attrcopy = {};
			$.extend(true, attrcopy, (payload === null ? lump : payload).attributemap);
			trc.attrcopy = attrcopy;
			trc.uselump = uselump;
			trc.endopen = endopen;
			trc.close = close;
			trc.nextpos = nextpos;
			trc.iselide = iselide;
			rewriteIDRelation(context);
			if (torendero === null) {
				if (lump.rsfID.indexOf("scr=") === (iselide ? 1 : 0)) {
					var scrname = lump.rsfID.substring(4 + (iselide ? 1 : 0));
					if (scrname === "ignore") {
						nextpos = trc.close.lumpindex + 1
					} else {
						if (scrname === "rewrite-url") {
							torendero = {
								componentType : "UILink",
								target : {}
								
							}
						} else {
							openTag();
							replaceAttributesOpen();
							nextpos = trc.endopen.lumpindex
						}
					}
				}
			}
			if (torendero !== null) {
				if (payload) {
					trc.endopen = lumps[payload.lumpindex + 1];
					trc.close = payload.close_tag;
					trc.uselump = payload;
					dumpTillLump(lumps, lumpindex, payload.lumpindex);
					lumpindex = payload.lumpindex
				}
				adjustForID(attrcopy, torendero);
				openTag();
				renderComponent(torendero);
				if (payload !== null) {
					if (trc.nextpos === nextpos) {
						dumpTillLump(lumps, trc.close.lumpindex + 1, outerclose.lumpindex + 1)
					}
				}
				nextpos = trc.nextpos
			}
			return nextpos
		}
		var renderRecurse;
		function renderContainer(child, targetlump) {
			var t2 = targetlump.parent;
			var firstchild = t2.lumps[targetlump.lumpindex + 1];
			if (child.children !== undefined) {
				dumpBranchHead(child, targetlump)
			} else {
				renderComponentSystem(child.parent, child, targetlump)
			}
			renderRecurse(child, targetlump, firstchild)
		}
		fetchComponent = function (basecontainer, id, lump) {
			if (id.indexOf("msg=") === 0) {
				var key = id.substring(4);
				return {
					componentType : "UIMessage",
					messagekey : key
				}
			}
			while (basecontainer) {
				var togo = basecontainer.childmap[id];
				if (togo) {
					return togo
				}
				basecontainer = basecontainer.parent
			}
			return null
		};
		function fetchComponents(basecontainer, id) {
			var togo;
			while (basecontainer) {
				togo = basecontainer.childmap[id];
				if (togo) {
					break
				}
				basecontainer = basecontainer.parent
			}
			return togo
		}
		function findChild(sourcescope, child) {
			var split = fluid.SplitID(child.ID);
			var headlumps = sourcescope.downmap[child.ID];
			if (!headlumps) {
				headlumps = sourcescope.downmap[split.prefix + ":"]
			}
			return headlumps ? headlumps[0] : null
		}
		renderRecurse = function (basecontainer, parentlump, baselump) {
			var renderindex = baselump.lumpindex;
			var basedepth = parentlump.nestingdepth;
			var t1 = parentlump.parent;
			var rendered;
			if (debugMode) {
				rendered = {}
				
			}
			while (true) {
				renderindex = dumpScan(t1.lumps, renderindex, basedepth, !parentlump.elide, false);
				if (renderindex === t1.lumps.length) {
					break
				}
				var lump = t1.lumps[renderindex];
				var id = lump.rsfID;
				if (lump.nestingdepth < basedepth || id === undefined) {
					break
				}
				if (id.charCodeAt(0) === 126) {
					id = id.substring(1)
				}
				if (id.indexOf(":") !== -1) {
					var prefix = fluid.getPrefix(id);
					var children = fetchComponents(basecontainer, prefix);
					var finallump = lump.uplump.finallump[prefix];
					var closefinal = finallump.close_tag;
					if (children) {
						for (var i = 0; i < children.length; ++i) {
							var child = children[i];
							if (child.children) {
								if (debugMode) {
									rendered[child.fullID] = true
								}
								var targetlump = branchmap[child.fullID];
								if (targetlump) {
									if (debugMode) {
										renderComment("Branching for " + child.fullID + " from " + fluid.debugLump(lump) + " to " + fluid.debugLump(targetlump))
									}
									renderContainer(child, targetlump);
									if (debugMode) {
										renderComment("Branch returned for " + child.fullID + fluid.debugLump(lump) + " to " + fluid.debugLump(targetlump))
									}
								} else {
									if (debugMode) {
										renderDebugMessage("No matching template branch found for branch container with full ID " + child.fullID + " rendering from parent template branch " + fluid.debugLump(baselump))
									}
								}
							} else {
								var targetlump = findChild(parentlump, child);
								if (!targetlump) {
									if (debugMode) {
										renderDebugMessage("Repetitive leaf with full ID " + child.fullID + " could not be rendered from parent template branch " + fluid.debugLump(baselump))
									}
									continue
								}
								var renderend = renderComponentSystem(basecontainer, child, targetlump);
								var wasopentag = renderend < t1.lumps.lengtn && t1.lumps[renderend].nestingdepth >= targetlump.nestingdepth;
								var newbase = child.children ? child : basecontainer;
								if (wasopentag) {
									renderRecurse(newbase, targetlump, t1.lumps[renderend]);
									renderend = targetlump.close_tag.lumpindex + 1
								}
								if (i !== children.length - 1) {
									if (renderend < closefinal.lumpindex) {
										dumpScan(t1.lumps, renderend, targetlump.nestingdepth - 1, false, false)
									}
								} else {
									dumpScan(t1.lumps, renderend, targetlump.nestingdepth, true, false)
								}
							}
						}
					} else {
						if (debugMode) {
							renderDebugMessage("No branch container with prefix " + prefix + ": found in container " + reportPath(basecontainer) + " rendering at template position " + fluid.debugLump(baselump) + ", skipping")
						}
					}
					renderindex = closefinal.lumpindex + 1;
					if (debugMode) {
						renderComment("Stack returned from branch for ID " + id + " to " + fluid.debugLump(baselump) + ": skipping from " + fluid.debugLump(lump) + " to " + fluid.debugLump(closefinal))
					}
				} else {
					var component;
					if (id) {
						component = fetchComponent(basecontainer, id, lump);
						if (debugMode && component) {
							rendered[component.fullID] = true
						}
					}
					if (component && component.children !== undefined) {
						renderContainer(component);
						renderindex = lump.close_tag.lumpindex + 1
					} else {
						renderindex = renderComponentSystem(basecontainer, component, lump)
					}
				}
				if (renderindex === t1.lumps.length) {
					break
				}
			}
			if (debugMode) {
				var children = basecontainer.children;
				for (var key = 0; key < children.length; ++key) {
					var child = children[key];
					if (!rendered[child.fullID]) {
						renderDebugMessage("Component " + child.componentType + " with full ID " + child.fullID + " could not be found within template " + fluid.debugLump(baselump))
					}
				}
			}
		};
		function renderCollect(collump) {
			dumpTillLump(collump.parent.lumps, collump.lumpindex, collump.close_tag.lumpindex + 1)
		}
		function renderCollects() {
			for (var key in collected) {
				var collist = collected[key];
				for (var i = 0; i < collist.length; ++i) {
					renderCollect(collist[i])
				}
			}
		}
		function processDecoratorQueue() {
			for (var i = 0; i < decoratorQueue.length; ++i) {
				var decorator = decoratorQueue[i];
				for (var j = 0; j < decorator.ids.length; ++j) {
					var id = decorator.ids[j];
					var node = fluid.byId(id, renderOptions.document);
					if (!node) {
						fluid.fail("Error during rendering - component with id " + id + " which has a queued decorator was not found in the output markup")
					}
					if (decorator.type === "jQuery") {
						var jnode = $(node);
						jnode[decorator.func].apply(jnode, $.makeArray(decorator.args))
					} else {
						if (decorator.type === "fluid") {
							var args = decorator.args;
							if (!args) {
								if (!decorator.container) {
									decorator.container = $(node)
								} else {
									decorator.container.push(node)
								}
								args = [node, decorator.options]
							}
							var that = renderer.invokeFluidDecorator(decorator.func, args, id, i, options);
							decorator.that = that
						} else {
							if (decorator.type === "event") {
								node[decorator.event] = decorator.handler
							}
						}
					}
				}
			}
		}
		that.renderTemplates = function () {
			tree = fixupTree(tree, options.model, options.resolverGetConfig);
			var template = templates[0];
			resolveBranches(templates.globalmap, tree, template.rootlump);
			renderedbindings = {};
			renderCollects();
			renderRecurse(tree, template.rootlump, template.lumps[template.firstdocumentindex]);
			return out
		};
		that.processDecoratorQueue = function () {
			processDecoratorQueue()
		};
		return that
	};
	jQuery.extend(true, fluid.renderer, renderer);
	fluid.ComponentReference = function (reference) {
		this.reference = reference
	};
	fluid.explode = function (hash, basepath) {
		var togo = [];
		for (var key in hash) {
			var binding = basepath === undefined ? key : basepath + "." + key;
			togo[togo.length] = {
				ID : key,
				value : hash[key],
				valuebinding : binding
			}
		}
		return togo
	};
	fluid.explodeSelectionToInputs = function (optionlist, opts) {
		return fluid.transform(optionlist, function (option, index) {
			return {
				ID : opts.rowID,
				children : [{
						ID : opts.inputID,
						parentRelativeID : "..::" + opts.selectID,
						choiceindex : index
					}, {
						ID : opts.labelID,
						parentRelativeID : "..::" + opts.selectID,
						choiceindex : index
					}
				]
			}
		})
	};
	fluid.resolveMessageSource = function (messageSource) {
		if (messageSource.type === "data") {
			if (messageSource.url === undefined) {
				return fluid.messageLocator(messageSource.messages, messageSource.resolveFunc)
			} else {}
			
		} else {
			if (messageSource.type === "resolver") {
				return messageSource.resolver.resolve
			}
		}
	};
	fluid.renderTemplates = function (templates, tree, options, fossilsIn) {
		var renderer = fluid.renderer(templates, tree, options, fossilsIn);
		var rendered = renderer.renderTemplates();
		return rendered
	};
	fluid.reRender = function (templates, node, tree, options) {
		options = options || {};
		node = fluid.unwrap(node);
		var lastFocusedElement = fluid.getLastFocusedElement ? fluid.getLastFocusedElement() : null;
		var lastId;
		if (lastFocusedElement && fluid.dom.isContainer(node, lastFocusedElement)) {
			lastId = lastFocusedElement.id
		}
		if ($.browser.msie) {
			$(node).empty()
		} else {
			node.innerHTML = ""
		}
		var fossils = options.fossils || {};
		var renderer = fluid.renderer(templates, tree, options, fossils);
		var rendered = renderer.renderTemplates();
		if (options.renderRaw) {
			rendered = fluid.XMLEncode(rendered);
			rendered = rendered.replace(/\n/g, "<br/>")
		}
		if (options.model) {
			fluid.bindFossils(node, options.model, fossils)
		}
		if ($.browser.msie) {
			$(node).html(rendered)
		} else {
			node.innerHTML = rendered
		}
		renderer.processDecoratorQueue();
		if (lastId) {
			var element = fluid.byId(lastId);
			if (element) {
				$(element).focus()
			}
		}
		return templates
	};
	function findNodeValue(rootNode) {
		var node = fluid.dom.iterateDom(rootNode, function (node) {
				return node.nodeType === 8 || node.nodeType === 4 ? "stop" : null
			}, true);
		var value = node.nodeValue;
		if (value.indexOf("[CDATA[") === 0) {
			return value.substring(6, value.length - 2)
		} else {
			return value
		}
	}
	fluid.extractTemplate = function (node, armouring) {
		if (!armouring) {
			return node.innerHTML
		} else {
			return findNodeValue(node)
		}
	};
	fluid.render = function (source, target, tree, options) {
		options = options || {};
		var template = source;
		if (typeof(source) === "object") {
			template = fluid.extractTemplate(fluid.unwrap(source.node), source.armouring)
		}
		target = fluid.unwrap(target);
		var resourceSpec = {
			base : {
				resourceText : template,
				href : ".",
				resourceKey : ".",
				cutpoints : options.cutpoints
			}
		};
		var templates = fluid.parseTemplates(resourceSpec, ["base"], options);
		return fluid.reRender(templates, target, tree, options)
	};
	fluid.selfRender = function (node, tree, options) {
		options = options || {};
		return fluid.render({
			node : node,
			armouring : options.armouring
		}, node, tree, options)
	}
})(jQuery, fluid_1_4);
fluid_1_4 = fluid_1_4 || {};
(function ($, fluid) {
	if (!fluid.renderer) {
		fluid.fail("fluidRenderer.js is a necessary dependency of RendererUtilities")
	}
	fluid.iota = function (count, first) {
		first = first || 0;
		var togo = [];
		for (var i = 0; i < count; ++i) {
			togo[togo.length] = first++
		}
		return togo
	};
	fluid.renderer.visitDecorators = function (that, visitor) {
		fluid.visitComponentChildren(that, function (component, name) {
			if (name.indexOf(fluid.renderer.decoratorComponentPrefix) === 0) {
				visitor(component, name)
			}
		}, {
			flat : true
		})
	};
	fluid.renderer.clearDecorators = function (instantiator, that) {
		fluid.renderer.visitDecorators(that, function (component, name) {
			instantiator.clearComponent(that, name)
		})
	};
	fluid.renderer.getDecoratorComponents = function (that) {
		var togo = {};
		fluid.renderer.visitDecorators(that, function (component, name) {
			togo[name] = component
		});
		return togo
	};
	fluid.renderer.modeliseOptions = function (options, defaults, baseOptions) {
		return $.extend({}, defaults, options, fluid.filterKeys(baseOptions, ["model", "applier"]))
	};
	fluid.renderer.reverseMerge = function (target, source, names) {
		names = fluid.makeArray(names);
		fluid.each(names, function (name) {
			if (target[name] === undefined && source[name] !== undefined) {
				target[name] = source[name]
			}
		})
	};
	fluid.renderer.createRendererSubcomponent = function (container, selectors, options, baseObject, fossils) {
		options = options || {};
		var source = options.templateSource ? options.templateSource : {
			node : $(container)
		};
		var rendererOptions = fluid.renderer.modeliseOptions(options.rendererOptions, null, baseObject);
		rendererOptions.fossils = fossils || {};
		var expanderOptions = fluid.renderer.modeliseOptions(options.expanderOptions, {
				ELstyle : "${}"
			}, baseObject);
		fluid.renderer.reverseMerge(expanderOptions, options, ["resolverGetConfig", "resolverSetConfig"]);
		var that = {};
		if (!options.noexpand) {
			that.expander = fluid.renderer.makeProtoExpander(expanderOptions)
		}
		var templates = null;
		that.render = function (tree) {
			var cutpointFn = options.cutpointGenerator || "fluid.renderer.selectorsToCutpoints";
			rendererOptions.cutpoints = rendererOptions.cutpoints || fluid.invokeGlobalFunction(cutpointFn, [selectors, options]);
			container = typeof(container) === "function" ? container() : $(container);
			if (templates) {
				fluid.clear(rendererOptions.fossils);
				fluid.reRender(templates, container, tree, rendererOptions)
			} else {
				if (typeof(source) === "function") {
					source = source()
				}
				templates = fluid.render(source, container, tree, rendererOptions)
			}
		};
		return that
	};
	fluid.defaults("fluid.rendererComponent", {
		gradeNames : ["fluid.viewComponent"],
		initFunction : "fluid.initRendererComponent",
		mergePolicy : {
			protoTree : "noexpand, replace"
		},
		rendererOptions : {
			autoBind : true
		},
		events : {
			prepareModelForRender : null,
			onRenderTree : null,
			afterRender : null,
			produceTree : "unicast"
		}
	});
	fluid.initRendererComponent = function (componentName, container, options) {
		var that = fluid.initView(componentName, container, options, {
				gradeNames : ["fluid.rendererComponent"]
			});
		fluid.fetchResources(that.options.resources);
		var rendererOptions = fluid.renderer.modeliseOptions(that.options.rendererOptions, null, that);
		if (!that.options.noUpgradeDecorators) {
			fluid.withInstantiator(that, function (currentInst) {
				rendererOptions.instantiator = currentInst;
				rendererOptions.parentComponent = that
			})
		}
		var messageResolver;
		if (!rendererOptions.messageSource && that.options.strings) {
			messageResolver = fluid.messageResolver({
					messageBase : that.options.strings,
					resolveFunc : that.options.messageResolverFunction,
					parents : fluid.makeArray(that.options.parentBundle)
				});
			rendererOptions.messageSource = {
				type : "resolver",
				resolver : messageResolver
			}
		}
		fluid.renderer.reverseMerge(rendererOptions, that.options, ["resolverGetConfig", "resolverSetConfig"]);
		var rendererFnOptions = $.extend({}, that.options.rendererFnOptions, {
				rendererOptions : rendererOptions,
				repeatingSelectors : that.options.repeatingSelectors,
				selectorsToIgnore : that.options.selectorsToIgnore,
				expanderOptions : {
					envAdd : {
						styles : that.options.styles
					}
				}
			});
		if (that.options.resources && that.options.resources.template) {
			rendererFnOptions.templateSource = function () {
				return that.options.resources.template.resourceText
			}
		}
		var produceTree = that.events.produceTree;
		produceTree.addListener(function () {
			return that.options.protoTree
		});
		if (that.options.produceTree) {
			produceTree.addListener(that.options.produceTree)
		}
		fluid.renderer.reverseMerge(rendererFnOptions, that.options, ["resolverGetConfig", "resolverSetConfig"]);
		if (rendererFnOptions.rendererTargetSelector) {
			container = function () {
				return that.dom.locate(rendererFnOptions.rendererTargetSelector)
			}
		}
		var renderer = {
			fossils : {},
			boundPathForNode : function (node) {
				return fluid.boundPathForNode(node, renderer.fossils)
			}
		};
		var rendererSub = fluid.renderer.createRendererSubcomponent(container, that.options.selectors, rendererFnOptions, that, renderer.fossils);
		that.renderer = $.extend(renderer, rendererSub);
		if (messageResolver) {
			that.messageResolver = messageResolver
		}
		that.refreshView = renderer.refreshView = function () {
			if (rendererOptions.instantiator && rendererOptions.parentComponent) {
				fluid.renderer.clearDecorators(rendererOptions.instantiator, rendererOptions.parentComponent)
			}
			that.events.prepareModelForRender.fire(that.model, that.applier, that);
			var tree = produceTree.fire(that);
			if (that.renderer.expander) {
				tree = that.renderer.expander(tree)
			}
			that.events.onRenderTree.fire(that, tree);
			that.renderer.render(tree);
			that.events.afterRender.fire(that)
		};
		if (that.options.renderOnInit) {
			that.refreshView()
		}
		return that
	};
	var removeSelectors = function (selectors, selectorsToIgnore) {
		fluid.each(fluid.makeArray(selectorsToIgnore), function (selectorToIgnore) {
			delete selectors[selectorToIgnore]
		});
		return selectors
	};
	var markRepeated = function (selectorKey, repeatingSelectors) {
		if (repeatingSelectors) {
			fluid.each(repeatingSelectors, function (repeatingSelector) {
				if (selectorKey === repeatingSelector) {
					selectorKey = selectorKey + ":"
				}
			})
		}
		return selectorKey
	};
	fluid.renderer.selectorsToCutpoints = function (selectors, options) {
		var togo = [];
		options = options || {};
		selectors = fluid.copy(selectors);
		if (options.selectorsToIgnore) {
			selectors = removeSelectors(selectors, options.selectorsToIgnore)
		}
		for (var selectorKey in selectors) {
			togo.push({
				id : markRepeated(selectorKey, options.repeatingSelectors),
				selector : selectors[selectorKey]
			})
		}
		return togo
	};
	fluid.renderer.NO_COMPONENT = {};
	fluid.renderer.mergeComponents = function (target, source) {
		for (var key in source) {
			target[key] = source[key]
		}
		return target
	};
	fluid.registerNamespace("fluid.renderer.selection");
	fluid.renderer.selection.inputs = function (options, container, key, config) {
		fluid.expect("Selection to inputs expander", ["selectID", "inputID", "labelID", "rowID"], options);
		var selection = config.expander(options.tree);
		var rows = fluid.transform(selection.optionlist.value, function (option, index) {
				var togo = {};
				var element = {
					parentRelativeID : "..::" + options.selectID,
					choiceindex : index
				};
				togo[options.inputID] = element;
				togo[options.labelID] = fluid.copy(element);
				return togo
			});
		var togo = {};
		togo[options.selectID] = selection;
		togo[options.rowID] = {
			children : rows
		};
		togo = config.expander(togo);
		return togo
	};
	fluid.renderer.repeat = function (options, container, key, config) {
		fluid.expect("Repetition expander", ["controlledBy", "tree"], options);
		var path = fluid.extractContextualPath(options.controlledBy, {
				ELstyle : "ALL"
			}, fluid.threadLocal());
		var list = fluid.get(config.model, path, config.resolverGetConfig);
		var togo = {};
		if (!list || list.length === 0) {
			return options.ifEmpty ? config.expander(options.ifEmpty) : togo
		}
		var expanded = [];
		fluid.each(list, function (element, i) {
			var EL = fluid.model.composePath(path, i);
			var envAdd = {};
			if (options.pathAs) {
				envAdd[options.pathAs] = EL
			}
			if (options.valueAs) {
				envAdd[options.valueAs] = fluid.get(config.model, EL, config.resolverGetConfig)
			}
			var expandrow = fluid.withEnvironment(envAdd, function () {
					return config.expander(options.tree)
				}, "rendererEnvironment");
			if (fluid.isArrayable(expandrow)) {
				if (expandrow.length > 0) {
					expanded.push({
						children : expandrow
					})
				}
			} else {
				if (expandrow !== fluid.renderer.NO_COMPONENT) {
					expanded.push(expandrow)
				}
			}
		});
		var repeatID = options.repeatID;
		if (repeatID.indexOf(":") === -1) {
			repeatID = repeatID + ":"
		}
		fluid.each(expanded, function (entry) {
			entry.ID = repeatID
		});
		return expanded
	};
	fluid.renderer.condition = function (options, container, key, config) {
		fluid.expect("Selection to condition expander", ["condition"], options);
		var condition;
		if (options.condition.funcName) {
			var args = config.expandLight(options.condition.args);
			condition = fluid.invoke(options.condition.funcName, args)
		} else {
			if (options.condition.expander) {
				condition = config.expander(options.condition)
			} else {
				condition = config.expandLight(options.condition)
			}
		}
		var tree = (condition ? options.trueTree : options.falseTree);
		if (!tree) {
			tree = fluid.renderer.NO_COMPONENT
		}
		return config.expander(tree)
	};
	fluid.extractContextualPath = function (string, options, env) {
		var parsed = fluid.extractELWithContext(string, options);
		if (parsed) {
			if (parsed.context) {
				var fetched = env[parsed.context];
				if (typeof(fetched) !== "string") {
					fluid.fail("Could not look up context path named " + parsed.context + " to string value")
				}
				return fluid.model.composePath(fetched, parsed.path)
			} else {
				return parsed.path
			}
		}
	};
	fluid.renderer.makeProtoExpander = function (expandOptions) {
		var options = $.extend({
				ELstyle : "${}"
			}, expandOptions);
		options.fetcher = fluid.makeEnvironmentFetcher("rendererEnvironment", options.model);
		var IDescape = options.IDescape || "\\";
		function fetchEL(string) {
			var env = fluid.threadLocal().rendererEnvironment;
			return fluid.extractContextualPath(string, options, env)
		}
		var expandLight = function (source) {
			return fluid.resolveEnvironment(source, options)
		};
		var expandBound = function (value, concrete) {
			if (value.messagekey !== undefined) {
				return {
					componentType : "UIMessage",
					messagekey : expandBound(value.messagekey),
					args : expandLight(value.args)
				}
			}
			var proto;
			if (!fluid.isPrimitive(value) && !fluid.isArrayable(value)) {
				proto = $.extend({}, value);
				if (proto.decorators) {
					proto.decorators = expandLight(proto.decorators)
				}
				value = proto.value;
				delete proto.value
			} else {
				proto = {}
				
			}
			var EL = typeof(value) === "string" ? fetchEL(value) : null;
			if (EL) {
				proto.valuebinding = EL
			} else {
				if (value !== undefined) {
					proto.value = value
				}
			}
			if (options.model && proto.valuebinding && proto.value === undefined) {
				proto.value = fluid.get(options.model, proto.valuebinding, options.resolverGetConfig)
			}
			if (concrete) {
				proto.componentType = "UIBound"
			}
			return proto
		};
		options.filter = fluid.expander.lightFilter;
		var expandCond;
		var expandLeafOrCond;
		var expandEntry = function (entry) {
			var comp = [];
			expandCond(entry, comp);
			return {
				children : comp
			}
		};
		var expandExternal = function (entry) {
			if (entry === fluid.renderer.NO_COMPONENT) {
				return entry
			}
			var singleTarget;
			var target = [];
			var pusher = function (comp) {
				singleTarget = comp
			};
			expandLeafOrCond(entry, target, pusher);
			return singleTarget || target
		};
		var expandConfig = {
			model : options.model,
			resolverGetConfig : options.resolverGetConfig,
			resolverSetConfig : options.resolverSetConfig,
			expander : expandExternal,
			expandLight : expandLight
		};
		var expandLeaf = function (leaf, componentType) {
			var togo = {
				componentType : componentType
			};
			var map = fluid.renderer.boundMap[componentType] || {};
			for (var key in leaf) {
				if (/decorators|args/.test(key)) {
					togo[key] = expandLight(leaf[key]);
					continue
				} else {
					if (map[key]) {
						togo[key] = expandBound(leaf[key])
					} else {
						togo[key] = leaf[key]
					}
				}
			}
			return togo
		};
		var expandChildren = function (entry, pusher) {
			var children = entry.children;
			for (var i = 0; i < children.length; ++i) {
				var target = [];
				var comp = {
					children : target
				};
				var child = children[i];
				var childPusher = function (comp) {
					target[target.length] = comp
				};
				expandLeafOrCond(child, target, childPusher);
				if (comp.children.length === 1 && !comp.children[0].ID) {
					comp = comp.children[0]
				}
				pusher(comp)
			}
		};
		function detectBareBound(entry) {
			return fluid.find(entry, function (value, key) {
				return key === "decorators"
			}) !== false
		}
		var expandLeafOrCond = function (entry, target, pusher) {
			var componentType = fluid.renderer.inferComponentType(entry);
			if (!componentType && (fluid.isPrimitive(entry) || detectBareBound(entry))) {
				componentType = "UIBound"
			}
			if (componentType) {
				pusher(componentType === "UIBound" ? expandBound(entry, true) : expandLeaf(entry, componentType))
			} else {
				if (!target) {
					fluid.fail("Illegal cond->cond transition")
				}
				expandCond(entry, target)
			}
		};
		expandCond = function (proto, target) {
			for (var key in proto) {
				var entry = proto[key];
				if (key.charAt(0) === IDescape) {
					key = key.substring(1)
				}
				if (key === "expander") {
					var expanders = fluid.makeArray(entry);
					fluid.each(expanders, function (expander) {
						var expanded = fluid.invokeGlobalFunction(expander.type, [expander, proto, key, expandConfig]);
						if (expanded !== fluid.renderer.NO_COMPONENT) {
							fluid.each(expanded, function (el) {
								target[target.length] = el
							})
						}
					})
				} else {
					if (entry) {
						var condPusher = function (comp) {
							comp.ID = key;
							target[target.length] = comp
						};
						if (entry.children) {
							if (key.indexOf(":") === -1) {
								key = key + ":"
							}
							expandChildren(entry, condPusher)
						} else {
							if (fluid.renderer.isBoundPrimitive(entry)) {
								condPusher(expandBound(entry, true))
							} else {
								expandLeafOrCond(entry, null, condPusher)
							}
						}
					}
				}
			}
		};
		return function (entry) {
			var initEnvironment = $.extend({}, options.envAdd);
			return fluid.withEnvironment({
				rendererEnvironment : initEnvironment
			}, function () {
				return expandEntry(entry)
			})
		}
	}
})(jQuery, fluid_1_4);
var fluid_1_4 = fluid_1_4 || {};
(function ($, fluid) {
	fluid.registerNamespace("fluid.browser");
	fluid.browser.binaryXHR = function () {
		var canSendBinary = window.FormData || (window.XMLHttpRequest && window.XMLHttpRequest.prototype && window.XMLHttpRequest.prototype.sendAsBinary);
		return canSendBinary ? fluid.typeTag("fluid.browser.supportsBinaryXHR") : undefined
	};
	fluid.browser.formData = function () {
		return window.FormData ? fluid.typeTag("fluid.browser.supportsFormData") : undefined
	};
	fluid.browser.flash = function () {
		var hasModernFlash = (typeof(swfobject) !== "undefined") && (swfobject.getFlashPlayerVersion().major > 8);
		return hasModernFlash ? fluid.typeTag("fluid.browser.supportsFlash") : undefined
	};
	fluid.progressiveChecker = function (options) {
		var that = fluid.initLittleComponent("fluid.progressiveChecker", options);
		return fluid.typeTag(fluid.find(that.options.checks, function (check) {
				if (check.feature) {
					return check.contextName
				}
			}, that.options.defaultContextName))
	};
	fluid.defaults("fluid.progressiveChecker", {
		gradeNames : "fluid.typeFount",
		checks : [],
		defaultContextName : undefined
	});
	fluid.progressiveCheckerForComponent = function (options) {
		var that = fluid.initLittleComponent("fluid.progressiveCheckerForComponent", options);
		var defaults = fluid.defaults(that.options.componentName);
		return fluid.progressiveChecker(fluid.expandOptions(defaults.progressiveCheckerOptions, that))
	};
	fluid.defaults("fluid.progressiveCheckerForComponent", {
		gradeNames : "fluid.typeFount"
	});
	$("head").append("<style type='text/css'>.fl-progEnhance-basic, .fl-ProgEnhance-basic { display: none; } .fl-progEnhance-enhanced, .fl-ProgEnhance-enhanced { display: block; }</style>");
	var features = {
		supportsBinaryXHR : fluid.browser.binaryXHR(),
		supportsFormData : fluid.browser.formData(),
		supportsFlash : fluid.browser.flash()
	};
	fluid.merge(null, fluid.staticEnvironment, features)
})(jQuery, fluid_1_4);
(function ($) {
	var increments = 0;
	$.widget("ui.tooltip", {
		options : {
			items : "[title]",
			content : function () {
				return $(this).attr("title")
			},
			position : {
				my : "left center",
				at : "right center",
				offset : "15 0"
			}
		},
		_create : function () {
			var self = this;
			this.tooltip = $("<div></div>").attr("id", "ui-tooltip-" + increments++).attr("role", "tooltip").attr("aria-hidden", "true").addClass("ui-tooltip ui-widget ui-corner-all ui-widget-content").appendTo(document.body).hide();
			this.tooltipContent = $("<div></div>").addClass("ui-tooltip-content").appendTo(this.tooltip);
			this.opacity = this.tooltip.css("opacity");
			this.element.bind("focus.tooltip mouseover.tooltip", function (event) {
				self.open(event)
			}).bind("blur.tooltip mouseout.tooltip", function (event) {
				self.close(event)
			})
		},
		enable : function () {
			this.options.disabled = false
		},
		disable : function () {
			this.options.disabled = true
		},
		destroy : function () {
			this.tooltip.remove();
			$.Widget.prototype.destroy.apply(this, arguments)
		},
		widget : function () {
			return this.element.pushStack(this.tooltip.get())
		},
		open : function (event) {
			var target = $(event && event.target || this.element).closest(this.options.items);
			if (this.current && this.current[0] == target[0]) {
				return
			}
			var self = this;
			this.current = target;
			this.currentTitle = target.attr("title");
			var content = this.options.content.call(target[0], function (response) {
					setTimeout(function () {
						if (self.current == target) {
							self._show(event, target, response)
						}
					}, 13)
				});
			if (content) {
				self._show(event, target, content)
			}
		},
		_show : function (event, target, content) {
			if (!content) {
				return
			}
			target.attr("title", "");
			if (this.options.disabled) {
				return
			}
			this.tooltipContent.html(content);
			this.tooltip.css({
				top : 0,
				left : 0
			}).show().position($.extend({
					of : target
				}, this.options.position)).hide();
			this.tooltip.attr("aria-hidden", "false");
			target.attr("aria-describedby", this.tooltip.attr("id"));
			this.tooltip.stop(false, true).fadeIn();
			this._trigger("open", event)
		},
		close : function (event) {
			if (!this.current) {
				return
			}
			var current = this.current;
			this.current = null;
			current.attr("title", this.currentTitle);
			if (this.options.disabled) {
				return
			}
			current.removeAttr("aria-describedby");
			this.tooltip.attr("aria-hidden", "true");
			this.tooltip.stop(false, true).fadeOut();
			this._trigger("close", event)
		}
	})
})(jQuery);
var fluid_1_4 = fluid_1_4 || {};
(function ($, fluid) {
	var STATE_INITIAL = "state_initial",
	STATE_CHANGED = "state_changed",
	STATE_REVERTED = "state_reverted";
	function defaultRenderer(that, targetContainer) {
		var str = that.options.strings;
		var markup = "<span class='flc-undo'><a href='#' class='flc-undo-undoControl'>" + str.undo + "</a><a href='#' class='flc-undo-redoControl'>" + str.redo + "</a></span>";
		var markupNode = $(markup).attr({
				role : "region",
				"aria-live" : "polite",
				"aria-relevant" : "all"
			});
		targetContainer.append(markupNode);
		return markupNode
	}
	function refreshView(that) {
		if (that.state === STATE_INITIAL) {
			that.locate("undoContainer").hide();
			that.locate("redoContainer").hide()
		} else {
			if (that.state === STATE_CHANGED) {
				that.locate("undoContainer").show();
				that.locate("redoContainer").hide()
			} else {
				if (that.state === STATE_REVERTED) {
					that.locate("undoContainer").hide();
					that.locate("redoContainer").show()
				}
			}
		}
	}
	var bindHandlers = function (that) {
		that.locate("undoControl").click(function () {
			if (that.state !== STATE_REVERTED) {
				fluid.model.copyModel(that.extremalModel, that.component.model);
				that.component.updateModel(that.initialModel, that);
				that.state = STATE_REVERTED;
				refreshView(that);
				that.locate("redoControl").focus()
			}
			return false
		});
		that.locate("redoControl").click(function () {
			if (that.state !== STATE_CHANGED) {
				that.component.updateModel(that.extremalModel, that);
				that.state = STATE_CHANGED;
				refreshView(that);
				that.locate("undoControl").focus()
			}
			return false
		});
		return {
			modelChanged : function (newModel, oldModel, source) {
				if (source !== that) {
					that.state = STATE_CHANGED;
					fluid.model.copyModel(that.initialModel, oldModel);
					refreshView(that)
				}
			}
		}
	};
	fluid.undoDecorator = function (component, userOptions) {
		var that = fluid.initLittleComponent("undo", userOptions);
		that.container = that.options.renderer(that, component.container);
		fluid.initDomBinder(that);
		fluid.tabindex(that.locate("undoControl"), 0);
		fluid.tabindex(that.locate("redoControl"), 0);
		that.component = component;
		that.initialModel = {};
		that.extremalModel = {};
		fluid.model.copyModel(that.initialModel, component.model);
		fluid.model.copyModel(that.extremalModel, component.model);
		that.state = STATE_INITIAL;
		refreshView(that);
		var listeners = bindHandlers(that);
		that.returnedOptions = {
			listeners : listeners
		};
		return that
	};
	fluid.defaults("undo", {
		selectors : {
			undoContainer : ".flc-undo-undoControl",
			undoControl : ".flc-undo-undoControl",
			redoContainer : ".flc-undo-redoControl",
			redoControl : ".flc-undo-redoControl"
		},
		strings : {
			undo : "undo edit",
			redo : "redo edit"
		},
		renderer : defaultRenderer
	})
})(jQuery, fluid_1_4);
var fluid_1_4 = fluid_1_4 || {};
(function ($, fluid) {
	var createContentFunc = function (content) {
		return typeof content === "function" ? content : function () {
			return content
		}
	};
	var setup = function (that) {
		that.container.tooltip({
			content : createContentFunc(that.options.content),
			position : that.options.position,
			items : that.options.items,
			open : function (event) {
				var tt = $(event.target).tooltip("widget");
				tt.stop(false, true);
				tt.hide();
				if (that.options.delay) {
					tt.delay(that.options.delay).fadeIn("default", that.events.afterOpen.fire())
				} else {
					tt.show();
					that.events.afterOpen.fire()
				}
			},
			close : function (event) {
				var tt = $(event.target).tooltip("widget");
				tt.stop(false, true);
				tt.hide();
				tt.clearQueue();
				that.events.afterClose.fire()
			}
		});
		that.elm = that.container.tooltip("widget");
		that.elm.addClass(that.options.styles.tooltip)
	};
	fluid.tooltip = function (container, options) {
		var that = fluid.initView("fluid.tooltip", container, options);
		that.updateContent = function (content) {
			that.container.tooltip("option", "content", createContentFunc(content))
		};
		that.destroy = function () {
			that.container.tooltip("destroy")
		};
		that.open = function () {
			that.container.tooltip("open")
		};
		that.close = function () {
			that.container.tooltip("close")
		};
		setup(that);
		return that
	};
	fluid.defaults("fluid.tooltip", {
		styles : {
			tooltip : ""
		},
		events : {
			afterOpen : null,
			afterClose : null
		},
		content : "",
		position : {
			my : "left top",
			at : "left bottom",
			offset : "0 5"
		},
		items : "*",
		delay : 300
	})
})(jQuery, fluid_1_4);
var fluid_1_4 = fluid_1_4 || {};
(function ($, fluid) {
	function sendKey(control, event, virtualCode, charCode) {
		var kE = document.createEvent("KeyEvents");
		kE.initKeyEvent(event, 1, 1, null, 0, 0, 0, 0, virtualCode, charCode);
		control.dispatchEvent(kE)
	}
	fluid.setCaretToEnd = function (control, value) {
		var pos = value ? value.length : 0;
		try {
			control.focus();
			if (control.setSelectionRange) {
				control.setSelectionRange(pos, pos);
				if ($.browser.mozilla && pos > 0) {
					sendKey(control, "keypress", 92, 92);
					sendKey(control, "keydown", 8, 0);
					sendKey(control, "keypress", 8, 0)
				}
			} else {
				if (control.createTextRange) {
					var range = control.createTextRange();
					range.move("character", pos);
					range.select()
				}
			}
		} catch (e) {}
		
	};
	var switchToViewMode = function (that) {
		that.editContainer.hide();
		that.displayModeRenderer.show()
	};
	var cancel = function (that) {
		if (that.isEditing()) {
			setTimeout(function () {
				that.editView.value(that.model.value)
			}, 1);
			switchToViewMode(that);
			that.events.afterFinishEdit.fire(that.model.value, that.model.value, that.editField[0], that.viewEl[0])
		}
	};
	var finish = function (that) {
		var newValue = that.editView.value();
		var oldValue = that.model.value;
		var viewNode = that.viewEl[0];
		var editNode = that.editField[0];
		var ret = that.events.onFinishEdit.fire(newValue, oldValue, editNode, viewNode);
		if (ret === false) {
			return
		}
		that.updateModelValue(newValue);
		that.events.afterFinishEdit.fire(newValue, oldValue, editNode, viewNode);
		switchToViewMode(that)
	};
	var bindEditFinish = function (that) {
		if (that.options.submitOnEnter === undefined) {
			that.options.submitOnEnter = "textarea" !== fluid.unwrap(that.editField).nodeName.toLowerCase()
		}
		function keyCode(evt) {
			return evt.keyCode ? evt.keyCode : (evt.which ? evt.which : 0)
		}
		var escHandler = function (evt) {
			var code = keyCode(evt);
			if (code === $.ui.keyCode.ESCAPE) {
				that.textEditButton.focus(0);
				cancel(that);
				return false
			}
		};
		var finishHandler = function (evt) {
			var code = keyCode(evt);
			if (code !== $.ui.keyCode.ENTER) {
				that.textEditButton.blur();
				return true
			} else {
				finish(that);
				that.textEditButton.focus(0)
			}
			return false
		};
		if (that.options.submitOnEnter) {
			that.editContainer.keypress(finishHandler)
		}
		that.editContainer.keydown(escHandler)
	};
	var bindBlurHandler = function (that) {
		if (that.options.blurHandlerBinder) {
			that.options.blurHandlerBinder(that)
		} else {
			var blurHandler = function (evt) {
				if (that.isEditing()) {
					finish(that)
				}
				return false
			};
			that.editField.blur(blurHandler)
		}
	};
	var initializeEditView = function (that, initial) {
		if (!that.editInitialized) {
			fluid.inlineEdit.renderEditContainer(that, !that.options.lazyEditView || !initial);
			if (!that.options.lazyEditView || !initial) {
				that.editView = fluid.initSubcomponent(that, "editView", that.editField);
				$.extend(true, that.editView, fluid.initSubcomponent(that, "editAccessor", that.editField));
				bindEditFinish(that);
				bindBlurHandler(that);
				that.editView.refreshView(that);
				that.editInitialized = true
			}
		}
	};
	var edit = function (that) {
		initializeEditView(that, false);
		var viewEl = that.viewEl;
		var displayText = that.displayView.value();
		that.updateModelValue(that.model.value === "" ? "" : displayText);
		if (that.options.applyEditPadding) {
			that.editField.width(Math.max(viewEl.width() + that.options.paddings.edit, that.options.paddings.minimumEdit))
		}
		that.displayModeRenderer.hide();
		that.editContainer.show();
		setTimeout(function () {
			fluid.setCaretToEnd(that.editField[0], that.editView.value());
			if (that.options.selectOnEdit) {
				that.editField[0].select()
			}
		}, 0);
		that.events.afterBeginEdit.fire()
	};
	var clearEmptyViewStyles = function (textEl, styles, originalViewPadding) {
		textEl.removeClass(styles.defaultViewStyle);
		textEl.css("padding-right", originalViewPadding);
		textEl.removeClass(styles.emptyDefaultViewText)
	};
	var showDefaultViewText = function (that) {
		that.displayView.value(that.options.defaultViewText);
		that.viewEl.css("padding-right", that.existingPadding);
		that.viewEl.addClass(that.options.styles.defaultViewStyle)
	};
	var showNothing = function (that) {
		that.displayView.value("");
		if ($.browser.msie) {
			if (that.viewEl.css("display") === "inline") {
				that.viewEl.css("display", "inline-block")
			}
		}
	};
	var showEditedText = function (that) {
		that.displayView.value(that.model.value);
		clearEmptyViewStyles(that.viewEl, that.options.styles, that.existingPadding)
	};
	var refreshView = function (that, source) {
		that.displayView.refreshView(that, source);
		if (that.editView) {
			that.editView.refreshView(that, source)
		}
	};
	var initModel = function (that, value) {
		that.model.value = value;
		that.refreshView()
	};
	var updateModelValue = function (that, newValue, source) {
		var comparator = that.options.modelComparator;
		var unchanged = comparator ? comparator(that.model.value, newValue) : that.model.value === newValue;
		if (!unchanged) {
			var oldModel = $.extend(true, {}, that.model);
			that.model.value = newValue;
			that.events.modelChanged.fire(that.model, oldModel, source);
			that.refreshView(source)
		}
	};
	var makeIsEditing = function (that) {
		var isEditing = false;
		that.events.onBeginEdit.addListener(function () {
			isEditing = true
		});
		that.events.afterFinishEdit.addListener(function () {
			isEditing = false
		});
		return function () {
			return isEditing
		}
	};
	var makeEditHandler = function (that) {
		return function () {
			var prevent = that.events.onBeginEdit.fire();
			if (prevent === false) {
				return false
			}
			edit(that);
			return true
		}
	};
	var initTooltips = function (that) {
		var tooltipOptions = {
			content : that.options.tooltipText,
			position : {
				my : "left top",
				at : "left bottom",
				offset : "0 5"
			},
			target : "*",
			delay : that.options.tooltipDelay,
			styles : {
				tooltip : that.options.styles.tooltip
			}
		};
		fluid.tooltip(that.viewEl, tooltipOptions);
		if (that.textEditButton) {
			fluid.tooltip(that.textEditButton, tooltipOptions)
		}
	};
	var calculateInitialPadding = function (viewEl) {
		var padding = viewEl.css("padding-right");
		return padding ? parseFloat(padding) : 0
	};
	var setupInlineEdit = function (componentContainer, that) {
		if (that.editContainer) {
			that.editContainer.hide()
		}
		if (that.tooltipEnabled()) {
			initTooltips(that)
		}
		that.decorators = fluid.initSubcomponents(that, "componentDecorators", [that, fluid.COMPONENT_OPTIONS])
	};
	var setupInlineEdits = function (editables, options) {
		var editors = [];
		editables.each(function (idx, editable) {
			editors.push(fluid.inlineEdit($(editable), options))
		});
		return editors
	};
	fluid.inlineEdit = function (componentContainer, userOptions) {
		var that = fluid.initView("inlineEdit", componentContainer, userOptions);
		that.viewEl = fluid.inlineEdit.setupDisplayText(that);
		that.displayView = fluid.initSubcomponent(that, "displayView", that.viewEl);
		$.extend(true, that.displayView, fluid.initSubcomponent(that, "displayAccessor", that.viewEl));
		that.model = {
			value : ""
		};
		that.edit = makeEditHandler(that);
		that.isEditing = makeIsEditing(that);
		that.finish = function () {
			finish(that)
		};
		that.cancel = function () {
			cancel(that)
		};
		that.tooltipEnabled = function () {
			return that.options.useTooltip && $.fn.tooltip
		};
		that.refreshView = function (source) {
			refreshView(that, source)
		};
		that.updateModelValue = function (newValue, source) {
			updateModelValue(that, newValue, source)
		};
		that.updateModel = function (newModel, source) {
			updateModelValue(that, newModel.value, source)
		};
		that.existingPadding = calculateInitialPadding(that.viewEl);
		initModel(that, that.displayView.value());
		that.displayModeRenderer = that.options.displayModeRenderer(that);
		initializeEditView(that, true);
		setupInlineEdit(componentContainer, that);
		return that
	};
	fluid.inlineEdit.setupEditField = function (editStyle, editField) {
		var eField = $(editField);
		eField = eField.length ? eField : $("<input type='text' class='flc-inlineEdit-edit'/>");
		eField.addClass(editStyle);
		return eField
	};
	fluid.inlineEdit.setupEditContainer = function (displayContainer, editField, editContainer) {
		var eContainer = $(editContainer);
		eContainer = eContainer.length ? eContainer : $("<span></span>");
		displayContainer.after(eContainer);
		eContainer.append(editField);
		return eContainer
	};
	fluid.inlineEdit.defaultEditModeRenderer = function (that) {
		var editField = fluid.inlineEdit.setupEditField(that.options.styles.edit, that.editField);
		var editContainer = fluid.inlineEdit.setupEditContainer(that.displayModeRenderer, editField, that.editContainer);
		var editModeInstruction = fluid.inlineEdit.setupEditModeInstruction(that.options.styles.editModeInstruction, that.options.strings.editModeInstruction);
		var id = fluid.allocateSimpleId(editModeInstruction);
		editField.attr("aria-describedby", id);
		fluid.inlineEdit.positionEditModeInstruction(editModeInstruction, editContainer, editField);
		return {
			container : editContainer,
			field : editField
		}
	};
	fluid.inlineEdit.renderEditContainer = function (that, lazyEditView) {
		that.editContainer = that.locate("editContainer");
		that.editField = that.locate("edit");
		if (that.editContainer.length !== 1) {
			if (that.editContainer.length > 1) {
				fluid.fail("InlineEdit did not find a unique container for selector " + that.options.selectors.editContainer + ": " + fluid.dumpEl(that.editContainer))
			}
		}
		if (!lazyEditView) {
			return
		}
		var editElms = that.options.editModeRenderer(that);
		if (editElms) {
			that.editContainer = editElms.container;
			that.editField = editElms.field
		}
	};
	fluid.inlineEdit.setupEditModeInstruction = function (editModeInstructionStyle, editModeInstructionText) {
		var editModeInstruction = $("<p></p>");
		editModeInstruction.addClass(editModeInstructionStyle);
		editModeInstruction.text(editModeInstructionText);
		return editModeInstruction
	};
	fluid.inlineEdit.positionEditModeInstruction = function (editModeInstruction, editContainer, editField) {
		editContainer.append(editModeInstruction);
		editField.focus(function () {
			editModeInstruction.show();
			var editFieldPosition = editField.offset();
			editModeInstruction.css({
				left : editFieldPosition.left
			});
			editModeInstruction.css({
				top : editFieldPosition.top + editField.height() + 5
			})
		})
	};
	fluid.inlineEdit.setupDisplayModeContainer = function (styles, displayModeWrapper) {
		var displayModeContainer = $(displayModeWrapper);
		displayModeContainer = displayModeContainer.length ? displayModeContainer : $("<span></span>");
		displayModeContainer.addClass(styles.displayView);
		return displayModeContainer
	};
	fluid.inlineEdit.setupDisplayText = function (that) {
		var viewEl = that.locate("text");
		viewEl.attr("tabindex", "-1");
		viewEl.addClass(that.options.styles.text);
		return viewEl
	};
	fluid.inlineEdit.setupTextEditButton = function (that) {
		var opts = that.options;
		var textEditButton = that.locate("textEditButton");
		if (textEditButton.length === 0) {
			var markup = $("<a href='#_' class='flc-inlineEdit-textEditButton'></a>");
			markup.addClass(opts.styles.textEditButton);
			markup.text(opts.tooltipText);
			fluid.inlineEdit.updateTextEditButton(markup, that.model.value || opts.defaultViewText, opts.strings.textEditButton);
			that.events.modelChanged.addListener(function () {
				fluid.inlineEdit.updateTextEditButton(markup, that.model.value || opts.defaultViewText, opts.strings.textEditButton)
			});
			that.locate("text").after(markup);
			textEditButton = that.locate("textEditButton")
		}
		return textEditButton
	};
	fluid.inlineEdit.updateTextEditButton = function (textEditButton, value, stringTemplate) {
		var buttonText = fluid.stringTemplate(stringTemplate, {
				text : value
			});
		textEditButton.text(buttonText)
	};
	fluid.inlineEdit.bindHoverHandlers = function (displayModeRenderer, invitationStyle) {
		var over = function (evt) {
			displayModeRenderer.addClass(invitationStyle)
		};
		var out = function (evt) {
			displayModeRenderer.removeClass(invitationStyle)
		};
		displayModeRenderer.hover(over, out)
	};
	fluid.inlineEdit.bindHighlightHandler = function (element, displayModeRenderer, styles) {
		element = $(element);
		var focusOn = function () {
			displayModeRenderer.addClass(styles.focus);
			displayModeRenderer.addClass(styles.invitation)
		};
		var focusOff = function () {
			displayModeRenderer.removeClass(styles.focus);
			displayModeRenderer.removeClass(styles.invitation)
		};
		element.focus(focusOn);
		element.blur(focusOff)
	};
	fluid.inlineEdit.bindMouseHandlers = function (element, edit) {
		element = $(element);
		var triggerGuard = fluid.inlineEdit.makeEditTriggerGuard(element, edit);
		element.click(function (e) {
			triggerGuard(e);
			return false
		})
	};
	fluid.inlineEdit.bindKeyboardHandlers = function (element, edit) {
		element = $(element);
		element.attr("role", "button");
		var guard = fluid.inlineEdit.makeEditTriggerGuard(element, edit);
		fluid.activatable(element, function (event) {
			return guard(event)
		})
	};
	fluid.inlineEdit.makeEditTriggerGuard = function (element, edit) {
		var selector = fluid.unwrap(element);
		return function (event) {
			var outer = fluid.findAncestor(event.target, function (elem) {
					if (/input|select|textarea|button|a/i.test(elem.nodeName) || elem === selector) {
						return true
					}
				});
			if (outer === selector) {
				edit();
				return false
			}
		}
	};
	fluid.inlineEdit.defaultDisplayModeRenderer = function (that) {
		var styles = that.options.styles;
		var displayModeWrapper = fluid.inlineEdit.setupDisplayModeContainer(styles);
		var displayModeRenderer = that.viewEl.wrap(displayModeWrapper).parent();
		that.textEditButton = fluid.inlineEdit.setupTextEditButton(that);
		displayModeRenderer.append(that.textEditButton);
		fluid.inlineEdit.bindHoverHandlers(displayModeRenderer, styles.invitation);
		fluid.inlineEdit.bindMouseHandlers(that.viewEl, that.edit);
		fluid.inlineEdit.bindMouseHandlers(that.textEditButton, that.edit);
		fluid.inlineEdit.bindKeyboardHandlers(that.textEditButton, that.edit);
		fluid.inlineEdit.bindHighlightHandler(that.viewEl, displayModeRenderer, styles);
		fluid.inlineEdit.bindHighlightHandler(that.textEditButton, displayModeRenderer, styles);
		return displayModeRenderer
	};
	fluid.inlineEdit.standardAccessor = function (element) {
		var nodeName = element.nodeName.toLowerCase();
		return {
			value : function (newValue) {
				return "input" === nodeName || "textarea" === nodeName ? fluid.value($(element), newValue) : $(element).text(newValue)
			}
		}
	};
	fluid.inlineEdit.standardDisplayView = function (viewEl) {
		var that = {
			refreshView : function (componentThat, source) {
				if (componentThat.model.value) {
					showEditedText(componentThat)
				} else {
					if (componentThat.options.defaultViewText) {
						showDefaultViewText(componentThat)
					} else {
						showNothing(componentThat)
					}
				}
				if ($.trim(componentThat.viewEl.text()).length === 0) {
					componentThat.viewEl.addClass(componentThat.options.styles.emptyDefaultViewText);
					if (componentThat.existingPadding < componentThat.options.paddings.minimumView) {
						componentThat.viewEl.css("padding-right", componentThat.options.paddings.minimumView)
					}
				}
			}
		};
		return that
	};
	fluid.inlineEdit.standardEditView = function (editField) {
		var that = {
			refreshView : function (componentThat, source) {
				if (!source || (componentThat.editField && componentThat.editField.index(source) === -1)) {
					componentThat.editView.value(componentThat.model.value)
				}
			}
		};
		$.extend(true, that, fluid.inlineEdit.standardAccessor(editField));
		return that
	};
	fluid.inlineEdits = function (componentContainer, options) {
		options = options || {};
		var selectors = $.extend({}, fluid.defaults("inlineEdits").selectors, options.selectors);
		var container = fluid.container(componentContainer);
		var editables = $(selectors.editables, container);
		return setupInlineEdits(editables, options)
	};
	fluid.defaults("inlineEdit", {
		selectors : {
			text : ".flc-inlineEdit-text",
			editContainer : ".flc-inlineEdit-editContainer",
			edit : ".flc-inlineEdit-edit",
			textEditButton : ".flc-inlineEdit-textEditButton"
		},
		styles : {
			text : "fl-inlineEdit-text",
			edit : "fl-inlineEdit-edit",
			invitation : "fl-inlineEdit-invitation",
			defaultViewStyle : "fl-inlineEdit-emptyText-invitation",
			emptyDefaultViewText : "fl-inlineEdit-emptyDefaultViewText",
			focus : "fl-inlineEdit-focus",
			tooltip : "fl-inlineEdit-tooltip",
			editModeInstruction : "fl-inlineEdit-editModeInstruction",
			displayView : "fl-inlineEdit-simple-editableText fl-inlineEdit-textContainer",
			textEditButton : "fl-offScreen-hidden"
		},
		events : {
			modelChanged : null,
			onBeginEdit : "preventable",
			afterBeginEdit : null,
			onFinishEdit : "preventable",
			afterFinishEdit : null,
			afterInitEdit : null
		},
		strings : {
			textEditButton : "Edit text %text",
			editModeInstruction : "Escape to cancel, Enter or Tab when finished"
		},
		paddings : {
			edit : 10,
			minimumEdit : 80,
			minimumView : 60
		},
		applyEditPadding : true,
		blurHandlerBinder : null,
		submitOnEnter : undefined,
		modelComparator : null,
		displayAccessor : {
			type : "fluid.inlineEdit.standardAccessor"
		},
		displayView : {
			type : "fluid.inlineEdit.standardDisplayView"
		},
		editAccessor : {
			type : "fluid.inlineEdit.standardAccessor"
		},
		editView : {
			type : "fluid.inlineEdit.standardEditView"
		},
		displayModeRenderer : fluid.inlineEdit.defaultDisplayModeRenderer,
		editModeRenderer : fluid.inlineEdit.defaultEditModeRenderer,
		lazyEditView : false,
		defaultViewText : "Click here to edit",
		useTooltip : true,
		tooltipText : "Select or press Enter to edit",
		tooltipDelay : 1000,
		selectOnEdit : false
	});
	fluid.defaults("inlineEdits", {
		selectors : {
			editables : ".flc-inlineEditable"
		}
	})
})(jQuery, fluid_1_4);
var fluid_1_4 = fluid_1_4 || {};
(function ($, fluid) {
	fluid.inlineEdit.makeViewAccessor = function (editorGetFn, setValueFn, getValueFn) {
		return function (editField) {
			return {
				value : function (newValue) {
					var editor = editorGetFn(editField);
					if (!editor) {
						if (newValue) {
							$(editField).val(newValue)
						}
						return ""
					}
					if (newValue) {
						setValueFn(editField, editor, newValue)
					} else {
						return getValueFn(editor)
					}
				}
			}
		}
	};
	fluid.inlineEdit.richTextViewAccessor = function (element) {
		return {
			value : function (newValue) {
				return $(element).html(newValue)
			}
		}
	};
	var configureInlineEdit = function (configurationName, container, options) {
		var defaults = fluid.defaults(configurationName);
		var assembleOptions = fluid.merge(defaults ? defaults.mergePolicy : null, {}, defaults, options);
		return fluid.inlineEdit(container, assembleOptions)
	};
	fluid.inlineEdit.normalizeHTML = function (value) {
		var togo = $.trim(value.replace(/\s+/g, " "));
		togo = togo.replace(/\s+<\//g, "</");
		togo = togo.replace(/\<([a-z0-9A-Z\/]+)\>/g, function (match) {
				return match.toLowerCase()
			});
		return togo
	};
	fluid.inlineEdit.htmlComparator = function (el1, el2) {
		return fluid.inlineEdit.normalizeHTML(el1) === fluid.inlineEdit.normalizeHTML(el2)
	};
	fluid.inlineEdit.bindRichTextHighlightHandler = function (element, displayModeRenderer, invitationStyle) {
		element = $(element);
		var focusOn = function () {
			displayModeRenderer.addClass(invitationStyle)
		};
		var focusOff = function () {
			displayModeRenderer.removeClass(invitationStyle)
		};
		element.focus(focusOn);
		element.blur(focusOff)
	};
	fluid.inlineEdit.setupRichTextEditButton = function (that) {
		var opts = that.options;
		var textEditButton = that.locate("textEditButton");
		if (textEditButton.length === 0) {
			var markup = $("<a href='#_' class='flc-inlineEdit-textEditButton'></a>");
			markup.text(opts.strings.textEditButton);
			that.locate("text").after(markup);
			textEditButton = that.locate("textEditButton")
		}
		return textEditButton
	};
	fluid.inlineEdit.richTextDisplayModeRenderer = function (that) {
		var styles = that.options.styles;
		var displayModeWrapper = fluid.inlineEdit.setupDisplayModeContainer(styles);
		var displayModeRenderer = that.viewEl.wrap(displayModeWrapper).parent();
		that.textEditButton = fluid.inlineEdit.setupRichTextEditButton(that);
		displayModeRenderer.append(that.textEditButton);
		displayModeRenderer.addClass(styles.focus);
		fluid.inlineEdit.bindHoverHandlers(displayModeRenderer, styles.invitation);
		fluid.inlineEdit.bindMouseHandlers(that.textEditButton, that.edit);
		fluid.inlineEdit.bindKeyboardHandlers(that.textEditButton, that.edit);
		fluid.inlineEdit.bindRichTextHighlightHandler(that.viewEl, displayModeRenderer, styles.invitation);
		fluid.inlineEdit.bindRichTextHighlightHandler(that.textEditButton, displayModeRenderer, styles.invitation);
		return displayModeRenderer
	};
	fluid.inlineEdit.tinyMCE = function (container, options) {
		var inlineEditor = configureInlineEdit("fluid.inlineEdit.tinyMCE", container, options);
		tinyMCE.init(inlineEditor.options.tinyMCE);
		return inlineEditor
	};
	fluid.inlineEdit.tinyMCE.getEditor = function (editField) {
		return tinyMCE.get(editField.id)
	};
	fluid.inlineEdit.tinyMCE.setValue = function (editField, editor, value) {
		$(editField).val(value);
		editor.setContent(value, {
			format : "raw"
		})
	};
	fluid.inlineEdit.tinyMCE.getValue = function (editor) {
		return editor.getContent()
	};
	var flTinyMCE = fluid.inlineEdit.tinyMCE;
	flTinyMCE.viewAccessor = fluid.inlineEdit.makeViewAccessor(flTinyMCE.getEditor, flTinyMCE.setValue, flTinyMCE.getValue);
	fluid.inlineEdit.tinyMCE.blurHandlerBinder = function (that) {
		function focusEditor(editor) {
			setTimeout(function () {
				tinyMCE.execCommand("mceFocus", false, that.editField[0].id);
				if ($.browser.mozilla && $.browser.version.substring(0, 3) === "1.8") {
					return
				}
				editor.selection.select(editor.getBody(), 1);
				editor.selection.collapse(0)
			}, 10)
		}
		that.events.afterInitEdit.addListener(function (editor) {
			focusEditor(editor);
			var editorBody = editor.getBody();
			fluid.deadMansBlur(that.editField, {
				exclusions : {
					body : $(editorBody)
				},
				handler : function () {
					that.cancel()
				}
			})
		});
		that.events.afterBeginEdit.addListener(function () {
			var editor = tinyMCE.get(that.editField[0].id);
			if (editor) {
				focusEditor(editor)
			}
		})
	};
	fluid.inlineEdit.tinyMCE.editModeRenderer = function (that) {
		var options = that.options.tinyMCE;
		options.elements = fluid.allocateSimpleId(that.editField);
		var oldinit = options.init_instance_callback;
		options.init_instance_callback = function (instance) {
			that.events.afterInitEdit.fire(instance);
			if (oldinit) {
				oldinit()
			}
		};
		tinyMCE.init(options)
	};
	fluid.defaults("fluid.inlineEdit.tinyMCE", {
		tinyMCE : {
			mode : "exact",
			theme : "simple"
		},
		useTooltip : true,
		selectors : {
			edit : "textarea"
		},
		styles : {
			invitation : "fl-inlineEdit-richText-invitation",
			displayView : "fl-inlineEdit-textContainer",
			text : ""
		},
		strings : {
			textEditButton : "Edit"
		},
		displayAccessor : {
			type : "fluid.inlineEdit.richTextViewAccessor"
		},
		editAccessor : {
			type : "fluid.inlineEdit.tinyMCE.viewAccessor"
		},
		lazyEditView : true,
		defaultViewText : "Click Edit",
		modelComparator : fluid.inlineEdit.htmlComparator,
		blurHandlerBinder : fluid.inlineEdit.tinyMCE.blurHandlerBinder,
		displayModeRenderer : fluid.inlineEdit.richTextDisplayModeRenderer,
		editModeRenderer : fluid.inlineEdit.tinyMCE.editModeRenderer
	});
	fluid.inlineEdit.CKEditor = function (container, options) {
		return configureInlineEdit("fluid.inlineEdit.CKEditor", container, options)
	};
	fluid.inlineEdit.CKEditor.getEditor = function (editField) {
		return CKEDITOR.instances[editField.id]
	};
	fluid.inlineEdit.CKEditor.setValue = function (editField, editor, value) {
		editor.setData(value)
	};
	fluid.inlineEdit.CKEditor.getValue = function (editor) {
		return editor.getData()
	};
	var flCKEditor = fluid.inlineEdit.CKEditor;
	flCKEditor.viewAccessor = fluid.inlineEdit.makeViewAccessor(flCKEditor.getEditor, flCKEditor.setValue, flCKEditor.getValue);
	fluid.inlineEdit.CKEditor.focus = function (editor) {
		setTimeout(function () {
			editor.focus()
		}, 0)
	};
	fluid.inlineEdit.CKEditor.normalizeHTML = function (value) {
		var togo = fluid.inlineEdit.normalizeHTML(value);
		var angpos = togo.indexOf(">");
		if (angpos !== -1 && angpos < togo.length - 1) {
			if (togo.charAt(angpos + 1) !== " ") {
				togo = togo.substring(0, angpos + 1) + " " + togo.substring(angpos + 1)
			}
		}
		return togo
	};
	fluid.inlineEdit.CKEditor.htmlComparator = function (el1, el2) {
		return fluid.inlineEdit.CKEditor.normalizeHTML(el1) === fluid.inlineEdit.CKEditor.normalizeHTML(el2)
	};
	fluid.inlineEdit.CKEditor.blurHandlerBinder = function (that) {
		that.events.afterInitEdit.addListener(fluid.inlineEdit.CKEditor.focus);
		that.events.afterBeginEdit.addListener(function () {
			var editor = fluid.inlineEdit.CKEditor.getEditor(that.editField[0]);
			if (editor) {
				fluid.inlineEdit.CKEditor.focus(editor)
			}
		})
	};
	fluid.inlineEdit.CKEditor.editModeRenderer = function (that) {
		var id = fluid.allocateSimpleId(that.editField);
		$.data(fluid.unwrap(that.editField), "fluid.inlineEdit.CKEditor", that);
		var editor = CKEDITOR.replace(id, that.options.CKEditor);
		editor.on("instanceReady", function (e) {
			fluid.inlineEdit.CKEditor.focus(e.editor);
			that.events.afterInitEdit.fire(e.editor)
		})
	};
	fluid.defaults("fluid.inlineEdit.CKEditor", {
		selectors : {
			edit : "textarea"
		},
		styles : {
			invitation : "fl-inlineEdit-richText-invitation",
			displayView : "fl-inlineEdit-textContainer",
			text : ""
		},
		strings : {
			textEditButton : "Edit"
		},
		displayAccessor : {
			type : "fluid.inlineEdit.richTextViewAccessor"
		},
		editAccessor : {
			type : "fluid.inlineEdit.CKEditor.viewAccessor"
		},
		lazyEditView : true,
		defaultViewText : "Click Edit",
		modelComparator : fluid.inlineEdit.CKEditor.htmlComparator,
		blurHandlerBinder : fluid.inlineEdit.CKEditor.blurHandlerBinder,
		displayModeRenderer : fluid.inlineEdit.richTextDisplayModeRenderer,
		editModeRenderer : fluid.inlineEdit.CKEditor.editModeRenderer,
		CKEditor : {}
		
	});
	fluid.inlineEdit.dropdown = function (container, options) {
		return configureInlineEdit("fluid.inlineEdit.dropdown", container, options)
	};
	fluid.inlineEdit.dropdown.editModeRenderer = function (that) {
		var id = fluid.allocateSimpleId(that.editField);
		that.editField.selectbox({
			finishHandler : function () {
				that.finish()
			}
		});
		return {
			container : that.editContainer,
			field : $("input.selectbox", that.editContainer)
		}
	};
	fluid.inlineEdit.dropdown.blurHandlerBinder = function (that) {
		fluid.deadMansBlur(that.editField, {
			exclusions : {
				selectBox : $("div.selectbox-wrapper", that.editContainer)
			},
			handler : function () {
				that.cancel()
			}
		})
	};
	fluid.defaults("fluid.inlineEdit.dropdown", {
		applyEditPadding : false,
		blurHandlerBinder : fluid.inlineEdit.dropdown.blurHandlerBinder,
		editModeRenderer : fluid.inlineEdit.dropdown.editModeRenderer
	})
})(jQuery, fluid_1_4);
(function ($) {
	$.fn.bgIframe = $.fn.bgiframe = function (s) {
		if ($.browser.msie && parseInt($.browser.version) <= 6) {
			s = $.extend({
					top : "auto",
					left : "auto",
					width : "auto",
					height : "auto",
					opacity : true,
					src : "javascript:false;"
				}, s || {});
			var prop = function (n) {
				return n && n.constructor == Number ? n + "px" : n
			},
			html = '<iframe class="bgiframe"frameborder="0"tabindex="-1"src="' + s.src + '"style="display:block;position:absolute;z-index:-1;' + (s.opacity !== false ? "filter:Alpha(Opacity='0');" : "") + "top:" + (s.top == "auto" ? "expression(((parseInt(this.parentNode.currentStyle.borderTopWidth)||0)*-1)+'px')" : prop(s.top)) + ";left:" + (s.left == "auto" ? "expression(((parseInt(this.parentNode.currentStyle.borderLeftWidth)||0)*-1)+'px')" : prop(s.left)) + ";width:" + (s.width == "auto" ? "expression(this.parentNode.offsetWidth+'px')" : prop(s.width)) + ";height:" + (s.height == "auto" ? "expression(this.parentNode.offsetHeight+'px')" : prop(s.height)) + ';"/>';
			return this.each(function () {
				if ($("> iframe.bgiframe", this).length == 0) {
					this.insertBefore(document.createElement(html), this.firstChild)
				}
			})
		}
		return this
	};
	if (!$.browser.version) {
		$.browser.version = navigator.userAgent.toLowerCase().match(/.+(?:rv|it|ra|ie)[\/: ]([\d.]+)/)[1]
	}
})(jQuery);
var fluid_1_4 = fluid_1_4 || {};
(function ($, fluid) {
	function updateStyles(pageListThat, newModel, oldModel) {
		if (!pageListThat.pageLinks) {
			return
		}
		if (oldModel.pageIndex !== undefined) {
			var oldLink = pageListThat.pageLinks.eq(oldModel.pageIndex);
			oldLink.removeClass(pageListThat.options.styles.currentPage)
		}
		var pageLink = pageListThat.pageLinks.eq(newModel.pageIndex);
		pageLink.addClass(pageListThat.options.styles.currentPage)
	}
	function bindLinkClick(link, events, eventArg) {
		link.unbind("click.fluid.pager");
		link.bind("click.fluid.pager", function () {
			events.initiatePageChange.fire(eventArg)
		})
	}
	function computePageCount(model) {
		model.pageCount = Math.max(1, Math.floor((model.totalRange - 1) / model.pageSize) + 1)
	}
	fluid.pager = function () {
		return fluid.pagerImpl.apply(null, arguments)
	};
	fluid.pager.computePageLimit = function (model) {
		return Math.min(model.totalRange, (model.pageIndex + 1) * model.pageSize)
	};
	fluid.pager.directPageList = function (container, events, options) {
		var that = fluid.initView("fluid.pager.directPageList", container, options);
		that.pageLinks = that.locate("pageLinks");
		for (var i = 0; i < that.pageLinks.length; ++i) {
			var pageLink = that.pageLinks.eq(i);
			bindLinkClick(pageLink, events, {
				pageIndex : i
			})
		}
		events.onModelChange.addListener(function (newModel, oldModel) {
			updateStyles(that, newModel, oldModel)
		});
		that.defaultModel = {
			pageIndex : undefined,
			pageSize : 1,
			totalRange : that.pageLinks.length
		};
		return that
	};
	fluid.iota = function (count, first) {
		first = first || 0;
		var togo = [];
		for (var i = 0; i < count; ++i) {
			togo[togo.length] = first++
		}
		return togo
	};
	fluid.pager.everyPageStrategy = fluid.iota;
	fluid.pager.gappedPageStrategy = function (locality, midLocality) {
		if (!locality) {
			locality = 3
		}
		if (!midLocality) {
			midLocality = locality
		}
		return function (count, first, mid) {
			var togo = [];
			var j = 0;
			var lastSkip = false;
			for (var i = 0; i < count; ++i) {
				if (i < locality || (count - i - 1) < locality || (i >= mid - midLocality && i <= mid + midLocality)) {
					togo[j++] = i;
					lastSkip = false
				} else {
					if (!lastSkip) {
						togo[j++] = -1;
						lastSkip = true
					}
				}
			}
			return togo
		}
	};
	fluid.pager.consistentGappedPageStrategy = function (endLinkCount, midLinkCount) {
		if (!endLinkCount) {
			endLinkCount = 1
		}
		if (!midLinkCount) {
			midLinkCount = endLinkCount
		}
		var endWidth = endLinkCount + 2 + midLinkCount;
		return function (count, first, mid) {
			var pages = [];
			var anchoredLeft = mid < endWidth;
			var anchoredRight = mid >= count - endWidth;
			var anchoredEndWidth = endWidth + midLinkCount;
			var midStart = mid - midLinkCount;
			var midEnd = mid + midLinkCount;
			var lastSkip = false;
			for (var page = 0; page < count; page++) {
				if (page < endLinkCount || count - page <= endLinkCount || (anchoredLeft && page < anchoredEndWidth) || (anchoredRight && page >= count - anchoredEndWidth) || (page >= midStart && page <= midEnd)) {
					pages.push(page);
					lastSkip = false
				} else {
					if (!lastSkip) {
						pages.push(-1);
						lastSkip = true
					}
				}
			}
			return pages
		}
	};
	fluid.pager.renderedPageList = function (container, events, pagerBarOptions, options, strings) {
		options = $.extend(true, pagerBarOptions, options);
		var that = fluid.initView("fluid.pager.renderedPageList", container, options);
		options = that.options;
		var idMap = {};
		var renderOptions = {
			cutpoints : [{
					id : "page-link:link",
					selector : pagerBarOptions.selectors.pageLinks
				}, {
					id : "page-link:skip",
					selector : pagerBarOptions.selectors.pageLinkSkip
				}
			],
			idMap : idMap
		};
		if (options.linkBody) {
			renderOptions.cutpoints[renderOptions.cutpoints.length] = {
				id : "payload-component",
				selector : options.linkBody
			}
		}
		var assembleComponent = function (page, isCurrent) {
			var obj = {
				ID : "page-link:link",
				localID : page + 1,
				value : page + 1,
				pageIndex : page,
				decorators : [{
						type : "jQuery",
						func : "click",
						args : function (event) {
							events.initiatePageChange.fire({
								pageIndex : page
							});
							event.preventDefault()
						}
					}
				]
			};
			if (isCurrent) {
				obj.current = true;
				obj.decorators = obj.decorators.concat([{
								type : "addClass",
								classes : that.options.styles.currentPage
							}, {
								type : "jQuery",
								func : "attr",
								args : ["aria-label", that.options.strings.currentPageIndexMsg]
							}
						])
			}
			return obj
		};
		function pageToComponent(current) {
			return function (page) {
				return page === -1 ? {
					ID : "page-link:skip"
				}
				 : assembleComponent(page, page === current)
			}
		}
		var root = that.locate("root");
		fluid.expectFilledSelector(root, "Error finding root template for fluid.pager.renderedPageList");
		var template = fluid.selfRender(root, {}, renderOptions);
		events.onModelChange.addListener(function (newModel, oldModel) {
			var pages = that.options.pageStrategy(newModel.pageCount, 0, newModel.pageIndex);
			var pageTree = fluid.transform(pages, pageToComponent(newModel.pageIndex));
			if (pageTree.length > 1) {
				pageTree[pageTree.length - 1].value = pageTree[pageTree.length - 1].value + strings.last
			}
			events.onRenderPageLinks.fire(pageTree, newModel);
			$.each(idMap, function (key, id) {
				var pageLink = fluid.jById(id);
				if (pageLink.tooltip) {
					pageLink.tooltip("destroy")
				}
			});
			fluid.reRender(template, root, pageTree, renderOptions);
			updateStyles(that, newModel, oldModel)
		});
		return that
	};
	fluid.defaults("fluid.pager.renderedPageList", {
		selectors : {
			root : ".flc-pager-links"
		},
		linkBody : "a",
		pageStrategy : fluid.pager.everyPageStrategy
	});
	var updatePreviousNext = function (that, options, newModel) {
		if (newModel.pageIndex === 0) {
			that.previous.addClass(options.styles.disabled)
		} else {
			that.previous.removeClass(options.styles.disabled)
		}
		if (newModel.pageIndex === newModel.pageCount - 1) {
			that.next.addClass(options.styles.disabled)
		} else {
			that.next.removeClass(options.styles.disabled)
		}
	};
	fluid.pager.previousNext = function (container, events, options) {
		var that = fluid.initView("fluid.pager.previousNext", container, options);
		that.previous = that.locate("previous");
		bindLinkClick(that.previous, events, {
			relativePage : -1
		});
		that.next = that.locate("next");
		bindLinkClick(that.next, events, {
			relativePage : +1
		});
		events.onModelChange.addListener(function (newModel, oldModel, overallThat) {
			updatePreviousNext(that, options, newModel)
		});
		return that
	};
	fluid.pager.pagerBar = function (events, container, options, strings) {
		var that = fluid.initView("fluid.pager.pagerBar", container, options);
		that.pageList = fluid.initSubcomponent(that, "pageList", [container, events, that.options, fluid.COMPONENT_OPTIONS, strings]);
		that.previousNext = fluid.initSubcomponent(that, "previousNext", [container, events, that.options, fluid.COMPONENT_OPTIONS, strings]);
		return that
	};
	fluid.defaults("fluid.pager.pagerBar", {
		previousNext : {
			type : "fluid.pager.previousNext"
		},
		pageList : {
			type : "fluid.pager.renderedPageList",
			options : {
				pageStrategy : fluid.pager.gappedPageStrategy(3, 1)
			}
		},
		selectors : {
			pageLinks : ".flc-pager-pageLink",
			pageLinkSkip : ".flc-pager-pageLink-skip",
			previous : ".flc-pager-previous",
			next : ".flc-pager-next"
		},
		styles : {
			currentPage : "fl-pager-currentPage",
			disabled : "fl-pager-disabled"
		},
		strings : {
			currentPageIndexMsg : "Current page"
		}
	});
	function getColumnDefs(that) {
		return that.options.columnDefs
	}
	fluid.pager.findColumnDef = function (columnDefs, key) {
		var columnDef = $.grep(columnDefs, function (def) {
				return def.key === key
			})[0];
		return columnDef
	};
	function getRoots(target, overallThat, index) {
		var cellRoot = (overallThat.options.dataOffset ? overallThat.options.dataOffset + "." : "");
		target.shortRoot = index;
		target.longRoot = cellRoot + target.shortRoot
	}
	function expandPath(EL, shortRoot, longRoot) {
		if (EL.charAt(0) === "*") {
			return longRoot + EL.substring(1)
		} else {
			return EL.replace("*", shortRoot)
		}
	}
	fluid.pager.fetchValue = function (that, dataModel, index, valuebinding, roots) {
		getRoots(roots, that, index);
		var path = expandPath(valuebinding, roots.shortRoot, roots.longRoot);
		return fluid.get(dataModel, path)
	};
	fluid.pager.basicSorter = function (overallThat, model) {
		var dataModel = overallThat.options.dataModel;
		var roots = {};
		var columnDefs = getColumnDefs(overallThat);
		var columnDef = fluid.pager.findColumnDef(columnDefs, model.sortKey);
		var sortrecs = [];
		for (var i = 0; i < model.totalRange; ++i) {
			sortrecs[i] = {
				index : i,
				value : fluid.pager.fetchValue(overallThat, dataModel, i, columnDef.valuebinding, roots)
			}
		}
		function sortfunc(arec, brec) {
			var a = arec.value;
			var b = brec.value;
			return a === b ? 0 : (a > b ? model.sortDir : -model.sortDir)
		}
		sortrecs.sort(sortfunc);
		return fluid.transform(sortrecs, function (row) {
			return row.index
		})
	};
	fluid.pager.directModelFilter = function (model, pagerModel, perm) {
		var togo = [];
		var limit = fluid.pager.computePageLimit(pagerModel);
		for (var i = pagerModel.pageIndex * pagerModel.pageSize; i < limit; ++i) {
			var index = perm ? perm[i] : i;
			togo[togo.length] = {
				index : index,
				row : model[index]
			}
		}
		return togo
	};
	function expandVariables(value, opts) {
		var togo = "";
		var index = 0;
		while (true) {
			var nextindex = value.indexOf("${", index);
			if (nextindex === -1) {
				togo += value.substring(index);
				break
			} else {
				togo += value.substring(index, nextindex);
				var endi = value.indexOf("}", nextindex + 2);
				var EL = value.substring(nextindex + 2, endi);
				if (EL === "VALUE") {
					EL = opts.EL
				} else {
					EL = expandPath(EL, opts.shortRoot, opts.longRoot)
				}
				var val = fluid.get(opts.dataModel, EL);
				togo += val;
				index = endi + 1
			}
		}
		return togo
	}
	function expandPaths(target, tree, opts) {
		for (var i in tree) {
			var val = tree[i];
			if (val === fluid.VALUE) {
				if (i === "valuebinding") {
					target[i] = opts.EL
				} else {
					target[i] = {
						valuebinding : opts.EL
					}
				}
			} else {
				if (i === "valuebinding") {
					target[i] = expandPath(tree[i], opts)
				} else {
					if (typeof(val) === "object") {
						target[i] = val.length !== undefined ? [] : {};
						expandPaths(target[i], val, opts)
					} else {
						if (typeof(val) === "string") {
							target[i] = expandVariables(val, opts)
						} else {
							target[i] = tree[i]
						}
					}
				}
			}
		}
		return target
	}
	function iDforColumn(columnDef, opts) {
		var options = opts.options;
		var EL = columnDef.valuebinding;
		var key = columnDef.key;
		if (!EL) {
			fluid.fail("Error in definition for column with key " + key + ": valuebinding is not set")
		}
		opts.EL = expandPath(EL, opts.shortRoot, opts.longRoot);
		if (!key) {
			var segs = fluid.model.parseEL(EL);
			key = segs[segs.length - 1]
		}
		var ID = (options.keyPrefix ? options.keyPrefix : "") + key;
		return ID
	}
	function expandColumnDefs(filteredRow, opts) {
		var tree = fluid.transform(opts.columnDefs, function (columnDef) {
				var ID = iDforColumn(columnDef, opts);
				var togo;
				if (!columnDef.components) {
					return {
						ID : ID,
						valuebinding : opts.EL
					}
				} else {
					if (typeof columnDef.components === "function") {
						togo = columnDef.components(filteredRow.row, filteredRow.index)
					} else {
						togo = columnDef.components
					}
				}
				togo = expandPaths({}, togo, opts);
				togo.ID = ID;
				return togo
			});
		return tree
	}
	function fetchModel(overallThat) {
		return fluid.get(overallThat.options.dataModel, overallThat.options.dataOffset)
	}
	function bigHeaderForKey(key, opts) {
		var id = opts.options.renderOptions.idMap["header:" + key];
		var smallHeader = fluid.jById(id);
		if (smallHeader.length === 0) {
			return null
		}
		var headerSortStylisticOffset = opts.overallOptions.selectors.headerSortStylisticOffset;
		var bigHeader = fluid.findAncestor(smallHeader, function (element) {
				return $(element).is(headerSortStylisticOffset)
			});
		return bigHeader
	}
	function setSortHeaderClass(styles, element, sort) {
		element = $(element);
		element.removeClass(styles.ascendingHeader);
		element.removeClass(styles.descendingHeader);
		if (sort !== 0) {
			element.addClass(sort === 1 ? styles.ascendingHeader : styles.descendingHeader);
			element.attr("aria-sort", sort === 1 ? "ascending" : "descending")
		}
	}
	function isCurrentColumnSortable(columnDefs, model) {
		var columnDef = model.sortKey ? fluid.pager.findColumnDef(columnDefs, model.sortKey) : null;
		return columnDef ? columnDef.sortable : false
	}
	function setModelSortHeaderClass(newModel, opts) {
		var styles = opts.overallOptions.styles;
		var sort = isCurrentColumnSortable(opts.columnDefs, newModel) ? newModel.sortDir : 0;
		setSortHeaderClass(styles, bigHeaderForKey(newModel.sortKey, opts), sort)
	}
	function fireModelChange(that, newModel, forceUpdate) {
		computePageCount(newModel);
		if (newModel.pageIndex >= newModel.pageCount) {
			newModel.pageIndex = newModel.pageCount - 1
		}
		if (forceUpdate || newModel.pageIndex !== that.model.pageIndex || newModel.pageSize !== that.model.pageSize || newModel.sortKey !== that.model.sortKey || newModel.sortDir !== that.model.sortDir) {
			var sorted = isCurrentColumnSortable(getColumnDefs(that), newModel) ? that.options.sorter(that, newModel) : null;
			that.permutation = sorted;
			that.events.onModelChange.fire(newModel, that.model, that);
			fluid.model.copyModel(that.model, newModel)
		}
	}
	function generateColumnClick(overallThat, columnDef, opts) {
		return function () {
			if (columnDef.sortable === true) {
				var model = overallThat.model;
				var newModel = fluid.copy(model);
				var styles = overallThat.options.styles;
				var oldKey = model.sortKey;
				if (columnDef.key !== model.sortKey) {
					newModel.sortKey = columnDef.key;
					newModel.sortDir = 1;
					var oldBig = bigHeaderForKey(oldKey, opts);
					if (oldBig) {
						setSortHeaderClass(styles, oldBig, 0)
					}
				} else {
					if (newModel.sortKey === columnDef.key) {
						newModel.sortDir = -1 * newModel.sortDir
					} else {
						return false
					}
				}
				newModel.pageIndex = 0;
				fireModelChange(overallThat, newModel, true);
				setModelSortHeaderClass(newModel, opts)
			}
			return false
		}
	}
	function fetchHeaderDecorators(decorators, columnDef) {
		return decorators[columnDef.sortable ? "sortableHeader" : "unsortableHeader"]
	}
	function generateHeader(overallThat, newModel, columnDefs, opts) {
		var sortableColumnTxt = opts.options.strings.sortableColumnText;
		if (newModel.sortDir === 1) {
			sortableColumnTxt = opts.options.strings.sortableColumnTextAsc
		} else {
			if (newModel.sortDir === -1) {
				sortableColumnTxt = opts.options.strings.sortableColumnTextDesc
			}
		}
		return {
			children : fluid.transform(columnDefs, function (columnDef) {
				return {
					ID : iDforColumn(columnDef, opts),
					value : columnDef.label,
					decorators : [{
							jQuery : ["click", generateColumnClick(overallThat, columnDef, opts)]
						}, {
							identify : "header:" + columnDef.key
						}, {
							type : "attrs",
							attributes : {
								title : (columnDef.key === newModel.sortKey) ? sortableColumnTxt : opts.options.strings.sortableColumnText
							}
						}
					].concat(fetchHeaderDecorators(opts.overallOptions.decorators, columnDef))
				}
			})
		}
	}
	fluid.pager.selfRender = function (overallThat, inOptions) {
		var that = fluid.initView("fluid.pager.selfRender", overallThat.container, inOptions);
		var options = that.options;
		options.renderOptions.idMap = options.renderOptions.idMap || {};
		var idMap = options.renderOptions.idMap;
		var root = that.locate("root");
		var template = fluid.selfRender(root, {}, options.renderOptions);
		root.addClass(options.styles.root);
		var columnDefs = getColumnDefs(overallThat);
		var expOpts = {
			options : options,
			columnDefs : columnDefs,
			overallOptions : overallThat.options,
			dataModel : overallThat.options.dataModel,
			idMap : idMap
		};
		var directModel = fetchModel(overallThat);
		return {
			returnedOptions : {
				listeners : {
					onModelChange : function (newModel, oldModel) {
						var filtered = overallThat.options.modelFilter(directModel, newModel, overallThat.permutation);
						var tree = fluid.transform(filtered, function (filteredRow) {
								getRoots(expOpts, overallThat, filteredRow.index);
								if (columnDefs === "explode") {
									return fluid.explode(filteredRow.row, expOpts.longRoot)
								} else {
									if (columnDefs.length) {
										return expandColumnDefs(filteredRow, expOpts)
									}
								}
							});
						var fullTree = {};
						fullTree[options.row] = tree;
						if (typeof(columnDefs) === "object") {
							fullTree[options.header] = generateHeader(overallThat, newModel, columnDefs, expOpts)
						}
						options.renderOptions = options.renderOptions || {};
						options.renderOptions.model = expOpts.dataModel;
						fluid.reRender(template, root, fullTree, options.renderOptions);
						overallThat.events.afterRender.fire(overallThat);
						setModelSortHeaderClass(newModel, expOpts)
					}
				}
			}
		}
	};
	fluid.defaults("fluid.pager.selfRender", {
		selectors : {
			root : ".flc-pager-body-template"
		},
		styles : {
			root : "fl-pager"
		},
		keyStrategy : "id",
		keyPrefix : "",
		row : "row:",
		header : "header:",
		strings : {
			sortableColumnText : "Select to sort",
			sortableColumnTextDesc : "Select to sort in ascending, currently in descending order.",
			sortableColumnTextAsc : "Select to sort in descending, currently in ascending order."
		},
		renderOptions : {}
		
	});
	fluid.pager.summaryAria = function (element) {
		element.attr({
			"aria-relevant" : "all",
			"aria-atomic" : "false",
			"aria-live" : "assertive",
			role : "status"
		})
	};
	fluid.pager.summary = function (dom, options) {
		var node = dom.locate("summary");
		fluid.pager.summaryAria(node);
		return {
			returnedOptions : {
				listeners : {
					onModelChange : function (newModel, oldModel) {
						var text = fluid.stringTemplate(options.message, {
								first : newModel.pageIndex * newModel.pageSize + 1,
								last : fluid.pager.computePageLimit(newModel),
								total : newModel.totalRange,
								currentPage : newModel.pageIndex + 1
							});
						if (node.length > 0) {
							node.text(text)
						}
					}
				}
			}
		}
	};
	fluid.pager.directPageSize = function (that) {
		var node = that.locate("pageSize");
		if (node.length > 0) {
			that.events.onModelChange.addListener(function (newModel, oldModel) {
				if (node.val() !== newModel.pageSize) {
					node.val(newModel.pageSize)
				}
			});
			node.change(function () {
				that.events.initiatePageSizeChange.fire(node.val())
			})
		}
	};
	fluid.pager.rangeAnnotator = function (that, options) {
		var roots = {};
		that.events.onRenderPageLinks.addListener(function (tree, newModel) {
			var column = that.options.annotateColumnRange;
			var dataModel = that.options.dataModel;
			var columnDefs = getColumnDefs(that);
			if (!column || !dataModel || !columnDefs) {
				return
			}
			var columnDef = fluid.pager.findColumnDef(columnDefs, column);
			function fetchValue(index) {
				index = that.permutation ? that.permutation[index] : index;
				return fluid.pager.fetchValue(that, dataModel, index, columnDef.valuebinding, roots)
			}
			var tModel = {};
			fluid.model.copyModel(tModel, newModel);
			fluid.transform(tree, function (cell) {
				if (cell.ID === "page-link:link") {
					var page = cell.pageIndex;
					var start = page * tModel.pageSize;
					tModel.pageIndex = page;
					var limit = fluid.pager.computePageLimit(tModel);
					var iValue = fetchValue(start);
					var lValue = fetchValue(limit - 1);
					var tooltipOpts = fluid.copy(that.options.tooltip.options) || {};
					if (!tooltipOpts.content) {
						tooltipOpts.content = function () {
							return fluid.stringTemplate(that.options.markup.rangeAnnotation, {
								first : iValue,
								last : lValue
							})
						}
					}
					if (!cell.current) {
						var decorators = [{
								type : "fluid",
								func : that.options.tooltip.type,
								options : tooltipOpts
							}, {
								identify : page
							}
						];
						cell.decorators = cell.decorators.concat(decorators)
					}
				}
			})
		})
	};
	fluid.pagerImpl = function (container, options) {
		var that = fluid.initView("fluid.pager", container, options);
		that.container.attr("role", "application");
		that.events.initiatePageChange.addListener(function (arg) {
			var newModel = fluid.copy(that.model);
			if (arg.relativePage !== undefined) {
				newModel.pageIndex = that.model.pageIndex + arg.relativePage
			} else {
				newModel.pageIndex = arg.pageIndex
			}
			if (newModel.pageIndex === undefined || newModel.pageIndex < 0) {
				newModel.pageIndex = 0
			}
			fireModelChange(that, newModel, arg.forceUpdate)
		});
		that.events.initiatePageSizeChange.addListener(function (arg) {
			var newModel = fluid.copy(that.model);
			newModel.pageSize = arg;
			fireModelChange(that, newModel)
		});
		var pagerBarElement = that.locate("pagerBar");
		if (pagerBarElement.length > 0) {
			that.pagerBar = fluid.initSubcomponent(that, "pagerBar", [that.events, pagerBarElement, fluid.COMPONENT_OPTIONS, that.options.strings])
		}
		var pagerBarSecondaryElement = that.locate("pagerBarSecondary");
		if (pagerBarSecondaryElement.length > 0) {
			that.pagerBarSecondary = fluid.initSubcomponent(that, "pagerBar", [that.events, pagerBarSecondaryElement, fluid.COMPONENT_OPTIONS, that.options.strings])
		}
		that.bodyRenderer = fluid.initSubcomponent(that, "bodyRenderer", [that, fluid.COMPONENT_OPTIONS]);
		that.summary = fluid.initSubcomponent(that, "summary", [that.dom, fluid.COMPONENT_OPTIONS]);
		that.pageSize = fluid.initSubcomponent(that, "pageSize", [that]);
		that.rangeAnnotator = fluid.initSubcomponent(that, "rangeAnnotator", [that, fluid.COMPONENT_OPTIONS]);
		that.model = fluid.copy(that.options.model);
		var dataModel = fetchModel(that);
		if (dataModel) {
			that.model.totalRange = dataModel.length
		}
		if (that.model.totalRange === undefined) {
			if (!that.pagerBar) {
				fluid.fail("Error in Pager configuration - cannot determine total range,  since not configured in model.totalRange and no PagerBar is configured")
			}
			that.model = that.pagerBar.pageList.defaultModel
		}
		that.applier = fluid.makeChangeApplier(that.model);
		that.events.initiatePageChange.fire({
			pageIndex : that.model.pageIndex ? that.model.pageIndex : 0,
			forceUpdate : true
		});
		return that
	};
	fluid.defaults("fluid.pager", {
		mergePolicy : {
			dataModel : "preserve",
			model : "preserve"
		},
		pagerBar : {
			type : "fluid.pager.pagerBar"
		},
		summary : {
			type : "fluid.pager.summary",
			options : {
				message : "Viewing page %currentPage. Showing records %first - %last of %total items."
			}
		},
		pageSize : {
			type : "fluid.pager.directPageSize"
		},
		modelFilter : fluid.pager.directModelFilter,
		sorter : fluid.pager.basicSorter,
		bodyRenderer : {
			type : "fluid.pager.selfRender"
		},
		model : {
			pageIndex : undefined,
			pageSize : 10,
			totalRange : undefined
		},
		dataModel : undefined,
		dataOffset : "",
		columnDefs : [],
		annotateColumnRange : undefined,
		tooltip : {
			type : "fluid.tooltip"
		},
		rangeAnnotator : {
			type : "fluid.pager.rangeAnnotator"
		},
		selectors : {
			pagerBar : ".flc-pager-top",
			pagerBarSecondary : ".flc-pager-bottom",
			summary : ".flc-pager-summary",
			pageSize : ".flc-pager-page-size",
			headerSortStylisticOffset : ".flc-pager-sort-header"
		},
		styles : {
			ascendingHeader : "fl-pager-asc",
			descendingHeader : "fl-pager-desc"
		},
		decorators : {
			sortableHeader : [],
			unsortableHeader : []
		},
		strings : {
			last : " (last)"
		},
		events : {
			initiatePageChange : null,
			initiatePageSizeChange : null,
			onModelChange : null,
			onRenderPageLinks : null,
			afterRender : null
		},
		markup : {
			rangeAnnotation : "<b> %first </b><br/>&mdash;<br/><b> %last </b>"
		}
	})
})(jQuery, fluid_1_4);
var fluid_1_4 = fluid_1_4 || {};
(function ($, fluid) {
	var animateDisplay = function (elm, animation, defaultAnimation) {
		animation = (animation) ? animation : defaultAnimation;
		elm.animate(animation.params, animation.duration, animation.callback)
	};
	var animateProgress = function (elm, width, speed) {
		elm.queue("fx", []);
		elm.animate({
			width : width,
			queue : false
		}, speed)
	};
	var showProgress = function (that, animation) {
		if (animation === false) {
			that.displayElement.show()
		} else {
			animateDisplay(that.displayElement, animation, that.options.showAnimation)
		}
	};
	var hideProgress = function (that, delay, animation) {
		delay = (delay === null || isNaN(delay)) ? that.options.delay : delay;
		if (delay) {
			var timeOut = setTimeout(function () {
					hideProgress(that, 0, animation)
				}, delay)
		} else {
			if (animation === false) {
				that.displayElement.hide()
			} else {
				animateDisplay(that.displayElement, animation, that.options.hideAnimation)
			}
		}
	};
	var updateWidth = function (that, newWidth, dontAnimate) {
		dontAnimate = dontAnimate || false;
		var currWidth = that.indicator.width();
		var direction = that.options.animate;
		if ((newWidth > currWidth) && (direction === "both" || direction === "forward") && !dontAnimate) {
			animateProgress(that.indicator, newWidth, that.options.speed)
		} else {
			if ((newWidth < currWidth) && (direction === "both" || direction === "backward") && !dontAnimate) {
				animateProgress(that.indicator, newWidth, that.options.speed)
			} else {
				that.indicator.width(newWidth)
			}
		}
	};
	var percentToPixels = function (that, percent) {
		return Math.round((Math.min(percent, 100) * that.progressBar.innerWidth()) / 100)
	};
	var refreshRelativeWidth = function (that) {
		var pixels = Math.max(percentToPixels(that, parseFloat(that.storedPercent)), that.options.minWidth);
		updateWidth(that, pixels, true)
	};
	var initARIA = function (ariaElement, ariaBusyText) {
		ariaElement.attr("role", "progressbar");
		ariaElement.attr("aria-valuemin", "0");
		ariaElement.attr("aria-valuemax", "100");
		ariaElement.attr("aria-valuenow", "0");
		if (ariaBusyText) {
			ariaElement.attr("aria-valuetext", "")
		}
		ariaElement.attr("aria-busy", "false")
	};
	var updateARIA = function (that, percent) {
		var str = that.options.strings;
		var busy = percent < 100 && percent > 0;
		that.ariaElement.attr("aria-busy", busy);
		that.ariaElement.attr("aria-valuenow", percent);
		if (str.ariaBusyText) {
			if (busy) {
				var busyString = fluid.stringTemplate(str.ariaBusyText, {
						percentComplete : percent
					});
				that.ariaElement.attr("aria-valuetext", busyString)
			} else {
				if (percent === 100) {
					that.ariaElement.attr("aria-valuetext", str.ariaDoneText)
				}
			}
		}
	};
	var updateText = function (label, value) {
		label.html(value)
	};
	var repositionIndicator = function (that) {
		that.indicator.css("top", that.progressBar.position().top).css("left", 0).height(that.progressBar.height());
		refreshRelativeWidth(that)
	};
	var updateProgress = function (that, percent, labelText, animationForShow) {
		showProgress(that, animationForShow);
		if (percent !== null) {
			that.storedPercent = percent;
			var pixels = Math.max(percentToPixels(that, parseFloat(percent)), that.options.minWidth);
			updateWidth(that, pixels)
		}
		if (labelText !== null) {
			updateText(that.label, labelText)
		}
		if (that.ariaElement) {
			updateARIA(that, percent)
		}
	};
	var setupProgress = function (that) {
		that.displayElement = that.locate("displayElement");
		if (that.options.initiallyHidden) {
			that.displayElement.hide()
		}
		that.progressBar = that.locate("progressBar");
		that.label = that.locate("label");
		that.indicator = that.locate("indicator");
		that.ariaElement = that.locate("ariaElement");
		that.indicator.width(that.options.minWidth);
		that.storedPercent = 0;
		if (that.ariaElement) {
			initARIA(that.ariaElement, that.options.strings.ariaBusyText)
		}
		if (that.options.hideAnimation.callback) {
			that.events.afterProgressHidden.addListener(that.options.hideAnimation.callback)
		}
		that.options.hideAnimation.callback = that.events.afterProgressHidden.fire;
		if (that.options.showAnimation.callback) {
			that.events.onProgressBegin.addListener(that.options.showAnimation.callback)
		}
		that.options.showAnimation.callback = that.events.onProgressBegin.fire
	};
	fluid.progress = function (container, options) {
		var that = fluid.initView("fluid.progress", container, options);
		setupProgress(that);
		that.show = function (animation) {
			showProgress(that, animation)
		};
		that.hide = function (delay, animation) {
			hideProgress(that, delay, animation)
		};
		that.update = function (percentage, labelValue, animationForShow) {
			updateProgress(that, percentage, labelValue, animationForShow)
		};
		that.refreshView = function () {
			repositionIndicator(that)
		};
		return that
	};
	fluid.defaults("fluid.progress", {
		gradeNames : "fluid.viewComponent",
		selectors : {
			displayElement : ".flc-progress",
			progressBar : ".flc-progress-bar",
			indicator : ".flc-progress-indicator",
			label : ".flc-progress-label",
			ariaElement : ".flc-progress-bar"
		},
		strings : {
			ariaBusyText : "Progress is %percentComplete percent complete",
			ariaDoneText : "Progress is complete."
		},
		showAnimation : {
			params : {
				opacity : "show"
			},
			duration : "slow",
			callback : null
		},
		hideAnimation : {
			params : {
				opacity : "hide"
			},
			duration : "slow",
			callback : null
		},
		events : {
			onProgressBegin : null,
			afterProgressHidden : null
		},
		minWidth : 5,
		delay : 0,
		speed : 200,
		animate : "forward",
		initiallyHidden : true,
		updatePosition : false
	})
})(jQuery, fluid_1_4);
(function ($, undefined) {
	$.widget("ui.draggable", $.ui.mouse, {
		widgetEventPrefix : "drag",
		options : {
			addClasses : true,
			appendTo : "parent",
			axis : false,
			connectToSortable : false,
			containment : false,
			cursor : "auto",
			cursorAt : false,
			grid : false,
			handle : false,
			helper : "original",
			iframeFix : false,
			opacity : false,
			refreshPositions : false,
			revert : false,
			revertDuration : 500,
			scope : "default",
			scroll : true,
			scrollSensitivity : 20,
			scrollSpeed : 20,
			snap : false,
			snapMode : "both",
			snapTolerance : 20,
			stack : false,
			zIndex : false
		},
		_create : function () {
			if (this.options.helper == "original" && !(/^(?:r|a|f)/).test(this.element.css("position"))) {
				this.element[0].style.position = "relative"
			}
			(this.options.addClasses && this.element.addClass("ui-draggable"));
			(this.options.disabled && this.element.addClass("ui-draggable-disabled"));
			this._mouseInit()
		},
		destroy : function () {
			if (!this.element.data("draggable")) {
				return
			}
			this.element.removeData("draggable").unbind(".draggable").removeClass("ui-draggable ui-draggable-dragging ui-draggable-disabled");
			this._mouseDestroy();
			return this
		},
		_mouseCapture : function (event) {
			var o = this.options;
			if (this.helper || o.disabled || $(event.target).is(".ui-resizable-handle")) {
				return false
			}
			this.handle = this._getHandle(event);
			if (!this.handle) {
				return false
			}
			return true
		},
		_mouseStart : function (event) {
			var o = this.options;
			this.helper = this._createHelper(event);
			this._cacheHelperProportions();
			if ($.ui.ddmanager) {
				$.ui.ddmanager.current = this
			}
			this._cacheMargins();
			this.cssPosition = this.helper.css("position");
			this.scrollParent = this.helper.scrollParent();
			this.offset = this.positionAbs = this.element.offset();
			this.offset = {
				top : this.offset.top - this.margins.top,
				left : this.offset.left - this.margins.left
			};
			$.extend(this.offset, {
				click : {
					left : event.pageX - this.offset.left,
					top : event.pageY - this.offset.top
				},
				parent : this._getParentOffset(),
				relative : this._getRelativeOffset()
			});
			this.originalPosition = this.position = this._generatePosition(event);
			this.originalPageX = event.pageX;
			this.originalPageY = event.pageY;
			(o.cursorAt && this._adjustOffsetFromHelper(o.cursorAt));
			if (o.containment) {
				this._setContainment()
			}
			if (this._trigger("start", event) === false) {
				this._clear();
				return false
			}
			this._cacheHelperProportions();
			if ($.ui.ddmanager && !o.dropBehaviour) {
				$.ui.ddmanager.prepareOffsets(this, event)
			}
			this.helper.addClass("ui-draggable-dragging");
			this._mouseDrag(event, true);
			return true
		},
		_mouseDrag : function (event, noPropagation) {
			this.position = this._generatePosition(event);
			this.positionAbs = this._convertPositionTo("absolute");
			if (!noPropagation) {
				var ui = this._uiHash();
				if (this._trigger("drag", event, ui) === false) {
					this._mouseUp({});
					return false
				}
				this.position = ui.position
			}
			if (!this.options.axis || this.options.axis != "y") {
				this.helper[0].style.left = this.position.left + "px"
			}
			if (!this.options.axis || this.options.axis != "x") {
				this.helper[0].style.top = this.position.top + "px"
			}
			if ($.ui.ddmanager) {
				$.ui.ddmanager.drag(this, event)
			}
			return false
		},
		_mouseStop : function (event) {
			var dropped = false;
			if ($.ui.ddmanager && !this.options.dropBehaviour) {
				dropped = $.ui.ddmanager.drop(this, event)
			}
			if (this.dropped) {
				dropped = this.dropped;
				this.dropped = false
			}
			if ((!this.element[0] || !this.element[0].parentNode) && this.options.helper == "original") {
				return false
			}
			if ((this.options.revert == "invalid" && !dropped) || (this.options.revert == "valid" && dropped) || this.options.revert === true || ($.isFunction(this.options.revert) && this.options.revert.call(this.element, dropped))) {
				var self = this;
				$(this.helper).animate(this.originalPosition, parseInt(this.options.revertDuration, 10), function () {
					if (self._trigger("stop", event) !== false) {
						self._clear()
					}
				})
			} else {
				if (this._trigger("stop", event) !== false) {
					this._clear()
				}
			}
			return false
		},
		cancel : function () {
			if (this.helper.is(".ui-draggable-dragging")) {
				this._mouseUp({})
			} else {
				this._clear()
			}
			return this
		},
		_getHandle : function (event) {
			var handle = !this.options.handle || !$(this.options.handle, this.element).length ? true : false;
			$(this.options.handle, this.element).find("*").andSelf().each(function () {
				if (this == event.target) {
					handle = true
				}
			});
			return handle
		},
		_createHelper : function (event) {
			var o = this.options;
			var helper = $.isFunction(o.helper) ? $(o.helper.apply(this.element[0], [event])) : (o.helper == "clone" ? this.element.clone() : this.element);
			if (!helper.parents("body").length) {
				helper.appendTo((o.appendTo == "parent" ? this.element[0].parentNode : o.appendTo))
			}
			if (helper[0] != this.element[0] && !(/(fixed|absolute)/).test(helper.css("position"))) {
				helper.css("position", "absolute")
			}
			return helper
		},
		_adjustOffsetFromHelper : function (obj) {
			if (typeof obj == "string") {
				obj = obj.split(" ")
			}
			if ($.isArray(obj)) {
				obj = {
					left : +obj[0],
					top : +obj[1] || 0
				}
			}
			if ("left" in obj) {
				this.offset.click.left = obj.left + this.margins.left
			}
			if ("right" in obj) {
				this.offset.click.left = this.helperProportions.width - obj.right + this.margins.left
			}
			if ("top" in obj) {
				this.offset.click.top = obj.top + this.margins.top
			}
			if ("bottom" in obj) {
				this.offset.click.top = this.helperProportions.height - obj.bottom + this.margins.top
			}
		},
		_getParentOffset : function () {
			this.offsetParent = this.helper.offsetParent();
			var po = this.offsetParent.offset();
			if (this.cssPosition == "absolute" && this.scrollParent[0] != document && $.ui.contains(this.scrollParent[0], this.offsetParent[0])) {
				po.left += this.scrollParent.scrollLeft();
				po.top += this.scrollParent.scrollTop()
			}
			if ((this.offsetParent[0] == document.body) || (this.offsetParent[0].tagName && this.offsetParent[0].tagName.toLowerCase() == "html" && $.browser.msie)) {
				po = {
					top : 0,
					left : 0
				}
			}
			return {
				top : po.top + (parseInt(this.offsetParent.css("borderTopWidth"), 10) || 0),
				left : po.left + (parseInt(this.offsetParent.css("borderLeftWidth"), 10) || 0)
			}
		},
		_getRelativeOffset : function () {
			if (this.cssPosition == "relative") {
				var p = this.element.position();
				return {
					top : p.top - (parseInt(this.helper.css("top"), 10) || 0) + this.scrollParent.scrollTop(),
					left : p.left - (parseInt(this.helper.css("left"), 10) || 0) + this.scrollParent.scrollLeft()
				}
			} else {
				return {
					top : 0,
					left : 0
				}
			}
		},
		_cacheMargins : function () {
			this.margins = {
				left : (parseInt(this.element.css("marginLeft"), 10) || 0),
				top : (parseInt(this.element.css("marginTop"), 10) || 0),
				right : (parseInt(this.element.css("marginRight"), 10) || 0),
				bottom : (parseInt(this.element.css("marginBottom"), 10) || 0)
			}
		},
		_cacheHelperProportions : function () {
			this.helperProportions = {
				width : this.helper.outerWidth(),
				height : this.helper.outerHeight()
			}
		},
		_setContainment : function () {
			var o = this.options;
			if (o.containment == "parent") {
				o.containment = this.helper[0].parentNode
			}
			if (o.containment == "document" || o.containment == "window") {
				this.containment = [(o.containment == "document" ? 0 : $(window).scrollLeft()) - this.offset.relative.left - this.offset.parent.left, (o.containment == "document" ? 0 : $(window).scrollTop()) - this.offset.relative.top - this.offset.parent.top, (o.containment == "document" ? 0 : $(window).scrollLeft()) + $(o.containment == "document" ? document : window).width() - this.helperProportions.width - this.margins.left, (o.containment == "document" ? 0 : $(window).scrollTop()) + ($(o.containment == "document" ? document : window).height() || document.body.parentNode.scrollHeight) - this.helperProportions.height - this.margins.top]
			}
			if (!(/^(document|window|parent)$/).test(o.containment) && o.containment.constructor != Array) {
				var ce = $(o.containment)[0];
				if (!ce) {
					return
				}
				var co = $(o.containment).offset();
				var over = ($(ce).css("overflow") != "hidden");
				this.containment = [co.left + (parseInt($(ce).css("borderLeftWidth"), 10) || 0) + (parseInt($(ce).css("paddingLeft"), 10) || 0), co.top + (parseInt($(ce).css("borderTopWidth"), 10) || 0) + (parseInt($(ce).css("paddingTop"), 10) || 0), co.left + (over ? Math.max(ce.scrollWidth, ce.offsetWidth) : ce.offsetWidth) - (parseInt($(ce).css("borderLeftWidth"), 10) || 0) - (parseInt($(ce).css("paddingRight"), 10) || 0) - this.helperProportions.width - this.margins.left - this.margins.right, co.top + (over ? Math.max(ce.scrollHeight, ce.offsetHeight) : ce.offsetHeight) - (parseInt($(ce).css("borderTopWidth"), 10) || 0) - (parseInt($(ce).css("paddingBottom"), 10) || 0) - this.helperProportions.height - this.margins.top - this.margins.bottom]
			} else {
				if (o.containment.constructor == Array) {
					this.containment = o.containment
				}
			}
		},
		_convertPositionTo : function (d, pos) {
			if (!pos) {
				pos = this.position
			}
			var mod = d == "absolute" ? 1 : -1;
			var o = this.options,
			scroll = this.cssPosition == "absolute" && !(this.scrollParent[0] != document && $.ui.contains(this.scrollParent[0], this.offsetParent[0])) ? this.offsetParent : this.scrollParent,
			scrollIsRootNode = (/(html|body)/i).test(scroll[0].tagName);
			return {
				top : (pos.top + this.offset.relative.top * mod + this.offset.parent.top * mod - ($.browser.safari && $.browser.version < 526 && this.cssPosition == "fixed" ? 0 : (this.cssPosition == "fixed" ? -this.scrollParent.scrollTop() : (scrollIsRootNode ? 0 : scroll.scrollTop())) * mod)),
				left : (pos.left + this.offset.relative.left * mod + this.offset.parent.left * mod - ($.browser.safari && $.browser.version < 526 && this.cssPosition == "fixed" ? 0 : (this.cssPosition == "fixed" ? -this.scrollParent.scrollLeft() : scrollIsRootNode ? 0 : scroll.scrollLeft()) * mod))
			}
		},
		_generatePosition : function (event) {
			var o = this.options,
			scroll = this.cssPosition == "absolute" && !(this.scrollParent[0] != document && $.ui.contains(this.scrollParent[0], this.offsetParent[0])) ? this.offsetParent : this.scrollParent,
			scrollIsRootNode = (/(html|body)/i).test(scroll[0].tagName);
			var pageX = event.pageX;
			var pageY = event.pageY;
			if (this.originalPosition) {
				if (this.containment) {
					if (event.pageX - this.offset.click.left < this.containment[0]) {
						pageX = this.containment[0] + this.offset.click.left
					}
					if (event.pageY - this.offset.click.top < this.containment[1]) {
						pageY = this.containment[1] + this.offset.click.top
					}
					if (event.pageX - this.offset.click.left > this.containment[2]) {
						pageX = this.containment[2] + this.offset.click.left
					}
					if (event.pageY - this.offset.click.top > this.containment[3]) {
						pageY = this.containment[3] + this.offset.click.top
					}
				}
				if (o.grid) {
					var top = this.originalPageY + Math.round((pageY - this.originalPageY) / o.grid[1]) * o.grid[1];
					pageY = this.containment ? (!(top - this.offset.click.top < this.containment[1] || top - this.offset.click.top > this.containment[3]) ? top : (!(top - this.offset.click.top < this.containment[1]) ? top - o.grid[1] : top + o.grid[1])) : top;
					var left = this.originalPageX + Math.round((pageX - this.originalPageX) / o.grid[0]) * o.grid[0];
					pageX = this.containment ? (!(left - this.offset.click.left < this.containment[0] || left - this.offset.click.left > this.containment[2]) ? left : (!(left - this.offset.click.left < this.containment[0]) ? left - o.grid[0] : left + o.grid[0])) : left
				}
			}
			return {
				top : (pageY - this.offset.click.top - this.offset.relative.top - this.offset.parent.top + ($.browser.safari && $.browser.version < 526 && this.cssPosition == "fixed" ? 0 : (this.cssPosition == "fixed" ? -this.scrollParent.scrollTop() : (scrollIsRootNode ? 0 : scroll.scrollTop())))),
				left : (pageX - this.offset.click.left - this.offset.relative.left - this.offset.parent.left + ($.browser.safari && $.browser.version < 526 && this.cssPosition == "fixed" ? 0 : (this.cssPosition == "fixed" ? -this.scrollParent.scrollLeft() : scrollIsRootNode ? 0 : scroll.scrollLeft())))
			}
		},
		_clear : function () {
			this.helper.removeClass("ui-draggable-dragging");
			if (this.helper[0] != this.element[0] && !this.cancelHelperRemoval) {
				this.helper.remove()
			}
			this.helper = null;
			this.cancelHelperRemoval = false
		},
		_trigger : function (type, event, ui) {
			ui = ui || this._uiHash();
			$.ui.plugin.call(this, type, [event, ui]);
			if (type == "drag") {
				this.positionAbs = this._convertPositionTo("absolute")
			}
			return $.Widget.prototype._trigger.call(this, type, event, ui)
		},
		plugins : {},
		_uiHash : function (event) {
			return {
				helper : this.helper,
				position : this.position,
				originalPosition : this.originalPosition,
				offset : this.positionAbs
			}
		}
	});
	$.extend($.ui.draggable, {
		version : "1.8.12"
	});
	$.ui.plugin.add("draggable", "connectToSortable", {
		start : function (event, ui) {
			var inst = $(this).data("draggable"),
			o = inst.options,
			uiSortable = $.extend({}, ui, {
					item : inst.element
				});
			inst.sortables = [];
			$(o.connectToSortable).each(function () {
				var sortable = $.data(this, "sortable");
				if (sortable && !sortable.options.disabled) {
					inst.sortables.push({
						instance : sortable,
						shouldRevert : sortable.options.revert
					});
					sortable.refreshPositions();
					sortable._trigger("activate", event, uiSortable)
				}
			})
		},
		stop : function (event, ui) {
			var inst = $(this).data("draggable"),
			uiSortable = $.extend({}, ui, {
					item : inst.element
				});
			$.each(inst.sortables, function () {
				if (this.instance.isOver) {
					this.instance.isOver = 0;
					inst.cancelHelperRemoval = true;
					this.instance.cancelHelperRemoval = false;
					if (this.shouldRevert) {
						this.instance.options.revert = true
					}
					this.instance._mouseStop(event);
					this.instance.options.helper = this.instance.options._helper;
					if (inst.options.helper == "original") {
						this.instance.currentItem.css({
							top : "auto",
							left : "auto"
						})
					}
				} else {
					this.instance.cancelHelperRemoval = false;
					this.instance._trigger("deactivate", event, uiSortable)
				}
			})
		},
		drag : function (event, ui) {
			var inst = $(this).data("draggable"),
			self = this;
			var checkPos = function (o) {
				var dyClick = this.offset.click.top,
				dxClick = this.offset.click.left;
				var helperTop = this.positionAbs.top,
				helperLeft = this.positionAbs.left;
				var itemHeight = o.height,
				itemWidth = o.width;
				var itemTop = o.top,
				itemLeft = o.left;
				return $.ui.isOver(helperTop + dyClick, helperLeft + dxClick, itemTop, itemLeft, itemHeight, itemWidth)
			};
			$.each(inst.sortables, function (i) {
				this.instance.positionAbs = inst.positionAbs;
				this.instance.helperProportions = inst.helperProportions;
				this.instance.offset.click = inst.offset.click;
				if (this.instance._intersectsWith(this.instance.containerCache)) {
					if (!this.instance.isOver) {
						this.instance.isOver = 1;
						this.instance.currentItem = $(self).clone().appendTo(this.instance.element).data("sortable-item", true);
						this.instance.options._helper = this.instance.options.helper;
						this.instance.options.helper = function () {
							return ui.helper[0]
						};
						event.target = this.instance.currentItem[0];
						this.instance._mouseCapture(event, true);
						this.instance._mouseStart(event, true, true);
						this.instance.offset.click.top = inst.offset.click.top;
						this.instance.offset.click.left = inst.offset.click.left;
						this.instance.offset.parent.left -= inst.offset.parent.left - this.instance.offset.parent.left;
						this.instance.offset.parent.top -= inst.offset.parent.top - this.instance.offset.parent.top;
						inst._trigger("toSortable", event);
						inst.dropped = this.instance.element;
						inst.currentItem = inst.element;
						this.instance.fromOutside = inst
					}
					if (this.instance.currentItem) {
						this.instance._mouseDrag(event)
					}
				} else {
					if (this.instance.isOver) {
						this.instance.isOver = 0;
						this.instance.cancelHelperRemoval = true;
						this.instance.options.revert = false;
						this.instance._trigger("out", event, this.instance._uiHash(this.instance));
						this.instance._mouseStop(event, true);
						this.instance.options.helper = this.instance.options._helper;
						this.instance.currentItem.remove();
						if (this.instance.placeholder) {
							this.instance.placeholder.remove()
						}
						inst._trigger("fromSortable", event);
						inst.dropped = false
					}
				}
			})
		}
	});
	$.ui.plugin.add("draggable", "cursor", {
		start : function (event, ui) {
			var t = $("body"),
			o = $(this).data("draggable").options;
			if (t.css("cursor")) {
				o._cursor = t.css("cursor")
			}
			t.css("cursor", o.cursor)
		},
		stop : function (event, ui) {
			var o = $(this).data("draggable").options;
			if (o._cursor) {
				$("body").css("cursor", o._cursor)
			}
		}
	});
	$.ui.plugin.add("draggable", "iframeFix", {
		start : function (event, ui) {
			var o = $(this).data("draggable").options;
			$(o.iframeFix === true ? "iframe" : o.iframeFix).each(function () {
				$('<div class="ui-draggable-iframeFix" style="background: #fff;"></div>').css({
					width : this.offsetWidth + "px",
					height : this.offsetHeight + "px",
					position : "absolute",
					opacity : "0.001",
					zIndex : 1000
				}).css($(this).offset()).appendTo("body")
			})
		},
		stop : function (event, ui) {
			$("div.ui-draggable-iframeFix").each(function () {
				this.parentNode.removeChild(this)
			})
		}
	});
	$.ui.plugin.add("draggable", "opacity", {
		start : function (event, ui) {
			var t = $(ui.helper),
			o = $(this).data("draggable").options;
			if (t.css("opacity")) {
				o._opacity = t.css("opacity")
			}
			t.css("opacity", o.opacity)
		},
		stop : function (event, ui) {
			var o = $(this).data("draggable").options;
			if (o._opacity) {
				$(ui.helper).css("opacity", o._opacity)
			}
		}
	});
	$.ui.plugin.add("draggable", "scroll", {
		start : function (event, ui) {
			var i = $(this).data("draggable");
			if (i.scrollParent[0] != document && i.scrollParent[0].tagName != "HTML") {
				i.overflowOffset = i.scrollParent.offset()
			}
		},
		drag : function (event, ui) {
			var i = $(this).data("draggable"),
			o = i.options,
			scrolled = false;
			if (i.scrollParent[0] != document && i.scrollParent[0].tagName != "HTML") {
				if (!o.axis || o.axis != "x") {
					if ((i.overflowOffset.top + i.scrollParent[0].offsetHeight) - event.pageY < o.scrollSensitivity) {
						i.scrollParent[0].scrollTop = scrolled = i.scrollParent[0].scrollTop + o.scrollSpeed
					} else {
						if (event.pageY - i.overflowOffset.top < o.scrollSensitivity) {
							i.scrollParent[0].scrollTop = scrolled = i.scrollParent[0].scrollTop - o.scrollSpeed
						}
					}
				}
				if (!o.axis || o.axis != "y") {
					if ((i.overflowOffset.left + i.scrollParent[0].offsetWidth) - event.pageX < o.scrollSensitivity) {
						i.scrollParent[0].scrollLeft = scrolled = i.scrollParent[0].scrollLeft + o.scrollSpeed
					} else {
						if (event.pageX - i.overflowOffset.left < o.scrollSensitivity) {
							i.scrollParent[0].scrollLeft = scrolled = i.scrollParent[0].scrollLeft - o.scrollSpeed
						}
					}
				}
			} else {
				if (!o.axis || o.axis != "x") {
					if (event.pageY - $(document).scrollTop() < o.scrollSensitivity) {
						scrolled = $(document).scrollTop($(document).scrollTop() - o.scrollSpeed)
					} else {
						if ($(window).height() - (event.pageY - $(document).scrollTop()) < o.scrollSensitivity) {
							scrolled = $(document).scrollTop($(document).scrollTop() + o.scrollSpeed)
						}
					}
				}
				if (!o.axis || o.axis != "y") {
					if (event.pageX - $(document).scrollLeft() < o.scrollSensitivity) {
						scrolled = $(document).scrollLeft($(document).scrollLeft() - o.scrollSpeed)
					} else {
						if ($(window).width() - (event.pageX - $(document).scrollLeft()) < o.scrollSensitivity) {
							scrolled = $(document).scrollLeft($(document).scrollLeft() + o.scrollSpeed)
						}
					}
				}
			}
			if (scrolled !== false && $.ui.ddmanager && !o.dropBehaviour) {
				$.ui.ddmanager.prepareOffsets(i, event)
			}
		}
	});
	$.ui.plugin.add("draggable", "snap", {
		start : function (event, ui) {
			var i = $(this).data("draggable"),
			o = i.options;
			i.snapElements = [];
			$(o.snap.constructor != String ? (o.snap.items || ":data(draggable)") : o.snap).each(function () {
				var $t = $(this);
				var $o = $t.offset();
				if (this != i.element[0]) {
					i.snapElements.push({
						item : this,
						width : $t.outerWidth(),
						height : $t.outerHeight(),
						top : $o.top,
						left : $o.left
					})
				}
			})
		},
		drag : function (event, ui) {
			var inst = $(this).data("draggable"),
			o = inst.options;
			var d = o.snapTolerance;
			var x1 = ui.offset.left,
			x2 = x1 + inst.helperProportions.width,
			y1 = ui.offset.top,
			y2 = y1 + inst.helperProportions.height;
			for (var i = inst.snapElements.length - 1; i >= 0; i--) {
				var l = inst.snapElements[i].left,
				r = l + inst.snapElements[i].width,
				t = inst.snapElements[i].top,
				b = t + inst.snapElements[i].height;
				if (!((l - d < x1 && x1 < r + d && t - d < y1 && y1 < b + d) || (l - d < x1 && x1 < r + d && t - d < y2 && y2 < b + d) || (l - d < x2 && x2 < r + d && t - d < y1 && y1 < b + d) || (l - d < x2 && x2 < r + d && t - d < y2 && y2 < b + d))) {
					if (inst.snapElements[i].snapping) {
						(inst.options.snap.release && inst.options.snap.release.call(inst.element, event, $.extend(inst._uiHash(), {
									snapItem : inst.snapElements[i].item
								})))
					}
					inst.snapElements[i].snapping = false;
					continue
				}
				if (o.snapMode != "inner") {
					var ts = Math.abs(t - y2) <= d;
					var bs = Math.abs(b - y1) <= d;
					var ls = Math.abs(l - x2) <= d;
					var rs = Math.abs(r - x1) <= d;
					if (ts) {
						ui.position.top = inst._convertPositionTo("relative", {
								top : t - inst.helperProportions.height,
								left : 0
							}).top - inst.margins.top
					}
					if (bs) {
						ui.position.top = inst._convertPositionTo("relative", {
								top : b,
								left : 0
							}).top - inst.margins.top
					}
					if (ls) {
						ui.position.left = inst._convertPositionTo("relative", {
								top : 0,
								left : l - inst.helperProportions.width
							}).left - inst.margins.left
					}
					if (rs) {
						ui.position.left = inst._convertPositionTo("relative", {
								top : 0,
								left : r
							}).left - inst.margins.left
					}
				}
				var first = (ts || bs || ls || rs);
				if (o.snapMode != "outer") {
					var ts = Math.abs(t - y1) <= d;
					var bs = Math.abs(b - y2) <= d;
					var ls = Math.abs(l - x1) <= d;
					var rs = Math.abs(r - x2) <= d;
					if (ts) {
						ui.position.top = inst._convertPositionTo("relative", {
								top : t,
								left : 0
							}).top - inst.margins.top
					}
					if (bs) {
						ui.position.top = inst._convertPositionTo("relative", {
								top : b - inst.helperProportions.height,
								left : 0
							}).top - inst.margins.top
					}
					if (ls) {
						ui.position.left = inst._convertPositionTo("relative", {
								top : 0,
								left : l
							}).left - inst.margins.left
					}
					if (rs) {
						ui.position.left = inst._convertPositionTo("relative", {
								top : 0,
								left : r - inst.helperProportions.width
							}).left - inst.margins.left
					}
				}
				if (!inst.snapElements[i].snapping && (ts || bs || ls || rs || first)) {
					(inst.options.snap.snap && inst.options.snap.snap.call(inst.element, event, $.extend(inst._uiHash(), {
								snapItem : inst.snapElements[i].item
							})))
				}
				inst.snapElements[i].snapping = (ts || bs || ls || rs || first)
			}
		}
	});
	$.ui.plugin.add("draggable", "stack", {
		start : function (event, ui) {
			var o = $(this).data("draggable").options;
			var group = $.makeArray($(o.stack)).sort(function (a, b) {
					return (parseInt($(a).css("zIndex"), 10) || 0) - (parseInt($(b).css("zIndex"), 10) || 0)
				});
			if (!group.length) {
				return
			}
			var min = parseInt(group[0].style.zIndex) || 0;
			$(group).each(function (i) {
				this.style.zIndex = min + i
			});
			this[0].style.zIndex = min + group.length
		}
	});
	$.ui.plugin.add("draggable", "zIndex", {
		start : function (event, ui) {
			var t = $(ui.helper),
			o = $(this).data("draggable").options;
			if (t.css("zIndex")) {
				o._zIndex = t.css("zIndex")
			}
			t.css("zIndex", o.zIndex)
		},
		stop : function (event, ui) {
			var o = $(this).data("draggable").options;
			if (o._zIndex) {
				$(ui.helper).css("zIndex", o._zIndex)
			}
		}
	})
})(jQuery);
(function ($, undefined) {
	var uiDialogClasses = "ui-dialog ui-widget ui-widget-content ui-corner-all ",
	sizeRelatedOptions = {
		buttons : true,
		height : true,
		maxHeight : true,
		maxWidth : true,
		minHeight : true,
		minWidth : true,
		width : true
	},
	resizableRelatedOptions = {
		maxHeight : true,
		maxWidth : true,
		minHeight : true,
		minWidth : true
	},
	attrFn = $.attrFn || {
		val : true,
		css : true,
		html : true,
		text : true,
		data : true,
		width : true,
		height : true,
		offset : true,
		click : true
	};
	$.widget("ui.dialog", {
		options : {
			autoOpen : true,
			buttons : {},
			closeOnEscape : true,
			closeText : "close",
			dialogClass : "",
			draggable : true,
			hide : null,
			height : "auto",
			maxHeight : false,
			maxWidth : false,
			minHeight : 150,
			minWidth : 150,
			modal : false,
			position : {
				my : "center",
				at : "center",
				collision : "fit",
				using : function (pos) {
					var topOffset = $(this).css(pos).offset().top;
					if (topOffset < 0) {
						$(this).css("top", pos.top - topOffset)
					}
				}
			},
			resizable : true,
			show : null,
			stack : true,
			title : "",
			width : 300,
			zIndex : 1000
		},
		_create : function () {
			this.originalTitle = this.element.attr("title");
			if (typeof this.originalTitle !== "string") {
				this.originalTitle = ""
			}
			this.options.title = this.options.title || this.originalTitle;
			var self = this,
			options = self.options,
			title = options.title || "&#160;",
			titleId = $.ui.dialog.getTitleId(self.element),
			uiDialog = (self.uiDialog = $("<div></div>")).appendTo(document.body).hide().addClass(uiDialogClasses + options.dialogClass).css({
				zIndex : options.zIndex
			}).attr("tabIndex", -1).css("outline", 0).keydown(function (event) {
				if (options.closeOnEscape && event.keyCode && event.keyCode === $.ui.keyCode.ESCAPE) {
					self.close(event);
					event.preventDefault()
				}
			}).attr({
				role : "dialog",
				"aria-labelledby" : titleId
			}).mousedown(function (event) {
				self.moveToTop(false, event)
			}),
			uiDialogContent = self.element.show().removeAttr("title").addClass("ui-dialog-content ui-widget-content").appendTo(uiDialog),
			uiDialogTitlebar = (self.uiDialogTitlebar = $("<div></div>")).addClass("ui-dialog-titlebar ui-widget-header ui-corner-all ui-helper-clearfix").prependTo(uiDialog),
			uiDialogTitlebarClose = $('<a href="#"></a>').addClass("ui-dialog-titlebar-close ui-corner-all").attr("role", "button").hover(function () {
					uiDialogTitlebarClose.addClass("ui-state-hover")
				}, function () {
					uiDialogTitlebarClose.removeClass("ui-state-hover")
				}).focus(function () {
					uiDialogTitlebarClose.addClass("ui-state-focus")
				}).blur(function () {
					uiDialogTitlebarClose.removeClass("ui-state-focus")
				}).click(function (event) {
					self.close(event);
					return false
				}).appendTo(uiDialogTitlebar),
			uiDialogTitlebarCloseText = (self.uiDialogTitlebarCloseText = $("<span></span>")).addClass("ui-icon ui-icon-closethick").text(options.closeText).appendTo(uiDialogTitlebarClose),
			uiDialogTitle = $("<span></span>").addClass("ui-dialog-title").attr("id", titleId).html(title).prependTo(uiDialogTitlebar);
			if ($.isFunction(options.beforeclose) && !$.isFunction(options.beforeClose)) {
				options.beforeClose = options.beforeclose
			}
			uiDialogTitlebar.find("*").add(uiDialogTitlebar).disableSelection();
			if (options.draggable && $.fn.draggable) {
				self._makeDraggable()
			}
			if (options.resizable && $.fn.resizable) {
				self._makeResizable()
			}
			self._createButtons(options.buttons);
			self._isOpen = false;
			if ($.fn.bgiframe) {
				uiDialog.bgiframe()
			}
		},
		_init : function () {
			if (this.options.autoOpen) {
				this.open()
			}
		},
		destroy : function () {
			var self = this;
			if (self.overlay) {
				self.overlay.destroy()
			}
			self.uiDialog.hide();
			self.element.unbind(".dialog").removeData("dialog").removeClass("ui-dialog-content ui-widget-content").hide().appendTo("body");
			self.uiDialog.remove();
			if (self.originalTitle) {
				self.element.attr("title", self.originalTitle)
			}
			return self
		},
		widget : function () {
			return this.uiDialog
		},
		close : function (event) {
			var self = this,
			maxZ,
			thisZ;
			if (false === self._trigger("beforeClose", event)) {
				return
			}
			if (self.overlay) {
				self.overlay.destroy()
			}
			self.uiDialog.unbind("keypress.ui-dialog");
			self._isOpen = false;
			if (self.options.hide) {
				self.uiDialog.hide(self.options.hide, function () {
					self._trigger("close", event)
				})
			} else {
				self.uiDialog.hide();
				self._trigger("close", event)
			}
			$.ui.dialog.overlay.resize();
			if (self.options.modal) {
				maxZ = 0;
				$(".ui-dialog").each(function () {
					if (this !== self.uiDialog[0]) {
						thisZ = $(this).css("z-index");
						if (!isNaN(thisZ)) {
							maxZ = Math.max(maxZ, thisZ)
						}
					}
				});
				$.ui.dialog.maxZ = maxZ
			}
			return self
		},
		isOpen : function () {
			return this._isOpen
		},
		moveToTop : function (force, event) {
			var self = this,
			options = self.options,
			saveScroll;
			if ((options.modal && !force) || (!options.stack && !options.modal)) {
				return self._trigger("focus", event)
			}
			if (options.zIndex > $.ui.dialog.maxZ) {
				$.ui.dialog.maxZ = options.zIndex
			}
			if (self.overlay) {
				$.ui.dialog.maxZ += 1;
				self.overlay.$el.css("z-index", $.ui.dialog.overlay.maxZ = $.ui.dialog.maxZ)
			}
			saveScroll = {
				scrollTop : self.element.attr("scrollTop"),
				scrollLeft : self.element.attr("scrollLeft")
			};
			$.ui.dialog.maxZ += 1;
			self.uiDialog.css("z-index", $.ui.dialog.maxZ);
			self.element.attr(saveScroll);
			self._trigger("focus", event);
			return self
		},
		open : function () {
			if (this._isOpen) {
				return
			}
			var self = this,
			options = self.options,
			uiDialog = self.uiDialog;
			self.overlay = options.modal ? new $.ui.dialog.overlay(self) : null;
			self._size();
			self._position(options.position);
			uiDialog.show(options.show);
			self.moveToTop(true);
			if (options.modal) {
				uiDialog.bind("keypress.ui-dialog", function (event) {
					if (event.keyCode !== $.ui.keyCode.TAB) {
						return
					}
					var tabbables = $(":tabbable", this),
					first = tabbables.filter(":first"),
					last = tabbables.filter(":last");
					if (event.target === last[0] && !event.shiftKey) {
						first.focus(1);
						return false
					} else {
						if (event.target === first[0] && event.shiftKey) {
							last.focus(1);
							return false
						}
					}
				})
			}
			$(self.element.find(":tabbable").get().concat(uiDialog.find(".ui-dialog-buttonpane :tabbable").get().concat(uiDialog.get()))).eq(0).focus();
			self._isOpen = true;
			self._trigger("open");
			return self
		},
		_createButtons : function (buttons) {
			var self = this,
			hasButtons = false,
			uiDialogButtonPane = $("<div></div>").addClass("ui-dialog-buttonpane ui-widget-content ui-helper-clearfix"),
			uiButtonSet = $("<div></div>").addClass("ui-dialog-buttonset").appendTo(uiDialogButtonPane);
			self.uiDialog.find(".ui-dialog-buttonpane").remove();
			if (typeof buttons === "object" && buttons !== null) {
				$.each(buttons, function () {
					return !(hasButtons = true)
				})
			}
			if (hasButtons) {
				$.each(buttons, function (name, props) {
					props = $.isFunction(props) ? {
						click : props,
						text : name
					}
					 : props;
					var button = $('<button type="button"></button>').click(function () {
							props.click.apply(self.element[0], arguments)
						}).appendTo(uiButtonSet);
					$.each(props, function (key, value) {
						if (key === "click") {
							return
						}
						if (key in attrFn) {
							button[key](value)
						} else {
							button.attr(key, value)
						}
					});
					if ($.fn.button) {
						button.button()
					}
				});
				uiDialogButtonPane.appendTo(self.uiDialog)
			}
		},
		_makeDraggable : function () {
			var self = this,
			options = self.options,
			doc = $(document),
			heightBeforeDrag;
			function filteredUi(ui) {
				return {
					position : ui.position,
					offset : ui.offset
				}
			}
			self.uiDialog.draggable({
				cancel : ".ui-dialog-content, .ui-dialog-titlebar-close",
				handle : ".ui-dialog-titlebar",
				containment : "document",
				start : function (event, ui) {
					heightBeforeDrag = options.height === "auto" ? "auto" : $(this).height();
					$(this).height($(this).height()).addClass("ui-dialog-dragging");
					self._trigger("dragStart", event, filteredUi(ui))
				},
				drag : function (event, ui) {
					self._trigger("drag", event, filteredUi(ui))
				},
				stop : function (event, ui) {
					options.position = [ui.position.left - doc.scrollLeft(), ui.position.top - doc.scrollTop()];
					$(this).removeClass("ui-dialog-dragging").height(heightBeforeDrag);
					self._trigger("dragStop", event, filteredUi(ui));
					$.ui.dialog.overlay.resize()
				}
			})
		},
		_makeResizable : function (handles) {
			handles = (handles === undefined ? this.options.resizable : handles);
			var self = this,
			options = self.options,
			position = self.uiDialog.css("position"),
			resizeHandles = (typeof handles === "string" ? handles : "n,e,s,w,se,sw,ne,nw");
			function filteredUi(ui) {
				return {
					originalPosition : ui.originalPosition,
					originalSize : ui.originalSize,
					position : ui.position,
					size : ui.size
				}
			}
			self.uiDialog.resizable({
				cancel : ".ui-dialog-content",
				containment : "document",
				alsoResize : self.element,
				maxWidth : options.maxWidth,
				maxHeight : options.maxHeight,
				minWidth : options.minWidth,
				minHeight : self._minHeight(),
				handles : resizeHandles,
				start : function (event, ui) {
					$(this).addClass("ui-dialog-resizing");
					self._trigger("resizeStart", event, filteredUi(ui))
				},
				resize : function (event, ui) {
					self._trigger("resize", event, filteredUi(ui))
				},
				stop : function (event, ui) {
					$(this).removeClass("ui-dialog-resizing");
					options.height = $(this).height();
					options.width = $(this).width();
					self._trigger("resizeStop", event, filteredUi(ui));
					$.ui.dialog.overlay.resize()
				}
			}).css("position", position).find(".ui-resizable-se").addClass("ui-icon ui-icon-grip-diagonal-se")
		},
		_minHeight : function () {
			var options = this.options;
			if (options.height === "auto") {
				return options.minHeight
			} else {
				return Math.min(options.minHeight, options.height)
			}
		},
		_position : function (position) {
			var myAt = [],
			offset = [0, 0],
			isVisible;
			if (position) {
				if (typeof position === "string" || (typeof position === "object" && "0" in position)) {
					myAt = position.split ? position.split(" ") : [position[0], position[1]];
					if (myAt.length === 1) {
						myAt[1] = myAt[0]
					}
					$.each(["left", "top"], function (i, offsetPosition) {
						if (+myAt[i] === myAt[i]) {
							offset[i] = myAt[i];
							myAt[i] = offsetPosition
						}
					});
					position = {
						my : myAt.join(" "),
						at : myAt.join(" "),
						offset : offset.join(" ")
					}
				}
				position = $.extend({}, $.ui.dialog.prototype.options.position, position)
			} else {
				position = $.ui.dialog.prototype.options.position
			}
			isVisible = this.uiDialog.is(":visible");
			if (!isVisible) {
				this.uiDialog.show()
			}
			this.uiDialog.css({
				top : 0,
				left : 0
			}).position($.extend({
					of : window
				}, position));
			if (!isVisible) {
				this.uiDialog.hide()
			}
		},
		_setOptions : function (options) {
			var self = this,
			resizableOptions = {},
			resize = false;
			$.each(options, function (key, value) {
				self._setOption(key, value);
				if (key in sizeRelatedOptions) {
					resize = true
				}
				if (key in resizableRelatedOptions) {
					resizableOptions[key] = value
				}
			});
			if (resize) {
				this._size()
			}
			if (this.uiDialog.is(":data(resizable)")) {
				this.uiDialog.resizable("option", resizableOptions)
			}
		},
		_setOption : function (key, value) {
			var self = this,
			uiDialog = self.uiDialog;
			switch (key) {
			case "beforeclose":
				key = "beforeClose";
				break;
			case "buttons":
				self._createButtons(value);
				break;
			case "closeText":
				self.uiDialogTitlebarCloseText.text("" + value);
				break;
			case "dialogClass":
				uiDialog.removeClass(self.options.dialogClass).addClass(uiDialogClasses + value);
				break;
			case "disabled":
				if (value) {
					uiDialog.addClass("ui-dialog-disabled")
				} else {
					uiDialog.removeClass("ui-dialog-disabled")
				}
				break;
			case "draggable":
				var isDraggable = uiDialog.is(":data(draggable)");
				if (isDraggable && !value) {
					uiDialog.draggable("destroy")
				}
				if (!isDraggable && value) {
					self._makeDraggable()
				}
				break;
			case "position":
				self._position(value);
				break;
			case "resizable":
				var isResizable = uiDialog.is(":data(resizable)");
				if (isResizable && !value) {
					uiDialog.resizable("destroy")
				}
				if (isResizable && typeof value === "string") {
					uiDialog.resizable("option", "handles", value)
				}
				if (!isResizable && value !== false) {
					self._makeResizable(value)
				}
				break;
			case "title":
				$(".ui-dialog-title", self.uiDialogTitlebar).html("" + (value || "&#160;"));
				break
			}
			$.Widget.prototype._setOption.apply(self, arguments)
		},
		_size : function () {
			var options = this.options,
			nonContentHeight,
			minContentHeight,
			isVisible = this.uiDialog.is(":visible");
			this.element.show().css({
				width : "auto",
				minHeight : 0,
				height : 0
			});
			if (options.minWidth > options.width) {
				options.width = options.minWidth
			}
			nonContentHeight = this.uiDialog.css({
					height : "auto",
					width : options.width
				}).height();
			minContentHeight = Math.max(0, options.minHeight - nonContentHeight);
			if (options.height === "auto") {
				if ($.support.minHeight) {
					this.element.css({
						minHeight : minContentHeight,
						height : "auto"
					})
				} else {
					this.uiDialog.show();
					var autoHeight = this.element.css("height", "auto").height();
					if (!isVisible) {
						this.uiDialog.hide()
					}
					this.element.height(Math.max(autoHeight, minContentHeight))
				}
			} else {
				this.element.height(Math.max(options.height - nonContentHeight, 0))
			}
			if (this.uiDialog.is(":data(resizable)")) {
				this.uiDialog.resizable("option", "minHeight", this._minHeight())
			}
		}
	});
	$.extend($.ui.dialog, {
		version : "1.8.12",
		uuid : 0,
		maxZ : 0,
		getTitleId : function ($el) {
			var id = $el.attr("id");
			if (!id) {
				this.uuid += 1;
				id = this.uuid
			}
			return "ui-dialog-title-" + id
		},
		overlay : function (dialog) {
			this.$el = $.ui.dialog.overlay.create(dialog)
		}
	});
	$.extend($.ui.dialog.overlay, {
		instances : [],
		oldInstances : [],
		maxZ : 0,
		events : $.map("focus,mousedown,mouseup,keydown,keypress,click".split(","), function (event) {
			return event + ".dialog-overlay"
		}).join(" "),
		create : function (dialog) {
			if (this.instances.length === 0) {
				setTimeout(function () {
					if ($.ui.dialog.overlay.instances.length) {
						$(document).bind($.ui.dialog.overlay.events, function (event) {
							if ($(event.target).zIndex() < $.ui.dialog.overlay.maxZ) {
								return false
							}
						})
					}
				}, 1);
				$(document).bind("keydown.dialog-overlay", function (event) {
					if (dialog.options.closeOnEscape && event.keyCode && event.keyCode === $.ui.keyCode.ESCAPE) {
						dialog.close(event);
						event.preventDefault()
					}
				});
				$(window).bind("resize.dialog-overlay", $.ui.dialog.overlay.resize)
			}
			var $el = (this.oldInstances.pop() || $("<div></div>").addClass("ui-widget-overlay")).appendTo(document.body).css({
				width : this.width(),
				height : this.height()
			});
			if ($.fn.bgiframe) {
				$el.bgiframe()
			}
			this.instances.push($el);
			return $el
		},
		destroy : function ($el) {
			var indexOf = $.inArray($el, this.instances);
			if (indexOf != -1) {
				this.oldInstances.push(this.instances.splice(indexOf, 1)[0])
			}
			if (this.instances.length === 0) {
				$([document, window]).unbind(".dialog-overlay")
			}
			$el.remove();
			var maxZ = 0;
			$.each(this.instances, function () {
				maxZ = Math.max(maxZ, this.css("z-index"))
			});
			this.maxZ = maxZ
		},
		height : function () {
			var scrollHeight,
			offsetHeight;
			if ($.browser.msie && $.browser.version < 7) {
				scrollHeight = Math.max(document.documentElement.scrollHeight, document.body.scrollHeight);
				offsetHeight = Math.max(document.documentElement.offsetHeight, document.body.offsetHeight);
				if (scrollHeight < offsetHeight) {
					return $(window).height() + "px"
				} else {
					return scrollHeight + "px"
				}
			} else {
				return $(document).height() + "px"
			}
		},
		width : function () {
			var scrollWidth,
			offsetWidth;
			if ($.browser.msie && $.browser.version < 7) {
				scrollWidth = Math.max(document.documentElement.scrollWidth, document.body.scrollWidth);
				offsetWidth = Math.max(document.documentElement.offsetWidth, document.body.offsetWidth);
				if (scrollWidth < offsetWidth) {
					return $(window).width() + "px"
				} else {
					return scrollWidth + "px"
				}
			} else {
				return $(document).width() + "px"
			}
		},
		resize : function () {
			var $overlays = $([]);
			$.each($.ui.dialog.overlay.instances, function () {
				$overlays = $overlays.add(this)
			});
			$overlays.css({
				width : 0,
				height : 0
			}).css({
				width : $.ui.dialog.overlay.width(),
				height : $.ui.dialog.overlay.height()
			})
		}
	});
	$.extend($.ui.dialog.overlay.prototype, {
		destroy : function () {
			$.ui.dialog.overlay.destroy(this.$el)
		}
	})
}
	(jQuery));
(function ($, undefined) {
	var numPages = 5;
	$.widget("ui.slider", $.ui.mouse, {
		widgetEventPrefix : "slide",
		options : {
			animate : false,
			distance : 0,
			max : 100,
			min : 0,
			orientation : "horizontal",
			range : false,
			step : 1,
			value : 0,
			values : null
		},
		_create : function () {
			var self = this,
			o = this.options;
			this._keySliding = false;
			this._mouseSliding = false;
			this._animateOff = true;
			this._handleIndex = null;
			this._detectOrientation();
			this._mouseInit();
			this.element.addClass("ui-slider ui-slider-" + this.orientation + " ui-widget ui-widget-content ui-corner-all");
			if (o.disabled) {
				this.element.addClass("ui-slider-disabled ui-disabled")
			}
			this.range = $([]);
			if (o.range) {
				if (o.range === true) {
					this.range = $("<div></div>");
					if (!o.values) {
						o.values = [this._valueMin(), this._valueMin()]
					}
					if (o.values.length && o.values.length !== 2) {
						o.values = [o.values[0], o.values[0]]
					}
				} else {
					this.range = $("<div></div>")
				}
				this.range.appendTo(this.element).addClass("ui-slider-range");
				if (o.range === "min" || o.range === "max") {
					this.range.addClass("ui-slider-range-" + o.range)
				}
				this.range.addClass("ui-widget-header")
			}
			if ($(".ui-slider-handle", this.element).length === 0) {
				$("<a href='#'></a>").appendTo(this.element).addClass("ui-slider-handle")
			}
			if (o.values && o.values.length) {
				while ($(".ui-slider-handle", this.element).length < o.values.length) {
					$("<a href='#'></a>").appendTo(this.element).addClass("ui-slider-handle")
				}
			}
			this.handles = $(".ui-slider-handle", this.element).addClass("ui-state-default ui-corner-all");
			this.handle = this.handles.eq(0);
			this.handles.add(this.range).filter("a").click(function (event) {
				event.preventDefault()
			}).hover(function () {
				if (!o.disabled) {
					$(this).addClass("ui-state-hover")
				}
			}, function () {
				$(this).removeClass("ui-state-hover")
			}).focus(function () {
				if (!o.disabled) {
					$(".ui-slider .ui-state-focus").removeClass("ui-state-focus");
					$(this).addClass("ui-state-focus")
				} else {
					$(this).blur()
				}
			}).blur(function () {
				$(this).removeClass("ui-state-focus")
			});
			this.handles.each(function (i) {
				$(this).data("index.ui-slider-handle", i)
			});
			this.handles.keydown(function (event) {
				var ret = true,
				index = $(this).data("index.ui-slider-handle"),
				allowed,
				curVal,
				newVal,
				step;
				if (self.options.disabled) {
					return
				}
				switch (event.keyCode) {
				case $.ui.keyCode.HOME:
				case $.ui.keyCode.END:
				case $.ui.keyCode.PAGE_UP:
				case $.ui.keyCode.PAGE_DOWN:
				case $.ui.keyCode.UP:
				case $.ui.keyCode.RIGHT:
				case $.ui.keyCode.DOWN:
				case $.ui.keyCode.LEFT:
					ret = false;
					if (!self._keySliding) {
						self._keySliding = true;
						$(this).addClass("ui-state-active");
						allowed = self._start(event, index);
						if (allowed === false) {
							return
						}
					}
					break
				}
				step = self.options.step;
				if (self.options.values && self.options.values.length) {
					curVal = newVal = self.values(index)
				} else {
					curVal = newVal = self.value()
				}
				switch (event.keyCode) {
				case $.ui.keyCode.HOME:
					newVal = self._valueMin();
					break;
				case $.ui.keyCode.END:
					newVal = self._valueMax();
					break;
				case $.ui.keyCode.PAGE_UP:
					newVal = self._trimAlignValue(curVal + ((self._valueMax() - self._valueMin()) / numPages));
					break;
				case $.ui.keyCode.PAGE_DOWN:
					newVal = self._trimAlignValue(curVal - ((self._valueMax() - self._valueMin()) / numPages));
					break;
				case $.ui.keyCode.UP:
				case $.ui.keyCode.RIGHT:
					if (curVal === self._valueMax()) {
						return
					}
					newVal = self._trimAlignValue(curVal + step);
					break;
				case $.ui.keyCode.DOWN:
				case $.ui.keyCode.LEFT:
					if (curVal === self._valueMin()) {
						return
					}
					newVal = self._trimAlignValue(curVal - step);
					break
				}
				self._slide(event, index, newVal);
				return ret
			}).keyup(function (event) {
				var index = $(this).data("index.ui-slider-handle");
				if (self._keySliding) {
					self._keySliding = false;
					self._stop(event, index);
					self._change(event, index);
					$(this).removeClass("ui-state-active")
				}
			});
			this._refreshValue();
			this._animateOff = false
		},
		destroy : function () {
			this.handles.remove();
			this.range.remove();
			this.element.removeClass("ui-slider ui-slider-horizontal ui-slider-vertical ui-slider-disabled ui-widget ui-widget-content ui-corner-all").removeData("slider").unbind(".slider");
			this._mouseDestroy();
			return this
		},
		_mouseCapture : function (event) {
			var o = this.options,
			position,
			normValue,
			distance,
			closestHandle,
			self,
			index,
			allowed,
			offset,
			mouseOverHandle;
			if (o.disabled) {
				return false
			}
			this.elementSize = {
				width : this.element.outerWidth(),
				height : this.element.outerHeight()
			};
			this.elementOffset = this.element.offset();
			position = {
				x : event.pageX,
				y : event.pageY
			};
			normValue = this._normValueFromMouse(position);
			distance = this._valueMax() - this._valueMin() + 1;
			self = this;
			this.handles.each(function (i) {
				var thisDistance = Math.abs(normValue - self.values(i));
				if (distance > thisDistance) {
					distance = thisDistance;
					closestHandle = $(this);
					index = i
				}
			});
			if (o.range === true && this.values(1) === o.min) {
				index += 1;
				closestHandle = $(this.handles[index])
			}
			allowed = this._start(event, index);
			if (allowed === false) {
				return false
			}
			this._mouseSliding = true;
			self._handleIndex = index;
			closestHandle.addClass("ui-state-active").focus();
			offset = closestHandle.offset();
			mouseOverHandle = !$(event.target).parents().andSelf().is(".ui-slider-handle");
			this._clickOffset = mouseOverHandle ? {
				left : 0,
				top : 0
			}
			 : {
				left : event.pageX - offset.left - (closestHandle.width() / 2),
				top : event.pageY - offset.top - (closestHandle.height() / 2) - (parseInt(closestHandle.css("borderTopWidth"), 10) || 0) - (parseInt(closestHandle.css("borderBottomWidth"), 10) || 0) + (parseInt(closestHandle.css("marginTop"), 10) || 0)
			};
			if (!this.handles.hasClass("ui-state-hover")) {
				this._slide(event, index, normValue)
			}
			this._animateOff = true;
			return true
		},
		_mouseStart : function (event) {
			return true
		},
		_mouseDrag : function (event) {
			var position = {
				x : event.pageX,
				y : event.pageY
			},
			normValue = this._normValueFromMouse(position);
			this._slide(event, this._handleIndex, normValue);
			return false
		},
		_mouseStop : function (event) {
			this.handles.removeClass("ui-state-active");
			this._mouseSliding = false;
			this._stop(event, this._handleIndex);
			this._change(event, this._handleIndex);
			this._handleIndex = null;
			this._clickOffset = null;
			this._animateOff = false;
			return false
		},
		_detectOrientation : function () {
			this.orientation = (this.options.orientation === "vertical") ? "vertical" : "horizontal"
		},
		_normValueFromMouse : function (position) {
			var pixelTotal,
			pixelMouse,
			percentMouse,
			valueTotal,
			valueMouse;
			if (this.orientation === "horizontal") {
				pixelTotal = this.elementSize.width;
				pixelMouse = position.x - this.elementOffset.left - (this._clickOffset ? this._clickOffset.left : 0)
			} else {
				pixelTotal = this.elementSize.height;
				pixelMouse = position.y - this.elementOffset.top - (this._clickOffset ? this._clickOffset.top : 0)
			}
			percentMouse = (pixelMouse / pixelTotal);
			if (percentMouse > 1) {
				percentMouse = 1
			}
			if (percentMouse < 0) {
				percentMouse = 0
			}
			if (this.orientation === "vertical") {
				percentMouse = 1 - percentMouse
			}
			valueTotal = this._valueMax() - this._valueMin();
			valueMouse = this._valueMin() + percentMouse * valueTotal;
			return this._trimAlignValue(valueMouse)
		},
		_start : function (event, index) {
			var uiHash = {
				handle : this.handles[index],
				value : this.value()
			};
			if (this.options.values && this.options.values.length) {
				uiHash.value = this.values(index);
				uiHash.values = this.values()
			}
			return this._trigger("start", event, uiHash)
		},
		_slide : function (event, index, newVal) {
			var otherVal,
			newValues,
			allowed;
			if (this.options.values && this.options.values.length) {
				otherVal = this.values(index ? 0 : 1);
				if ((this.options.values.length === 2 && this.options.range === true) && ((index === 0 && newVal > otherVal) || (index === 1 && newVal < otherVal))) {
					newVal = otherVal
				}
				if (newVal !== this.values(index)) {
					newValues = this.values();
					newValues[index] = newVal;
					allowed = this._trigger("slide", event, {
							handle : this.handles[index],
							value : newVal,
							values : newValues
						});
					otherVal = this.values(index ? 0 : 1);
					if (allowed !== false) {
						this.values(index, newVal, true)
					}
				}
			} else {
				if (newVal !== this.value()) {
					allowed = this._trigger("slide", event, {
							handle : this.handles[index],
							value : newVal
						});
					if (allowed !== false) {
						this.value(newVal)
					}
				}
			}
		},
		_stop : function (event, index) {
			var uiHash = {
				handle : this.handles[index],
				value : this.value()
			};
			if (this.options.values && this.options.values.length) {
				uiHash.value = this.values(index);
				uiHash.values = this.values()
			}
			this._trigger("stop", event, uiHash)
		},
		_change : function (event, index) {
			if (!this._keySliding && !this._mouseSliding) {
				var uiHash = {
					handle : this.handles[index],
					value : this.value()
				};
				if (this.options.values && this.options.values.length) {
					uiHash.value = this.values(index);
					uiHash.values = this.values()
				}
				this._trigger("change", event, uiHash)
			}
		},
		value : function (newValue) {
			if (arguments.length) {
				this.options.value = this._trimAlignValue(newValue);
				this._refreshValue();
				this._change(null, 0);
				return
			}
			return this._value()
		},
		values : function (index, newValue) {
			var vals,
			newValues,
			i;
			if (arguments.length > 1) {
				this.options.values[index] = this._trimAlignValue(newValue);
				this._refreshValue();
				this._change(null, index);
				return
			}
			if (arguments.length) {
				if ($.isArray(arguments[0])) {
					vals = this.options.values;
					newValues = arguments[0];
					for (i = 0; i < vals.length; i += 1) {
						vals[i] = this._trimAlignValue(newValues[i]);
						this._change(null, i)
					}
					this._refreshValue()
				} else {
					if (this.options.values && this.options.values.length) {
						return this._values(index)
					} else {
						return this.value()
					}
				}
			} else {
				return this._values()
			}
		},
		_setOption : function (key, value) {
			var i,
			valsLength = 0;
			if ($.isArray(this.options.values)) {
				valsLength = this.options.values.length
			}
			$.Widget.prototype._setOption.apply(this, arguments);
			switch (key) {
			case "disabled":
				if (value) {
					this.handles.filter(".ui-state-focus").blur();
					this.handles.removeClass("ui-state-hover");
					this.handles.attr("disabled", "disabled");
					this.element.addClass("ui-disabled")
				} else {
					this.handles.removeAttr("disabled");
					this.element.removeClass("ui-disabled")
				}
				break;
			case "orientation":
				this._detectOrientation();
				this.element.removeClass("ui-slider-horizontal ui-slider-vertical").addClass("ui-slider-" + this.orientation);
				this._refreshValue();
				break;
			case "value":
				this._animateOff = true;
				this._refreshValue();
				this._change(null, 0);
				this._animateOff = false;
				break;
			case "values":
				this._animateOff = true;
				this._refreshValue();
				for (i = 0; i < valsLength; i += 1) {
					this._change(null, i)
				}
				this._animateOff = false;
				break
			}
		},
		_value : function () {
			var val = this.options.value;
			val = this._trimAlignValue(val);
			return val
		},
		_values : function (index) {
			var val,
			vals,
			i;
			if (arguments.length) {
				val = this.options.values[index];
				val = this._trimAlignValue(val);
				return val
			} else {
				vals = this.options.values.slice();
				for (i = 0; i < vals.length; i += 1) {
					vals[i] = this._trimAlignValue(vals[i])
				}
				return vals
			}
		},
		_trimAlignValue : function (val) {
			if (val <= this._valueMin()) {
				return this._valueMin()
			}
			if (val >= this._valueMax()) {
				return this._valueMax()
			}
			var step = (this.options.step > 0) ? this.options.step : 1,
			valModStep = (val - this._valueMin()) % step;
			alignValue = val - valModStep;
			if (Math.abs(valModStep) * 2 >= step) {
				alignValue += (valModStep > 0) ? step : (-step)
			}
			return parseFloat(alignValue.toFixed(5))
		},
		_valueMin : function () {
			return this.options.min
		},
		_valueMax : function () {
			return this.options.max
		},
		_refreshValue : function () {
			var oRange = this.options.range,
			o = this.options,
			self = this,
			animate = (!this._animateOff) ? o.animate : false,
			valPercent,
			_set = {},
			lastValPercent,
			value,
			valueMin,
			valueMax;
			if (this.options.values && this.options.values.length) {
				this.handles.each(function (i, j) {
					valPercent = (self.values(i) - self._valueMin()) / (self._valueMax() - self._valueMin()) * 100;
					_set[self.orientation === "horizontal" ? "left" : "bottom"] = valPercent + "%";
					$(this).stop(1, 1)[animate ? "animate" : "css"](_set, o.animate);
					if (self.options.range === true) {
						if (self.orientation === "horizontal") {
							if (i === 0) {
								self.range.stop(1, 1)[animate ? "animate" : "css"]({
									left : valPercent + "%"
								}, o.animate)
							}
							if (i === 1) {
								self.range[animate ? "animate" : "css"]({
									width : (valPercent - lastValPercent) + "%"
								}, {
									queue : false,
									duration : o.animate
								})
							}
						} else {
							if (i === 0) {
								self.range.stop(1, 1)[animate ? "animate" : "css"]({
									bottom : (valPercent) + "%"
								}, o.animate)
							}
							if (i === 1) {
								self.range[animate ? "animate" : "css"]({
									height : (valPercent - lastValPercent) + "%"
								}, {
									queue : false,
									duration : o.animate
								})
							}
						}
					}
					lastValPercent = valPercent
				})
			} else {
				value = this.value();
				valueMin = this._valueMin();
				valueMax = this._valueMax();
				valPercent = (valueMax !== valueMin) ? (value - valueMin) / (valueMax - valueMin) * 100 : 0;
				_set[self.orientation === "horizontal" ? "left" : "bottom"] = valPercent + "%";
				this.handle.stop(1, 1)[animate ? "animate" : "css"](_set, o.animate);
				if (oRange === "min" && this.orientation === "horizontal") {
					this.range.stop(1, 1)[animate ? "animate" : "css"]({
						width : valPercent + "%"
					}, o.animate)
				}
				if (oRange === "max" && this.orientation === "horizontal") {
					this.range[animate ? "animate" : "css"]({
						width : (100 - valPercent) + "%"
					}, {
						queue : false,
						duration : o.animate
					})
				}
				if (oRange === "min" && this.orientation === "vertical") {
					this.range.stop(1, 1)[animate ? "animate" : "css"]({
						height : valPercent + "%"
					}, o.animate)
				}
				if (oRange === "max" && this.orientation === "vertical") {
					this.range[animate ? "animate" : "css"]({
						height : (100 - valPercent) + "%"
					}, {
						queue : false,
						duration : o.animate
					})
				}
			}
		}
	});
	$.extend($.ui.slider, {
		version : "1.8.12"
	})
}
	(jQuery));
(function ($, undefined) {
	var tabId = 0,
	listId = 0;
	function getNextTabId() {
		return ++tabId
	}
	function getNextListId() {
		return ++listId
	}
	$.widget("ui.tabs", {
		options : {
			add : null,
			ajaxOptions : null,
			cache : false,
			cookie : null,
			collapsible : false,
			disable : null,
			disabled : [],
			enable : null,
			event : "click",
			fx : null,
			idPrefix : "ui-tabs-",
			load : null,
			panelTemplate : "<div></div>",
			remove : null,
			select : null,
			show : null,
			spinner : "<em>Loading&#8230;</em>",
			tabTemplate : "<li><a href='#{href}'><span>#{label}</span></a></li>"
		},
		_create : function () {
			this._tabify(true)
		},
		_setOption : function (key, value) {
			if (key == "selected") {
				if (this.options.collapsible && value == this.options.selected) {
					return
				}
				this.select(value)
			} else {
				this.options[key] = value;
				this._tabify()
			}
		},
		_tabId : function (a) {
			return a.title && a.title.replace(/\s/g, "_").replace(/[^\w\u00c0-\uFFFF-]/g, "") || this.options.idPrefix + getNextTabId()
		},
		_sanitizeSelector : function (hash) {
			return hash.replace(/:/g, "\\:")
		},
		_cookie : function () {
			var cookie = this.cookie || (this.cookie = this.options.cookie.name || "ui-tabs-" + getNextListId());
			return $.cookie.apply(null, [cookie].concat($.makeArray(arguments)))
		},
		_ui : function (tab, panel) {
			return {
				tab : tab,
				panel : panel,
				index : this.anchors.index(tab)
			}
		},
		_cleanup : function () {
			this.lis.filter(".ui-state-processing").removeClass("ui-state-processing").find("span:data(label.tabs)").each(function () {
				var el = $(this);
				el.html(el.data("label.tabs")).removeData("label.tabs")
			})
		},
		_tabify : function (init) {
			var self = this,
			o = this.options,
			fragmentId = /^#.+/;
			this.list = this.element.find("ol,ul").eq(0);
			this.lis = $(" > li:has(a[href])", this.list);
			this.anchors = this.lis.map(function () {
					return $("a", this)[0]
				});
			this.panels = $([]);
			this.anchors.each(function (i, a) {
				var href = $(a).attr("href");
				var hrefBase = href.split("#")[0],
				baseEl;
				if (hrefBase && (hrefBase === location.toString().split("#")[0] || (baseEl = $("base")[0]) && hrefBase === baseEl.href)) {
					href = a.hash;
					a.href = href
				}
				if (fragmentId.test(href)) {
					self.panels = self.panels.add(self.element.find(self._sanitizeSelector(href)))
				} else {
					if (href && href !== "#") {
						$.data(a, "href.tabs", href);
						$.data(a, "load.tabs", href.replace(/#.*$/, ""));
						var id = self._tabId(a);
						a.href = "#" + id;
						var $panel = self.element.find("#" + id);
						if (!$panel.length) {
							$panel = $(o.panelTemplate).attr("id", id).addClass("ui-tabs-panel ui-widget-content ui-corner-bottom").insertAfter(self.panels[i - 1] || self.list);
							$panel.data("destroy.tabs", true)
						}
						self.panels = self.panels.add($panel)
					} else {
						o.disabled.push(i)
					}
				}
			});
			if (init) {
				this.element.addClass("ui-tabs ui-widget ui-widget-content ui-corner-all");
				this.list.addClass("ui-tabs-nav ui-helper-reset ui-helper-clearfix ui-widget-header ui-corner-all");
				this.lis.addClass("ui-state-default ui-corner-top");
				this.panels.addClass("ui-tabs-panel ui-widget-content ui-corner-bottom");
				if (o.selected === undefined) {
					if (location.hash) {
						this.anchors.each(function (i, a) {
							if (a.hash == location.hash) {
								o.selected = i;
								return false
							}
						})
					}
					if (typeof o.selected !== "number" && o.cookie) {
						o.selected = parseInt(self._cookie(), 10)
					}
					if (typeof o.selected !== "number" && this.lis.filter(".ui-tabs-selected").length) {
						o.selected = this.lis.index(this.lis.filter(".ui-tabs-selected"))
					}
					o.selected = o.selected || (this.lis.length ? 0 : -1)
				} else {
					if (o.selected === null) {
						o.selected = -1
					}
				}
				o.selected = ((o.selected >= 0 && this.anchors[o.selected]) || o.selected < 0) ? o.selected : 0;
				o.disabled = $.unique(o.disabled.concat($.map(this.lis.filter(".ui-state-disabled"), function (n, i) {
								return self.lis.index(n)
							}))).sort();
				if ($.inArray(o.selected, o.disabled) != -1) {
					o.disabled.splice($.inArray(o.selected, o.disabled), 1)
				}
				this.panels.addClass("ui-tabs-hide");
				this.lis.removeClass("ui-tabs-selected ui-state-active");
				if (o.selected >= 0 && this.anchors.length) {
					self.element.find(self._sanitizeSelector(self.anchors[o.selected].hash)).removeClass("ui-tabs-hide");
					this.lis.eq(o.selected).addClass("ui-tabs-selected ui-state-active");
					self.element.queue("tabs", function () {
						self._trigger("show", null, self._ui(self.anchors[o.selected], self.element.find(self._sanitizeSelector(self.anchors[o.selected].hash))[0]))
					});
					this.load(o.selected)
				}
				$(window).bind("unload", function () {
					self.lis.add(self.anchors).unbind(".tabs");
					self.lis = self.anchors = self.panels = null
				})
			} else {
				o.selected = this.lis.index(this.lis.filter(".ui-tabs-selected"))
			}
			this.element[o.collapsible ? "addClass" : "removeClass"]("ui-tabs-collapsible");
			if (o.cookie) {
				this._cookie(o.selected, o.cookie)
			}
			for (var i = 0, li; (li = this.lis[i]); i++) {
				$(li)[$.inArray(i, o.disabled) != -1 && !$(li).hasClass("ui-tabs-selected") ? "addClass" : "removeClass"]("ui-state-disabled")
			}
			if (o.cache === false) {
				this.anchors.removeData("cache.tabs")
			}
			this.lis.add(this.anchors).unbind(".tabs");
			if (o.event !== "mouseover") {
				var addState = function (state, el) {
					if (el.is(":not(.ui-state-disabled)")) {
						el.addClass("ui-state-" + state)
					}
				};
				var removeState = function (state, el) {
					el.removeClass("ui-state-" + state)
				};
				this.lis.bind("mouseover.tabs", function () {
					addState("hover", $(this))
				});
				this.lis.bind("mouseout.tabs", function () {
					removeState("hover", $(this))
				});
				this.anchors.bind("focus.tabs", function () {
					addState("focus", $(this).closest("li"))
				});
				this.anchors.bind("blur.tabs", function () {
					removeState("focus", $(this).closest("li"))
				})
			}
			var hideFx,
			showFx;
			if (o.fx) {
				if ($.isArray(o.fx)) {
					hideFx = o.fx[0];
					showFx = o.fx[1]
				} else {
					hideFx = showFx = o.fx
				}
			}
			function resetStyle($el, fx) {
				$el.css("display", "");
				if (!$.support.opacity && fx.opacity) {
					$el[0].style.removeAttribute("filter")
				}
			}
			var showTab = showFx ? function (clicked, $show) {
				$(clicked).closest("li").addClass("ui-tabs-selected ui-state-active");
				$show.hide().removeClass("ui-tabs-hide").animate(showFx, showFx.duration || "normal", function () {
					resetStyle($show, showFx);
					self._trigger("show", null, self._ui(clicked, $show[0]))
				})
			}
			 : function (clicked, $show) {
				$(clicked).closest("li").addClass("ui-tabs-selected ui-state-active");
				$show.removeClass("ui-tabs-hide");
				self._trigger("show", null, self._ui(clicked, $show[0]))
			};
			var hideTab = hideFx ? function (clicked, $hide) {
				$hide.animate(hideFx, hideFx.duration || "normal", function () {
					self.lis.removeClass("ui-tabs-selected ui-state-active");
					$hide.addClass("ui-tabs-hide");
					resetStyle($hide, hideFx);
					self.element.dequeue("tabs")
				})
			}
			 : function (clicked, $hide, $show) {
				self.lis.removeClass("ui-tabs-selected ui-state-active");
				$hide.addClass("ui-tabs-hide");
				self.element.dequeue("tabs")
			};
			this.anchors.bind(o.event + ".tabs", function () {
				var el = this,
				$li = $(el).closest("li"),
				$hide = self.panels.filter(":not(.ui-tabs-hide)"),
				$show = self.element.find(self._sanitizeSelector(el.hash));
				if (($li.hasClass("ui-tabs-selected") && !o.collapsible) || $li.hasClass("ui-state-disabled") || $li.hasClass("ui-state-processing") || self.panels.filter(":animated").length || self._trigger("select", null, self._ui(this, $show[0])) === false) {
					this.blur();
					return false
				}
				o.selected = self.anchors.index(this);
				self.abort();
				if (o.collapsible) {
					if ($li.hasClass("ui-tabs-selected")) {
						o.selected = -1;
						if (o.cookie) {
							self._cookie(o.selected, o.cookie)
						}
						self.element.queue("tabs", function () {
							hideTab(el, $hide)
						}).dequeue("tabs");
						this.blur();
						return false
					} else {
						if (!$hide.length) {
							if (o.cookie) {
								self._cookie(o.selected, o.cookie)
							}
							self.element.queue("tabs", function () {
								showTab(el, $show)
							});
							self.load(self.anchors.index(this));
							this.blur();
							return false
						}
					}
				}
				if (o.cookie) {
					self._cookie(o.selected, o.cookie)
				}
				if ($show.length) {
					if ($hide.length) {
						self.element.queue("tabs", function () {
							hideTab(el, $hide)
						})
					}
					self.element.queue("tabs", function () {
						showTab(el, $show)
					});
					self.load(self.anchors.index(this))
				} else {
					throw "jQuery UI Tabs: Mismatching fragment identifier."
				}
				if ($.browser.msie) {
					this.blur()
				}
			});
			this.anchors.bind("click.tabs", function () {
				return false
			})
		},
		_getIndex : function (index) {
			if (typeof index == "string") {
				index = this.anchors.index(this.anchors.filter("[href$=" + index + "]"))
			}
			return index
		},
		destroy : function () {
			var o = this.options;
			this.abort();
			this.element.unbind(".tabs").removeClass("ui-tabs ui-widget ui-widget-content ui-corner-all ui-tabs-collapsible").removeData("tabs");
			this.list.removeClass("ui-tabs-nav ui-helper-reset ui-helper-clearfix ui-widget-header ui-corner-all");
			this.anchors.each(function () {
				var href = $.data(this, "href.tabs");
				if (href) {
					this.href = href
				}
				var $this = $(this).unbind(".tabs");
				$.each(["href", "load", "cache"], function (i, prefix) {
					$this.removeData(prefix + ".tabs")
				})
			});
			this.lis.unbind(".tabs").add(this.panels).each(function () {
				if ($.data(this, "destroy.tabs")) {
					$(this).remove()
				} else {
					$(this).removeClass(["ui-state-default", "ui-corner-top", "ui-tabs-selected", "ui-state-active", "ui-state-hover", "ui-state-focus", "ui-state-disabled", "ui-tabs-panel", "ui-widget-content", "ui-corner-bottom", "ui-tabs-hide"].join(" "))
				}
			});
			if (o.cookie) {
				this._cookie(null, o.cookie)
			}
			return this
		},
		add : function (url, label, index) {
			if (index === undefined) {
				index = this.anchors.length
			}
			var self = this,
			o = this.options,
			$li = $(o.tabTemplate.replace(/#\{href\}/g, url).replace(/#\{label\}/g, label)),
			id = !url.indexOf("#") ? url.replace("#", "") : this._tabId($("a", $li)[0]);
			$li.addClass("ui-state-default ui-corner-top").data("destroy.tabs", true);
			var $panel = self.element.find("#" + id);
			if (!$panel.length) {
				$panel = $(o.panelTemplate).attr("id", id).data("destroy.tabs", true)
			}
			$panel.addClass("ui-tabs-panel ui-widget-content ui-corner-bottom ui-tabs-hide");
			if (index >= this.lis.length) {
				$li.appendTo(this.list);
				$panel.appendTo(this.list[0].parentNode)
			} else {
				$li.insertBefore(this.lis[index]);
				$panel.insertBefore(this.panels[index])
			}
			o.disabled = $.map(o.disabled, function (n, i) {
					return n >= index ? ++n : n
				});
			this._tabify();
			if (this.anchors.length == 1) {
				o.selected = 0;
				$li.addClass("ui-tabs-selected ui-state-active");
				$panel.removeClass("ui-tabs-hide");
				this.element.queue("tabs", function () {
					self._trigger("show", null, self._ui(self.anchors[0], self.panels[0]))
				});
				this.load(0)
			}
			this._trigger("add", null, this._ui(this.anchors[index], this.panels[index]));
			return this
		},
		remove : function (index) {
			index = this._getIndex(index);
			var o = this.options,
			$li = this.lis.eq(index).remove(),
			$panel = this.panels.eq(index).remove();
			if ($li.hasClass("ui-tabs-selected") && this.anchors.length > 1) {
				this.select(index + (index + 1 < this.anchors.length ? 1 : -1))
			}
			o.disabled = $.map($.grep(o.disabled, function (n, i) {
						return n != index
					}), function (n, i) {
					return n >= index ? --n : n
				});
			this._tabify();
			this._trigger("remove", null, this._ui($li.find("a")[0], $panel[0]));
			return this
		},
		enable : function (index) {
			index = this._getIndex(index);
			var o = this.options;
			if ($.inArray(index, o.disabled) == -1) {
				return
			}
			this.lis.eq(index).removeClass("ui-state-disabled");
			o.disabled = $.grep(o.disabled, function (n, i) {
					return n != index
				});
			this._trigger("enable", null, this._ui(this.anchors[index], this.panels[index]));
			return this
		},
		disable : function (index) {
			index = this._getIndex(index);
			var self = this,
			o = this.options;
			if (index != o.selected) {
				this.lis.eq(index).addClass("ui-state-disabled");
				o.disabled.push(index);
				o.disabled.sort();
				this._trigger("disable", null, this._ui(this.anchors[index], this.panels[index]))
			}
			return this
		},
		select : function (index) {
			index = this._getIndex(index);
			if (index == -1) {
				if (this.options.collapsible && this.options.selected != -1) {
					index = this.options.selected
				} else {
					return this
				}
			}
			this.anchors.eq(index).trigger(this.options.event + ".tabs");
			return this
		},
		load : function (index) {
			index = this._getIndex(index);
			var self = this,
			o = this.options,
			a = this.anchors.eq(index)[0],
			url = $.data(a, "load.tabs");
			this.abort();
			if (!url || this.element.queue("tabs").length !== 0 && $.data(a, "cache.tabs")) {
				this.element.dequeue("tabs");
				return
			}
			this.lis.eq(index).addClass("ui-state-processing");
			if (o.spinner) {
				var span = $("span", a);
				span.data("label.tabs", span.html()).html(o.spinner)
			}
			this.xhr = $.ajax($.extend({}, o.ajaxOptions, {
						url : url,
						success : function (r, s) {
							self.element.find(self._sanitizeSelector(a.hash)).html(r);
							self._cleanup();
							if (o.cache) {
								$.data(a, "cache.tabs", true)
							}
							self._trigger("load", null, self._ui(self.anchors[index], self.panels[index]));
							try {
								o.ajaxOptions.success(r, s)
							} catch (e) {}
							
						},
						error : function (xhr, s, e) {
							self._cleanup();
							self._trigger("load", null, self._ui(self.anchors[index], self.panels[index]));
							try {
								o.ajaxOptions.error(xhr, s, index, a)
							} catch (e) {}
							
						}
					}));
			self.element.dequeue("tabs");
			return this
		},
		abort : function () {
			this.element.queue([]);
			this.panels.stop(false, true);
			this.element.queue("tabs", this.element.queue("tabs").splice(-2, 2));
			if (this.xhr) {
				this.xhr.abort();
				delete this.xhr
			}
			this._cleanup();
			return this
		},
		url : function (index, url) {
			this.anchors.eq(index).removeData("cache.tabs").data("load.tabs", url);
			return this
		},
		length : function () {
			return this.anchors.length
		}
	});
	$.extend($.ui.tabs, {
		version : "1.8.12"
	});
	$.extend($.ui.tabs.prototype, {
		rotation : null,
		rotate : function (ms, continuing) {
			var self = this,
			o = this.options;
			var rotate = self._rotate || (self._rotate = function (e) {
					clearTimeout(self.rotation);
					self.rotation = setTimeout(function () {
							var t = o.selected;
							self.select(++t < self.anchors.length ? t : 0)
						}, ms);
					if (e) {
						e.stopPropagation()
					}
				});
			var stop = self._unrotate || (self._unrotate = !continuing ? function (e) {
					if (e.clientX) {
						self.rotate(null)
					}
				}
					 : function (e) {
					t = o.selected;
					rotate()
				});
			if (ms) {
				this.element.bind("tabsshow", rotate);
				this.anchors.bind(o.event + ".tabs", stop);
				rotate()
			} else {
				clearTimeout(self.rotation);
				this.element.unbind("tabsshow", rotate);
				this.anchors.unbind(o.event + ".tabs", stop);
				delete this._rotate;
				delete this._unrotate
			}
			return this
		}
	})
})(jQuery);
if (!this.JSON) {
	JSON = function () {
		function f(n) {
			return n < 10 ? "0" + n : n
		}
		Date.prototype.toJSON = function () {
			return this.getUTCFullYear() + "-" + f(this.getUTCMonth() + 1) + "-" + f(this.getUTCDate()) + "T" + f(this.getUTCHours()) + ":" + f(this.getUTCMinutes()) + ":" + f(this.getUTCSeconds()) + "Z"
		};
		var m = {
			"\b" : "\\b",
			"\t" : "\\t",
			"\n" : "\\n",
			"\f" : "\\f",
			"\r" : "\\r",
			'"' : '\\"',
			"\\" : "\\\\"
		};
		function stringify(value, whitelist) {
			var a,
			i,
			k,
			l,
			r = /["\\\x00-\x1f\x7f-\x9f]/g,
			v;
			switch (typeof value) {
			case "string":
				return r.test(value) ? '"' + value.replace(r, function (a) {
					var c = m[a];
					if (c) {
						return c
					}
					c = a.charCodeAt();
					return "\\u00" + Math.floor(c / 16).toString(16) + (c % 16).toString(16)
				}) + '"' : '"' + value + '"';
			case "number":
				return isFinite(value) ? String(value) : "null";
			case "boolean":
			case "null":
				return String(value);
			case "object":
				if (!value) {
					return "null"
				}
				if (typeof value.toJSON === "function") {
					return stringify(value.toJSON())
				}
				a = [];
				if (typeof value.length === "number" && !(value.propertyIsEnumerable("length"))) {
					l = value.length;
					for (i = 0; i < l; i += 1) {
						a.push(stringify(value[i], whitelist) || "null")
					}
					return "[" + a.join(",") + "]"
				}
				if (whitelist) {
					l = whitelist.length;
					for (i = 0; i < l; i += 1) {
						k = whitelist[i];
						if (typeof k === "string") {
							v = stringify(value[k], whitelist);
							if (v) {
								a.push(stringify(k) + ":" + v)
							}
						}
					}
				} else {
					for (k in value) {
						if (typeof k === "string") {
							v = stringify(value[k], whitelist);
							if (v) {
								a.push(stringify(k) + ":" + v)
							}
						}
					}
				}
				return "{" + a.join(",") + "}"
			}
		}
		return {
			stringify : stringify,
			parse : function (text, filter) {
				var j;
				function walk(k, v) {
					var i,
					n;
					if (v && typeof v === "object") {
						for (i in v) {
							if (Object.prototype.hasOwnProperty.apply(v, [i])) {
								n = walk(i, v[i]);
								if (n !== undefined) {
									v[i] = n
								}
							}
						}
					}
					return filter(k, v)
				}
				if (/^[\],:{}\s]*$/.test(text.replace(/\\./g, "@").replace(/"[^"\\\n\r]*"|true|false|null|-?\d+(?:\.\d*)?(:?[eE][+\-]?\d+)?/g, "]").replace(/(?:^|:|,)(?:\s*\[)+/g, ""))) {
					j = eval("(" + text + ")");
					return typeof filter === "function" ? walk("", j) : j
				}
				throw new SyntaxError("parseJSON")
			}
		}
	}
	()
};
var fluid_1_4 = fluid_1_4 || {};
(function ($, fluid) {
	fluid.dom.computeAbsolutePosition = function (element) {
		var curleft = 0,
		curtop = 0;
		if (element.offsetParent) {
			do {
				curleft += element.offsetLeft;
				curtop += element.offsetTop;
				element = element.offsetParent
			} while (element);
			return [curleft, curtop]
		}
	};
	fluid.dom.cleanseScripts = function (element) {
		var cleansed = $.data(element, fluid.dom.cleanseScripts.MARKER);
		if (!cleansed) {
			fluid.dom.iterateDom(element, function (node) {
				return node.tagName.toLowerCase() === "script" ? "delete" : null
			});
			$.data(element, fluid.dom.cleanseScripts.MARKER, true)
		}
	};
	fluid.dom.cleanseScripts.MARKER = "fluid-scripts-cleansed";
	fluid.dom.insertAfter = function (newChild, refChild) {
		var nextSib = refChild.nextSibling;
		if (!nextSib) {
			refChild.parentNode.appendChild(newChild)
		} else {
			refChild.parentNode.insertBefore(newChild, nextSib)
		}
	};
	fluid.dom.isWhitespaceNode = function (node) {
		return !(/[^\t\n\r ]/.test(node.data))
	};
	fluid.dom.isIgnorableNode = function (node) {
		return (node.nodeType === 8) || ((node.nodeType === 3) && fluid.dom.isWhitespaceNode(node))
	}
})(jQuery, fluid_1_4);
var fluid_1_4 = fluid_1_4 || {};
(function ($, fluid) {
	fluid.orientation = {
		HORIZONTAL : 4,
		VERTICAL : 1
	};
	fluid.rectSides = {
		4 : ["left", "right"],
		1 : ["top", "bottom"],
		8 : "top",
		12 : "bottom",
		2 : "left",
		3 : "right"
	};
	fluid.position = {
		BEFORE : -1,
		AFTER : 1,
		INSIDE : 2,
		REPLACE : 3
	};
	fluid.direction = {
		NEXT : 1,
		PREVIOUS : -1,
		UP : 8,
		DOWN : 12,
		LEFT : 2,
		RIGHT : 3
	};
	fluid.directionSign = function (direction) {
		return direction === fluid.direction.UP || direction === fluid.direction.LEFT ? fluid.direction.PREVIOUS : fluid.direction.NEXT
	};
	fluid.directionAxis = function (direction) {
		return direction === fluid.direction.LEFT || direction === fluid.direction.RIGHT ? 0 : 1
	};
	fluid.directionOrientation = function (direction) {
		return fluid.directionAxis(direction) ? fluid.orientation.VERTICAL : fluid.orientation.HORIZONTAL
	};
	fluid.keycodeDirection = {
		up : fluid.direction.UP,
		down : fluid.direction.DOWN,
		left : fluid.direction.LEFT,
		right : fluid.direction.RIGHT
	};
	fluid.moveDom = function (source, target, position) {
		source = fluid.unwrap(source);
		target = fluid.unwrap(target);
		var scan;
		if (position === fluid.position.INSIDE) {
			target.appendChild(source)
		} else {
			if (position === fluid.position.BEFORE) {
				for (scan = target.previousSibling; ; scan = scan.previousSibling) {
					if (!scan || !fluid.dom.isIgnorableNode(scan)) {
						if (scan !== source) {
							fluid.dom.cleanseScripts(source);
							target.parentNode.insertBefore(source, target)
						}
						break
					}
				}
			} else {
				if (position === fluid.position.AFTER) {
					for (scan = target.nextSibling; ; scan = scan.nextSibling) {
						if (!scan || !fluid.dom.isIgnorableNode(scan)) {
							if (scan !== source) {
								fluid.dom.cleanseScripts(source);
								fluid.dom.insertAfter(source, target)
							}
							break
						}
					}
				} else {
					fluid.fail("Unrecognised position supplied to fluid.moveDom: " + position)
				}
			}
		}
	};
	fluid.normalisePosition = function (position, samespan, targeti, sourcei) {
		if (position === fluid.position.REPLACE) {
			position = samespan && targeti >= sourcei ? fluid.position.AFTER : fluid.position.BEFORE
		}
		return position
	};
	fluid.permuteDom = function (element, target, position, sourceelements, targetelements) {
		element = fluid.unwrap(element);
		target = fluid.unwrap(target);
		var sourcei = $.inArray(element, sourceelements);
		if (sourcei === -1) {
			fluid.fail("Error in permuteDom: source element " + fluid.dumpEl(element) + " not found in source list " + fluid.dumpEl(sourceelements))
		}
		var targeti = $.inArray(target, targetelements);
		if (targeti === -1) {
			fluid.fail("Error in permuteDom: target element " + fluid.dumpEl(target) + " not found in source list " + fluid.dumpEl(targetelements))
		}
		var samespan = sourceelements === targetelements;
		position = fluid.normalisePosition(position, samespan, targeti, sourcei);
		var oldn = {};
		oldn[fluid.position.AFTER] = element.nextSibling;
		oldn[fluid.position.BEFORE] = element.previousSibling;
		fluid.moveDom(sourceelements[sourcei], targetelements[targeti], position);
		var frontlimit = samespan ? targeti - 1 : sourceelements.length - 2;
		var i;
		if (position === fluid.position.BEFORE && samespan) {
			frontlimit--
		}
		if (!samespan || targeti > sourcei) {
			for (i = frontlimit; i > sourcei; --i) {
				fluid.moveDom(sourceelements[i + 1], sourceelements[i], fluid.position.AFTER)
			}
			if (sourcei + 1 < sourceelements.length) {
				fluid.moveDom(sourceelements[sourcei + 1], oldn[fluid.position.AFTER], fluid.position.BEFORE)
			}
		}
		var backlimit = samespan ? sourcei - 1 : targetelements.length - 1;
		if (position === fluid.position.AFTER) {
			targeti++
		}
		if (!samespan || targeti < sourcei) {
			for (i = targeti; i < backlimit; ++i) {
				fluid.moveDom(targetelements[i], targetelements[i + 1], fluid.position.BEFORE)
			}
			if (backlimit >= 0 && backlimit < targetelements.length - 1) {
				fluid.moveDom(targetelements[backlimit], oldn[fluid.position.BEFORE], fluid.position.AFTER)
			}
		}
	};
	var curCss = function (a, name) {
		return window.getComputedStyle ? window.getComputedStyle(a, null).getPropertyValue(name) : a.currentStyle[name]
	};
	var isAttached = function (node) {
		while (node && node.nodeName) {
			if (node.nodeName === "BODY") {
				return true
			}
			node = node.parentNode
		}
		return false
	};
	var generalHidden = function (a) {
		return "hidden" === a.type || curCss(a, "display") === "none" || curCss(a, "visibility") === "hidden" || !isAttached(a)
	};
	var computeGeometry = function (element, orientation, disposition) {
		var elem = {};
		elem.element = element;
		elem.orientation = orientation;
		if (disposition === fluid.position.INSIDE) {
			elem.position = disposition
		}
		if (generalHidden(element)) {
			elem.clazz = "hidden"
		}
		var pos = fluid.dom.computeAbsolutePosition(element) || [0, 0];
		var width = element.offsetWidth;
		var height = element.offsetHeight;
		elem.rect = {
			left : pos[0],
			top : pos[1]
		};
		elem.rect.right = pos[0] + width;
		elem.rect.bottom = pos[1] + height;
		return elem
	};
	var SENTINEL_DIMENSION = 10000;
	function dumprect(rect) {
		return "Rect top: " + rect.top + " left: " + rect.left + " bottom: " + rect.bottom + " right: " + rect.right
	}
	function dumpelem(cacheelem) {
		if (!cacheelem || !cacheelem.rect) {
			return "null"
		} else {
			return dumprect(cacheelem.rect) + " position: " + cacheelem.position + " for " + fluid.dumpEl(cacheelem.element)
		}
	}
	fluid.dropManager = function () {
		var targets = [];
		var cache = {};
		var that = {};
		var lastClosest;
		var lastGeometry;
		var displacementX,
		displacementY;
		that.updateGeometry = function (geometricInfo) {
			lastGeometry = geometricInfo;
			targets = [];
			cache = {};
			var mapper = geometricInfo.elementMapper;
			for (var i = 0; i < geometricInfo.extents.length; ++i) {
				var thisInfo = geometricInfo.extents[i];
				var orientation = thisInfo.orientation;
				var sides = fluid.rectSides[orientation];
				var processElement = function (element, sentB, sentF, disposition, j) {
					var cacheelem = computeGeometry(element, orientation, disposition);
					cacheelem.owner = thisInfo;
					if (cacheelem.clazz !== "hidden" && mapper) {
						cacheelem.clazz = mapper(element)
					}
					cache[fluid.dropManager.cacheKey(element)] = cacheelem;
					var backClass = fluid.dropManager.getRelativeClass(thisInfo.elements, j, fluid.position.BEFORE, cacheelem.clazz, mapper);
					var frontClass = fluid.dropManager.getRelativeClass(thisInfo.elements, j, fluid.position.AFTER, cacheelem.clazz, mapper);
					if (disposition === fluid.position.INSIDE) {
						targets[targets.length] = cacheelem
					} else {
						fluid.dropManager.splitElement(targets, sides, cacheelem, disposition, backClass, frontClass)
					}
					if (sentB && geometricInfo.sentinelize) {
						fluid.dropManager.sentinelizeElement(targets, sides, cacheelem, 1, disposition, backClass)
					}
					if (sentF && geometricInfo.sentinelize) {
						fluid.dropManager.sentinelizeElement(targets, sides, cacheelem, 0, disposition, frontClass)
					}
					return cacheelem
				};
				var allHidden = true;
				for (var j = 0; j < thisInfo.elements.length; ++j) {
					var element = thisInfo.elements[j];
					var cacheelem = processElement(element, j === 0, j === thisInfo.elements.length - 1, fluid.position.INTERLEAVED, j);
					if (cacheelem.clazz !== "hidden") {
						allHidden = false
					}
				}
				if (allHidden && thisInfo.parentElement) {
					processElement(thisInfo.parentElement, true, true, fluid.position.INSIDE)
				}
			}
		};
		that.startDrag = function (event, handlePos, handleWidth, handleHeight) {
			var handleMidX = handlePos[0] + handleWidth / 2;
			var handleMidY = handlePos[1] + handleHeight / 2;
			var dX = handleMidX - event.pageX;
			var dY = handleMidY - event.pageY;
			that.updateGeometry(lastGeometry);
			lastClosest = null;
			displacementX = dX;
			displacementY = dY;
			$("body").bind("mousemove.fluid-dropManager", that.mouseMove)
		};
		that.lastPosition = function () {
			return lastClosest
		};
		that.endDrag = function () {
			$("body").unbind("mousemove.fluid-dropManager")
		};
		that.mouseMove = function (evt) {
			var x = evt.pageX + displacementX;
			var y = evt.pageY + displacementY;
			var closestTarget = that.closestTarget(x, y, lastClosest);
			if (closestTarget && closestTarget !== fluid.dropManager.NO_CHANGE) {
				lastClosest = closestTarget;
				that.dropChangeFirer.fire(closestTarget)
			}
		};
		that.dropChangeFirer = fluid.event.getEventFirer();
		var blankHolder = {
			element : null
		};
		that.closestTarget = function (x, y, lastClosest) {
			var mindistance = Number.MAX_VALUE;
			var minelem = blankHolder;
			var minlockeddistance = Number.MAX_VALUE;
			var minlockedelem = blankHolder;
			for (var i = 0; i < targets.length; ++i) {
				var cacheelem = targets[i];
				if (cacheelem.clazz === "hidden") {
					continue
				}
				var distance = fluid.geom.minPointRectangle(x, y, cacheelem.rect);
				if (cacheelem.clazz === "locked") {
					if (distance < minlockeddistance) {
						minlockeddistance = distance;
						minlockedelem = cacheelem
					}
				} else {
					if (distance < mindistance) {
						mindistance = distance;
						minelem = cacheelem
					}
					if (distance === 0) {
						break
					}
				}
			}
			if (!minelem) {
				return minelem
			}
			if (minlockeddistance >= mindistance) {
				minlockedelem = blankHolder
			}
			if (lastClosest && lastClosest.position === minelem.position && fluid.unwrap(lastClosest.element) === fluid.unwrap(minelem.element) && fluid.unwrap(lastClosest.lockedelem) === fluid.unwrap(minlockedelem.element)) {
				return fluid.dropManager.NO_CHANGE
			}
			return {
				position : minelem.position,
				element : minelem.element,
				lockedelem : minlockedelem.element
			}
		};
		that.shuffleProjectFrom = function (element, direction, includeLocked, disableWrap) {
			var togo = that.projectFrom(element, direction, includeLocked, disableWrap);
			if (togo) {
				togo.position = fluid.position.REPLACE
			}
			return togo
		};
		that.projectFrom = function (element, direction, includeLocked, disableWrap) {
			that.updateGeometry(lastGeometry);
			var cacheelem = cache[fluid.dropManager.cacheKey(element)];
			var projected = fluid.geom.projectFrom(cacheelem.rect, direction, targets, includeLocked, disableWrap);
			if (!projected.cacheelem) {
				return null
			}
			var retpos = projected.cacheelem.position;
			return {
				element : projected.cacheelem.element,
				position : retpos ? retpos : fluid.position.BEFORE
			}
		};
    
    
		that.logicalFrom = function (element, direction, includeLocked, disableWrap) {
			var orderables = that.getOwningSpan(element, fluid.position.INTERLEAVED, includeLocked);
			var pos;
			switch (direction) {
				case fluid.direction.UP:
				case fluid.direction.LEFT:
				case fluid.direction.PREVIOUS:
					pos = fluid.position.BEFORE;
					break;
				case fluid.direction.DOWN:
				case fluid.direction.RIGHT:
				case fluid.direction.NEXT:
					pos = fluid.position.AFTER;
					break;
			}

			return {
				element : fluid.dropManager.getRelativeElement(element, direction, orderables, disableWrap),
				position : pos
			}
		};
		that.lockedWrapFrom = function (element, direction, includeLocked, disableWrap) {
			var base = that.logicalFrom(element, direction, includeLocked, disableWrap);
			var selectables = that.getOwningSpan(element, fluid.position.INTERLEAVED, includeLocked);
			var allElements = cache[fluid.dropManager.cacheKey(element)].owner.elements;
			if (includeLocked || selectables[0] === allElements[0]) {
				return base
			}
			var directElement = fluid.dropManager.getRelativeElement(element, direction, allElements, disableWrap);
			if (lastGeometry.elementMapper(directElement) === "locked") {
				base.element = null;
				base.clazz = "locked"
			}
			return base
		};
		that.getOwningSpan = function (element, position, includeLocked) {
			var owner = cache[fluid.dropManager.cacheKey(element)].owner;
			var elements = position === fluid.position.INSIDE ? [owner.parentElement] : owner.elements;
			if (!includeLocked && lastGeometry.elementMapper) {
				elements = $.makeArray(elements);
				fluid.remove_if(elements, function (element) {
					return lastGeometry.elementMapper(element) === "locked"
				})
			}
			return elements
		};
		that.geometricMove = function (element, target, position) {
			var sourceElements = that.getOwningSpan(element, null, true);
			var targetElements = that.getOwningSpan(target, position, true);
			fluid.permuteDom(element, target, position, sourceElements, targetElements)
		};
		return that
	};
	fluid.dropManager.NO_CHANGE = "no change";
	fluid.dropManager.cacheKey = function (element) {
		return fluid.allocateSimpleId(element)
	};
	fluid.dropManager.sentinelizeElement = function (targets, sides, cacheelem, fc, disposition, clazz) {
		var elemCopy = $.extend(true, {}, cacheelem);
		elemCopy.rect[sides[fc]] = elemCopy.rect[sides[1 - fc]] + (fc ? 1 : -1);
		elemCopy.rect[sides[1 - fc]] = (fc ? -1 : 1) * SENTINEL_DIMENSION;
		elemCopy.position = disposition === fluid.position.INSIDE ? disposition : (fc ? fluid.position.BEFORE : fluid.position.AFTER);
		elemCopy.clazz = clazz;
		targets[targets.length] = elemCopy
	};
	fluid.dropManager.splitElement = function (targets, sides, cacheelem, disposition, clazz1, clazz2) {
		var elem1 = $.extend(true, {}, cacheelem);
		var elem2 = $.extend(true, {}, cacheelem);
		var midpoint = (elem1.rect[sides[0]] + elem1.rect[sides[1]]) / 2;
		elem1.rect[sides[1]] = midpoint;
		elem1.position = fluid.position.BEFORE;
		elem2.rect[sides[0]] = midpoint;
		elem2.position = fluid.position.AFTER;
		elem1.clazz = clazz1;
		elem2.clazz = clazz2;
		targets[targets.length] = elem1;
		targets[targets.length] = elem2
	};
	fluid.dropManager.getRelativeClass = function (thisElements, index, relative, thisclazz, mapper) {
		index += relative;
		if (index < 0 && thisclazz === "locked") {
			return "locked"
		}
		if (index >= thisElements.length || mapper === null) {
			return null
		} else {
			relative = thisElements[index];
			return mapper(relative) === "locked" && thisclazz === "locked" ? "locked" : null
		}
	};
	fluid.dropManager.getRelativeElement = function (element, direction, elements, disableWrap) {
		var folded = fluid.directionSign(direction);
		var index = $(elements).index(element) + folded;
		if (index < 0) {
			index += elements.length
		}
		if (disableWrap) {
			if (index === elements.length || index === (elements.length + folded)) {
				return element
			}
		}
		index %= elements.length;
		return elements[index]
	};
	fluid.geom = fluid.geom || {};
	fluid.geom.minPointRectangle = function (x, y, rectangle) {
		var dx = x < rectangle.left ? (rectangle.left - x) : (x > rectangle.right ? (x - rectangle.right) : 0);
		var dy = y < rectangle.top ? (rectangle.top - y) : (y > rectangle.bottom ? (y - rectangle.bottom) : 0);
		return dx * dx + dy * dy
	};
	fluid.geom.minRectRect = function (rect1, rect2) {
		var dx = rect1.right < rect2.left ? rect2.left - rect1.right : rect2.right < rect1.left ? rect1.left - rect2.right : 0;
		var dy = rect1.bottom < rect2.top ? rect2.top - rect1.bottom : rect2.bottom < rect1.top ? rect1.top - rect2.bottom : 0;
		return dx * dx + dy * dy
	};
	var makePenCollect = function () {
		return {
			mindist : Number.MAX_VALUE,
			minrdist : Number.MAX_VALUE
		}
	};
	fluid.geom.projectFrom = function (baserect, direction, targets, forSelection, disableWrap) {
		var axis = fluid.directionAxis(direction);
		var frontSide = fluid.rectSides[direction];
		var backSide = fluid.rectSides[axis * 15 + 5 - direction];
		var dirSign = fluid.directionSign(direction);
		var penrect = {
			left : (7 * baserect.left + 1 * baserect.right) / 8,
			right : (5 * baserect.left + 3 * baserect.right) / 8,
			top : (7 * baserect.top + 1 * baserect.bottom) / 8,
			bottom : (5 * baserect.top + 3 * baserect.bottom) / 8
		};
		penrect[frontSide] = dirSign * SENTINEL_DIMENSION;
		penrect[backSide] = -penrect[frontSide];
		function accPen(collect, cacheelem, backSign) {
			var thisrect = cacheelem.rect;
			var pdist = fluid.geom.minRectRect(penrect, thisrect);
			var rdist = -dirSign * backSign * (baserect[backSign === 1 ? frontSide : backSide] - thisrect[backSign === 1 ? backSide : frontSide]);
			if (pdist <= collect.mindist && rdist >= 0) {
				if (pdist === collect.mindist && rdist * backSign > collect.minrdist) {
					return
				}
				collect.minrdist = rdist * backSign;
				collect.mindist = pdist;
				collect.minelem = cacheelem
			}
		}
		var collect = makePenCollect();
		var backcollect = makePenCollect();
		var lockedcollect = makePenCollect();
		for (var i = 0; i < targets.length; ++i) {
			var elem = targets[i];
			var isPure = elem.owner && elem.element === elem.owner.parentElement;
			if (elem.clazz === "hidden" || (forSelection && isPure)) {
				continue
			} else {
				if (!forSelection && elem.clazz === "locked") {
					accPen(lockedcollect, elem, 1)
				} else {
					accPen(collect, elem, 1);
					accPen(backcollect, elem, -1)
				}
			}
		}
		var wrap = !collect.minelem || backcollect.mindist < collect.mindist;
		wrap = wrap && !disableWrap;
		var mincollect = wrap ? backcollect : collect;
		var togo = {
			wrapped : wrap,
			cacheelem : mincollect.minelem
		};
		if (lockedcollect.mindist < mincollect.mindist) {
			togo.lockedelem = lockedcollect.minelem
		}
		return togo
	}
})(jQuery, fluid_1_4);
var fluid_1_4 = fluid_1_4 || {};
(function ($, fluid) {
	var defaultAvatarCreator = function (item, cssClass, dropWarning) {
		fluid.dom.cleanseScripts(fluid.unwrap(item));
		var avatar = $(item).clone();
		fluid.dom.iterateDom(avatar.get(0), function (node) {
			node.removeAttribute("id");
			if (node.tagName.toLowerCase() === "input") {
				node.setAttribute("disabled", "disabled")
			}
		});
		avatar.removeProp("id");
		avatar.removeClass("ui-droppable");
		avatar.addClass(cssClass);
		if (dropWarning) {
			var avatarContainer = $(document.createElement("div"));
			avatarContainer.append(avatar);
			avatarContainer.append(dropWarning);
			avatar = avatarContainer
		}
		$("body").append(avatar);
		if (!$.browser.safari) {
			avatar.css("display", "block").width(item.offsetWidth).height(item.offsetHeight)
		}
		if ($.browser.opera) {
			avatar.hide()
		}
		return avatar
	};
	function bindHandlersToContainer(container, keyDownHandler, keyUpHandler, mouseMoveHandler) {
		var actualKeyDown = keyDownHandler;
		var advancedPrevention = false;
		if ($.browser.opera) {
			container.keypress(function (evt) {
				if (advancedPrevention) {
					advancedPrevention = false;
					evt.preventDefault();
					return false
				}
			});
			actualKeyDown = function (evt) {
				var oldret = keyDownHandler(evt);
				if (oldret === false) {
					advancedPrevention = true
				}
			}
		}
		container.keydown(actualKeyDown);
		container.keyup(keyUpHandler)
	}
	function addRolesToContainer(that) {
		that.container.attr("role", that.options.containerRole.container);
		that.container.attr("aria-multiselectable", "false");
		that.container.attr("aria-readonly", "false");
		that.container.attr("aria-disabled", "false")
	}
	function createAvatarId(parentId) {
		return parentId + "_avatar"
	}
	var adaptKeysets = function (options) {
		if (options.keysets && !(options.keysets instanceof Array)) {
			options.keysets = [options.keysets]
		}
	};
	fluid.reorderer = function (container, options) {
		if (!container) {
			fluid.fail("Reorderer initialised with no container")
		}
		var thatReorderer = fluid.initView("fluid.reorderer", container, options);
		options = thatReorderer.options;
		var dropManager = fluid.dropManager();
		thatReorderer.layoutHandler = fluid.initSubcomponent(thatReorderer, "layoutHandler", [thatReorderer.container, options, dropManager, thatReorderer.dom]);
		thatReorderer.activeItem = undefined;
		adaptKeysets(options);
		var kbDropWarning = thatReorderer.locate("dropWarning");
		var mouseDropWarning;
		if (kbDropWarning) {
			mouseDropWarning = kbDropWarning.clone()
		}
		var isMove = function (evt) {
			var keysets = options.keysets;
			for (var i = 0; i < keysets.length; i++) {
				if (keysets[i].modifier(evt)) {
					return true
				}
			}
			return false
		};
		var isActiveItemMovable = function () {
			return $.inArray(thatReorderer.activeItem, thatReorderer.dom.fastLocate("movables")) >= 0
		};
		var setDropEffects = function (value) {
			thatReorderer.dom.fastLocate("dropTargets").attr("aria-dropeffect", value)
		};
		var styles = options.styles;
		var noModifier = function (evt) {
			return (!evt.ctrlKey && !evt.altKey && !evt.shiftKey && !evt.metaKey)
		};
		var handleDirectionKeyDown = function (evt) {
			var item = thatReorderer.activeItem;
			if (!item) {
				return true
			}
			var keysets = options.keysets;
			for (var i = 0; i < keysets.length; i++) {
				var keyset = keysets[i];
				var keydir = fluid.keyForValue(keyset, evt.keyCode);
				if (!keydir) {
					continue
				}
				var isMovement = keyset.modifier(evt);
				var dirnum = fluid.keycodeDirection[keydir];
				var relativeItem = thatReorderer.layoutHandler.getRelativePosition(item, dirnum, !isMovement);
				if (!relativeItem) {
					continue
				}
				if (isMovement) {
					var prevent = thatReorderer.events.onBeginMove.fire(item);
					if (prevent === false) {
						return false
					}
					if (kbDropWarning.length > 0) {
						if (relativeItem.clazz === "locked") {
							thatReorderer.events.onShowKeyboardDropWarning.fire(item, kbDropWarning);
							kbDropWarning.show()
						} else {
							kbDropWarning.hide()
						}
					}
					if (relativeItem.element) {
						thatReorderer.requestMovement(relativeItem, item)
					}
				} else {
					if (noModifier(evt)) {
						item.blur();
						$(relativeItem.element).focus()
					}
				}
				return false
			}
			return true
		};
		thatReorderer.handleKeyDown = function (evt) {
			if (!thatReorderer.activeItem || thatReorderer.activeItem !== evt.target) {
				return true
			}
			var jActiveItem = $(thatReorderer.activeItem);
			if (!jActiveItem.hasClass(styles.dragging) && isMove(evt)) {
				if (isActiveItemMovable()) {
					jActiveItem.removeClass(styles.selected);
					jActiveItem.addClass(styles.dragging);
					jActiveItem.attr("aria-grabbed", "true");
					setDropEffects("move")
				}
				return false
			}
			return handleDirectionKeyDown(evt)
		};
		thatReorderer.handleKeyUp = function (evt) {
			if (!thatReorderer.activeItem || thatReorderer.activeItem !== evt.target) {
				return true
			}
			var jActiveItem = $(thatReorderer.activeItem);
			if (jActiveItem.hasClass(styles.dragging) && !isMove(evt)) {
				if (kbDropWarning) {
					kbDropWarning.hide()
				}
				jActiveItem.removeClass(styles.dragging);
				jActiveItem.addClass(styles.selected);
				jActiveItem.attr("aria-grabbed", "false");
				setDropEffects("none");
				return false
			}
			return false
		};
		var dropMarker;
		var createDropMarker = function (tagName) {
			var dropMarker = $(document.createElement(tagName));
			dropMarker.addClass(options.styles.dropMarker);
			dropMarker.hide();
			return dropMarker
		};
		thatReorderer.requestMovement = function (requestedPosition, item) {
			item = fluid.unwrap(item);
			if (!requestedPosition || fluid.unwrap(requestedPosition.element) === item) {
				return
			}
			var activeItem = $(thatReorderer.activeItem);
			activeItem.unbind("blur.fluid.reorderer");
			var prevent = thatReorderer.events.onMove.fire(item, requestedPosition);

			if (prevent === false) {
				return false
			}

			dropManager.geometricMove(item, requestedPosition.element, requestedPosition.position);
			activeItem.focus();
			thatReorderer.refresh();
			dropManager.updateGeometry(thatReorderer.layoutHandler.getGeometricInfo());
			thatReorderer.events.afterMove.fire(item, requestedPosition, thatReorderer.dom.fastLocate("movables"))
		};
		var hoverStyleHandler = function (item, state) {
			thatReorderer.dom.fastLocate("grabHandle", item)[state ? "addClass" : "removeClass"](styles.hover)
		};
		function initMovable(item) {
			var styles = options.styles;
			item.attr("aria-grabbed", "false");
			item.mouseover(function () {
				thatReorderer.events.onHover.fire(item, true)
			});
			item.mouseout(function () {
				thatReorderer.events.onHover.fire(item, false)
			});
			var avatar;
			thatReorderer.dom.fastLocate("grabHandle", item).draggable({
				refreshPositions : false,
				scroll : true,
				helper : function () {
					var dropWarningEl;
					if (mouseDropWarning) {
						dropWarningEl = mouseDropWarning[0]
					}
					avatar = $(options.avatarCreator(item[0], styles.avatar, dropWarningEl));
					avatar.prop("id", createAvatarId(thatReorderer.container.id));
					return avatar
				},
				start : function (e, ui) {
					var prevent = thatReorderer.events.onBeginMove.fire(item);
					if (prevent === false) {
						return false
					}
					var handle = thatReorderer.dom.fastLocate("grabHandle", item)[0];
					var handlePos = fluid.dom.computeAbsolutePosition(handle);
					var handleWidth = handle.offsetWidth;
					var handleHeight = handle.offsetHeight;
					item.focus();
					item.removeClass(options.styles.selected);
					item.addClass(options.styles.mouseDrag);
					item.attr("aria-grabbed", "true");
					setDropEffects("move");
					dropManager.startDrag(e, handlePos, handleWidth, handleHeight);
					avatar.show()
				},
				stop : function (e, ui) {
					item.removeClass(options.styles.mouseDrag);
					item.addClass(options.styles.selected);
					$(thatReorderer.activeItem).attr("aria-grabbed", "false");
					var markerNode = fluid.unwrap(dropMarker);
					if (markerNode.parentNode) {
						markerNode.parentNode.removeChild(markerNode)
					}
					avatar.hide();
					ui.helper = null;
					setDropEffects("none");
					dropManager.endDrag();
					thatReorderer.requestMovement(dropManager.lastPosition(), item);
					thatReorderer.activeItem.focus()
				},
				handle : thatReorderer.dom.fastLocate("grabHandle", item)
			})
		}
		function changeSelectedToDefault(jItem, styles) {
			jItem.removeClass(styles.selected);
			jItem.removeClass(styles.dragging);
			jItem.addClass(styles.defaultStyle);
			jItem.attr("aria-selected", "false")
		}
		var selectItem = function (anItem) {
			thatReorderer.events.onSelect.fire(anItem);
			var styles = options.styles;
			if (thatReorderer.activeItem && thatReorderer.activeItem !== anItem) {
				changeSelectedToDefault($(thatReorderer.activeItem), styles)
			}
			thatReorderer.activeItem = anItem;
			var jItem = $(anItem);
			jItem.removeClass(styles.defaultStyle);
			jItem.addClass(styles.selected);
			jItem.attr("aria-selected", "true")
		};
		var initSelectables = function () {
			var handleBlur = function (evt) {
				changeSelectedToDefault($(this), options.styles);
				return evt.stopPropagation()
			};
			var handleFocus = function (evt) {
				selectItem(this);
				return evt.stopPropagation()
			};
			var selectables = thatReorderer.dom.fastLocate("selectables");
			for (var i = 0; i < selectables.length; ++i) {
				var selectable = $(selectables[i]);
				if (!$.data(selectable[0], "fluid.reorderer.selectable-initialised")) {
					selectable.addClass(styles.defaultStyle);
					selectable.bind("blur.fluid.reorderer", handleBlur);
					selectable.focus(handleFocus);
					selectable.click(function (evt) {
						var handle = fluid.unwrap(thatReorderer.dom.fastLocate("grabHandle", this));
						if (fluid.dom.isContainer(handle, evt.target)) {
							$(this).focus()
						}
					});
					selectable.attr("role", options.containerRole.item);
					selectable.attr("aria-selected", "false");
					selectable.attr("aria-disabled", "false");
					$.data(selectable[0], "fluid.reorderer.selectable-initialised", true)
				}
			}
			if (!thatReorderer.selectableContext) {
				thatReorderer.selectableContext = fluid.selectable(thatReorderer.container, {
						selectableElements : selectables,
						selectablesTabindex : thatReorderer.options.selectablesTabindex,
						direction : null
					})
			}
		};
		var dropChangeListener = function (dropTarget) {
			fluid.moveDom(dropMarker, dropTarget.element, dropTarget.position);
			dropMarker.css("display", "");
			if (mouseDropWarning) {
				if (dropTarget.lockedelem) {
					mouseDropWarning.show()
				} else {
					mouseDropWarning.hide()
				}
			}
		};
		var initItems = function () {
			var movables = thatReorderer.dom.fastLocate("movables");
			var dropTargets = thatReorderer.dom.fastLocate("dropTargets");
			initSelectables();
			for (var i = 0; i < movables.length; i++) {
				var item = movables[i];
				if (!$.data(item, "fluid.reorderer.movable-initialised")) {
					initMovable($(item));
					$.data(item, "fluid.reorderer.movable-initialised", true)
				}
			}
			if (movables.length > 0 && !dropMarker) {
				dropMarker = createDropMarker(movables[0].tagName)
			}
			dropManager.updateGeometry(thatReorderer.layoutHandler.getGeometricInfo());
			dropManager.dropChangeFirer.addListener(dropChangeListener, "fluid.Reorderer");
			dropTargets.attr("aria-dropeffect", "none")
		};
		if (thatReorderer.container) {
			bindHandlersToContainer(thatReorderer.container, thatReorderer.handleKeyDown, thatReorderer.handleKeyUp);
			addRolesToContainer(thatReorderer);
			fluid.tabbable(thatReorderer.container);
			initItems()
		}
		if (options.afterMoveCallbackUrl) {
			thatReorderer.events.afterMove.addListener(function () {
				var layoutHandler = thatReorderer.layoutHandler;
				var model = layoutHandler.getModel ? layoutHandler.getModel() : options.acquireModel(thatReorderer);
				$.post(options.afterMoveCallbackUrl, JSON.stringify(model))
			}, "postModel")
		}
		thatReorderer.events.onHover.addListener(hoverStyleHandler, "style");
		thatReorderer.refresh = function () {
			thatReorderer.dom.refresh("movables");
			thatReorderer.dom.refresh("selectables");
			thatReorderer.dom.refresh("grabHandle", thatReorderer.dom.fastLocate("movables"));
			thatReorderer.dom.refresh("stylisticOffset", thatReorderer.dom.fastLocate("movables"));
			thatReorderer.dom.refresh("dropTargets");
			thatReorderer.events.onRefresh.fire();
			initItems();
			thatReorderer.selectableContext.selectables = thatReorderer.dom.fastLocate("selectables");
			thatReorderer.selectableContext.selectablesUpdated(thatReorderer.activeItem)
		};
		fluid.initDependents(thatReorderer);
		thatReorderer.refresh();
		return thatReorderer
	};
	fluid.reorderer.keys = {
		TAB : 9,
		ENTER : 13,
		SHIFT : 16,
		CTRL : 17,
		ALT : 18,
		META : 19,
		SPACE : 32,
		LEFT : 37,
		UP : 38,
		RIGHT : 39,
		DOWN : 40,
		i : 73,
		j : 74,
		k : 75,
		m : 77
	};
	fluid.reorderer.defaultKeysets = [{
			modifier : function (evt) {
				return evt.ctrlKey
			},
			up : fluid.reorderer.keys.UP,
			down : fluid.reorderer.keys.DOWN,
			right : fluid.reorderer.keys.RIGHT,
			left : fluid.reorderer.keys.LEFT
		}, {
			modifier : function (evt) {
				return evt.ctrlKey
			},
			up : fluid.reorderer.keys.i,
			down : fluid.reorderer.keys.m,
			right : fluid.reorderer.keys.k,
			left : fluid.reorderer.keys.j
		}
	];
	fluid.reorderer.roles = {
		GRID : {
			container : "grid",
			item : "gridcell"
		},
		LIST : {
			container : "list",
			item : "listitem"
		},
		REGIONS : {
			container : "main",
			item : "article"
		}
	};
	var simpleInit = function (container, layoutHandler, options) {
		options = options || {};
		options.layoutHandler = layoutHandler;
		return fluid.reorderer(container, options)
	};
	fluid.reorderList = function (container, options) {
		return simpleInit(container, "fluid.listLayoutHandler", options)
	};
	fluid.reorderGrid = function (container, options) {
		return simpleInit(container, "fluid.gridLayoutHandler", options)
	};
	fluid.reorderer.SHUFFLE_GEOMETRIC_STRATEGY = "shuffleProjectFrom";
	fluid.reorderer.GEOMETRIC_STRATEGY = "projectFrom";
	fluid.reorderer.LOGICAL_STRATEGY = "logicalFrom";
	fluid.reorderer.WRAP_LOCKED_STRATEGY = "lockedWrapFrom";
	fluid.reorderer.NO_STRATEGY = null;
	fluid.reorderer.relativeInfoGetter = function (orientation, coStrategy, contraStrategy, dropManager, dom, disableWrap) {
		return function (item, direction, forSelection) {
			var dirorient = fluid.directionOrientation(direction);
			var strategy = dirorient === orientation ? coStrategy : contraStrategy;
			return strategy !== null ? dropManager[strategy](item, direction, forSelection, disableWrap) : null
		}
	};
	fluid.defaults("fluid.reorderer", {
		styles : {
			defaultStyle : "fl-reorderer-movable-default",
			selected : "fl-reorderer-movable-selected",
			dragging : "fl-reorderer-movable-dragging",
			mouseDrag : "fl-reorderer-movable-dragging",
			hover : "fl-reorderer-movable-hover",
			dropMarker : "fl-reorderer-dropMarker",
			avatar : "fl-reorderer-avatar"
		},
		selectors : {
			dropWarning : ".flc-reorderer-dropWarning",
			movables : ".flc-reorderer-movable",
			grabHandle : "",
			stylisticOffset : ""
		},
		avatarCreator : defaultAvatarCreator,
		keysets : fluid.reorderer.defaultKeysets,
		layoutHandler : {
			type : "fluid.listLayoutHandler"
		},
		events : {
			onShowKeyboardDropWarning : null,
			onSelect : null,
			onBeginMove : "preventable",
			onMove : "preventable",
			afterMove : null,
			onHover : null,
			onRefresh : null
		},
		mergePolicy : {
			keysets : "replace",
			"selectors.labelSource" : "selectors.grabHandle",
			"selectors.selectables" : "selectors.movables",
			"selectors.dropTargets" : "selectors.movables"
		},
		components : {
			labeller : {
				type : "fluid.reorderer.labeller",
				options : {
					dom : "{reorderer}.dom",
					getGeometricInfo : "{reorderer}.layoutHandler.getGeometricInfo",
					orientation : "{reorderer}.options.orientation",
					layoutType : "{reorderer}.options.layoutHandler"
				}
			}
		},
		disableWrap : false
	});
	fluid.reorderer.makeGeometricInfoGetter = function (orientation, sentinelize, dom) {
		return function () {
			var that = {
				sentinelize : sentinelize,
				extents : [{
						orientation : orientation,
						elements : dom.fastLocate("dropTargets")
					}
				],
				elementMapper : function (element) {
					return $.inArray(element, dom.fastLocate("movables")) === -1 ? "locked" : null
				},
				elementIndexer : function (element) {
					var selectables = dom.fastLocate("selectables");
					return {
						elementClass : that.elementMapper(element),
						index : $.inArray(element, selectables),
						length : selectables.length
					}
				}
			};
			return that
		}
	};
	fluid.defaults(true, "fluid.listLayoutHandler", {
		orientation : fluid.orientation.VERTICAL,
		containerRole : fluid.reorderer.roles.LIST,
		selectablesTabindex : -1,
		sentinelize : true
	});
	fluid.listLayoutHandler = function (container, options, dropManager, dom) {
		var that = {};
		that.getRelativePosition = fluid.reorderer.relativeInfoGetter(options.orientation, fluid.reorderer.LOGICAL_STRATEGY, null, dropManager, dom, options.disableWrap);
		that.getGeometricInfo = fluid.reorderer.makeGeometricInfoGetter(options.orientation, options.sentinelize, dom);
		return that
	};
	fluid.defaults(true, "fluid.gridLayoutHandler", {
		orientation : fluid.orientation.HORIZONTAL,
		containerRole : fluid.reorderer.roles.GRID,
		selectablesTabindex : -1,
		sentinelize : false
	});
	fluid.gridLayoutHandler = function (container, options, dropManager, dom) {
		var that = {};
		that.getRelativePosition = fluid.reorderer.relativeInfoGetter(options.orientation, options.disableWrap ? fluid.reorderer.SHUFFLE_GEOMETRIC_STRATEGY : fluid.reorderer.LOGICAL_STRATEGY, fluid.reorderer.SHUFFLE_GEOMETRIC_STRATEGY, dropManager, dom, options.disableWrap);
		that.getGeometricInfo = fluid.reorderer.makeGeometricInfoGetter(options.orientation, options.sentinelize, dom);
		return that
	};
	fluid.defaults("fluid.reorderer.labeller", {
		strings : {
			overallTemplate : "%recentStatus %item %position %movable",
			position : "%index of %length",
			position_moduleLayoutHandler : "%index of %length in %moduleCell %moduleIndex of %moduleLength",
			moduleCell_0 : "row",
			moduleCell_1 : "column",
			movable : "movable",
			fixed : "fixed",
			recentStatus : "moved from position %position"
		},
		components : {
			resolver : {
				type : "fluid.messageResolver",
				options : {
					messageBase : "{labeller}.options.strings"
				}
			}
		},
		invokers : {
			renderLabel : {
				funcName : "fluid.reorderer.labeller.renderLabel",
				args : ["{labeller}", "@0", "@1"]
			}
		}
	});
	fluid.reorderer.indexRebaser = function (indices) {
		indices.index++;
		if (indices.moduleIndex !== undefined) {
			indices.moduleIndex++
		}
		return indices
	};
	fluid.reorderer.labeller = function (options) {
		var that = fluid.initLittleComponent("fluid.reorderer.labeller", options);
		fluid.initDependents(that);
		that.dom = that.options.dom;
		that.moduleCell = that.resolver.resolve("moduleCell_" + that.options.orientation);
		var layoutType = fluid.computeNickName(that.options.layoutType);
		that.positionTemplate = that.resolver.lookup(["position_" + layoutType, "position"]);
		var movedMap = {};
		that.returnedOptions = {
			listeners : {
				onRefresh : function () {
					var selectables = that.dom.locate("selectables");
					fluid.each(selectables, function (selectable) {
						var labelOptions = {};
						var id = fluid.allocateSimpleId(selectable);
						var moved = movedMap[id];
						var label = that.renderLabel(selectable);
						var plainLabel = label;
						if (moved) {
							moved.newRender = plainLabel;
							label = that.renderLabel(selectable, moved.oldRender.position);
							$(selectable).one("focusout", function () {
								if (movedMap[id]) {
									var oldLabel = movedMap[id].newRender.label;
									delete movedMap[id];
									fluid.updateAriaLabel(selectable, oldLabel)
								}
							});
							labelOptions.dynamicLabel = true
						}
						fluid.updateAriaLabel(selectable, label.label, labelOptions)
					})
				},
				onMove : function (item, newPosition) {
					fluid.clear(movedMap);
					var movingId = fluid.allocateSimpleId(item);
					movedMap[movingId] = {
						oldRender : that.renderLabel(item)
					}
				}
			}
		};
		return that
	};
	fluid.reorderer.labeller.renderLabel = function (that, selectable, recentPosition) {
		var geom = that.options.getGeometricInfo();
		var indices = fluid.reorderer.indexRebaser(geom.elementIndexer(selectable));
		indices.moduleCell = that.moduleCell;
		var elementClass = geom.elementMapper(selectable);
		var labelSource = that.dom.locate("labelSource", selectable);
		var recentStatus;
		if (recentPosition) {
			recentStatus = that.resolver.resolve("recentStatus", {
					position : recentPosition
				})
		}
		var topModel = {
			item : typeof(labelSource) === "string" ? labelSource : fluid.dom.getElementText(fluid.unwrap(labelSource)),
			position : that.positionTemplate.resolveFunc(that.positionTemplate.template, indices),
			movable : that.resolver.resolve(elementClass === "locked" ? "fixed" : "movable"),
			recentStatus : recentStatus || ""
		};
		var template = that.resolver.lookup(["overallTemplate"]);
		var label = template.resolveFunc(template.template, topModel);
		return {
			position : topModel.position,
			label : label
		}
	}
})(jQuery, fluid_1_4);
var fluid_1_4 = fluid_1_4 || {};
(function ($, fluid) {
	var deriveLightboxCellBase = function (namebase, index) {
		return namebase + "lightbox-cell:" + index + ":"
	};
	var addThumbnailActivateHandler = function (container) {
		var enterKeyHandler = function (evt) {
			if (evt.which === fluid.reorderer.keys.ENTER) {
				var thumbnailAnchors = $("a", evt.target);
				document.location = thumbnailAnchors.attr("href")
			}
		};
		container.keypress(enterKeyHandler)
	};
	var seekNodesById = function (rootnode, tagname, idmatch) {
		var inputs = rootnode.getElementsByTagName(tagname);
		var togo = [];
		for (var i = 0; i < inputs.length; i += 1) {
			var input = inputs[i];
			var id = input.id;
			if (id && id.match(idmatch)) {
				togo.push(input)
			}
		}
		return togo
	};
	var createImageCellFinder = function (parentNode, containerId) {
		parentNode = fluid.unwrap(parentNode);
		var lightboxCellNamePattern = "^" + deriveLightboxCellBase(containerId, "[0-9]+") + "$";
		return function () {
			return seekNodesById(parentNode, "div", lightboxCellNamePattern)
		}
	};
	var seekForm = function (container) {
		return fluid.findAncestor(container, function (element) {
			return $(element).is("form")
		})
	};
	var seekInputs = function (container, reorderform) {
		return seekNodesById(reorderform, "input", "^" + deriveLightboxCellBase(container.prop("id"), "[^:]*") + "reorder-index$")
	};
	var mapIdsToNames = function (container, reorderform) {
		var inputs = seekInputs(container, reorderform);
		for (var i = 0; i < inputs.length; i++) {
			var input = inputs[i];
			var name = input.name;
			input.name = name || input.id
		}
	};
	var createIDAfterMoveListener = function (container) {
		var reorderform = seekForm(container);
		mapIdsToNames(container, reorderform);
		return function () {
			var inputs,
			i;
			inputs = seekInputs(container, reorderform);
			for (i = 0; i < inputs.length; i += 1) {
				inputs[i].value = i
			}
			if (reorderform && reorderform.action) {
				var order = $(reorderform).serialize();
				$.post(reorderform.action, order, function (type, data, evt) {})
			}
		}
	};
	var setDefaultValue = function (target, path, value) {
		var previousValue = fluid.get(target, path);
		var valueToSet = previousValue || value;
		fluid.set(target, path, valueToSet)
	};
	fluid.reorderImages = function (container, options) {
		var defaults = fluid.defaults("fluid.reorderImages");
		var mergedOptions = fluid.merge(defaults.mergePolicy, {}, defaults, options);
		container = fluid.container(container);
		setDefaultValue(mergedOptions, "listeners.afterMove", mergedOptions.afterMoveCallback || createIDAfterMoveListener(container));
		setDefaultValue(mergedOptions, "selectors.movables", createImageCellFinder(container, container.prop("id")));
		var reorderer = fluid.reorderer(container, mergedOptions);
		fluid.tabindex($("a", container), -1);
		addThumbnailActivateHandler(container);
		return reorderer
	};
	fluid.lightbox = fluid.reorderImages;
	fluid.defaults("fluid.reorderImages", {
		layoutHandler : "fluid.gridLayoutHandler",
		selectors : {
			labelSource : ".flc-reorderer-imageTitle"
		}
	})
})(jQuery, fluid_1_4);
var fluid_1_4 = fluid_1_4 || {};
(function ($, fluid) {
	fluid.registerNamespace("fluid.moduleLayout");
	fluid.moduleLayout.findColumnAndItemIndices = function (item, layout) {
		return fluid.find(layout.columns, function (column, colIndex) {
			var index = $.inArray(item, column.elements);
			return index === -1 ? undefined : {
				columnIndex : colIndex,
				itemIndex : index
			}
		}, {
			columnIndex : -1,
			itemIndex : -1
		})
	};
	fluid.moduleLayout.findColIndex = function (item, layout) {
		return fluid.find(layout.columns, function (column, colIndex) {
			return item === column.container ? colIndex : undefined
		}, -1)
	};
	fluid.moduleLayout.updateLayout = function (item, target, position, layout) {
		item = fluid.unwrap(item);
		target = fluid.unwrap(target);
		var itemIndices = fluid.moduleLayout.findColumnAndItemIndices(item, layout);
		layout.columns[itemIndices.columnIndex].elements.splice(itemIndices.itemIndex, 1);
		var targetCol;
		if (position === fluid.position.INSIDE) {
			targetCol = layout.columns[fluid.moduleLayout.findColIndex(target, layout)].elements;
			targetCol.splice(targetCol.length, 0, item)
		} else {
			var relativeItemIndices = fluid.moduleLayout.findColumnAndItemIndices(target, layout);
			targetCol = layout.columns[relativeItemIndices.columnIndex].elements;
			position = fluid.normalisePosition(position, itemIndices.columnIndex === relativeItemIndices.columnIndex, relativeItemIndices.itemIndex, itemIndices.itemIndex);
			var relative = position === fluid.position.BEFORE ? 0 : 1;
			targetCol.splice(relativeItemIndices.itemIndex + relative, 0, item)
		}
	};
	fluid.moduleLayout.layoutFromFlat = function (container, columns, portlets) {
		var layout = {};
		layout.container = container;
		layout.columns = fluid.transform(columns, function (column) {
				return {
					container : column,
					elements : $.makeArray(portlets.filter(function () {
							return fluid.dom.isContainer(column, this)
						}))
				}
			});
		return layout
	};
	fluid.moduleLayout.layoutFromIds = function (idLayout) {
		return {
			container : fluid.byId(idLayout.id),
			columns : fluid.transform(idLayout.columns, function (column) {
				return {
					container : fluid.byId(column.id),
					elements : fluid.transform(column.children, fluid.byId)
				}
			})
		}
	};
	fluid.moduleLayout.layoutToIds = function (idLayout) {
		return {
			id : fluid.getId(idLayout.container),
			columns : fluid.transform(idLayout.columns, function (column) {
				return {
					id : fluid.getId(column.container),
					children : fluid.transform(column.elements, fluid.getId)
				}
			})
		}
	};
	var defaultOnShowKeyboardDropWarning = function (item, dropWarning) {
		if (dropWarning) {
			var offset = $(item).offset();
			dropWarning = $(dropWarning);
			dropWarning.css("position", "absolute");
			dropWarning.css("top", offset.top);
			dropWarning.css("left", offset.left)
		}
	};
	fluid.defaults(true, "fluid.moduleLayoutHandler", {
		orientation : fluid.orientation.VERTICAL,
		containerRole : fluid.reorderer.roles.REGIONS,
		selectablesTabindex : -1,
		sentinelize : true
	});
	fluid.moduleLayoutHandler = function (container, options, dropManager, dom) {
		var that = {};
		function computeLayout() {
			var togo;
			if (options.selectors.modules) {
				togo = fluid.moduleLayout.layoutFromFlat(container, dom.locate("columns"), dom.locate("modules"))
			}
			if (!togo) {
				var idLayout = fluid.get(options, "moduleLayout.layout");
				fluid.moduleLayout.layoutFromIds(idLayout)
			}
			return togo
		}
		var layout = computeLayout();
		that.layout = layout;
		function isLocked(item) {
			var lockedModules = options.selectors.lockedModules ? dom.fastLocate("lockedModules") : [];
			return $.inArray(item, lockedModules) !== -1
		}
		that.getRelativePosition = fluid.reorderer.relativeInfoGetter(options.orientation, fluid.reorderer.WRAP_LOCKED_STRATEGY, fluid.reorderer.GEOMETRIC_STRATEGY, dropManager, dom, options.disableWrap);
		that.getGeometricInfo = function () {
			var extents = [];
			var togo = {
				extents : extents,
				sentinelize : options.sentinelize
			};
			togo.elementMapper = function (element) {
				return isLocked(element) ? "locked" : null
			};
			togo.elementIndexer = function (element) {
				var indices = fluid.moduleLayout.findColumnAndItemIndices(element, that.layout);
				return {
					index : indices.itemIndex,
					length : layout.columns[indices.columnIndex].elements.length,
					moduleIndex : indices.columnIndex,
					moduleLength : layout.columns.length
				}
			};
			for (var col = 0; col < layout.columns.length; col++) {
				var column = layout.columns[col];
				var thisEls = {
					orientation : options.orientation,
					elements : $.makeArray(column.elements),
					parentElement : column.container
				};
				extents.push(thisEls)
			}
			return togo
		};
		function computeModules(all) {
			return function () {
				var modules = fluid.accumulate(layout.columns, function (column, list) {
						return list.concat(column.elements)
					}, []);
				if (!all) {
					fluid.remove_if(modules, isLocked)
				}
				return modules
			}
		}
		that.returnedOptions = {
			selectors : {
				movables : computeModules(false),
				dropTargets : computeModules(false),
				selectables : computeModules(true)
			},
			listeners : {
				onMove : {
					priority : "last",
					listener : function (item, requestedPosition) {
						fluid.moduleLayout.updateLayout(item, requestedPosition.element, requestedPosition.position, layout)
					}
				},
				onRefresh : function () {
					layout = computeLayout();
					that.layout = layout
				},
				"onShowKeyboardDropWarning.setPosition" : defaultOnShowKeyboardDropWarning
			}
		};
		that.getModel = function () {
			return fluid.moduleLayout.layoutToIds(layout)
		};
		return that
	}
})(jQuery, fluid_1_4);
var fluid_1_4 = fluid_1_4 || {};
(function ($, fluid) {
	fluid.reorderLayout = function (container, userOptions) {
		var assembleOptions = {
			layoutHandler : "fluid.moduleLayoutHandler",
			selectors : {
				columns : ".flc-reorderer-column",
				modules : ".flc-reorderer-module"
			}
		};
		var options = $.extend(true, assembleOptions, userOptions);
		return fluid.reorderer(container, options)
	}
})(jQuery, fluid_1_4);
/*
 * jQuery UI AriaTabs (12.07.10)
 * http://github.com/fnagel/jQuery-Accessible-RIA
 *
 * Copyright (c) 2009 Felix Nagel for Namics (Deustchland) GmbH
 * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
 *
 * Depends: ui.core.js 1.8
 *   		ui.tabs.js
 */
(function ($) {
	$.fn.extend($.ui.tabs.prototype, {
		_create : function () {
			var self = this,
			options = this.options;
			if ($.address) {
				var jqAddressDefOpt = {
					enable : true,
					title : {
						enable : true,
						split : " | "
					}
				};
				if (!$.isEmptyObject(options.jqAddress)) {
					$.extend(true, jqAddressDefOpt, options.jqAddress)
				} else {
					options.jqAddress = {}
					
				}
				$.extend(true, options.jqAddress, jqAddressDefOpt)
			}
			if ($.address && options.jqAddress.enable) {
				var anchorId = "#" + $.address.value().replace("/", "")
			}
			self._tabify(true);
			this.anchors.bind(options.event + ".tabs-accessibility", function () {
				this.focus()
			});
			self.list.attr("role", "tablist");
			for (var x = 0; x < self.anchors.length; x++) {
				if ($.address && options.jqAddress.enable && anchorId != "#" && $(self.anchors[x]).attr("href") == anchorId) {
					self.select(x)
				}
				self._ariaInit(x)
			}
			self.list.keydown(function (event) {
				switch (event.keyCode) {
				case $.ui.keyCode.RIGHT:
					self.select(options.selected + 1);
					return false;
					break;
				case $.ui.keyCode.DOWN:
					self.select(options.selected + 1);
					break;
				case $.ui.keyCode.UP:
					self.select(options.selected - 1);
					return false;
					break;
				case $.ui.keyCode.LEFT:
					self.select(options.selected - 1);
					return false;
					break;
				case $.ui.keyCode.END:
					self.select(self.anchors.length - 1);
					return false;
					break;
				case $.ui.keyCode.HOME:
					self.select(0);
					return false;
					break
				}
			});
			if ($.address && this.options.jqAddress.enable) {
				$.address.externalChange(function (event) {
					var anchorId = "#" + event.value.replace("/", "");
					var x = 0;
					while (x < self.anchors.length) {
						if ($(self.anchors[x]).attr("href") == anchorId) {
							self.select(x);
							return
						}
						x++
					}
				})
			}
		},
		_original_load : $.ui.tabs.prototype.load,
		load : function (index) {
			if ($.address && this.options.jqAddress.enable) {
				if ($(this.anchors[0]).attr("aria-selected") !== undefined) {
					if (this.options.forceFirst === 0 && index !== 0) {
						if ($.address.value() == "") {
							$.address.history(false)
						}
						$.address.value($(this.anchors[0]).attr("href").replace(/^#/, ""));
						$.address.history(true);
						this.options.forceFirst = false
					}
					if (this.options.jqAddress.title.enable) {
						$.address.title($.address.title().split(this.options.jqAddress.title.split)[0] + this.options.jqAddress.title.split + $(this.anchors[index]).text())
					}
					$.address.value($(this.anchors[index]).attr("href").replace(/^#/, ""))
				} else {
					this.options.forceFirst = index
				}
			}
			for (var x = 0; x < this.anchors.length; x++) {
				this._ariaSet(x, false);
				if ($.data(this.anchors[x], "href.tabs")) {
					$(this.panels[x]).removeAttr("aria-live").removeAttr("aria-busy")
				}
			}
			if ($.data(this.anchors[index], "href.tabs")) {
				$(this.panels[index]).attr("aria-live", "polite").attr("aria-busy", "true")
			}
			this._original_load(index);
			if ($.data(this.anchors[index], "href.tabs")) {
				$(this.panels[index]).attr("aria-busy", "false")
			}
			this._ariaSet(index, true)
		},
		_ariaSet : function (index, state) {
			var tabindex = (state) ? 0 : -1;
			var anchor = $(this.anchors[index]);
			anchor.attr("tabindex", tabindex).attr("aria-selected", state);
			if (state) {
				if (!$.browser.msie) {
					anchor.focus()
				}
			} else {
				anchor.closest("li").removeClass("ui-state-focus")
			}
			$(this.panels[index]).attr("aria-hidden", !state).attr("aria-expanded", state);
			if ($.browser.msie) {
				this.options.timeout = window.setTimeout(function () {
						anchor.focus()
					}, 100)
			}
			if (state) {
				this._updateVirtualBuffer()
			}
		},
		_ariaInit : function (index) {
			var self = this;
			var panelId = $(this.panels[index]).attr("id");
			$(this.anchors[index]).attr("role", "tab").attr("aria-controls", panelId).attr("id", panelId + "-tab").parent().attr("role", "presentation");
			$(this.panels[index]).attr("role", "tabpanel").attr("tabindex", 0).attr("aria-labelledby", panelId + "-tab");
			if (this.options.collapsible) {
				$(this.anchors[index]).bind(this.options.event, function (event) {
					self._ariaSet(index, !$(self.panels[index]).hasClass("ui-tabs-hide"))
				})
			}
		},
		_original_add : $.ui.tabs.prototype.add,
		add : function (url, label, index) {
			this._original_add(url, label, index);
			this.element.attr("aria-live", "polite").attr("aria-relevant", "additions");
			if (index) {
				this._ariaInit(index);
				this._ariaSet(index, false)
			} else {
				this._ariaInit(this.anchors.length - 1);
				this._ariaSet(this.anchors.length - 1, false)
			}
		},
		_original_remove : $.ui.tabs.prototype.remove,
		remove : function (index) {
			this._original_remove(index);
			this.element.attr("aria-live", "polite").attr("aria-relevant", "removals")
		},
		_original_destroy : $.ui.tabs.prototype.destroy,
		destroy : function () {
			var self = this,
			options = this.options;
			self.element.removeAttr("role").removeAttr("aria-live").removeAttr("aria-relevant");
			self.list.removeAttr("role");
			for (var x = 0; x < self.anchors.length; x++) {
				$(self.anchors[x]).removeAttr("aria-selected").removeAttr("aria-controls").removeAttr("role").removeAttr("id").removeAttr("tabindex").parent().removeAttr("role");
				$(self.panels[x]).removeAttr("aria-hidden").removeAttr("aria-expanded").removeAttr("aria-labelledby").removeAttr("aria-live").removeAttr("aria-busy").removeAttr("aria-relevant").removeAttr("role")
			}
			$("body>form #virtualBufferForm").parent().remove();
			this._original_destroy()
		},
		_updateVirtualBuffer : function () {
			var form = $("body>form #virtualBufferForm");
			if (form.length) {
				if (form.val() == "1") {
					form.val("0")
				} else {
					form.val("1")
				}
				if (form.hasClass("ui-accessibility-odd")) {
					form.addClass("ui-accessibility-even").removeClass("ui-accessibility-odd")
				} else {
					form.addClass("ui-accessibility-odd").removeClass("ui-accessibility-even")
				}
			} else {
				$("body").append('<form><input id="virtualBufferForm" type="hidden" value="1" /></form>')
			}
		}
	})
})(jQuery);
var fluid_1_4 = fluid_1_4 || {};
(function ($, fluid) {
	fluid.defaults("fluid.tabs", {
		gradeNames : ["fluid.viewComponent", "autoInit"],
		tabOptions : {},
		events : {
			tabsselect : "preventable",
			tabsload : null,
			tabsshow : null
		},
		finalInitFunction : "fluid.tabs.finalInit"
	});
	fluid.tabs.finalInit = function (that) {
		that.container.tabs(that.options.tabOptions);
		fluid.each(that.options.events, function (value, eventName) {
			that.container.bind(eventName, function (event, ui) {
				return that.events[eventName].fire(that, event, ui)
			})
		})
	}
})(jQuery, fluid_1_4);
var fluid_1_4 = fluid_1_4 || {};
(function ($, fluid) {
	fluid.registerNamespace("fluid.tableOfContents");
	fluid.tableOfContents.insertAnchor = function (name, element) {
		var anchor = $("<a></a>", element.ownerDocument);
		anchor.prop({
			name : name,
			id : name
		});
		anchor.insertBefore(element)
	};
	fluid.tableOfContents.generateGUID = function () {
		return fluid.allocateSimpleId()
	};
	fluid.tableOfContents.filterHeadings = function (headings) {
		return headings.filter(":visible")
	};
	fluid.tableOfContents.finalInit = function (that) {
		var headings = that.filterHeadings(that.locate("headings"));
		that.headingTextToAnchor = function (heading) {
			var guid = that.generateGUID();
			var anchorInfo = {
				id : guid,
				url : "#" + guid
			};
			that.insertAnchor(anchorInfo.id, heading);
			return anchorInfo
		};
		that.anchorInfo = fluid.transform(headings, function (heading) {
				return that.headingTextToAnchor(heading)
			});
		that.hide = function () {
			that.locate("tocContainer").hide()
		};
		that.show = function () {
			that.locate("tocContainer").show()
		};
		that.model = that.modelBuilder.assembleModel(headings, that.anchorInfo);
		that.events.onReady.fire()
	};
	fluid.defaults("fluid.tableOfContents", {
		gradeNames : ["fluid.viewComponent", "autoInit"],
		finalInitFunction : "fluid.tableOfContents.finalInit",
		components : {
			levels : {
				type : "fluid.tableOfContents.levels",
				container : "{tableOfContents}.dom.tocContainer",
				createOnEvent : "onReady",
				options : {
					model : {
						headings : "{tableOfContents}.model"
					},
					events : {
						afterRender : "{tableOfContents}.events.afterRender"
					}
				}
			},
			modelBuilder : {
				type : "fluid.tableOfContents.modelBuilder"
			}
		},
		invokers : {
			insertAnchor : "fluid.tableOfContents.insertAnchor",
			generateGUID : "fluid.tableOfContents.generateGUID",
			filterHeadings : "fluid.tableOfContents.filterHeadings"
		},
		selectors : {
			headings : ":header",
			tocContainer : ".flc-toc-tocContainer"
		},
		events : {
			onReady : null,
			afterRender : null
		}
	});
	fluid.registerNamespace("fluid.tableOfContents.modelBuilder");
	fluid.tableOfContents.modelBuilder.toModel = function (headingInfo, modelLevelFn) {
		var headings = fluid.copy(headingInfo);
		var buildModelLevel = function (headings, level) {
			var modelLevel = [];
			while (headings.length > 0) {
				var heading = headings[0];
				if (heading.level < level) {
					break
				}
				if (heading.level > level) {
					var subHeadings = buildModelLevel(headings, level + 1);
					if (modelLevel.length > 0) {
						modelLevel[modelLevel.length - 1].headings = subHeadings
					} else {
						modelLevel = modelLevelFn(modelLevel, subHeadings)
					}
				}
				if (heading.level === level) {
					modelLevel.push(heading);
					headings.shift()
				}
			}
			return modelLevel
		};
		return buildModelLevel(headings, 1)
	};
	fluid.tableOfContents.modelBuilder.gradualModelLevelFn = function (modelLevel, subHeadings) {
		var subHeadingsClone = fluid.copy(subHeadings);
		subHeadingsClone[0].level--;
		return subHeadingsClone
	};
	fluid.tableOfContents.modelBuilder.skippedModelLevelFn = function (modelLevel, subHeadings) {
		modelLevel.push({
			headings : subHeadings
		});
		return modelLevel
	};
	fluid.tableOfContents.modelBuilder.finalInit = function (that) {
		that.convertToHeadingObjects = function (headings, anchorInfo) {
			headings = $(headings);
			return fluid.transform(headings, function (heading, index) {
				return {
					level : that.headingCalculator.getHeadingLevel(heading),
					text : $(heading).text(),
					url : anchorInfo[index].url
				}
			})
		};
		that.assembleModel = function (headings, anchorInfo) {
			var headingInfo = that.convertToHeadingObjects(headings, anchorInfo);
			return that.toModel(headingInfo)
		}
	};
	fluid.defaults("fluid.tableOfContents.modelBuilder", {
		gradeNames : ["fluid.littleComponent", "autoInit"],
		finalInitFunction : "fluid.tableOfContents.modelBuilder.finalInit",
		components : {
			headingCalculator : {
				type : "fluid.tableOfContents.modelBuilder.headingCalculator"
			}
		},
		invokers : {
			toModel : {
				funcName : "fluid.tableOfContents.modelBuilder.toModel",
				args : ["{arguments}.0", "{modelBuilder}.modelLevelFn"]
			},
			modelLevelFn : "fluid.tableOfContents.modelBuilder.gradualModelLevelFn"
		}
	});
	fluid.registerNamespace("fluid.tableOfContents.modelBuilder.headingCalculator");
	fluid.tableOfContents.modelBuilder.headingCalculator.finalInit = function (that) {
		that.getHeadingLevel = function (heading) {
			return $.inArray(heading.tagName, that.options.levels) + 1
		}
	};
	fluid.defaults("fluid.tableOfContents.modelBuilder.headingCalculator", {
		gradeNames : ["fluid.littleComponent", "autoInit"],
		finalInitFunction : "fluid.tableOfContents.modelBuilder.headingCalculator.finalInit",
		levels : ["H1", "H2", "H3", "H4", "H5", "H6"]
	});
	fluid.registerNamespace("fluid.tableOfContents.levels");
	fluid.tableOfContents.levels.finalInit = function (that) {
		fluid.fetchResources(that.options.resources, function () {
			that.container.append(that.options.resources.template.resourceText);
			that.refreshView()
		})
	};
	fluid.tableOfContents.levels.objModel = function (type, ID) {
		var objModel = {
			ID : type + ID + ":",
			children : []
		};
		return objModel
	};
	fluid.tableOfContents.levels.handleEmptyItemObj = function (itemObj) {
		itemObj.decorators = [{
				type : "addClass",
				classes : "fl-tableOfContents-hide-bullet"
			}
		]
	};
	fluid.tableOfContents.levels.generateTree = function (headingsModel, currentLevel) {
		currentLevel = currentLevel || 0;
		var levelObj = fluid.tableOfContents.levels.objModel("level", currentLevel);
		if (headingsModel.headings.length === 0) {
			return []
		}
		if (currentLevel === 0) {
			var tree = {
				children : [fluid.tableOfContents.levels.generateTree(headingsModel, currentLevel + 1)]
			};
			return tree
		}
		$.each(headingsModel.headings, function (index, model) {
			var itemObj = fluid.tableOfContents.levels.objModel("items", currentLevel);
			var linkObj = {
				ID : "link" + currentLevel,
				target : model.url,
				linktext : model.text
			};
			if (!model.level) {
				fluid.tableOfContents.levels.handleEmptyItemObj(itemObj)
			} else {
				itemObj.children.push(linkObj)
			}
			if (model.headings) {
				itemObj.children.push(fluid.tableOfContents.levels.generateTree(model, currentLevel + 1))
			}
			levelObj.children.push(itemObj)
		});
		return levelObj
	};
	fluid.tableOfContents.levels.produceTree = function (that) {
		return fluid.tableOfContents.levels.generateTree(that.model)
	};
	fluid.defaults("fluid.tableOfContents.levels", {
		gradeNames : ["fluid.rendererComponent", "autoInit"],
		finalInitFunction : "fluid.tableOfContents.levels.finalInit",
		produceTree : "fluid.tableOfContents.levels.produceTree",
		selectors : {
			level1 : ".flc-toc-levels-level1",
			level2 : ".flc-toc-levels-level2",
			level3 : ".flc-toc-levels-level3",
			level4 : ".flc-toc-levels-level4",
			level5 : ".flc-toc-levels-level5",
			level6 : ".flc-toc-levels-level6",
			items1 : ".flc-toc-levels-items1",
			items2 : ".flc-toc-levels-items2",
			items3 : ".flc-toc-levels-items3",
			items4 : ".flc-toc-levels-items4",
			items5 : ".flc-toc-levels-items5",
			items6 : ".flc-toc-levels-items6",
			link1 : ".flc-toc-levels-link1",
			link2 : ".flc-toc-levels-link2",
			link3 : ".flc-toc-levels-link3",
			link4 : ".flc-toc-levels-link4",
			link5 : ".flc-toc-levels-link5",
			link6 : ".flc-toc-levels-link6"
		},
		repeatingSelectors : ["level1", "level2", "level3", "level4", "level5", "level6", "items1", "items2", "items3", "items4", "items5", "items6"],
		model : {
			headings : []
		},
		resources : {
			template : {
				forceCache : true,
				url : "../html/TableOfContents.html"
			}
		},
		rendererFnOptions : {
			noexpand : true
		},
		rendererOptions : {
			debugMode : false
		}
	})
})(jQuery, fluid_1_4);
var fluid_1_4 = fluid_1_4 || {};
(function ($, fluid) {
	fluid.generate = function (n, generator) {
		var togo = [];
		for (var i = 0; i < n; ++i) {
			togo[i] = typeof(generator) === "function" ? generator.call(null, i) : generator
		}
		return togo
	};
	fluid.registerNamespace("fluid.url");
	fluid.url.generateDepth = function (depth) {
		return fluid.generate(depth, "../").join("")
	};
	fluid.url.parsePathInfo = function (pathInfo) {
		var togo = {};
		var segs = pathInfo.split("/");
		if (segs.length > 0) {
			var top = segs.length - 1;
			var dotpos = segs[top].indexOf(".");
			if (dotpos !== -1) {
				togo.extension = segs[top].substring(dotpos + 1);
				segs[top] = segs[top].substring(0, dotpos)
			}
		}
		togo.pathInfo = segs;
		return togo
	};
	fluid.url.parsePathInfoTrim = function (pathInfo) {
		var togo = fluid.url.parsePathInfo(pathInfo);
		if (togo.pathInfo[togo.pathInfo.length - 1] === "") {
			togo.pathInfo.length--
		}
		return togo
	};
	fluid.url.collapseSegs = function (segs, from, to) {
		var togo = "";
		if (from === undefined) {
			from = 0
		}
		if (to === undefined) {
			to = segs.length
		}
		for (var i = from; i < to - 1; ++i) {
			togo += segs[i] + "/"
		}
		if (to > from) {
			togo += segs[to - 1]
		}
		return togo
	};
	fluid.url.makeRelPath = function (parsed, index) {
		var togo = fluid.kettle.collapseSegs(parsed.pathInfo, index);
		if (parsed.extension) {
			togo += "." + parsed.extension
		}
		return togo
	};
	fluid.url.cononocolosePath = function (pathInfo) {
		var consume = 0;
		for (var i = 0; i < pathInfo.length; ++i) {
			if (pathInfo[i] === "..") {
				++consume
			} else {
				if (consume !== 0) {
					pathInfo.splice(i - consume * 2, consume * 2);
					i -= consume * 2;
					consume = 0
				}
			}
		}
		return pathInfo
	};
	fluid.url.parseUri = function (str) {
		var o = fluid.url.parseUri.options,
		m = o.parser[o.strictMode ? "strict" : "loose"].exec(str),
		uri = {},
		i = 14;
		while (i--) {
			uri[o.key[i]] = m[i] || ""
		}
		uri[o.q.name] = {};
		uri[o.key[12]].replace(o.q.parser, function ($0, $1, $2) {
			if ($1) {
				uri[o.q.name][$1] = $2
			}
		});
		return uri
	};
	fluid.url.parseUri.options = {
		strictMode : true,
		key : ["source", "protocol", "authority", "userInfo", "user", "password", "host", "port", "relative", "path", "directory", "file", "query", "anchor"],
		q : {
			name : "queryKey",
			parser : /(?:^|&)([^&=]*)=?([^&]*)/g
		},
		parser : {
			strict : /^(?:([^:\/?#]+):)?(?:\/\/((?:(([^:@]*)(?::([^:@]*))?)?@)?([^:\/?#]*)(?::(\d*))?))?((((?:[^?#\/]*\/)*)([^?#]*))(?:\?([^#]*))?(?:#(.*))?)/,
			loose : /^(?:(?![^:@]+:[^:@\/]*@)([^:\/?#.]+):)?(?:\/\/)?((?:(([^:@]*)(?::([^:@]*))?)?@)?([^:\/?#]*)(?::(\d*))?)(((\/(?:[^?#](?![^?#\/]*\.[^?#\/.]+(?:[?#]|$)))*\/?)?([^?#\/]*))(?:\?([^#]*))?(?:#(.*))?)/
		}
	};
	fluid.url.parseSegs = function (url) {
		var parsed = fluid.url.parseUri(url);
		var parsedSegs = fluid.url.parsePathInfoTrim(parsed.directory);
		return parsedSegs.pathInfo
	};
	fluid.url.isAbsoluteUrl = function (url) {
		var parseRel = fluid.url.parseUri(url);
		return (parseRel.host || parseRel.protocol || parseRel.directory.charAt(0) === "/")
	};
	fluid.url.computeRelativePrefix = function (outerLocation, iframeLocation, relPath) {
		if (fluid.url.isAbsoluteUrl(relPath)) {
			return relPath
		}
		var relSegs = fluid.url.parsePathInfo(relPath).pathInfo;
		var parsedOuter = fluid.url.parseSegs(outerLocation);
		var parsedRel = parsedOuter.concat(relSegs);
		fluid.url.cononocolosePath(parsedRel);
		var parsedInner = fluid.url.parseSegs(iframeLocation);
		var seg = 0;
		for (; seg < parsedRel.length; ++seg) {
			if (parsedRel[seg] != parsedInner[seg]) {
				break
			}
		}
		var excess = parsedInner.length - seg;
		var back = fluid.url.generateDepth(excess);
		var front = fluid.url.collapseSegs(parsedRel, seg);
		return back + front
	}
})(jQuery, fluid_1_4);
var fluid_1_4 = fluid_1_4 || {};
(function ($, fluid) {
	fluid.defaults("fluid.uiOptions.store", {
		gradeNames : ["fluid.littleComponent", "autoInit"],
		defaultSiteSettings : {
			textFont : "default",
			theme : "default",
			textSize : 1,
			lineSpacing : 1,
			layout : false,
			toc : false,
			links : false,
			inputsLarger : false
		}
	});
	fluid.defaults("fluid.cookieStore", {
		gradeNames : ["fluid.uiOptions.store", "autoInit"],
		invokers : {
			fetch : {
				funcName : "fluid.cookieStore.fetch",
				args : ["{cookieStore}.options.cookie.name", "{cookieStore}.options.defaultSiteSettings"]
			},
			save : {
				funcName : "fluid.cookieStore.save",
				args : ["{arguments}.0", "{cookieStore}.options.cookie"]
			}
		},
		cookie : {
			name : "fluid-ui-settings",
			path : "/",
			expires : ""
		}
	});
	fluid.cookieStore.fetch = function (cookieName, defaults) {
		var cookie = document.cookie;
		var cookiePrefix = cookieName + "=";
		var retObj,
		startIndex,
		endIndex;
		if (cookie.length > 0) {
			startIndex = cookie.indexOf(cookiePrefix);
			if (startIndex > -1) {
				startIndex = startIndex + cookiePrefix.length;
				endIndex = cookie.indexOf(";", startIndex);
				if (endIndex < startIndex) {
					endIndex = cookie.length
				}
				retObj = JSON.parse(decodeURIComponent(cookie.substring(startIndex, endIndex)))
			}
		}
		return retObj || defaults
	};
	fluid.cookieStore.assembleCookie = function (cookieOptions) {
		var cookieStr = cookieOptions.name + "=" + cookieOptions.data;
		if (cookieOptions.expires) {
			cookieStr += "; expires=" + cookieOptions.expires
		}
		if (cookieOptions.path) {
			cookieStr += "; path=" + cookieOptions.path
		}
		return cookieStr
	};
	fluid.cookieStore.save = function (settings, cookieOptions) {
		cookieOptions.data = encodeURIComponent(JSON.stringify(settings));
		document.cookie = fluid.cookieStore.assembleCookie(cookieOptions)
	};
	fluid.defaults("fluid.tempStore", {
		gradeNames : ["fluid.uiOptions.store", "autoInit"],
		invokers : {
			fetch : {
				funcName : "fluid.tempStore.fetch",
				args : ["{tempStore}"]
			},
			save : {
				funcName : "fluid.tempStore.save",
				args : ["{arguments}.0", "{tempStore}"]
			}
		},
		finalInitFunction : "fluid.tempStore.finalInit"
	});
	fluid.tempStore.finalInit = function (that) {
		that.model = that.options.defaultSiteSettings
	};
	fluid.tempStore.fetch = function (that) {
		return that.model
	};
	fluid.tempStore.save = function (settings, that) {
		that.model = settings
	}
})(jQuery, fluid_1_4);
var fluid_1_4 = fluid_1_4 || {};
(function ($, fluid) {
	fluid.registerNamespace("fluid.browser.version");
	fluid.browser.msie = function () {
		var isIE = ($.browser.msie);
		return isIE ? fluid.typeTag("fluid.browser.msie") : undefined
	};
	fluid.browser.majorVersion = function () {
		var version = $.browser.version;
		var dotpos = version.indexOf(".");
		var majorVersion = version.substring(0, dotpos);
		return fluid.typeTag("fluid.browser.majorVersion." + majorVersion)
	};
	var features = {
		browserIE : fluid.browser.msie(),
		browserMajorVersion : fluid.browser.majorVersion()
	};
	fluid.merge(null, fluid.staticEnvironment, features);
	fluid.hasFeature = function (tagName) {
		return fluid.find(fluid.staticEnvironment, function (value) {
			return value && value.typeName === tagName ? true : undefined
		})
	};
	fluid.defaults("fluid.uiEnhancer", {
		gradeNames : ["fluid.viewComponent", "autoInit"],
		components : {
			textSize : {
				type : "fluid.uiEnhancer.textSizer",
				container : "{uiEnhancer}.container",
				options : {
					invokers : {
						calcInitSize : {
							funcName : "fluid.uiEnhancer.textSizer.calcInitSize",
							args : ["{textSizer}.container", "{uiEnhancer}.options.fontSizeMap"]
						}
					}
				}
			},
			tableOfContents : {
				type : "fluid.tableOfContents",
				container : "{uiEnhancer}.container",
				createOnEvent : "onCreateTOCReady",
				options : {
					components : {
						levels : {
							type : "fluid.tableOfContents.levels",
							options : {
								resources : {
									template : {
										forceCache : true,
										url : "{uiEnhancer}.options.tocTemplate"
									}
								}
							}
						}
					},
					listeners : {
						afterRender : "{uiEnhancer}.lateRefreshRelay"
					}
				}
			},
			textFont : {
				type : "fluid.uiEnhancer.classSwapper",
				container : "{uiEnhancer}.container",
				options : {
					classes : "{uiEnhancer}.options.classnameMap.textFont"
				}
			},
			lineSpacing : {
				type : "fluid.uiEnhancer.lineSpacer",
				container : "{uiEnhancer}.container",
				options : {
					invokers : {
						calcInitSize : {
							funcName : "fluid.uiEnhancer.lineSpacer.calcInitSize",
							args : ["{lineSpacer}.container", "{uiEnhancer}.options.fontSizeMap"]
						}
					}
				}
			},
			theme : {
				type : "fluid.uiEnhancer.classSwapper",
				container : "{uiEnhancer}.container",
				options : {
					classes : "{uiEnhancer}.options.classnameMap.theme"
				}
			},
			settingsStore : {
				type : "fluid.uiOptions.store",
				options : {
					defaultSiteSettings : "{uiEnhancer}.options.defaultSiteSettings"
				}
			}
		},
		invokers : {
			updateModel : {
				funcName : "fluid.uiEnhancer.updateModel",
				args : ["@0", "{uiEnhancer}.applier"]
			},
			updateFromSettingsStore : {
				funcName : "fluid.uiEnhancer.updateFromSettingsStore",
				args : ["{uiEnhancer}"]
			},
			refreshView : {
				funcName : "fluid.uiEnhancer.refreshView",
				args : ["{uiEnhancer}"]
			},
			styleElements : "fluid.uiEnhancer.styleElements",
			setLayout : "fluid.uiEnhancer.setLayout",
			styleLinks : "fluid.uiEnhancer.styleLinks",
			styleInputs : "fluid.uiEnhancer.styleInputs",
			setIE6ColorInversion : "fluid.uiEnhancer.setIE6ColorInversion"
		},
		events : {
			onCreateTOCReady : null,
			lateRefreshView : null,
			modelChanged : null
		},
		listeners : {
			"lateRefreshView.domReading" : "fluid.uiEnhancer.applyDomReadingSettings"
		},
		classnameMap : {
			textFont : {
				"default" : "",
				times : "fl-font-uio-times",
				comic : "fl-font-uio-comic-sans",
				arial : "fl-font-uio-arial",
				verdana : "fl-font-uio-verdana"
			},
			theme : {
				"default" : "fl-uio-default-theme",
				bw : "fl-theme-uio-bw fl-theme-bw",
				wb : "fl-theme-uio-wb fl-theme-wb",
				by : "fl-theme-uio-by fl-theme-by",
				yb : "fl-theme-uio-yb fl-theme-yb"
			},
			layout : "fl-layout-linear",
			links : "fl-text-underline fl-text-bold fl-text-larger",
			inputsLarger : "fl-text-larger"
		},
		fontSizeMap : {
			"xx-small" : "9px",
			"x-small" : "11px",
			small : "13px",
			medium : "15px",
			large : "18px",
			"x-large" : "23px",
			"xx-large" : "30px"
		},
		selectors : {
			colorInversion : ".fl-inverted-color"
		},
		styles : {
			colorInversionClass : "fl-inverted-color"
		},
		finalInitFunction : "fluid.uiEnhancer.finalInit"
	});
	fluid.uiEnhancer.finalInit = function (that) {
		that.applier.modelChanged.addListener("", function (newModel, oldModel, changeRequest) {
			that.events.modelChanged.fire(newModel, oldModel, changeRequest);
			that.refreshView()
		});
		that.lateRefreshRelay = function () {
			that.events.lateRefreshView.fire(that)
		};
		that.updateFromSettingsStore();
		return that
	};
	fluid.uiEnhancer.updateFromSettingsStore = function (that) {
		that.updateModel(that.settingsStore.fetch())
	};
	fluid.uiEnhancer.updateModel = function (newModel, applier) {
		applier.requestChange("", newModel)
	};
	fluid.uiEnhancer.applyTocSetting = function (that) {
		var async = false;
		if (that.model.toc) {
			if (that.tableOfContents) {
				that.tableOfContents.show()
			} else {
				that.events.onCreateTOCReady.fire();
				async = true
			}
		} else {
			if (that.tableOfContents) {
				that.tableOfContents.hide()
			}
		}
		if (!async) {
			that.lateRefreshRelay()
		}
	};
	fluid.uiEnhancer.applyDomReadingSettings = function (that) {
		that.setLayout(that);
		that.styleLinks(that);
		that.styleInputs(that);
		that.setIE6ColorInversion(that)
	};
	fluid.uiEnhancer.refreshView = function (that) {
		that.textSize.set(that.model.textSize);
		that.textFont.swap(that.model.textFont);
		that.lineSpacing.set(that.model.lineSpacing);
		that.theme.swap(that.model.theme);
		$(document).ready(function () {
			fluid.uiEnhancer.applyTocSetting(that)
		})
	};
	fluid.uiEnhancer.styleElements = function (elements, setting, classname) {
		if (setting) {
			elements.addClass(classname)
		} else {
			$("." + classname, elements).andSelf().removeClass(classname)
		}
	};
	fluid.uiEnhancer.setLayout = function (that) {
		that.styleElements(that.container, that.model.layout, that.options.classnameMap.layout)
	};
	fluid.uiEnhancer.styleLinks = function (that) {
		var links = $("a", that.container);
		that.styleElements(links, that.model.links, that.options.classnameMap.links)
	};
	fluid.uiEnhancer.styleInputs = function (that) {
		that.styleElements($("input, button", that.container), that.model.inputsLarger, that.options.classnameMap.inputsLarger)
	};
	fluid.uiEnhancer.setIE6ColorInversion = function (that) {
		if (fluid.hasFeature("fluid.browser.msie") && fluid.hasFeature("fluid.browser.majorVersion.6") && that.model.theme === "default") {
			that.locate("colorInversion").removeClass(that.options.styles.colorInversionClass)
		}
	};
	fluid.uiEnhancer.getTextSizeInPx = function (container, fontSizeMap) {
		var fontSize = container.css("font-size");
		if (fontSizeMap[fontSize]) {
			fontSize = fontSizeMap[fontSize]
		}
		return parseFloat(fontSize)
	};
	fluid.uiEnhancer.getTextSizeInEm = function (container, fontSizeMap) {
		var px2emFactor = fluid.uiEnhancer.getPx2EmFactor(container, fontSizeMap);
		return Math.round(fluid.uiEnhancer.getTextSizeInPx(container, fontSizeMap) / px2emFactor * 10000) / 10000
	};
	fluid.uiEnhancer.getPx2EmFactor = function (container, fontSizeMap) {
		if (container.get(0).tagName !== "BODY") {
			container = container.parent()
		}
		return fluid.uiEnhancer.getTextSizeInPx(container, fontSizeMap)
	};
	fluid.uiEnhancer.getLineHeight = function (container) {
		var lineHeight;
		if (container[0].currentStyle) {
			lineHeight = container[0].currentStyle.lineHeight
		} else {
			lineHeight = container.css("line-height")
		}
		return lineHeight
	};
	fluid.uiEnhancer.numerizeLineHeight = function (lineHeight, fontSize) {
		if (!lineHeight) {
			return 0
		}
		if (lineHeight === "normal") {
			return 1.2
		}
		if (lineHeight.match(/[0-9]$/)) {
			return lineHeight
		}
		return Math.round(parseFloat(lineHeight) / fontSize * 100) / 100
	};
	fluid.defaults("fluid.uiEnhancer.textSizer", {
		gradeNames : ["fluid.viewComponent", "autoInit"],
		invokers : {
			set : {
				funcName : "fluid.uiEnhancer.textSizer.set",
				args : ["@0", "{textSizer}"]
			}
		}
	});
	fluid.uiEnhancer.textSizer.set = function (times, that) {
		if (!that.initialSize) {
			that.initialSize = that.calcInitSize()
		}
		if (that.initialSize) {
			var targetSize = times * that.initialSize;
			that.container.css("font-size", targetSize + "em")
		}
	};
	fluid.uiEnhancer.textSizer.calcInitSize = function (container, fontSizeMap) {
		return fluid.uiEnhancer.getTextSizeInEm(container, fontSizeMap)
	};
	fluid.defaults("fluid.uiEnhancer.classSwapper", {
		gradeNames : ["fluid.viewComponent", "autoInit"],
		invokers : {
			clearClasses : {
				funcName : "fluid.uiEnhancer.classSwapper.clearClasses",
				args : ["{classSwapper}"]
			},
			swap : {
				funcName : "fluid.uiEnhancer.classSwapper.swap",
				args : ["@0", "{classSwapper}"]
			}
		},
		classes : {},
		finalInitFunction : "fluid.uiEnhancer.classSwapper.finalInit"
	});
	fluid.uiEnhancer.classSwapper.finalInit = function (that) {
		that.classSelector = "";
		that.classStr = "";
		fluid.each(that.options.classes, function (className) {
			if (className) {
				that.classSelector += that.classSelector ? ", ." + className : "." + className;
				that.classStr += that.classStr ? " " + className : className
			}
		})
	};
	fluid.uiEnhancer.classSwapper.clearClasses = function (that) {
		that.container.removeClass(that.classStr)
	};
	fluid.uiEnhancer.classSwapper.swap = function (classname, that) {
		that.clearClasses(that);
		that.container.addClass(that.options.classes[classname])
	};
	fluid.defaults("fluid.uiEnhancer.lineSpacer", {
		gradeNames : ["fluid.viewComponent", "autoInit"],
		invokers : {
			set : {
				funcName : "fluid.uiEnhancer.lineSpacer.set",
				args : ["@0", "{lineSpacer}"]
			}
		}
	});
	fluid.uiEnhancer.lineSpacer.set = function (times, that) {
		if (!that.initialSize) {
			that.initialSize = that.calcInitSize()
		}
		if (that.initialSize) {
			var targetLineSpacing = times * that.initialSize;
			that.container.css("line-height", targetLineSpacing + "em")
		}
	};
	fluid.uiEnhancer.lineSpacer.calcInitSize = function (container, fontSizeMap) {
		var lineHeight = fluid.uiEnhancer.getLineHeight(container);
		var fontSize = fluid.uiEnhancer.getTextSizeInPx(container, fontSizeMap);
		return fluid.uiEnhancer.numerizeLineHeight(lineHeight, fontSize)
	};
	fluid.pageEnhancer = function (uiEnhancerOptions) {
		var that = fluid.initLittleComponent("fluid.pageEnhancer");
		uiEnhancerOptions = fluid.copy(uiEnhancerOptions);
		uiEnhancerOptions.originalUserOptions = fluid.copy(uiEnhancerOptions);
		that.uiEnhancerOptions = uiEnhancerOptions;
		fluid.initDependents(that);
		fluid.staticEnvironment.uiEnhancer = that.uiEnhancer;
		return that
	};
	fluid.defaults("fluid.pageEnhancer", {
		gradeNames : ["fluid.littleComponent"],
		components : {
			uiEnhancer : {
				type : "fluid.uiEnhancer",
				container : "body",
				options : "{pageEnhancer}.uiEnhancerOptions"
			}
		}
	});
	fluid.demands("fluid.uiOptions.store", ["fluid.uiEnhancer"], {
		funcName : "fluid.cookieStore"
	})
})(jQuery, fluid_1_4);
var fluid_1_4 = fluid_1_4 || {};
(function ($, fluid) {
	fluid.defaults("fluid.textfieldSlider", {
		gradeNames : ["fluid.viewComponent", "autoInit"],
		components : {
			textfield : {
				type : "fluid.textfieldSlider.textfield",
				container : "{textfieldSlider}.dom.textfield",
				options : {
					model : "{textfieldSlider}.model",
					applier : "{textfieldSlider}.applier"
				}
			},
			slider : {
				type : "fluid.textfieldSlider.slider",
				container : "{textfieldSlider}.dom.slider",
				options : {
					model : "{textfieldSlider}.model",
					applier : "{textfieldSlider}.applier"
				}
			}
		},
		selectors : {
			textfield : ".flc-textfieldSlider-field",
			slider : ".flc-textfieldSlider-slider"
		},
		events : {
			modelChanged : null
		},
		model : {
			value : null,
			min : 0,
			max : 100
		},
		sliderOptions : {
			orientation : "horizontal",
			step : 0.1
		},
		finalInitFunction : "fluid.textfieldSlider.finalInit"
	});
	fluid.textfieldSlider.finalInit = function (that) {
		var sliderOptions = $.extend(true, {}, that.options.sliderOptions, that.model);
		that.slider.initSlider(sliderOptions);
		that.refreshView = function () {
			var val = that.model.value;
			that.textfield.container.val(val);
			that.slider.setSliderValue(val);
			that.slider.setSliderAria(val)
		};
		that.applier.modelChanged.addListener("value", function (newModel) {
			that.events.modelChanged.fire(newModel.value)
		});
		that.events.modelChanged.addListener(that.refreshView);
		that.refreshView()
	};
	fluid.defaults("fluid.textfieldSlider.textfield", {
		gradeNames : ["fluid.viewComponent", "autoInit"],
		finalInitFunction : "fluid.textfieldSlider.textfield.finalInit"
	});
	fluid.textfieldSlider.validateValue = function (model, changeRequest, applier) {
		var oldValue = model.value;
		var newValue = changeRequest.value;
		var isValidNum = !isNaN(parseInt(newValue, 10));
		if (isValidNum) {
			if (newValue < model.min) {
				newValue = model.min
			} else {
				if (newValue > model.max) {
					newValue = model.max
				}
			}
			changeRequest.value = newValue
		} else {
			changeRequest.value = oldValue
		}
	};
	fluid.textfieldSlider.textfield.finalInit = function (that) {
		that.applier.guards.addListener({
			path : "value",
			transactional : true
		}, fluid.textfieldSlider.validateValue);
		that.container.change(function (source) {
			that.applier.requestChange("value", source.target.value)
		})
	};
	fluid.defaults("fluid.textfieldSlider.slider", {
		gradeNames : ["fluid.viewComponent", "autoInit"],
		finalInitFunction : "fluid.textfieldSlider.slider.finalInit",
		selectors : {
			thumb : ".ui-slider-handle"
		}
	});
	var initSliderAria = function (thumb, opts) {
		var ariaDefaults = {
			role : "slider",
			"aria-valuenow" : opts.value,
			"aria-valuemin" : opts.min,
			"aria-valuemax" : opts.max
		};
		thumb.attr(ariaDefaults)
	};
	fluid.textfieldSlider.slider.finalInit = function (that) {
		that.slider = that.container.slider(that.model);
		that.initSlider = function (sliderOptions) {
			var slider = that.slider.slider(sliderOptions);
			initSliderAria(that.locate("thumb"), sliderOptions)
		};
		that.setSliderValue = function (value) {
			that.slider.slider("value", value)
		};
		that.setSliderAria = function (value) {
			that.locate("thumb").attr("aria-valuenow", value)
		};
		that.slider.bind("slide", function (e, ui) {
			that.applier.requestChange("value", ui.value)
		})
	}
})(jQuery, fluid_1_4);
(function ($, fluid) {
	fluid.registerNamespace("fluid.uiOptions.inline");
	fluid.defaults("fluid.uiOptions.inline", {
		gradeNames : ["fluid.viewComponent"],
		mergePolicy : {
			uiOptionsTransform : "noexpand",
			derivedDefaults : "noexpand"
		},
		components : {
			uiOptionsLoader : {
				type : "fluid.uiOptions.loader"
			},
			templateLoader : {
				priority : "first",
				type : "fluid.uiOptions.templateLoader"
			}
		},
		uiOptionsTransform : {
			transformer : "fluid.uiOptions.mapOptions",
			config : {
				"*.templateLoader" : "templateLoader",
				"*.templateLoader.*.templatePath.options.value" : "prefix",
				"*.uiOptionsLoader" : "uiOptionsLoader",
				"*.uiOptionsLoader.container" : "container",
				"*.uiOptionsLoader.*.uiOptions" : "uiOptions",
				"*.uiOptionsLoader.*.uiOptions.*.textControls" : "textControls",
				"*.uiOptionsLoader.*.uiOptions.*.layoutControls" : "layoutControls",
				"*.uiOptionsLoader.*.uiOptions.*.linksControls" : "linksControls",
				"*.uiOptionsLoader.*.uiOptions.*.preview" : "preview",
				"*.uiOptionsLoader.*.uiOptions.*.preview.*.enhancer" : "previewEnhancer"
			}
		},
		derivedDefaults : {
			uiOptions : {
				options : {
					components : {
						settingsStore : "{uiEnhancer}.settingsStore"
					},
					listeners : {
						onUIOptionsRefresh : "{uiEnhancer}.updateFromSettingsStore"
					}
				}
			}
		}
	});
	fluid.uiOptions.inline.makeCreator = function (componentName, processor) {
		fluid.setGlobalValue(componentName, function (container, options) {
			var defaults = fluid.defaults(componentName);
			options.container = container;
			options = processor(options);
			var mappedOptions = fluid.uiOptions.mapOptions(options, defaults.uiOptionsTransform.config, defaults.mergePolicy, fluid.copy(defaults.derivedDefaults));
			var that = fluid.initView(componentName, container, mappedOptions);
			fluid.diagnoseFailedView(componentName, that, fluid.defaults(componentName), [componentName, container, mappedOptions]);
			fluid.initDependents(that);
			return that
		})
	};
	fluid.uiOptions.sortByKeyLength = function (inObject) {
		var keys = [];
		for (var k in inObject) {
			keys.push(k)
		}
		keys.sort(function (a, b) {
			return a.length - b.length
		});
		return keys
	};
	fluid.uiOptions.mapOptionsRecord = function (options, sortedConfigKeys, config) {
		var opRecs = [{}, {}, options || {}
			
		];
		var appliers = fluid.transform(opRecs, function (opRec) {
				return fluid.makeChangeApplier(opRec)
			});
		fluid.each(sortedConfigKeys, function (origDest) {
			var source = config[origDest];
			var dest = fluid.uiOptions.expandShortPath(origDest);
			var applier = appliers[origDest.charAt(0) === "!" ? 0 : 1];
			var value = fluid.get(options, source);
			if (value) {
				applier.requestChange(dest, value, "ADD");
				appliers[2].requestChange(source, value, "DELETE")
			}
		});
		return opRecs
	};
	fluid.uiOptions.mapOptions = function (options, config, mergePolicy, derivedDefaults) {
		var sortedConfigKeys = fluid.uiOptions.sortByKeyLength(config);
		var optrecs = fluid.uiOptions.mapOptionsRecord(options, sortedConfigKeys, config);
		var devrecs = fluid.uiOptions.mapOptionsRecord(derivedDefaults, sortedConfigKeys, config);
		var mergeOpts = [mergePolicy].concat(devrecs).concat(optrecs);
		return fluid.merge.apply(null, mergeOpts)
	};
	fluid.uiOptions.expandShortPath = function (path) {
		if (path.charAt(0) === "!") {
			path = path.substring(1)
		}
		var strToreplaceFirst = "components";
		var strToreplaceRest = "options.components";
		var newPath = (path.charAt(0) === "*") ? path.replace("*", strToreplaceFirst) : path;
		newPath = newPath.replace(/\*/g, strToreplaceRest);
		return newPath
	};
	fluid.defaults("fluid.uiOptions.templateLoader", {
		gradeNames : ["fluid.eventedComponent", "autoInit"],
		finalInitFunction : "fluid.uiOptions.templateLoader.resolveTemplates",
		templates : {
			uiOptions : "%prefix/FatPanelUIOptions.html",
			textControls : "%prefix/UIOptionsTemplate-text.html",
			layoutControls : "%prefix/UIOptionsTemplate-layout.html",
			linksControls : "%prefix/UIOptionsTemplate-links.html"
		},
		components : {
			templatePath : {
				type : "fluid.uiOptions.templatePath"
			}
		},
		invokers : {
			transformURL : {
				funcName : "fluid.stringTemplate",
				args : ["{arguments}.0", {
						"prefix/" : "{templateLoader}.templatePath.options.value"
					}
				]
			}
		}
	});
	fluid.uiOptions.templateLoader.resolveTemplates = function (that) {
		var mapped = fluid.transform(that.options.templates, that.transformURL);
		that.resources = fluid.transform(mapped, function (url) {
				return {
					url : url,
					forceCache : true
				}
			})
	};
	fluid.defaults("fluid.uiOptions.templatePath", {
		gradeNames : ["fluid.littleComponent", "autoInit"],
		value : "../html/"
	});
	fluid.demands("fluid.uiOptions.textControls", ["fluid.uiOptions"], {
		options : {
			classnameMap : "{uiEnhancer}.options.classnameMap"
		}
	});
	fluid.demands("fluid.uiOptions.layoutControls", ["fluid.uiOptions"], {
		options : {
			classnameMap : "{uiEnhancer}.options.classnameMap"
		}
	});
	fluid.demands("fluid.uiOptions.linksControls", ["fluid.uiOptions"], {
		options : {
			classnameMap : "{uiEnhancer}.options.classnameMap"
		}
	});
	fluid.uiOptions.onReadyFirer = function (uiOptionsLoader, uiOptions) {
		uiOptionsLoader.events.onReady.fire(uiOptionsLoader, uiOptions)
	};
	fluid.defaults("fluid.uiOptions.loader", {
		gradeNames : ["fluid.viewComponent", "autoInit"],
		resources : "{templateLoader}.resources",
		finalInitFunction : "fluid.uiOptions.loader.finalInit",
		events : {
			onUIOptionsTemplateReady : null,
			onUIOptionsComponentReady : null,
			onUIOptionsReadyBridge : {
				event : "onUIOptionsComponentReady",
				args : ["{fluid.uiOptions.loader}", "{arguments}.0"]
			},
			onReady : null
		},
		listeners : {
			onUIOptionsReadyBridge : {
				listener : fluid.uiOptions.onReadyFirer,
				priority : "last"
			}
		},
		components : {
			uiOptions : {
				type : "fluid.uiOptions",
				container : "{loader}.container",
				createOnEvent : "onUIOptionsTemplateReady",
				options : {
					events : {
						onUIOptionsComponentReady : "{loader}.events.onUIOptionsComponentReady"
					}
				}
			}
		}
	});
	fluid.uiOptions.loader.finalInit = function (that) {
		fluid.fetchResources(that.options.resources, function () {
			that.events.onUIOptionsTemplateReady.fire()
		})
	};
	fluid.defaults("fluid.uiOptions", {
		gradeNames : ["fluid.viewComponent", "autoInit"],
		components : {
			textControls : {
				type : "fluid.uiOptions.textControls",
				container : "{uiOptions}.dom.textControls",
				createOnEvent : "onUIOptionsComponentReady",
				options : {
					model : "{uiOptions}.model",
					applier : "{uiOptions}.applier",
					events : {
						onUIOptionsRefresh : "{uiOptions}.events.onUIOptionsRefresh"
					}
				}
			},
			layoutControls : {
				type : "fluid.uiOptions.layoutControls",
				container : "{uiOptions}.dom.layoutControls",
				createOnEvent : "onUIOptionsComponentReady",
				options : {
					model : "{uiOptions}.model",
					applier : "{uiOptions}.applier",
					events : {
						onUIOptionsRefresh : "{uiOptions}.events.onUIOptionsRefresh"
					}
				}
			},
			linksControls : {
				type : "fluid.uiOptions.linksControls",
				container : "{uiOptions}.dom.linksControls",
				createOnEvent : "onUIOptionsComponentReady",
				options : {
					model : "{uiOptions}.model",
					applier : "{uiOptions}.applier",
					events : {
						onUIOptionsRefresh : "{uiOptions}.events.onUIOptionsRefresh"
					}
				}
			},
			preview : {
				type : "fluid.uiOptions.preview",
				createOnEvent : "onUIOptionsComponentReady",
				container : "{uiOptions}.dom.previewFrame"
			},
			eventBinder : {
				type : "fluid.uiOptions.eventBinder"
			}
		},
		selectors : {
			textControls : ".flc-uiOptions-text-controls",
			layoutControls : ".flc-uiOptions-layout-controls",
			linksControls : ".flc-uiOptions-links-controls",
			cancel : ".flc-uiOptions-cancel",
			reset : ".flc-uiOptions-reset",
			save : ".flc-uiOptions-save",
			previewFrame : ".flc-uiOptions-preview-frame"
		},
		events : {
			onSave : null,
			onCancel : null,
			onReset : null,
			onAutoSave : null,
			modelChanged : null,
			onUIOptionsRefresh : null,
			onUIOptionsComponentReady : null
		},
		finalInitFunction : "fluid.uiOptions.finalInit",
		resources : {
			template : "{templateLoader}.resources.uiOptions"
		},
		autoSave : false
	});
	fluid.uiOptions.finalInit = function (that) {
		that.applier.requestChange("selections", fluid.copy(that.settingsStore.fetch()));
		that.save = function () {
			that.events.onSave.fire(that.model.selections);
			var savedSelections = fluid.copy(that.model.selections);
			that.settingsStore.save(savedSelections)
		};
		that.saveAndApply = function () {
			that.save();
			that.events.onUIOptionsRefresh.fire()
		};
		that.reset = function () {
			that.updateModel(fluid.copy(that.settingsStore.options.defaultSiteSettings));
			that.events.onReset.fire(that);
			that.events.onUIOptionsRefresh.fire()
		};
		that.cancel = function () {
			that.events.onCancel.fire();
			that.updateModel(that.settingsStore.fetch());
			that.events.onUIOptionsRefresh.fire()
		};
		that.updateModel = function (newModel) {
			that.applier.requestChange("selections", newModel)
		};
		that.applier.modelChanged.addListener("selections", function (newModel, oldModel, changeRequest) {
			that.events.modelChanged.fire(newModel, oldModel, changeRequest.source);
			if (that.options.autoSave) {
				that.events.onAutoSave.fire()
			}
		});
		var bindHandlers = function (that) {
			var saveButton = that.locate("save");
			if (saveButton.length > 0) {
				saveButton.click(that.saveAndApply);
				var form = fluid.findForm(saveButton);
				$(form).submit(function () {
					that.saveAndApply()
				})
			}
			that.locate("reset").click(that.reset);
			that.locate("cancel").click(that.cancel)
		};
		var bindEventHandlers = function (that) {
			that.events.onAutoSave.addListener(function () {
				that.save()
			})
		};
		fluid.fetchResources(that.options.resources, function () {
			setTimeout(function () {
				that.container.append(that.options.resources.template.resourceText);
				bindHandlers(that);
				bindEventHandlers(that);
				that.events.onUIOptionsComponentReady.fire(that)
			}, 1)
		})
	};
	fluid.defaults("fluid.uiOptions.eventBinder", {
		gradeNames : ["fluid.eventedComponent", "autoInit"]
	});
	var initModel = function (that) {
		fluid.each(that.options.controlValues, function (item, key) {
			that.applier.requestChange("labelMap." + key, {
				values : that.options.controlValues[key],
				names : that.options.strings[key],
				classes : that.options.classnameMap[key]
			})
		})
	};
	var createSliderNode = function (that, item) {
		return {
			decorators : {
				type : "fluid",
				func : "fluid.textfieldSlider",
				options : {
					listeners : {
						modelChanged : function (value) {
							that.applier.requestChange("selections." + item, value)
						}
					},
					model : {
						min : that.options[item].min,
						max : that.options[item].max,
						value : that.model.selections[item]
					}
				}
			}
		}
	};
	fluid.uiOptions.controlsFinalInit = function (that) {
		initModel(that);
		that.refreshView()
	};
	fluid.uiOptions.lateRefreshViewBinder = function (that) {
		that.refreshView = function () {
			that.renderer.refreshView()
		}
	};
	fluid.defaults("fluid.uiOptions.textControls", {
		gradeNames : ["fluid.rendererComponent", "autoInit"],
		strings : {
			textFont : ["Default", "Times New Roman", "Comic Sans", "Arial", "Verdana"],
			theme : ["Default", "Black on white", "White on black", "Black on yellow", "Yellow on black"]
		},
		controlValues : {
			textFont : ["default", "times", "comic", "arial", "verdana"],
			theme : ["default", "bw", "wb", "by", "yb"]
		},
		textSize : {
			min : 1,
			max : 2
		},
		lineSpacing : {
			min : 1,
			max : 2
		},
		selectors : {
			textFont : ".flc-uiOptions-text-font",
			theme : ".flc-uiOptions-theme",
			textSize : ".flc-uiOptions-min-text-size",
			lineSpacing : ".flc-uiOptions-line-spacing"
		},
		events : {
			onUIOptionsRefresh : null
		},
		listeners : {
			onUIOptionsRefresh : "{textControls}.refreshView"
		},
		preInitFunction : "fluid.uiOptions.lateRefreshViewBinder",
		finalInitFunction : "fluid.uiOptions.controlsFinalInit",
		produceTree : "fluid.uiOptions.textControls.produceTree",
		resources : {
			template : "{templateLoader}.resources.textControls"
		}
	});
	fluid.uiOptions.textControls.produceTree = function (that) {
		var tree = {};
		for (var item in that.model.selections) {
			if (item === "textFont" || item === "theme") {
				tree[item] = {
					optionnames : "${labelMap." + item + ".names}",
					optionlist : "${labelMap." + item + ".values}",
					selection : "${selections." + item + "}",
					decorators : {
						type : "fluid",
						func : "fluid.uiOptions.selectDecorator",
						options : {
							styles : that.options.classnameMap[item]
						}
					}
				}
			} else {
				if (item === "textSize" || item === "lineSpacing") {
					tree[item] = createSliderNode(that, item)
				}
			}
		}
		return tree
	};
	fluid.demands("fluid.uiOptions.selectDecorator", "fluid.uiOptions", {
		container : "{arguments}.0"
	});
	fluid.defaults("fluid.uiOptions.selectDecorator", {
		gradeNames : ["fluid.viewComponent", "autoInit"],
		finalInitFunction : "fluid.uiOptions.selectDecorator.finalInit",
		styles : {
			preview : "fl-preview-theme"
		}
	});
	fluid.uiOptions.selectDecorator.finalInit = function (that) {
		fluid.each($("option", that.container), function (option) {
			var styles = that.options.styles;
			$(option).addClass(styles.preview + " " + styles[fluid.value(option)])
		})
	};
	fluid.defaults("fluid.uiOptions.layoutControls", {
		gradeNames : ["fluid.rendererComponent", "autoInit"],
		selectors : {
			layout : ".flc-uiOptions-layout",
			toc : ".flc-uiOptions-toc"
		},
		events : {
			onUIOptionsRefresh : null
		},
		listeners : {
			onUIOptionsRefresh : "{layoutControls}.refreshView"
		},
		preInitFunction : "fluid.uiOptions.lateRefreshViewBinder",
		finalInitFunction : "fluid.uiOptions.controlsFinalInit",
		produceTree : "fluid.uiOptions.layoutControls.produceTree",
		resources : {
			template : "{templateLoader}.resources.layoutControls"
		}
	});
	fluid.uiOptions.layoutControls.produceTree = function (that) {
		var tree = {};
		for (var item in that.model.selections) {
			if (item === "layout" || item === "toc") {
				tree[item] = "${selections." + item + "}"
			}
		}
		return tree
	};
	fluid.defaults("fluid.uiOptions.linksControls", {
		gradeNames : ["fluid.rendererComponent", "autoInit"],
		selectors : {
			links : ".flc-uiOptions-links",
			inputsLarger : ".flc-uiOptions-inputs-larger"
		},
		events : {
			onUIOptionsRefresh : null
		},
		listeners : {
			onUIOptionsRefresh : "{linksControls}.refreshView"
		},
		preInitFunction : "fluid.uiOptions.lateRefreshViewBinder",
		finalInitFunction : "fluid.uiOptions.controlsFinalInit",
		produceTree : "fluid.uiOptions.linksControls.produceTree",
		resources : {
			template : "{templateLoader}.resources.linksControls"
		}
	});
	fluid.uiOptions.linksControls.produceTree = function (that) {
		var tree = {};
		for (var item in that.model.selections) {
			if (item === "links" || item === "inputsLarger") {
				tree[item] = "${selections." + item + "}"
			}
		}
		return tree
	};
	fluid.defaults("fluid.uiOptions.preview", {
		gradeNames : ["fluid.viewComponent", "autoInit"],
		components : {
			enhancer : {
				type : "fluid.uiEnhancer",
				createOnEvent : "onReady",
				options : {
					settingsStore : {
						type : "fluid.uiEnhancer.tempStore"
					}
				}
			},
			eventBinder : {
				type : "fluid.uiOptions.preview.eventBinder",
				createOnEvent : "onReady"
			},
			templateLoader : "{templateLoader}"
		},
		invokers : {
			updateModel : {
				funcName : "fluid.uiOptions.preview.updateModel",
				args : ["{preview}", "{uiOptions}.model.selections"]
			}
		},
		finalInitFunction : "fluid.uiOptions.preview.finalInit",
		events : {
			onReady : null
		},
		templateUrl : "%prefix/UIOptionsPreview.html"
	});
	fluid.uiOptions.preview.updateModel = function (that, selections) {
		setTimeout(function () {
			if (that.enhancer) {
				that.enhancer.updateModel(selections)
			}
		}, 0)
	};
	fluid.uiOptions.preview.finalInit = function (that) {
		var templateUrl = that.templateLoader.transformURL(that.options.templateUrl);
		that.container.load(function () {
			that.enhancerContainer = $("body", that.container.contents());
			that.events.onReady.fire()
		});
		that.container.attr("src", templateUrl)
	};
	fluid.demands("fluid.uiEnhancer", "fluid.uiOptions.preview", {
		funcName : "fluid.uiEnhancer",
		args : ["{preview}.enhancerContainer", "{options}"]
	});
	fluid.defaults("fluid.uiOptions.preview.eventBinder", {
		gradeNames : ["fluid.eventedComponent", "autoInit"]
	});
	fluid.demands("fluid.uiOptions.preview.eventBinder", ["fluid.uiOptions.preview", "fluid.uiOptions"], {
		options : {
			listeners : {
				"{uiOptions}.events.modelChanged" : "{preview}.updateModel"
			}
		}
	})
})(jQuery, fluid_1_4);
var fluid_1_4 = fluid_1_4 || {};
(function ($, fluid) {
	fluid.defaults("fluid.uiOptions.fatPanelEventBinder", {
		gradeNames : ["fluid.eventedComponent", "autoInit"],
		finalInitFunction : "fluid.uiOptions.fatPanelEventBinder.finalInit",
		components : {
			uiOptionsLoader : {
				type : "fluid.uiOptions.loader"
			},
			slidingPanel : {
				type : "fluid.slidingPanel"
			}
		}
	});
	fluid.defaults("fluid.uiOptions.fatPanelEventBinder.binder", {
		gradeNames : ["fluid.eventedComponent", "autoInit"]
	});
	fluid.registerNamespace("fluid.dom");
	fluid.dom.getDocumentHeight = function (dokkument) {
		var body = $("body", dokkument)[0];
		return body.offsetHeight
	};
	fluid.uiOptions.fatPanelEventBinder.updateView = function (uiOptions) {
		uiOptions.uiEnhancer.updateFromSettingsStore();
		uiOptions.events.onSignificantDOMChange.fire()
	};
	fluid.uiOptions.fatPanelEventBinder.bindLateEvents = function (uiOptions, eventBinder, fatPanel) {
		eventBinder.uiOptions = uiOptions;
		uiOptions.events.modelChanged.addListener(function (model) {
			eventBinder.uiEnhancer.updateModel(model.selections);
			uiOptions.save()
		});
		uiOptions.events.onReset.addListener(function (uiOptions) {
			fluid.uiOptions.fatPanelEventBinder.updateView(uiOptions)
		});
		uiOptions.events.onSignificantDOMChange.addListener(function () {
			var dokkument = uiOptions.container[0].ownerDocument;
			var height = fluid.dom.getDocumentHeight(dokkument);
			var iframe = fatPanel.markupRenderer.iframe;
			var attrs = {
				height : height + 15
			};
			iframe.animate(attrs, 400)
		});
		fatPanel.slidingPanel.events.afterPanelHide.addListener(function () {
			fatPanel.markupRenderer.iframe.height(0)
		})
	};
	fluid.uiOptions.fatPanelEventBinder.finalInit = function (that) {
		that.slidingPanel.events.afterPanelShow.addListener(function () {
			fluid.uiOptions.fatPanelEventBinder.updateView(that.uiOptions)
		})
	};
	fluid.uiOptions.fatPanelEventBinder.showPanel = function (panel, callback) {
		panel.show();
		setTimeout(callback, 1)
	};
	fluid.defaults("fluid.uiOptions.fatPanel", {
		gradeNames : ["fluid.viewComponent"],
		selectors : {
			iframe : ".flc-uiOptions-iframe"
		},
		relativePrefix : "./",
		components : {
			slidingPanel : {
				type : "fluid.slidingPanel",
				container : "{fatPanel}.container",
				options : {
					invokers : {
						operateShow : {
							funcName : "fluid.uiOptions.fatPanelEventBinder.showPanel"
						}
					}
				},
				createOnEvent : "afterRender"
			},
			markupRenderer : {
				type : "fluid.uiOptions.renderIframe",
				container : "{fatPanel}.dom.iframe",
				options : {
					markupProps : {
						src : "%prefix/FatPanelUIOptionsFrame.html"
					},
					events : {
						afterRender : "{fatPanel}.events.afterRender"
					}
				}
			},
			uiEnhancer : "{uiEnhancer}",
			eventBinder : {
				type : "fluid.uiOptions.fatPanelEventBinder",
				options : {
					components : {
						uiEnhancer : "{fatPanel}.uiEnhancer",
						uiOptionsLoader : "{fatPanel}.bridge.uiOptionsLoader",
						slidingPanel : "{fatPanel}.slidingPanel",
						binder : {
							type : "fluid.uiOptions.fatPanelEventBinder.binder",
							priority : "last",
							options : {
								events : {
									onUIOptionsComponentReady : {
										event : "{uiOptionsLoader}.events.onUIOptionsComponentReady",
										args : ["{arguments}.0", "{fluid.uiOptions.fatPanelEventBinder}", "{fatPanel}"]
									}
								},
								listeners : {
									onUIOptionsComponentReady : fluid.uiOptions.fatPanelEventBinder.bindLateEvents
								}
							}
						}
					}
				},
				createOnEvent : "afterRender",
				priority : "last"
			},
			bridge : {
				type : "fluid.uiOptions.bridge",
				createOnEvent : "afterRender",
				priority : "first",
				options : {
					components : {
						uiEnhancer : "{fatPanel}.uiEnhancer",
						markupRenderer : "{fatPanel}.markupRenderer"
					}
				}
			}
		},
		uiOptionsTransform : {
			transformer : "fluid.uiOptions.mapOptions",
			config : {
				"*.slidingPanel" : "slidingPanel",
				"*.markupRenderer" : "markupRenderer",
				"*.markupRenderer.options.prefix" : "prefix",
				"*.eventBinder" : "eventBinder",
				"selectors.iframe" : "iframe",
				"*.bridge.options.templateLoader" : "templateLoader",
				"*.bridge.options.prefix" : "relativePrefix",
				"*.bridge.options.uiOptionsLoader" : "uiOptionsLoader",
				"*.bridge.options.uiOptions" : "uiOptions",
				"*.bridge.options.textControls" : "textControls",
				"*.bridge.options.layoutControls" : "layoutControls",
				"*.bridge.options.linksControls" : "linksControls",
				"*.bridge.options.uiEnhancer" : "uiEnhancer"
			}
		},
		events : {
			afterRender : null
		}
	});
	fluid.defaults("fluid.uiOptions.renderIframe", {
		gradeNames : ["fluid.viewComponent", "autoInit"],
		finalInitFunction : "fluid.uiOptions.renderIframe.finalInit",
		events : {
			afterRender : null
		},
		styles : {
			containerFlex : "fl-container-flex",
			container : "fl-uiOptions-fatPanel-iframe"
		},
		prefix : "./",
		markupProps : {
			style : "overflow-x:hidden; overflow-y:auto;",
			"class" : "flc-iframe",
			src : "%prefix/uiOptionsIframe.html"
		}
	});
	fluid.uiOptions.renderIframe.finalInit = function (that) {
		var styles = that.options.styles;
		that.options.markupProps.src = fluid.stringTemplate(that.options.markupProps.src, {
				"prefix/" : that.options.prefix
			});
		that.iframeSrc = that.options.markupProps.src;
		that.iframe = $("<iframe/>");
		that.iframe.load(function () {
			that.events.afterRender.fire()
		});
		that.iframe.attr(that.options.markupProps);
		that.iframe.addClass(styles.containerFlex);
		that.iframe.addClass(styles.container);
		that.iframe.appendTo(that.container)
	};
	fluid.defaults("fluid.uiOptions.bridge", {
		gradeNames : ["fluid.littleComponent", "autoInit"],
		finalInitFunction : "fluid.uiOptions.bridge.finalInit",
		iframe : null
	});
	fluid.uiOptions.tabSelectRelay = function (uiOptions) {
		uiOptions.events.onSignificantDOMChange.fire()
	};
	fluid.defaults("fluid.uiOptions.FatPanelOtherWorldLoader", {
		gradeNames : ["fluid.uiOptions.inline", "autoInit"],
		derivedDefaults : {
			uiOptions : {
				options : {
					events : {
						onSignificantDOMChange : null
					},
					components : {
						uiEnhancer : {
							type : "fluid.uiEnhancer",
							container : "body",
							priority : "first",
							options : {
								tocTemplate : "../../tableOfContents/html/TableOfContents.html"
							}
						},
						settingsStore : "{uiEnhancer}.settingsStore",
						preview : {
							type : "fluid.emptySubcomponent"
						},
						tabs : {
							type : "fluid.tabs",
							container : "body",
							createOnEvent : "onUIOptionsComponentReady",
							options : {
								events : {
									boiledTabShow : {
										event : "tabsshow",
										args : ["{uiOptions}"]
									}
								},
								listeners : {
									boiledTabShow : fluid.uiOptions.tabSelectRelay
								}
							}
						}
					}
				}
			}
		},
		uiOptionsTransform : {
			config : {
				"!*.uiOptionsLoader.*.uiOptions.*.uiEnhancer.options" : "uiEnhancer.options"
			}
		}
	});
	fluid.uiOptions.bridge.finalInit = function (that) {
		var iframe = that.markupRenderer.iframe;
		var origPrefix = that.markupRenderer.options.prefix;
		var iframeDoc = iframe.contents();
		var iframeWin = iframe[0].contentWindow;
		var innerFluid = iframeWin.fluid;
		var container = $("body", iframeDoc);
		var outerLocation = window.location.href;
		var iframeLocation = iframeWin.location.href;
		var relativePrefix = fluid.url.computeRelativePrefix(outerLocation, iframeLocation, origPrefix);
		that.options.relativePrefix = relativePrefix;
		var overallOptions = {};
		overallOptions.container = container;
		var bridgeMapping = fluid.defaults("fluid.uiOptions.fatPanel").uiOptionsTransform.config;
		var swappedBridgeMapping = {};
		fluid.each(bridgeMapping, function (value, key) {
			swappedBridgeMapping[value] = key
		});
		var bridgeSymbol = "*.bridge.options";
		fluid.each(swappedBridgeMapping, function (value, key) {
			if (value.indexOf(bridgeSymbol) === 0 && that.options[key]) {
				var keyInOtherWorld = value.substring(bridgeSymbol.length + 1);
				fluid.set(overallOptions, keyInOtherWorld, that.options[key])
			}
		});
		var defaults = fluid.defaults("fluid.uiOptions.FatPanelOtherWorldLoader");
		fluid.set(overallOptions, "uiEnhancer.options", that.uiEnhancer.options.originalUserOptions);
		var mappedOptions = fluid.uiOptions.mapOptions(overallOptions, defaults.uiOptionsTransform.config, defaults.mergePolicy, fluid.copy(defaults.derivedDefaults));
		var component = innerFluid.invokeGlobalFunction("fluid.uiOptions.FatPanelOtherWorldLoader", [container, mappedOptions]);
		that.uiOptionsLoader = component.uiOptionsLoader
	};
	fluid.uiOptions.fatPanel = function (container, options) {
		var defaults = fluid.defaults("fluid.uiOptions.fatPanel");
		var config = defaults.uiOptionsTransform.config;
		var mappedOptions = fluid.uiOptions.mapOptions(options, config, defaults.mergePolicy);
		var that = fluid.initView("fluid.uiOptions.fatPanel", container, mappedOptions);
		fluid.initDependents(that);
		return that
	}
})(jQuery, fluid_1_4);
var fluid_1_4 = fluid_1_4 || {};
(function ($, fluid) {
	fluid.defaults("fluid.uiOptions.fullNoPreview", {
		gradeNames : ["fluid.uiOptions.inline"],
		container : "{fullNoPreview}.container",
		derivedDefaults : {
			templateLoader : {
				options : {
					templates : {
						uiOptions : "%prefix/FullNoPreviewUIOptions.html"
					}
				}
			},
			uiOptions : {
				options : {
					components : {
						preview : {
							type : "fluid.emptySubcomponent"
						}
					},
					listeners : {
						onReset : function (uiOptions) {
							uiOptions.save()
						}
					}
				}
			}
		}
	});
	fluid.uiOptions.inline.makeCreator("fluid.uiOptions.fullNoPreview", fluid.identity)
})(jQuery, fluid_1_4);
var fluid_1_4 = fluid_1_4 || {};
(function ($, fluid) {
	fluid.defaults("fluid.uiOptions.fullPreview", {
		gradeNames : ["fluid.uiOptions.inline"],
		container : "{fullPreview}.container",
		uiOptionsTransform : {
			config : {
				"!*.uiOptionsLoader.*.uiOptions.*.preview.*.enhancer.options" : "outerPreviewEnhancerOptions"
			}
		},
		derivedDefaults : {
			templateLoader : {
				options : {
					templates : {
						uiOptions : "%prefix/FullPreviewUIOptions.html"
					}
				}
			}
		}
	});
	fluid.uiOptions.inline.makeCreator("fluid.uiOptions.fullPreview", function (options) {
		var enhancerOptions = fluid.get(fluid, "staticEnvironment.uiEnhancer.options.originalUserOptions");
		options.outerPreviewEnhancerOptions = enhancerOptions;
		return options
	})
})(jQuery, fluid_1_4);
var fluid_1_4 = fluid_1_4 || {};
(function ($, fluid) {
	fluid.defaults("fluid.slidingPanel", {
		gradeNames : ["fluid.viewComponent", "autoInit"],
		selectors : {
			panel : ".flc-slidingPanel-panel",
			toggleButton : ".flc-slidingPanel-toggleButton"
		},
		strings : {
			showText : "+ Show Display Preferences",
			hideText : "- Hide"
		},
		events : {
			onPanelHide : null,
			onPanelShow : null,
			afterPanelHide : null,
			afterPanelShow : null
		},
		finalInitFunction : "fluid.slidingPanel.finalInit",
		invokers : {
			operateHide : "fluid.slidingPanel.slideUp",
			operateShow : "fluid.slidingPanel.slideDown"
		},
		hideByDefault : true
	});
	fluid.slidingPanel.slideUp = function (element, callback, duration) {
		$(element).slideUp(duration || "400", callback)
	};
	fluid.slidingPanel.slideDown = function (element, callback, duration) {
		$(element).slideDown(duration || "400", callback)
	};
	fluid.slidingPanel.finalInit = function (that) {
		that.showPanel = function () {
			that.events.onPanelShow.fire(that);
			that.locate("toggleButton").text(that.options.strings.hideText);
			that.operateShow(that.locate("panel"), that.events.afterPanelShow.fire)
		};
		that.hidePanel = function () {
			that.events.onPanelHide.fire(that);
			that.locate("toggleButton").text(that.options.strings.showText);
			that.operateHide(that.locate("panel"), that.events.afterPanelHide.fire)
		};
		that.togglePanel = function () {
			if (that.locate("panel").is(":hidden")) {
				that.showPanel()
			} else {
				that.hidePanel()
			}
		};
		that.setPanelHeight = function (newHeight) {
			that.locate("panel").height(newHeight)
		};
		that.locate("toggleButton").click(that.togglePanel);
		if (that.options.hideByDefault) {
			that.locate("toggleButton").text(that.options.strings.showText);
			that.locate("panel").hide()
		}
	}
})(jQuery, fluid_1_4);
(function ($) {
	var $scrollTo = $.scrollTo = function (target, duration, settings) {
		$(window).scrollTo(target, duration, settings)
	};
	$scrollTo.defaults = {
		axis : "xy",
		duration : parseFloat($.fn.jquery) >= 1.3 ? 0 : 1
	};
	$scrollTo.window = function (scope) {
		return $(window)._scrollable()
	};
	$.fn._scrollable = function () {
		return this.map(function () {
			var elem = this,
			isWin = !elem.nodeName || $.inArray(elem.nodeName.toLowerCase(), ["iframe", "#document", "html", "body"]) != -1;
			if (!isWin) {
				return elem
			}
			var doc = (elem.contentWindow || elem).document || elem.ownerDocument || elem;
			return $.browser.safari || doc.compatMode == "BackCompat" ? doc.body : doc.documentElement
		})
	};
	$.fn.scrollTo = function (target, duration, settings) {
		if (typeof duration == "object") {
			settings = duration;
			duration = 0
		}
		if (typeof settings == "function") {
			settings = {
				onAfter : settings
			}
		}
		if (target == "max") {
			target = 9000000000
		}
		settings = $.extend({}, $scrollTo.defaults, settings);
		duration = duration || settings.speed || settings.duration;
		settings.queue = settings.queue && settings.axis.length > 1;
		if (settings.queue) {
			duration /= 2
		}
		settings.offset = both(settings.offset);
		settings.over = both(settings.over);
		return this._scrollable().each(function () {
			var elem = this,
			$elem = $(elem),
			targ = target,
			toff,
			attr = {},
			win = $elem.is("html,body");
			switch (typeof targ) {
			case "number":
			case "string":
				if (/^([+-]=)?\d+(\.\d+)?(px|%)?$/.test(targ)) {
					targ = both(targ);
					break
				}
				targ = $(targ, this);
			case "object":
				if (targ.is || targ.style) {
					toff = (targ = $(targ)).offset()
				}
			}
			$.each(settings.axis.split(""), function (i, axis) {
				var Pos = axis == "x" ? "Left" : "Top",
				pos = Pos.toLowerCase(),
				key = "scroll" + Pos,
				old = elem[key],
				max = $scrollTo.max(elem, axis);
				if (toff) {
					attr[key] = toff[pos] + (win ? 0 : old - $elem.offset()[pos]);
					if (settings.margin) {
						attr[key] -= parseInt(targ.css("margin" + Pos)) || 0;
						attr[key] -= parseInt(targ.css("border" + Pos + "Width")) || 0
					}
					attr[key] += settings.offset[pos] || 0;
					if (settings.over[pos]) {
						attr[key] += targ[axis == "x" ? "width" : "height"]() * settings.over[pos]
					}
				} else {
					var val = targ[pos];
					attr[key] = val.slice && val.slice(-1) == "%" ? parseFloat(val) / 100 * max : val
				}
				if (/^\d+$/.test(attr[key])) {
					attr[key] = attr[key] <= 0 ? 0 : Math.min(attr[key], max)
				}
				if (!i && settings.queue) {
					if (old != attr[key]) {
						animate(settings.onAfterFirst)
					}
					delete attr[key]
				}
			});
			animate(settings.onAfter);
			function animate(callback) {
				$elem.animate(attr, duration, settings.easing, callback && function () {
					callback.call(this, target, settings)
				})
			}
		}).end()
	};
	$scrollTo.max = function (elem, axis) {
		var Dim = axis == "x" ? "Width" : "Height",
		scroll = "scroll" + Dim;
		if (!$(elem).is("html,body")) {
			return elem[scroll] - $(elem)[Dim.toLowerCase()]()
		}
		var size = "client" + Dim,
		html = elem.ownerDocument.documentElement,
		body = elem.ownerDocument.body;
		return Math.max(html[scroll], body[scroll]) - Math.min(html[size], body[size])
	};
	function both(val) {
		return typeof val == "object" ? val : {
			top : val,
			left : val
		}
	}
})(jQuery);
/*	SWFObject v2.2 <http://code.google.com/p/swfobject/>
is released under the MIT License <http://www.opensource.org/licenses/mit-license.php>
 */
var swfobject = function () {
	var UNDEF = "undefined",
	OBJECT = "object",
	SHOCKWAVE_FLASH = "Shockwave Flash",
	SHOCKWAVE_FLASH_AX = "ShockwaveFlash.ShockwaveFlash",
	FLASH_MIME_TYPE = "application/x-shockwave-flash",
	EXPRESS_INSTALL_ID = "SWFObjectExprInst",
	ON_READY_STATE_CHANGE = "onreadystatechange",
	win = window,
	doc = document,
	nav = navigator,
	plugin = false,
	domLoadFnArr = [main],
	regObjArr = [],
	objIdArr = [],
	listenersArr = [],
	storedAltContent,
	storedAltContentId,
	storedCallbackFn,
	storedCallbackObj,
	isDomLoaded = false,
	isExpressInstallActive = false,
	dynamicStylesheet,
	dynamicStylesheetMedia,
	autoHideShow = true,
	ua = function () {
		var w3cdom = typeof doc.getElementById != UNDEF && typeof doc.getElementsByTagName != UNDEF && typeof doc.createElement != UNDEF,
		u = nav.userAgent.toLowerCase(),
		p = nav.platform.toLowerCase(),
		windows = p ? /win/.test(p) : /win/.test(u),
		mac = p ? /mac/.test(p) : /mac/.test(u),
		webkit = /webkit/.test(u) ? parseFloat(u.replace(/^.*webkit\/(\d+(\.\d+)?).*$/, "$1")) : false,
		ie = ! + "\v1",
		playerVersion = [0, 0, 0],
		d = null;
		if (typeof nav.plugins != UNDEF && typeof nav.plugins[SHOCKWAVE_FLASH] == OBJECT) {
			d = nav.plugins[SHOCKWAVE_FLASH].description;
			if (d && !(typeof nav.mimeTypes != UNDEF && nav.mimeTypes[FLASH_MIME_TYPE] && !nav.mimeTypes[FLASH_MIME_TYPE].enabledPlugin)) {
				plugin = true;
				ie = false;
				d = d.replace(/^.*\s+(\S+\s+\S+$)/, "$1");
				playerVersion[0] = parseInt(d.replace(/^(.*)\..*$/, "$1"), 10);
				playerVersion[1] = parseInt(d.replace(/^.*\.(.*)\s.*$/, "$1"), 10);
				playerVersion[2] = /[a-zA-Z]/.test(d) ? parseInt(d.replace(/^.*[a-zA-Z]+(.*)$/, "$1"), 10) : 0
			}
		} else {
			if (typeof win.ActiveXObject != UNDEF) {
				try {
					var a = new ActiveXObject(SHOCKWAVE_FLASH_AX);
					if (a) {
						d = a.GetVariable("$version");
						if (d) {
							ie = true;
							d = d.split(" ")[1].split(",");
							playerVersion = [parseInt(d[0], 10), parseInt(d[1], 10), parseInt(d[2], 10)]
						}
					}
				} catch (e) {}
				
			}
		}
		return {
			w3 : w3cdom,
			pv : playerVersion,
			wk : webkit,
			ie : ie,
			win : windows,
			mac : mac
		}
	}
	(),
	onDomLoad = function () {
		if (!ua.w3) {
			return
		}
		if ((typeof doc.readyState != UNDEF && doc.readyState == "complete") || (typeof doc.readyState == UNDEF && (doc.getElementsByTagName("body")[0] || doc.body))) {
			callDomLoadFunctions()
		}
		if (!isDomLoaded) {
			if (typeof doc.addEventListener != UNDEF) {
				doc.addEventListener("DOMContentLoaded", callDomLoadFunctions, false)
			}
			if (ua.ie && ua.win) {
				doc.attachEvent(ON_READY_STATE_CHANGE, function () {
					if (doc.readyState == "complete") {
						doc.detachEvent(ON_READY_STATE_CHANGE, arguments.callee);
						callDomLoadFunctions()
					}
				});
				if (win == top) {
					(function () {
						if (isDomLoaded) {
							return
						}
						try {
							doc.documentElement.doScroll("left")
						} catch (e) {
							setTimeout(arguments.callee, 0);
							return
						}
						callDomLoadFunctions()
					})()
				}
			}
			if (ua.wk) {
				(function () {
					if (isDomLoaded) {
						return
					}
					if (!/loaded|complete/.test(doc.readyState)) {
						setTimeout(arguments.callee, 0);
						return
					}
					callDomLoadFunctions()
				})()
			}
			addLoadEvent(callDomLoadFunctions)
		}
	}
	();
	function callDomLoadFunctions() {
		if (isDomLoaded) {
			return
		}
		try {
			var t = doc.getElementsByTagName("body")[0].appendChild(createElement("span"));
			t.parentNode.removeChild(t)
		} catch (e) {
			return
		}
		isDomLoaded = true;
		var dl = domLoadFnArr.length;
		for (var i = 0; i < dl; i++) {
			domLoadFnArr[i]()
		}
	}
	function addDomLoadEvent(fn) {
		if (isDomLoaded) {
			fn()
		} else {
			domLoadFnArr[domLoadFnArr.length] = fn
		}
	}
	function addLoadEvent(fn) {
		if (typeof win.addEventListener != UNDEF) {
			win.addEventListener("load", fn, false)
		} else {
			if (typeof doc.addEventListener != UNDEF) {
				doc.addEventListener("load", fn, false)
			} else {
				if (typeof win.attachEvent != UNDEF) {
					addListener(win, "onload", fn)
				} else {
					if (typeof win.onload == "function") {
						var fnOld = win.onload;
						win.onload = function () {
							fnOld();
							fn()
						}
					} else {
						win.onload = fn
					}
				}
			}
		}
	}
	function main() {
		if (plugin) {
			testPlayerVersion()
		} else {
			matchVersions()
		}
	}
	function testPlayerVersion() {
		var b = doc.getElementsByTagName("body")[0];
		var o = createElement(OBJECT);
		o.setAttribute("type", FLASH_MIME_TYPE);
		var t = b.appendChild(o);
		if (t) {
			var counter = 0;
			(function () {
				if (typeof t.GetVariable != UNDEF) {
					var d = t.GetVariable("$version");
					if (d) {
						d = d.split(" ")[1].split(",");
						ua.pv = [parseInt(d[0], 10), parseInt(d[1], 10), parseInt(d[2], 10)]
					}
				} else {
					if (counter < 10) {
						counter++;
						setTimeout(arguments.callee, 10);
						return
					}
				}
				b.removeChild(o);
				t = null;
				matchVersions()
			})()
		} else {
			matchVersions()
		}
	}
	function matchVersions() {
		var rl = regObjArr.length;
		if (rl > 0) {
			for (var i = 0; i < rl; i++) {
				var id = regObjArr[i].id;
				var cb = regObjArr[i].callbackFn;
				var cbObj = {
					success : false,
					id : id
				};
				if (ua.pv[0] > 0) {
					var obj = getElementById(id);
					if (obj) {
						if (hasPlayerVersion(regObjArr[i].swfVersion) && !(ua.wk && ua.wk < 312)) {
							setVisibility(id, true);
							if (cb) {
								cbObj.success = true;
								cbObj.ref = getObjectById(id);
								cb(cbObj)
							}
						} else {
							if (regObjArr[i].expressInstall && canExpressInstall()) {
								var att = {};
								att.data = regObjArr[i].expressInstall;
								att.width = obj.getAttribute("width") || "0";
								att.height = obj.getAttribute("height") || "0";
								if (obj.getAttribute("class")) {
									att.styleclass = obj.getAttribute("class")
								}
								if (obj.getAttribute("align")) {
									att.align = obj.getAttribute("align")
								}
								var par = {};
								var p = obj.getElementsByTagName("param");
								var pl = p.length;
								for (var j = 0; j < pl; j++) {
									if (p[j].getAttribute("name").toLowerCase() != "movie") {
										par[p[j].getAttribute("name")] = p[j].getAttribute("value")
									}
								}
								showExpressInstall(att, par, id, cb)
							} else {
								displayAltContent(obj);
								if (cb) {
									cb(cbObj)
								}
							}
						}
					}
				} else {
					setVisibility(id, true);
					if (cb) {
						var o = getObjectById(id);
						if (o && typeof o.SetVariable != UNDEF) {
							cbObj.success = true;
							cbObj.ref = o
						}
						cb(cbObj)
					}
				}
			}
		}
	}
	function getObjectById(objectIdStr) {
		var r = null;
		var o = getElementById(objectIdStr);
		if (o && o.nodeName == "OBJECT") {
			if (typeof o.SetVariable != UNDEF) {
				r = o
			} else {
				var n = o.getElementsByTagName(OBJECT)[0];
				if (n) {
					r = n
				}
			}
		}
		return r
	}
	function canExpressInstall() {
		return !isExpressInstallActive && hasPlayerVersion("6.0.65") && (ua.win || ua.mac) && !(ua.wk && ua.wk < 312)
	}
	function showExpressInstall(att, par, replaceElemIdStr, callbackFn) {
		isExpressInstallActive = true;
		storedCallbackFn = callbackFn || null;
		storedCallbackObj = {
			success : false,
			id : replaceElemIdStr
		};
		var obj = getElementById(replaceElemIdStr);
		if (obj) {
			if (obj.nodeName == "OBJECT") {
				storedAltContent = abstractAltContent(obj);
				storedAltContentId = null
			} else {
				storedAltContent = obj;
				storedAltContentId = replaceElemIdStr
			}
			att.id = EXPRESS_INSTALL_ID;
			if (typeof att.width == UNDEF || (!/%$/.test(att.width) && parseInt(att.width, 10) < 310)) {
				att.width = "310"
			}
			if (typeof att.height == UNDEF || (!/%$/.test(att.height) && parseInt(att.height, 10) < 137)) {
				att.height = "137"
			}
			doc.title = doc.title.slice(0, 47) + " - Flash Player Installation";
			var pt = ua.ie && ua.win ? "ActiveX" : "PlugIn",
			fv = "MMredirectURL=" + win.location.toString().replace(/&/g, "%26") + "&MMplayerType=" + pt + "&MMdoctitle=" + doc.title;
			if (typeof par.flashvars != UNDEF) {
				par.flashvars += "&" + fv
			} else {
				par.flashvars = fv
			}
			if (ua.ie && ua.win && obj.readyState != 4) {
				var newObj = createElement("div");
				replaceElemIdStr += "SWFObjectNew";
				newObj.setAttribute("id", replaceElemIdStr);
				obj.parentNode.insertBefore(newObj, obj);
				obj.style.display = "none";
				(function () {
					if (obj.readyState == 4) {
						obj.parentNode.removeChild(obj)
					} else {
						setTimeout(arguments.callee, 10)
					}
				})()
			}
			createSWF(att, par, replaceElemIdStr)
		}
	}
	function displayAltContent(obj) {
		if (ua.ie && ua.win && obj.readyState != 4) {
			var el = createElement("div");
			obj.parentNode.insertBefore(el, obj);
			el.parentNode.replaceChild(abstractAltContent(obj), el);
			obj.style.display = "none";
			(function () {
				if (obj.readyState == 4) {
					obj.parentNode.removeChild(obj)
				} else {
					setTimeout(arguments.callee, 10)
				}
			})()
		} else {
			obj.parentNode.replaceChild(abstractAltContent(obj), obj)
		}
	}
	function abstractAltContent(obj) {
		var ac = createElement("div");
		if (ua.win && ua.ie) {
			ac.innerHTML = obj.innerHTML
		} else {
			var nestedObj = obj.getElementsByTagName(OBJECT)[0];
			if (nestedObj) {
				var c = nestedObj.childNodes;
				if (c) {
					var cl = c.length;
					for (var i = 0; i < cl; i++) {
						if (!(c[i].nodeType == 1 && c[i].nodeName == "PARAM") && !(c[i].nodeType == 8)) {
							ac.appendChild(c[i].cloneNode(true))
						}
					}
				}
			}
		}
		return ac
	}
	function createSWF(attObj, parObj, id) {
		var r,
		el = getElementById(id);
		if (ua.wk && ua.wk < 312) {
			return r
		}
		if (el) {
			if (typeof attObj.id == UNDEF) {
				attObj.id = id
			}
			if (ua.ie && ua.win) {
				var att = "";
				for (var i in attObj) {
					if (attObj[i] != Object.prototype[i]) {
						if (i.toLowerCase() == "data") {
							parObj.movie = attObj[i]
						} else {
							if (i.toLowerCase() == "styleclass") {
								att += ' class="' + attObj[i] + '"'
							} else {
								if (i.toLowerCase() != "classid") {
									att += " " + i + '="' + attObj[i] + '"'
								}
							}
						}
					}
				}
				var par = "";
				for (var j in parObj) {
					if (parObj[j] != Object.prototype[j]) {
						par += '<param name="' + j + '" value="' + parObj[j] + '" />'
					}
				}
				el.outerHTML = '<object classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000"' + att + ">" + par + "</object>";
				objIdArr[objIdArr.length] = attObj.id;
				r = getElementById(attObj.id)
			} else {
				var o = createElement(OBJECT);
				o.setAttribute("type", FLASH_MIME_TYPE);
				for (var m in attObj) {
					if (attObj[m] != Object.prototype[m]) {
						if (m.toLowerCase() == "styleclass") {
							o.setAttribute("class", attObj[m])
						} else {
							if (m.toLowerCase() != "classid") {
								o.setAttribute(m, attObj[m])
							}
						}
					}
				}
				for (var n in parObj) {
					if (parObj[n] != Object.prototype[n] && n.toLowerCase() != "movie") {
						createObjParam(o, n, parObj[n])
					}
				}
				el.parentNode.replaceChild(o, el);
				r = o
			}
		}
		return r
	}
	function createObjParam(el, pName, pValue) {
		var p = createElement("param");
		p.setAttribute("name", pName);
		p.setAttribute("value", pValue);
		el.appendChild(p)
	}
	function removeSWF(id) {
		var obj = getElementById(id);
		if (obj && obj.nodeName == "OBJECT") {
			if (ua.ie && ua.win) {
				obj.style.display = "none";
				(function () {
					if (obj.readyState == 4) {
						removeObjectInIE(id)
					} else {
						setTimeout(arguments.callee, 10)
					}
				})()
			} else {
				obj.parentNode.removeChild(obj)
			}
		}
	}
	function removeObjectInIE(id) {
		var obj = getElementById(id);
		if (obj) {
			for (var i in obj) {
				if (typeof obj[i] == "function") {
					obj[i] = null
				}
			}
			obj.parentNode.removeChild(obj)
		}
	}
	function getElementById(id) {
		var el = null;
		try {
			el = doc.getElementById(id)
		} catch (e) {}
		
		return el
	}
	function createElement(el) {
		return doc.createElement(el)
	}
	function addListener(target, eventType, fn) {
		target.attachEvent(eventType, fn);
		listenersArr[listenersArr.length] = [target, eventType, fn]
	}
	function hasPlayerVersion(rv) {
		var pv = ua.pv,
		v = rv.split(".");
		v[0] = parseInt(v[0], 10);
		v[1] = parseInt(v[1], 10) || 0;
		v[2] = parseInt(v[2], 10) || 0;
		return (pv[0] > v[0] || (pv[0] == v[0] && pv[1] > v[1]) || (pv[0] == v[0] && pv[1] == v[1] && pv[2] >= v[2])) ? true : false
	}
	function createCSS(sel, decl, media, newStyle) {
		if (ua.ie && ua.mac) {
			return
		}
		var h = doc.getElementsByTagName("head")[0];
		if (!h) {
			return
		}
		var m = (media && typeof media == "string") ? media : "screen";
		if (newStyle) {
			dynamicStylesheet = null;
			dynamicStylesheetMedia = null
		}
		if (!dynamicStylesheet || dynamicStylesheetMedia != m) {
			var s = createElement("style");
			s.setAttribute("type", "text/css");
			s.setAttribute("media", m);
			dynamicStylesheet = h.appendChild(s);
			if (ua.ie && ua.win && typeof doc.styleSheets != UNDEF && doc.styleSheets.length > 0) {
				dynamicStylesheet = doc.styleSheets[doc.styleSheets.length - 1]
			}
			dynamicStylesheetMedia = m
		}
		if (ua.ie && ua.win) {
			if (dynamicStylesheet && typeof dynamicStylesheet.addRule == OBJECT) {
				dynamicStylesheet.addRule(sel, decl)
			}
		} else {
			if (dynamicStylesheet && typeof doc.createTextNode != UNDEF) {
				dynamicStylesheet.appendChild(doc.createTextNode(sel + " {" + decl + "}"))
			}
		}
	}
	function setVisibility(id, isVisible) {
		if (!autoHideShow) {
			return
		}
		var v = isVisible ? "visible" : "hidden";
		if (isDomLoaded && getElementById(id)) {
			getElementById(id).style.visibility = v
		} else {
			createCSS("#" + id, "visibility:" + v)
		}
	}
	function urlEncodeIfNecessary(s) {
		var regex = /[\\\"<>\.;]/;
		var hasBadChars = regex.exec(s) != null;
		return hasBadChars && typeof encodeURIComponent != UNDEF ? encodeURIComponent(s) : s
	}
	var cleanup = function () {
		if (ua.ie && ua.win) {
			window.attachEvent("onunload", function () {
				var ll = listenersArr.length;
				for (var i = 0; i < ll; i++) {
					listenersArr[i][0].detachEvent(listenersArr[i][1], listenersArr[i][2])
				}
				var il = objIdArr.length;
				for (var j = 0; j < il; j++) {
					removeSWF(objIdArr[j])
				}
				for (var k in ua) {
					ua[k] = null
				}
				ua = null;
				for (var l in swfobject) {
					swfobject[l] = null
				}
				swfobject = null
			})
		}
	}
	();
	return {
		registerObject : function (objectIdStr, swfVersionStr, xiSwfUrlStr, callbackFn) {
			if (ua.w3 && objectIdStr && swfVersionStr) {
				var regObj = {};
				regObj.id = objectIdStr;
				regObj.swfVersion = swfVersionStr;
				regObj.expressInstall = xiSwfUrlStr;
				regObj.callbackFn = callbackFn;
				regObjArr[regObjArr.length] = regObj;
				setVisibility(objectIdStr, false)
			} else {
				if (callbackFn) {
					callbackFn({
						success : false,
						id : objectIdStr
					})
				}
			}
		},
		getObjectById : function (objectIdStr) {
			if (ua.w3) {
				return getObjectById(objectIdStr)
			}
		},
		embedSWF : function (swfUrlStr, replaceElemIdStr, widthStr, heightStr, swfVersionStr, xiSwfUrlStr, flashvarsObj, parObj, attObj, callbackFn) {
			var callbackObj = {
				success : false,
				id : replaceElemIdStr
			};
			if (ua.w3 && !(ua.wk && ua.wk < 312) && swfUrlStr && replaceElemIdStr && widthStr && heightStr && swfVersionStr) {
				setVisibility(replaceElemIdStr, false);
				addDomLoadEvent(function () {
					widthStr += "";
					heightStr += "";
					var att = {};
					if (attObj && typeof attObj === OBJECT) {
						for (var i in attObj) {
							att[i] = attObj[i]
						}
					}
					att.data = swfUrlStr;
					att.width = widthStr;
					att.height = heightStr;
					var par = {};
					if (parObj && typeof parObj === OBJECT) {
						for (var j in parObj) {
							par[j] = parObj[j]
						}
					}
					if (flashvarsObj && typeof flashvarsObj === OBJECT) {
						for (var k in flashvarsObj) {
							if (typeof par.flashvars != UNDEF) {
								par.flashvars += "&" + k + "=" + flashvarsObj[k]
							} else {
								par.flashvars = k + "=" + flashvarsObj[k]
							}
						}
					}
					if (hasPlayerVersion(swfVersionStr)) {
						var obj = createSWF(att, par, replaceElemIdStr);
						if (att.id == replaceElemIdStr) {
							setVisibility(replaceElemIdStr, true)
						}
						callbackObj.success = true;
						callbackObj.ref = obj
					} else {
						if (xiSwfUrlStr && canExpressInstall()) {
							att.data = xiSwfUrlStr;
							showExpressInstall(att, par, replaceElemIdStr, callbackFn);
							return
						} else {
							setVisibility(replaceElemIdStr, true)
						}
					}
					if (callbackFn) {
						callbackFn(callbackObj)
					}
				})
			} else {
				if (callbackFn) {
					callbackFn(callbackObj)
				}
			}
		},
		switchOffAutoHideShow : function () {
			autoHideShow = false
		},
		ua : ua,
		getFlashPlayerVersion : function () {
			return {
				major : ua.pv[0],
				minor : ua.pv[1],
				release : ua.pv[2]
			}
		},
		hasFlashPlayerVersion : hasPlayerVersion,
		createSWF : function (attObj, parObj, replaceElemIdStr) {
			if (ua.w3) {
				return createSWF(attObj, parObj, replaceElemIdStr)
			} else {
				return undefined
			}
		},
		showExpressInstall : function (att, par, replaceElemIdStr, callbackFn) {
			if (ua.w3 && canExpressInstall()) {
				showExpressInstall(att, par, replaceElemIdStr, callbackFn)
			}
		},
		removeSWF : function (objElemIdStr) {
			if (ua.w3) {
				removeSWF(objElemIdStr)
			}
		},
		createCSS : function (selStr, declStr, mediaStr, newStyleBoolean) {
			if (ua.w3) {
				createCSS(selStr, declStr, mediaStr, newStyleBoolean)
			}
		},
		addDomLoadEvent : addDomLoadEvent,
		addLoadEvent : addLoadEvent,
		getQueryParamValue : function (param) {
			var q = doc.location.search || doc.location.hash;
			if (q) {
				if (/\?/.test(q)) {
					q = q.split("?")[1]
				}
				if (param == null) {
					return urlEncodeIfNecessary(q)
				}
				var pairs = q.split("&");
				for (var i = 0; i < pairs.length; i++) {
					if (pairs[i].substring(0, pairs[i].indexOf("=")) == param) {
						return urlEncodeIfNecessary(pairs[i].substring((pairs[i].indexOf("=") + 1)))
					}
				}
			}
			return ""
		},
		expressInstallCallback : function () {
			if (isExpressInstallActive) {
				var obj = getElementById(EXPRESS_INSTALL_ID);
				if (obj && storedAltContent) {
					obj.parentNode.replaceChild(storedAltContent, obj);
					if (storedAltContentId) {
						setVisibility(storedAltContentId, true);
						if (ua.ie && ua.win) {
							storedAltContent.style.display = "block"
						}
					}
					if (storedCallbackFn) {
						storedCallbackFn(storedCallbackObj)
					}
				}
				isExpressInstallActive = false
			}
		}
	}
}
();
var SWFUpload;
if (SWFUpload == undefined) {
	SWFUpload = function (settings) {
		this.initSWFUpload(settings)
	}
}
SWFUpload.prototype.initSWFUpload = function (settings) {
	try {
		this.customSettings = {};
		this.settings = settings;
		this.eventQueue = [];
		this.movieName = "SWFUpload_" + SWFUpload.movieCount++;
		this.movieElement = null;
		SWFUpload.instances[this.movieName] = this;
		this.initSettings();
		this.loadFlash();
		this.displayDebugInfo()
	} catch (ex) {
		delete SWFUpload.instances[this.movieName];
		throw ex
	}
};
SWFUpload.instances = {};
SWFUpload.movieCount = 0;
SWFUpload.version = "2.2.0 2009-03-25";
SWFUpload.QUEUE_ERROR = {
	QUEUE_LIMIT_EXCEEDED : -100,
	FILE_EXCEEDS_SIZE_LIMIT : -110,
	ZERO_BYTE_FILE : -120,
	INVALID_FILETYPE : -130
};
SWFUpload.UPLOAD_ERROR = {
	HTTP_ERROR : -200,
	MISSING_UPLOAD_URL : -210,
	IO_ERROR : -220,
	SECURITY_ERROR : -230,
	UPLOAD_LIMIT_EXCEEDED : -240,
	UPLOAD_FAILED : -250,
	SPECIFIED_FILE_ID_NOT_FOUND : -260,
	FILE_VALIDATION_FAILED : -270,
	FILE_CANCELLED : -280,
	UPLOAD_STOPPED : -290
};
SWFUpload.FILE_STATUS = {
	QUEUED : -1,
	IN_PROGRESS : -2,
	ERROR : -3,
	COMPLETE : -4,
	CANCELLED : -5
};
SWFUpload.BUTTON_ACTION = {
	SELECT_FILE : -100,
	SELECT_FILES : -110,
	START_UPLOAD : -120
};
SWFUpload.CURSOR = {
	ARROW : -1,
	HAND : -2
};
SWFUpload.WINDOW_MODE = {
	WINDOW : "window",
	TRANSPARENT : "transparent",
	OPAQUE : "opaque"
};
SWFUpload.completeURL = function (url) {
	if (typeof(url) !== "string" || url.match(/^https?:\/\//i) || url.match(/^\//)) {
		return url
	}
	var currentURL = window.location.protocol + "//" + window.location.hostname + (window.location.port ? ":" + window.location.port : "");
	var indexSlash = window.location.pathname.lastIndexOf("/");
	if (indexSlash <= 0) {
		path = "/"
	} else {
		path = window.location.pathname.substr(0, indexSlash) + "/"
	}
	return path + url
};
SWFUpload.prototype.initSettings = function () {
	this.ensureDefault = function (settingName, defaultValue) {
		this.settings[settingName] = (this.settings[settingName] == undefined) ? defaultValue : this.settings[settingName]
	};
	this.ensureDefault("upload_url", "");
	this.ensureDefault("preserve_relative_urls", false);
	this.ensureDefault("file_post_name", "Filedata");
	this.ensureDefault("post_params", {});
	this.ensureDefault("use_query_string", false);
	this.ensureDefault("requeue_on_error", false);
	this.ensureDefault("http_success", []);
	this.ensureDefault("assume_success_timeout", 0);
	this.ensureDefault("file_types", "*.*");
	this.ensureDefault("file_types_description", "All Files");
	this.ensureDefault("file_size_limit", 0);
	this.ensureDefault("file_upload_limit", 0);
	this.ensureDefault("file_queue_limit", 0);
	this.ensureDefault("flash_url", "swfupload.swf");
	this.ensureDefault("prevent_swf_caching", true);
	this.ensureDefault("button_image_url", "");
	this.ensureDefault("button_width", 1);
	this.ensureDefault("button_height", 1);
	this.ensureDefault("button_text", "");
	this.ensureDefault("button_text_style", "color: #000000; font-size: 16pt;");
	this.ensureDefault("button_text_top_padding", 0);
	this.ensureDefault("button_text_left_padding", 0);
	this.ensureDefault("button_action", SWFUpload.BUTTON_ACTION.SELECT_FILES);
	this.ensureDefault("button_disabled", false);
	this.ensureDefault("button_placeholder_id", "");
	this.ensureDefault("button_placeholder", null);
	this.ensureDefault("button_cursor", SWFUpload.CURSOR.ARROW);
	this.ensureDefault("button_window_mode", SWFUpload.WINDOW_MODE.WINDOW);
	this.ensureDefault("debug", false);
	this.settings.debug_enabled = this.settings.debug;
	this.settings.return_upload_start_handler = this.returnUploadStart;
	this.ensureDefault("swfupload_loaded_handler", null);
	this.ensureDefault("file_dialog_start_handler", null);
	this.ensureDefault("file_queued_handler", null);
	this.ensureDefault("file_queue_error_handler", null);
	this.ensureDefault("file_dialog_complete_handler", null);
	this.ensureDefault("upload_start_handler", null);
	this.ensureDefault("upload_progress_handler", null);
	this.ensureDefault("upload_error_handler", null);
	this.ensureDefault("upload_success_handler", null);
	this.ensureDefault("upload_complete_handler", null);
	this.ensureDefault("debug_handler", this.debugMessage);
	this.ensureDefault("custom_settings", {});
	this.customSettings = this.settings.custom_settings;
	if (!!this.settings.prevent_swf_caching) {
		this.settings.flash_url = this.settings.flash_url + (this.settings.flash_url.indexOf("?") < 0 ? "?" : "&") + "preventswfcaching=" + new Date().getTime()
	}
	if (!this.settings.preserve_relative_urls) {
		this.settings.upload_url = SWFUpload.completeURL(this.settings.upload_url);
		this.settings.button_image_url = SWFUpload.completeURL(this.settings.button_image_url)
	}
	delete this.ensureDefault
};
SWFUpload.prototype.loadFlash = function () {
	var targetElement,
	tempParent;
	if (document.getElementById(this.movieName) !== null) {
		throw "ID " + this.movieName + " is already in use. The Flash Object could not be added"
	}
	targetElement = document.getElementById(this.settings.button_placeholder_id) || this.settings.button_placeholder;
	if (targetElement == undefined) {
		throw "Could not find the placeholder element: " + this.settings.button_placeholder_id
	}
	tempParent = document.createElement("div");
	tempParent.innerHTML = this.getFlashHTML();
	targetElement.parentNode.replaceChild(tempParent.firstChild, targetElement);
	if (window[this.movieName] == undefined) {
		window[this.movieName] = this.getMovieElement()
	}
};
SWFUpload.prototype.getFlashHTML = function () {
	return ['<object id="', this.movieName, '" type="application/x-shockwave-flash" data="', this.settings.flash_url, '" width="', this.settings.button_width, '" height="', this.settings.button_height, '" class="swfupload">', '<param name="wmode" value="', this.settings.button_window_mode, '" />', '<param name="movie" value="', this.settings.flash_url, '" />', '<param name="quality" value="high" />', '<param name="menu" value="false" />', '<param name="allowScriptAccess" value="always" />', '<param name="flashvars" value="' + this.getFlashVars() + '" />', "</object>"].join("")
};
SWFUpload.prototype.getFlashVars = function () {
	var paramString = this.buildParamString();
	var httpSuccessString = this.settings.http_success.join(",");
	return ["movieName=", encodeURIComponent(this.movieName), "&amp;uploadURL=", encodeURIComponent(this.settings.upload_url), "&amp;useQueryString=", encodeURIComponent(this.settings.use_query_string), "&amp;requeueOnError=", encodeURIComponent(this.settings.requeue_on_error), "&amp;httpSuccess=", encodeURIComponent(httpSuccessString), "&amp;assumeSuccessTimeout=", encodeURIComponent(this.settings.assume_success_timeout), "&amp;params=", encodeURIComponent(paramString), "&amp;filePostName=", encodeURIComponent(this.settings.file_post_name), "&amp;fileTypes=", encodeURIComponent(this.settings.file_types), "&amp;fileTypesDescription=", encodeURIComponent(this.settings.file_types_description), "&amp;fileSizeLimit=", encodeURIComponent(this.settings.file_size_limit), "&amp;fileUploadLimit=", encodeURIComponent(this.settings.file_upload_limit), "&amp;fileQueueLimit=", encodeURIComponent(this.settings.file_queue_limit), "&amp;debugEnabled=", encodeURIComponent(this.settings.debug_enabled), "&amp;buttonImageURL=", encodeURIComponent(this.settings.button_image_url), "&amp;buttonWidth=", encodeURIComponent(this.settings.button_width), "&amp;buttonHeight=", encodeURIComponent(this.settings.button_height), "&amp;buttonText=", encodeURIComponent(this.settings.button_text), "&amp;buttonTextTopPadding=", encodeURIComponent(this.settings.button_text_top_padding), "&amp;buttonTextLeftPadding=", encodeURIComponent(this.settings.button_text_left_padding), "&amp;buttonTextStyle=", encodeURIComponent(this.settings.button_text_style), "&amp;buttonAction=", encodeURIComponent(this.settings.button_action), "&amp;buttonDisabled=", encodeURIComponent(this.settings.button_disabled), "&amp;buttonCursor=", encodeURIComponent(this.settings.button_cursor)].join("")
};
SWFUpload.prototype.getMovieElement = function () {
	if (this.movieElement == undefined) {
		this.movieElement = document.getElementById(this.movieName)
	}
	if (this.movieElement === null) {
		throw "Could not find Flash element"
	}
	return this.movieElement
};
SWFUpload.prototype.buildParamString = function () {
	var postParams = this.settings.post_params;
	var paramStringPairs = [];
	if (typeof(postParams) === "object") {
		for (var name in postParams) {
			if (postParams.hasOwnProperty(name)) {
				paramStringPairs.push(encodeURIComponent(name.toString()) + "=" + encodeURIComponent(postParams[name].toString()))
			}
		}
	}
	return paramStringPairs.join("&amp;")
};
SWFUpload.prototype.destroy = function () {
	try {
		this.cancelUpload(null, false);
		var movieElement = null;
		movieElement = this.getMovieElement();
		if (movieElement && typeof(movieElement.CallFunction) === "unknown") {
			for (var i in movieElement) {
				try {
					if (typeof(movieElement[i]) === "function") {
						movieElement[i] = null
					}
				} catch (ex1) {}
				
			}
			try {
				movieElement.parentNode.removeChild(movieElement)
			} catch (ex) {}
			
		}
		window[this.movieName] = null;
		SWFUpload.instances[this.movieName] = null;
		delete SWFUpload.instances[this.movieName];
		this.movieElement = null;
		this.settings = null;
		this.customSettings = null;
		this.eventQueue = null;
		this.movieName = null;
		return true
	} catch (ex2) {
		return false
	}
};
SWFUpload.prototype.displayDebugInfo = function () {
	this.debug(["---SWFUpload Instance Info---\n", "Version: ", SWFUpload.version, "\n", "Movie Name: ", this.movieName, "\n", "Settings:\n", "\t", "upload_url:               ", this.settings.upload_url, "\n", "\t", "flash_url:                ", this.settings.flash_url, "\n", "\t", "use_query_string:         ", this.settings.use_query_string.toString(), "\n", "\t", "requeue_on_error:         ", this.settings.requeue_on_error.toString(), "\n", "\t", "http_success:             ", this.settings.http_success.join(", "), "\n", "\t", "assume_success_timeout:   ", this.settings.assume_success_timeout, "\n", "\t", "file_post_name:           ", this.settings.file_post_name, "\n", "\t", "post_params:              ", this.settings.post_params.toString(), "\n", "\t", "file_types:               ", this.settings.file_types, "\n", "\t", "file_types_description:   ", this.settings.file_types_description, "\n", "\t", "file_size_limit:          ", this.settings.file_size_limit, "\n", "\t", "file_upload_limit:        ", this.settings.file_upload_limit, "\n", "\t", "file_queue_limit:         ", this.settings.file_queue_limit, "\n", "\t", "debug:                    ", this.settings.debug.toString(), "\n", "\t", "prevent_swf_caching:      ", this.settings.prevent_swf_caching.toString(), "\n", "\t", "button_placeholder_id:    ", this.settings.button_placeholder_id.toString(), "\n", "\t", "button_placeholder:       ", (this.settings.button_placeholder ? "Set" : "Not Set"), "\n", "\t", "button_image_url:         ", this.settings.button_image_url.toString(), "\n", "\t", "button_width:             ", this.settings.button_width.toString(), "\n", "\t", "button_height:            ", this.settings.button_height.toString(), "\n", "\t", "button_text:              ", this.settings.button_text.toString(), "\n", "\t", "button_text_style:        ", this.settings.button_text_style.toString(), "\n", "\t", "button_text_top_padding:  ", this.settings.button_text_top_padding.toString(), "\n", "\t", "button_text_left_padding: ", this.settings.button_text_left_padding.toString(), "\n", "\t", "button_action:            ", this.settings.button_action.toString(), "\n", "\t", "button_disabled:          ", this.settings.button_disabled.toString(), "\n", "\t", "custom_settings:          ", this.settings.custom_settings.toString(), "\n", "Event Handlers:\n", "\t", "swfupload_loaded_handler assigned:  ", (typeof this.settings.swfupload_loaded_handler === "function").toString(), "\n", "\t", "file_dialog_start_handler assigned: ", (typeof this.settings.file_dialog_start_handler === "function").toString(), "\n", "\t", "file_queued_handler assigned:       ", (typeof this.settings.file_queued_handler === "function").toString(), "\n", "\t", "file_queue_error_handler assigned:  ", (typeof this.settings.file_queue_error_handler === "function").toString(), "\n", "\t", "upload_start_handler assigned:      ", (typeof this.settings.upload_start_handler === "function").toString(), "\n", "\t", "upload_progress_handler assigned:   ", (typeof this.settings.upload_progress_handler === "function").toString(), "\n", "\t", "upload_error_handler assigned:      ", (typeof this.settings.upload_error_handler === "function").toString(), "\n", "\t", "upload_success_handler assigned:    ", (typeof this.settings.upload_success_handler === "function").toString(), "\n", "\t", "upload_complete_handler assigned:   ", (typeof this.settings.upload_complete_handler === "function").toString(), "\n", "\t", "debug_handler assigned:             ", (typeof this.settings.debug_handler === "function").toString(), "\n"].join(""))
};
SWFUpload.prototype.addSetting = function (name, value, default_value) {
	if (value == undefined) {
		return (this.settings[name] = default_value)
	} else {
		return (this.settings[name] = value)
	}
};
SWFUpload.prototype.getSetting = function (name) {
	if (this.settings[name] != undefined) {
		return this.settings[name]
	}
	return ""
};
SWFUpload.prototype.callFlash = function (functionName, argumentArray) {
	argumentArray = argumentArray || [];
	var movieElement = this.getMovieElement();
	var returnValue,
	returnString;
	try {
		returnString = movieElement.CallFunction('<invoke name="' + functionName + '" returntype="javascript">' + __flash__argumentsToXML(argumentArray, 0) + "</invoke>");
		returnValue = eval(returnString)
	} catch (ex) {
		throw "Call to " + functionName + " failed"
	}
	if (returnValue != undefined && typeof returnValue.post === "object") {
		returnValue = this.unescapeFilePostParams(returnValue)
	}
	return returnValue
};
SWFUpload.prototype.selectFile = function () {
	this.callFlash("SelectFile")
};
SWFUpload.prototype.selectFiles = function () {
	this.callFlash("SelectFiles")
};
SWFUpload.prototype.startUpload = function (fileID) {
	this.callFlash("StartUpload", [fileID])
};
SWFUpload.prototype.cancelUpload = function (fileID, triggerErrorEvent) {
	if (triggerErrorEvent !== false) {
		triggerErrorEvent = true
	}
	this.callFlash("CancelUpload", [fileID, triggerErrorEvent])
};
SWFUpload.prototype.stopUpload = function () {
	this.callFlash("StopUpload")
};
SWFUpload.prototype.getStats = function () {
	return this.callFlash("GetStats")
};
SWFUpload.prototype.setStats = function (statsObject) {
	this.callFlash("SetStats", [statsObject])
};
SWFUpload.prototype.getFile = function (fileID) {
	if (typeof(fileID) === "number") {
		return this.callFlash("GetFileByIndex", [fileID])
	} else {
		return this.callFlash("GetFile", [fileID])
	}
};
SWFUpload.prototype.addFileParam = function (fileID, name, value) {
	return this.callFlash("AddFileParam", [fileID, name, value])
};
SWFUpload.prototype.removeFileParam = function (fileID, name) {
	this.callFlash("RemoveFileParam", [fileID, name])
};
SWFUpload.prototype.setUploadURL = function (url) {
	this.settings.upload_url = url.toString();
	this.callFlash("SetUploadURL", [url])
};
SWFUpload.prototype.setPostParams = function (paramsObject) {
	this.settings.post_params = paramsObject;
	this.callFlash("SetPostParams", [paramsObject])
};
SWFUpload.prototype.addPostParam = function (name, value) {
	this.settings.post_params[name] = value;
	this.callFlash("SetPostParams", [this.settings.post_params])
};
SWFUpload.prototype.removePostParam = function (name) {
	delete this.settings.post_params[name];
	this.callFlash("SetPostParams", [this.settings.post_params])
};
SWFUpload.prototype.setFileTypes = function (types, description) {
	this.settings.file_types = types;
	this.settings.file_types_description = description;
	this.callFlash("SetFileTypes", [types, description])
};
SWFUpload.prototype.setFileSizeLimit = function (fileSizeLimit) {
	this.settings.file_size_limit = fileSizeLimit;
	this.callFlash("SetFileSizeLimit", [fileSizeLimit])
};
SWFUpload.prototype.setFileUploadLimit = function (fileUploadLimit) {
	this.settings.file_upload_limit = fileUploadLimit;
	this.callFlash("SetFileUploadLimit", [fileUploadLimit])
};
SWFUpload.prototype.setFileQueueLimit = function (fileQueueLimit) {
	this.settings.file_queue_limit = fileQueueLimit;
	this.callFlash("SetFileQueueLimit", [fileQueueLimit])
};
SWFUpload.prototype.setFilePostName = function (filePostName) {
	this.settings.file_post_name = filePostName;
	this.callFlash("SetFilePostName", [filePostName])
};
SWFUpload.prototype.setUseQueryString = function (useQueryString) {
	this.settings.use_query_string = useQueryString;
	this.callFlash("SetUseQueryString", [useQueryString])
};
SWFUpload.prototype.setRequeueOnError = function (requeueOnError) {
	this.settings.requeue_on_error = requeueOnError;
	this.callFlash("SetRequeueOnError", [requeueOnError])
};
SWFUpload.prototype.setHTTPSuccess = function (http_status_codes) {
	if (typeof http_status_codes === "string") {
		http_status_codes = http_status_codes.replace(" ", "").split(",")
	}
	this.settings.http_success = http_status_codes;
	this.callFlash("SetHTTPSuccess", [http_status_codes])
};
SWFUpload.prototype.setAssumeSuccessTimeout = function (timeout_seconds) {
	this.settings.assume_success_timeout = timeout_seconds;
	this.callFlash("SetAssumeSuccessTimeout", [timeout_seconds])
};
SWFUpload.prototype.setDebugEnabled = function (debugEnabled) {
	this.settings.debug_enabled = debugEnabled;
	this.callFlash("SetDebugEnabled", [debugEnabled])
};
SWFUpload.prototype.setButtonImageURL = function (buttonImageURL) {
	if (buttonImageURL == undefined) {
		buttonImageURL = ""
	}
	this.settings.button_image_url = buttonImageURL;
	this.callFlash("SetButtonImageURL", [buttonImageURL])
};
SWFUpload.prototype.setButtonDimensions = function (width, height) {
	this.settings.button_width = width;
	this.settings.button_height = height;
	var movie = this.getMovieElement();
	if (movie != undefined) {
		movie.style.width = width + "px";
		movie.style.height = height + "px"
	}
	this.callFlash("SetButtonDimensions", [width, height])
};
SWFUpload.prototype.setButtonText = function (html) {
	this.settings.button_text = html;
	this.callFlash("SetButtonText", [html])
};
SWFUpload.prototype.setButtonTextPadding = function (left, top) {
	this.settings.button_text_top_padding = top;
	this.settings.button_text_left_padding = left;
	this.callFlash("SetButtonTextPadding", [left, top])
};
SWFUpload.prototype.setButtonTextStyle = function (css) {
	this.settings.button_text_style = css;
	this.callFlash("SetButtonTextStyle", [css])
};
SWFUpload.prototype.setButtonDisabled = function (isDisabled) {
	this.settings.button_disabled = isDisabled;
	this.callFlash("SetButtonDisabled", [isDisabled])
};
SWFUpload.prototype.setButtonAction = function (buttonAction) {
	this.settings.button_action = buttonAction;
	this.callFlash("SetButtonAction", [buttonAction])
};
SWFUpload.prototype.setButtonCursor = function (cursor) {
	this.settings.button_cursor = cursor;
	this.callFlash("SetButtonCursor", [cursor])
};
SWFUpload.prototype.queueEvent = function (handlerName, argumentArray) {
	if (argumentArray == undefined) {
		argumentArray = []
	} else {
		if (!(argumentArray instanceof Array)) {
			argumentArray = [argumentArray]
		}
	}
	var self = this;
	if (typeof this.settings[handlerName] === "function") {
		this.eventQueue.push(function () {
			this.settings[handlerName].apply(this, argumentArray)
		});
		setTimeout(function () {
			self.executeNextEvent()
		}, 0)
	} else {
		if (this.settings[handlerName] !== null) {
			throw "Event handler " + handlerName + " is unknown or is not a function"
		}
	}
};
SWFUpload.prototype.executeNextEvent = function () {
	var f = this.eventQueue ? this.eventQueue.shift() : null;
	if (typeof(f) === "function") {
		f.apply(this)
	}
};
SWFUpload.prototype.unescapeFilePostParams = function (file) {
	var reg = /[$]([0-9a-f]{4})/i;
	var unescapedPost = {};
	var uk;
	if (file != undefined) {
		for (var k in file.post) {
			if (file.post.hasOwnProperty(k)) {
				uk = k;
				var match;
				while ((match = reg.exec(uk)) !== null) {
					uk = uk.replace(match[0], String.fromCharCode(parseInt("0x" + match[1], 16)))
				}
				unescapedPost[uk] = file.post[k]
			}
		}
		file.post = unescapedPost
	}
	return file
};
SWFUpload.prototype.testExternalInterface = function () {
	try {
		return this.callFlash("TestExternalInterface")
	} catch (ex) {
		return false
	}
};
SWFUpload.prototype.flashReady = function () {
	var movieElement = this.getMovieElement();
	if (!movieElement) {
		this.debug("Flash called back ready but the flash movie can't be found.");
		return
	}
	this.cleanUp(movieElement);
	this.queueEvent("swfupload_loaded_handler")
};
SWFUpload.prototype.cleanUp = function (movieElement) {
	try {
		if (this.movieElement && typeof(movieElement.CallFunction) === "unknown") {
			this.debug("Removing Flash functions hooks (this should only run in IE and should prevent memory leaks)");
			for (var key in movieElement) {
				try {
					if (typeof(movieElement[key]) === "function") {
						movieElement[key] = null
					}
				} catch (ex) {}
				
			}
		}
	} catch (ex1) {}
	
	window.__flash__removeCallback = function (instance, name) {
		try {
			if (instance) {
				instance[name] = null
			}
		} catch (flashEx) {}
		
	}
};
SWFUpload.prototype.fileDialogStart = function () {
	this.queueEvent("file_dialog_start_handler")
};
SWFUpload.prototype.fileQueued = function (file) {
	file = this.unescapeFilePostParams(file);
	this.queueEvent("file_queued_handler", file)
};
SWFUpload.prototype.fileQueueError = function (file, errorCode, message) {
	file = this.unescapeFilePostParams(file);
	this.queueEvent("file_queue_error_handler", [file, errorCode, message])
};
SWFUpload.prototype.fileDialogComplete = function (numFilesSelected, numFilesQueued, numFilesInQueue) {
	this.queueEvent("file_dialog_complete_handler", [numFilesSelected, numFilesQueued, numFilesInQueue])
};
SWFUpload.prototype.uploadStart = function (file) {
	file = this.unescapeFilePostParams(file);
	this.queueEvent("return_upload_start_handler", file)
};
SWFUpload.prototype.returnUploadStart = function (file) {
	var returnValue;
	if (typeof this.settings.upload_start_handler === "function") {
		file = this.unescapeFilePostParams(file);
		returnValue = this.settings.upload_start_handler.call(this, file)
	} else {
		if (this.settings.upload_start_handler != undefined) {
			throw "upload_start_handler must be a function"
		}
	}
	if (returnValue === undefined) {
		returnValue = true
	}
	returnValue = !!returnValue;
	this.callFlash("ReturnUploadStart", [returnValue])
};
SWFUpload.prototype.uploadProgress = function (file, bytesComplete, bytesTotal) {
	file = this.unescapeFilePostParams(file);
	this.queueEvent("upload_progress_handler", [file, bytesComplete, bytesTotal])
};
SWFUpload.prototype.uploadError = function (file, errorCode, message) {
	file = this.unescapeFilePostParams(file);
	this.queueEvent("upload_error_handler", [file, errorCode, message])
};
SWFUpload.prototype.uploadSuccess = function (file, serverData, responseReceived) {
	file = this.unescapeFilePostParams(file);
	this.queueEvent("upload_success_handler", [file, serverData, responseReceived])
};
SWFUpload.prototype.uploadComplete = function (file) {
	file = this.unescapeFilePostParams(file);
	this.queueEvent("upload_complete_handler", file)
};
SWFUpload.prototype.debug = function (message) {
	this.queueEvent("debug_handler", message)
};
SWFUpload.prototype.debugMessage = function (message) {
	if (this.settings.debug) {
		var exceptionMessage,
		exceptionValues = [];
		if (typeof message === "object" && typeof message.name === "string" && typeof message.message === "string") {
			for (var key in message) {
				if (message.hasOwnProperty(key)) {
					exceptionValues.push(key + ": " + message[key])
				}
			}
			exceptionMessage = exceptionValues.join("\n") || "";
			exceptionValues = exceptionMessage.split("\n");
			exceptionMessage = "EXCEPTION: " + exceptionValues.join("\nEXCEPTION: ");
			SWFUpload.Console.writeLine(exceptionMessage)
		} else {
			SWFUpload.Console.writeLine(message)
		}
	}
};
SWFUpload.Console = {};
SWFUpload.Console.writeLine = function (message) {
	var console,
	documentForm;
	try {
		console = document.getElementById("SWFUpload_Console");
		if (!console) {
			documentForm = document.createElement("form");
			document.getElementsByTagName("body")[0].appendChild(documentForm);
			console = document.createElement("textarea");
			console.id = "SWFUpload_Console";
			console.style.fontFamily = "monospace";
			console.setAttribute("wrap", "off");
			console.wrap = "off";
			console.style.overflow = "auto";
			console.style.width = "700px";
			console.style.height = "350px";
			console.style.margin = "5px";
			documentForm.appendChild(console)
		}
		console.value += message + "\n";
		console.scrollTop = console.scrollHeight - console.clientHeight
	} catch (ex) {
		alert("Exception: " + ex.name + " Message: " + ex.message)
	}
};
var fluid_1_4 = fluid_1_4 || {};
(function ($, fluid) {
	var fileOrFiles = function (that, numFiles) {
		return (numFiles === 1) ? that.options.strings.progress.singleFile : that.options.strings.progress.pluralFiles
	};
	var enableElement = function (that, elm) {
		elm.prop("disabled", false);
		elm.removeClass(that.options.styles.dim)
	};
	var disableElement = function (that, elm) {
		elm.prop("disabled", true);
		elm.addClass(that.options.styles.dim)
	};
	var showElement = function (that, elm) {
		elm.removeClass(that.options.styles.hidden)
	};
	var hideElement = function (that, elm) {
		elm.addClass(that.options.styles.hidden)
	};
	var maxFilesUploaded = function (that) {
		var fileUploadLimit = that.queue.getUploadedFiles().length + that.queue.getReadyFiles().length + that.queue.getErroredFiles().length;
		return (fileUploadLimit === that.options.queueSettings.fileUploadLimit)
	};
	var setTotalProgressStyle = function (that, didError) {
		didError = didError || false;
		var indicator = that.totalProgress.indicator;
		indicator.toggleClass(that.options.styles.totalProgress, !didError);
		indicator.toggleClass(that.options.styles.totalProgressError, didError)
	};
	var setStateEmpty = function (that) {
		disableElement(that, that.locate("uploadButton"));
		if (that.queue.files.length === 0) {
			that.locate("browseButtonText").text(that.options.strings.buttons.browse);
			that.locate("browseButton").removeClass(that.options.styles.browseButton);
			showElement(that, that.locate("instructions"))
		}
	};
	var enableBrowseButton = function (that) {
		if (!maxFilesUploaded(that)) {
			enableElement(that, that.locate("browseButton"));
			that.strategy.local.enableBrowseButton()
		}
	};
	var setStateDone = function (that) {
		disableElement(that, that.locate("uploadButton"));
		hideElement(that, that.locate("pauseButton"));
		showElement(that, that.locate("uploadButton"));
		enableBrowseButton(that)
	};
	var setStateLoaded = function (that) {
		that.locate("browseButtonText").text(that.options.strings.buttons.addMore);
		that.locate("browseButton").addClass(that.options.styles.browseButton);
		hideElement(that, that.locate("pauseButton"));
		showElement(that, that.locate("uploadButton"));
		enableElement(that, that.locate("uploadButton"));
		hideElement(that, that.locate("instructions"));
		that.totalProgress.hide();
		enableBrowseButton(that)
	};
	var setStateUploading = function (that) {
		that.totalProgress.hide(false, false);
		setTotalProgressStyle(that);
		hideElement(that, that.locate("uploadButton"));
		disableElement(that, that.locate("browseButton"));
		that.strategy.local.disableBrowseButton();
		enableElement(that, that.locate("pauseButton"));
		showElement(that, that.locate("pauseButton"));
		that.locate(that.options.focusWithEvent.afterUploadStart).focus()
	};
	var setStateFull = function (that) {
		that.locate("browseButtonText").text(that.options.strings.buttons.addMore);
		that.locate("browseButton").addClass(that.options.styles.browseButton);
		hideElement(that, that.locate("pauseButton"));
		showElement(that, that.locate("uploadButton"));
		enableElement(that, that.locate("uploadButton"));
		disableElement(that, that.locate("browseButton"));
		that.strategy.local.disableBrowseButton();
		hideElement(that, that.locate("instructions"));
		that.totalProgress.hide()
	};
	var renderUploadTotalMessage = function (that) {
		var numReadyFiles = that.queue.getReadyFiles().length;
		var bytesReadyFiles = that.queue.sizeOfReadyFiles();
		var fileLabelStr = fileOrFiles(that, numReadyFiles);
		var totalStateStr = fluid.stringTemplate(that.options.strings.progress.toUploadLabel, {
				fileCount : numReadyFiles,
				fileLabel : fileLabelStr,
				totalBytes : fluid.uploader.formatFileSize(bytesReadyFiles)
			});
		that.locate("totalFileStatusText").html(totalStateStr)
	};
	var renderFileUploadLimit = function (that) {
		if (that.options.queueSettings.fileUploadLimit > 0) {
			var fileUploadLimitText = fluid.stringTemplate(that.options.strings.progress.fileUploadLimitLabel, {
					fileUploadLimit : that.options.queueSettings.fileUploadLimit,
					fileLabel : fileOrFiles(that, that.options.queueSettings.fileUploadLimit)
				});
			that.locate("fileUploadLimitText").html(fileUploadLimitText)
		}
	};
	var updateTotalProgress = function (that) {
		var batch = that.queue.currentBatch;
		var totalPercent = fluid.uploader.derivePercent(batch.totalBytesUploaded, batch.totalBytes);
		var numFilesInBatch = batch.files.length;
		var fileLabelStr = fileOrFiles(that, numFilesInBatch);
		var totalProgressStr = fluid.stringTemplate(that.options.strings.progress.totalProgressLabel, {
				curFileN : batch.fileIdx,
				totalFilesN : numFilesInBatch,
				fileLabel : fileLabelStr,
				currBytes : fluid.uploader.formatFileSize(batch.totalBytesUploaded),
				totalBytes : fluid.uploader.formatFileSize(batch.totalBytes)
			});
		that.totalProgress.update(totalPercent, totalProgressStr)
	};
	var updateTotalAtCompletion = function (that) {
		var numErroredFiles = that.queue.getErroredFiles().length;
		var numTotalFiles = that.queue.files.length;
		var fileLabelStr = fileOrFiles(that, numTotalFiles);
		var errorStr = "";
		if (numErroredFiles > 0) {
			var errorLabelString = (numErroredFiles === 1) ? that.options.strings.progress.singleError : that.options.strings.progress.pluralErrors;
			setTotalProgressStyle(that, true);
			errorStr = fluid.stringTemplate(that.options.strings.progress.numberOfErrors, {
					errorsN : numErroredFiles,
					errorLabel : errorLabelString
				})
		}
		var totalProgressStr = fluid.stringTemplate(that.options.strings.progress.completedLabel, {
				curFileN : that.queue.getUploadedFiles().length,
				totalFilesN : numTotalFiles,
				errorString : errorStr,
				fileLabel : fileLabelStr,
				totalCurrBytes : fluid.uploader.formatFileSize(that.queue.sizeOfUploadedFiles())
			});
		that.totalProgress.update(100, totalProgressStr)
	};
	var updateQueueSummaryText = function (that) {
		var fileQueueTable = that.locate("fileQueue");
		if (that.queue.files.length === 0) {
			fileQueueTable.attr("summary", that.options.strings.queue.emptyQueue)
		} else {
			var queueSummary = fluid.stringTemplate(that.options.strings.queue.queueSummary, {
					totalUploaded : that.queue.getUploadedFiles().length,
					totalInUploadQueue : that.queue.files.length - that.queue.getUploadedFiles().length
				});
			fileQueueTable.attr("summary", queueSummary)
		}
	};
	var bindDOMEvents = function (that) {
		that.locate("uploadButton").click(function () {
			that.start()
		});
		that.locate("pauseButton").click(function () {
			that.stop()
		})
	};
	var updateStateAfterFileDialog = function (that) {
		var queueLength = that.queue.getReadyFiles().length;
		if (queueLength > 0) {
			if (queueLength === that.options.queueSettings.fileUploadLimit) {
				setStateFull(that)
			} else {
				setStateLoaded(that)
			}
			renderUploadTotalMessage(that);
			that.locate(that.options.focusWithEvent.afterFileDialog).focus();
			updateQueueSummaryText(that)
		}
	};
	var updateStateAfterFileRemoval = function (that) {
		if (that.queue.getReadyFiles().length === 0) {
			setStateEmpty(that)
		} else {
			setStateLoaded(that)
		}
		renderUploadTotalMessage(that);
		updateQueueSummaryText(that)
	};
	var updateStateAfterCompletion = function (that) {
		if (that.queue.getReadyFiles().length === 0) {
			setStateDone(that)
		} else {
			setStateLoaded(that)
		}
		updateTotalAtCompletion(that);
		updateQueueSummaryText(that)
	};
	var uploadNextOrFinish = function (that) {
		if (that.queue.shouldUploadNextFile()) {
			that.strategy.remote.uploadNextFile()
		} else {
			that.events.afterUploadComplete.fire(that.queue.currentBatch.files);
			that.queue.clearCurrentBatch()
		}
	};
	var bindEvents = function (that) {
		that.events.afterFileDialog.addListener(function () {
			updateStateAfterFileDialog(that)
		});
		that.events.afterFileQueued.addListener(function (file) {
			that.queue.addFile(file)
		});
		that.events.onFileRemoved.addListener(function (file) {
			that.removeFile(file)
		});
		that.events.afterFileRemoved.addListener(function () {
			updateStateAfterFileRemoval(that)
		});
		that.events.onUploadStart.addListener(function () {
			setStateUploading(that)
		});
		that.events.onUploadStop.addListener(function () {
			that.locate(that.options.focusWithEvent.onUploadStop).focus()
		});
		that.events.onFileStart.addListener(function (file) {
			file.filestatus = fluid.uploader.fileStatusConstants.IN_PROGRESS;
			that.queue.startFile()
		});
		that.events.onFileProgress.addListener(function (file, currentBytes, totalBytes) {
			that.queue.updateBatchStatus(currentBytes);
			updateTotalProgress(that)
		});
		that.events.onFileComplete.addListener(function (file) {
			that.queue.finishFile(file);
			that.events.afterFileComplete.fire(file);
			uploadNextOrFinish(that)
		});
		that.events.onFileSuccess.addListener(function (file) {
			file.filestatus = fluid.uploader.fileStatusConstants.COMPLETE;
			if (that.queue.currentBatch.bytesUploadedForFile === 0) {
				that.queue.currentBatch.totalBytesUploaded += file.size
			}
			updateTotalProgress(that)
		});
		that.events.onFileError.addListener(function (file, error) {
			if (error === fluid.uploader.errorConstants.UPLOAD_STOPPED) {
				file.filestatus = fluid.uploader.fileStatusConstants.CANCELLED;
				return
			} else {
				file.filestatus = fluid.uploader.fileStatusConstants.ERROR;
				if (that.queue.isUploading) {
					that.queue.currentBatch.totalBytesUploaded += file.size;
					that.queue.currentBatch.numFilesErrored++;
					uploadNextOrFinish(that)
				}
			}
		});
		that.events.afterUploadComplete.addListener(function () {
			that.queue.isUploading = false;
			updateStateAfterCompletion(that)
		})
	};
	var setupUploader = function (that) {
		that.demo = fluid.typeTag(that.options.demo ? "fluid.uploader.demo" : "fluid.uploader.live");
		fluid.initDependents(that);
		disableElement(that, that.locate("uploadButton"));
		bindDOMEvents(that);
		bindEvents(that);
		updateQueueSummaryText(that);
		that.statusUpdater();
		renderFileUploadLimit(that);
		that.container.attr("role", "application")
	};
	fluid.uploader = function (container, uploaderOptions) {
		var that = fluid.initView("fluid.uploader", container);
		if (fluid.uploader.transformOptions) {
			uploaderOptions = fluid.uploader.transformOptions(uploaderOptions)
		}
		that.uploaderOptions = uploaderOptions;
		fluid.initDependents(that);
		return that.uploaderImpl
	};
	fluid.uploaderImpl = function () {
		fluid.fail('Error creating uploader component - please make sure that a progressiveCheckerForComponent for "fluid.uploader" is registered either in the static environment or else is visible in the current component tree')
	};
	fluid.defaults("fluid.uploader", {
		gradeNames : ["fluid.viewComponent"],
		components : {
			uploaderContext : {
				type : "fluid.progressiveCheckerForComponent",
				options : {
					componentName : "fluid.uploader"
				}
			},
			uploaderImpl : {
				type : "fluid.uploaderImpl",
				container : "{uploader}.container",
				options : "{uploader}.uploaderOptions"
			}
		},
		progressiveCheckerOptions : {
			checks : [{
					feature : "{fluid.browser.supportsBinaryXHR}",
					contextName : "fluid.uploader.html5"
				}, {
					feature : "{fluid.browser.supportsFlash}",
					contextName : "fluid.uploader.swfUpload"
				}
			],
			defaultContextName : "fluid.uploader.singleFile"
		}
	});
	fluid.alias("fluid.uploader", "fluid.uploaderImpl");
	fluid.progressiveEnhanceableUploader = function (container, enhanceable, options) {
		return fluid.uploader(container, options)
	};
	fluid.uploader.multiFileUploader = function (container, options) {
		var that = fluid.initView("fluid.uploader.multiFileUploader", container, options);
		that.queue = fluid.uploader.fileQueue();
		that.browse = function () {
			if (!that.queue.isUploading) {
				that.strategy.local.browse()
			}
		};
		that.removeFile = function (file) {
			that.queue.removeFile(file);
			that.strategy.local.removeFile(file);
			that.events.afterFileRemoved.fire(file)
		};
		that.start = function () {
			that.queue.start();
			that.events.onUploadStart.fire(that.queue.currentBatch.files);
			that.strategy.remote.uploadNextFile()
		};
		that.stop = function () {
			that.events.onUploadStop.fire();
			that.strategy.remote.stop()
		};
		setupUploader(that);
		return that
	};
	fluid.defaults("fluid.uploader.multiFileUploader", {
		gradeNames : "fluid.viewComponent",
		components : {
			strategy : {
				type : "fluid.uploader.progressiveStrategy"
			},
			errorPanel : {
				type : "fluid.uploader.errorPanel"
			},
			fileQueueView : {
				type : "fluid.uploader.fileQueueView",
				options : {
					model : "{multiFileUploader}.queue.files",
					uploaderContainer : "{multiFileUploader}.container"
				}
			},
			totalProgress : {
				type : "fluid.uploader.totalProgressBar",
				options : {
					selectors : {
						progressBar : ".flc-uploader-queue-footer",
						displayElement : ".flc-uploader-total-progress",
						label : ".flc-uploader-total-progress-text",
						indicator : ".flc-uploader-total-progress",
						ariaElement : ".flc-uploader-total-progress"
					}
				}
			}
		},
		invokers : {
			statusUpdater : "fluid.uploader.ariaLiveRegionUpdater"
		},
		queueSettings : {
			uploadURL : "",
			postParams : {},
			fileSizeLimit : "20480",
			fileTypes : null,
			fileTypesDescription : null,
			fileUploadLimit : 0,
			fileQueueLimit : 0
		},
		demo : false,
		selectors : {
			fileQueue : ".flc-uploader-queue",
			browseButton : ".flc-uploader-button-browse",
			browseButtonText : ".flc-uploader-button-browse-text",
			uploadButton : ".flc-uploader-button-upload",
			pauseButton : ".flc-uploader-button-pause",
			totalFileStatusText : ".flc-uploader-total-progress-text",
			fileUploadLimitText : ".flc-uploader-upload-limit-text",
			instructions : ".flc-uploader-browse-instructions",
			statusRegion : ".flc-uploader-status-region",
			errorsPanel : ".flc-uploader-errorsPanel"
		},
		focusWithEvent : {
			afterFileDialog : "uploadButton",
			afterUploadStart : "pauseButton",
			onUploadStop : "uploadButton"
		},
		styles : {
			disabled : "fl-uploader-disabled",
			hidden : "fl-uploader-hidden",
			dim : "fl-uploader-dim",
			totalProgress : "fl-uploader-total-progress-okay",
			totalProgressError : "fl-uploader-total-progress-errored",
			browseButton : "fl-uploader-browseMore"
		},
		events : {
			afterReady : null,
			onFileDialog : null,
			onFilesSelected : null,
			onFileQueued : null,
			afterFileQueued : null,
			onFileRemoved : null,
			afterFileRemoved : null,
			afterFileDialog : null,
			onUploadStart : null,
			onUploadStop : null,
			onFileStart : null,
			onFileProgress : null,
			onFileError : null,
			onQueueError : null,
			onFileSuccess : null,
			onFileComplete : null,
			afterFileComplete : null,
			afterUploadComplete : null
		},
		strings : {
			progress : {
				fileUploadLimitLabel : "%fileUploadLimit %fileLabel maximum",
				toUploadLabel : "To upload: %fileCount %fileLabel (%totalBytes)",
				totalProgressLabel : "Uploading: %curFileN of %totalFilesN %fileLabel (%currBytes of %totalBytes)",
				completedLabel : "Uploaded: %curFileN of %totalFilesN %fileLabel (%totalCurrBytes)%errorString",
				numberOfErrors : ", %errorsN %errorLabel",
				singleFile : "file",
				pluralFiles : "files",
				singleError : "error",
				pluralErrors : "errors"
			},
			buttons : {
				browse : "Browse Files",
				addMore : "Add More",
				stopUpload : "Stop Upload",
				cancelRemaning : "Cancel remaining Uploads",
				resumeUpload : "Resume Upload"
			},
			queue : {
				emptyQueue : "File list: No files waiting to be uploaded.",
				queueSummary : "File list:  %totalUploaded files uploaded, %totalInUploadQueue file waiting to be uploaded."
			}
		},
		mergePolicy : {
			"fileQueueView.options.model" : "preserve"
		}
	});
	fluid.demands("fluid.uploader.totalProgressBar", "fluid.uploader.multiFileUploader", {
		funcName : "fluid.progress",
		container : "{multiFileUploader}.container"
	});
	fluid.demands("fluid.uploader.fileQueueView", "fluid.uploader.multiFileUploader", {
		container : "{multiFileUploader}.dom.fileQueue",
		options : {
			events : {
				onFileRemoved : "{multiFileUploader}.events.onFileRemoved"
			}
		}
	});
	fluid.demands("fluid.uploader.fileQueueView.eventBinder", ["fluid.uploader.multiFileUploader", "fluid.uploader.fileQueueView"], {
		options : {
			listeners : {
				"{multiFileUploader}.events.afterFileQueued" : "{fileQueueView}.addFile",
				"{multiFileUploader}.events.onUploadStart" : "{fileQueueView}.prepareForUpload",
				"{multiFileUploader}.events.onFileStart" : "{fileQueueView}.showFileProgress",
				"{multiFileUploader}.events.onFileProgress" : "{fileQueueView}.updateFileProgress",
				"{multiFileUploader}.events.onFileSuccess" : "{fileQueueView}.markFileComplete",
				"{multiFileUploader}.events.onFileError" : "{fileQueueView}.showErrorForFile",
				"{multiFileUploader}.events.afterFileComplete" : "{fileQueueView}.hideFileProgress",
				"{multiFileUploader}.events.afterUploadComplete" : "{fileQueueView}.refreshAfterUpload"
			}
		}
	});
	fluid.uploader.formatFileSize = function (bytes) {
		if (typeof(bytes) === "number") {
			if (bytes === 0) {
				return "0.0 KB"
			} else {
				if (bytes > 0) {
					if (bytes < 1048576) {
						return (Math.ceil(bytes / 1024 * 10) / 10).toFixed(1) + " KB"
					} else {
						return (Math.ceil(bytes / 1048576 * 10) / 10).toFixed(1) + " MB"
					}
				}
			}
		}
		return ""
	};
	fluid.uploader.derivePercent = function (num, total) {
		return Math.round((num * 100) / total)
	};
	fluid.uploader.ariaLiveRegionUpdater = function (statusRegion, totalFileStatusText, events) {
		statusRegion.attr("role", "log");
		statusRegion.attr("aria-live", "assertive");
		statusRegion.attr("aria-relevant", "text");
		statusRegion.attr("aria-atomic", "true");
		var regionUpdater = function () {
			statusRegion.text(totalFileStatusText.text())
		};
		events.afterFileDialog.addListener(regionUpdater);
		events.afterFileRemoved.addListener(regionUpdater);
		events.afterUploadComplete.addListener(regionUpdater)
	};
	fluid.demands("fluid.uploader.ariaLiveRegionUpdater", "fluid.uploader.multiFileUploader", {
		funcName : "fluid.uploader.ariaLiveRegionUpdater",
		args : ["{multiFileUploader}.dom.statusRegion", "{multiFileUploader}.dom.totalFileStatusText", "{multiFileUploader}.events"]
	});
	fluid.uploader.queueErrorConstants = {
		QUEUE_LIMIT_EXCEEDED : -100,
		FILE_EXCEEDS_SIZE_LIMIT : -110,
		ZERO_BYTE_FILE : -120,
		INVALID_FILETYPE : -130
	};
	fluid.uploader.errorConstants = {
		HTTP_ERROR : -200,
		MISSING_UPLOAD_URL : -210,
		IO_ERROR : -220,
		SECURITY_ERROR : -230,
		UPLOAD_LIMIT_EXCEEDED : -240,
		UPLOAD_FAILED : -250,
		SPECIFIED_FILE_ID_NOT_FOUND : -260,
		FILE_VALIDATION_FAILED : -270,
		FILE_CANCELLED : -280,
		UPLOAD_STOPPED : -290
	};
	fluid.uploader.fileStatusConstants = {
		QUEUED : -1,
		IN_PROGRESS : -2,
		ERROR : -3,
		COMPLETE : -4,
		CANCELLED : -5
	};
	var toggleVisibility = function (toShow, toHide) {
		if (window.opera) {
			toShow.show().removeClass("hideUploaderForOpera");
			toHide.show().addClass("hideUploaderForOpera")
		} else {
			toShow.show();
			toHide.hide()
		}
	};
	fluid.uploader.singleFileUploader = function (container, options) {
		var that = fluid.initView("fluid.uploader.singleFileUploader", container, options);
		toggleVisibility($(that.options.selectors.basicUpload), that.container);
		return that
	};
	fluid.defaults("fluid.uploader.singleFileUploader", {
		gradeNames : "fluid.viewComponent",
		selectors : {
			basicUpload : ".fl-progEnhance-basic"
		}
	});
	fluid.demands("fluid.uploaderImpl", "fluid.uploader.singleFile", {
		funcName : "fluid.uploader.singleFileUploader"
	})
})(jQuery, fluid_1_4);
var fluid_1_4 = fluid_1_4 || {};
(function ($, fluid) {
	fluid.uploader = fluid.uploader || {};
	var filterFiles = function (files, filterFn) {
		var filteredFiles = [];
		for (var i = 0; i < files.length; i++) {
			var file = files[i];
			if (filterFn(file) === true) {
				filteredFiles.push(file)
			}
		}
		return filteredFiles
	};
	fluid.uploader.fileQueue = function () {
		var that = {};
		that.files = [];
		that.isUploading = false;
		that.start = function () {
			that.setupCurrentBatch();
			that.isUploading = true;
			that.shouldStop = false
		};
		that.startFile = function () {
			that.currentBatch.fileIdx++;
			that.currentBatch.bytesUploadedForFile = 0;
			that.currentBatch.previousBytesUploadedForFile = 0
		};
		that.finishFile = function (file) {
			that.currentBatch.numFilesCompleted++
		};
		that.shouldUploadNextFile = function () {
			return !that.shouldStop && that.isUploading && (that.currentBatch.numFilesCompleted + that.currentBatch.numFilesErrored) < that.currentBatch.files.length
		};
		that.addFile = function (file) {
			that.files.push(file)
		};
		that.removeFile = function (file) {
			var idx = $.inArray(file, that.files);
			that.files.splice(idx, 1)
		};
		that.totalBytes = function () {
			return fluid.uploader.fileQueue.sizeOfFiles(that.files)
		};
		that.getReadyFiles = function () {
			return filterFiles(that.files, function (file) {
				return (file.filestatus === fluid.uploader.fileStatusConstants.QUEUED || file.filestatus === fluid.uploader.fileStatusConstants.CANCELLED)
			})
		};
		that.getErroredFiles = function () {
			return filterFiles(that.files, function (file) {
				return (file.filestatus === fluid.uploader.fileStatusConstants.ERROR)
			})
		};
		that.sizeOfReadyFiles = function () {
			return fluid.uploader.fileQueue.sizeOfFiles(that.getReadyFiles())
		};
		that.getUploadedFiles = function () {
			return filterFiles(that.files, function (file) {
				return (file.filestatus === fluid.uploader.fileStatusConstants.COMPLETE)
			})
		};
		that.sizeOfUploadedFiles = function () {
			return fluid.uploader.fileQueue.sizeOfFiles(that.getUploadedFiles())
		};
		that.setupCurrentBatch = function () {
			that.clearCurrentBatch();
			that.updateCurrentBatch()
		};
		that.clearCurrentBatch = function () {
			that.currentBatch = {
				fileIdx : 0,
				files : [],
				totalBytes : 0,
				numFilesCompleted : 0,
				numFilesErrored : 0,
				bytesUploadedForFile : 0,
				previousBytesUploadedForFile : 0,
				totalBytesUploaded : 0
			}
		};
		that.updateCurrentBatch = function () {
			var readyFiles = that.getReadyFiles();
			that.currentBatch.files = readyFiles;
			that.currentBatch.totalBytes = fluid.uploader.fileQueue.sizeOfFiles(readyFiles)
		};
		that.updateBatchStatus = function (currentBytes) {
			var byteIncrement = currentBytes - that.currentBatch.previousBytesUploadedForFile;
			that.currentBatch.totalBytesUploaded += byteIncrement;
			that.currentBatch.bytesUploadedForFile += byteIncrement;
			that.currentBatch.previousBytesUploadedForFile = currentBytes
		};
		return that
	};
	fluid.uploader.fileQueue.sizeOfFiles = function (files) {
		var totalBytes = 0;
		for (var i = 0; i < files.length; i++) {
			var file = files[i];
			totalBytes += file.size
		}
		return totalBytes
	}
})(jQuery, fluid_1_4);
var fluid_1_4 = fluid_1_4 || {};
(function ($, fluid) {
	var rowForFile = function (that, file) {
		return that.locate("fileQueue").find("#" + file.id)
	};
	var errorRowForFile = function (that, file) {
		return $("#" + file.id + "_error", that.container)
	};
	var fileForRow = function (that, row) {
		var files = that.model;
		var i;
		for (i = 0; i < files.length; i++) {
			var file = files[i];
			if (file.id.toString() === row.prop("id")) {
				return file
			}
		}
		return null
	};
	var progressorForFile = function (that, file) {
		var progressId = file.id + "_progress";
		return that.fileProgressors[progressId]
	};
	var startFileProgress = function (that, file) {
		var fileRowElm = rowForFile(that, file);
		that.scroller.scrollTo(fileRowElm);
		var fileProgressor = progressorForFile(that, file);
		fileProgressor.refreshView();
		fileProgressor.show()
	};
	var updateFileProgress = function (that, file, fileBytesComplete, fileTotalBytes) {
		var filePercent = fluid.uploader.derivePercent(fileBytesComplete, fileTotalBytes);
		var filePercentStr = filePercent + "%";
		progressorForFile(that, file).update(filePercent, filePercentStr)
	};
	var hideFileProgress = function (that, file) {
		var fileRowElm = rowForFile(that, file);
		progressorForFile(that, file).hide();
		if (file.filestatus === fluid.uploader.fileStatusConstants.COMPLETE) {
			that.locate("fileIconBtn", fileRowElm).removeClass(that.options.styles.dim)
		}
	};
	var removeFileProgress = function (that, file) {
		var fileProgressor = progressorForFile(that, file);
		if (!fileProgressor) {
			return
		}
		var rowProgressor = fileProgressor.displayElement;
		rowProgressor.remove()
	};
	var animateRowRemoval = function (that, row) {
		row.fadeOut("fast", function () {
			row.remove();
			that.refreshView()
		})
	};
	var removeFileErrorRow = function (that, file) {
		if (file.filestatus === fluid.uploader.fileStatusConstants.ERROR) {
			animateRowRemoval(that, errorRowForFile(that, file))
		}
	};
	var removeFileAndRow = function (that, file, row) {
		removeFileProgress(that, file);
		removeFileErrorRow(that, file);
		that.events.onFileRemoved.fire(file);
		animateRowRemoval(that, row)
	};
	var removeFileForRow = function (that, row) {
		var file = fileForRow(that, row);
		if (!file || file.filestatus === fluid.uploader.fileStatusConstants.COMPLETE) {
			return
		}
		removeFileAndRow(that, file, row)
	};
	var removeRowForFile = function (that, file) {
		var row = rowForFile(that, file);
		removeFileAndRow(that, file, row)
	};
	var bindHover = function (row, styles) {
		var over = function () {
			if (row.hasClass(styles.ready) && !row.hasClass(styles.uploading)) {
				row.addClass(styles.hover)
			}
		};
		var out = function () {
			if (row.hasClass(styles.ready) && !row.hasClass(styles.uploading)) {
				row.removeClass(styles.hover)
			}
		};
		row.hover(over, out)
	};
	var bindDeleteKey = function (that, row) {
		var deleteHandler = function () {
			removeFileForRow(that, row)
		};
		fluid.activatable(row, null, {
			additionalBindings : [{
					key : $.ui.keyCode.DELETE,
					activateHandler : deleteHandler
				}
			]
		})
	};
	var bindRowHandlers = function (that, row) {
		if ($.browser.msie && $.browser.version < 7) {
			bindHover(row, that.options.styles)
		}
		that.locate("fileIconBtn", row).click(function () {
			removeFileForRow(that, row)
		});
		bindDeleteKey(that, row)
	};
	var renderRowFromTemplate = function (that, file) {
		var row = that.rowTemplate.clone(),
		fileName = file.name,
		fileSize = fluid.uploader.formatFileSize(file.size);
		row.removeClass(that.options.styles.hiddenTemplate);
		that.locate("fileName", row).text(fileName);
		that.locate("fileSize", row).text(fileSize);
		that.locate("fileIconBtn", row).addClass(that.options.styles.remove);
		row.prop("id", file.id);
		row.addClass(that.options.styles.ready);
		bindRowHandlers(that, row);
		fluid.updateAriaLabel(row, fileName + " " + fileSize);
		return row
	};
	var createProgressorFromTemplate = function (that, row) {
		var rowProgressor = that.rowProgressorTemplate.clone();
		var rowId = row.prop("id");
		var progressId = rowId + "_progress";
		rowProgressor.prop("id", progressId);
		rowProgressor.css("top", row.position().top);
		rowProgressor.height(row.height()).width(5);
		that.container.after(rowProgressor);
		that.fileProgressors[progressId] = fluid.progress(that.options.uploaderContainer, {
				selectors : {
					progressBar : "#" + rowId,
					displayElement : "#" + progressId,
					label : "#" + progressId + " .fl-uploader-file-progress-text",
					indicator : "#" + progressId
				}
			})
	};
	var addFile = function (that, file) {
		var row = renderRowFromTemplate(that, file);
		if (!($.browser.msie && ($.browser.version >= 8))) {
			row.hide()
		}
		that.container.append(row);
		row.attr("title", that.options.strings.status.remove);
		row.fadeIn("slow");
		createProgressorFromTemplate(that, row);
		that.refreshView();
		that.scroller.scrollTo("100%")
	};
	var enableRows = function (rows, state) {
		var i;
		for (i = 0; i < rows.length; i++) {
			fluid.enabled(rows[i], state)
		}
	};
	var prepareForUpload = function (that) {
		var rowButtons = that.locate("fileIconBtn", that.locate("fileRows"));
		rowButtons.prop("disabled", true);
		rowButtons.addClass(that.options.styles.dim);
		enableRows(that.locate("fileRows"), false)
	};
	var refreshAfterUpload = function (that) {
		var rowButtons = that.locate("fileIconBtn", that.locate("fileRows"));
		rowButtons.prop("disabled", false);
		rowButtons.removeClass(that.options.styles.dim);
		enableRows(that.locate("fileRows"), true)
	};
	var changeRowState = function (that, row, newState) {
		row.removeClass(that.options.styles.ready).removeClass(that.options.styles.error).addClass(newState)
	};
	var markRowAsComplete = function (that, file) {
		var row = rowForFile(that, file);
		changeRowState(that, row, that.options.styles.uploaded);
		row.attr("title", that.options.strings.status.success);
		fluid.enabled(row, false);
		var removeRowBtn = that.locate("fileIconBtn", row);
		removeRowBtn.unbind("click");
		removeRowBtn.removeClass(that.options.styles.remove);
		removeRowBtn.attr("title", that.options.strings.status.success)
	};
	var renderErrorInfoRowFromTemplate = function (that, fileRow, error) {
		var errorRow = that.errorInfoRowTemplate.clone();
		errorRow.prop("id", fileRow.prop("id") + "_error");
		var errorType = fluid.keyForValue(fluid.uploader.errorConstants, error);
		var errorMsg = that.options.strings.errors[errorType];
		that.locate("errorText", errorRow).text(errorMsg);
		fileRow.after(errorRow);
		that.scroller.scrollTo(errorRow)
	};
	var showErrorForFile = function (that, file, error) {
		hideFileProgress(that, file);
		if (file.filestatus === fluid.uploader.fileStatusConstants.ERROR) {
			var fileRowElm = rowForFile(that, file);
			changeRowState(that, fileRowElm, that.options.styles.error);
			renderErrorInfoRowFromTemplate(that, fileRowElm, error)
		}
	};
	var addKeyboardNavigation = function (that) {
		fluid.tabbable(that.container);
		that.selectableContext = fluid.selectable(that.container, {
				selectableSelector : that.options.selectors.fileRows,
				onSelect : function (itemToSelect) {
					$(itemToSelect).addClass(that.options.styles.selected)
				},
				onUnselect : function (selectedItem) {
					$(selectedItem).removeClass(that.options.styles.selected)
				}
			})
	};
	var prepareTemplateElements = function (that) {
		that.rowTemplate = that.locate("rowTemplate").remove();
		that.errorInfoRowTemplate = that.locate("errorInfoRowTemplate").remove();
		that.errorInfoRowTemplate.removeClass(that.options.styles.hiddenTemplate);
		that.rowProgressorTemplate = that.locate("rowProgressorTemplate", that.options.uploaderContainer).remove()
	};
	fluid.registerNamespace("fluid.uploader.fileQueueView");
	fluid.uploader.fileQueueView.finalInit = function (that) {
		prepareTemplateElements(that);
		addKeyboardNavigation(that)
	};
	fluid.uploader.fileQueueView.preInit = function (that) {
		that.fileProgressors = {};
		that.addFile = function (file) {
			addFile(that, file)
		};
		that.removeFile = function (file) {
			removeRowForFile(that, file)
		};
		that.prepareForUpload = function () {
			prepareForUpload(that)
		};
		that.refreshAfterUpload = function () {
			refreshAfterUpload(that)
		};
		that.showFileProgress = function (file) {
			startFileProgress(that, file)
		};
		that.updateFileProgress = function (file, fileBytesComplete, fileTotalBytes) {
			updateFileProgress(that, file, fileBytesComplete, fileTotalBytes)
		};
		that.markFileComplete = function (file) {
			progressorForFile(that, file).update(100, "100%");
			markRowAsComplete(that, file)
		};
		that.showErrorForFile = function (file, error) {
			showErrorForFile(that, file, error)
		};
		that.hideFileProgress = function (file) {
			hideFileProgress(that, file)
		};
		that.refreshView = function () {
			that.selectableContext.refresh();
			that.scroller.refreshView()
		}
	};
	fluid.defaults("fluid.uploader.fileQueueView", {
		gradeNames : ["fluid.viewComponent", "autoInit"],
		preInitFunction : "fluid.uploader.fileQueueView.preInit",
		finalInitFunction : "fluid.uploader.fileQueueView.finalInit",
		components : {
			scroller : {
				type : "fluid.scrollableTable"
			},
			eventBinder : {
				type : "fluid.uploader.fileQueueView.eventBinder"
			}
		},
		selectors : {
			fileRows : ".flc-uploader-file",
			fileName : ".flc-uploader-file-name",
			fileSize : ".flc-uploader-file-size",
			fileIconBtn : ".flc-uploader-file-action",
			errorText : ".flc-uploader-file-error",
			rowTemplate : ".flc-uploader-file-tmplt",
			errorInfoRowTemplate : ".flc-uploader-file-error-tmplt",
			rowProgressorTemplate : ".flc-uploader-file-progressor-tmplt"
		},
		styles : {
			hover : "fl-uploader-file-hover",
			selected : "fl-uploader-file-focus",
			ready : "fl-uploader-file-state-ready",
			uploading : "fl-uploader-file-state-uploading",
			uploaded : "fl-uploader-file-state-uploaded",
			error : "fl-uploader-file-state-error",
			remove : "fl-uploader-file-action-remove",
			dim : "fl-uploader-dim",
			hiddenTemplate : "fl-uploader-hidden-templates"
		},
		strings : {
			progress : {
				toUploadLabel : "To upload: %fileCount %fileLabel (%totalBytes)",
				singleFile : "file",
				pluralFiles : "files"
			},
			status : {
				success : "File Uploaded",
				error : "File Upload Error",
				remove : "Press Delete key to remove file"
			},
			errors : {
				HTTP_ERROR : "File upload error: a network error occured or the file was rejected (reason unknown).",
				IO_ERROR : "File upload error: a network error occured.",
				UPLOAD_LIMIT_EXCEEDED : "File upload error: you have uploaded as many files as you are allowed during this session",
				UPLOAD_FAILED : "File upload error: the upload failed for an unknown reason.",
				QUEUE_LIMIT_EXCEEDED : "You have as many files in the queue as can be added at one time. Removing files from the queue may allow you to add different files.",
				FILE_EXCEEDS_SIZE_LIMIT : "One or more of the files that you attempted to add to the queue exceeded the limit of %fileSizeLimit.",
				ZERO_BYTE_FILE : "One or more of the files that you attempted to add contained no data.",
				INVALID_FILETYPE : "One or more files were not added to the queue because they were of the wrong type."
			}
		},
		events : {
			onFileRemoved : null
		},
		mergePolicy : {
			model : "preserve"
		}
	});
	fluid.defaults("fluid.uploader.fileQueueView.eventBinder", {
		gradeNames : ["fluid.eventedComponent", "autoInit"]
	});
	fluid.demands("fluid.uploader.fileQueueView.eventBinder", [], {});
	fluid.scrollable = function (element, options) {
		var that = fluid.initView("fluid.scrollable", element, options);
		that.scrollable = that.options.makeScrollableFn(that.container, that.options);
		that.maxHeight = that.scrollable.css("max-height");
		that.scrollTo = function () {
			that.scrollable.scrollTo.apply(that.scrollable, arguments)
		};
		that.refreshView = function () {
			if ($.browser.msie && $.browser.version === "6.0") {
				that.scrollable.css("height", "");
				if (that.scrollable.height() >= parseInt(that.maxHeight, 10)) {
					that.scrollable.css("height", that.maxHeight)
				}
			}
		};
		that.refreshView();
		return that
	};
	fluid.scrollable.makeSimple = function (element, options) {
		return fluid.container(element)
	};
	fluid.scrollable.makeTable = function (table, options) {
		table.wrap(options.wrapperMarkup);
		return table.closest(".fl-scrollable-scroller")
	};
	fluid.defaults("fluid.scrollable", {
		makeScrollableFn : fluid.scrollable.makeSimple
	});
	fluid.scrollableTable = function (table, options) {
		options = $.extend({}, fluid.defaults("fluid.scrollableTable"), options);
		return fluid.scrollable(table, options)
	};
	fluid.defaults("fluid.scrollableTable", {
		gradeNames : "fluid.viewComponent",
		makeScrollableFn : fluid.scrollable.makeTable,
		wrapperMarkup : "<div class='fl-scrollable-scroller'><div class='fl-scrollable-inner'></div></div>"
	});
	fluid.demands("fluid.scrollableTable", "fluid.uploader.fileQueueView", {
		funcName : "fluid.scrollableTable",
		args : ["{fileQueueView}.container"]
	})
})(jQuery, fluid_1_4);
var fluid_1_4 = fluid_1_4 || {};
(function ($, fluid) {
	fluid.uploader = fluid.uploader || {};
	fluid.defaults("fluid.uploader.errorPanel", {
		gradeNames : ["fluid.viewComponent", "autoInit"],
		preInitFunction : "fluid.uploader.errorPanel.preInit",
		postInitFunction : "fluid.uploader.errorPanel.renderSectionTemplates",
		finalInitFunction : "fluid.uploader.errorPanel.finalInit",
		components : {
			fileSizeErrorSection : {
				type : "fluid.uploader.errorPanel.section",
				container : "{errorPanel}.dom.fileSizeErrorSection",
				options : {
					model : {
						errorCode : fluid.uploader.queueErrorConstants.FILE_EXCEEDS_SIZE_LIMIT
					},
					strings : {
						header : "{errorPanel}.options.strings.exceedsFileSize"
					}
				}
			},
			numFilesErrorSection : {
				type : "fluid.uploader.errorPanel.section",
				container : "{errorPanel}.dom.numFilesErrorSection",
				options : {
					model : {
						errorCode : fluid.uploader.queueErrorConstants.QUEUE_LIMIT_EXCEEDED
					},
					strings : {
						header : "{errorPanel}.options.strings.exceedsNumFilesLimit"
					}
				}
			}
		},
		selectors : {
			header : ".flc-uploader-errorPanel-header",
			sectionTemplate : ".flc-uploader-errorPanel-section-tmplt",
			fileSizeErrorSection : ".flc-uploader-errorPanel-section-fileSize",
			numFilesErrorSection : ".flc-uploader-errorPanel-section-numFiles"
		},
		strings : {
			headerText : "Warning(s)",
			exceedsNumFilesLimit : "Too many files were selected. %numFiles were not added to the queue.",
			exceedsFileSize : "%numFiles files were too large and were not added to the queue."
		},
		styles : {
			hiddenTemplate : "fl-hidden-templates"
		}
	});
	fluid.uploader.errorPanel.preInit = function (that) {
		that.refreshView = function () {
			for (var i = 0; i < that.sections.length; i++) {
				if (that.sections[i].model.files.length > 0) {
					that.container.show();
					return
				}
			}
			that.container.hide()
		}
	};
	fluid.uploader.errorPanel.renderSectionTemplates = function (that) {
		var sectionTmpl = that.locate("sectionTemplate").remove().removeClass(that.options.styles.hiddenTemplate);
		that.locate("fileSizeErrorSection").append(sectionTmpl.clone());
		that.locate("numFilesErrorSection").append(sectionTmpl.clone())
	};
	fluid.uploader.errorPanel.finalInit = function (that) {
		that.sections = [that.fileSizeErrorSection, that.numFilesErrorSection];
		that.locate("header").text(that.options.strings.headerText);
		that.container.hide()
	};
	fluid.demands("fluid.uploader.errorPanel", "fluid.uploader.multiFileUploader", {
		container : "{multiFileUploader}.dom.errorsPanel",
		options : {
			listeners : {
				"{multiFileUploader}.events.afterFileDialog" : "{errorPanel}.refreshView"
			}
		}
	});
	fluid.defaults("fluid.uploader.errorPanel.section", {
		gradeNames : ["fluid.viewComponent", "autoInit"],
		preInitFunction : "fluid.uploader.errorPanel.section.preInit",
		finalInitFunction : "fluid.uploader.errorPanel.section.finalInit",
		model : {
			errorCode : undefined,
			files : [],
			showingDetails : false
		},
		events : {
			afterErrorsCleared : null
		},
		selectors : {
			errorTitle : ".fl-uploader-errorPanel-section-title",
			deleteErrorButton : ".flc-uploader-errorPanel-section-removeButton",
			errorDetails : ".flc-uploader-errorPanel-section-details",
			erroredFiles : ".flc-uploader-errorPanel-section-files",
			showHideFilesToggle : ".flc-uploader-errorPanel-section-toggleDetails"
		},
		strings : {
			hideFiles : "Hide files",
			showFiles : "Show files",
			fileListDelimiter : ", "
		}
	});
	fluid.uploader.errorPanel.section.preInit = function (that) {
		that.toggleDetails = function () {
			var detailsAction = that.model.showingDetails ? that.hideDetails : that.showDetails;
			detailsAction()
		};
		that.showDetails = function () {
			that.locate("errorDetails").show();
			that.locate("showHideFilesToggle").text(that.options.strings.hideFiles);
			that.model.showingDetails = true
		};
		that.hideDetails = function () {
			that.locate("errorDetails").hide();
			that.locate("showHideFilesToggle").text(that.options.strings.showFiles);
			that.model.showingDetails = false
		};
		that.addFile = function (file, errorCode) {
			if (errorCode === that.model.errorCode) {
				that.model.files.push(file.name);
				that.refreshView()
			}
		};
		that.clear = function () {
			that.model.files = [];
			that.refreshView();
			that.events.afterErrorsCleared.fire()
		};
		that.refreshView = function () {
			fluid.uploader.errorPanel.section.renderHeader(that);
			fluid.uploader.errorPanel.section.renderErrorDetails(that);
			that.hideDetails();
			if (that.model.files.length <= 0) {
				that.container.hide()
			} else {
				that.container.show()
			}
		}
	};
	fluid.uploader.errorPanel.section.finalInit = function (that) {
		that.locate("deleteErrorButton").click(that.clear);
		that.locate("showHideFilesToggle").click(that.toggleDetails);
		that.refreshView()
	};
	fluid.uploader.errorPanel.section.renderHeader = function (that) {
		var errorTitle = fluid.stringTemplate(that.options.strings.header, {
				numFiles : that.model.files.length
			});
		that.locate("errorTitle").text(errorTitle)
	};
	fluid.uploader.errorPanel.section.renderErrorDetails = function (that) {
		var files = that.model.files;
		var filesList = files.length > 0 ? files.join(that.options.strings.fileListDelimiter) : "";
		that.locate("erroredFiles").text(filesList)
	};
	fluid.demands("fluid.uploader.errorPanel.section", ["fluid.uploader.errorPanel", "fluid.uploader.multiFileUploader"], {
		options : {
			listeners : {
				"{multiFileUploader}.events.onQueueError" : "{section}.addFile",
				"{multiFileUploader}.events.onFilesSelected" : "{section}.clear",
				"{multiFileUploader}.events.onUploadStart" : "{section}.clear",
				"{section}.events.afterErrorsCleared" : "{errorPanel}.refreshView"
			}
		}
	})
})(jQuery, fluid_1_4);
var fluid_1_4 = fluid_1_4 || {};
(function ($, fluid) {
	fluid.uploader = fluid.uploader || {};
	fluid.demands("fluid.uploaderImpl", "fluid.uploader.swfUpload", {
		funcName : "fluid.uploader.multiFileUploader"
	});
	fluid.uploader.swfUploadStrategy = function (options) {
		var that = fluid.initLittleComponent("fluid.uploader.swfUploadStrategy", options);
		fluid.initDependents(that);
		return that
	};
	fluid.defaults("fluid.uploader.swfUploadStrategy", {
		components : {
			engine : {
				type : "fluid.uploader.swfUploadStrategy.engine",
				options : {
					queueSettings : "{multiFileUploader}.options.queueSettings",
					flashMovieSettings : "{swfUploadStrategy}.options.flashMovieSettings"
				}
			},
			local : {
				type : "fluid.uploader.local",
				options : {
					errorHandler : "{multiFileUploader}.dom.errorHandler"
				}
			},
			remote : {
				type : "fluid.uploader.remote"
			}
		},
		flashMovieSettings : {
			flashURL : "../../../lib/swfupload/flash/swfupload.swf",
			flashButtonPeerId : "",
			flashButtonAlwaysVisible : false,
			flashButtonTransparentEvenInIE : true,
			flashButtonImageURL : "../images/browse.png",
			flashButtonCursorEffect : SWFUpload.CURSOR.HAND,
			debug : false
		},
		styles : {
			browseButtonOverlay : "fl-uploader-browse-overlay",
			flash9Container : "fl-uploader-flash9-container",
			uploaderWrapperFlash10 : "fl-uploader-flash10-wrapper"
		}
	});
	fluid.demands("fluid.uploader.progressiveStrategy", "fluid.uploader.swfUpload", {
		funcName : "fluid.uploader.swfUploadStrategy"
	});
	fluid.uploader.swfUploadStrategy.remote = function (swfUpload, queue, options) {
		var that = fluid.initLittleComponent("fluid.uploader.swfUploadStrategy.remote", options);
		that.swfUpload = swfUpload;
		that.queue = queue;
		that.uploadNextFile = function () {
			that.swfUpload.startUpload()
		};
		that.stop = function () {
			that.queue.shouldStop = true
		};
		return that
	};
	fluid.demands("fluid.uploader.remote", "fluid.uploader.swfUploadStrategy", {
		funcName : "fluid.uploader.swfUploadStrategy.remote",
		args : ["{engine}.swfUpload", "{multiFileUploader}.queue", "{options}"]
	});
	fluid.uploader.swfUploadStrategy.local = function (swfUpload, options) {
		var that = fluid.initLittleComponent("fluid.uploader.swfUploadStrategy.local", options);
		that.swfUpload = swfUpload;
		that.browse = function () {
			if (that.options.file_queue_limit === 1) {
				that.swfUpload.selectFile()
			} else {
				that.swfUpload.selectFiles()
			}
		};
		that.removeFile = function (file) {
			that.swfUpload.cancelUpload(file.id)
		};
		that.enableBrowseButton = function () {
			that.swfUpload.setButtonDisabled(false)
		};
		that.disableBrowseButton = function () {
			that.swfUpload.setButtonDisabled(true)
		};
		return that
	};
	fluid.demands("fluid.uploader.local", "fluid.uploader.swfUploadStrategy", {
		funcName : "fluid.uploader.swfUploadStrategy.local",
		args : ["{engine}.swfUpload", "{options}"]
	});
	fluid.uploader.swfUploadStrategy.engine = function (options) {
		var that = fluid.initLittleComponent("fluid.uploader.swfUploadStrategy.engine", options);
		var flashVersion = swfobject.getFlashPlayerVersion().major;
		that.flashVersionContext = fluid.typeTag("fluid.uploader.flash." + flashVersion);
		that.config = $.extend({}, that.options.queueSettings, that.options.flashMovieSettings);
		fluid.initDependents(that);
		that.flashContainer = that.setupDOM();
		that.swfUploadConfig = that.setupConfig();
		that.swfUpload = new SWFUpload(that.swfUploadConfig);
		that.bindEvents();
		return that
	};
	fluid.defaults("fluid.uploader.swfUploadStrategy.engine", {
		invokers : {
			setupDOM : "fluid.uploader.swfUploadStrategy.setupDOM",
			setupConfig : "fluid.uploader.swfUploadStrategy.setupConfig",
			bindEvents : "fluid.uploader.swfUploadStrategy.eventBinder"
		}
	});
	fluid.demands("fluid.uploader.swfUploadStrategy.engine", "fluid.uploader.swfUploadStrategy", {
		funcName : "fluid.uploader.swfUploadStrategy.engine",
		args : [fluid.COMPONENT_OPTIONS]
	});
	fluid.uploader.swfUploadStrategy.fileTypeTransformer = function (model, expandSpec) {
		var fileExts = "";
		var mimeTypes = fluid.get(model, expandSpec.path);
		var mimeTypesMap = fluid.uploader.mimeTypeRegistry;
		if (!mimeTypes) {
			return "*"
		} else {
			if (typeof(mimeTypes) === "string") {
				return mimeTypes
			}
		}
		fluid.each(mimeTypes, function (mimeType) {
			fluid.each(mimeTypesMap, function (mimeTypeForExt, ext) {
				if (mimeTypeForExt === mimeType) {
					fileExts += "*." + ext + ";"
				}
			})
		});
		return fileExts.length === 0 ? "*" : fileExts.substring(0, fileExts.length - 1)
	};
	fluid.uploader.swfUploadStrategy.flash10SetupDOM = function (uploaderContainer, browseButton, progressBar, styles) {
		uploaderContainer.wrap("<div class='" + styles.uploaderWrapperFlash10 + "'></div>");
		var flashContainer = $("<div><span></span></div>");
		flashContainer.addClass(styles.browseButtonOverlay);
		uploaderContainer.after(flashContainer);
		progressBar.append(flashContainer);
		browseButton.attr("tabindex", -1);
		return flashContainer
	};
	fluid.demands("fluid.uploader.swfUploadStrategy.setupDOM", ["fluid.uploader.swfUploadStrategy.engine", "fluid.uploader.flash.10"], {
		funcName : "fluid.uploader.swfUploadStrategy.flash10SetupDOM",
		args : ["{multiFileUploader}.container", "{multiFileUploader}.dom.browseButton", "{totalProgress}.dom.progressBar", "{swfUploadStrategy}.options.styles"]
	});
	var swfUploadOptionsMap = {
		uploadURL : "upload_url",
		flashURL : "flash_url",
		postParams : "post_params",
		fileSizeLimit : "file_size_limit",
		fileTypes : "file_types",
		fileUploadLimit : "file_upload_limit",
		fileQueueLimit : "file_queue_limit",
		flashButtonPeerId : "button_placeholder_id",
		flashButtonImageURL : "button_image_url",
		flashButtonHeight : "button_height",
		flashButtonWidth : "button_width",
		flashButtonWindowMode : "button_window_mode",
		flashButtonCursorEffect : "button_cursor",
		debug : "debug"
	};
	var swfUploadEventMap = {
		afterReady : "swfupload_loaded_handler",
		onFileDialog : "file_dialog_start_handler",
		onFileQueued : "file_queued_handler",
		onQueueError : "file_queue_error_handler",
		afterFileDialog : "file_dialog_complete_handler",
		onFileStart : "upload_start_handler",
		onFileProgress : "upload_progress_handler",
		onFileComplete : "upload_complete_handler",
		onFileError : "upload_error_handler",
		onFileSuccess : "upload_success_handler"
	};
	var mapNames = function (nameMap, source, target) {
		var result = target || {};
		for (var key in source) {
			var mappedKey = nameMap[key];
			if (mappedKey) {
				result[mappedKey] = source[key]
			}
		}
		return result
	};
	var mapSWFUploadEvents = function (nameMap, events, target) {
		var result = target || {};
		for (var eventType in events) {
			var fireFn = events[eventType].fire;
			var mappedName = nameMap[eventType];
			if (mappedName) {
				result[mappedName] = fireFn
			}
		}
		return result
	};
	fluid.uploader.swfUploadStrategy.convertConfigForSWFUpload = function (flashContainer, config, events, queueSettings) {
		config.flashButtonPeerId = fluid.allocateSimpleId(flashContainer.children().eq(0));
		config.fileTypes = fluid.uploader.swfUploadStrategy.fileTypeTransformer(queueSettings, {
				path : "fileTypes"
			});
		var convertedConfig = mapNames(swfUploadOptionsMap, config);
		convertedConfig.file_upload_limit = 0;
		convertedConfig.file_size_limit = 0;
		return mapSWFUploadEvents(swfUploadEventMap, events, convertedConfig)
	};
	fluid.uploader.swfUploadStrategy.flash10SetupConfig = function (config, events, flashContainer, browseButton, queueSettings) {
		var isTransparent = config.flashButtonAlwaysVisible ? false : (!$.browser.msie || config.flashButtonTransparentEvenInIE);
		config.flashButtonImageURL = isTransparent ? undefined : config.flashButtonImageURL;
		config.flashButtonHeight = config.flashButtonHeight || browseButton.outerHeight();
		config.flashButtonWidth = config.flashButtonWidth || browseButton.outerWidth();
		config.flashButtonWindowMode = isTransparent ? SWFUpload.WINDOW_MODE.TRANSPARENT : SWFUpload.WINDOW_MODE.OPAQUE;
		return fluid.uploader.swfUploadStrategy.convertConfigForSWFUpload(flashContainer, config, events, queueSettings)
	};
	fluid.demands("fluid.uploader.swfUploadStrategy.setupConfig", ["fluid.uploader.swfUploadStrategy.engine", "fluid.uploader.flash.10"], {
		funcName : "fluid.uploader.swfUploadStrategy.flash10SetupConfig",
		args : ["{engine}.config", "{multiFileUploader}.events", "{engine}.flashContainer", "{multiFileUploader}.dom.browseButton", "{multiFileUploader}.options.queueSettings"]
	});
	var unbindSWFUploadSelectFiles = function () {
		var emptyFunction = function () {};
		SWFUpload.prototype.selectFile = emptyFunction;
		SWFUpload.prototype.selectFiles = emptyFunction
	};
	fluid.uploader.swfUploadStrategy.bindFileEventListeners = function (model, events) {
		var manualModelUpdater = function (file) {
			fluid.find(model, function (potentialMatch) {
				if (potentialMatch.id === file.id) {
					potentialMatch.filestatus = file.filestatus;
					return true
				}
			})
		};
		events.onFileStart.addListener(manualModelUpdater);
		events.onFileProgress.addListener(manualModelUpdater);
		events.onFileError.addListener(manualModelUpdater);
		events.onFileSuccess.addListener(manualModelUpdater)
	};
	var filterErroredFiles = function (file, events, queue, queueSettings) {
		var fileSizeLimit = queueSettings.fileSizeLimit * 1000;
		var fileUploadLimit = queueSettings.fileUploadLimit;
		var processedFiles = queue.getReadyFiles().length + queue.getUploadedFiles().length;
		if (file.size > fileSizeLimit) {
			file.filestatus = fluid.uploader.fileStatusConstants.ERROR;
			events.onQueueError.fire(file, fluid.uploader.queueErrorConstants.FILE_EXCEEDS_SIZE_LIMIT)
		} else {
			if (processedFiles >= fileUploadLimit) {
				events.onQueueError.fire(file, fluid.uploader.queueErrorConstants.QUEUE_LIMIT_EXCEEDED)
			} else {
				events.afterFileQueued.fire(file)
			}
		}
	};
	fluid.uploader.swfUploadStrategy.flash10EventBinder = function (queue, queueSettings, events) {
		var model = queue.files;
		unbindSWFUploadSelectFiles();
		events.onFileQueued.addListener(function (file) {
			filterErroredFiles(file, events, queue, queueSettings)
		});
		fluid.uploader.swfUploadStrategy.bindFileEventListeners(model, events)
	};
	fluid.demands("fluid.uploader.swfUploadStrategy.eventBinder", ["fluid.uploader.swfUploadStrategy.engine", "fluid.uploader.flash.10"], {
		funcName : "fluid.uploader.swfUploadStrategy.flash10EventBinder",
		args : ["{multiFileUploader}.queue", "{multiFileUploader}.queue.files", "{multiFileUploader}.events"]
	})
})(jQuery, fluid_1_4);
var fluid_1_4 = fluid_1_4 || {};
(function ($, fluid) {
	fluid.registerNamespace("fluid.uploader.swfUploadStrategy");
	fluid.uploader.swfUploadStrategy.flash9SetupDOM = function (styles) {
		var container = $("<div><span></span></div>");
		container.addClass(styles.flash9Container);
		$("body").append(container);
		return container
	};
	fluid.demands("fluid.uploader.swfUploadStrategy.setupDOM", ["fluid.uploader.swfUploadStrategy.engine", "fluid.uploader.flash.9"], {
		funcName : "fluid.uploader.swfUploadStrategy.flash9SetupDOM",
		args : ["{swfUploadStrategy}.options.styles"]
	});
	fluid.uploader.swfUploadStrategy.flash9SetupConfig = function (flashContainer, config, events) {
		return fluid.uploader.swfUploadStrategy.convertConfigForSWFUpload(flashContainer, config, events)
	};
	fluid.demands("fluid.uploader.swfUploadStrategy.setupConfig", ["fluid.uploader.swfUploadStrategy.engine", "fluid.uploader.flash.9"], {
		funcName : "fluid.uploader.swfUploadStrategy.flash9SetupConfig",
		args : ["{engine}.flashContainer", "{engine}.config", "{multiFileUploader}.events"]
	});
	fluid.uploader.swfUploadStrategy.flash9EventBinder = function (model, events, local, browseButton) {
		browseButton.click(function (e) {
			local.browse();
			e.preventDefault()
		});
		fluid.uploader.swfUploadStrategy.bindFileEventListeners(model, events)
	};
	fluid.demands("fluid.uploader.swfUploadStrategy.eventBinder", ["fluid.uploader.swfUploadStrategy.engine", "fluid.uploader.flash.9"], {
		funcName : "fluid.uploader.swfUploadStrategy.flash9EventBinder",
		args : ["{multiFileUploader}.queue.files", "{multiFileUploader}.events", "{local}", "{multiFileUploader}.dom.browseButton"]
	})
})(jQuery, fluid_1_4);
var fluid_1_4 = fluid_1_4 || {};
(function ($, fluid) {
	fluid.demands("fluid.uploaderImpl", "fluid.uploader.html5", {
		funcName : "fluid.uploader.multiFileUploader"
	});
	fluid.demands("fluid.uploader.progressiveStrategy", "fluid.uploader.html5", {
		funcName : "fluid.uploader.html5Strategy"
	});
	fluid.defaults("fluid.uploader.html5Strategy", {
		gradeNames : ["fluid.littleComponent", "autoInit"],
		components : {
			local : {
				type : "fluid.uploader.local",
				options : {
					queueSettings : "{multiFileUploader}.options.queueSettings",
					events : {
						onFileDialog : "{multiFileUploader}.events.onFileDialog",
						onFilesSelected : "{multiFileUploader}.events.onFilesSelected",
						afterFileDialog : "{multiFileUploader}.events.afterFileDialog",
						afterFileQueued : "{multiFileUploader}.events.afterFileQueued",
						onQueueError : "{multiFileUploader}.events.onQueueError"
					}
				}
			},
			remote : {
				type : "fluid.uploader.remote",
				options : {
					queueSettings : "{multiFileUploader}.options.queueSettings",
					events : {
						afterReady : "{multiFileUploader}.events.afterReady",
						onFileStart : "{multiFileUploader}.events.onFileStart",
						onFileProgress : "{multiFileUploader}.events.onFileProgress",
						onFileSuccess : "{multiFileUploader}.events.onFileSuccess",
						onFileError : "{multiFileUploader}.events.onFileError",
						onFileComplete : "{multiFileUploader}.events.onFileComplete"
					}
				}
			}
		},
		legacyBrowserFileLimit : 100000
	});
	fluid.uploader.html5Strategy.fileSuccessHandler = function (file, events, xhr) {
		events.onFileSuccess.fire(file, xhr.responseText, xhr);
		events.onFileComplete.fire(file)
	};
	fluid.uploader.html5Strategy.fileErrorHandler = function (file, events, xhr) {
		events.onFileError.fire(file, fluid.uploader.errorConstants.UPLOAD_FAILED, xhr.status, xhr);
		events.onFileComplete.fire(file)
	};
	fluid.uploader.html5Strategy.fileStopHandler = function (file, events, xhr) {
		events.onFileError.fire(file, fluid.uploader.errorConstants.UPLOAD_STOPPED, xhr.status, xhr);
		events.onFileComplete.fire(file)
	};
	fluid.uploader.html5Strategy.monitorFileUploadXHR = function (file, events, xhr) {
		xhr.onreadystatechange = function () {
			if (xhr.readyState === 4) {
				var status = xhr.status;
				if (status === 200) {
					fluid.uploader.html5Strategy.fileSuccessHandler(file, events, xhr)
				} else {
					if (status === 0) {
						fluid.uploader.html5Strategy.fileStopHandler(file, events, xhr)
					} else {
						fluid.uploader.html5Strategy.fileErrorHandler(file, events, xhr)
					}
				}
			}
		};
		xhr.upload.onprogress = function (pe) {
			events.onFileProgress.fire(file, pe.loaded, pe.total)
		}
	};
	fluid.uploader.html5Strategy.remote = function (queue, options) {
		var that = fluid.initLittleComponent("fluid.uploader.html5Strategy.remote", options);
		that.queue = queue;
		that.queueSettings = that.options.queueSettings;
		that.uploadNextFile = function () {
			var batch = that.queue.currentBatch;
			var file = batch.files[batch.fileIdx];
			that.uploadFile(file)
		};
		that.uploadFile = function (file) {
			that.events.onFileStart.fire(file);
			that.currentXHR = that.createXHR();
			fluid.uploader.html5Strategy.monitorFileUploadXHR(file, that.events, that.currentXHR);
			that.fileSender.send(file, that.queueSettings, that.currentXHR)
		};
		that.stop = function () {
			that.queue.isUploading = false;
			that.currentXHR.abort()
		};
		fluid.initDependents(that);
		that.events.afterReady.fire();
		return that
	};
	fluid.defaults("fluid.uploader.html5Strategy.remote", {
		gradeNames : ["fluid.eventedComponent"],
		argumentMap : {
			options : 1
		},
		components : {
			fileSender : {
				type : "fluid.uploader.html5Strategy.fileSender"
			}
		},
		invokers : {
			createXHR : "fluid.uploader.html5Strategy.createXHR"
		}
	});
	fluid.demands("fluid.uploader.remote", ["fluid.uploader.html5Strategy", "fluid.uploader.live"], {
		funcName : "fluid.uploader.html5Strategy.remote",
		args : ["{multiFileUploader}.queue", fluid.COMPONENT_OPTIONS]
	});
	fluid.uploader.html5Strategy.createXHR = function () {
		return new XMLHttpRequest()
	};
	fluid.uploader.html5Strategy.createFormData = function () {
		return new FormData()
	};
	var setPostParams = function (formData, postParams) {
		$.each(postParams, function (key, value) {
			formData.append(key, value)
		})
	};
	fluid.defaults("fluid.uploader.html5Strategy.formDataSender", {
		gradeNames : ["fluid.littleComponent", "autoInit"],
		finalInitFunction : "fluid.uploader.html5Strategy.formDataSender.init",
		invokers : {
			createFormData : "fluid.uploader.html5Strategy.createFormData"
		}
	});
	fluid.uploader.html5Strategy.formDataSender.init = function (that) {
		that.send = function (file, queueSettings, xhr) {
			var formData = that.createFormData();
			formData.append("file", file);
			setPostParams(formData, queueSettings.postParams);
			xhr.open("POST", queueSettings.uploadURL, true);
			xhr.send(formData);
			return formData
		}
	};
	fluid.demands("fluid.uploader.html5Strategy.fileSender", ["fluid.uploader.html5Strategy.remote", "fluid.browser.supportsFormData"], {
		funcName : "fluid.uploader.html5Strategy.formDataSender"
	});
	fluid.uploader.html5Strategy.generateMultipartBoundary = function () {
		var boundary = "---------------------------";
		boundary += Math.floor(Math.random() * 32768);
		boundary += Math.floor(Math.random() * 32768);
		boundary += Math.floor(Math.random() * 32768);
		return boundary
	};
	fluid.uploader.html5Strategy.generateMultiPartContent = function (boundary, file) {
		var CRLF = "\r\n";
		var multipart = "";
		multipart += "--" + boundary + CRLF;
		multipart += 'Content-Disposition: form-data; name="fileData"; filename="' + file.name + '"' + CRLF;
		multipart += "Content-Type: " + file.type + CRLF + CRLF;
		multipart += file.getAsBinary();
		multipart += CRLF + "--" + boundary + "--" + CRLF;
		return multipart
	};
	fluid.defaults("fluid.uploader.html5Strategy.rawMIMESender", {
		gradeNames : ["fluid.littleComponent", "autoInit"],
		finalInitFunction : "fluid.uploader.html5Strategy.rawMIMESender.init"
	});
	fluid.uploader.html5Strategy.rawMIMESender.init = function (that) {
		that.send = function (file, queueSettings, xhr) {
			var boundary = fluid.uploader.html5Strategy.generateMultipartBoundary();
			var multipart = fluid.uploader.html5Strategy.generateMultiPartContent(boundary, file);
			xhr.open("POST", queueSettings.uploadURL, true);
			xhr.setRequestHeader("Content-Type", "multipart/form-data; boundary=" + boundary);
			xhr.sendAsBinary(multipart);
			return multipart
		}
	};
	fluid.demands("fluid.uploader.html5Strategy.fileSender", "fluid.uploader.html5Strategy.remote", {
		funcName : "fluid.uploader.html5Strategy.rawMIMESender"
	});
	fluid.uploader.html5Strategy.local = function (queue, legacyBrowserFileLimit, options) {
		var that = fluid.initLittleComponent("fluid.uploader.html5Strategy.local", options);
		that.queue = queue;
		that.queueSettings = that.options.queueSettings;
		that.addFiles = function (files) {
			var sizeLimit = (legacyBrowserFileLimit || that.queueSettings.fileSizeLimit) * 1024;
			var fileLimit = that.queueSettings.fileUploadLimit;
			var uploaded = that.queue.getUploadedFiles().length;
			var queued = that.queue.getReadyFiles().length;
			var remainingUploadLimit = fileLimit - uploaded - queued;
			that.events.onFilesSelected.fire(files.length);
			var numFilesAdded = 0;
			for (var i = 0; i < files.length; i++) {
				var file = files[i];
				if (fileLimit && remainingUploadLimit === 0) {
					that.events.onQueueError.fire(file, fluid.uploader.queueErrorConstants.QUEUE_LIMIT_EXCEEDED)
				} else {
					if (file.size >= sizeLimit) {
						file.filestatus = fluid.uploader.fileStatusConstants.ERROR;
						that.events.onQueueError.fire(file, fluid.uploader.queueErrorConstants.FILE_EXCEEDS_SIZE_LIMIT)
					} else {
						if (!fileLimit || remainingUploadLimit > 0) {
							file.id = "file-" + fluid.allocateGuid();
							file.filestatus = fluid.uploader.fileStatusConstants.QUEUED;
							that.events.afterFileQueued.fire(file);
							remainingUploadLimit--;
							numFilesAdded++
						}
					}
				}
			}
			that.events.afterFileDialog.fire(numFilesAdded)
		};
		that.removeFile = function (file) {};
		that.enableBrowseButton = function () {
			that.browseButtonView.enable()
		};
		that.disableBrowseButton = function () {
			that.browseButtonView.disable()
		};
		fluid.initDependents(that);
		return that
	};
	fluid.defaults("fluid.uploader.html5Strategy.local", {
		argumentMap : {
			options : 2
		},
		gradeNames : ["fluid.eventedComponent"],
		components : {
			browseButtonView : {
				type : "fluid.uploader.html5Strategy.browseButtonView",
				options : {
					queueSettings : "{multiFileUploader}.options.queueSettings",
					selectors : {
						browseButton : "{multiFileUploader}.selectors.browseButton"
					},
					listeners : {
						onFilesQueued : "{local}.addFiles"
					}
				}
			}
		}
	});
	fluid.demands("fluid.uploader.local", "fluid.uploader.html5Strategy", {
		funcName : "fluid.uploader.html5Strategy.local",
		args : ["{multiFileUploader}.queue", "{html5Strategy}.options.legacyBrowserFileLimit", "{options}"]
	});
	fluid.demands("fluid.uploader.local", ["fluid.uploader.html5Strategy", "fluid.browser.supportsFormData"], {
		funcName : "fluid.uploader.html5Strategy.local",
		args : ["{multiFileUploader}.queue", undefined, "{options}"]
	});
	var bindEventsToFileInput = function (that, fileInput) {
		fileInput.click(function () {
			that.events.onBrowse.fire()
		});
		fileInput.change(function () {
			var files = fileInput[0].files;
			that.renderFreshMultiFileInput();
			that.events.onFilesQueued.fire(files)
		});
		fileInput.focus(function () {
			that.browseButton.addClass("focus")
		});
		fileInput.blur(function () {
			that.browseButton.removeClass("focus")
		})
	};
	var renderMultiFileInput = function (that) {
		var multiFileInput = $(that.options.multiFileInputMarkup);
		var fileTypes = that.options.queueSettings.fileTypes;
		if (fluid.isArrayable(fileTypes)) {
			fileTypes = fileTypes.join();
			multiFileInput.attr("accept", fileTypes)
		}
		bindEventsToFileInput(that, multiFileInput);
		return multiFileInput
	};
	var setupBrowseButtonView = function (that) {
		var multiFileInput = renderMultiFileInput(that);
		that.browseButton.append(multiFileInput);
		that.browseButton.attr("tabindex", -1)
	};
	fluid.uploader.html5Strategy.browseButtonView = function (container, options) {
		var that = fluid.initView("fluid.uploader.html5Strategy.browseButtonView", container, options);
		that.browseButton = that.locate("browseButton");
		that.renderFreshMultiFileInput = function () {
			var previousInput = that.locate("fileInputs").last();
			previousInput.hide();
			previousInput.attr("tabindex", -1);
			var newInput = renderMultiFileInput(that);
			previousInput.after(newInput)
		};
		that.enable = function () {
			that.locate("fileInputs").prop("disabled", false)
		};
		that.disable = function () {
			that.locate("fileInputs").prop("disabled", true)
		};
		that.isEnabled = function () {
			return !that.locate("fileInputs").prop("disabled")
		};
		setupBrowseButtonView(that);
		return that
	};
	fluid.defaults("fluid.uploader.html5Strategy.browseButtonView", {
		gradeNames : "fluid.viewComponent",
		multiFileInputMarkup : "<input type='file' multiple='' class='flc-uploader-html5-input' />",
		queueSettings : {},
		selectors : {
			browseButton : ".flc-uploader-button-browse",
			fileInputs : ".flc-uploader-html5-input"
		},
		events : {
			onBrowse : null,
			onFilesQueued : null
		}
	});
	fluid.demands("fluid.uploader.html5Strategy.browseButtonView", "fluid.uploader.html5Strategy.local", {
		container : "{multiFileUploader}.container",
		mergeOptions : {
			events : {
				onBrowse : "{local}.events.onFileDialog"
			}
		}
	})
})(jQuery, fluid_1_4);
var fluid_1_4 = fluid_1_4 || {};
(function ($, fluid) {
	fluid.uploader = fluid.uploader || {};
	var startUploading;
	var updateProgress = function (file, events, demoState, isUploading) {
		if (!isUploading) {
			return
		}
		var chunk = Math.min(demoState.chunkSize, file.size);
		demoState.bytesUploaded = Math.min(demoState.bytesUploaded + chunk, file.size);
		events.onFileProgress.fire(file, demoState.bytesUploaded, file.size)
	};
	var finishAndContinueOrCleanup = function (that, file) {
		that.queue.finishFile(file);
		that.events.afterFileComplete.fire(file);
		if (that.queue.shouldUploadNextFile()) {
			startUploading(that)
		} else {
			that.events.afterUploadComplete.fire(that.queue.currentBatch.files);
			if (file.status !== fluid.uploader.fileStatusConstants.CANCELLED) {
				that.queue.clearCurrentBatch()
			}
		}
	};
	var finishUploading = function (that) {
		if (!that.queue.isUploading) {
			return
		}
		var file = that.demoState.currentFile;
		that.events.onFileSuccess.fire(file);
		that.demoState.fileIdx++;
		finishAndContinueOrCleanup(that, file)
	};
	var simulateUpload = function (that) {
		if (!that.queue.isUploading) {
			return
		}
		var file = that.demoState.currentFile;
		if (that.demoState.bytesUploaded < file.size) {
			fluid.invokeAfterRandomDelay(function () {
				updateProgress(file, that.events, that.demoState, that.queue.isUploading);
				simulateUpload(that)
			})
		} else {
			finishUploading(that)
		}
	};
	startUploading = function (that) {
		that.demoState.currentFile = that.queue.files[that.demoState.fileIdx];
		that.demoState.chunksForCurrentFile = Math.ceil(that.demoState.currentFile / that.demoState.chunkSize);
		that.demoState.bytesUploaded = 0;
		that.queue.isUploading = true;
		that.events.onFileStart.fire(that.demoState.currentFile);
		simulateUpload(that)
	};
	var stopDemo = function (that) {
		var file = that.demoState.currentFile;
		file.filestatus = fluid.uploader.fileStatusConstants.CANCELLED;
		that.queue.shouldStop = true;
		that.events.onFileError.fire(file, fluid.uploader.errorConstants.UPLOAD_STOPPED, "The demo upload was paused by the user.");
		finishAndContinueOrCleanup(that, file);
		that.events.onUploadStop.fire()
	};
	var setupDemo = function (that) {
		if (that.simulateDelay === undefined || that.simulateDelay === null) {
			that.simulateDelay = true
		}
		that.demoState = {
			fileIdx : 0,
			chunkSize : 200000
		};
		return that
	};
	fluid.uploader.demoRemote = function (queue, options) {
		var that = fluid.initLittleComponent("fluid.uploader.demoRemote", options);
		that.queue = queue;
		that.uploadNextFile = function () {
			startUploading(that)
		};
		that.stop = function () {
			stopDemo(that)
		};
		setupDemo(that);
		return that
	};
	fluid.invokeAfterRandomDelay = function (fn) {
		var delay = Math.floor(Math.random() * 1000 + 100);
		setTimeout(fn, delay)
	};
	fluid.defaults("fluid.uploader.demoRemote", {
		gradeNames : ["fluid.eventedComponent"],
		argumentMap : {
			options : 1
		},
		events : {
			onFileProgress : "{multiFileUploader}.events.onFileProgress",
			afterFileComplete : "{multiFileUploader}.events.afterFileComplete",
			afterUploadComplete : "{multiFileUploader}.events.afterUploadComplete",
			onFileSuccess : "{multiFileUploader}.events.onFileSuccess",
			onFileStart : "{multiFileUploader}.events.onFileStart",
			onFileError : "{multiFileUploader}.events.onFileError",
			onUploadStop : "{multiFileUploader}.events.onUploadStop"
		}
	});
	fluid.demands("fluid.uploader.remote", ["fluid.uploader.multiFileUploader", "fluid.uploader.demo"], {
		funcName : "fluid.uploader.demoRemote",
		args : ["{multiFileUploader}.queue", "{multiFileUploader}.events", fluid.COMPONENT_OPTIONS]
	})
})(jQuery, fluid_1_4);
var fluid_1_4 = fluid_1_4 || {};
(function (fluid) {
	fluid.uploader = fluid.uploader || {};
	fluid.uploader.mimeTypeRegistry = {
		jpg : "image/jpeg",
		jpeg : "image/jpeg",
		bmp : "image/bmp",
		png : "image/png",
		tif : "image/tiff",
		tiff : "image/tiff",
		mp3 : "audio/mpeg",
		m4a : "audio/mp4a-latm",
		ogg : "audio/ogg",
		wav : "audio/x-wav",
		aiff : "audio/x-aiff",
		mpg : "video/mpeg",
		mpeg : "video/mpeg",
		m4v : "video/x-m4v",
		ogv : "video/ogg",
		mov : "video/quicktime",
		avi : "video/x-msvideo",
		html : "text/html",
		htm : "text/html",
		text : "text/plain",
		doc : "application/msword",
		docx : "application/msword",
		xls : "application/vnd.ms-excel",
		xlsx : "application/vnd.ms-excel",
		ppt : "application/vnd.ms-powerpoint",
		pptx : "application/vnd.ms-powerpoint"
	}
})(fluid_1_4);
