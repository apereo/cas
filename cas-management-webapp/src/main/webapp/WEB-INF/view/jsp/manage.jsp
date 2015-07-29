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
