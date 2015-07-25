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

<%@include file="includes/header.jsp" %>

        <!-- Content -->
        <div class="container-fluid casmgmt-content">

            <!-- Manage services content -->
            <div class="casmgmt-manage" ng-show="action.isSelected('manage')">
                <div class="row">
                    <div class="col-sm-12">
                        <h1><i class="fa fa-gears"></i> <spring:message code="management.services.header.navbar.navitem.manageService" /></h1>

                        <!-- Warning alert -->
                        <c:if test="${fn:length(services) eq 0}">
                            <div class="row">
                                <div class="col-sm-8 col-sm-offset-2">
                                    <div id="msg" class="alert alert-warning" role="alert">
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
                            </div>
                        </c:if>

                        <!-- Success alert -->
                        <c:if test="${not empty param.status}">
                            <div class="row">
                                <div class="col-sm-8 col-sm-offset-2">
                                    <div id="msg" class="alert alert-success" role="alert">
                                        <div class="row">
                                            <div class="col-xs-2">
                                                <p class="fa fa-5x fa-check-circle pull-right"></p>
                                            </div>
                                            <div class="col-xs-9">
                                                <p><spring:message code="management.services.status.${param.status}" arguments="${param.serviceName}" /></p>
                                            </div>
                                            <div class="col-xs-1">
                                                <button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true"><i class="fa fa-times"></i></span></button>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </c:if>

                        <!-- Warning alert -->
                        <div id="errorsDiv" class="row">
                            <div class="col-sm-8 col-sm-offset-2">
                                <div class="alert alert-danger" role="alert">
                                    <div class="row">
                                        <div class="col-xs-2">
                                            <p class="fa fa-5x fa-exclamation-triangle pull-right"></p>
                                        </div>
                                        <div class="col-xs-9">
                                            <p><spring:message code="management.services.status.evaluationOrder.notupdated" /></p>
                                        </div>
                                        <div class="col-xs-1">
                                            <button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true"><i class="fa fa-times"></i></span></button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div> <!-- end .col-sm-12 div -->
                </div> <!-- end .row div -->

                <!-- Services table -->
                <%@include file="includes/services.jsp" %>

                <div class="row">
                    <div class="col-sm-12">
                        <button class="btn btn-info" ng-click="action.serviceAdd()">
                            <i class="fa fa-plus-circle"></i>
                            <spring:message code="management.services.header.navbar.navitem.addNewService" />
                        </button>
                    </div>
                </div>
            </div> <!-- end .casmgmt-manage div -->

            <!-- Add/edit services form -->
            <div class="casmgmt-form" ng-show="action.isSelected('add') || action.isSelected('edit')">
                <%@include file="includes/service-form.jsp" %>
            </div> <!-- end .casmgmt-form div -->

        </div> <!-- end .casmgmt-content div -->

        <!-- Footer -->
<%@include file="includes/footer.jsp" %>
