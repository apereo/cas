<%@ page session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<!-- $Id: casFailureView.jsp,v 1.1 2004/12/09 16:10:20 dkopylen Exp $ -->
	
		<!-- DOCUMENT TITLE: CHANGE TO NEW TITLE -->
		<title>JA-SIG  Central  Authentication  Service (CAS)</title>
		<meta http-equiv="content-type" content="text/html; charset=iso-8859-1" />
	
		<!-- KEYWORDS AND DESCRIPTIONS GO INTO THIS SECTION -->
	
		<meta name="keywords" content="Central Authentication Service,JA-SIG,CAS" />
	
		<!-- THIS CODE PROVIDES THE FORMATTING FOR THE TEXT - PLEASE LEAVE INTACT -->
		<link rel="stylesheet" href="css/acs.css" type="text/css" media="all" />
		<link rel="stylesheet" href="css/cas.css" type="text/css" media="all" />
		<script src="js/common.js" type="text/javascript"></script>
	</head>


  <body onload="init();">
	<!-- HEADER -->
	<div id="header">
		<a id="top">Java Architecture Special Interest Group</a>
		<h1>JA-SIG Central  Authentication  Service</h1>
	</div>
	<!-- END HEADER -->

	<!-- CONTENT -->
	<div id="content">

		<div class="dataset clear" style="position: relative;">
			<h2 style="margin-bottom:0;">Please Log In</h2>

			<p style="margin-top:-.5em;border:1px solid #ccc;background-color:#ffc;color:#000;padding:5px;">
			  Congratulations on bringing CAS online!  The default authentication handler authenticates where usernames equal passwords: go ahead, try it out. <br /><br />
			  For security reasons, please Log Out and Exit your web browser when you are done accessing services that require authentication!
			</p>

			<form method="post">
			<spring:bind path="credentials.*">
			  <c:forEach var="error" items="${status.errorMessages}">
			      <br /><c:out value="${error}"/>
			  </c:forEach>
			</spring:bind>
				<fieldset>
					<legend><strong>Enter your JA-SIG NetID and Password</strong></legend>
					<div style="margin-left:25%;">
						<div>

							<p>
								<label for="userName"><span class="accesskey">N</span>etID:</label><br />
								<input class="required" id="userName" name="userName" size="32" tabindex="1" accesskey="n" />
							</p>

							<p>
								<label for="password"><span class="accesskey">P</span>assword:</label><br />

								<input class="required" type="password" id="password" name="password" size="32" tabindex="2" accesskey="p" />
							</p>

							<p><input style="width:1.5em;border:0;padding:0;margin:0;" type="checkbox" id="warn" name="warn" value="true" tabindex="3" /> 
							   <label for="warn"  accesskey="w"><span class="accesskey">W</span>arn me before logging me into other sites.</label></p>

							<input type="hidden" name="ticket" value="${loginTicket}" />
							<input type="hidden" name="service" value="${casAttributes.service}" />


							<p><input type="submit" class="button" accesskey="l" value="LOGIN" tabindex="4" /></p>
						</div>

					</div>
				</fieldset>
			</form>
		</div>
	</div><!-- END CONTENT -->

	<!-- FOOTER -->
   	<div id="footer">
		<hr />
		<p style="margin-top:1em;">
			Copyright &copy; 2004 JA-SIG.  All rights reserved.
		</p>
	</div>
	<!-- END FOOTER -->

</body>
</html>

