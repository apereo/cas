<jsp:directive.include file="/WEB-INF/view/jsp/default/ui/includes/top.jsp" />

<%@ page isErrorPage="true" %>
<%@ page import="org.jasig.cas.web.support.WebUtils"%>

<div id="msg" class="errors">
    <h2>${pageContext.errorData.statusCode} - <spring:message code="screen.blocked.header" /></h2>

    <%
        Object casAcessDeniedKey = request.getAttribute(WebUtils.CAS_ACCESS_DENIED_REASON);
        request.setAttribute("casAcessDeniedKey", casAcessDeniedKey);

    %>

    <c:choose>
        <c:when test="${not empty casAcessDeniedKey}">
            <p><spring:message code="${casAcessDeniedKey}" /></p>
        </c:when>
    </c:choose>
    <p><%=request.getAttribute("javax.servlet.error.message")%></p>
    <p><spring:message code="AbstractAccessDecisionManager.accessDenied"/></p>
</div>
<jsp:directive.include file="/WEB-INF/view/jsp/default/ui/includes/bottom.jsp" />
