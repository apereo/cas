<%@ page session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!-- Minimal Web pages, starting point for Web Designers -->

<%@page import="org.springframework.util.StringUtils"%>
<html xmlns="http://www.w3.org/1999/xhtml" lang="en">
	<head>
		<title>JA-SIG  Central  Authentication  Service (CAS)</title>
	</head>
  <body>
	<form method="post" action="<%=response.encodeRedirectURL("login" + (StringUtils.hasText(request.getQueryString()) ? "?" + request.getQueryString() : ""))%>">
	
	<!-- Begin error message generating Server-Side tags -->
	<spring:hasBindErrors name="credentials">
	  <c:forEach var="error" items="${errors.allErrors}">
	      <br /><spring:message code="${error.code}" text="${error.defaultMessage}" />
	  </c:forEach>
	</spring:hasBindErrors>
	<!-- End error message generating Server-Side tags -->
	
	<p>Userid 
		<input id="username" name="username" size="32" tabindex="1" accesskey="n" />
	</p>

	<p>Password
		<input type="password" id="password" name="password" size="32" tabindex="2" accesskey="p" />
	</p>

	<p><input type="checkbox" id="warn" name="warn" value="false" tabindex="3" /> 
	   Warn me before logging me into other sites.
	   
	<!-- The following hidden field must be part of the submitted Form -->   
			<input type="hidden" name="lt" value="${flowExecutionKey}" />
			<input type="hidden" name="_eventId" value="submit" />

	</p>

	<p>
	<input type="submit" class="button" accesskey="l" value="LOGIN" tabindex="4" />
	</p>
	</form>
</body>
</html>

