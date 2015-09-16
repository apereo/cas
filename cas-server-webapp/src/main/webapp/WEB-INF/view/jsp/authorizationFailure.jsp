<%--

    Licensed to Apereo under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Apereo licenses this file to you under the Apache License,
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
<jsp:directive.include file="/WEB-INF/view/jsp/default/ui/includes/top.jsp" />

<%@ page isErrorPage="true" %>
<%@ page import="org.jasig.cas.web.support.WebUtils"%>

<div id="msg" class="errors">
    <h2>${pageContext.errorData.statusCode} - <spring:message code="screen.blocked.header" /></h2>

    <%
        Object casAcessDeniedKey = request.getAttribute(WebUtils.CAS_ACCESS_DENIED_REASON);
        request.setAttribute("casAcessDeniedKey", casAcessDeniedKey);

    %>

    <c:choose>
        <c:when test="${not empty casAcessDeniedKey}">
            <p><spring:message code="${casAcessDeniedKey}" /></p>
        </c:when>
    </c:choose>
    <p><%=request.getAttribute("javax.servlet.error.message")%></p>
    <p><spring:message code="AbstractAccessDecisionManager.accessDenied"/></p>
</div>
<jsp:directive.include file="/WEB-INF/view/jsp/default/ui/includes/bottom.jsp" />
