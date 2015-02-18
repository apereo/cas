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
</div>
<!-- END CONTENT -->
<!-- FOOTER -->

<div id="footer" class="fl-panel fl-note fl-bevel-white fl-font-size-80">
  <a id="jasig" href="http://www.apereo.org" title="go to Apereo home page"></a>
  <div>
    <h4><spring:message code="footer.links" /></h4>
    <ul id="nav-campus-sites">
      <li><a href="http://www.apereo.org/cas" rel="_blank"><spring:message code="footer.homePage" /></a>,</li>
      <li><a href="http://wiki.jasig.org" rel="_blank"><spring:message code="footer.wiki" /></a>,</li>
      <li><a href="http://issues.jasig.org" rel="_blank"><spring:message code="footer.issueTracker" /></a>,</li>
      <li><a href="http://www.apereo.org/cas/mailing-lists" rel="_blank"><spring:message code="footer.mailingLists" /></a>.</li>
    </ul>
  </div>
  <div id="copyright">
    <p><spring:message code="footer.copyright" /></p>
    <p><spring:message code="footer.poweredBy" arguments="<%=org.jasig.cas.CasVersion.getVersion()%>" /></p>
  </div>
</div>
</body>
</html>
<!-- END FOOTER -->
