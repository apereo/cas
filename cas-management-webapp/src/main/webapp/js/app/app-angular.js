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
        '$log',
        '$location',
        function ($log, $location) {
            var factory = {assignedId: null};

            factory.httpHeaders = {};
            factory.httpHeaders[ $("meta[name='_csrf_header']").attr("content") ] = $("meta[name='_csrf']").attr("content");

            factory.httpConfig = { // In case we can get $http.post to work
                headers: factory.httpHeaders,
                responseType: 'json'
            };

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
                $location.url('/cas-management/logout/html');
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
        '$http',
        '$log',
        '$timeout',
        'sharedFactoryCtrl',
        function ($http, $log, $timeout, sharedFactory) {
            var servicesData = this,
                httpHeaders = sharedFactory.httpHeaders,
                delayedAlert = function(n, t, d, skipScrollTop) {
                    skipScrollTop = skipScrollTop || false;
                    $timeout(function () {
                        servicesData.alert = {
                            name:   n,
                            type:   t,
                            data:   d
                        };
                    }, 10);
                    if(!skipScrollTop) {
                        $timeout(function () {
                            $('html, body').animate({
                                scrollTop: $('.alert[role=alert]').offset().top
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
                    servicesData.detailRow = -1;
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
                            url: '/cas-management/updateRegisteredServiceEvaluationOrder.html',
                            data: myData,
                            headers: httpHeaders,
                            dataType: 'json',
                            success: function (data, status) {
                                if(data.status != 200)
                                    delayedAlert('notupdated', 'danger', data);
                                else if(angular.isString(data))
                                    sharedFactory.forceReload();
                                else
                                    servicesData.getServices();
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
                $http.get('/cas-management/getServices.html')
                    .then(function (response) {
                        if(response.status != 200) {
                            delayedAlert('listfail', 'danger', response.data);
                        }
                        else {
                            if(servicesData.alert && servicesData.alert.type != 'info') 
                                servicesData.alert = null;
                            servicesData.dataTable = response.data.services || [];
                            angular.forEach(servicesData.dataTable, function(service) {
                                if(service.evalOrder > sharedFactory.maxEvalOrder) {
                                    sharedFactory.maxEvalOrder = service.evalOrder;
                                }
                            });
                        }
                    });
            };

            this.openModalDelete = function (item) {
                servicesData.modalItem = item;
                $timeout(function () {
                    $('#confirm-delete .btn-default').focus();
                }, 100);
            };
            this.closeModalDelete = function () {
                servicesData.modalItem = null;
            };
            this.deleteService = function (item) {
                var myData = {id: item.assignedId};

                servicesData.closeModalDelete();
                $.ajax({
                    type: 'post',
                    url: '/cas-management/deleteRegisteredService.html',
                    data: myData,
                    headers: httpHeaders,
                    success: function (data, status) {
                        if(data.status != 200)
                            delayedAlert('notdeleted', 'danger', data);
                        else if(angular.isString(data))
                            sharedFactory.forceReload();
                        else {
                            servicesData.getServices();
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
                servicesData.serviceTableQuery = "";
            };

            this.toggleDetail = function (rowId) {
                servicesData.detailRow = servicesData.detailRow == rowId ? -1 : rowId;
            };

            this.getServices();
        }
    ]);

// Service Form: Add/Edit Service View
    app.controller('ServiceFormController', [
        '$scope',
        '$http',
        '$log',
        '$timeout',
        'sharedFactoryCtrl',
        function ($scope, $http, $log, $timeout, sharedFactory) {
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
                                scrollTop: $('.alert[role=alert]').offset().top
                            }, 750);
                        }, 100);
                    }
                },
                showInstructions = function () { // Just an alias.
                    delayedAlert('instructions', 'info', null, true);
                };

            this.serviceData = {};
            this.formData = {};
            this.formErrors = null;
            this.radioWatchBypass = true;
            this.showOAuthSecret = false;

            this.selectOptions = {
                serviceTypeList: [
                    {name: 'CAS Client',                value: 'cas'},
                    {name: 'OAuth Client',              value: 'oauth'},
                    {name: 'OAuth Callback Authorize',  value: 'oauth_callback_authz'}
                ],
                logoutTypeList: [
                    {name: 'None',              value: ''},
                    {name: '1 - BACK_CHANNEL',  value: 'back'},
                    {name: '2 - FRONT_CHANNEL', value: 'front'}
                ],
                timeUnitsList: [
                    {name: 'MILLISECONDS',  value: 'milliseconds'},
                    {name: 'SECONDS',       value: 'seconds'},
                    {name: 'MINUTES',       value: 'minutes'},
                    {name: 'HOURS',         value: 'hours'},
                    {name: 'DAYS',          value: 'days'}
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
                serviceDataTransformation('save');                
                serviceForm.validateForm();

                if(serviceForm.formErrors.length !== 0) {
                    delayedAlert('notvalid', 'danger', serviceForm.formErrors);
                    angular.forEach(serviceForm.formErrors, function(fieldId) {
                        $('#'+fieldId).addClass('required-missing');
                    });
                    return;
                }

                $.ajax({
                    type: 'post',
                    url: '/cas-management/saveService.html',
                    data: serviceForm.serviceData,
                    headers: httpHeaders,
                    success: function (data, status) {
                        if(data.status != 200) {
                            delayedAlert('notsaved', 'danger', data);
                            serviceForm.newService();
                        }
                        else if(angular.isString(data))
                            sharedFactory.forceReload();
                        else
                            delayedAlert('saved', 'info', null, true);
                    },
                    error: function(xhr, status) {
                        if(xhr.status == 403)
                            sharedFactory.forceReload();
                        else
                            delayedAlert('notsaved','danger', xhr.responseJSON);
                    }
                });
            };

            this.validateForm = function () {
                var data = serviceForm.serviceData,
                    opts = serviceForm.selectOptions;

                serviceForm.formErrors = [];
                $('.required-missing').removeClass('required-missing');

                // Service Basics
                if(!data.serviceId)
                    serviceForm.formErrors.push('serviceId');
                if(!data.name)
                    serviceForm.formErrors.push('serviceName');
                if(!data.description)
                    serviceForm.formErrors.push('serviceDesc');
                if(!data.type)
                    serviceForm.formErrors.push('serviceType');
                // Username Attribute Provider Options
                if(data.userAttrProvider.type == 'attr' && !data.userAttrProvider.value)
                    serviceForm.formErrors.push('uapUsernameAttribute');
                // Principle Attribute Repository Options
                if(data.attrRelease.attrOption == 'cached') {
                    if(!data.attrRelease.cachedTimeUnit)
                        serviceForm.formErrors.push('cachedTime');
                    if(!data.attrRelease.mergingStrategy)
                        serviceForm.formErrors.push('mergingStrategy');
                    $log.log(data.attrRelease);
                }
                // Attribute Policy Options
                if(data.userAttrProvider.value == 'anon' && !data.userAttrProvider.value) { serviceForm.formErrors.push('uapSaltSetting'); }
                if(data.proxyPolicy.type == 'regex' && !data.proxyPolicy.value) { serviceForm.formErrors.push('proxyPolicyRegex'); }
            };

            this.newService = function () {
                serviceForm.radioWatchBypass = true;

                serviceForm.showOAuthSecret = false;
                serviceForm.serviceData = {
                    assignedId: null,
                    evalOrder: sharedFactory.maxEvalOrder + 1,
                    logoutType: '',
                    publicKey: {algorithm: 'RSA'},
                    supportAccess: {casEnabled: true},
                    userAttrProvider: {type: 'default'},
                    proxyPolicy: {type: 'refuse'},
                    attrRelease: {
                        attrOption: 'default',
                        attrPolicy: {type: 'all'}
                    }
                };
                serviceDataTransformation('load');
                showInstructions();

                $http.get('/cas-management/getService.html?id=-1')
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

                $http.get('/cas-management/getService.html?id=' + serviceId)
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

            // Transform the data so it is ready from/to the form to/from the server.
            var serviceDataTransformation = function(dir) {
                var data = serviceForm.serviceData;

                if(dir == 'load') {
                    data.reqHandlersStr = data.requiredHandlers ? data.requiredHandlers.join("\n") : '';
                    data.userAttrProvider.valueAnon = (data.userAttrProvider.type == 'anon') ? data.userAttrProvider.value : '';
                    data.userAttrProvider.valueAttr = (data.userAttrProvider.type == 'attr') ? data.userAttrProvider.value : '';
                } else {
                    data.requiredHandlers = data.reqHandlersStr.split("\n");
                    for (var i = data.requiredHandlers.length-1; i >= 0; i--) {
                        data.requiredHandlers[i] = data.requiredHandlers[i].trim();
                        if (!data.requiredHandlers[i])
                            data.requiredHandlers.splice(i, 1);
                    }

                    if(data.userAttrProvider.type == 'anon')
                        data.userAttrProvider.value = data.userAttrProvider.valueAnon;
                    else if(data.userAttrProvider.type == 'attr')
                        data.userAttrProvider.value = data.userAttrProvider.valueAttr;
                }

                switch(data.attrRelease.attrPolicy.type) {
                    case 'mapped':
                        if(dir == 'load')
                            data.attrRelease.attrPolicy.mapped = data.attrRelease.attrPolicy.value;
                        else
                            data.attrRelease.attrPolicy.value = data.attrRelease.attrPolicy.mapped;
                        break;
                    case 'allowed':
                        if(dir == 'load')
                            data.attrRelease.attrPolicy.allowed = data.attrRelease.attrPolicy.value;
                        else
                            data.attrRelease.attrPolicy.value = data.attrRelease.attrPolicy.allowed;
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
