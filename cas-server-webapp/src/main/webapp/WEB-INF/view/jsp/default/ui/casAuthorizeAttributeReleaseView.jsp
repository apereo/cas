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