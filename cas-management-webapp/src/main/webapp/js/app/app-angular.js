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



            //logoutTypes = [
            //    { 'name': 'None', 'value': 'None' },
            //    { 'name': 'Back Channel', 'value': 'back_channel' },
            //    { 'name': 'Front Channel', 'value': 'front_channel' }
            //];

            //service = { type : logoutTypes[0].value };

            // serviceForm.service.logoutType = [
            //     { 'name': 'None', 'value': 'None' },
            //     { 'name': 'Back Channel', 'value': 'back_channel' },
            //     { 'name': 'Front Channel', 'value': 'front_channel' }
            // ];

            // serviceForm.service.logoutType = serviceForm.service.logoutType[0].value;

            this.addService = function (service) {
                // service.services.push(this.service);

                this.serviceForm = {};
            };
        });
    })();
