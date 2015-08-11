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
<!DOCTYPE html>

<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<html lang="en">
<head>
  <meta charset="UTF-8" />
  
  <title>CAS &#8211; Central Authentication Service</title>

  <!-- Twitter Bootstrap UI framework -->
  <link href="/cas/css/bootstrap.min.css" rel="stylesheet">  

  <!-- Google Fonts -->
  <link href='//fonts.googleapis.com/css?family=Lato:300,400,700' rel='stylesheet' type='text/css'>
  <link href='//fonts.googleapis.com/css?family=Open+Sans:400,700,800' rel='stylesheet' type='text/css'>         
    
  <spring:theme code="standard.custom.css.file" var="customCssFile" />
  <link rel="stylesheet" href="<c:url value="${customCssFile}" />" />
  <link rel="icon" href="<c:url value="/favicon.ico" />" type="image/x-icon" />
     
        
  <!--[if lt IE 9]>
    <script src="//cdnjs.cloudflare.com/ajax/libs/html5shiv/3.6.1/html5shiv.js" type="text/javascript">   </script>
  <![endif]-->
</head>
<body>

        <!-- Main Header/Navigation 
        <nav class="navbar navbar-default navbar-static-top" id="top-navbar" role="navigation">
            <div class="container">
                <a class="navbar-brand" href="#"><img src="/cas/images/6inchbrand-64.png" alt="USUHS Logo" />
                    <span class="logo hidden-xs"><span class="heavy">CAS</span>Monitoring</span></a>
            </div>
        </nav>-->
    
    
      <div class="container">
         <header>
            <a id="logo" href="http://www.apereo.org" title="<spring:message code="logo.title" />">Apereo</a>
            <h1>Central Authentication Service (CAS)</h1>
          </header> 
          <div id="content">
