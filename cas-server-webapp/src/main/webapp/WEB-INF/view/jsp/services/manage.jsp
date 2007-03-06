<%@include file="includes/top.jsp"%>
	<form id="" method="post" action="#">
		<table cellspacing="0" class="large">
			<thead>
			<tr>
				<th>Service Name</th>
				<th>ID</th>
				<th class="center">Enabled</th>
				<th class="center">Allowed to Proxy</th>
				<th class="center">SSO Participant</th>
				<th colspan="3">Attributes</th>
			</tr>
			</thead>
			<tbody>
			<c:forEach items="${services}" var="service">
			<tr id="${service.id}"${param.action eq 'delete' and param.id eq service.id ? ' class="highlightTop"' : ''}>
				<td>${service.name}</td>
				<td>${service.id}</td>
				<td class="center"><img src="../images/services/${service.enabled}.gif" /></td>
				<td class="center"><img src="../images/services/${service.allowedToProxy}.gif" /></td>
				<td class="center"><img src="../images/services/${service.ssoEnabled}.gif" /></td>
				<td>
					<select name="attributes">
					<c:forEach items="${service.allowedAttributes}" var="attribute">
					<option value="${attribute}">${attribute}</option>
					</c:forEach>
				</td>
				<td><a href="manage.html?id=${service.id}" class="edit">edit</a></td>
				<td><a href="manage.html?id=${service.id}&amp;action=delete#${service.id}" class="del">delete</a></td>
			</tr>
			<c:if test="${param.action eq 'delete' and param.id eq service.id}">
			<tr class="highlightBottom">
				<td colspan="8">
			   	Are you sure you want to delete this service?
			   	<a href="?action=delete&id=${service.id}&confirm=true">Yes</a>
			   	<a href="?action=delete&id=${service.id}&confirm=false">No</a>
			    </td>
			    </tr>
			</c:if>
			</c:forEach>
			</tbody>
		</table>
	</form>
<%@include file="includes/bottom.jsp" %>