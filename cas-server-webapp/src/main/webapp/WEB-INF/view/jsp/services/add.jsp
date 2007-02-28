<%@ page language="java"  session="false" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>CAS Admin Edit Form</title>
	<link href="../css/services/cas.css" type="text/css" rel="stylesheet" media="all" />
	<!--[if lte IE 6]><link href="../css/services/ieFix.css" type="text/css" rel="stylesheet" media="all" /><![endif]-->
</head>
<body>
<h3><span>Edit Registered Service</span></h3>

<div id="navcontainer">
	<p>sidebar</p>
	<ul id="navlist"
		><li><a href="http://www.acs.rutgers.edu" title="link 1 title">Link1</a></li
		><li><span>Registered Services</span
			><ul
				><li><a href="">Edit</a></li
				><li><span>Something</span></li
			></ul
		></li
		><li><a href="http://www.acs.rutgers.edu" title="link 1 title">Edit Services</a></li
		><li><a href="http://www.acs.rutgers.edu" title="link 1 title">Link1</a></li
		><li><a href="http://www.acs.rutgers.edu" title="link 1 title">Link1</a></li
	></ul>
</div>

<form:form action="add.html" cssClass="v" cssStyle="width:75%;" commandName="registeredService">
	<fieldset class="repeat"><legend>Add New Service</legend>
		<p class="instructions">Please make sure to commit your changes by clicking on the Save Changes button at the bottom of the page</p>
		<span class="oneField" style="display:block; margin:5px 0;">
			<label for="name" class="preField">Name </label>
			<form:input path="name" size="55" cssClass="required" cssErrorClass="error" />
			<form:errors path="name" />
			<br />
		</span>
		
		<span class="oneField">
			<label for="id" class="preField">Id</label>
			<form:input path="id" size="55" cssClass="required" cssErrorClass="error" />
			<form:errors path="id" />
			<br />
		</span>
		
		<span class="oneField">
			<label for="description" class="preField">Description</label>
			<form:input path="description" size="55" cssClass="required" cssErrorClass="error" />
			<form:errors path="description" />
			<br />
		</span>
		
		<span class="oneField">
			<label for="theme" class="preField">Theme Name</label>
			<form:input path="theme" size="55" cssClass="required" cssErrorClass="error" />
			<form:errors path="theme" />
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
		<fieldset style="background:#ffc; margin-left:.5em;">
			<form:select path="allowedAttributes" items="${availableAttributes}" multiple="true" />
		</fieldset>
		</span>
	</fieldset>
	<div class="actions">
		<button type="submit" class="primaryAction" id="submit-wf_FormGardenDemonst" value="Save Changes">Save Changes</button> or <a href="" style="color:#b00;">Cancel</a>
	</div>
</form:form>

</body>
</html>
