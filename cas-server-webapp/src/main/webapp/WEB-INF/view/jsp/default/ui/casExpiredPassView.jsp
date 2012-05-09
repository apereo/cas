<jsp:directive.include file="includes/top.jsp" />
<div class="errors">
  <p><h2><spring:message code="screen.expiredpass.heading" /></h2></p>
  <p><spring:message code="screen.expiredpass.message" arguments="${passwordPolicyUrl}" /></p></div>
<jsp:directive.include file="includes/bottom.jsp" />
