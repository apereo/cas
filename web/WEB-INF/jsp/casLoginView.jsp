<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<!-- $Id: casFailureView.jsp,v 1.1 2004/12/09 16:10:20 dkopylen Exp $ -->
	
		<!-- DOCUMENT TITLE: CHANGE TO NEW TITLE -->
		<title>Rutgers  Central  Authentication  Service (CAS)</title>
		<meta http-equiv="content-type" content="text/html; charset=iso-8859-1" />
	
		<!-- KEYWORDS AND DESCRIPTIONS GO INTO THIS SECTION -->
	
		<meta name="keywords" content="Central Authentication Service,Rutgers University,CAS" />
	
		<!-- THIS CODE PROVIDES THE FORMATTING FOR THE TEXT - PLEASE LEAVE INTACT -->
		<link rel="stylesheet" href="css/acs.css" type="text/css" media="all" />
		<link rel="stylesheet" href="css/cas.css" type="text/css" media="all" />
		<script src="js/common.js" type="text/javascript"></script>
	</head>


  <body onload="init();">
	<!-- HEADER -->

	<div id="header">
		<a id="top">Rutgers, The State University of New Jersey</a>
		<h1>Rutgers  Central  Authentication  Service</h1>

	</div>
	<!-- END HEADER -->

	<!-- CONTENT -->
	<!-- $Id: casFailureView.jsp,v 1.1 2004/12/09 16:10:20 dkopylen Exp $ -->
	<div id="content">

		<div class="dataset clear" style="position: relative;">
			<h2 style="margin-bottom:0;">Please Log In</h2>

			<p style="margin-top:-.5em;border:1px solid #ccc;background-color:#ffc;color:#000;padding:5px;">
			
               
                     You may establish Rutgers authentication now in order to access protected services later.
               
              This is not a public network and explicit authorization is required.
			  For security reasons, please Log Out and Exit your web browser when you are done accessing services that require authentication!
			</p>

			<form method="post">
			<spring:bind path="authenticationRequest.*">
			  <c:forEach var="error" items="${status.errorMessages}">
			      <br /><c:out value="${error}"/>
			  </c:forEach>
			</spring:bind>
				<fieldset>
					<legend><strong>Enter your Rutgers NetID and Password</strong></legend>
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
			
			<p style="margin-top:-.5em;border:1px solid #ccc;background-color:#ffc;color:#000;padding:5px;">
				<strong>NetID:</strong>
				Most RUCS applications are now using the Rutgers NetID for authentication. 
				If you have a NetID and do not remember what it is you may look it up using the 
				<a href="https://www.acs.rutgers.edu:8889/netid/index.jsp">NetID Lookup</a> application.
				If you do not yet have a NetID you may 
				<a href="http://rucs.rutgers.edu/services/account/quick.html">Create a RUCS Account and NetID</a>.
			</p>

			
		</div>
	</div><!-- END CONTENT -->

	<!-- FOOTER -->
   	<div id="footer">
		<hr />
		<img src="images/logo_gray.gif" width="142" height="44" alt="" title="" style="float:right; margin-top: 10px;" />
		<p style="margin-top:1em;">

			Links to campus web sites: 
			  <a href="http://camden-www.rutgers.edu/" target="_blank">Camden</a>, 
			  <a href="http://rutgers-newark.rutgers.edu/" target="_blank">Newark</a>, 
			  <a href="http://nbp.rutgers.edu/" target="_blank">New Brunswick/Piscataway</a>, 
			  <a href="http://www.rutgers.edu" target="_blank">Rutgers University</a>.
			<br />For assistance, contact the Help Desks in: 
			  <a href="http://rucs.camden.rutgers.edu/" target="_blank">Camden</a>, 
			  <a href="http://www.ncs.rutgers.edu/helpdesk/" target="_blank">Newark</a>, 
			  or <a href="http://www.nbcs.rutgers.edu/helpdesk/index.php3" target="_blank">New Brunswick/Piscataway</a>.
		</p>

	</div>
	<!-- END FOOTER -->

</body>
</html>

