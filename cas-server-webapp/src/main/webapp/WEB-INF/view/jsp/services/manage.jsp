<%@include file="includes/top.jsp"%>

	<c:if test="${not empty param.status}">
		<div id="msg" class="success"><spring:message code="services.manage.status.${param.status}" /></div>
	</c:if>

	<p><strong>Registry Status</strong>: ${currentRegistryStatus ? "Enabled" : "Disabled"} [${currentRegistryStatus ? '<a href="manage.html?action=enable&confirm=false">Disable</a>' : '<a href="manage.html?action=enable&confirm=true">Enable</a>'}]</p>
	
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
		<tr${param.action eq 'delete' and param.id eq service.id ? ' class="highlightTop"' : ''}${param.action eq 'add' and param.id eq service.id ? ' class="added" id="added"' : ''}>
			<td id="${service.id}">${service.name}</td>
			<td>${service.serviceId}</td>
			<td class="ac"><img src="../images/services/${service.enabled}.gif" /></td>
			<td class="ac"><img src="../images/services/${service.allowedToProxy}.gif" /></td>
			<td class="ac"><img src="../images/services/${service.ssoEnabled}.gif" /></td>

			<c:if test="${service.id != -1}">
			<td><a href="edit.html?id=${service.id}" class="edit">edit</a></td>
			<td><a href="manage.html?id=${service.id}&amp;action=delete#${service.id}" class="del">delete</a></td>
			</c:if>
			
			<c:if test="${service.id == -1}">
			<td>&nbsp;</td>
			<td>&nbsp;</td>
			</c:if>
			
			
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
			<a href="add.html" class="add">Add another?</a>
			</td>
		</tr>
		</c:if>
		</c:forEach>
		</tbody>
	</table>
<%@include file="includes/bottom.jsp" %>