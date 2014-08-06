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
<%@ page session="false" contentType="application/xml; charset=UTF-8" %>
<%@ page import="java.util.*, java.util.Map.Entry" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>
    <cas:authenticationSuccess>
        <cas:user>${fn:escapeXml(assertion.primaryAuthentication.principal.id)}</cas:user>
        <c:if test="${not empty pgtIou}">
            <cas:proxyGrantingTicket>${pgtIou}</cas:proxyGrantingTicket>
        </c:if>
        <c:if test="${fn:length(assertion.chainedAuthentications) > 1}">
            <cas:proxies>
                <c:forEach var="proxy" items="${assertion.chainedAuthentications}" varStatus="loopStatus" begin="0"
                           end="${fn:length(assertion.chainedAuthentications)-2}" step="1">
                    <cas:proxy>${fn:escapeXml(proxy.principal.id)}</cas:proxy>
                </c:forEach>
            </cas:proxies>
        </c:if>

        <c:if test="${fn:length(assertion.primaryAuthentication.principal.attributes) > 0}">
            <cas:attributes>
                <c:forEach var="attr"
                           items="${assertion.primaryAuthentication.principal.attributes}"
                           varStatus="loopStatus" begin="0"
                           end="${fn:length(assertion.primaryAuthentication.principal.attributes)}"
                           step="1">
                   	<%-- ${attr.value['class'].simpleName} fails for List: use scriptlet instead --%>
                   	<%
                   	    Entry entry = (Entry) pageContext.getAttribute("attr");
                   	    Object value = entry.getValue();
                   	    pageContext.setAttribute("isMultiple", value instanceof Collection || value instanceof Map || value instanceof Object[]
                                                                || value instanceof Iterator || value instanceof Enumeration);
                   	%>
                    <c:choose>
                        <%-- it's a single element, output its toString() --%>
                        <c:when test="${!isMultiple}">
                            <cas:${fn:escapeXml(attr.key)}>${fn:escapeXml(attr.value)}</cas:${fn:escapeXml(attr.key)}>
                        </c:when>
                        <%-- if attribute is multi-valued, list each value under the same attribute name --%>
                        <c:otherwise>
                            <c:forEach var="attrval" items="${attr.value}">
                                <cas:${fn:escapeXml(attr.key)}>${fn:escapeXml(attrval)}</cas:${fn:escapeXml(attr.key)}>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </c:forEach>
            </cas:attributes>
        </c:if>

    </cas:authenticationSuccess>
</cas:serviceResponse>
