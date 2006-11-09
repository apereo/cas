<%@ page session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
	<head>
		<title>JA-SIG Central Authentication Service (CAS) Services Management</title>
		<meta http-equiv="Content-Type"	content="application/xhtml+xml; charset=UTF-8" />
		<meta name="keywords" content="Central Authentication Service,JA-SIG,CAS" />
		<link rel="stylesheet" href="css/home.css" type="text/css" media="all" />
		<link rel="stylesheet" href="css/jasig.css" type="text/css" media="all" />
		<script src="js/common.js" type="text/javascript"></script>
	</head>
	<body>
		<h1>JA-SIG Central Authentication Registered Services Management</h1>
		<form
			name="deleteServices"
			action="deleteServices.html"
			method="post">
			
			<table>
				<thead>
					<tr>
						<th>Delete?</th>
						<th>ID</th>
						<th>Enabled?</th>
						<th>Proxy?</th>
						<th>SSO-Enabled</th>
					</tr>
				</thead>
				<tbody>
					<for:each var="service" items="services">
					<tr>
						<td><input type="checkbox" value="${service.id}" name="id" /></td>
						<td>${service.id}</td>
						<td>${service.enabled}</td>
						<td>${service.allowedToProxy}</td>
						<td>${service.ssoParticipant}</td>
					</tr>
					</for:each>
				</tbody>
			</table>
			<input type="submit" value="Delete" />
		</form>
	</body>
</html>