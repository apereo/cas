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
<h1><spring:message code="${pageTitle}" /></h1>
<form:form action="${registeredService.id ge 0 ? 'edit.html' : 'add.html'}?id=${fn:escapeXml(param.id)}"
ssClass="v" cssStyle="width:75%;" modelAttribute="registeredService">

		<c:if test="${not empty successMessage}">
			<div id="msg" class="info">${successMessage}</div>
		</c:if>

		<spring:hasBindErrors name="${commandName}">
			<div id="msg" class="errors">
			<spring:message code="application.errors.global" />
		</div>
		</spring:hasBindErrors>
	<fieldset class="repeat"><legend><spring:message code="${pageTitle}" /></legend>
	<div class="fieldset-inner">
		<p class="instructions"><spring:message code="management.services.add.instructions" /></p>
		<span class="oneField" style="display:block; margin:5px 0;">
			<label for="name" class="preField"><spring:message code="management.services.add.property.name" /> </label>
			<form:input path="name" size="51" maxlength="50" cssClass="required" cssErrorClass="error" />
			<form:errors path="name" cssClass="formError" />
			<br />
		</span>
		
		<span class="oneField">
			<label for="serviceId" class="preField"><spring:message code="management.services.add.property.serviceUrl" /></label>
			<form:input path="serviceId" size="51" maxlength="255" cssClass="required" cssErrorClass="error" />
			<form:errors path="serviceId" cssClass="formError" />
			<br />
			<div class="hint"><spring:message code="management.services.add.property.serviceUrl.instructions" /></div>
		</span>

		
		<span class="oneField">
			<label for="description" class="preField"><spring:message code="management.services.add.property.description" /></label>
			<form:textarea path="description" cssClass="required" cssErrorClass="error" cols="49" rows="5" />
			<form:errors path="description" cssClass="formError" />
			<br />
		</span>
		
		<span class="oneField">
			<label for="theme" class="preField"><spring:message code="management.services.add.property.themeName" /></label>
			<form:input path="theme" size="11" maxlength="10" cssClass="required" cssErrorClass="error" />
			<form:errors path="theme" cssClass="formError" />
			<br />
		</span>
 
		<span class="oneField">
			<span class="label preField"><spring:message code="management.services.add.property.status" /></span>
			<span>
				<span class="oneChoice">
					<form:checkbox path="enabled" value="true" cssClass="check" />
					<label for="enabled1" id="enabled-l" class="postField"><spring:message code="management.services.add.property.status.enabled" /></label>
				</span>
				<span class="oneChoice">
					<form:checkbox path="ssoEnabled" value="true" cssClass="check" />
					<label for="ssoEnabled1" id="ssl-l" class="postField"><spring:message code="management.services.add.property.status.ssoParticipant" /></label>
				</span>
				
				<span class="oneChoice">
					<form:checkbox path="anonymousAccess" value="true" cssClass="check" />
					<label for="anonymousAccess1" id="anonymousAccess-l" class="postField"><spring:message code="management.services.add.property.status.anonymousAccess" /></label>
				</span>
			</span>
			<br/>
		</span>
	     
    	<span class="oneField"><label class="preField" style="float:left;"><spring:message code="management.services.manage.label.usernameAttribute" /></label>
    		<form:select path="usernameAttribute" items="${availableUsernameAttributes}" />
    		<form:errors path="usernameAttribute" cssClass="formError" />
    	</span>
      		            	    
	    <span class="oneField">
	      <label for="theme" class="preField"><spring:message code="management.services.add.property.evaluationOrder" /></label>
	      <form:input path="evaluationOrder" size="11" maxlength="10" cssClass="required" cssErrorClass="error" />
	      <form:errors path="evaluationOrder" cssClass="formError" />
	      <br />
	    </span>

	</div>
	</fieldset>
	<div class="actions">
		<button type="submit" class="primaryAction" id="submit-wf_FormGardenDemonst" value="<spring:message code="management.services.add.button.save" />">
		<spring:message code="management.services.add.button.save" /></button> 
		or <a href="manage.html" style="color:#b00;"><spring:message code="management.services.add.button.cancel" /></a>
	</div>
</form:form>
<%@include file="includes/bottom.jsp" %>