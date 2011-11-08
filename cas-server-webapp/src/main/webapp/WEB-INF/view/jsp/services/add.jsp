<%@include file="includes/top.jsp"%>

<form:form 
    action="${registeredService.id ge 0 ? 'edit.html' : 'add.html'}?id=${fn:escapeXml(param.id)}" 
    cssClass="v"
    commandName="${commandName}">

    <c:if test="${not empty successMessage}">
        <div id="msg" class="info">${successMessage}</div>
    </c:if>

    <!-- Errors -->
    <spring:hasBindErrors name="${commandName}">
    	<div id="msg" class="errors">
    	    <spring:message code="application.errors.global" />
        </div>
    </spring:hasBindErrors>
    
    <!-- General -->
	<fieldset>
	    <h2><spring:message code="management.services.fieldset.title.general" /></h2>
		<div class="oneField">
			<label for="name" class="preField"><spring:message code="management.services.add.property.name" /> </label>
			<form:input path="name" size="51" maxlength="50" cssClass="required" cssErrorClass="error" />
			<form:errors path="name" cssClass="formError" />
		</div>
		<div class="oneField">
			<label for="serviceId" class="preField"><spring:message code="management.services.add.property.serviceUrl" /></label>
			<form:input path="serviceId" size="51" maxlength="255" cssClass="required" cssErrorClass="error" />
			<form:errors path="serviceId" cssClass="formError" />
			<div class="hint"><spring:message code="management.services.add.property.serviceUrl.instructions" /></div>
		</div>
		<div class="oneField textarea">
			<label for="description" class="preField"><spring:message code="management.services.add.property.description" /></label>
			<form:textarea path="description" cssClass="required" cssErrorClass="error" cols="49" rows="5" />
			<form:errors path="description" cssClass="formError" />
		</div>
		<div class="oneField">
		    <label for="theme" class="preField">
		        <spring:message code="management.services.add.property.themeName" />
		    </label>
            <form:input path="theme" size="11" maxlength="10" cssErrorClass="error" />
            <form:errors path="theme" cssClass="formError" />
		</div>
    </fieldset>

    <!-- Status -->
    <fieldset>
        <h2><spring:message code="management.services.fieldset.title.status" /></h2>
		<div class="oneField">
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
		</div>
	</fieldset>
	
    <!-- Attributes -->
	<fieldset>
	    <h2><spring:message code="management.services.fieldset.title.attributes" /></h2>
		<div class="oneField">
		    <p><spring:message code="management.services.add.property.attributes" /></p>
			<form:select path="allowedAttributes" items="${availableAttributes}" multiple="true" />
		</div>
		<div class="oneField">
			<form:checkbox path="ignoreAttributes" value="true" cssClass="check" />
			<label for="ignoreAttributes1" id="ignoreAttributes-l" class="postField"><spring:message code="management.services.add.property.ignoreAttributes" /></label>
		</div>
	</fieldset>
	
    <!-- Advanced -->
	<fieldset>
	    <h2><spring:message code="management.services.fieldset.title.advanced" /></h2>
	    <div class="oneField">
            <label for="theme" class="preField"><spring:message code="management.services.add.property.evaluationOrder" /></label>
            <form:input path="evaluationOrder" size="11" maxlength="10" cssClass="required" cssErrorClass="error" />
            <form:errors path="evaluationOrder" cssClass="formError" />
        </div>
	</fieldset>
	
    <!-- Submit -->
	<div class="actions">
        <button type="submit" class="primaryAction" id="submit-wf_FormGardenDemonst" value="Save Changes">
            <spring:message code="management.services.add.button.save" />
        </button> 
        <a href="manage.html"><spring:message code="management.services.add.button.cancel" /></a>
	</div>
</form:form>

<%@include file="includes/bottom.jsp" %>
