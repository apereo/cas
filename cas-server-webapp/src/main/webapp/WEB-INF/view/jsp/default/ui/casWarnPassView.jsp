<jsp:directive.include file="includes/top.jsp" />
<c:set var="ticketArg"  value="${serviceTicketId}" scope="page"/>
<c:if test="${fn:length(ticketArg) > 0}">
  <c:set var="ticketArg"  value="ticket=${serviceTicketId}"/>
</c:if>

<div class="errors">
  <p>
    <c:if test="${expireDays == 0}">
      <h2><spring:message code="screen.warnpass.heading.today" /></h2>
    </c:if>
    <c:if test="${expireDays == 1}">
      <h2><spring:message code="screen.warnpass.heading.tomorrow" /></h2>
    </c:if>
    <c:if test="${expireDays > 1}">
      <h2><spring:message code="screen.warnpass.heading.other" arguments="${expireDays}" /></h2>
    </c:if>
  </p>

  <p>
  <spring:message code="screen.warnpass.message.line1" arguments="${passwordPolicyUrl}"  />
  </p>
  <p>
  <spring:message code="screen.warnpass.message.line2" arguments="${fn:escapeXml(param.service)}${fn:indexOf(param.service, '?') eq -1 ? '?' : '&'}${ticketArg}" />
  </p>
</div>
<script type="text/javascript">
<!--

  function redirectTo(URL) {
    window.location = URL ;
  }
  setTimeout("redirectTo('${param.service}${fn:indexOf(param.service, '?') eq -1 ? '?' : '&'}${ticketArg}')", 10000);

</script>
