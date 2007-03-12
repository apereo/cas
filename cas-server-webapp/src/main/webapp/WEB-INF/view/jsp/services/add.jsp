<%@include file="includes/top.jsp"%>

<form:form action="${registeredService.id ge 0 ? 'edit.html' : 'add.html'}?id=${param.id}" cssClass="v" cssStyle="width:75%;" commandName="${commandName}">

		<c:if test="${not empty successMessage}">
			<div id="msg" class="info">${successMessage}</div>
		</c:if>

		<spring:hasBindErrors name="${commandName}">
			<div id="status" class="errors">
			Please correct the errors below:
		</div>
		</spring:hasBindErrors>
	<fieldset class="repeat"><legend>Add New Service</legend>
		<p class="instructions">Please make sure to commit your changes by clicking on the Save Changes button at the bottom of the page</p>
		<span class="oneField" style="display:block; margin:5px 0;">
			<label for="name" class="preField">Name </label>
			<form:input path="name" size="51" maxlength="50" cssClass="required" cssErrorClass="error" />
			<form:errors path="name" cssClass="formError" />
			<br />
		</span>
		
		<span class="oneField">
			<label for="serviceId" class="preField">Service Url</label>
			<form:input path="serviceId" size="51" maxlength="255" cssClass="required" cssErrorClass="error" />
			<form:errors path="serviceId" cssClass="formError" />
			<br />
			You can use Ant-style Pattern Matching
		</span>

		
		<span class="oneField">
			<label for="description" class="preField">Description</label>
			<form:textarea path="description" cssClass="required" cssErrorClass="error" cols="49" rows="5" />
			<form:errors path="description" cssClass="formError" />
			<br />
		</span>
		
		<span class="oneField">
			<label for="theme" class="preField">Theme Name</label>
			<form:input path="theme" size="11" maxlength="10" cssClass="required" cssErrorClass="error" />
			<form:errors path="theme" cssClass="formError" />
			<br />
		</span>

		<span class="oneField">
			<span class="label preField">Status</span>
			<span class="required">
				<span class="oneChoice">
					<form:checkbox path="enabled" value="true" cssClass="check" />
					<label for="enabled" id="enabled-l" class="postField">Enabled</label>
				</span>
				<span class="oneChoice">
					<form:checkbox path="allowedToProxy" value="true" cssClass="check" />
					<label for="allowedToProxy" id="proxy-l" class="postField">Allowed to proxy</label>
				</span>
				<span class="oneChoice">
					<form:checkbox path="ssoEnabled" value="true" cssClass="check" />
					<label for="ssoEnabled" id="ssl-l" class="postField">SSO Participant</label>
				</span>
			</span>
			<br/>
		</span>
			
		<span class="oneField"><label class="preField ieFix" style="float:left;">Attributes</label>
			<form:select path="allowedAttributes" items="${availableAttributes}" multiple="true" />
		</span>
	</fieldset>
	<div class="actions">
		<button type="submit" class="primaryAction" id="submit-wf_FormGardenDemonst" value="Save Changes">Save Changes</button> or <a href="manage.html" style="color:#b00;">Cancel</a>
	</div>
</form:form>
<%@include file="includes/bottom.jsp" %>