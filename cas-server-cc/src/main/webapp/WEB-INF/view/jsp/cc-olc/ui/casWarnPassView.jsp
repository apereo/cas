<jsp:directive.include file="includes/top.jsp" />
<div class="errors">
	<p>
		<c:if test="${expireDays == 0}">
			<h2><spring:message code="screen.warnpass.heading.today" /></h2>
		</c:if>
		<c:if test="${expireDays == 1}">
			<h2><spring:message code="screen.warnpass.heading.tomorrow" /></h2>
		</c:if>
		<c:if test="${expireDays > 1}">
			<h2><spring:message code="screen.warnpass.heading.other" arguments="${expireDays}" /></h2>
		</c:if>
	</p>
	
	<p>
	<spring:message code="screen.warnpass.message.line1" />
	</p>
	<p>
	<spring:message code="screen.warnpass.message.line2" arguments="${fn:escapeXml(param.service)}${fn:indexOf(param.service, '?') eq -1 ? '?' : '&'}ticket=${serviceTicketId}" />
	</p>
</div>
<script type="text/javascript">
<!--

	function redirectTo(URL) {
		window.location = URL ;
	}

	
			setTimeout("redirectTo('${param.service}${fn:indexOf(param.service, '?') eq -1 ? '?' : '&'}ticket=${serviceTicketId}')", 5000);
	
//-->

</script>