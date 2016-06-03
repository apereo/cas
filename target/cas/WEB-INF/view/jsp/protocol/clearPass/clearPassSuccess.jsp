<%@ page session="false" contentType="application/xml; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<cas:clearPassResponse xmlns:cas='http://www.yale.edu/tp/cas'>
    <cas:clearPassSuccess>
        <cas:credentials>${fn:escapeXml(credentials)}</cas:credentials>
    </cas:clearPassSuccess>
</cas:clearPassResponse>
