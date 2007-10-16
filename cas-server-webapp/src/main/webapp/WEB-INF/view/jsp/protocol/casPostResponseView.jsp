<%@ page language="java"  session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
	<body onload="document.acsForm.submit();">
		<form name="acsForm" action="${originalUrl}" method="post">
            <div style="display: none">
            <c:forEach items="${parameters}" var="entry">
	            <textarea rows=10 cols=80 name="${entry.key}"><c:out value="${entry.value}" /></textarea>
			</c:forEach>
            </div>
          </form>
	</body>
</html>