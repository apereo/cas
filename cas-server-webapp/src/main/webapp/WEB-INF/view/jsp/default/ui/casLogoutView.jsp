<%-- <jsp:directive.include file="includes/login_top.jsp" />
	<div style="background-color: #333;">
		<div id="msg" class="success">
			<h2><spring:message code="screen.logout.header" /></h2>
			<p><spring:message code="screen.logout.success" /></p>
			<p><spring:message code="screen.logout.security" /></p>
		</div>
		<div><a href="/cas/login">Log in</a></div>
	</div>
<jsp:directive.include file="includes/login_bottom.jsp" /> --%>

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
		<title>Wavity Logout</title>
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
	</head>
	<body role="application" class="bodyLayout">
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
	    %>
	    <c:set var="tenantLogo" value="<%=tenantLogo%>"/>
	    
	    <spring:theme code="standard.login.app.logo" var="defaultAppLogo" />
	    <c:set var="appLogo" value="${defaultAppLogo}" />
	    <spring:theme code="standard.logout.ics.logo" var="defaultICSLogo" />
	    <c:set var="icsLogo" value="${defaultICSLogo}" />
		
		<header role="banner" id="ot-header" class="header">
			<!-- header region -->
		</header>
		<main role="main" id="ot-main" class="main">
			<section id="loginColumns" class="animated fadeInDown">
				<div class="row">		
					<div class="col-md-6 hidden-xs tenant-logo-wrapper">
						<img id="domainIcon" class="m-t-50" src="${tenantLogo}"/>
					</div>
					<div class="col-md-6">
						<div style="color: #333; width: 400px;">
							<div id="images" style="margin-bottom: 30px;">
								<img src="${appLogo}" style="width: 150px; height: 150px; float: left;">
								<img src="${icsLogo}" style="width: 150px; height: 150px; margin-left: 30px;">
							</div>
							<div id="msg" class="success">
								<h2><spring:message code="screen.logout.header" /></h2>
								<p><spring:message code="screen.logout.success" /></p>
								<p><spring:message code="screen.logout.security" /></p>
								<p id="service-url-container">This page will be redirected to <span id="service-url"></span> after <span id="counter"></span> seconds.</p>
								<p id="service-url-link-container">If you want go to the service instantly, click <a id="service-url-link">this link.</a></p>
							</div>
						</div>
					</div>
				</div>
			</section>
		</main>
		<footer role="contentinfo" id="ot-footer" class="footer">
			<!-- footer region -->
		</footer>
		<script type="text/javascript">
			window.onload = function(e){
				var tenantLogo = document.getElementById("domainIcon");
				if(tenantLogo.width > 400 || tenantLogo.width <= 0) {
					tenantLogo.width = 400;
				}
				
				if(tenantLogo.height > 400 || tenantLogo.height <= 0) {
					tenantLogo.height = 400;
				}
				
			    var serviceUrl = getQueryParams(window.location.search);
			    if(serviceUrl != null && serviceUrl != "" && validateUrl(serviceUrl)) {
				    var serviceUrlElement = document.getElementById("service-url");
				    serviceUrlElement.textContent = serviceUrl;
				    
				    var linkElement = document.getElementById("service-url-link");
				    linkElement.href = serviceUrl;
				    
				    var counter = 5;
				    var counterElement = document.getElementById("counter");
				    counterElement.textContent = counter
				    setInterval(function(){ 
				    	--counter;
				    	if(counter >= 0) {
				    		counterElement.textContent = counter;
				    	}
				    	if(counter == 0) {
				    		window.location.replace(serviceUrl);
				    	}
				    }, 1000);
			    }
			    else {
			    	var serviceUrlContainerElement = document.getElementById("service-url-container");
			    	serviceUrlContainerElement.remove();
				    var serviceUrlLinkContainerElement = document.getElementById("service-url-link-container");
				    serviceUrlLinkContainerElement.remove();
			    }
			}
			function getQueryParams(qs, parameterName) {
				if(qs == null || qs == "" || qs.substring(0) === "?") {
					return "";
				}
				
				var parameterName="service";
				var value = decodeURIComponent((new RegExp('[?|&]' + parameterName + '=' + '([^&;]+?)(&|#|;|$)').exec(location.search) || [null, ''])[1].replace(/\+/g, '%20')) || null;
				return value;
			}
			function validateUrl(value){
				return /^(https?|ftp):\/\/(((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:)*@)?(((\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5]))|((([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.)+(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.?)(:\d*)?)(\/((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)+(\/(([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)*)*)?)?(\?((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)|[\uE000-\uF8FF]|\/|\?)*)?(\#((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)|\/|\?)*)?$/i.test(value);
			}
		</script>
	</body>
</html>