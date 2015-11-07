<%@ page session="false" contentType="application/xml; charset=UTF-8" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<cas:clearPassResponse xmlns:cas='http://www.yale.edu/tp/cas'>
    <cas:clearPassFailure>${fn:escapeXml(description)}</cas:clearPassFailure>
</cas:clearPassResponse>
