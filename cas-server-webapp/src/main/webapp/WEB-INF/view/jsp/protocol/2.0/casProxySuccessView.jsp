<%@ page session="false" contentType="application/xml; charset=UTF-8" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>
    <cas:proxySuccess>
        <cas:proxyTicket>${fn:escapeXml(ticket)}</cas:proxyTicket>
    </cas:proxySuccess>
</cas:serviceResponse>
