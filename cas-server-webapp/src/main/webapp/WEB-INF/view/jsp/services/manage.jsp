<%@include file="includes/top.jsp"%>

	<c:if test="${not empty param.status}">
		<div id="msg" class="success"><spring:message code="management.services.status.${param.status}" arguments="${param.serviceName}" /></div>
	</c:if>

      <table cellspacing="0" id="headerTable" class="headerTable">
			<tr>
				<th class="th1"><spring:message code="management.services.manage.label.name" /></th>
				<th class="th2"><spring:message code="management.services.manage.label.serviceUrl" /></th>
				<th class="th3 ac"><spring:message code="management.services.manage.label.enabled" /></th>
				<th class="th4 ac"><spring:message code="management.services.manage.label.allowedToProxy" /></th>
				<th class="th5 ac"><spring:message code="management.services.manage.label.ssoParticipant" /></th>
				<th class="th6">&nbsp;</th>
				<th class="th7">&nbsp;</th>
			</tr>
		</table>
	
	
	<div id="tableWrapper" class="tableWrapper">
		<table cellspacing="0" id="scrollTable" class="scrollTable highlight large">
         <thead>
			<tr>
				<th><spring:message code="management.services.manage.label.name" /></th>
				<th><spring:message code="management.services.manage.label.serviceUrl" /></th>
				<th><spring:message code="management.services.manage.label.enabled" /></th>
				<th><spring:message code="management.services.manage.label.allowedToProxy" /></th>
				<th><spring:message code="management.services.manage.label.ssoParticipant" /></th>
				<th>&nbsp;</th>
				<th>&nbsp;</th>
			</tr>
			</thead>
			
			<tbody>
		<c:forEach items="${services}" var="service" varStatus="status">
		<tr id="row${status.index}"${param.id eq service.id ? ' class="added"' : ''}>
			<td id="${service.id}" class="td1">${service.name}</td>
			<td class="td2">${fn:length(service.serviceId) < 50 ? service.serviceId : fn:substring(service.serviceId, 0, 50)}</td>
			<td class="ac td3"><img src="../images/services/${service.enabled}.gif" alt="${service.enabled ? 'Enabled' : 'Disabled'}" /></td>
			<td class="ac td4"><img src="../images/services/${service.allowedToProxy}.gif" alt="${service.allowedToProxy ? 'Allowed to Proxy' : 'Not Allowed to Proxy'}" /></td>
			<td class="ac td5"><img src="../images/services/${service.ssoEnabled}.gif" alt="${service.ssoEnabled ? 'SSO Enabled' : 'SSO Disabled'}" /></td>

			<td class="td6" id="edit${status.index}"><a href="edit.html?id=${service.id}" class="edit"><spring:message code="management.services.manage.action.edit" /></a></td>
			<td class="td7" id="delete${status.index}"><a href="#" class="del" onclick="swapButtonsForConfirm('${status.index}','${service.id}'); return false;"><spring:message code="management.services.manage.action.delete" /></a></td>
		</tr>
		</c:forEach>
			</tbody>
		</table>
	</div>
<div class="add"><a href="add.html"><span style="text-transform: lowercase;"><spring:message code="addServiceView" /></span></a></div>	  
<%@include file="includes/bottom.jsp" %>