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