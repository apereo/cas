<jsp:directive.include file="includes/top.jsp" />
  <div id="msg" class="errors">
    <h2><spring:message code="screen.service.error.header" /></h2>
    <p><spring:message code="${rootCauseException.code}" /></p>
  </div>
<jsp:directive.include file="includes/bottom.jsp" />
