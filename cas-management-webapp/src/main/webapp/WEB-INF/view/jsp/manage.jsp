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
<!doctype html>
<%@ page language="java" session="false" %>
<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<html xml:lang="en" lang="en" ng-app="casmgmt">

    <head>
        <title><spring:message code="management.services.title" /></title>
        <meta name="version" content="<%=org.jasig.cas.CasVersion.getVersion()%>" />
        <link rel="icon" href="<c:url value=" ../favicon.ico" />" type="image/x-icon" />
        <link rel="stylesheet" href="//maxcdn.bootstrapcdn.com/font-awesome/4.3.0/css/font-awesome.min.css">
        <link rel="stylesheet" href="<c:url value="/css/cas-management.css" />" type="text/css" />

        <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js"></script>
        <script type="text/javascript"  
        src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/js/bootstrap.min.js"></script>
        <script type="text/javascript"
        src="https://ajax.googleapis.com/ajax/libs/angularjs/1.3.15/angular.min.js"></script>
        <script type="text/javascript" src="<c:url value="/js/app/app.js"/>"></script>
    </head>

    <body class="casmgmt-app" ng-controller="actionsController as action">

        <!-- Header -->
        <header id="casmgmt-header">
            <nav class="navbar navbar-default">
                <div class="container-fluid">
                    <!-- Brand and toggle get grouped for better mobile display -->
                    <div class="navbar-header">
                        <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#casmgt-navbar-collapse">
                            <span class="sr-only">Toggle navigation</span>
                            <span class="icon-bar"></span>
                            <span class="icon-bar"></span>
                            <span class="icon-bar"></span>
                        </button>
                        <div class="navbar-brand">
                            <img src="<c:url value="/images/logo_cas.png"/>" alt="Jasig CAS logo" />
                        </div>
                        <h4>Services Management</h4>
                    </div>

                    <!-- Collect the nav links, forms, and other content for toggling -->
                    <div class="collapse navbar-collapse" id="casmgt-navbar-collapse">
                        <ul class="nav navbar-nav navbar-right">
                            <li><a href ng-click="action.selectAction('manage')"><i class="fa fa-gears"></i> <spring:message code="manageServiceView" /></a></li>
                            <li><a href ng-click="action.selectAction('add')"><i class="fa fa-plus-circle"></i> <spring:message code="addServiceView" /></a></li>
                            <li><a href="logout.html"><i class="fa fa-sign-out"></i> <spring:message code="management.services.link.logout" /></a></li>
                        </ul>
                    </div><!-- end .navbar-collapse div -->
                </div> <!-- end .container-fluid div -->
            </nav> <!-- end .navbar div -->
        </header> <!-- end .casmgmt-header header -->

        <!-- Content -->
        <div class="container-fluid casmgmt-content">

            <!-- Manage services content -->
            <div class="casmgmt-manage" ng-show="action.isSelected('manage')">
                <div class="row">
                    <div class="col-sm-12">
                        <h1><i class="fa fa-gears"></i> <spring:message code="manageServiceView" /></h1>

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
                <div class="row">
                    <div class="col-sm-12">
                        <table class="table table-striped table-hover services-table">
                            <thead>
                                <tr>
                                    <th class="col-sm-4"><spring:message code="management.services.manage.label.name" /></th>
                                    <th class="col-sm-4"><spring:message code="management.services.manage.label.serviceUrl" /></th>
                                    <th class="col-sm-2"><spring:message code="management.services.manage.label.evaluationOrder" /></th>
                                    <th class="col-sm-1"></th>
                                    <th class="col-sm-1"></th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach items="${services}" var="service" varStatus="status">
                                    <tr id="row${status.index}"${param.id eq service.id ? ' class="added"' : ''}>
                                        <td id="${service.id}">${service.name}</td>
                                        <td>${fn:length(service.serviceId) < 100 ? service.serviceId : fn:substring(service.serviceId, 0, 100)}</td>
                                        <td>${service.evaluationOrder}</td>
                                        <td id="edit${status.index}">
                                            <button class="btn btn-success" ng-click="action.selectAction('add')">
                                                <i class="fa fa-lg fa-pencil"></i> <spring:message code="management.services.manage.action.edit" />
                                            </button>
                                        </td>
                                        <td id="delete${status.index}">
                                            <button class="btn btn-danger" onclick="javascript:;">
                                                <i class="fa fa-lg fa-trash"></i> <spring:message code="management.services.manage.action.delete" />
                                            </button>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>  <!-- end .services-table table -->
                    </div> <!-- end .col-sm-12 div -->
                </div> <!-- end .row div -->

                <div class="row">
                    <div class="col-sm-12">
                        <button class="btn btn-info" ng-click="action.selectAction('add')"><i class="fa fa-plus-circle"></i> <spring:message code="addServiceView" /></button>
                    </div>
                </div>
            </div> <!-- end .casmgmt-manage div -->

            <!-- Add/edit services content -->
            <div class="casmgmt-form" ng-show="action.isSelected('add')">
                <h1><i class="fa fa-plus-circle"></i> <spring:message code="addServiceView" /></h1>

                <div class="row">
                    <div class="col-sm-6 col-sm-offset-1">
                        <div class="alert alert-info" role="alert">
                            <i class="fa fa-lg fa-exclamation-circle"></i> <spring:message code="management.services.manage.instructions" />
                        </div>
                        <form class="form-horizontal" ng-controller="ServiceFormController as serviceFormCtrl" ng-submit="serviceFormCtrl.addService(service)">
                            <div class="form-group">
                                <!-- Service Name -->
                                <label class="col-sm-2" for="serviceName"><spring:message code="management.services.manage.label.name" /></label>
                                <div class="col-sm-10">
                                    <input type="text" class="form-control" id="serviceName" ng-model="serviceFormCtrl.service.name">
                                </div>
                            </div>
                            <div class="form-group">
                                <!-- Service Description -->
                                <label class="col-sm-2" for="serviceDesc"><spring:message code="management.services.manage.label.description" /></label>
                                <div class="col-sm-10">
                                    <textarea class="form-control" rows="5" id="serviceDesc" ng-model="serviceFormCtrl.service.description"></textarea>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-sm-6">
                                    <div class="form-group">
                                        <!-- Service Type -->
                                        <label class="col-sm-4" for="serviceType"><spring:message code="management.services.manage.label.type" /></label>
                                        <div class="col-sm-8">
                                            <select class="form-control" id="serviceType" ng-model="serviceFormCtrl.service.type">
                                                <option value="cas" selected>CAS Client</option>
                                                <option value="oauth">OAuth Client</option>
                                            </select>
                                        </div>
                                    </div>
                                </div>
                                <div class="col-sm-6">
                                    <div class="form-group">
                                        <!-- Service Theme -->
                                        <label class="col-sm-3" for="serviceTheme"><spring:message code="management.services.manage.label.themeName" /></label>
                                        <div class="col-sm-9">
                                            <input type="text" class="form-control" id="serviceTheme" ng-model="serviceFormCtrl.service.theme">
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="form-group">
                                <!-- Service ID -->
                                <label class="col-sm-2" for="serviceId"><spring:message code="management.services.manage.label.serviceId" /></label>
                                <div class="col-sm-10">
                                    <input type="text" class="form-control" id="serviceId" ng-model="serviceFormCtrl.service.serviceId">
                                </div>
                            </div>
                            <!-- OAuth Options Only -->
                            <div class="oauth-only-options-container">
                                <div class="panel panel-warning">
                                    <div class="panel-heading">
                                        <h3 class="panel-title"><spring:message code="management.services.manage.header.oauthOptions" /></h3>
                                    </div>
                                    <div class="panel-body">
                                        <div class="form-group">
                                            <!-- OAuth Client Secret -->
                                            <label class="col-sm-4" for="oauthClientSecret"><spring:message code="management.services.manage.label.oauthClientSecret" /></label>
                                            <div class="col-sm-8">
                                                <input type="text" class="form-control" id="oauthClientSecret" ng-model="serviceFormCtrl.service.oauthClientSecret">
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <!-- OAuth Client ID -->
                                            <label class="col-sm-4" for="oauthClientId"><spring:message code="management.services.manage.label.oauthClientId" /></label>
                                            <div class="col-sm-8">
                                                <input type="text" class="form-control" id="oauthClientId" ng-model="serviceFormCtrl.service.oauthClientId">
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <!-- OAuth Bypass Approval Prompt -->
                                            <label class="col-sm-4" for="oauthBypass"><spring:message code="management.services.manage.label.oauthBypass" /></label>
                                            <div class="col-sm-8">
                                                <input type="text" class="form-control" id="oauthBypass" ng-model="serviceFormCtrl.service.oauthBypass">
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-sm-6">
                                    <div class="form-group">
                                        <!-- Service Required Handlers -->
                                        <label class="col-sm-4" for="serviceReqHandlers"><spring:message code="management.services.manage.label.requiredHandlers" /></label>
                                        <div class="col-sm-8">
                                            <div class="input-group">
                                                <input type="text" class="form-control">
                                                <span class="input-group-btn">
                                                    <button class="btn btn-default" type="button"><i class="fa fa-plus-circle"></i></button>
                                                </span>
                                            </div>
                                            <select multiple class="form-control form-padding" id="serviceReqHandlers" ng-model="serviceFormCtrl.service.handlers">
                                                <option>1</option>
                                                <option>2</option>
                                                <option>3</option>
                                                <option>4</option>
                                                <option>5</option>
                                            </select>
                                        </div>
                                    </div>
                                </div>
                                <div class="col-sm-6">
                                    <div class="form-group">
                                        <!-- Evaulation Order -->
                                        <label class="col-sm-3" for="evalOrder"><spring:message code="management.services.manage.label.evalOrder" /></label>
                                        <div class="col-sm-9">
                                            <input type="text" class="form-control" id="evalOrder" ng-model="serviceFormCtrl.service.order">
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="form-group">
                                <!-- Logout URL -->
                                <label class="col-sm-2" for="logoutUrl"><spring:message code="management.services.manage.label.logoutUrl" /></label>
                                <div class="col-sm-10">
                                    <input type="text" class="form-control" id="logoutUrl" ng-model="serviceFormCtrl.service.logoutUrl">
                                </div>
                            </div>
                            <div class="form-group">
                                <!-- Logo URL -->
                                <label class="col-sm-2" for="logoUrl"><spring:message code="management.services.manage.label.logoUrl" /></label>
                                <div class="col-sm-10">
                                    <input type="text" class="form-control" id="logoUrl" ng-model="serviceFormCtrl.service.logoUrl">
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-sm-6">
                                    <div class="form-group">
                                        <!-- Logout Type -->
                                        <label class="col-sm-4" for="logoutType"><spring:message code="management.services.manage.label.logoutType" /></label>
                                        <div class="col-sm-8">
                                            <select class="form-control" id="logoutType" ng-model="service.logoutType" ng-options="type.value as type.name for type in logoutTypes"></select>
                                        </div>
                                    </div>
                                </div>
                                <div class="col-sm-6">
                                    <div class="form-group">
                                        <!-- Assigned ID -->
                                        <label class="col-sm-3" for="assignedId"><spring:message code="management.services.manage.label.assignedId" /></label>
                                        <div class="col-sm-9">
                                            <input type="text" class="form-control" id="assignedId" ng-model="serviceFormCtrl.serviceForm.assignedId">
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="form-group services-button-group">
                                <div class="col-sm-10 col-sm-offset-2">
                                    <button type="submit" class="btn btn-primary"><i class="fa fa-floppy-o"></i> <spring:message code="management.services.manage.button.save" /></button>&nbsp;&nbsp;&nbsp;

                                    <a href ng-click="action.isSelected('manage')" class="btn btn-default"><i class="fa fa-times"></i> <spring:message code="management.services.manage.button.cancel" /></a>
                                </div>
                            </div>
                        </form>
                    </div>
                    <div class="col-sm-4">
                        <div class="panel panel-success">
                            <div class="panel-heading">
                                <h3 class="panel-title"><spring:message code="management.services.manage.header.summary" /></h3>
                            </div>
                            <div class="panel-body">
                                <div class="row">
                                    <table class="table table-condensed services-summary-table">
                                        <tbody>
                                            <tr>
                                                <td class="col-sm-5 text-right"><strong><spring:message code="management.services.manage.label.name" /></strong></td>
                                                <td>{{ serviceFormController.name }}</td>
                                            </tr>
                                            <tr>
                                                <td class="col-sm-5 text-right"><strong><spring:message code="management.services.manage.label.description" /></strong></td>
                                                <td>{{ serviceFormController.description }}</td>
                                            </tr>
                                            <tr>
                                                <td class="col-sm-5 text-right"><strong><spring:message code="management.services.manage.label.type" /></strong></td>
                                                <td>{{ serviceFormController.type }}</td>
                                            </tr>
                                            <tr>
                                                <td class="col-sm-5 text-right"><strong><spring:message code="management.services.manage.label.themeName" /></strong></td>
                                                <td>{{ serviceFormController.theme }}</td>
                                            </tr>
                                            <tr>
                                                <td class="col-sm-5 text-right"><strong><spring:message code="management.services.manage.label.serviceId" /></strong></td>
                                                <td>{{ serviceFormController.serviceId }}</td>
                                            </tr>
                                            <tr>
                                                <td class="col-sm-5 text-right"><strong><spring:message code="management.services.manage.label.requiredHandlers" /></strong></td>
                                                <td>{{ serviceFormController.handlers }}</td>
                                            </tr>
                                            <tr>
                                                <td class="col-sm-5 text-right"><strong><spring:message code="management.services.manage.label.evalOrder" /></strong></td>
                                                <td>{{ serviceFormController.order }}</td>
                                            </tr>
                                            <tr>
                                                <td class="col-sm-5 text-right"><strong><spring:message code="management.services.manage.label.logoutUrl" /></strong></td>
                                                <td>{{ serviceFormController.logoutUrl }}</td>
                                            </tr>
                                            <tr>
                                                <td class="col-sm-5 text-right"><strong><spring:message code="management.services.manage.label.logoUrl" /></strong></td>
                                                <td>{{ serviceFormController.logoUrl }}</td>
                                            </tr>
                                            <tr>
                                                <td class="col-sm-5 text-right"><strong><spring:message code="management.services.manage.label.logoutType" /></strong></td>
                                                <td>{{ serviceFormController.logoutType }}</td>
                                            </tr>
                                            <tr>
                                                <td class="col-sm-5 text-right"><strong><spring:message code="management.services.manage.label.assignedId" /></strong></td>
                                                <td>{{ serviceFormCtrl.assignedId }}</td>
                                            </tr>
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                        </div>
                    </div>
                </div> <!-- end .row div -->
            </div> <!-- end .casmgmt-form div -->
        </div> <!-- end .casmgmt-content div -->

        <!-- Footer -->
        <footer id="casmgmt-footer">
            <div class="row">
                <div class="col-sm-10">
                    <span><spring:message code="footer.links" /></span>
                    <ul class="nav-campus-sites list-inline">
                        <li><a href="http://www.apereo.org/cas" rel="_blank"><spring:message code="footer.homePage" /></a>,</li>
                        <li><a href="http://wiki.jasig.org" rel="_blank"><spring:message code="footer.wiki" /></a>,</li>
                        <li><a href="http://issues.jasig.org" rel="_blank"><spring:message code="footer.issueTracker" /></a>,</li>
                        <li><a href="http://www.apereo.org/cas/mailing-lists" rel="_blank"><spring:message code="footer.mailingLists"
                        /></a>.</li>
                    </ul>
                    <div class="copyright">
                        <p>
                            <spring:message code="footer.copyright" /><br/>
                            <spring:message code="footer.poweredBy" arguments="<%=org.jasig.cas.CasVersion.getVersion()%>" />
                        </p>
                    </div>
                </div>
                <div class="col-sm-2">
                    <div class="jasig-link">
                        <a href="http://www.apereo.org" _target="blank" title="Apereo CAS Home Page"><img src="<c:url value="/images/logo_apereo.png"/>" /></a>
                    </div>
                </div>
            </div>
        </footer>  <!-- end .casmgmt-footer footer -->


    </body>
</html>