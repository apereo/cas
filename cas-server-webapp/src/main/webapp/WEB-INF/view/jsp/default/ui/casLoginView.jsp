<jsp:directive.include file="includes/top.jsp" />
	<form method="post" action="<%=response.encodeRedirectURL("login" + (request.getQueryString() != null && request.getQueryString().length() > 0 ? "?" + request.getQueryString() : ""))%>">
	
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

				<input type="hidden" name="lt" value="${flowExecutionKey}" />
				<input type="hidden" name="_eventId" value="submit" />

				<p style="text-align: center;"><input type="submit" class="button" accesskey="l" value="<spring:message code="screen.welcome.button.login" />" tabindex="4" />
				   <input type="reset" class="button" accesskey="c" value="<spring:message code="screen.welcome.button.clear" />" tabindex="5" /></p>
			</div>
				<p style="text-align: center;">
					Languages:
				
				<c:set var="query" value="<%=request.getQueryString() == null ? "" : request.getQueryString().replaceAll("&locale=[A-Za-z][A-Za-z]|^locale=[A-Za-z][A-Za-z]", "")%>" />
					
				<a href="login?${query}${not empty query ? '&' : ''}locale=en">English</a> |
				<a href="login?${query}${not empty query ? '&' : ''}locale=es">Español</a> |					
				<a href="login?${query}${not empty query ? '&' : ''}locale=fr">Français</a> |
				<a href="login?${query}${not empty query ? '&' : ''}locale=ru">Russian</a> |
				<a href="login?${query}${not empty query ? '&' : ''}locale=nl">Nederlands</a> |
				<a href="login?${query}${not empty query ? '&' : ''}locale=sv">Svenskt</a> |
				<a href="login?${query}${not empty query ? '&' : ''}locale=it">Italiano</a> |
				<a href="login?${query}${not empty query ? '&' : ''}locale=ur">Urdu</a> |
				<a href="login?${query}${not empty query ? '&' : ''}locale=zh_CN">Simplified Chinese</a>
			</p>
		</div>
	</form>
<jsp:directive.include file="includes/bottom.jsp" />