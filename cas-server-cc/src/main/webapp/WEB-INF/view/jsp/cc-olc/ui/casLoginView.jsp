<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--

    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License. You may obtain a
    copy of the License at:

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on
    an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
    specific language governing permissions and limitations
    under the License.

--%>

<%@ page contentType="text/html; charset=UTF-8" %>
<jsp:directive.include file="includes/top.jsp" />
<!--  
<c:if test="${not pageContext.request.secure}">
<div class="errors">
<p>You are currently accessing CAS over a non-secure connection.  Single Sign on WILL NOT WORK.  In order to have single sign on work, you MUST log in over HTTPS.</p>
</div>
</c:if>
-->

<div class="bodyCopy">
<!-- From headerinfo.html:  END -->
<!-- ========================================== OLC Middle: Start  -->				
<!-- From contentarea120.html:  START -->
<!-- Top Controlling Table -->
				<a name="maincontent" id="maincontent"></a>
				<table width="100%" cellpadding="0" cellspacing="0" border="0" id="olc-Content" height="250">
				<tr>
<!-- From contentarea120.html:  END -->
				<td valign="top">
				<form:form method="post" id="fm1" cssClass="fm-v clearfix" commandName="${commandName}" htmlEscape="true">
                <form:errors path="*" cssClass="errors" id="status" element="div" />
				<p>				
				</p>
				<h1>Sign In</h1>
				<p>
				Please enter your User ID and Password to access members-only features.
				</p>
<!-- these are the possible error messages, which are customizable -->
<!--			<p>
				<font color="RED">Sorry, the information you provided is invalid. Please try again.<br>Note: Your User ID and password are case sensitive.</font>
				</p>
-->
<!--			<p>
				<font color="RED">A system error has occurred. Please try again.</font>
				</p>
-->
<!-- outlined table lines -->
				<table border=0 cellspacing=0 cellpadding=4 class="olc-formTable" width="325">
				<tr>
					<td colspan=2 class="olc-formTitle">Sign In</td>
				</tr>
				<tr>
					<td colspan=2 valign="top">Sign in to access members-only areas of the community.</td>
				</tr>

				<tr>
					<td><label for="User ID">User ID: </label></td>
					<td>
                    <c:if test="${not empty sessionScope.openIdLocalId}">
						<strong>${sessionScope.openIdLocalId}</strong>
						<input type="hidden" id="username" name="username" value="${sessionScope.openIdLocalId}" />
						</c:if>

						<c:if test="${empty sessionScope.openIdLocalId}">
						<spring:message code="screen.welcome.label.netid.accesskey" var="userNameAccessKey" />
						<form:input cssClass="required" cssErrorClass="error" id="username" size="25" tabindex="1" accesskey="${userNameAccessKey}" path="username" autocomplete="false" htmlEscape="true" />
						</c:if>
                    <!--REMOVE  <INPUT class="olc-text" NAME="username" TYPE=text VALUE="" SIZE=15 maxlength=20>--></td>
				</tr>
				<tr>
					<td><label for="Password">Password: </label></b></td>
					<td>
                    <spring:message code="screen.welcome.label.password.accesskey" var="passwordAccessKey" />
					<form:password cssClass="required" cssErrorClass="error" id="password" size="25" tabindex="2" path="password"  accesskey="${passwordAccessKey}" htmlEscape="true" autocomplete="off" />
                    <!--REMOVE  <INPUT class="olc-text" NAME="password" TYPE=password VALUE="" SIZE=15 maxlength=20>--></td>
				</tr>

				<tr>
					<td>&nbsp;</td>
					<td>
                    <input type="hidden" name="lt" value="${loginTicket}" />
					<input type="hidden" name="execution" value="${flowExecutionKey}" />
					<input type="hidden" name="_eventId" value="submit" />
                    <input class="btn-submit" name="submit" accesskey="l" value="<spring:message code="screen.welcome.button.login" />" tabindex="4" type="submit" />
                    <input class="btn-reset" name="reset" accesskey="c" value="<spring:message code="screen.welcome.button.clear" />" tabindex="5" type="reset" />
                    <!--<INPUT class="olc-formButton" TYPE="SUBMIT" VALUE=" Sign In ">--></td>
				</tr>
				<tr>
					<td>&nbsp;</td>
					<td><a href="http://www.alumniconnections.com/olc/pub/CTC/forgot/forgot.cgi">Forgot</a> your user ID or password? </td>

				</tr>

				</table>

				<INPUT TYPE="hidden" NAME="referer" VALUE="/olc/membersonly/CTC/old/directory.cgi?FNC=basicsearch"><INPUT TYPE="hidden" NAME="SaFormName" VALUE="SubmitLogin__Floginform_html">
				</form:form>

				<p>
				<strong>Not yet registered for the community?</strong><br>Take advantage of all the community has to offer! Click here to <a href="https://www.alumniconnections.com/olc/pub/CTC/register/register.cgi" target="_top">register now</a>!
				</p>

				<p>
				<em><strong>Please note:</strong> You must be using a "cookie enabled" browser in order to access the members-only areas. If you have disabled cookie use in your browser, you must enable it before entering your authentication info.</em>
				</p>

				</td>
				<!-- From contentarea121.html:  START -->
<!-- Top Controlling Table -->
		</tr>

		</table>
<!-- From contentarea121.html:  END -->

<!-- ========================================== OLC Middle: End   -->
<!-- From footerinfo.html:  START -->
					</div>




 
<jsp:directive.include file="includes/bottom.jsp" />
