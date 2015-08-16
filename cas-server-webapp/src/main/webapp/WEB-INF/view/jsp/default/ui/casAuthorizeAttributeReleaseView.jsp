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
<jsp:directive.include file="includes/top.jsp" />

<script type="text/javascript">
    function cancelAuthorize() {
        $("#eventId").val("cancel");
        $("#fm1").submit();
    }
</script>


<div id="login">
    <p>The following attributes will be released to <strong>${service}</strong></p>
    <table>
        <c:forEach var="attr"
                   items="${attributes}"
                   varStatus="loopStatus" begin="0"
                   end="${fn:length(attributes)}"
                   step="1">

            <tr>
                <td>${attr.key}</td>
            </tr>

            <c:forEach var="attrval" items="${attr.value}">
            <tr>
                <td>${fn:escapeXml(attrval)}</td>
            </tr>
            </c:forEach>

        </c:forEach>

        <c:forEach var="attribute"
                   items="${attributes}" varStatus="loopStatus" begin="0"
                   end="${fn:length(attributes)}" step="1">



        </c:forEach>
    </table>

    <form:form method="post" id="fm1" htmlEscape="true">
        <section class="row btn-row">
            <input type="hidden" name="execution" value="${flowExecutionKey}" />
            <input type="hidden" id="eventId" name="_eventId" value="accept" />

            <input class="btn-submit" type="submit"
                   value="<spring:message code="screen.aup.button.accept" />" >
            <input class="btn-reset" type="reset"
                   onclick="cancelAuthorize();"
                   value="<spring:message code="screen.aup.button.cancel" />" >
        </section>
    </form:form>

</div>
<jsp:directive.include file="includes/bottom.jsp" />