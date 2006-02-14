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
			<p><spring:message code="screen.welcome.welcome" /></p>
			<p><spring:message code="screen.welcome.security" /></p>

			<div style="margin-left: auto; margin-right: auto; left: 50%; width: 300px;">
				<p><strong><spring:message code="screen.welcome.instructions" /></strong></p>
				<p>
					<label for="username"><spring:message code="screen.welcome.label.netid" /></label><br />
					<input class="required" id="username" name="username" size="32" tabindex="1" accesskey="<spring:message code="screen.welcome.label.netid.accesskey" />" />
				</p>

				<p>
					<label for="password"><spring:message code="screen.welcome.label.password" /></label><br />

				<%--
				NOTE: Certain browsers will offer the option of caching passwords for a user.  There is a non-standard attribute,
				"autocomplete" that when set to "off" will tell certain browsers not to prompt to cache credentials.  For more
				information, see the following web page:
				http://www.geocities.com/technofundo/tech/web/ie_autocomplete.html
				--%>
					<input class="required" type="password" id="password" name="password" size="32" tabindex="2" accesskey="<spring:message code="screen.welcome.label.password.accesskey" />" />
				</p>

				<p><input style="width:1.5em;border:0;padding:0;margin:0;" type="checkbox" id="warn" name="warn" value="true" tabindex="3" /> 
				   <label for="warn"  accesskey="<spring:message code="screen.welcome.label.warn.accesskey" />"><spring:message code="screen.welcome.label.warn" /></label></p>

				<input type="hidden" name="lt" value="${flowExecutionId}" />
				<input type="hidden" name="_currentStateId" value="${currentStateId}" />
				<input type="hidden" name="_eventId" value="submit" />

				<p style="text-align: center;"><input type="submit" class="button" accesskey="l" value="<spring:message code="screen.welcome.button.login" />" tabindex="4" />
				   <input type="reset" class="button" accesskey="c" value="<spring:message code="screen.welcome.button.clear" />" tabindex="5" /></p>
				<p style="text-align: center;">
					Languages:
					<c:set var="query" value="" />
					<c:forEach var="item" items="${pageContext.request.parameterNames}" varStatus="status">
						<c:if test="${item != 'locale' and item != 'password' and item != 'username' and item != 'lt' and item != '_currentStateId' and item != '_eventId'}">
							<c:set var="query" value="${query}${item}=${param[item]}" />
							<c:if test="${not status.last}">
								<c:set var="query" value="${query}&amp;" />
							</c:if>
						</c:if>
					</c:forEach>
					<a href="login?${query}${not empty query ? '&' : ''}locale=en">English</a> |
					<a href="login?${query}${not empty query ? '&' : ''}locale=fr">French</a> |
					<a href="login?${query}${not empty query ? '&' : ''}locale=nl">Dutch</a>
				</p>
			</div>
		</div>
	</form>
<jsp:directive.include file="includes/bottom.jsp" />