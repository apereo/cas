<jsp:directive.include file="includes/top.jsp" />

		<div id="welcome">
			<h2 style="margin-bottom:0;">Logout successful</h2>

			<p>	You have successfully logged out of the Central Authentication Service./p>
			<p>For security reasons, exit your web browser.</p>
			
			<%--
			 Implementation of support for the "url" parameter to logout as recommended in CAS spec section 2.3.1.
			 A service sending a user to CAS for logout can specify this parameter to suggest that we offer
			 the user a particular link out from the logout UI once logout is completed.  We do that here.
			--%>
			<c:if test="${not empty url}">
			<p>
				The service from which you arrived has supplied a 
				<a href="${url}">link you may follow by clicking here</a>.
			</p>
			</c:if>
		</div>
<jsp:directive.include file="includes/bottom.jsp" />