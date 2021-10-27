<jsp:directive.include file="includes/top.jsp" />
  <div id="msg" class="errors">
    <h2><spring:message code="screen.expiredpass.heading" /></h2>
    <p><spring:message code="screen.expiredpass.message" arguments="${passwordPolicyUrl}" /></p>
  </div>
<jsp:directive.include file="includes/bottom.jsp" />
