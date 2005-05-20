<%@ page session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" lang="en">
	<head>
		<!-- $Id$ -->
	
		<!-- DOCUMENT TITLE: CHANGE TO NEW TITLE -->
		<title>JA-SIG  Central  Authentication  Service (CAS)</title>
		<meta http-equiv="content-type" content="text/html; charset=iso-8859-1" />
	
		<!-- KEYWORDS AND DESCRIPTIONS GO INTO THIS SECTION -->
	
		<meta name="keywords" content="Central Authentication Service,JA-SIG,CAS" />
		<meta name="description" content="Error page reporting that the whole context could not load." />
		
	
		<!-- THIS CODE PROVIDES THE FORMATTING FOR THE TEXT - PLEASE LEAVE INTACT -->
		<!-- TODO: Revisit to make more robust.  Potentially, eliminate use of external .css and .js. -->
	</head>


  <body onload="init();">
	<!-- HEADER -->
	<div style="	margin:0; padding:0; width:100%; clear:both; background:#b00 top left repeat scroll;">
		<a style="display:block; margin:0; padding:0 0 0 3px; text-decoration:none; background-color:#b00; color:#ffc; font-size:1em; line-height:19px;" id="top">Java Architecture Special Interest Group</a>
		<h1>JA-SIG Central  Authentication  Service</h1>
	</div>
	<!-- END HEADER -->

	<!-- CONTENT -->
	<div style="border-top:1px solid #fff; margin:0 5%; padding:0; width:auto !important; width /**/:100%; clear:both;">

		<div style="	margin:30px 0 0 0; padding:10px; border:1px solid #eee; clear:both; width:auto !important;width /**/:100%; position: relative;">
			<h2 style="margin-bottom:0; position:relative; top:-20px; left:-20px; width:350px; margin:0; padding:0 0 1px 5px; border:1px solid #ccc; background-color:#eee; color:#066; font-weight:bold; font-size:1em; line-height:1.3em;">CAS is Unavailable</h2>

			<p style="margin-top:-.5em;border:1px solid #ccc;background-color:#ffc;color:#000;padding:5px;">
			  There was a fatal error initializing the CAS application context.  This is almost always because of an error in the Spring bean configuration files.
			  Are the files valid XML?  Do the beans they refer to all exist?<br /><br />
			  Before placing CAS in production, you should change this page to present a UI appropriate for the case where the CAS
			  web application is fundamentally broken.  Perhaps "Sorry, CAS is currently unavailable." with some links to your user support information.
			</p>
			
			<!-- Render a message about who logged what where -->
			
			<c:if test="${not empty applicationScope.exceptionCaughtByServlet and not empty applicationScope.exceptionCaughtByListener}">
			<p style="margin-top:-.5em;border:1px solid #ccc;background-color:#ffc;color:#000;padding:5px;">
			  The Throwables representing these fatal errors have been logged by the <em>SafeContextLoaderListener</em>
			  and by the <em>SafeDispatcherServlet</em> via Commons Logging, via ServletContext logging, and to System.err.
			</p>
			</c:if>
			
			<c:if test="${not empty applicationScope.exceptionCaughtByServlet and empty applicationScope.exceptionCaughtByListener}">
			<p style="margin-top:-.5em;border:1px solid #ccc;background-color:#ffc;color:#000;padding:5px;">
			  The Throwable representing the fatal error has been logged by the <em>SafeDispatcherServlet</em>
			  via Commons Logging, via ServletContext logging, and to System.err.
			</p>
			</c:if>
			
			<c:if test="${empty applicationScope.exceptionCaughtByServlet and not empty applicationScope.exceptionCaughtByListener}">
			<p style="margin-top:-.5em;border:1px solid #ccc;background-color:#ffc;color:#000;padding:5px;">
			  The Throwable representing the fatal error has been logged by the SafeContextLoaderListener
			  via Commons Logging, via ServletContext logging, and to System.err.
			</p>
			</c:if>
			
			<c:if test="${empty applicationScope.exceptionCaughtByServlet and empty applicationScope.exceptionCaughtByListener}">
			<p style="margin-top:-.5em;border:1px solid #ccc;background-color:#ffc;color:#000;padding:5px;">
			   A general exception occurred while trying to access CAS.  Please notify your system administrator.
			</p>
			</c:if>	
			
			<!-- Render information about the throwables themselves -->
			
			<c:if test="${not empty applicationScope.exceptionCaughtByListener}">
			<p style="margin-top:-.5em;border:1px solid #ccc;background-color:#FF99FF;color:#000;padding:5px;">
			  The Throwable encountered at context listener initialization was: <br/> <br/>
			  <c:out value="${applicationScope.exceptionCaughtByListener}"/>
			</p>
			</c:if>
			
			<c:if test="${not empty applicationScope.exceptionCaughtByServlet}">
			<p style="margin-top:-.5em;border:1px solid #ccc;background-color:#FF99FF;color:#000;padding:5px;">
			  The Throwable encountered at dispatcher servlet initialization was: <br/> <br/>
			  <c:out value="${applicationScope.exceptionCaughtByServlet}"/>
			</p>
			</c:if>
			
		</div>
	</div><!-- END CONTENT -->

	<!-- FOOTER -->
   	<div id="footer">
		<hr />
		<p style="margin-top:1em; margin:0; padding:0; font-size:1em; line-height:1.2em; color:#999;">
			Copyright &copy; 2005 JA-SIG.  All rights reserved.
		</p>
	</div>
	<!-- END FOOTER -->

</body>
</html>

