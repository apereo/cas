<%@include file="includes/top.jsp"%>

	<c:if test="${not empty param.status}">
		<div id="msg" class="success"><spring:message code="services.manage.status.${param.status}" /></div>
	</c:if>

	<p><strong>Registry Status</strong>: ${currentRegistryStatus ? "Enabled" : "Disabled"} [${currentRegistryStatus ? '<a href="disableRegistryService.html">Disable</a>' : '<a href="enableRegistryService.html">Enable</a>'}]</p>
	
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
		<c:forEach items="${services}" var="service" varStatus="status">
		<tr id="row${status.index}"${param.action eq 'add' and param.id eq service.id ? ' class="added" id="added"' : ''}>
			<td id="${service.id}">${service.name}</td>
			<td>${service.serviceId}</td>
			<td class="ac"><img src="../images/services/${service.enabled}.gif" /></td>
			<td class="ac"><img src="../images/services/${service.allowedToProxy}.gif" /></td>
			<td class="ac"><img src="../images/services/${service.ssoEnabled}.gif" /></td>

			<c:if test="${service.id != -1}">
			<td id="edit${status.index}"><a href="edit.html?id=${service.id}" class="edit">edit</a></td>
			<td id="delete${status.index}"><a href="#" class="del" onclick="swapButtonsForConfirm('${status.index}');">delete</a></td>
			<td colspan="2" class="confirm" id="confirm${status.index}">Are you sure? <a id="yes" href="deleteRegisteredService.html?id=${service.id}">Yes</a> <a id="no" href="#" onclick="swapConfirmForButtons('${status.index}');">No</a></td>
			</c:if>
			
			<c:if test="${service.id == -1}">
			<td>&nbsp;</td>
			<td>&nbsp;</td>
			</c:if>
		</tr>
		</c:forEach>
		<tr>
			<td colspan="7">
			<a href="add.html" class="add">Add another?</a>
			</td>
		</tr>
		</tbody>
	</table>
<%@include file="includes/bottom.jsp" %>