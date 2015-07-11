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

    app.controller('actionsController', function () {
        this.actionPanel = 'manage';

        this.selectAction = function (setAction) {
            this.actionPanel = setAction;
        };

        this.isSelected = function (checkAction) {
            return this.actionPanel === checkAction;
        };
    });

    app.controller('ServiceFormController', function () {
        this.serviceForm = {};

        this.serviceTypeList=[
            {
                name: 'CAS Client',
                value: 'cas'
            },
            {
                name: 'OAuth Client',
                value: 'oauth'
            }
        ];
        this.serviceType = this.serviceTypeList[0];

        this.logoutTypeList=[
            {
                name: '0 - None',
                value: 'none'
            },
            {
                name: '1 - BACK_CHANNEL',
                value: 'back'
            },
            {
                name: '2 = FRONT_CHANNEL',
                value: 'front'
            }
        ];
        this.logoutType = this.logoutTypeList[0];

        this.publicKeyAlgorithmList=[
            {
                name: 'RSA',
                value: 'rsa'
            }
        ];
        this.publicKeyAlgorithm = this.publicKeyAlgorithmList[0];

        this.themeList=[
            {
                name: '(No Theme)',
                value: ''
            },
            {
                name: 'Theme 01',
                value: 'theme01'
            },
            {
                name: 'Theme 02',
                value: 'theme02'
            }
        ];

        this.themeType = this.themeList[0];

        this.reqHandlerList=[
            {
                name: '(No Required Handler)',
                value: ''
            },
            {
                name: 'This Required Handler',
                value: 'reqHandler01'
            },
            {
                name: 'That Required Handler',
                value: 'reqHandler01'
            }
        ];

        this.reqHandler = this.reqHandlerList[0];

        this.addService = function (service) {
            // service.services.push(this.service);

            this.serviceForm = {};
        };
    });

    app.controller('ServicesTableController', ['$http', function ($http) {
        var servicesData = this;

        this.dataTable = [];
        this.sortableOptions = {
            axis: 'y',
            items: '> tr',
            start: function() {
                // Close any open details row before the placeholder shows
                servicesData.toggleDetail(servicesData.activePosition);
            },
            stop: function(e, ui) {
                $.each($(this).sortable('toArray'), function(i, assignedId) {
                    assignedId = assignedId.replace('assignedId_', '');
                    $.each(servicesData.dataTable, function(j, tableItem) {
                        if(tableItem.assignedId == assignedId) {
                            tableItem.evalOrder = i;
                        }
                    });
                });
                // The code above is for example purposes of how it should work only and should be removed later! -ALH

                $http.post('/services/order', $(this).sortable('serialize')) // TODO: fix URL
                    .success(function() {
                        // TODO: Once the POST URL is in place, kill off the n² loop and uncomment the line below.
                        //servicesData.getServices();
                        console.log('sorting post successful');
                    })
                    .error(function(data, status) {
                        // TODO: how do we want to show an error, like server down or etc?
                        console.error(data, status);
                    });
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

        this.toggleDetail = function($index) {
            this.activePosition = this.activePosition == $index ? -1 : $index;
        };

        this.getServices();
    }]);
})();
