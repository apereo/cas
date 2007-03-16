<%@include file="includes/top.jsp"%>

	<c:if test="${not empty param.status}">
		<div id="msg" class="success"><spring:message code="services.manage.status.${param.status}" /></div>
	</c:if>

	<p><strong>Registry Status</strong>: ${currentRegistryStatus ? "Enabled" : "Disabled"} [${currentRegistryStatus ? '<a href="disableRegistryService.html">Disable</a>' : '<a href="enableRegistryService.html">Enable</a>'}]</p>
	
      <table cellspacing="0" id="headerTable" class="headerTable">
			<tr>
				<th class="th1">Service Name</th>
				<th class="th2">Service Url</th>
				<th class="th3 ac">Enabled</th>
				<th class="th4 ac">Can Proxy</th>
				<th class="th5 ac">SSO</th>
				<th class="th6">&nbsp;</th>
				<th class="th7">&nbsp;</th>
			</tr>
		</table>
	
	
	<div id="tableWrapper" class="tableWrapper">
		<table cellspacing="0" id="scrollTable" class="scrollTable highlight large">
         <thead>
			<tr>
				<th>Service Name</th>
				<th>Service Url</th>
				<th>Enabled</th>
				<th>Can Proxy</th>
				<th>SSO</th>
				<th>&nbsp;</th>
				<th>&nbsp;</th>
			</tr>
			</thead>
			
			<tbody>
		<c:forEach items="${services}" var="service" varStatus="status">
		<tr id="row${status.index}"${param.id eq service.id ? ' class="added"' : ''}>
			<td id="${service.id}" class="td1">${service.name}</td>
			<td class="td2">${service.serviceId}</td>
			<td class="ac td3"><img src="../images/services/${service.enabled}.gif" alt="${service.enabled ? 'Enabled' : 'Disabled'}" /></td>
			<td class="ac td4"><img src="../images/services/${service.allowedToProxy}.gif" alt="${service.allowedToProxy ? 'Allowed to Proxy' : 'Not Allowed to Proxy'}" /></td>
			<td class="ac td5"><img src="../images/services/${service.ssoEnabled}.gif" alt="${service.ssoEnabled ? 'SSO Enabled' : 'SSO Disabled'}" /></td>

			<td class="td6" id="edit${status.index}"><a href="edit.html?id=${service.id}" class="edit">edit</a></td>
			<td class="td7" id="delete${status.index}"><a href="#" class="del" onclick="swapButtonsForConfirm('${status.index}','${service.id}'); return false;">delete</a></td>
		</tr>
		</c:forEach>
			</tbody>
		</table>
	</div>
<div class="add"><a href="add.html">add a service</a></div>	  
<%@include file="includes/bottom.jsp" %>