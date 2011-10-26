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
    <meta name="version" content="<%=org.jasig.cas.CasVersion.getVersion()%>" />
    <title><spring:message code="${pageTitle}" text="Logged Out" /></title>
    
    <link rel="stylesheet" href="<c:url value="/css/services/cas.css" />" type="text/css" media="screen" />
    
    <!--[if IE]>
        <link rel="stylesheet" href="<c:url value="/css/services/ieFix.css" />" type="text/css" media="screen" />
    <![endif]-->
    
    <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.4.2/jquery.min.js"></script>
    <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.5/jquery-ui.min.js"></script>
    <script type="text/javascript" src="<c:url value="/js/cas.js" />"></script>

    <style type="text/css">
        #nav #${pageTitle} span {
            background:#fff;
            color: #000;
        }
    </style>
</head>

<body id="${pageTitle}-body">
    <div id="container">
          <div id="header">
              <ul class="nav">
                  <li><a href="logout.html" title="Log out of current session"><spring:message code="management.services.link.logout" /></a></li>
              </ul>
              <h2><spring:message code="application.title" /></h2>
              <h1><spring:message code="management.services.title" /></h1>
          </div>
          <div id="nav">
              <ul>
                  <li><a id="addServiceView" href="add.html"><span><spring:message code="addServiceView" /></span></a></li>
                  <li><a id="manageServiceView" href="manage.html"><span><spring:message code="manageServiceView" /></span></a></li>
                  <li><a id="viewStatisticsView" href="viewStatistics.html"><span><spring:message code="viewStatisticsView" /></span></a></li>
              </ul>
          </div>

        <!-- CONTENT -->
        <div id="content">
          <h1><spring:message code="${pageTitle}" text="Logged Out" /></h1>

