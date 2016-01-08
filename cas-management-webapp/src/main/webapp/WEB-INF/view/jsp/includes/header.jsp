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
        <title><spring:message code="management.services.header.apptitle" /></title>
        <meta name="version" content="<%=org.jasig.cas.CasVersion.getVersion()%>" />
        <meta name="_csrf" content="${_csrf.token}"/>
        <meta name="_csrf_header" content="${_csrf.headerName}"/>
        <link rel="icon" href="<c:url value="/images/favicon.ico" />" type="image/x-icon" />
        <link rel="stylesheet" href="//maxcdn.bootstrapcdn.com/font-awesome/4.3.0/css/font-awesome.min.css">
        <link rel="stylesheet" href="<c:url value="/css/cas-management.css" />" type="text/css" />

        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js"></script>
        <script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/jqueryui/1.11.4/jquery-ui.min.js"></script>
        <script type="text/javascript" src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/js/bootstrap.min.js"></script>
        <script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/angular.js/1.3.16/angular.min.js"></script>
        <script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/angular.js/1.3.16/angular-route.min.js"></script>
        <script type="text/javascript" src="//cdnjs.cloudflare.com/ajax/libs/angular-ui/0.4.0/angular-ui.min.js"></script>
        <script type="text/javascript" src="//cdnjs.cloudflare.com/ajax/libs/angular-ui-sortable/0.13.4/sortable.min.js"></script>
        <script type="text/javascript" src="//cdnjs.cloudflare.com/ajax/libs/moment.js/2.10.6/moment.js"></script>
        <script type="text/javascript" src="//cdnjs.cloudflare.com/ajax/libs/knockout/3.3.0/knockout-min.js"></script>
        <script type="text/javascript" src="//cdnjs.cloudflare.com/ajax/libs/bootstrap-datetimepicker/4.17.37/js/bootstrap-datetimepicker.min.js"></script>

        <script type="text/javascript" src="<c:url value="/js/app/app-angular.js"/>"></script>
        <script type="text/javascript" src="<c:url value="/js/app-jquery.js"/>"></script>

    </head>
    <body class="casmgmt-app" ng-controller="actionsController as action">

<header id="casmgmt-header">
    <nav class="navbar navbar-default">
        <div class="container-fluid">
            <!-- Brand and toggle get grouped for better mobile display -->
            <div class="navbar-header">
                <a href="manage.html" target="_self" id="homepageUrlLink">
                    <div class="navbar-brand">
                        <img src="<c:url value="/images/logo_cas.png"/>" alt="Jasig CAS logo" />
                    </div>
                    <h4><spring:message code="management.services.header.apptitle" /></h4>
                </a>
            </div><%-- end .navbar-header div --%>

            <!-- Collect the nav links, forms, and other content for toggling -->
            <div class="collapse navbar-collapse" id="casmgt-navbar-collapse">
                <ul class="nav navbar-nav navbar-right quicklinks">
                    <li>
                        <a id="manageServices" href="javascript://" ng-click="action.homepage()">
                            <i class="fa fa-gears"></i>
                            <spring:message code="management.services.header.navbar.navitem.manageService" />
                        </a>
                    </li>
                    <li>
                        <a href="javascript://" ng-click="action.serviceAdd()">
                            <i class="fa fa-plus-circle"></i>
                            <spring:message code="management.services.header.navbar.navitem.addNewService" />
                        </a>
                    </li>
                    <li>
                        <a href="logout.html" target="_self" id="logoutUrlLink">
                            <i class="fa fa-sign-out"></i>
                            <spring:message code="management.services.header.navbar.navitem.logout" />
                        </a>
                    </li>
                </ul>
            </div><%-- end .navbar-collapse div --%>
        </div><%-- end .container-fluid div --%>
    </nav><%-- end .navbar div --%>
</header><%-- end .casmgmt-header header --%>
