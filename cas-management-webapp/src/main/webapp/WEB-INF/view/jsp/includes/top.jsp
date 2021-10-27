<%--

    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
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
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page language="java"  session="false" %>
<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
  <title><spring:message code="management.services.title" /></title>
  <meta name="version" content="<%=org.jasig.cas.CasVersion.getVersion()%>" />
  <link rel="icon" href="<c:url value="../favicon.ico" />" type="image/x-icon" />
  <link rel="stylesheet" href="<c:url value="/css/management.css" />" type="text/css" />
  <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js"></script>
  <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.10.3/jquery-ui.min.js"></script>
  
  <script type="text/javascript" src="<c:url value="/js/management.js" />"></script>

  <style type="text/css">
  #nav-main #${pageTitle} span {
      background:#fff;
      color: #000;
  }
  </style>
</head>

<body>
<div id="header">
  <div id="nav-system">
    <ul>
    	<li><a href="logout.html" title="logout of current session"><spring:message code="management.services.link.logout" /></a></li>
    </ul>
  </div>
  <p id="tagline"><spring:message code="application.title" /></p>
  <h1 id="app-name"><spring:message code="management.services.title" /></h1>
</div>
<div id="nav-main">
  <ul>
  	<li><a id="addServiceView" href="add.html"><span><spring:message code="addServiceView" /></span></a></li>
  	<li><a id="manageServiceView" href="manage.html"><span><spring:message code="manageServiceView" /></span></a></li>
  </ul>
</div>
<!-- CONTENT -->
<div id="content">
