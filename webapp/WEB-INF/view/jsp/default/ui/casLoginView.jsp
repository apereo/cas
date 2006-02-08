<jsp:directive.include file="includes/top.jsp" />


	<%--
	NOTE: By default our example JSP page purposefully leaves out the url in the action attribute of a FORM tag.
	This is to allow the page to POST to itself, retaining all the request paramteters that were initially presented
	on the GET request.
	
	If you are going to provide a value for the action attribute, be sure to also place the request parameters in 
	the action attribute.  Otherwise, CAS may exhibit unexpected behavior. E.x. if you forget to pass in the
	service paramter, CAS will do a generic login rather than a redirect to the service.
	
	You can easily access request parameters via the Request object.
	--%>
	<form method="post" action="">
	
		<spring:hasBindErrors name="credentials">
			<div id="errors">
				<ul>
		  <c:forEach var="error" items="${errors.allErrors}">
		      <li><spring:message code="${error.code}" text="${error.defaultMessage}" /></li>
		  </c:forEach>
		  </ul>
		  </div>
		</spring:hasBindErrors>
	
		<div id="welcome">
			<p>Congratulations on bringing CAS online!  The default authentication handler authenticates where usernames equal passwords: go ahead, try it out. </p>
			<p>For security reasons, please Log Out and Exit your web browser when you are done accessing services that require authentication!</p>

			<div style="margin-left: auto; margin-right: auto; left: 50%; width: 300px;">
				<p><strong>Enter your JA-SIG NetID and Password.</strong></p>
				<p>
					<label for="username"><span class="accesskey">N</span>etID:</label><br />
					<input class="required" id="username" name="username" size="32" tabindex="1" accesskey="n" />
				</p>

				<p>
					<label for="password"><span class="accesskey">P</span>assword:</label><br />

				<%--
				NOTE: Certain browsers will offer the option of caching passwords for a user.  There is a non-standard attribute,
				"autocomplete" that when set to "off" will tell certain browsers not to prompt to cache credentials.  For more
				information, see the following web page:
				http://www.geocities.com/technofundo/tech/web/ie_autocomplete.html
				--%>
					<input class="required" type="password" id="password" name="password" size="32" tabindex="2" accesskey="p" />
				</p>

				<p><input style="width:1.5em;border:0;padding:0;margin:0;" type="checkbox" id="warn" name="warn" value="true" tabindex="3" /> 
				   <label for="warn"  accesskey="w"><span class="accesskey">W</span>arn me before logging me into other sites.</label></p>

				<input type="hidden" name="lt" value="${flowExecutionId}" />
				<input type="hidden" name="_currentStateId" value="${currentStateId}" />
				<input type="hidden" name="_eventId" value="submit" />

				<p style="text-align: center;"><input type="submit" class="button" accesskey="l" value="LOGIN" tabindex="4" />
				   <input type="reset" class="button" accesskey="c" value="CLEAR" tabindex="5" /></p>
			</div>
		</div>
	</form>
<jsp:directive.include file="includes/bottom.jsp" />