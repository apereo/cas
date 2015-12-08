/*!
 * jQuery QueryBuilder 2.2.0
 * Copyright 2014-2015 Damien "Mistic" Sorel (http://www.strangeplanet.fr)
 * Licensed under MIT (http://opensource.org/licenses/MIT)
 */
! function(a, b) {
    "function" == typeof define && define.amd ? define(["jquery", "jquery-extendext"], b) : b(a.jQuery)
}(this, function($) {
    "use strict";

    function a(b) {
        return this instanceof a ? (this.root = null, void(this.$ = $(this))) : a.getModel(b)
    }

    function b(a, b) {
        b.forEach(function(b) {
            Object.defineProperty(a.prototype, b, {
                enumerable: !0,
                get: function() {
                    return this.__[b]
                },
                set: function(a) {
                    var c = null !== this.__[b] && "object" == typeof this.__[b] ? $.extend({}, this.__[b]) : this.__[b];
                    this.__[b] = a, null !== this.model && this.model.trigger("update", this, b, a, c)
                }
            })
        })
    }

    function c(a, b) {
        a && ($.isArray(a) ? a.forEach(function(a) {
            $.isPlainObject(a) ? $.each(a, function(a, c) {
                return b(a, c), !1
            }) : b(a, a)
        }) : $.each(a, function(a, c) {
            b(a, c)
        }))
    }

    function d(a, b) {
        return b = Array.prototype.slice.call(arguments), a.replace(/{([0-9]+)}/g, function(a, c) {
            return b[parseInt(c) + 1]
        })
    }

    function e() {
        $.error(d.apply(null, arguments))
    }

    function f(a, b, c) {
        switch (b) {
            case "integer":
                return parseInt(a);
            case "double":
                return parseFloat(a);
            case "boolean":
                var d = "true" === a.trim().toLowerCase() || "1" === a.trim() || 1 === a;
                return c ? d ? 1 : 0 : d;
            default:
                return a
        }
    }

    function g(a) {
        return "string" != typeof a ? a : a.replace(/[\0\n\r\b\\\'\"]/g, function(a) {
            switch (a) {
                case "\x00":
                    return "\\0";
                case "\n":
                    return "\\n";
                case "\r":
                    return "\\r";
                case "\b":
                    return "\\b";
                default:
                    return "\\" + a
            }
        }).replace(/\t/g, "\\t").replace(/\x1a/g, "\\Z")
    }

    function h(a) {
        return a.replace(/[\-\[\]\/\{\}\(\)\*\+\?\.\\\^\$\|]/g, "\\$&")
    }

    function i(b, c) {
        var d;
        return d = c.closest(".rule-container"), d.length ? void b.moveAfter(a(d)) : (d = c.closest(".rules-group-header"), d.length ? (d = c.closest(".rules-group-container"), void b.moveAtBegin(a(d))) : (d = c.closest(".rules-group-container"), d.length ? void b.moveAtEnd(a(d)) : void 0))
    }
    var j = function(a, b) {
            this.init(a, b)
        },
        k = Array.prototype.slice;
    $.extend(j.prototype, {
        change: function(a, b) {
            var c = new $.Event(a + ".queryBuilder.filter", {
                builder: this,
                value: b
            });
            return this.$el.triggerHandler(c, k.call(arguments, 2)), c.value;
            $('select').selectpicker();
        },
        trigger: function(a) {
            var b = new $.Event(a + ".queryBuilder", {
                builder: this
            });
            return this.$el.triggerHandler(b, k.call(arguments, 1)), b
        },
        on: function(a, b) {
            return this.$el.on(a + ".queryBuilder", b), this
        },
        off: function(a, b) {
            return this.$el.off(a + ".queryBuilder", b), this
        },
        once: function(a, b) {
            return this.$el.one(a + ".queryBuilder", b), this
        }
    }), j.plugins = {}, j.defaults = function(a) {
        return "object" != typeof a ? "string" == typeof a ? "object" == typeof j.DEFAULTS[a] ? $.extend(!0, {}, j.DEFAULTS[a]) : j.DEFAULTS[a] : $.extend(!0, {}, j.DEFAULTS) : void $.extendext(!0, "replace", j.DEFAULTS, a)
    }, j.define = function(a, b, c) {
        j.plugins[a] = {
            fct: b,
            def: c || {}
        }
    }, j.extend = function(a) {
        $.extend(j.prototype, a)
    }, j.prototype.initPlugins = function() {
        if (this.plugins) {
            if ($.isArray(this.plugins)) {
                var a = {};
                this.plugins.forEach(function(b) {
                    a[b] = null
                }), this.plugins = a
            }
            Object.keys(this.plugins).forEach(function(a) {
                a in j.plugins ? (this.plugins[a] = $.extend(!0, {}, j.plugins[a].def, this.plugins[a] || {}), j.plugins[a].fct.call(this, this.plugins[a])) : e('Unable to find plugin "{0}"', a)
            }, this)
        }
    }, j.types = {
        string: "string",
        integer: "number",
        "double": "number",
        date: "datetime",
        time: "datetime",
        datetime: "datetime",
        "boolean": "boolean"
    }, j.inputs = ["text", "textarea", "radio", "checkbox", "select"], j.modifiable_options = ["display_errors", "allow_groups", "allow_empty"], j.regional = {}, j.DEFAULTS = {
        filters: [],
        plugins: [],
        display_errors: !0,
        allow_groups: -1,
        allow_empty: !1,
        conditions: ["AND", "OR"],
        default_condition: "AND",
        inputs_separator: " , ",
        select_placeholder: "------",
        default_rule_flags: {
            filter_readonly: !1,
            operator_readonly: !1,
            value_readonly: !1,
            no_delete: !1
        },
        template: {
            group: null,
            rule: null
        },
        lang_code: "en",
        lang: {},
        operators: [{
            type: "equal",
            nb_inputs: 1,
            multiple: !1,
            apply_to: ["string", "number", "datetime", "boolean"]
        }, {
            type: "not_equal",
            nb_inputs: 1,
            multiple: !1,
            apply_to: ["string", "number", "datetime", "boolean"]
        }, {
            type: "in",
            nb_inputs: 1,
            multiple: !0,
            apply_to: ["string", "number", "datetime"]
        }, {
            type: "not_in",
            nb_inputs: 1,
            multiple: !0,
            apply_to: ["string", "number", "datetime"]
        }, {
            type: "less",
            nb_inputs: 1,
            multiple: !1,
            apply_to: ["number", "datetime"]
        }, {
            type: "less_or_equal",
            nb_inputs: 1,
            multiple: !1,
            apply_to: ["number", "datetime"]
        }, {
            type: "greater",
            nb_inputs: 1,
            multiple: !1,
            apply_to: ["number", "datetime"]
        }, {
            type: "greater_or_equal",
            nb_inputs: 1,
            multiple: !1,
            apply_to: ["number", "datetime"]
        }, {
            type: "between",
            nb_inputs: 2,
            multiple: !1,
            apply_to: ["number", "datetime"]
        }, {
            type: "begins_with",
            nb_inputs: 1,
            multiple: !1,
            apply_to: ["string"]
        }, {
            type: "not_begins_with",
            nb_inputs: 1,
            multiple: !1,
            apply_to: ["string"]
        }, {
            type: "contains",
            nb_inputs: 1,
            multiple: !1,
            apply_to: ["string"]
        }, {
            type: "not_contains",
            nb_inputs: 1,
            multiple: !1,
            apply_to: ["string"]
        }, {
            type: "ends_with",
            nb_inputs: 1,
            multiple: !1,
            apply_to: ["string"]
        }, {
            type: "not_ends_with",
            nb_inputs: 1,
            multiple: !1,
            apply_to: ["string"]
        }, {
            type: "is_empty",
            nb_inputs: 0,
            multiple: !1,
            apply_to: ["string"]
        }, {
            type: "is_not_empty",
            nb_inputs: 0,
            multiple: !1,
            apply_to: ["string"]
        }, {
            type: "is_null",
            nb_inputs: 0,
            multiple: !1,
            apply_to: ["string", "number", "datetime", "boolean"]
        }, {
            type: "is_not_null",
            nb_inputs: 0,
            multiple: !1,
            apply_to: ["string", "number", "datetime", "boolean"]
        }],
        icons: {
            add_group: "glyphicon glyphicon-plus-sign",
            add_rule: "glyphicon glyphicon-plus",
            remove_group: "glyphicon glyphicon-remove",
            remove_rule: "glyphicon glyphicon-remove",
            error: "glyphicon glyphicon-warning-sign"
        }
    }, j.prototype.init = function(b, c) {
        b[0].queryBuilder = this, this.$el = b, this.settings = $.extendext(!0, "replace", {}, j.DEFAULTS, c), this.model = new a, this.status = {
            group_id: 0,
            rule_id: 0,
            generated_id: !1,
            has_optgroup: !1,
            id: null,
            updating_value: !1
        }, this.settings.allow_groups === !1 ? this.settings.allow_groups = 0 : this.settings.allow_groups === !0 && (this.settings.allow_groups = -1), this.filters = this.settings.filters, this.icons = this.settings.icons, this.operators = this.settings.operators, this.template = this.settings.template, this.plugins = this.settings.plugins, void 0 === j.regional.en && e('"i18n/en.js" not loaded.'), this.lang = $.extendext(!0, "replace", {}, j.regional.en, j.regional[this.settings.lang_code], this.settings.lang), null === this.template.group && (this.template.group = this.getGroupTemplate), null === this.template.rule && (this.template.rule = this.getRuleTemplate), this.$el.attr("id") || (this.$el.attr("id", "qb_" + Math.floor(99999 * Math.random())), this.status.generated_id = !0), this.status.id = this.$el.attr("id"), this.$el.addClass("query-builder form-inline"), this.checkFilters(), this.bindEvents(), this.initPlugins(), this.trigger("afterInit"), c.rules ? (this.setRules(c.rules), delete this.settings.rules) : this.setRoot(!0)
    }, j.prototype.checkFilters = function() {
        var a = [],
            b = this;
        if (this.filters && 0 !== this.filters.length || e("Missing filters list"), this.filters.forEach(function(c, d) {
                switch (c.id || e("Missing filter {0} id", d), -1 != a.indexOf(c.id) && e('Filter "{0}" already defined', c.id), a.push(c.id), c.type ? j.types[c.type] || e('Invalid type "{0}"', c.type) : c.type = "string", c.input ? "function" != typeof c.input && -1 == j.inputs.indexOf(c.input) && e('Invalid input "{0}"', c.input) : c.input = "text", c.field || (c.field = c.id), c.label || (c.label = c.field), c.optgroup ? b.status.has_optgroup = !0 : c.optgroup = null, c.input) {
                    case "radio":
                    case "checkbox":
                        (!c.values || c.values.length < 1) && e('Missing filter "{0}" values', c.id)
                }
            }), this.status.has_optgroup) {
            var c = [],
                d = [];
            this.filters.forEach(function(a) {
                var b;
                a.optgroup ? (b = c.lastIndexOf(a.optgroup), -1 == b ? b = c.length : b++) : b = c.length, c.splice(b, 0, a.optgroup), d.splice(b, 0, a)
            }), this.filters = d
        }
    }, j.prototype.bindEvents = function() {
        var b = this;
        this.$el.on("change.queryBuilder", ".rules-group-header [name$=_cond]", function() {
            if ($(this).is(":checked")) {
                var b = $(this).closest(".rules-group-container");
                a(b).condition = $(this).val()
            }
            $('select').selectpicker();
        }), this.$el.on("change.queryBuilder", ".rule-filter-container [name$=_filter]", function() {
            var c = $(this).closest(".rule-container");
            a(c).filter = b.getFilterById($(this).val());
            $('select').removeAttr("multiple");
            $('select').selectpicker();
        }), this.$el.on("change.queryBuilder", ".rule-operator-container [name$=_operator]", function() {
            var c = $(this).closest(".rule-container");
            a(c).operator = b.getOperatorByType($(this).val());
            if ($(this).val() == "in" || $(this).val() == "not_in") {
                $(this).closest(".rule-container").find(".rule-value-container select").hide();
                $(this).closest(".rule-container").find(".multiselectClass").attr("multiple", "multiple");
                $(".multiselectClass option[value='-1']").remove();
                $(this).closest(".rule-container").find(".multiselectClass").multiselect({
                    includeSelectAllOption: true,
                    maxHeight: 200,
                    multiple: true
                });
                $(this).closest(".rule-container").find(".rule-value-container div:last-child").css("display", "none");
            } else {
                $(this).closest(".rule-container").find(".multiselectClass").multiselect('destroy');
                $(this).closest(".rule-container").find(".multiselectClass").removeAttr("multiple");
                $(this).closest(".rule-container").find('select').selectpicker();
                $(this).closest(".rule-container").find(".rule-value-container select").hide();
                $(this).closest(".rule-container").find(".rule-value-container div:last-child").css("display", "");
            }
        }), this.$el.on("click.queryBuilder", "[data-add=rule]", function() {
            var c = $(this).closest(".rules-group-container");
            b.addRule(a(c));
            $('.rule-filter-container select').selectpicker();
        }), this.$el.on("click.queryBuilder", "[data-delete=rule]", function() {
            var c = $(this).closest(".rule-container");
            b.deleteRule(a(c));
            $('select').selectpicker();
            if ($(".rule-container").length < 4) {
                $(".scrollDiv").parent().removeAttr("style");
                $(".scrollDiv").removeAttr("style");
                return false;
            }
        }), 0 !== this.settings.allow_groups && (this.$el.on("click.queryBuilder", "[data-add=group]", function() {
            var c = $(this).closest(".rules-group-container");
            b.addGroup(a(c));
            $('select').selectpicker();
        }), this.$el.on("click.queryBuilder", "[data-delete=group]", function() {
            var c = $(this).closest(".rules-group-container");
            b.deleteGroup(a(c));
            if ($(".rule-container").length < 4) {
                $(".scrollDiv").parent().removeAttr("style");
                $(".scrollDiv").removeAttr("style");
                return false;
            }
        })), this.model.on({
            drop: function(a, b) {
                b.$el.remove()
            },
            add: function(a, b, c) {
                b.$el.detach(), 0 === c ? b.$el.prependTo(b.parent.$el.find(">.rules-group-body>.rules-list")) : b.$el.insertAfter(b.parent.rules[c - 1].$el)
            },
            update: function(a, c, d, e, f) {
                switch (d) {
                    case "error":
                        b.displayError(c);
                        break;
                    case "condition":
                        b.updateGroupCondition(c);
                        break;
                    case "filter":
                        b.updateRuleFilter(c);
                        break;
                    case "operator":
                        b.updateRuleOperator(c, f);
                        break;
                    case "flags":
                        b.applyRuleFlags(c);
                        break;
                    case "value":
                        b.updateRuleValue(c)
                }
            }
        })
    }, j.prototype.setRoot = function(a, b) {
        a = void 0 === a || a === !0;
        var c = this.nextGroupId(),
            d = $(this.template.group.call(this, c, 1));
        return this.$el.append(d), this.model.root = new m(null, d), this.model.root.model = this.model, this.model.root.condition = this.settings.default_condition, void 0 !== b && (this.model.root.data = b), a && this.addRule(this.model.root), this.model.root
    }, j.prototype.addGroup = function(a, b, c) {
        b = void 0 === b || b === !0;
        var d = a.level + 1,
            e = this.trigger("beforeAddGroup", a, b, d);
        if (e.isDefaultPrevented()) return null;
        var f = this.nextGroupId(),
            g = $(this.template.group.call(this, f, d)),
            h = a.addGroup(g);
        return void 0 !== c && (h.data = c), this.trigger("afterAddGroup", h), h.condition = this.settings.default_condition, b && this.addRule(h), h
    }, j.prototype.deleteGroup = function(a) {
        if (a.isRoot()) return !1;
        var b = this.trigger("beforeDeleteGroup", a);
        if (b.isDefaultPrevented()) return !1;
        var c = !0;
        return a.each("reverse", function(a) {
            c &= this.deleteRule(a)
        }, function(a) {
            c &= this.deleteGroup(a)
        }, this), c && (a.drop(), this.trigger("afterDeleteGroup")), c
    }, j.prototype.updateGroupCondition = function(a) {
        a.$el.find(">.rules-group-header [name$=_cond]").each(function() {
            var b = $(this);
            b.prop("checked", b.val() === a.condition), b.parent().toggleClass("active", b.val() === a.condition)
        }), this.trigger("afterUpdateGroupCondition", a)
    }, j.prototype.addRule = function(a, b) {
        var c = this.trigger("beforeAddRule", a);
        if (c.isDefaultPrevented()) return null;
        var d = this.nextRuleId(),
            e = $(this.template.rule.call(this, d)),
            f = a.addRule(e);
        return void 0 !== b && (f.data = b), this.trigger("afterAddRule", f), this.createRuleFilters(f), f
    }, j.prototype.deleteRule = function(a) {
        if (a.flags.no_delete) return !1;
        var b = this.trigger("beforeDeleteRule", a);
        return b.isDefaultPrevented() ? !1 : (a.drop(), this.trigger("afterDeleteRule"), !0)
    }, j.prototype.createRuleFilters = function(a) {
        var b = this.change("getRuleFilters", this.filters, a),
            c = $(this.getRuleFilterSelect(a, b));
        a.$el.find(".rule-filter-container").append(c), this.trigger("afterCreateRuleFilters", a)
    }, j.prototype.createRuleOperators = function(a) {
        var b = a.$el.find(".rule-operator-container").empty();
        if (a.filter) {
            var c = this.getOperators(a.filter),
                d = $(this.getRuleOperatorSelect(a, c));
            b.html(d), a.__.operator = c[0], this.trigger("afterCreateRuleOperators", a, c)
        }
    }, j.prototype.createRuleInput = function(a) {
        var b = a.$el.find(".rule-value-container").empty();
        if (a.__.value = void 0, a.filter && a.operator && 0 !== a.operator.nb_inputs) {
            for (var c = this, d = $(), e = a.filter, f = 0; f < a.operator.nb_inputs; f++) {
                var g = $(this.getRuleInput(a, f));
                f > 0 && b.append(this.settings.inputs_separator), b.append(g), d = d.add(g)
            }
            b.show(), d.on("change", function() {
                c.status.updating_value = !0, a.value = c.getRuleValue(a), c.status.updating_value = !1;
                $('select').selectpicker();
            }), e.plugin && d[e.plugin](e.plugin_config || {}), this.trigger("afterCreateRuleInput", a), void 0 !== e.default_value && (a.value = e.default_value)
        }
    }, j.prototype.updateRuleFilter = function(a) {
        this.createRuleOperators(a), this.createRuleInput(a), a.$el.find(".rule-filter-container [name$=_filter]").val(a.filter ? a.filter.id : "-1"), this.trigger("afterUpdateRuleFilter", a)
    }, j.prototype.updateRuleOperator = function(a, b) {
        var c = a.$el.find(".rule-value-container");
        a.operator && 0 !== a.operator.nb_inputs ? (c.show(), (c.is(":empty") || a.operator.nb_inputs !== b.nb_inputs) && this.createRuleInput(a)) : (c.hide(), a.__.value = void 0), a.operator && a.$el.find(".rule-operator-container [name$=_operator]").val(a.operator.type), this.trigger("afterUpdateRuleOperator", a)
    }, j.prototype.updateRuleValue = function(a) {
        this.status.updating_value || this.setRuleValue(a, a.value), this.trigger("afterUpdateRuleValue", a)
    }, j.prototype.applyRuleFlags = function(a) {
        var b = a.flags;
        b.filter_readonly && a.$el.find("[name$=_filter]").prop("disabled", !0), b.operator_readonly && a.$el.find("[name$=_operator]").prop("disabled", !0), b.value_readonly && a.$el.find("[name*=_value_]").prop("disabled", !0), b.no_delete && a.$el.find("[data-delete=rule]").remove(), this.trigger("afterApplyRuleFlags", a)
    }, j.prototype.clearErrors = function(a) {
        a = a || this.model.root, a && (a.error = null, a instanceof m && a.each(function(a) {
            a.error = null
        }, function(a) {
            this.clearErrors(a)
        }, this))
    }, j.prototype.displayError = function(a) {
        if (this.settings.display_errors)
            if (null === a.error) a.$el.removeClass("has-error");
            else {
                var b = $.extend([], a.error, [this.lang.errors[a.error[0]] || a.error[0]]);
                a.$el.addClass("has-error").find(".error-container").eq(0).attr("title", d.apply(null, b))
            }
    }, j.prototype.triggerValidationError = function(a, b, c) {
        $.isArray(b) || (b = [b]);
        var d = this.trigger("validationError", a, b, c);
        d.isDefaultPrevented() || (a.error = b)
    }, j.prototype.destroy = function() {
        this.trigger("beforeDestroy"), this.status.generated_id && this.$el.removeAttr("id"), this.clear(), this.model = null, this.$el.off(".queryBuilder").removeClass("query-builder").removeData("queryBuilder"), delete this.$el[0].queryBuilder
    }, j.prototype.reset = function() {
        this.status.group_id = 1, this.status.rule_id = 0, this.model.root.empty(), this.addRule(this.model.root), this.trigger("afterReset")
    }, j.prototype.clear = function() {
        this.status.group_id = 0, this.status.rule_id = 0, this.model.root && (this.model.root.drop(), this.model.root = null), this.trigger("afterClear")
    }, j.prototype.setOptions = function(a) {
        $.makeArray($(Object.keys(a)).filter(j.modifiable_options)).forEach(function(b) {
            this.settings[b] = a[b]
        }, this)
    }, j.prototype.validate = function() {
        this.clearErrors();
        var a = this,
            b = function c(b) {
                var d = 0,
                    e = 0;
                return b.each(function(b) {
                    if (!b.filter) return a.triggerValidationError(b, "no_filter", null), void e++;
                    if (0 !== b.operator.nb_inputs) {
                        var c = a.validateValue(b, b.value);
                        if (c !== !0) return a.triggerValidationError(b, c, b.value), void e++
                    }
                    d++
                }, function(a) {
                    c(a) ? d++ : e++
                }), e > 0 ? !1 : 0 !== d || a.settings.allow_empty && b.isRoot() ? !0 : (a.triggerValidationError(b, "empty_group", null), !1)
            }(this.model.root);
        return this.change("validate", b)
    }, j.prototype.getRules = function() {
        if (!this.validate()) return {};
        var a = function b(a) {
            var c = {
                condition: a.condition,
                rules: []
            }; 
            return a.data && (c.data = $.extendext(!0, "replace", {}, a.data)), a.each(function(a) {
                var b = null;
                0 !== a.operator.nb_inputs && (b = a.value);
                var d = {
                    id: a.filter.id,
                    field: a.filter.field,
                    type: a.filter.type,
                    input: a.filter.input,
                    operator: a.operator.type,
                    value: b
                };
                (a.filter.data || a.data) && (d.data = $.extendext(!0, "replace", {}, a.filter.data, a.data)), c.rules.push(d)               
            }, function(a) {
                c.rules.push(b(a))
            }), c
        }(this.model.root);
        return this.change("getRules", a)
    }, j.prototype.setRules = function(a) {
        a && a.rules && (0 !== a.rules.length || this.settings.allow_empty) || e("Incorrect data object passed"), this.clear(), this.setRoot(!1, a.data), a = this.change("setRules", a);
        var b = this;
        ! function c(a, d) {
            null !== d && (void 0 === a.condition ? a.condition = b.settings.default_condition : -1 == b.settings.conditions.indexOf(a.condition) && e('Invalid condition "{0}"', a.condition), d.condition = a.condition, a.rules.forEach(function(a) {
                var f;
                if (a.rules && a.rules.length > 0) - 1 != b.settings.allow_groups && b.settings.allow_groups < d.level ? (b.reset(), e("No more than {0} groups are allowed", b.settings.allow_groups)) : (f = b.addGroup(d, !1, a.data), c(a, f));
                else {
                    if (void 0 === a.id && e("Missing rule field id"), void 0 === a.operator && (a.operator = "equal"), f = b.addRule(d, a.data), null === f) return;
                    f.filter = b.getFilterById(a.id), f.operator = b.getOperatorByType(a.operator), f.flags = b.parseRuleFlags(a), 0 !== f.operator.nb_inputs && void 0 !== a.value && (f.value = a.value)
                }
            }))
        }(a, this.model.root)
    }, j.prototype.validateValue = function(a, b) {
        var c = a.filter.validation || {},
            d = !0;
        return d = c.callback ? c.callback.call(this, b, a) : this.validateValueInternal(a, b), this.change("validateValue", d, b, a)
    }, j.prototype.validateValueInternal = function(a, b) {
        var c, d = a.filter,
            f = a.operator,
            g = d.validation || {},
            h = !0;
        b = 1 === a.operator.nb_inputs ? [b] : b;
        for (var i = 0; i < f.nb_inputs; i++) {
            switch (d.input) {
                case "radio":
                    if (void 0 === b[i]) {
                        h = ["radio_empty"];
                        break
                    }
                    break;
                case "checkbox":
                    if (void 0 === b[i] || 0 === b[i].length) {
                        h = ["checkbox_empty"];
                        break
                    }
                    if (!f.multiple && b[i].length > 1) {
                        h = ["operator_not_multiple", this.lang[f.type] || f.type];
                        break
                    }
                    break;
                case "select":
                    if (d.multiple) {
                    	 if (void 0 === b[i] || 0 === b[i].length || b[i] == "-1") {  
                            h = ["select_empty"];
                            break
                        }
                        if (!f.multiple && b[i].length > 1) {
                            h = ["operator_not_multiple", this.lang[f.type] || f.type];
                            break
                        }
                    } else if (void 0 === b[i]) {
                        h = ["select_empty"];
                        break
                    }
                    break;
                default:
                    switch (j.types[d.type]) {
                        case "string":
                            if (void 0 === b[i] || 0 === b[i].length) {
                                h = ["string_empty"];
                                break
                            }
                            if (void 0 !== g.min && b[i].length < parseInt(g.min)) {
                                h = ["string_exceed_min_length", g.min];
                                break
                            }
                            if (void 0 !== g.max && b[i].length > parseInt(g.max)) {
                                h = ["string_exceed_max_length", g.max];
                                break
                            }
                            if (g.format && ("string" == typeof g.format && (g.format = new RegExp(g.format)), !g.format.test(b[i]))) {
                                h = ["string_invalid_format", g.format];
                                break
                            }
                            break;
                        case "number":
                            if (void 0 === b[i] || isNaN(b[i])) {
                                h = ["number_nan"];
                                break
                            }
                            if ("integer" == d.type) {
                                if (parseInt(b[i]) != b[i]) {
                                    h = ["number_not_integer"];
                                    break
                                }
                            } else if (parseFloat(b[i]) != b[i]) {
                                h = ["number_not_double"];
                                break
                            }
                            if (void 0 !== g.min && b[i] < parseFloat(g.min)) {
                                h = ["number_exceed_min", g.min];
                                break
                            }
                            if (void 0 !== g.max && b[i] > parseFloat(g.max)) {
                                h = ["number_exceed_max", g.max];
                                break
                            }
                            if (void 0 !== g.step) {
                                var k = b[i] / g.step;
                                if (parseInt(k) != k) {
                                    h = ["number_wrong_step", g.step];
                                    break
                                }
                            }
                            break;
                        case "datetime":
                            if (void 0 === b[i] || 0 === b[i].length) {
                                h = ["datetime_empty"];
                                break
                            }
                            if (g.format) {
                                "moment" in window || e("MomentJS is required for Date/Time validation. Get it here http://momentjs.com");
                                var l = moment(b[i], g.format);
                                if (!l.isValid()) {
                                    h = ["datetime_invalid"];
                                    break
                                }
                                if (g.min && l < moment(g.min, g.format)) {
                                    h = ["datetime_exceed_min", g.min];
                                    break
                                }
                                if (g.max && l > moment(g.max, g.format)) {
                                    h = ["datetime_exceed_max", g.max];
                                    break
                                }
                            }
                            break;
                        case "boolean":
                            if (c = b[i].trim().toLowerCase(), "true" !== c && "false" !== c && "1" !== c && "0" !== c && 1 !== b[i] && 0 !== b[i]) {
                                h = ["boolean_not_valid"];
                                break
                            }
                    }
            }
            if (h !== !0) break
        }
        return h
    }, j.prototype.nextGroupId = function() {
        return this.status.id + "_group_" + this.status.group_id++
    }, j.prototype.nextRuleId = function() {
        return this.status.id + "_rule_" + this.status.rule_id++
    }, j.prototype.getOperators = function(a) {
        "string" == typeof a && (a = this.getFilterById(a));
        for (var b = [], c = 0, d = this.operators.length; d > c; c++) {
            if (a.operators) {
                if (-1 == a.operators.indexOf(this.operators[c].type)) continue
            } else if (-1 == this.operators[c].apply_to.indexOf(j.types[a.type])) continue;
            b.push(this.operators[c])
        }
        return a.operators && b.sort(function(b, c) {
            return a.operators.indexOf(b.type) - a.operators.indexOf(c.type)
        }), this.change("getOperators", b, a)
    }, j.prototype.getFilterById = function(a) {
        if ("-1" == a) return null;
        for (var b = 0, c = this.filters.length; c > b; b++)
            if (this.filters[b].id == a) return this.filters[b];
        e('Undefined filter "{0}"', a)
    }, j.prototype.getOperatorByType = function(a) {
        if ("-1" == a) return null;
        for (var b = 0, c = this.operators.length; c > b; b++)
            if (this.operators[b].type == a) return this.operators[b];
        e('Undefined operator  "{0}"', a)
    }, j.prototype.getRuleValue = function(a) {
        var b = a.filter,
            c = a.operator,
            d = [];
        if (b.valueGetter) d = b.valueGetter.call(this, a);
        else {
            for (var e, f = a.$el.find(".rule-value-container"), g = 0; g < c.nb_inputs; g++) {
                var h = a.id + "_value_" + g;
                switch (b.input) {
                    case "radio":
                        d.push(f.find("[name=" + h + "]:checked").val());
                        break;
                    case "checkbox":
                        e = [], f.find("[name=" + h + "]:checked").each(function() {
                            e.push($(this).val())
                        }), d.push(e);
                        break;
                    case "select":
                        b.multiple ? (e = [], f.find("[name=" + h + "] option:selected").each(function() {
                            e.push($(this).val())
                        }), d.push(e)) : d.push(f.find("[name=" + h + "] option:selected").val());
                        break;
                    default:
                        d.push(f.find("[name=" + h + "]").val())
                }
            }
            1 === c.nb_inputs && (d = d[0]), b.valueParser && (d = b.valueParser.call(this, a, d))
        }
        return this.change("getRuleValue", d, a)
    }, j.prototype.setRuleValue = function(a, b) {
        var c = a.filter,
            d = a.operator;
        if (c.valueSetter) c.valueSetter.call(this, a, b);
        else {
            var e = a.$el.find(".rule-value-container");
            b = 1 == d.nb_inputs ? [b] : b;
            for (var f = 0; f < d.nb_inputs; f++) {
                var g = a.id + "_value_" + f;
                switch (c.input) {
                    case "radio":
                        e.find("[name=" + g + '][value="' + b[f] + '"]').prop("checked", !0).trigger("change");
                        break;
                    case "checkbox":
                        $.isArray(b[f]) || (b[f] = [b[f]]), b[f].forEach(function(a) {
                            e.find("[name=" + g + '][value="' + a + '"]').prop("checked", !0).trigger("change")
                        });
                        break;
                    default:
                        e.find("[name=" + g + "]").val(b[f]).trigger("change")
                }
            }
        }
    }, j.prototype.parseRuleFlags = function(a) {
        var b = $.extend({}, this.settings.default_rule_flags);
        return a.readonly && $.extend(b, {
            filter_readonly: !0,
            operator_readonly: !0,
            value_readonly: !0,
            no_delete: !0
        }), a.flags && $.extend(b, a.flags), this.change("parseRuleFlags", b, a)
    }, j.prototype.getGroupTemplate = function(a, b) {
        var c = '<dl id="' + a + '" class="rules-group-container">   <dt class="rules-group-header">     <div class="btn-group pull-right group-actions">       <button type="button" class="btn btn-xs btn-success" data-add="rule">         <i class="' + this.icons.add_rule + '"></i> ' + this.lang.add_rule + "       </button>       " + (-1 === this.settings.allow_groups && b === 1 ? '<button type="button" class="btn btn-xs btn-success" data-add="group">           <i class="' + this.icons.add_group + '"></i> ' + this.lang.add_group + "         </button>" : "") + "       " + (b > 1 ? '<button type="button" class="btn btn-xs btn-danger" data-delete="group">           <i class="' + this.icons.remove_group + '"></i> ' + this.lang.delete_group + "         </button>" : "") + '     </div>     <div class="btn-group group-conditions">       ' + this.getGroupConditions(a, b) + "     </div>     " + (this.settings.display_errors ? '<div class="error-container"><i class="' + this.icons.error + '"></i></div>' : "") + "  </dt>   <dd class=rules-group-body>     <ul class=rules-list></ul>   </dd> </dl>";
        return this.change("getGroupTemplate", c, b)
    }, j.prototype.getGroupConditions = function(a, b) {
        for (var c = "", d = 0, e = this.settings.conditions.length; e > d; d++) {
            var f = this.settings.conditions[d],
                g = this.lang.conditions[f] || f;
            c += '        <label class="btn btn-xs btn-primary">           <input type="radio" name="' + a + '_cond" value="' + f + '"> ' + g + "         </label>"
        }
        return this.change("getGroupConditions", c, b)
    }, j.prototype.getRuleTemplate = function(a) {
        var b = '<li id="' + a + '" class="rule-container">   <div class="rule-header">   <div class="btn-group pull-right rule-actions">     <button type="button" class="btn btn-xs btn-danger" data-delete="rule">       <i class="' + this.icons.remove_rule + '"></i> ' + this.lang.delete_rule + "     </button>   </div>   </div>   " + (this.settings.display_errors ? '<div class="error-container"><i class="' + this.icons.error + '"></i></div>' : "") + '  <div class="rule-filter-container col-sm-3"></div>   <div class="rule-operator-container col-sm-3"></div>   <div class="rule-value-container col-sm-3"></div> </li>';
        return this.change("getRuleTemplate", b)
    }, j.prototype.getRuleFilterSelect = function(a, b) {
        var c = null,
            d = '<select class="input-sm select p-0" name="' + a.id + '_filter">';
        return d += '<option value="-1">' + this.settings.select_placeholder + "</option>", b.forEach(function(a) {
            c != a.optgroup && (null !== c && (d += "</optgroup>"), c = a.optgroup, null !== c && (d += '<optgroup label="' + c + '">')), d += '<option id="' + a.datatypeId + '" value="' + a.id + '">' + a.label + "</option>"
        }), null !== c && (d += "</optgroup>"), d += "</select>", this.change("getRuleFilterSelect", d, a)
    }, j.prototype.getRuleOperatorSelect = function(a, b) {
        for (var c = '<select class="input-sm select p-0" name="' + a.id + '_operator">', d = 0, e = b.length; e > d; d++) {
            var f = this.lang.operators[b[d].type] || b[d].type;
            c += '<option value="' + b[d].type + '">' + f + "</option>"
        }
        return c += "</select>", this.change("getRuleOperatorSelect", c, a)
    }, j.prototype.getRuleInput = function(a, b) {
        var d = a.filter,
            e = a.filter.validation || {},
            f = a.id + "_value_" + b,
            g = d.vertical ? " class=block" : "",
            h = "";
        if ("function" == typeof d.input) h = d.input.call(this, a, f);
        else switch (d.input) {
            case "radio":
                c(d.values, function(a, b) {
                    h += "<label" + g + '><input type="radio" name="' + f + '" value="' + a + '"> ' + b + "</label> "
                });
                break;
            case "checkbox":
                c(d.values, function(a, b) {
                    h += "<label" + g + '><input type="checkbox" name="' + f + '" value="' + a + '"> ' + b + "</label> "
                });
                break;
            case "select":
                h += '<select class="input-sm select multiselectClass p-0" multiple="multiple" name="' + f + '"' + (d.multiple ? " multiple" : "") + ">";
                h += '<option value="-1">--select--</option>', c(d.values, function(a, b) {
                	
                    h += '<option value="' + a + '"> ' + b + "</option> "
                }), h += "</select>";
                break;
            case "textarea":
                h += '<textarea class="form-control" name="' + f + '"', d.size && (h += ' cols="' + d.size + '"'), d.rows && (h += ' rows="' + d.rows + '"'), void 0 !== e.min && (h += ' minlength="' + e.min + '"'), void 0 !== e.max && (h += ' maxlength="' + e.max + '"'), d.placeholder && (h += ' placeholder="' + d.placeholder + '"'), h += "></textarea>";
                break;
            default:
                switch (j.types[d.type]) {
                    case "number":
                        h += '<input class="form-control input-sm" type="number" name="' + f + '"', void 0 !== e.step && (h += ' step="' + e.step + '"'), void 0 !== e.min && (h += ' min="' + e.min + '"'), void 0 !== e.max && (h += ' max="' + e.max + '"'), d.placeholder && (h += ' placeholder="' + d.placeholder + '"'), d.size && (h += ' size="' + d.size + '"'), h += ">";
                        break;
                    default:
                        h += '<input class="form-control input-sm" type="text" name="' + f + '"', d.placeholder && (h += ' placeholder="' + d.placeholder + '"'), "string" === d.type && void 0 !== e.min && (h += ' minlength="' + e.min + '"'), "string" === d.type && void 0 !== e.max && (h += ' maxlength="' + e.max + '"'), d.size && (h += ' size="' + d.size + '"'), h += ">"
                }
        }
        return this.change("getRuleInput", h, a, f)
    }, $.extend(a.prototype, {
        trigger: function(a) {
            return this.$.triggerHandler(a, k.call(arguments, 1)), this
        },
        on: function() {
            return this.$.on.apply(this.$, k.call(arguments)), this
        },
        off: function() {
            return this.$.off.apply(this.$, k.call(arguments)), this
        },
        once: function() {
            return this.$.one.apply(this.$, k.call(arguments)), this
        }
    }), a.getModel = function(a) {
        return a ? a instanceof l ? a : $(a).data("queryBuilderModel") : null
    };
    var l = function(a, b) {
        return this instanceof l ? (Object.defineProperty(this, "__", {
            value: {}
        }), b.data("queryBuilderModel", this), this.__.level = 0, this.__.error = null, this.__.data = void 0, this.$el = b, this.id = b[0].id, this.model = null, void(this.parent = a)) : new l
    };
    b(l, ["level", "error", "data"]), Object.defineProperty(l.prototype, "parent", {
        enumerable: !0,
        get: function() {
            return this.__.parent
        },
        set: function(a) {
            this.__.parent = a, this.level = null === a ? 1 : a.level + 1, this.model = null === a ? null : a.model
        }
    }), l.prototype.isRoot = function() {
        return 1 === this.level
    }, l.prototype.getPos = function() {
        return this.isRoot() ? -1 : this.parent.getNodePos(this)
    }, l.prototype.drop = function() {
        null !== this.model && this.model.trigger("drop", this), this.isRoot() || (this.parent._dropNode(this), this.parent = null)
    }, l.prototype.moveAfter = function(a) {
        return this.isRoot() ? void 0 : (this.parent._dropNode(this), a.parent._addNode(this, a.getPos() + 1), this)
    }, l.prototype.moveAtBegin = function(a) {
        return this.isRoot() ? void 0 : (void 0 === a && (a = this.parent), this.parent._dropNode(this), a._addNode(this, 0), this)
    }, l.prototype.moveAtEnd = function(a) {
        return this.isRoot() ? void 0 : (void 0 === a && (a = this.parent), this.parent._dropNode(this), a._addNode(this, a.length()), this)
    };
    var m = function(a, b) {
        return this instanceof m ? (l.call(this, a, b), this.rules = [], void(this.__.condition = null)) : new m(a, b)
    };
    m.prototype = Object.create(l.prototype), m.prototype.constructor = m, b(m, ["condition"]), m.prototype.empty = function() {
        this.each("reverse", function(a) {
            a.drop()
        }, function(a) {
            a.drop()
        })
    }, m.prototype.drop = function() {
        this.empty(), l.prototype.drop.call(this)
    }, m.prototype.length = function() {
        return this.rules.length
    }, m.prototype._addNode = function(a, b) {
        return void 0 === b && (b = this.length()), this.rules.splice(b, 0, a), a.parent = this, null !== this.model && this.model.trigger("add", a, b), a
    }, m.prototype.addGroup = function(a, b) {
        return this._addNode(new m(this, a), b)
    }, m.prototype.addRule = function(a, b) {
        return this._addNode(new n(this, a), b)
    }, m.prototype._dropNode = function(a) {
        var b = this.getNodePos(a);
        return -1 !== b && (a.parent = null, this.rules.splice(b, 1)), this
    }, m.prototype.getNodePos = function(a) {
        return this.rules.indexOf(a)
    }, m.prototype.each = function(a, b, c, d) {
        "function" == typeof a && (d = c, c = b, b = a, a = !1), d = void 0 === d ? null : d;
        for (var e = a ? this.rules.length - 1 : 0, f = a ? 0 : this.rules.length - 1, g = a ? -1 : 1, h = function() {
                return a ? e >= f : f >= e
            }, i = !1; h() && (this.rules[e] instanceof m ? void 0 !== c && (i = c.call(d, this.rules[e]) === !1) : i = b.call(d, this.rules[e]) === !1, !i); e += g);
        return !i
    }, m.prototype.contains = function(a, b) {
        return -1 !== this.getNodePos(a) ? !0 : b ? !this.each(function() {
            return !0
        }, function(b) {
            return !b.contains(a, !0)
        }) : !1
    };
    var n = function(a, b) {
        return this instanceof n ? (l.call(this, a, b), this.__.filter = null, this.__.operator = null, this.__.flags = {}, void(this.__.value = void 0)) : new n(a, b)
    };
    n.prototype = Object.create(l.prototype), n.prototype.constructor = n, b(n, ["filter", "operator", "flags", "value"]), j.Group = m, j.Rule = n, $.fn.queryBuilder = function(a) {
        this.length > 1 && e("Unable to initialize on multiple target");
        var b = this.data("queryBuilder"),
            c = "object" == typeof a && a || {};
        return b || "destroy" != a ? (b || this.data("queryBuilder", new j(this, c)), "string" == typeof a ? b[a].apply(b, Array.prototype.slice.call(arguments, 1)) : this) : this
    }, $.fn.queryBuilder.constructor = j, $.fn.queryBuilder.defaults = j.defaults, $.fn.queryBuilder.extend = j.extend, $.fn.queryBuilder.define = j.define, $.fn.queryBuilder.regional = j.regional, j.define("bt-checkbox", function(a) {
        if ("glyphicons" == a.font) {
            var b = document.createElement("style");
            b.innerHTML = '.checkbox input[type=checkbox]:checked + label:after {     font-family: "Glyphicons Halflings";     content: "\\e013"; } .checkbox label:after {     padding-left: 4px;     padding-top: 2px;     font-size: 9px; }', document.body.appendChild(b)
        }
        this.on("getRuleInput.filter", function(b, d, e) {
            var f = d.filter;
            if (("radio" === f.input || "checkbox" === f.input) && !f.plugin) {
                b.value = "", f.colors || (f.colors = {}), f.color && (f.colors._def_ = f.color);
                var g, h, i = f.vertical ? ' style="display:block"' : "",
                    j = 0;
                c(f.values, function(c, d) {
                    g = f.colors[c] || f.colors._def_ || a.color, h = e + "_" + j++, b.value += "<div" + i + ' class="' + f.input + " " + f.input + "-" + g + '">   <input type="' + f.input + '" name="' + e + '" id="' + h + '" value="' + c + '">   <label for="' + h + '">' + d + "</label> </div>"
                })
            }
        })
    }, {
        font: "glyphicons",
        color: "default"
    }), j.define("bt-selectpicker", function(a) {
        $.fn.selectpicker && $.fn.selectpicker.Constructor || e('Bootstrap Select is required to use "bt-selectpicker" plugin. Get it here: http://silviomoreto.github.io/bootstrap-select'), this.on("afterCreateRuleFilters", function(b, c) {
            c.$el.find(".rule-filter-container select").removeClass("form-control").selectpicker(a)
        }), this.on("afterCreateRuleOperators", function(b, c) {
            c.$el.find(".rule-operator-container select").removeClass("form-control").selectpicker(a)
        }), this.on("afterUpdateRuleFilter", function(a, b) {
            b.$el.find(".rule-filter-container select").selectpicker("render")
        }), this.on("afterUpdateRuleOperator", function(a, b) {
            b.$el.find(".rule-operator-container select").selectpicker("render")
        })
    }, {
        container: "body",
        style: "btn-inverse btn-xs",
        width: "auto",
        showIcon: !1
    }), j.define("bt-tooltip-errors", function(a) {
        $.fn.tooltip && $.fn.tooltip.Constructor && $.fn.tooltip.Constructor.prototype.fixTitle || e('Bootstrap Tooltip is required to use "bt-tooltip-errors" plugin. Get it here: http://getbootstrap.com');
        var b = this;
        this.on("getRuleTemplate.filter", function(a) {
            a.value = a.value.replace('class="error-container"', 'class="error-container" data-toggle="tooltip"')
        }), this.on("getGroupTemplate.filter", function(a) {
            a.value = a.value.replace('class="error-container"', 'class="error-container" data-toggle="tooltip"')
        }), this.model.on("update", function(c, d, e) {
            "error" == e && b.settings.display_errors && d.$el.find(".error-container").eq(0).tooltip(a).tooltip("hide").tooltip("fixTitle")
        })
    }, {
        placement: "right"
    }), j.define("filter-description", function(a) {
        "inline" === a.mode ? this.on("afterUpdateRuleFilter", function(b, c) {
            var d = c.$el.find("p.filter-description");
            c.filter && c.filter.description ? (0 === d.length ? (d = $('<p class="filter-description"></p>'), d.appendTo(c.$el)) : d.show(), d.html('<i class="' + a.icon + '"></i> ' + c.filter.description)) : d.hide()
        }) : "popover" === a.mode ? ($.fn.popover && $.fn.popover.Constructor && $.fn.popover.Constructor.prototype.fixTitle || e('Bootstrap Popover is required to use "filter-description" plugin. Get it here: http://getbootstrap.com'),

            this.on("afterUpdateRuleFilter", function(b, c) {
                var d = c.$el.find("button.filter-description");
                c.filter && c.filter.description ? (0 === d.length ? (d = $('<button type="button" class="btn btn-xs btn-info filter-description" data-toggle="popover"><i class="' + a.icon + '"></i></button>'), d.prependTo(c.$el.find(".rule-actions")), d.popover({
                    placement: "left",
                    container: "body",
                    html: !0
                }), d.on("mouseout", function() {
                    d.popover("hide")
                })) : d.show(), d.data("bs.popover").options.content = c.filter.description, d.attr("aria-describedby") && d.popover("show")) : (d.hide(), d.data("bs.popover") && d.popover("hide"))
            })) : "bootbox" === a.mode && ("bootbox" in window || e('Bootbox is required to use "filter-description" plugin. Get it here: http://bootboxjs.com'), this.on("afterUpdateRuleFilter", function(b, c) {
            var d = c.$el.find("button.filter-description");
            c.filter && c.filter.description ? (0 === d.length && (d = $('<button type="button" class="btn btn-xs btn-info filter-description" data-toggle="bootbox"><i class="' + a.icon + '"></i></button>'), d.prependTo(c.$el.find(".rule-actions")), d.on("click", function() {
                bootbox.alert(d.data("description"))
            })), d.data("description", c.filter.description)) : d.hide()
        }))
    }, {
        icon: "glyphicon glyphicon-info-sign",
        mode: "popover"
    }), j.defaults({
        mongoOperators: {
            equal: function(a) {
                return a[0]
            },
            not_equal: function(a) {
                return {
                    $ne: a[0]
                }
            },
            "in": function(a) {
                return {
                    $in: a
                }
            },
            not_in: function(a) {
                return {
                    $nin: a
                }
            },
            less: function(a) {
                return {
                    $lt: a[0]
                }
            },
            less_or_equal: function(a) {
                return {
                    $lte: a[0]
                }
            },
            greater: function(a) {
                return {
                    $gt: a[0]
                }
            },
            greater_or_equal: function(a) {
                return {
                    $gte: a[0]
                }
            },
            between: function(a) {
                return {
                    $gte: a[0],
                    $lte: a[1]
                }
            },
            begins_with: function(a) {
                return {
                    $regex: "^" + h(a[0])
                }
            },
            not_begins_with: function(a) {
                return {
                    $regex: "^(?!" + h(a[0]) + ")"
                }
            },
            contains: function(a) {
                return {
                    $regex: h(a[0])
                }
            },
            not_contains: function(a) {
                return {
                    $regex: "^((?!" + h(a[0]) + ").)*$",
                    $options: "s"
                }
            },
            ends_with: function(a) {
                return {
                    $regex: h(a[0]) + "$"
                }
            },
            not_ends_with: function(a) {
                return {
                    $regex: "(?<!" + h(a[0]) + ")$"
                }
            },
            is_empty: function() {
                return ""
            },
            is_not_empty: function() {
                return {
                    $ne: ""
                }
            },
            is_null: function() {
                return null
            },
            is_not_null: function() {
                return {
                    $ne: null
                }
            }
        },
        mongoRuleOperators: {
            $ne: function(a) {
                return a = a.$ne, {
                    val: a,
                    op: null === a ? "is_not_null" : "" === a ? "is_not_empty" : "not_equal"
                }
            },
            eq: function(a) {
                return {
                    val: a,
                    op: null === a ? "is_null" : "" === a ? "is_empty" : "equal"
                }
            },
            $regex: function(a) {
                return a = a.$regex, "^(?!" == a.slice(0, 4) && ")" == a.slice(-1) ? {
                    val: a.slice(4, -1),
                    op: "not_begins_with"
                } : "^((?!" == a.slice(0, 5) && ").)*$" == a.slice(-5) ? {
                    val: a.slice(5, -5),
                    op: "not_contains"
                } : "(?<!" == a.slice(0, 4) && ")$" == a.slice(-2) ? {
                    val: a.slice(4, -2),
                    op: "not_ends_with"
                } : "$" == a.slice(-1) ? {
                    val: a.slice(0, -1),
                    op: "ends_with"
                } : "^" == a.slice(0, 1) ? {
                    val: a.slice(1),
                    op: "begins_with"
                } : {
                    val: a,
                    op: "contains"
                }
            },
            between: function(a) {
                return {
                    val: [a.$gte, a.$lte],
                    op: "between"
                }
            },
            $in: function(a) {
                return {
                    val: a.$in,
                    op: "in"
                }
            },
            $nin: function(a) {
                return {
                    val: a.$nin,
                    op: "not_in"
                }
            },
            $lt: function(a) {
                return {
                    val: a.$lt,
                    op: "less"
                }
            },
            $lte: function(a) {
                return {
                    val: a.$lte,
                    op: "less_or_equal"
                }
            },
            $gt: function(a) {
                return {
                    val: a.$gt,
                    op: "greater"
                }
            },
            $gte: function(a) {
                return {
                    val: a.$gte,
                    op: "greater_or_equal"
                }
            }
        }
    }), j.extend({
        getMongo: function(a) {
            a = void 0 === a ? this.getRules() : a;
            var b = this;
            return function c(a) {
                if (a.condition || (a.condition = b.settings.default_condition), -1 === ["AND", "OR"].indexOf(a.condition.toUpperCase()) && e('Unable to build MongoDB query with condition "{0}"', a.condition), !a.rules) return {};
                var d = [];
                a.rules.forEach(function(a) {
                    if (a.rules && a.rules.length > 0) d.push(c(a));
                    else {
                        var g = b.settings.mongoOperators[a.operator],
                            h = b.getOperatorByType(a.operator),
                            i = [];
                        void 0 === g && e('Unknown MongoDB operation for operator "{0}"', a.operator), 0 !== h.nb_inputs && (a.value instanceof Array || (a.value = [a.value]), a.value.forEach(function(b) {
                            i.push(f(b, a.type, !1))
                        }));
                        var j = {};
                        j[a.field] = g.call(b, i), d.push(j)
                    }
                });
                var g = {};
                return d.length > 0 && (g["$" + a.condition.toLowerCase()] = d), g
            }(a)
        },
        getRulesFromMongo: function(a) {
            if (void 0 === a || null === a) return null;
            var b = this,
                c = ["$and", "$or"];
            return function d(a) {
                var f = Object.keys(a);
                f.length > 1 && e("Invalid MongoDB query format."), -1 === c.indexOf(f[0].toLowerCase()) && e('Unable to build Rule from MongoDB query with condition "{0}"', f[0]);
                var g = f[0].toLowerCase() === c[0] ? "AND" : "OR",
                    h = a[f[0]],
                    i = [];
                h.forEach(function(a) {
                    var f = Object.keys(a);
                    if (-1 !== c.indexOf(f[0].toLowerCase())) i.push(d(a));
                    else {
                        var g = f[0],
                            h = a[g],
                            j = b.determineMongoOperator(h, g);
                        void 0 === j && e("Invalid MongoDB query format.");
                        var k = b.settings.mongoRuleOperators[j];
                        void 0 === k && e('JSON Rule operation unknown for operator "{0}"', j);
                        var l = k.call(b, h);
                        i.push({
                            id: b.change("getMongoDBFieldID", g, h),
                            field: g,
                            operator: l.op,
                            value: l.val
                        })
                    }
                });
                var j = {};
                return i.length > 0 && (j.condition = g, j.rules = i), j
            }(a)
        },
        determineMongoOperator: function(a) {
            if (null !== a && "object" == typeof a) {
                var b = Object.keys(a);
                return 1 === b.length ? b[0] : void 0 !== a.$gte && void 0 !== a.$lte ? "between" : void 0 !== a.$regex ? "$regex" : void 0
            }
            return "eq"
        },
        setRulesFromMongo: function(a) {
            this.setRules(this.getRulesFromMongo(a))
        }
    }), j.define("sortable", function(b) {
        this.on("afterInit", function(b) {
            $.event.props.push("dataTransfer");
            var c, d, e = b.builder;
            e.$el.on("mouseover", ".drag-handle", function() {
                e.$el.find(".rule-container, .rules-group-container").attr("draggable", !0)
            }), e.$el.on("mouseout", ".drag-handle", function() {
                e.$el.find(".rule-container, .rules-group-container").removeAttr("draggable")
            }), e.$el.on("dragstart", "[draggable]", function(b) {
                b.stopPropagation(), b.dataTransfer.setData("text", "drag"), d = a(b.target), setTimeout(function() {
                    var a = $('<div class="rule-placeholder">&nbsp;</div>');
                    a.css("min-height", d.$el.height()), c = d.parent.addRule(a, d.getPos()), d.$el.hide()
                }, 0)
            }), e.$el.on("dragenter", "[draggable]", function(a) {
                a.preventDefault(), a.stopPropagation(), c && i(c, $(a.target))
            }), e.$el.on("dragover", "[draggable]", function(a) {
                a.preventDefault(), a.stopPropagation()
            }), e.$el.on("drop", function(a) {
                a.preventDefault(), a.stopPropagation(), i(d, $(a.target))
            }), e.$el.on("dragend", "[draggable]", function(a) {
                a.preventDefault(), a.stopPropagation(), d.$el.show(), c.drop(), d = c = null, e.$el.find(".rule-container, .rules-group-container").removeAttr("draggable")
            })
        }), this.on("parseRuleFlags.filter", function(a) {
            void 0 === a.value.no_sortable && (a.value.no_sortable = b.default_no_sortable)
        }), this.on("afterApplyRuleFlags", function(a, b) {
            b.flags.no_sortable && b.$el.find(".drag-handle").remove()
        }), this.on("getGroupTemplate.filter", function(a, c) {
            if (c > 1) {
                var d = $(a.value);
                d.find(".group-conditions").after('<div class="drag-handle"><i class="' + b.icon + '"></i></div>'), a.value = d.prop("outerHTML")
            }
        }), this.on("getRuleTemplate.filter", function(a) {
            var c = $(a.value);
            c.find(".rule-header").after('<div class="drag-handle pull-left"><i class="' + b.icon + '"></i></div>'), a.value = c.prop("outerHTML")
        })
    }, {
        default_no_sortable: !1,
        icon: "glyphicon glyphicon-sort"
    }), j.defaults({
        sqlOperators: {
            equal: {
                op: "= ?"
            },
            not_equal: {
                op: "!= ?"
            },
            "in": {
                op: "IN(?)",
                sep: ", "
            },
            not_in: {
                op: "NOT IN(?)",
                sep: ", "
            },
            less: {
                op: "< ?"
            },
            less_or_equal: {
                op: "<= ?"
            },
            greater: {
                op: "> ?"
            },
            greater_or_equal: {
                op: ">= ?"
            },
            between: {
                op: "BETWEEN ?",
                sep: " AND "
            },
            begins_with: {
                op: "LIKE(?)",
                fn: function(a) {
                    return a + "%"
                }
            },
            not_begins_with: {
                op: "NOT LIKE(?)",
                fn: function(a) {
                    return a + "%"
                }
            },
            contains: {
                op: "LIKE(?)",
                fn: function(a) {
                    return "%" + a + "%"
                }
            },
            not_contains: {
                op: "NOT LIKE(?)",
                fn: function(a) {
                    return "%" + a + "%"
                }
            },
            ends_with: {
                op: "LIKE(?)",
                fn: function(a) {
                    return "%" + a
                }
            },
            not_ends_with: {
                op: "NOT LIKE(?)",
                fn: function(a) {
                    return "%" + a
                }
            },
            is_empty: {
                op: "= ''"
            },
            is_not_empty: {
                op: "!= ''"
            },
            is_null: {
                op: "IS NULL"
            },
            is_not_null: {
                op: "IS NOT NULL"
            }
        },
        sqlRuleOperator: {
            "=": function(a) {
                return {
                    val: a,
                    op: "" === a ? "is_empty" : "equal"
                }
            },
            "!=": function(a) {
                return {
                    val: a,
                    op: "" === a ? "is_not_empty" : "not_equal"
                }
            },
            LIKE: function(a) {
                return "%" == a.slice(0, 1) && "%" == a.slice(-1) ? {
                    val: a.slice(1, -1),
                    op: "contains"
                } : "%" == a.slice(0, 1) ? {
                    val: a.slice(1),
                    op: "ends_with"
                } : "%" == a.slice(-1) ? {
                    val: a.slice(0, -1),
                    op: "begins_with"
                } : void e("Invalid value for LIKE operator")
            },
            IN: function(a) {
                return {
                    val: a,
                    op: "in"
                }
            },
            "NOT IN": function(a) {
                return {
                    val: a,
                    op: "not_in"
                }
            },
            "<": function(a) {
                return {
                    val: a,
                    op: "less"
                }
            },
            "<=": function(a) {
                return {
                    val: a,
                    op: "less_or_equal"
                }
            },
            ">": function(a) {
                return {
                    val: a,
                    op: "greater"
                }
            },
            ">=": function(a) {
                return {
                    val: a,
                    op: "greater_or_equal"
                }
            },
            BETWEEN: function(a) {
                return {
                    val: a,
                    op: "between"
                }
            },
            IS: function(a) {
                return null !== a && e("Invalid value for IS operator"), {
                    val: null,
                    op: "is_null"
                }
            },
            "IS NOT": function(a) {
                return null !== a && e("Invalid value for IS operator"), {
                    val: null,
                    op: "is_not_null"
                }
            }
        },
        sqlStatements: {
            question_mark: function() {
                var a = [];
                return {
                    add: function(b, c) {
                        return a.push(c), "?"
                    },
                    run: function() {
                        return a
                    }
                }
            },
            numbered: function() {
                var a = 0,
                    b = [];
                return {
                    add: function(c, d) {
                        return b.push(d), a++, "$" + a
                    },
                    run: function() {
                        return b
                    }
                }
            },
            named: function() {
                var a = {},
                    b = {};
                return {
                    add: function(c, d) {
                        a[c.field] || (a[c.field] = 0), a[c.field]++;
                        var e = c.field + "_" + a[c.field];
                        return b[e] = d, ":" + e
                    },
                    run: function() {
                        return b
                    }
                }
            }
        },
        sqlRuleStatement: {
            question_mark: function(a) {
                var b = 0;
                return {
                    get: function(c) {
                        return $.isArray(c) ? c.map(function(c) {
                            return "?" == c ? a[b++] : c
                        }) : "?" == c ? a[b++] : c
                    },
                    esc: function(a) {
                        return a.replace(/\?/g, "'?'")
                    }
                }
            },
            numbered: function(a) {
                return {
                    get: function(b) {
                        return $.isArray(b) ? b.map(function(b) {
                            return /^\$[0-9]+$/.test(b) ? a[b.slice(1) - 1] : b
                        }) : /^\$[0-9]+$/.test(b) ? a[b.slice(1) - 1] : b
                    },
                    esc: function(a) {
                        return a.replace(/\$([0-9]+)/g, "'$$$1'")
                    }
                }
            },
            named: function(a) {
                return {
                    get: function(b) {
                        return $.isArray(b) ? b.map(function(b) {
                            return /^:/.test(b) ? a[b.slice(1)] : b
                        }) : /^:/.test(b) ? a[b.slice(1)] : b
                    },
                    esc: function(b) {
                        return b.replace(new RegExp(":(" + Object.keys(a).join("|") + ")", "g"), "':$1'")
                    }
                }
            }
        }
    }), j.extend({
        getSQL: function(a, b, c) {
            c = void 0 === c ? this.getRules() : c, b = b === !0 ? "\n" : " ", (a === !0 || void 0 === a) && (a = "question_mark"), "string" == typeof a && (a = this.settings.sqlStatements[a]());
            var d = this,
                h = function i(c) {
                    if (c.condition || (c.condition = d.settings.default_condition), -1 === ["AND", "OR"].indexOf(c.condition.toUpperCase()) && e('Unable to build SQL query with condition "{0}"', c.condition), !c.rules) return "";
                    var h = [];
                    return c.rules.forEach(function(c) {
                        if (c.rules && c.rules.length > 0) h.push("(" + b + i(c) + b + ")" + b);
                        else {
                            var j = d.settings.sqlOperators[c.operator],
                                k = d.getOperatorByType(c.operator),
                                l = "";
                            void 0 === j && e('Unknown SQL operation for operator "{0}"', c.operator), 0 !== k.nb_inputs && (c.value instanceof Array || (c.value = [c.value]), c.value.forEach(function(b, d) {
                                d > 0 && (l += j.sep), "integer" == c.type || "double" == c.type || "boolean" == c.type ? b = f(b, c.type, !0) : a || (b = g(b)), j.fn && (b = j.fn(b)), a ? l += a.add(c, b) : ("string" == typeof b && (b = "'" + b + "'"), l += b)
                            })), h.push(c.field + " " + j.op.replace(/\?/, l))
                        }
                    }), h.join(" " + c.condition + b)
                }(c);
            return a ? {
                sql: h,
                params: a.run()
            } : {
                sql: h
            }
        },
        getRulesFromSQL: function(a, b) {
            "SQLParser" in window || e("SQLParser is required to parse SQL queries. Get it here https://github.com/forward/sql-parser");
            var c = this;
            "string" == typeof a && (a = {
                sql: a
            }), "string" == typeof b && (b = this.settings.sqlRuleStatement[b](a.params), a.sql = b.esc(a.sql)), a.sql.toUpperCase().startsWith("SELECT") || (a.sql = "SELECT * FROM table WHERE " + a.sql);
            var d = SQLParser.parse(a.sql);
            d.where || e("No WHERE clause found");
            var f = {
                    condition: this.settings.default_condition,
                    rules: []
                },
                g = f;
            return function h(a, d) {
                if (-1 !== ["AND", "OR"].indexOf(a.operation.toUpperCase())) {
                    d > 0 && g.condition != a.operation.toUpperCase() && (g.rules.push({
                        condition: c.settings.default_condition,
                        rules: []
                    }), g = g.rules[g.rules.length - 1]), g.condition = a.operation.toUpperCase(), d++;
                    var f = g;
                    h(a.left, d), g = f, h(a.right, d)
                } else {
                    (void 0 === a.left.value || void 0 === a.right.value) && e("Missing field and/or value"), $.isPlainObject(a.right.value) && e("Value format not supported for {0}.", a.left.value);
                    var i;
                    i = $.isArray(a.right.value) ? a.right.value.map(function(a) {
                        return a.value
                    }) : a.right.value, b && (i = b.get(i));
                    var j = a.operation.toUpperCase();
                    "<>" == j && (j = "!=");
                    var k;
                    k = "NOT LIKE" == j ? c.settings.sqlRuleOperator.LIKE : c.settings.sqlRuleOperator[j], void 0 === k && e("Invalid SQL operation {0}.", a.operation);
                    var l = k.call(this, i, a.operation);
                    "NOT LIKE" == j && (l.op = "not_" + l.op), g.rules.push({
                        id: c.change("getSQLFieldID", a.left.value, i),
                        field: a.left.value,
                        operator: l.op,
                        value: l.val
                    })
                }
            }(d.where.conditions, 0), f
        },
        setRulesFromSQL: function(a, b) {
            this.setRules(this.getRulesFromSQL(a, b))
        }
    }), j.define("unique-filter", function() {
        this.status.used_filters = {}, this.on("afterUpdateRuleFilter", this.updateDisabledFilters), this.on("afterDeleteRule", this.updateDisabledFilters), this.on("afterCreateRuleFilters", this.applyDisabledFilters)
    }), j.extend({
        updateDisabledFilters: function(a) {
            var b = a.builder;
            b.status.used_filters = {}, b.model && (! function c(a) {
                a.each(function(a) {
                    a.filter && a.filter.unique && (b.status.used_filters[a.filter.id] || (b.status.used_filters[a.filter.id] = []), "group" == a.filter.unique && b.status.used_filters[a.filter.id].push(a.parent))
                }, function(a) {
                    c(a)
                })
            }(b.model.root), b.applyDisabledFilters(a))
        },
        applyDisabledFilters: function(a) {
            var b = a.builder;
            b.$el.find(".rule-filter-container option").prop("disabled", !1), $.each(b.status.used_filters, function(a, c) {
                0 === c.length ? b.$el.find('.rule-filter-container option[value="' + a + '"]:not(:selected)').prop("disabled", !0) : c.forEach(function(b) {
                    b.each(function(b) {
                        b.$el.find('.rule-filter-container option[value="' + a + '"]:not(:selected)').prop("disabled", !0)
                    })
                })
            }), b.settings.plugins && b.settings.plugins["bt-selectpicker"] && b.$el.find(".rule-filter-container select").selectpicker("render")
        }
    }), j.regional.en = {
        __locale: "English (en)",
        __author: 'Damien "Mistic" Sorel, http://www.strangeplanet.fr',
        add_rule: "Add rule",
        add_group: "Add group",
        delete_rule: "",
        delete_group: "Delete",
        conditions: {
            AND: "AND",
            OR: "OR"
        },
        operators: {
            equal: "equal",
            not_equal: "not equal",
            "in": "in",
            not_in: "not in",
            less: "less",
            less_or_equal: "less or equal",
            greater: "greater",
            greater_or_equal: "greater or equal",
            between: "between",
            begins_with: "begins with",
            not_begins_with: "doesn't begin with",
            contains: "contains",
            not_contains: "doesn't contain",
            ends_with: "ends with",
            not_ends_with: "doesn't end with",
            is_empty: "is empty",
            is_not_empty: "is not empty",
            is_null: "is null",
            is_not_null: "is not null"
        },
        errors: {
            no_filter: "No filter selected",
            empty_group: "The group is empty",
            radio_empty: "No value selected",
            checkbox_empty: "No value selected",
            select_empty: "No value selected",
            string_empty: "Empty value",
            string_exceed_min_length: "Must contain at least {0} characters",
            string_exceed_max_length: "Must not contain more than {0} characters",
            string_invalid_format: "Invalid format ({0})",
            number_nan: "Not a number",
            number_not_integer: "Not an integer",
            number_not_double: "Not a real number",
            number_exceed_min: "Must be greater than {0}",
            number_exceed_max: "Must be lower than {0}",
            number_wrong_step: "Must be a multiple of {0}",
            datetime_empty: "Empty value",
            datetime_invalid: "Invalid date format ({0})",
            datetime_exceed_min: "Must be after {0}",
            datetime_exceed_max: "Must be before {0}",
            boolean_not_valid: "Not a boolean",
            operator_not_multiple: "Operator {0} cannot accept multiple values"
        }
    }, j.defaults({
        lang_code: "en"
    })
});