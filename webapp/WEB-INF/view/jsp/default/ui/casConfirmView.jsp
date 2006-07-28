<jsp:directive.include file="includes/top.jsp" />
<div id="welcome">
	<p><spring:message code="screen.confirmation.message" arguments="${param.service}${fn:indexOf(params.service, '?') eq -1 ? '?' : '&'}ticket=${ticket}" /></p>
</div>
<jsp:directive.include file="includes/bottom.jsp" />