<jsp:directive.include file="includes/top.jsp" />
<div id="status" class="errors">
	<p><spring:message code="screen.confirmation.message" arguments="${param.service}${fn:indexOf(param.service, '?') eq -1 ? '?' : '&'}ticket=${serviceTicketId}" /></p>
</div>
<jsp:directive.include file="includes/bottom.jsp" />