<%--

    Licensed to Apereo under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Apereo licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License.  You may obtain a
    copy of the License at the following location:

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.

--%>
<%@include file="includes/top.jsp"%>
<script type="text/javascript" src="<c:url value="/js/MyInfusion.js" />"></script>
<h1><spring:message code="${pageTitle}" /></h1>
<c:if test="${fn:length(services) eq 0}">
       <div id="msg" class="errors"><p><spring:message code="management.services.service.warn" arguments="${defaultServiceUrl}" /></p></div>
</c:if>

<c:if test="${not empty param.status}">
	<div id="msg" class="success"><spring:message code="management.services.status.${param.status}" arguments="${param.serviceName}" /></div>
</c:if>

<div class="errors" id="errorsDiv">
	<spring:message code="management.services.status.evaluationOrder.notupdated" />
</div>

<table id="headerTable" class="headerTable">
	<tr>
		<th class="th1"><spring:message code="management.services.manage.label.name" /></th>
		<th class="th2"><spring:message code="management.services.manage.label.serviceUrl" /></th>
		<th class="th4 ac"></th>
  		<th class="th8 ac"><spring:message code="management.services.manage.label.evaluationOrder" /></th>
   		<th class="th9">&nbsp;</th>
		<th class="th10">&nbsp;</th> 
	</tr>
</table>

<div id="tableWrapper" class="tableWrapper">
	<table id="scrollTable" class="scrollTable highlight large">
		<tbody>
	       <c:forEach items="${services}" var="service" varStatus="status">
	       <tr id="row${status.index}"${param.id eq service.id ? ' class="added"' : ''}>
	         <td id="${service.id}" class="td1">${service.name}</td>
	         <td class="td2">${fn:length(service.serviceId) < 100 ? service.serviceId : fn:substring(service.serviceId, 0, 100)}</td>
	         <td class="ac td4"></td>
	         <td class="ac td8">${service.evaluationOrder}</td>
	         <td class="td9" id="edit${status.index}"><a href="edit.html?id=${service.id}" class="edit"><spring:message code="management.services.manage.action.edit" /></a></td> 
	         <td class="td10" id="delete${status.index}"><a href="#" class="del" onclick="swapButtonsForConfirm('${status.index}','${service.id}'); return false;"><spring:message code="management.services.manage.action.delete" /></a></td>
	       </tr>
	       </c:forEach>
		</tbody>
	</table>
</div>
<div class="add"><a href="add.html"><span style="text-transform: lowercase;"><spring:message code="addServiceView" /></span></a></div>	  
<%@include file="includes/bottom.jsp" %>