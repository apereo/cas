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
<div id="login" style="width: 100%;">
    <form:form method="post" id="fm1" htmlEscape="true">

        <h2>Acceptable Usage Policy</h2>
        <div>
            The purpose of this policy is to establish acceptable and unacceptable use of electronic devices and network resources in conjunction with the established culture of ethical and lawful behavior, openness, trust, and integrity.

            <p>
                By using these resources, you agree to abide by the Acceptable Usage Policy.
            </p>

            <p>Click '<spring:message code="screen.aup.button.accept" />' to continue. Otherwise, click '<spring:message code="screen.aup.button.cancel" />'.</p>
        </div>

        <section class="row btn-row">
            <input type="hidden" name="execution" value="${flowExecutionKey}" />
            <input type="hidden" name="_eventId" value="submit" />
            <input class="btn-submit" name="submit" accesskey="s" value="<spring:message code="screen.aup.button.accept" />"  type="submit" />
            <input class="btn-reset" name="cancel" accesskey="c"
                   value="<spring:message code="screen.aup.button.cancel" />" type="button"
                   onclick="location.href = location.href;" />
        </section>
    </form:form>
</div>
<jsp:directive.include file="includes/bottom.jsp" />
