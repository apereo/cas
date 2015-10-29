/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
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

var url = new URL(document.location);
var array = url.pathname.split("/");
if (array.length == 3) {
    appContext = "/" + array[1];
} else {
    appContext = "";
}

(function () {
    var app = angular.module('casmgmt', [
        'ui.sortable'
    ]);
   

    app.filter('checkmark', function () {
        return function (input) {
            return input ? '\u2713' : '\u2718';
        };
    })
        .filter('wordCharTrunc', function () {
            return function (str, limit) {
                if(typeof str != 'string') { return ''; }
                if(!limit || str.length <= limit) { return str; }

                var newStr = str.substring(0, limit).replace(/\w+$/, '');
                return (newStr || str.substring(0, limit)) + '...';
            };
        })
        .filter('serviceTableFilter', function () {
            return function (services, fields, regex) {
                if(typeof fields == 'string') { fields = [fields]; }
                try {
                    regex = regex ? new RegExp(regex, 'i') : false;
                } catch(e) {
                    // TODO: How do we want to tell the user their regex is bad? On error, return list or null?
                    regex = false;
                }
                if(!services || !fields || !regex) { return services; }

                var matches = [];
                angular.forEach(services, function (service, i) {
                    angular.forEach(fields, function (field, j) {
                        if(regex.test(service[field]) && matches.indexOf(service) == -1) {
                            matches.push(service);
                        }
                    });
                });
                return matches;
            };
        });

    app.factory('sharedFactoryCtrl', [
        function () {
            var factory = {assignedId: null};

            factory.httpHeaders = {};
            factory.httpHeaders[ $("meta[name='_csrf_header']").attr("content") ] = $("meta[name='_csrf']").attr("content");

            factory.maxEvalOrder = 0;

            factory.setItem = function (id) {
                factory.assignedId = id;
            };
            factory.clearItem = function () {
                factory.assignedId = null;
            };
            factory.getItem = function () {
                return factory.assignedId;
            };

            factory.forceReload = function() {
                $('#logoutUrlLink')[0].click();
            };

            return factory;
        }
    ]);

// View Swapper
    app.controller('actionsController', [
        '$timeout',
        'sharedFactoryCtrl',
        function ($timeout, sharedFactory) {
            var action = this;

            this.actionPanel = 'manage';

            this.selectAction = function (setAction) {
                action.actionPanel = setAction;
            };

            this.isSelected = function (checkAction) {
                return action.actionPanel === checkAction;
            };

            this.homepage = function () {
                action.selectAction('manage');
                sharedFactory.clearItem();
            };

            this.serviceAdd = function () {
                sharedFactory.clearItem();
                $timeout(function(){ action.selectAction('add'); }, 100);
            };

            this.serviceEdit = function (id) {
                sharedFactory.setItem(id);
                $timeout(function(){ action.selectAction('edit'); }, 100);
            };
        }
    ]);

// Services Table: Manage View
    app.controller('ServicesTableController', [
        '$scope',
        '$http',
        '$timeout',
        'sharedFactoryCtrl',
        function ($scope, $http, $timeout, sharedFactory) {
            var serviceData = this,
                httpHeaders = sharedFactory.httpHeaders,
                delayedAlert = function(n, t, d, skipScrollTop) {
                    skipScrollTop = skipScrollTop || false;
                    $timeout(function () {
                        serviceData.alert = {
                            name:   n,
                            type:   t,
                            data:   d
                        };
                    }, 10);
                    if(!skipScrollTop) {
                        $timeout(function () {
                            $('html, body').animate({
                                scrollTop: $('.service-editor').offset().top
                            }, 750);
                        }, 100);
                    }
                };

            this.dataTable = null; // Prevents 'flashing' on load
            this.sortableOptions = {
                axis: 'y',
                items: '> tr',
                handle: '.grabber-icon',
                placeholder: 'tr-placeholder',
                start: function (e, ui) {
                    serviceData.detailRow = -1;
                    ui.item.data('data_changed', false);
                },
                update: function (e, ui) {
                    ui.item.data('data_changed', true);
                },
                stop: function (e, ui) {
                    if(ui.item.data('data_changed')) {
                        var myData = $(this).sortable('serialize', {key: 'id'});

                        $.ajax({
                            type: 'post',
                            url: appContext + '/updateRegisteredServiceEvaluationOrder.html',
                            data: myData,
                            headers: httpHeaders,
                            dataType: 'json',
                            success: function (data, status) {
                                if(data.status != 200)
                                    delayedAlert('notupdated', 'danger', data);
                                else if(angular.isString(data))
                                    sharedFactory.forceReload();
                                else
                                    serviceData.getServices();
                            },
                            error: function(xhr, status) {
                                if(xhr.status == 403)
                                    sharedFactory.forceReload();
                                else
                                    delayedAlert('notupdated', 'danger', xhr.responseJSON);
                            }
                        });
                    }
                }
            };

            this.getServices = function () {
                $http.get(appContext + '/getServices.html')
                    .then(function (response) {
                        if(response.status != 200) {
                            delayedAlert('listfail', 'danger', response.data);
                        }
                        else {
                            if(serviceData.alert && serviceData.alert.type != 'info')
                                serviceData.alert = null;
                            serviceData.dataTable = response.data.services || [];
                            angular.forEach(serviceData.dataTable, function(service) {
                                if(service.evalOrder > sharedFactory.maxEvalOrder) {
                                    sharedFactory.maxEvalOrder = service.evalOrder;
                                }
                            });
                        }
                    });
            };

            this.openModalDelete = function (item) {
                serviceData.modalItem = item;
                $timeout(function () {
                    $('#confirm-delete .btn-default').focus();
                }, 100);
            };
            this.closeModalDelete = function () {
                serviceData.modalItem = null;
            };
                   

            this.deleteService = function (item) {
                var myData = {id: item.assignedId};

                serviceData.closeModalDelete();
                $.ajax({
                    type: 'post',
                    url: appContext + '/deleteRegisteredService.html',
                    data: myData,
                    headers: httpHeaders,
                    success: function (data, status) {
                        if(data.status != 200)
                            delayedAlert('notdeleted', 'danger', data);
                        else if(angular.isString(data))
                            sharedFactory.forceReload();
                        else {
                            serviceData.getServices();
                            delayedAlert('deleted', 'info', item, true);
                        }
                    },
                    error: function(xhr, status) {
                        if(xhr.status == 403)
                            sharedFactory.forceReload();
                        else
                            delayedAlert('notdeleted', 'danger', xhr.responseJSON);
                    }
                });
            };

            this.clearFilter = function () {
                serviceData.serviceTableQuery = "";
            };

            this.toggleDetail = function (rowId) {
                serviceData.detailRow = serviceData.detailRow == rowId ? -1 : rowId;
            };

            $scope.$watch(
                function() { return sharedFactory.assignedId; },
                function (newAssignedId, oldAssignedId) {
                    if(oldAssignedId && !newAssignedId)
                        serviceData.getServices();
                }
            );

            this.getServices();
        }
    ]);

// Service Form: Add/Edit Service View
    app.controller('ServiceFormController', [
        '$scope',
        '$http',
        '$timeout',
        'sharedFactoryCtrl',
        function ($scope, $http, $timeout, sharedFactory) {
            var serviceForm = this,
                httpHeaders = sharedFactory.httpHeaders,
                delayedAlert = function(n, t, d, skipScrollTop) {
                    skipScrollTop = skipScrollTop || false;
                    $timeout(function () {
                        serviceForm.alert = {
                            name:   n,
                            type:   t,
                            data:   d
                        };
                    }, 10);
                    if(!skipScrollTop) {
                        $timeout(function () {
                            $('html, body').animate({
                                scrollTop: $('.service-editor').offset().top
                            }, 750);
                        }, 100);
                    }
                },
                showInstructions = function () {
                    $('.required-missing').removeClass('required-missing');
                    delayedAlert('instructions', 'info', null, true);
                };

            this.serviceData = {};
            this.formData = {};
            this.radioWatchBypass = true;
            this.showOAuthSecret = false;

            this.selectOptions = {
                serviceTypeList: [
                    {name: 'CAS Client',                value: 'cas'},
                    {name: 'OAuth Client',              value: 'oauth'},
                    {name: 'OAuth Callback Authorize',  value: 'oauth_callback_authz'}
                ],
                logoutTypeList: [
                    {name: 'None',          value: ''},
                    {name: 'Back Channel',  value: 'back'},
                    {name: 'Front Channel', value: 'front'}
                ],
                timeUnitsList: [
                    {name: 'MILLISECONDS',  value: 'MILLISECONDS'},
                    {name: 'SECONDS',       value: 'SECONDS'},
                    {name: 'MINUTES',       value: 'MINUTES'},
                    {name: 'HOURS',         value: 'HOURS'},
                    {name: 'DAYS',          value: 'DAYS'}
                ],
                mergeStrategyList: [
                    {name: 'DEFAULT',       value: 'default'},
                    {name: 'ADD',           value: 'add'},
                    {name: 'MULTI-VALUED',  value: 'multi-valued'},
                    {name: 'REPLACE',       value: 'replace'}
                ]
            };

            this.isSelected = function(option, selected) {
                if(!angular.isArray(selected)) {
                    return option == selected;
                }

                angular.forEach(selected, function(opt) {
                    if(option == opt) return true;
                });
                return false;
            };

            this.isEmpty = function(thing) {
                if(angular.isArray(thing)) { return  thing.length === 0; }
                if(angular.isObject(thing)) { return jQuery.isEmptyObject(thing); }
                return !thing;
            };


            this.saveForm = function () {
                var formErrors;

                serviceDataTransformation('save');
                formErrors = serviceForm.validateForm();

                if(formErrors.length !== 0) {
                    delayedAlert('notvalid', 'danger', formErrors);
                    angular.forEach(formErrors, function(fieldId) {
                        $('#'+fieldId).addClass('required-missing');
                    });
                    return;
                } else $('.required-missing').removeClass('required-missing');

                $.ajax({
                    type: 'post',
                    url: appContext + '/saveService.html',
                    contentType: "application/json; charset=utf-8",
                    dataType: "json",
                    data: JSON.stringify(serviceForm.serviceData),
                    headers: httpHeaders,
                    success: function (data, status) {
                        if(data.status != 200) {
                            delayedAlert('notsaved', 'danger', data);
                            serviceForm.newService();
                        } else if(angular.isString(data)) {
                            sharedFactory.forceReload();
                        } else {

                            var hasIdAssignedAlready = (serviceForm.serviceData.assignedId != undefined
                            && serviceForm.serviceData.assignedId > 0);

                            if (!hasIdAssignedAlready && data.id > 0) {
                                serviceForm.serviceData.assignedId = data.id;
                                sharedFactory.assignedId = data.id;

                                delayedAlert('added', 'info', null);
                            }
                            else {
                                delayedAlert('updated', 'info', null);
                            }
                            $timeout(function () {
                                sharedFactory.clearItem();
                                $('#manageServices').click();
                            }, 200);



                        }
                    },
                    error: function(xhr, status) {
                        if(xhr.status == 403)
                            sharedFactory.forceReload();
                        else
                            delayedAlert('notsaved','danger', xhr.responseJSON);
                    }
                });
            };

            this.validateRegex = function(pattern) {
                try {
                    if (pattern == "")
                        return true;
                    var patt = new RegExp(pattern);
		    return true;
                } catch (e) {
                    return false;
                }
            };

            this.validateForm = function () {
                var err = [],
                    data = serviceForm.serviceData;

                // Service Basics
                if(!data.serviceId) err.push('serviceId');
                if(!data.name) err.push('serviceName');
                if(!data.description) err.push('serviceDesc');
                if(!data.type) err.push('serviceType');
                // OAuth Client Options Only
                if(data.type == 'oauth') {
                    if(!data.oauth.clientId) err.push('oauthClientId');
                    if(!data.oauth.clientSecret) err.push('oauthClientSecret');
                }
                // Username Attribute Provider Options
                if(!data.userAttrProvider.value) {
                    if(data.userAttrProvider.type == 'attr') err.push('uapUsernameAttribute');
                    if(data.userAttrProvider.type == 'anon') err.push('uapSaltSetting');
                }
                // Proxy Policy Options
                if(data.proxyPolicy.type == 'regex' && !data.proxyPolicy.value) err.push('proxyPolicyRegex');

                if(data.proxyPolicy.type == 'regex' && data.proxyPolicy.value != null) {
                    if (!this.validateRegex(data.proxyPolicy.value)) err.push('proxyPolicyRegex');
                }


                // Principle Attribute Repository Options
                if(data.attrRelease.attrOption == 'cached') {
                    if(!data.attrRelease.cachedTimeUnit) err.push('cachedTime');
                    if(!data.attrRelease.mergingStrategy) err.push('mergingStrategy');
                }
                if (data.attrRelease.attrFilter != null) {
                    if (!this.validateRegex(data.attrRelease.attrFilter)) err.push('attFilter');
                }
                return err;
            };

            this.newService = function () {
                serviceForm.radioWatchBypass = true;

                serviceForm.showOAuthSecret = false;
                serviceForm.serviceData = new Object({
                    assignedId: 0,
                    evalOrder: sharedFactory.maxEvalOrder + 1,
                    type: serviceForm.selectOptions.serviceTypeList[0].value,
                    logoutType: serviceForm.selectOptions.logoutTypeList[0].value,
                    attrRelease: {
                        attrOption: 'default',
                        attrPolicy: {type: 'all'},
                        cachedTimeUnit: serviceForm.selectOptions.timeUnitsList[0].value,
                        mergingStrategy: serviceForm.selectOptions.mergeStrategyList[0].value
                    },
                    supportAccess: {casEnabled: true, ssoEnabled:true},
                    publicKey: {algorithm: 'RSA'},
                    userAttrProvider: {type: 'default'},
                    proxyPolicy: {type: 'refuse'}
                });
                serviceDataTransformation('load');
                showInstructions();

                $http.get(appContext + '/getService.html?id=-1')
                    .then(function (response) {
                        if(response.status != 200)
                            delayedAlert('notloaded', 'danger', data);
                        else if(angular.isString(response.data))
                            sharedFactory.forceReload();
                        else
                            serviceForm.formData = response.data.formData;

                    });

                serviceForm.radioWatchBypass = false;
            };

            this.loadService = function (serviceId) {
                serviceForm.radioWatchBypass = true;

                $http.get(appContext + '/getService.html?id=' + serviceId)
                    .then(function (response) {
                        if(response.status != 200) {
                            delayedAlert('notloaded', 'danger', data);
                            serviceForm.newService();
                        }
                        else if(angular.isString(response.data))
                            sharedFactory.forceReload();
                        else {
                            serviceForm.showOAuthSecret = false;
                            if(serviceForm.formData != response.data.formData)
                                serviceForm.formData = response.data.formData;
                            serviceForm.serviceData = response.data.serviceData;
                            serviceDataTransformation('load');
                            showInstructions();
                        }
                    });

                serviceForm.radioWatchBypass = false;
            };

            // Parse the data for textareas to/from a(n) string/array from/to a(n) array/string
            var textareaArrParse = function(dir, value) {
                var newValue;
                if(dir == 'load') {
                    newValue = value ? value.join("\n") : '';
                }
                else {
                    if (value != undefined) {
                        newValue = value.split("\n");
                        for (var i = newValue.length-1; i >= 0; i--) {
                            newValue[i] = newValue[i].trim();
                            if (!newValue[i]) newValue.splice(i, 1);
                        }
                    } else {
                        newValue = [];
                    }
                }
                return newValue;
            };

            // Transform the data so it is ready from/to the form to/from the server.
            var serviceDataTransformation = function(dir) {
                var data = serviceForm.serviceData;

                // Logic safeties
                serviceForm.formData.availableAttributes = serviceForm.formData.availableAttributes || [];
                data.supportAccess.requiredAttr = data.supportAccess.requiredAttr || {};
                data.supportAccess.requiredAttrStr = data.supportAccess.requiredAttrStr || {};

                if(dir == 'load') {
                    angular.forEach(serviceForm.formData.availableAttributes, function(item) {
                        data.supportAccess.requiredAttrStr[item] = textareaArrParse(dir, data.supportAccess.requiredAttr[item]);
                    });

                    data.reqHandlersStr = textareaArrParse(dir, data.requiredHandlers);
                    data.userAttrProvider.valueAnon = (data.userAttrProvider.type == 'anon') ? data.userAttrProvider.value : '';
                    data.userAttrProvider.valueAttr = (data.userAttrProvider.type == 'attr') ? data.userAttrProvider.value : '';
                } else {
                    angular.forEach(serviceForm.formData.availableAttributes, function(item) {
                        data.supportAccess.requiredAttr[item] = textareaArrParse(dir, data.supportAccess.requiredAttrStr[item]);
                    });

                    data.requiredHandlers = textareaArrParse(dir, data.reqHandlersStr);
                    if(data.userAttrProvider.type == 'anon')
                        data.userAttrProvider.value = data.userAttrProvider.valueAnon;
                    else if(data.userAttrProvider.type == 'attr')
                        data.userAttrProvider.value = data.userAttrProvider.valueAttr;
                }

                switch(data.attrRelease.attrPolicy.type) {
                    case 'mapped':
                        if(dir == 'load')
                            data.attrRelease.attrPolicy.mapped = data.attrRelease.attrPolicy.attributes;
                        else
                            data.attrRelease.attrPolicy.attributes = data.attrRelease.attrPolicy.mapped || {};
                        break;
                    case 'allowed':
                        if(dir == 'load')
                            data.attrRelease.attrPolicy.allowed = data.attrRelease.attrPolicy.attributes;
                        else
                            data.attrRelease.attrPolicy.attributes = data.attrRelease.attrPolicy.allowed || [];
                        break;
                    default:
                        data.attrRelease.attrPolicy.value = null;
                        break;
                }

                serviceForm.serviceData = data;
            };

            $scope.$watch(
                function() { return sharedFactory.assignedId; },
                function (assignedId) {
                    if(serviceForm.alert && serviceForm.alert.type != 'info')
                        serviceForm.alert = null;
                    if(!assignedId) {serviceForm.newService(); }
                    else { serviceForm.loadService(assignedId); }
                }
            );
        }
    ]);

})();
