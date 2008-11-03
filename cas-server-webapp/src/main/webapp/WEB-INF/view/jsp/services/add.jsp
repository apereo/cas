<%@include file="includes/top.jsp"%>

<form:form action="${registeredService.id ge 0 ? 'edit.html' : 'add.html'}?id=${param.id}" cssClass="v" cssStyle="width:75%;" commandName="${commandName}">

		<c:if test="${not empty successMessage}">
			<div id="msg" class="info">${successMessage}</div>
		</c:if>

		<spring:hasBindErrors name="${commandName}">
			<div id="status" class="errors">
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
					<form:checkbox path="allowedToProxy" value="true" cssClass="check" />
					<label for="allowedToProxy1" id="proxy-l" class="postField"><spring:message code="management.services.add.property.status.allowedToProxy" /></label>
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
			
		<span class="oneField"><label class="preField ieFix" style="float:left;"><spring:message code="management.services.add.property.attributes" /></label>
			<form:select path="allowedAttributes" items="${availableAttributes}" multiple="true" />
		</span>
		
				<span class="oneChoice">
					<form:checkbox path="ignoreAttributes" value="true" cssClass="check" />
					<label for="ignoreAttributes1" id="ignoreAttributes-l" class="postField"><spring:message code="management.services.add.property.ignoreAttributes" /></label>
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
		<button type="submit" class="primaryAction" id="submit-wf_FormGardenDemonst" value="Save Changes"><spring:message code="management.services.add.button.save" /></button> or <a href="manage.html" style="color:#b00;"><spring:message code="management.services.add.button.cancel" /></a>
	</div>
</form:form>
<%@include file="includes/bottom.jsp" %>