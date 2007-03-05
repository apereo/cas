<%@include file="includes/top.jsp"%>
	<form id="" method="post" action="#">
		<table cellspacing="0" class="large">
			<thead>
			<tr>
<!--			<th>Select</th> -->
				<th>Service Name</th>
				<th>ID</th>
				<th>Enabled</th>
				<th>Allowed to Proxy</th>
				<th>SSL Participant</th>
				<th>Attributes</th>
				<th colspan="2">Actions</th>
			</tr>
			</thead>
			<tbody>
			<c:forEach items="${services}" var="service">
			<tr id="${service.id}"${param.action eq 'delete' and param.id eq service.id ? ' class="highlightTop"' : ''}>
				<td>${service.name}</td>
				<td>${service.id}</td>
				<td>${service.enabled ? 'Yes' : 'No'}</td>
				<td>${service.allowedToProxy ? 'Yes' : 'No'}</td>
				<td>${service.ssoEnabled ? 'Yes' : 'No'}</td>
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
			   <form action="#" method="post">
			   Are you sure you want to delete this service? <input type="submit" name="confirm" value="Yes" /> <input type="submit" name="confirm" value="No" />
			   </form>
			    </td>
			    </tr>
			</c:if>
			</c:forEach>
			</tbody>
		</table>
	</form>
<%@include file="includes/bottom.jsp" %>