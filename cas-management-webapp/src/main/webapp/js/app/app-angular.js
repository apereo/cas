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
    var app = angular.module('casmgmt', ['ui.sortable']);

    app.filter('checkmark', function() {
            return function(input) {
                return input ? '\u2713' : '\u2718';
            };
        })
        .filter('wordCharTrunc', function() {
            return function(str, limit) {
                if(typeof str != 'string') { return ''; }
                if(!limit || str.length <= limit) { return str; }

                var newStr = str.substring(0, limit).replace(/\w+$/, '');
                return (newStr ? newStr : str.substring(0, limit)) + '...';
            };
        })
        .filter('serviceTableFilter', function() {
            return function(services, fields, regex) {
                if(typeof fields == 'string') { fields = [fields]; }
                try {
                    regex = regex ? new RegExp(regex, 'i') : false;
                } catch(e) {
                    // TODO: How do we want to tell the user their regex is bad? On error, return list or null?
                    regex = false;
                }
                if(!services || !fields || !regex) { return services; }

                var matches = [];
                angular.forEach(services, function(service, i) {
                    angular.forEach(fields, function(field, j) {
                        if(regex.test(service[field]) && matches.indexOf(service) == -1) {
                            matches.push(service);
                        }
                    });
                });
                return matches;
            };
        });

/** // Routes not working yet, so commented out
    app.config([
        '$routeProvider',
        function($routeProvider) {
            $routeProvider.
                when('/manage', {
                    templateUrl: '',
                    controller: 'ServicesTableController'
                }).
                when('/service/', {
                    templateUrl: '',
                    controller: 'ServiceFormController'
                }).
                when('/service/:assignedId', {
                    templateUrl: '',
                    controller: 'ServiceFormController'
                }).
                when('/logout', {
                    templateUrl: '/jsp/includes',
                    controller: ''
                }).
                otherwise({
                    redirectTo: '/manage'
                });
        }
    ]);
**/

// View Swapper
    app.controller('actionsController', [
        function () {
            this.actionPanel = 'manage';

            this.selectAction = function (setAction) {
                this.actionPanel = setAction;
            };

            this.isSelected = function (checkAction) {
                return this.actionPanel === checkAction;
            };
        }
    ]);

// Services Table: Manage View
    app.controller('ServicesTableController', [
        '$http',
        '$log',
        function ($http, $log) {
            var servicesData = this;

            this.dataTable = [];
            this.sortableOptions = {
                axis: 'y',
                items: '> tr',
                handle: '.grabber-icon',
                placeholder: 'tr-placeholder',
                start: function(e, ui) {
                    servicesData.detailRow = 0;
                },
                update: function(e, ui) {
/**
                    $http.post('/rest/services/reorder', $(this).sortable('serialize')) // TODO: fix URL
                        .success(function() {
                            //servicesData.getServices(); // TODO: Need/want? Helpful if multiple instances/users.
                        })
                        .error(function(data, status) {
                            // TODO: If an error does occur, what happens?
                        });
**/
                }
            };

            this.getServices = function() {
                $http.get('js/app/data/services.json').success(function (data) { // TODO: fix URL
                    servicesData.dataTable = data;
                });
            };

            this.clearFilter = function() {
                this.serviceTableQuery = "";
            };

            this.toggleDetail = function(rowId) {
                this.detailRow = this.detailRow == rowId ? 0 : rowId;
            };

            this.getServices();
        }
    ]);

// Service Form: Add/Edit Service View
    app.controller('ServiceFormController', [
        //'$routeParams',
        '$log',
        function ($log) { //$routeParams, 
            var serviceForm = this;

            this.formData = {};
            this.formErrors = null;

            this.serviceTypeList = [
                {name: 'CAS Client',    value: 'cas'},
                {name: 'OAuth Client',  value: 'oauth'}
            ];
            this.serviceType = this.serviceTypeList[0];

            this.logoutTypeList = [
                {name: '0 - None',          value: 'none'},
                {name: '1 - BACK_CHANNEL',  value: 'back'},
                {name: '2 - FRONT_CHANNEL', value: 'front'}
            ];
            this.logoutType = this.logoutTypeList[0];

            this.publicKeyAlgorithmList = [
                {name: 'RSA', value: 'rsa'}
            ];
            this.publicKeyAlgorithm = this.publicKeyAlgorithmList[0];

            this.themeList=[
                {name: '(No Theme)',    value: ''},
                {name: 'Theme 01',      value: 'theme01'},
                {name: 'Theme 02',      value: 'theme02'}
            ];
            this.themeType = this.themeList[0];

            this.reqHandlerList=[
                {name: 'Required Handler 1', value: 'reqHandler01'},
                {name: 'Required Handler 2', value: 'reqHandler02'},
                {name: 'Required Handler 3', value: 'reqHandler03'},
                {name: 'Required Handler 4', value: 'reqHandler04'},
                {name: 'Required Handler 5', value: 'reqHandler05'}
            ];
            this.reqHandler = this.reqHandlerList[0];

            this.loadForm = function() {
                // TODO: Needed?
            };

            this.saveForm = function() {
                $log.log('saveForm()');
                serviceForm.validateForm();

                //if(this.formErrors) {}
            };

            this.validateForm = function() {
                $log.log('validateForm()');
                serviceForm.formErrors = null;
            };

            this.addService = function (service) {
                // service.services.push(this.service);
                serviceForm.formData = {};
            };

/**
            if($routeParams.assignedId) {
                $http.get('js/app/data/service-' + $routeParams.assignedId + '.json').success(function (data) { // TODO: fix URL
                    serviceForm.formData = data[0];
                    serviceForm.loadForm();
                });
            }
**/
        }
    ]);

})();
