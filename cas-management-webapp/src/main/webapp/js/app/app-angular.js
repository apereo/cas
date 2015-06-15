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
    var app = angular.module('casmgmt', []);

    app.controller('actionsController', function () {
        this.actionPanel = 'manage';

        this.selectAction = function (setAction) {
            this.actionPanel = setAction;
        };

        this.isSelected = function (checkAction) {
            return this.actionPanel === checkAction;
        }
    });

    app.controller('ServiceFormController', function () {
        this.serviceForm = {};

        this.serviceTypeList=[
            {
                name: 'CAS client',
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
                name: 'Theme 01',
                value: 'theme01'
            },
            {
                name: 'Theme 02',
                value: 'theme02'
            }
        ];

        this.themeType = this.themeList[0];

            this.addService = function (service) {
                // service.services.push(this.service);

                this.serviceForm = {};
            };
        });
    })();
