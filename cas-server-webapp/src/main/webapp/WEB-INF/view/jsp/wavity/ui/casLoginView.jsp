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

<%@ page import="java.net.URL" %>
<%@ page import="org.jasig.cas.web.wavity.ThemeUtils" %>
<%! public URL fileURL;%>
	
<html lang="en">
	<head>
		<meta charset="UTF-8">
		<meta http-equiv="X-UA-Compatible" content="IE=edge">
		<title>Wavity Login</title>
		<meta name="viewport" content="width=device-width, initial-scale=1">

		<spring:theme code="standard.login.css.bootstrap" var="loginCssBootstrap" />
	    <spring:theme code="standard.login.css.form" var="loginCssForm" />
	    <spring:theme code="standard.login.css.animate" var="loginCssAnimate" />
	    <spring:theme code="standard.login.css.login" var="loginCssLogin" />
	    <spring:theme code="standard.login.css.stickyFooter" var="loginCssStickyFooter" />
		<link rel="stylesheet" href="<c:url value="${loginCssBootstrap}" />" />
		<link rel="stylesheet" href="<c:url value="${loginCssForm}" />" />
		<link rel="stylesheet" href="<c:url value="${loginCssAnimate}" />" />
		<link rel="stylesheet" href="<c:url value="${loginCssLogin}" />" />
		<link rel="stylesheet" href="<c:url value="${loginCssStickyFooter}" />" />
		
		<spring:theme code="cas.login.javascript.require" var="loginJsRequire" />
		<script type="text/javascript" src="<c:url value="${loginJsRequire}" />"></script>
		<script>
			require(['themes/wavity/res/index'], function() {
				require(['jquery', '../wavity/js/init/logininit'], function($, LoginInit){
					$(function() {
						var errorCode = "null";
						LoginInit(errorCode);
						console.log("Wavity login is ready.");
					});
				});
			});
		</script>
	</head>
	<body role="application" class="bodyLayout">
		<form:form method="post" id="tempForm" commandName="${commandName}" htmlEscape="true"></form:form>
        <%
		String serviceUrl = request.getParameter("service");
        StringBuilder casLoginUrl = new StringBuilder()
        		.append(request.getScheme())
        		.append("://")
        		.append(request.getServerName())
        		.append("/auth/login");
        response.addHeader("Cas-Server-Login-Url", casLoginUrl.toString());

		String tenantName = "";
		String appName = "";
	    if (serviceUrl != null) {
			String[] str2 = serviceUrl.split("//");
			tenantName = str2[1].split("\\.")[0];

			String str3 = str2[1].split("/")[1];
			appName = str3.split("\\?")[0];
	    }

	    String tenantLogo = ThemeUtils.fetchTenantLogo(request, tenantName);
		//String appLogo = ThemeUtils.fetchAppLogo(request, appName);
		String appLogo = null;
	    %>

		<spring:theme code="standard.login.app.logo" var="defaultAppLogo" />
		<spring:theme code="standard.login.tenant.logo" var="defaultTenantLogo" />
        <c:set var="appLogo" value="<%=appLogo%>"/>
        
        <c:if test="${empty appLogo}">
            <c:set var="appLogo" value="${defaultAppLogo}" />
        </c:if>
		
		<c:set var="tenantLogo" value="<%=tenantLogo%>"/>
		<c:if test="${empty tenantLogo}">
            <c:set var="tenantLogo" value="${defaultTenantLogo}" />
        </c:if>

		<input type="hidden" name="appLogo" value="${appLogo}" />
		<input type="hidden" name="tenantLogo" value="${tenantLogo}" />

		<spring:theme code="standard.login.app.logo" var="defaultAppLogo" />
		<spring:theme code="standard.login.tenant.logo" var="defaultTenantLogo" />
		<input type="hidden" name="defaultAppLogo" value="${defaultAppLogo}" />
		<input type="hidden" name="defaultTenantLogo" value="${defaultTenantLogo}" />
		
		<input type="hidden" name="appName" value="${appName}" />
		<input type="hidden" name="tenantName" value="${tenantName}" />
		<input type="hidden" name="tenantLogoUrl" value="${largeLogo}" />
		<input type="hidden" name="loginTicket" value="${loginTicket}" />
		<input type="hidden" name="flowExecutionKey" value="${flowExecutionKey}" />

		<header role="banner" id="ot-header" class="header">
			<!-- header region -->
		</header>
		<main role="main" id="ot-main" class="main">

		</main>
		
		<form:errors path="*" />
		
		<c:forEach var="message" items="${flowRequestContext.messageContext.allMessages}">
    <c:if test="${message.severity eq 'ERROR'}">
        <span>${message.text}</span>
    </c:if>
</c:forEach>
  
		<c:if test="${!empty pac4jUrls}">
            <div id="list-providers" style="color:#fff">
                <h3><spring:message code="screen.welcome.label.loginwith" /></h3>
                <form>
                    <ul>
                        <c:forEach var="entry" items="${pac4jUrls}">
                            <li id="${entry.key}"><a href="${entry.value}" style="color:#fff">${entry.key}</a></li>
                        </c:forEach>
                    </ul>
                    
                </form>
            </div>
        </c:if>
		<footer role="contentinfo" id="ot-footer" class="footer login-footer">
			<!-- footer region -->
		</footer>

	</body>
</html>