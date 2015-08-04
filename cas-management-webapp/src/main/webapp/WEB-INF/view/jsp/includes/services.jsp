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
    <div class="row" ng-if="serviceTableCtrl.dataTable && serviceTableCtrl.dataTable.length == 0">
        <div class="col-sm-8 col-sm-offset-2">
            <div id="msg" class="alert alert-warning no-services" role="alert">
                <div class="row">
                    <div class="col-xs-2">
                        <p class="fa fa-5x fa-exclamation-circle pull-right"></p>
                    </div>
                    <div class="col-xs-9">
                        <p><spring:message code="management.services.service.warn" arguments="${defaultServiceUrl}" /></p>
                    </div>
                    <div class="col-xs-1">
                        <button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true"><i class="fa fa-times"></i></span></button>
                    </div>
                </div>
            </div>
        </div>
    </div><%-- end if(services == 0) --%>
    <div ng-if="serviceTableCtrl.dataTable.length > 0">
        <div class="row">
            <div class="col-sm-12">
                <div class="form-group has-feedback search-form">
                    <input type="text" class="form-control input-lg" ng-model="serviceTableCtrl.serviceTableQuery" placeholder="<spring:message code="management.services.table.label.search" /> " autofocus/>
                    <a href="javascript://" class="fa fa-2x fa-times-circle-o form-control-feedback search-clear" ng-click="serviceTableCtrl.clearFilter()">
<%-- TODO: Needs accessibility text for screen readers --%>
                    </a>
                </div>
            </div>
        </div>

        <div class="row">
            <div class="col-sm-12">

                <div class="alert alert-{{ serviceTableCtrl.alert.type }}" role="alert" ng-if="serviceTableCtrl.alert">
                    <p>
                        <i class="fa fa-lg fa-{{ serviceTableCtrl.alert.type === 'danger' ? 'exclamation-triangle' : 'info-circle' }}"></i>
                        <span ng-if="serviceTableCtrl.alert.name === 'deleted'">
                            <i>{{ serviceTableCtrl.alert.data.name }}</i>
                            <spring:message code="management.services.status.deleted" />
                        </span>
                        <span ng-if="serviceTableCtrl.alert.name === 'notdeleted'">
                            <spring:message code="management.services.status.notdeleted" />
                        </span>
                        <span ng-if="serviceTableCtrl.alert.name === 'listfailed'">
                            <spring:message code="management.services.status.listfail" />
                        </span>
                        <span ng-if="serviceTableCtrl.alert.name === 'notupdated'">
                            <spring:message code="management.services.status.evaluationOrder.notupdated" />
                        </span>
                        <a href="javascript://" class="fa fa-2x fa-times close-link" ng-click="serviceTableCtrl.alert = null;">
<%-- TODO: Needs accessibility text for screen readers --%>
                        </a>
                    </p>
                </div>

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
                        <tr id="assignedId_{{ item.assignedId }}"
                            ng-repeat="item in serviceTableCtrl.dataTable | serviceTableFilter:['name','serviceId','description']:serviceTableCtrl.serviceTableQuery"
                            ng-class="{ 'row-disabled': !item.sasCASEnabled }">
                            <td colspan="6">

                    <table class="table-service">
                        <tr class="main-row">
                            <td class="col-sm-3">
                                <div class="grabber-icon"><i class="fa fa-lg fa-ellipsis-v"></i></div>
<%-- TODO: How does uiSortable deal with keyboard accessibility requirements for drag-and-drop? --%>
                                <span ng-bind="item.name"></span>
                                <a href="javascript://" class="more" ng-click="serviceTableCtrl.toggleDetail(item.assignedId)">
                                    <i class="fa fa-chevron-{{ serviceTableCtrl.detailRow === item.assignedId ? 'up' : 'down' }}"></i>
<%-- TODO: Needs accessibility text for screen readers --%>
                                </a>
                            </td>
                            <td class="col-sm-3" ng-bind="item.serviceId"></td>
                            <td class="col-sm-4" ng-bind="item.description | wordCharTrunc:60"></td>
                            <td class="col-sm-1">
                                <button class="btn btn-success" ng-click="action.serviceEdit( item.assignedId )">
                                    <i class="fa fa-lg fa-pencil"></i>
                                    <spring:message code="management.services.table.button.edit" />
                                </button>
                            </td>
                            <td class="col-sm-1">
                                <button class="btn btn-danger" ng-click="serviceTableCtrl.openModalDelete(item)">
                                    <i class="fa fa-lg fa-trash"></i>
                                    <spring:message code="management.services.table.button.delete" />
                                </button>
                            </td>
                        </tr>
                        <tr class="detail-row" ng-show="serviceTableCtrl.detailRow == item.assignedId">
                            <td colspan="6">
                                <table class="table-details">
                                    <tr>
                                        <td class="col-sm-2 detail-label"><spring:message code="management.services.table.details.description" />:</td>
                                        <td class="col-sm-8" ng-bind="item.description"></td>
                                        <td class="col-sm-2 detail-logo" colspan="2" rowspan="5" ng-if="item.logoUrl"><img ng-attr-src="{{ item.logoUrl }}" alt="{{ item.name }}" /></td>
<%-- TODO: Confirm security of the item.logoUrl and item.name values so that they won't break the code/layout. --%>
                                    </tr>
                                    <tr>
                                        <td class="col-sm-2 detail-label"><spring:message code="management.services.table.details.proxyPolicy" />:</td>
                                        <td class="col-sm-8">
                                            <span ng-if="!item.proxyPolicy.value" ng-bind="item.proxyPolicy.type | uppercase"></span>
                                            <span nf-if="item.proxyPolicy.value" ng-bind="item.proxyPolicy.value"></span>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="col-sm-2 detail-label"><spring:message code="management.services.table.details.attrPolicy" />:</td>
                                        <td class="col-sm-8" ng-bind="item.attrRelease.attrPolicy | uppercase"></td>
                                    </tr>
                                    <tr>
                                        <td class="col-sm-2 detail-label"><spring:message code="management.services.table.details.releaseCred" />:</td>
                                        <td class="col-sm-8" ng-bind="item.attrRelease.releasePassword | checkmark"></td>
                                    </tr>
                                    <tr>
                                        <td class="col-sm-2 detail-label"><spring:message code="management.services.table.details.releaseProxy" />:</td>
                                        <td class="col-sm-8" ng-bind="item.attrRelease.releaseTicket | checkmark"></td>
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
    </div><%-- end if(services > 0) --%>

    <div class="modal" id="confirm-delete" tabindex="-1" role="dialog" aria-labelledby="deleteModalLabel" aria-hidden="true"
        ng-class="{ 'show': serviceTableCtrl.modalItem, 'fade': !serviceTableCtrl.modalItem }">
        <div class="modal-dialog">
            <div class="modal-content">
                <div id="deleteModalLabel" class="modal-header">
                    <h3><spring:message code="management.services.table.modal.delete.header" /></h3>
                </div>
                <div class="modal-body">
                    <spring:message code="management.services.table.modal.delete.msgPt1" /> {{ serviceTableCtrl.modalItem.name }}.<br />
                    <spring:message code="management.services.table.modal.delete.msgPt2" />
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal" ng-click="serviceTableCtrl.closeModalDelete();">Cancel</button>
                    <button type="button" class="btn btn-danger btn-ok" ng-click="serviceTableCtrl.deleteService( serviceTableCtrl.modalItem )">Delete</button>
                </div>
            </div>
        </div>
    </div>

</div><%-- end .services-table-container div --%>