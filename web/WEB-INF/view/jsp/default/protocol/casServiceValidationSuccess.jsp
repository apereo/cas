<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<cas:serviceResponse xmlns:cas="http://www.yale.edu/tp/cas">
<cas:authenticationSuccess>
<cas:user>${principal.id}</cas:user>
<c:if test="${empty pgtUrl}">
	<cas:proxyGrantingTicket>${pgtUrl}</cas:proxyGrantingTicket>
</c:if>
<cas:proxies>
<c:forEach var="proxy" items="${proxies}" varStatus="loopStatus">
	<cas:proxy>${proxy}</cas:proxy>
</c:forEach>
</cas:proxies>
</cas:authenticationSuccess>
</cas:serviceResponse>