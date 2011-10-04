<jsp:directive.include file="/WEB-INF/view/jsp/default/ui/includes/top.jsp" />
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
	<form name="acsForm" action="${originalUrl}" method="post">
            <c:forEach items="${parameters}" var="entry">
                    <input type="hidden" name="${entry.key}" value="<c:out value="${entry.value}" />">
            </c:forEach>
	<p>
	<spring:message code="screen.warnpass.message.line1" />
	</p>
	<p>
	<spring:message code="screen.warnpass.post.message.line2" /> <br>
	<input type="submit" name ="submit" value="<spring:message code="screen.warnpass.post.button" />">
	</p>
	</form>
</div>
<script type="text/javascript">
<!--
	
			setTimeout(document.acsForm.submit(), 5000);

//-->

</script>