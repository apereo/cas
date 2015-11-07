<jsp:directive.include file="../../default/ui/includes/top.jsp"/>
<div id="msg" class="question">
    <h2><spring:message code="screen.oauth.confirm.header"/></h2>

    <p>
        <spring:message code="screen.oauth.confirm.message" arguments="${serviceName}"/>
    </p>

    <p>
        <a id="allow" name="allow" href="${callbackUrl}"><spring:message code="screen.oauth.confirm.allow"/></a>
    </p>
</div>
<jsp:directive.include file="../../default/ui/includes/bottom.jsp"/>
