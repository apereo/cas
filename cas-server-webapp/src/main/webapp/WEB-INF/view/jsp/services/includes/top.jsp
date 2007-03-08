<%@ page language="java"  session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
  <title><spring:message code="${pageTitle}" /></title>
  <meta name="version" content="<%=org.jasig.cas.CasVersion.getVersion()%>" />
  <link rel="stylesheet" href="../css/services/cas.css" type="text/css" media="screen" />
  <!--[if lt IE 7]>
    <link rel="stylesheet" href="../css/services/ie.css" type="text/css" media="screen" />
  <![endif]-->
  <link rel="stylesheet" href="css/regis-print.css" type="text/css" media="print" />
<!--  <script type="text/javascript" src="js/common.js"></script>-->
  <script type="text/javascript" src="../js/common_rosters.js"></script>
  
  <style type="text/css">
  
  #nav-main #${pageTitle} span {
      background:#fff;
      color: #000;
  }
  </style>
</head>

<body id="${pageTitle}">
<div id="header">
  <div id="nav-system">
    <ul
      ><li><a href="help.pdf">Help</a></li
      ><li><a href="logout.html" title="logout of current session">Log Out</a></li
    ></ul>
  </div>
  <p id="tagline">JA-SIG Central Authentication Service</p>
  <h1 id="app-name">Services Management</h1>
</div>
<div id="nav-main">
  <ul
    ><li><a id="addServiceView" href="add.html"><span>Add Service</span></a></li
    ><li><a id="manageServiceView" href="manage.html"><span>Manage Services</span></a></li
  ></ul>
</div>
<!-- CONTENT -->
<div id="content">
  <h1><spring:message code="${pageTitle}" /></h1>
