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
            'ui.sortable',
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
        function ($log) {
            var factory = {assignedId: null};

            factory.httpHeaders = {};
            factory.httpHeaders[ $("meta[name='_csrf_header']").attr("content") ] = $("meta[name='_csrf']").attr("content");

            factory.httpConfig = { // In case we can get $http.post to work
                headers: factory.httpHeaders,
                responseType: 'json'
            };


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
                $('#homepageUrlAnchor').click();
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
                delayedAlert = function(n, t, d) {
                    $timeout(function () {
                        servicesData.alert = {
                            name:   n,
                            type:   t,
                            data:   d
                        };
                    }, 100);
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
                            success: function (data) {
                                servicesData.alert = null;
                                servicesData.getServices();
                            },
                            error: function(xhr, status) {
                                delayedAlert('notupdated', 'danger', xhr);
                            }
                        });
                    }
                }
            };

            this.getServices = function () {
                $http.get('/cas-management/getServices.html')
                    .success(function (data) {
                        servicesData.alert = null;
                        servicesData.dataTable = data.services || [];
                    })
                    .error(function (xhr, status) {
                        delayedAlert('listfail', 'danger', xhr);
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
                    success: function (data) {
                        servicesData.getServices();
                        delayedAlert('deleted', 'info', item);
                    },
                    error: function(xhr, status) {
                        delayedAlert('notdeleted', 'danger', xhr);
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
                delayedAlert = function(n, t, d) {
                    $timeout(function () {
                        serviceForm.alert = {
                            name:   n,
                            type:   t,
                            data:   d
                        };
                    }, 100);
                },
                showInstructions = function () { // Just an alias.
                    delayedAlert('instructions', 'info', null);
                };

            this.formData = {};
            this.formErrors = null;
            this.radioWatchBypass = false;
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
                    {name: 'Milliseconds',  value: 'MILLISECONDS'},
                    {name: 'Seconds',       value: 'SECONDS'},
                    {name: 'Minutes',       value: 'MINUTES'},
                    {name: 'Hours',         value: 'HOURS'},
                    {name: 'Days',          value: 'DAYS'}
                ],
                mergeStrategyList: [
                    {name: 'Default',       value: 'DEFAULT'},
                    {name: 'Add',           value: 'ADD'},
                    {name: 'Multi-Valued',  value: 'MULTI-VALUED'},
                    {name: 'Replace',       value: 'REPLACE'}
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
                serviceForm.validateForm();

                if(serviceForm.formErrors.length !== 0) {
                    delayedAlert('notvalid', 'danger', serviceForm.formErrors);
                    angular.forEach(serviceForm.formErrors, function(fieldId) {
                        $('#'+fieldId).addClass('required-missing');
                    });
                    return;
                } else {
                    $('.service-editor input, .service-editor select, .service-editor textarea').removeClass('required-missing');
                }

                $http.post('/cas-management/forcedError', serviceForm.formData)  // TODO: fix this call
                    .success(function (data) {
                        serviceForm.formData = data[0];
                        delayedAlert('saved', 'info', null);
                    })
                    .error(function (xhr, status) {
                        delayedAlert('notsaved','danger', xhr);
                    });
            };

            this.validateForm = function () {
                serviceForm.formErrors = [];

                // Service Basics
                if(!serviceForm.formData.serviceId)
                    serviceForm.formErrors.push('serviceId');
                if(!serviceForm.formData.name)
                    serviceForm.formErrors.push('serviceName');
                if(!serviceForm.formData.description)
                    serviceForm.formErrors.push('serviceDesc');
                if(!serviceForm.formData.type)
                    serviceForm.formErrors.push('serviceType');
                // Principle Attribute Repository Options
                if(serviceForm.formData.attrRelease.attrOption == 'cached' && !serviceForm.formData.attrRelease.cachedTimeUnit)
                    serviceForm.formErrors.push('cachedTime');
                if(serviceForm.formData.attrRelease.attrOption == 'cached' && !serviceForm.formData.attrRelease.mergingStrategy)
                    serviceForm.formErrors.push('mergeStrategy');
                // Attribute Policy Options
                if(serviceForm.formData.userAttrProvider.value == 'anon' && !serviceForm.formData.userAttrProvider.value)
                    serviceForm.formErrors.push('uapSaltSetting');
                if(serviceForm.formData.proxyPolicy.type == 'regex' && !serviceForm.formData.proxyPolicy.value)
                    serviceForm.formErrors.push('proxyPolicyRegex');
            };

            this.newService = function () {
                serviceForm.radioWatchBypass = true;

                serviceForm.showOAuthSecret = false;
                serviceForm.formData = {
                    assignedId: null,
                    evalOrder: 100,
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
                showInstructions();

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
                        else if(angular.isString(response.data)) {
                            sharedFactoryCtrl.forceReload();
                        }
                        else {
                            serviceForm.showOAuthSecret = false;
                            serviceForm.formData = response.data;
                            formDataTransformation('load');
                            showInstructions();
                        }
                    });

                serviceForm.radioWatchBypass = false;
            };

            // Transform the data so it is ready from/to the form to/from the server.
            var formDataTransformation = function(dir) {
                var data = serviceForm.formData;

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

                serviceForm.formData = data;
            };

            $scope.$watch(
                function() { return sharedFactory.assignedId; },
                function (assignedId) {
                    serviceForm.alert = null;
                    if(!assignedId) { serviceForm.newService(); }
                    else { serviceForm.loadService(assignedId); }
                }
            );

            $scope.$watch(
                function() {
                    return serviceForm.formData.userAttrProvider.type;
                },
                function () {
                    if(serviceForm.radioWatchBypass) return;
                    //$log.debug('userAttrProvider.type changed, so .value cleared');
                    serviceForm.formData.userAttrProvider.value = '';
                }
            );
        }
    ]);

})();
