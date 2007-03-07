<%@include file="includes/top.jsp"%>
	<table cellspacing="0" class="large">
		<thead>
		<tr>
			<th>Service Name</th>
			<th>Service Url</th>
			<th class="ac">Enabled</th>
			<th class="ac">Allowed to Proxy</th>
			<th class="ac">SSO Participant</th>
			<th colspan="2">&nbsp;</th>
		</tr>
		</thead>
		<tbody>
		<c:forEach items="${services}" var="service">
		<tr id="${service.id}"${param.action eq 'delete' and param.id eq service.id ? ' class="highlightTop"' : ''}${param.action eq 'add' and param.id eq service.id ? ' class="added"' : ''}>
			<td>${service.name}</td>
			<td>${service.serviceId}</td>
			<td class="ac"><img src="../images/services/${service.enabled}.gif" /></td>
			<td class="ac"><img src="../images/services/${service.allowedToProxy}.gif" /></td>
			<td class="ac"><img src="../images/services/${service.ssoEnabled}.gif" /></td>
			<td><a href="manage.html?id=${service.id}" class="edit">edit</a></td>
			<td><a href="manage.html?id=${service.id}&amp;action=delete#${service.id}" class="del">delete</a></td>
		</tr>
		<c:if test="${param.action eq 'delete' and param.id eq service.id}">
		<tr class="highlightBottom">
			<td colspan="7">
		   	Are you sure you want to delete this service?
		   	<a href="?action=delete&id=${service.id}&confirm=true">Yes</a>
		   	<a href="?action=delete&id=${service.id}&confirm=false">No</a>
		    </td>
		    </tr>
		</c:if>
		
		<c:if test="${param.action eq 'add' and param.id eq service.id}">
		<tr>
			<td colspan="7">
			<a href="add.html">Add another?</a>
			</td>
		</tr>
		</c:if>
		</c:forEach>
		</tbody>
	</table>
<%@include file="includes/bottom.jsp" %>