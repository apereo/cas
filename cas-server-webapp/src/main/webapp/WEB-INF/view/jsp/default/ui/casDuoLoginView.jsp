<jsp:directive.include file="includes/top.jsp"/>
<script src="<c:url value='js/duo/Duo-Web-v2.min.js'/>"></script>
<form:form method="post" id="duo_form" cssClass="fm-v clearfix" commandName="${commandName}" htmlEscape="true">
    <input type="hidden" name="execution" value="${flowExecutionKey}"/>
    <input type="hidden" name="_eventId" value="submit"/>

    <div class="box fl-panel" id="login">
        <iframe id="duo_iframe"
                width="620"
                height="330"
                frameborder="0"
                data-host="${apiHost}"
                data-sig-request="${sigRequest}"
                data-post-argument="signedDuoResponse">
        </iframe>
    </div>
</form:form>
<jsp:directive.include file="includes/bottom.jsp"/>
