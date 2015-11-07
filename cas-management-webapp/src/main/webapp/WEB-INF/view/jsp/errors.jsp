<%@include file="includes/header.noauth.jsp" %>

<%@ page isErrorPage="true" %>
<div class="col-sm-12">
    <div id="msg" class="errors">
        <h2><spring:message code="screen.unavailable.heading"/></h2>
        <p><spring:message code="screen.unavailable.message"/></p>
    </div>
</div>

<%@include file="includes/footer.jsp" %>
