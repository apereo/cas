<jsp:directive.include file="includes/top.jsp" />
  <c:url var="url" value="/login">
    <c:param name="service" value="${param.service}" />
    <c:param name="renew" value="true" />
  </c:url>
  
  <div id="msg" class="errors">
    <h2><spring:message code="screen.service.sso.error.header" /></h2>
    <p><spring:message code="screen.service.sso.error.message"  arguments="${fn:escapeXml(url)}" /></p>
  </div>
<jsp:directive.include file="includes/bottom.jsp" />
