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

<footer id="casmgmt-footer">
    <div class="row">
        <div class="col-sm-10">
            <span><spring:message code="footer.links" /></span>
            <ul class="nav-campus-sites list-inline">
                <li><a href="http://www.apereo.org/cas" rel="_blank"><spring:message code="footer.homePage" /></a>,</li>
                <li><a href="http://jasig.github.io/cas" rel="_blank"><spring:message code="footer.wiki" /></a>,</li>
                <li><a href="https://github.com/Jasig/cas/issues" rel="_blank"><spring:message code="footer.issueTracker" /></a>,</li>
                <li><a href="http://jasig.github.io/cas/Mailing-Lists.html" rel="_blank"><spring:message code="footer.mailingLists"
                    /></a>.</li>
            </ul>
            <div class="copyright">
                <p>
                    <spring:message code="footer.copyright" /><br/>
                    <spring:message code="footer.poweredBy" arguments="<%=org.jasig.cas.CasVersion.getVersion()%>" />
                </p>
            </div>
        </div>
        <div class="col-sm-2">
            <div class="jasig-link">
                <a href="http://www.apereo.org" _target="blank" title="Apereo CAS Home Page"><img src="<c:url value="/images/logo_apereo.png"/>" /></a>
            </div>
        </div>
    </div>
</footer><%-- end .casmgmt-footer footer --%>

    </body>
</html>