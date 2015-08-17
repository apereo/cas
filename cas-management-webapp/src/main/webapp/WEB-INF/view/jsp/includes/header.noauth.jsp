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
        <link rel="icon" href="<c:url value="/images/favicon.ico" />" type="image/x-icon" />
        <link rel="stylesheet" href="//maxcdn.bootstrapcdn.com/font-awesome/4.3.0/css/font-awesome.min.css">
        <link rel="stylesheet" href="<c:url value="/css/cas-management.css" />" type="text/css" />
    </head>
    <body class="casmgmt-app" ng-controller="actionsController as action">

<header id="casmgmt-header">
    <nav class="navbar navbar-default">
        <div class="container-fluid">
            <div class="navbar-header">
                <a href="manage.html">
                    <div class="navbar-brand">
                        <img src="<c:url value="/images/logo_cas.png"/>" alt="Jasig CAS logo" />
                    </div>
                    <h4><spring:message code="management.services.header.apptitle" /></h4>
                </a>
            </div><%-- end .navbar-header div --%>
        </div><%-- end .container-fluid div --%>
    </nav><%-- end .navbar div --%>
</header><%-- end .casmgmt-header header --%>
