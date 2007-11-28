<%@include file="includes/top.jsp"%>

<h2>Filter</h2>
<form method="get" action="viewLogRequests.html" style="width:100%; display: block;" class="full">
<p><label for="eventType"><spring:message code="administration.logs.form.filter.eventType" />:</label>
<select name="eventType">
	<option></option>
	<c:forEach items="${eventTypes}" var="eventType">
		<option${eventType eq logSearchRequest.eventType ? ' selected="selected"' : ''}>${eventType}</option>
	</c:forEach>
</select>
<fmt:formatDate value="${logSearchRequest.dateFrom}" pattern="yyyy-MM-dd HH:mm" var="dateFrom" />
<label for="dateFrom"><spring:message code="administration.logs.form.filter.dateFrom" />:</label> <input type="text" value="${dateFrom}" name="dateFrom" /></p>
<label for="principal"><spring:message code="administration.logs.form.filter.principal" />:</label> <input type="text" value="${logSearchRequest.principal}" name="principal" />
<input type="submit" value="<spring:message code="administration.logs.form.filter.button.submit" />" /></p>
</form>

<br style="clear:both" />

<table>
	<thead>
		<tr>
			<td><spring:message code="administration.logs.table.header.date" /></td>
			<td><spring:message code="administration.logs.table.header.principal" /></td>
			<td><spring:message code="administration.logs.table.header.eventType" /></td>
			<td><spring:message code="administration.logs.table.header.service" /></td>
			<td><spring:message code="administration.logs.table.header.clientIpAddress" /></td>
			<td><spring:message code="administration.logs.table.header.serverIpAddress" /></td>
			<td><spring:message code="administration.logs.table.header.userAgent" /></td>
		</tr>
	</thead>
	<tbody>
<c:forEach items="${logRequests}" var="logRequest">
		<tr>
		
			<td><fmt:formatDate value="${logRequest.clientInfo.requestDate}" pattern="yyyy-MM-dd HH:mm" /></td>
			<td>${logRequest.principal}</td>
			<td>${logRequest.eventType}</td>
			<td><span title="${logRequest.service}">${fn:substring(logRequest.service, 0, 50)}</span></td>
			<td>${logRequest.clientInfo.clientIpAddress}</td>
			<td>${logRequest.clientInfo.serverIpAddress}</td>
			<td><span title="${logRequest.clientInfo.userAgent}">${fn:substring(logRequest.clientInfo.userAgent, 0, 100)}</span></td>
		</tr>
</c:forEach>
	</tbody>
</table>
<%@include file="includes/bottom.jsp"%>