<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="org.springframework.security.ui.AbstractProcessingFilter"%>
<jsp:directive.include file="/WEB-INF/view/jsp/default/ui/includes/top.jsp" />
<div class="errors" id="status">
	<h2>Authorization Failure</h2>
	<p>You are not authorized to use this application for the following reason: 
	<%final Exception e = (Exception) request.getSession().getAttribute(AbstractProcessingFilter.SPRING_SECURITY_LAST_EXCEPTION_KEY);%>
	<%=e.getMessage()%>.
	</p>
	<p>
</div>
<jsp:directive.include file="/WEB-INF/view/jsp/default/ui/includes/bottom.jsp" />