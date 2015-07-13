<%--

    Licensed to Apereo under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Apereo licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License.  You may obtain a
    copy of the License at the following location:

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.

--%>

<div class="services-table-container" ng-controller="ServicesTableController as serviceTableCtrl">
    <div class="row">
        <div class="col-sm-12">
            <div class="form-group has-feedback search-form">
                <input type="text" class="form-control input-lg" ng-model="serviceTableCtrl.serviceTableQuery" placeholder="<spring:message code="management.services.table.label.search" /> " autofocus/>
                <a href="javascript://" class="fa fa-2x fa-times-circle-o form-control-feedback search-clear" ng-click="serviceTableCtrl.clearFilter()">
<%-- TODO: Needs accessibility text for screen readers --%>
                </a>
            </div>
        </div>
    </div> <!-- end .row div -->
    <div class="row">
        <div class="col-sm-12">
            <table class="table table-hover table-striped services-table" ng-model="serviceTableCtrl.dataTable">
                <thead>
                    <tr>
                        <th class="col-sm-3"><spring:message code="management.services.table.header.serviceName" /></th>
                        <th class="col-sm-3"><spring:message code="management.services.table.header.serviceId" /></th>
                        <th class="col-sm-4"><spring:message code="management.services.table.header.serviceDesc" /></th>
                        <th class="col-sm-1"></th>
                        <th class="col-sm-1"></th>
                    </tr>
                </thead>
                <tbody ui-sortable="serviceTableCtrl.sortableOptions" ng-model="serviceTableCtrl.dataTable">
                    <tr id="assignedId_{{ item.assignedId }}" ng-repeat="item in serviceTableCtrl.dataTable | filter: serviceTableCtrl.serviceTableQuery">
                        <td colspan="6">

                <table>
                    <tr class="main-row">
                        <td class="col-sm-3">
                            <div class="grabber-icon"><i class="fa fa-lg fa-ellipsis-v"></i></div>
                            {{ item.name }}
                            <a href="javascript://" class="more" ng-click="serviceTableCtrl.toggleDetail(item.assignedId)">
<%-- TODO: Needs accessibility text for screen readers --%>
                                <i class="fa fa-chevron-{{ serviceTableCtrl.detailRow === item.assignedId ? 'up' : 'down' }}"></i>
                            </a>
                        </td>
                        <td class="col-sm-3">{{ item.serviceId }}</td>
                        <td class="col-sm-4">{{ item.description | wordCharTrunc:60 }}</td>
                        <td class="col-sm-1">
                            <button class="btn btn-success" ng-click="action.selectAction('add')">
                                <i class="fa fa-lg fa-pencil"></i> <spring:message code="management.services.table.button.edit" />
                            </button>
                        </td>
                        <td class="col-sm-1">
                            <button class="btn btn-danger" onclick="javascript:;">
                                <i class="fa fa-lg fa-trash"></i> <spring:message code="management.services.table.button.delete" />
                            </button>
                        </td>
                    </tr>
                    <tr class="detail-row" ng-show="serviceTableCtrl.detailRow == item.assignedId">
                        <td colspan="6">
                            <table>
                                <tr>
                                    <td class="col-sm-2 detail-label">Full Description:</td>
                                    <td class="col-sm-7">{{ item.description }}</td>
                                    <td class="col-sm-3 detail-logo" colspan="2" rowspan="5" ng-if="item.logoUrl"><img src="{{ item.logoUrl }}" alt="{{ item.name }}" /></td>
<%-- TODO: Confirm security of the item.logoUrl and item.name values so that they won't break the code/layout. --%>
                                </tr>
                                <tr>
                                    <td class="col-sm-2 detail-label">Proxy Policy:</td>
                                    <td class="col-sm-7">
                                        <span ng-if="!item.proxyPolicy.value">{{ item.proxyPolicy.type | uppercase }}</span>
                                        <span nf-if="item.proxyPolicy.value">{{ item.proxyPolicy.value }}</span>
                                    </td>
                                </tr>
                                <tr>
                                    <td class="col-sm-2 detail-label">Attribute Policy Option:</td>
                                    <td class="col-sm-7">{{ item.attrRelease.attrPolicy.type | uppercase }}</td>
                                </tr>
                                <tr>
                                    <td class="col-sm-2 detail-label">Release Credential:</td>
                                    <td class="col-sm-7">{{ item.attrRelease.releasePassword | checkmark }}</td>
                                </tr>
                                <tr>
                                    <td class="col-sm-2 detail-label">Release Proxy ID:</td>
                                    <td class="col-sm-7">{{ item.attrRelease.releaseTicket | checkmark }}</td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>

                        </td>
                    </tr>
                </tbody>
            </table><%-- end .services-table table --%>
        </div><%-- end .col-sm-12 div --%>
    </div><%-- end .row div --%>
</div><%-- end .services-table-container div --%>