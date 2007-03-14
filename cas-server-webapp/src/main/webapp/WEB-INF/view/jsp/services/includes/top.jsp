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
  <script type="text/javascript" src="../js/common_rosters.js"></script>
  
  <style type="text/css">
  #nav-main #${pageTitle} span {
      background:#fff;
      color: #000;
  }
  </style>
  
    <style type="text/css">

@media screen {
    div#container {width:100%; min-width:952px; margin:0; padding:0;}
    
        table#headerTable {width:100%; background:#999; margin:0; padding:0; border:0; border-collapse:collapse;}
    
    div.tableWrapper {width:100%; min-width:952px; height:250px; overflow:auto; overflow-x:hidden; overflow-y:scroll;}
       
        table#scrollTable {width:100%; min-width:935px;}

            table#scrollTable thead {display:none;}
                table#headerTable th, table#scrollTable td {padding:0 5px; border:0;}
                table#scrollTable td {text-align:left; border-bottom:1px solid #eee;}
                table#headerTable th {height:38px; border:0 !important;}

                th.th1, td.td1 {width:200px; overflow:hidden;}
                th.th2, td.td2 {overflow:hidden;}
                th.th3, td.td3 {width:50px}
                th.th4, td.td4 {width:70px}
                th.th5, td.td5 {width:50px}
                th.th6, td.td6 {width:70px; text-align:right !important;}
                th.th7, td.td7 {width:102px; text-align:right !important;}
                td.td7 {width:85px;}
}
</style>
<!--[if IE]>
<style type="text/css">
 td.td7 {width:85px; padding-right:22px !important;}
 </style>
<![endif]--> 
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
