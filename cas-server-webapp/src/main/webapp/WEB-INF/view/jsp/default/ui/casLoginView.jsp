<%@ page session="true" %>
<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en">
	<head>
	    <title>CAS &#8211; Central Authentication Service</title>
	    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	    <style type="text/css" media="screen">@import 'css/cas.css'/**/;</style>
	    <!--[if gte IE 6]><style type="text/css" media="screen">@import 'css/ie_cas.css';</style><![endif]-->
	    <script type="text/javascript" src="js/common_rosters.js"></script>
	</head>

	<body id="cas" onload="document.getElementById('username').focus();">
	    <div id="header">
	        <h1 id="app-name">Central Authentication Service (CAS)</h1>
	    </div>

	    <div id="content">
			<form:form method="post" id="fm1" cssClass="fm-v clearfix" commandName="credentials" htmlEscape="true">
			    <form:errors path="*" cssClass="errors" id="status" element="div" />
	            <fieldset>
	                <legend>CAS Login Form</legend>
	                <div class="box" id="login">
	                <!-- <spring:message code="screen.welcome.welcome" /> -->
	                    <h2><spring:message code="screen.welcome.instructions" /></h2>
	                    <div class="row">
	                        <label for="username"><spring:message code="screen.welcome.label.netid" /></label>
							<c:if test="${not empty sessionScope.openIdLocalId}">
							<strong>${sessionScope.openIdLocalId}</strong>
							<input type="hidden" id="username" name="username" value="${sessionScope.openIdLocalId}" />
							</c:if>
							
							<c:if test="${empty sessionScope.openIdLocalId}">
							<spring:message code="screen.welcome.label.netid.accesskey" var="userNameAccessKey" />
							<form:input cssClass="required" cssErrorClass="error" id="username" size="25" tabindex="1" accesskey="${userNameAccessKey}" path="username" autocomplete="false" htmlEscape="true" />
							</c:if>
	                    </div>
	                    <div class="row">
	                        <label for="password"><spring:message code="screen.welcome.label.password" /></label>
							<%--
							NOTE: Certain browsers will offer the option of caching passwords for a user.  There is a non-standard attribute,
							"autocomplete" that when set to "off" will tell certain browsers not to prompt to cache credentials.  For more
							information, see the following web page:
							http://www.geocities.com/technofundo/tech/web/ie_autocomplete.html
							--%>
							<spring:message code="screen.welcome.label.password.accesskey" var="passwordAccessKey" />
							<form:password cssClass="required" cssErrorClass="error" id="password" size="25" tabindex="2" path="password"  accesskey="${passwordAccessKey}" htmlEscape="true" />
	                    </div>
	                    <div class="row check">
	                        <input id="warn" name="warn" value="true" tabindex="3" accesskey="<spring:message code="screen.welcome.label.warn.accesskey" />" type="checkbox" />
	                        <label for="warn"><spring:message code="screen.welcome.label.warn" /></label>
	                    </div>
	                    <div class="row btn-row">
							<input type="hidden" name="lt" value="${flowExecutionKey}" />
							<input type="hidden" name="_eventId" value="submit" />

	                        <input class="btn-submit" accesskey="l" value="<spring:message code="screen.welcome.button.login" />" tabindex="4" type="submit" />
	                        <input class="btn-reset" accesskey="c" value="<spring:message code="screen.welcome.button.clear" />" tabindex="5" type="reset" />
	                    </div>
	                </div>
	            </fieldset>
               
            <div id="sidebar">
                <p><spring:message code="screen.welcome.security" /></p>
                <div id="list-languages">
					<c:set var="query" value="<%=request.getQueryString() == null ? "" : request.getQueryString().replaceAll("&locale=([A-Za-z][A-Za-z]_)?[A-Za-z][A-Za-z]|^locale=([A-Za-z][A-Za-z]_)?[A-Za-z][A-Za-z]", "")%>" />
					<c:set var="loginUrl" value="login?${query}${not empty query ? '&' : ''}locale=" />
                    <h3>Languages:</h3>
					<ul
						><li class="first"><a href="login?${query}${not empty query ? '&' : ''}locale=en">English</a></li
						><li><a href="${loginUrl}es">Español</a></li				
						><li><a href="${loginUrl}fr">Français</a></li
						><li><a href="${loginUrl}ru">Russian</a></li
						><li><a href="${loginUrl}nl">Nederlands</a></li
						><li><a href="${loginUrl}sv">Svenskt</a></li
						><li><a href="${loginUrl}it">Italiano</a></li
						><li><a href="${loginUrl}ur">Urdu</a></li
						><li><a href="${loginUrl}zh_CN">Simplified Chinese</a></li
						><li><a href="${loginUrl}de">Deutsch</a></li
						><li class="last"><a href="${loginUrl}ja">Japanese</a></li
					></ul>
                </div>
            </div>
        </form:form>
    </div>
    <div id="footer">
        <div>
            <p>Copyright &copy; 2005-2007 JA-SIG. All rights reserved.</p>
            <p>Powered by <a href="http://www.ja-sig.org/products/cas/"><%=org.jasig.cas.CasVersion.getVersion()%></a></p>
        </div>
        <a href="http://www.ja-sig.org" title="go to JA-SIG home page"><img id="logo" src="images/ja-sig-logo.gif" width="118" height="31" alt="JA-SIG" title="go to JA-SIG home page" /></a>
    </div>

</body>
</html>

