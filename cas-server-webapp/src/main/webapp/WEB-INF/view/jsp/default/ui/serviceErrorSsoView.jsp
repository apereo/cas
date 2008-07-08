<jsp:directive.include file="includes/top.jsp" />
		<div id="status" class="errors">
			<h2><spring:message code="screen.service.sso.error.header" /></h2>
			<p><spring:message code="screen.service.sso.error.message"  arguments="${fn:escapeXml(request.requestURI)}" /></p>
		</div>
<jsp:directive.include file="includes/bottom.jsp" />