<%--

    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License.  You may obtain a
    copy of the License at the following location:

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.

--%>
<jsp:directive.include file="includes/top.jsp" />
<c:set var="ticketArg"  value="${serviceTicketId}" scope="page"/>
<c:if test="${fn:length(ticketArg) > 0}">
  <c:set var="ticketArg"  value="ticket=${serviceTicketId}"/>
</c:if>

<div id="msg" class="errors">
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
  <spring:message code="screen.warnpass.message.line2" arguments="${fn:escapeXml(service)}${fn:indexOf(service, '?') eq -1 ? '?' : '&'}${ticketArg}" />
  </p>
</div>
<script type="text/javascript">
  function redirectTo(URL) {
    window.location = URL ;
  }
  var timeout = ${redirectTimeout};
  setTimeout("redirectTo('${service}${fn:indexOf(service, '?') eq -1 ? '?' : '&'}${ticketArg}')", timeout);
</script>
