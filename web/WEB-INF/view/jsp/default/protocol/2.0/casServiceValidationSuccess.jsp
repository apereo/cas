<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<cas:serviceResponse xmlns:cas="http://www.yale.edu/tp/cas">
<cas:authenticationSuccess>
<cas:user>${assertion.chainedPrincipals[0].id}</cas:user>
<c:if test="${not empty pgtIou}">
	<cas:proxyGrantingTicket>${pgtIou}</cas:proxyGrantingTicket>
</c:if>
<c:if test="${fn:length(assertion.chainedPrincipals) > 1}">
<cas:proxies>
<c:forEach var="proxy" items="${assertion.chainedPrincipals}" varStatus="loopStatus" begin="1">
	<cas:proxy>${proxy.id}</cas:proxy>
</c:forEach>
</cas:proxies>
</c:if>
</cas:authenticationSuccess>
</cas:serviceResponse>