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
    <%@ page language="java"  session="false" %>
    <%@ page pageEncoding="UTF-8" %>
    <%@ page contentType="text/html; charset=UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
    <%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
    <%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
    <%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
    <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

    <html xml:lang="en" lang="en" ng-app="casmgt">
    <head>
        <title><spring:message code="management.services.title" /></title>
        <meta name="version" content="<%=org.jasig.cas.CasVersion.getVersion()%>" />
        <link rel="icon" href="<c:url value="../favicon.ico" />" type="image/x-icon" />
        <link rel="stylesheet" href="//maxcdn.bootstrapcdn.com/font-awesome/4.3.0/css/font-awesome.min.css">
        <link rel="stylesheet" href="<c:url value="/css/cas-management.css" />" type="text/css" />

        <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js"></script>
        <script type="text/javascript" src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/js/bootstrap.min.js"></script>
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/angularjs/1.3.15/angular.min.js"></script>
        <script type="text/javascript" src="<c:url value="/js/app/app.js"/>"></script>
    </head>
    <body class="casmgmt-app">

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
                            <li><a href="logout.html"><spring:message code="management.services.link.logout" /></a></li>
                        </ul>
                    </div><!-- /.navbar-collapse -->
                </div><!-- /.container-fluid -->
            </nav>
        </header>
        <div class="container-fluid">



            <!-- Content -->
            <div class="row">
                <div class="col-md-12">
                    <h3>Hello, title! {{ "Angular" }} is working!</h3>
                </div>
            </div>


        </div>
        <!-- Footer -->
        <footer id="casmgmt-footer">
            <div class="container-fluid">
                <div class="row">
                    <div class="col-sm-10">
                        <span><spring:message code="footer.links" /></span>
                        <ul class="nav-campus-sites list-inline">
                            <li><a href="http://www.apereo.org/cas" rel="_blank"><spring:message code="footer.homePage" /></a>,</li>
                            <li><a href="http://wiki.jasig.org" rel="_blank"><spring:message code="footer.wiki" /></a>,</li>
                            <li><a href="http://issues.jasig.org" rel="_blank"><spring:message code="footer.issueTracker" /></a>,</li>
                            <li><a href="http://www.apereo.org/cas/mailing-lists" rel="_blank"><spring:message code="footer.mailingLists" /></a>.</li>
                        </ul>
                        <div class="copyright">
                            <p><spring:message code="footer.copyright" /><br/>
                            <spring:message code="footer.poweredBy" arguments="<%=org.jasig.cas.CasVersion.getVersion()%>" /></p>
                        </div>
                    </div>
                    <div class="col-sm-2">
                        <div class="jasig-link">
                            <a href="http://www.apereo.org" _target="blank" title="Apereo CAS Home Page"><img src="<c:url value="/images/logo_apereo.png"/>" /></a>
                        </div>
                    </div>
                </div>
            </div>
        </footer>
    </body>
    </html>