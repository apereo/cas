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
                            <i class="fa fa-lg fa-exclamation-circle"></i> <spring:message code="management.services.add.instructions" />
                        </div>
                        <form class="form-horizontal">
                            <div class="form-group">
                                <label class="col-sm-2" for="serviceName"><spring:message code="management.services.add.property.name" /></label>
                                <div class="col-sm-10">
                                    <input type="text" class="form-control" id="serviceName" ng-model="service.name">
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="col-sm-2" for="serviceUrl"><spring:message code="management.services.add.property.serviceUrl" /></label>
                                <div class="col-sm-10">
                                    <input type="text" class="form-control" id="serviceUrl" ng-model="service.url">
                                </div>
                            </div>
                            <div class="form-group">
                                <div class="col-sm-10 col-sm-offset-2">
                                    <div class="alert alert-info" role="alert">
                                        <spring:message code="management.services.add.property.serviceUrl.instructions" />
                                    </div>
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="col-sm-2" for="serviceDesc"><spring:message code="management.services.add.property.description" /></label>
                                <div class="col-sm-10">
                                    <textarea class="form-control" rows="5" ng-model="service.description"></textarea>
                                </div>
                            </div>
                            <div class="form-group">
                                <div class="col-sm-10 col-sm-offset-2">
                                    <label class="checkbox-inline">
                                        <input type="checkbox" id="serviceEnabled" value="enabled" ng-model="service.enabled"> <spring:message code="management.services.add.property.status.enabled" />
                                    </label>
                                    <label class="checkbox-inline">
                                        <input type="checkbox" id="serviceProxy" value="proxy" ng-model="service.proxy"> <spring:message code="management.services.add.property.status.allowedToProxy" />
                                    </label>
                                    <label class="checkbox-inline">
                                        <input type="checkbox" id="serviceSso" value="sso" ng-model="service.sso"> <spring:message code="management.services.add.property.status.ssoParticipant" />
                                    </label>
                                    <label class="checkbox-inline">
                                        <input type="checkbox" id="serviceAnonAccess" value="anonAccess" ng-model="service.anonAccess"> <spring:message code="management.services.add.property.status.anonymousAccess" />
                                    </label>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-sm-6">
                                    <div class="form-group">
                                        <label class="col-sm-4" for="serviceAttributes"><spring:message code="management.services.add.property.attributes" /></label>
                                        <div class="col-sm-8">
                                            <select multiple class="form-control" id="serviceAttributes" ng-model="service.attributes">
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
                                        <label class="col-sm-3" for="serviceTheme"><spring:message code="management.services.add.property.themeName" /></label>
                                        <div class="col-sm-9">
                                            <input type="text" class="form-control" id="serviceTheme" ng-model="service.theme">
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label class="col-sm-3" for="serviceOrder"><spring:message code="management.services.manage.label.evaluationOrder" /></label>
                                        <div class="col-sm-9">
                                            <input type="text" class="form-control" id="serviceOrder" ng-model="service.order">
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="form-group">
                                <div class="col-sm-10 col-sm-offset-2">
                                    <label class="checkbox-inline">
                                        <input type="checkbox" id="ignoreAttribute" value="enabled" ng-model="service.ignoreAttribute"> <spring:message code="management.services.manage.label.ignoreAttribute" />
                                    </label>
                                </div>
                            </div>
                            <div class="form-group services-button-group">
                                <div class="col-sm-10 col-sm-offset-2">
                                    <button type="submit" class="btn btn-primary"><i class="fa fa-floppy-o"></i> <spring:message code="management.services.add.button.save" /></button>&nbsp;&nbsp;&nbsp;

                                    <a href ng-click="action.isSelected('manage')" class="btn btn-default"><i class="fa fa-times"></i> <spring:message code="management.services.add.button.cancel" /></a>
                                </div>
                            </div>
                        </form>
                    </div>
                    <div class="col-sm-4">
                        <div class="panel panel-success">
                            <div class="panel-heading">
                                <h3 class="panel-title"><spring:message code="management.services.manage.label.summary" /></h3>
                            </div>
                            <div class="panel-body">
                                <div class="row">
                                    <table class="table table-condensed services-summary-table">
                                        <tbody>
                                            <tr>
                                                <td class="col-sm-5 text-right"><strong><spring:message code="management.services.add.property.name" /></strong></td>
                                                <td>{{ service.name }}</td>
                                            </tr>
                                            <tr>
                                                <td class="col-sm-5 text-right"><strong><spring:message code="management.services.add.property.serviceUrl" /></strong></td>
                                                <td>{{ service.url }}</td>
                                            </tr>
                                            <tr>
                                                <td class="col-sm-5 text-right"><strong><spring:message code="management.services.add.property.description" /></strong></td>
                                                <td>{{ service.description }}</td>
                                            </tr>
                                            <tr>
                                                <td class="col-sm-5 text-right"><strong><spring:message code="management.services.add.property.status.enabled" />?</strong></td>
                                                <td>{{ service.enabled }}</td>
                                            </tr>
                                            <tr>
                                                <td class="col-sm-5 text-right"><strong><spring:message code="management.services.add.property.status.allowedToProxy" />?</strong></td>
                                                <td>{{ service.proxy }}</td>
                                            </tr>
                                            <tr>
                                                <td class="col-sm-5 text-right"><strong><spring:message code="management.services.add.property.status.ssoParticipant" />?</strong></td>
                                                <td>{{ service.sso }}</td>
                                            </tr>
                                            <tr>
                                                <td class="col-sm-5 text-right"><strong><spring:message code="management.services.add.property.status.anonymousAccess" />?</strong></td>
                                                <td>{{ service.anonAccess }}</td>
                                            </tr>
                                            <tr>
                                                <td class="col-sm-5 text-right"><strong><spring:message code="management.services.add.property.attributes" /></strong></td>
                                                <td>{{ service.attributes }}</td>
                                            </tr>
                                            <tr>
                                                <td class="col-sm-5 text-right"><strong><spring:message code="management.services.add.property.themeName" /></strong></td>
                                                <td>{{ service.theme }}</td>
                                            </tr>
                                            <tr>
                                                <td class="col-sm-5 text-right"><strong><spring:message code="management.services.manage.label.evaluationOrder" /></strong></td>
                                                <td>{{ service.order }}</td>
                                            </tr>
                                            <tr>
                                                <td class="col-sm-5 text-right"><strong><spring:message code="management.services.manage.label.ignoreAttribute" />?</strong></td>
                                                <td>{{ service.ignoreAttribute }}</td>
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