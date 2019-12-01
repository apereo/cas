package org.apereo.cas;

import org.apereo.cas.support.saml.web.idp.audit.SamlRequestAuditResourceResolverTests;
import org.apereo.cas.support.saml.web.idp.audit.SamlResponseAuditPrincipalIdProviderTests;
import org.apereo.cas.support.saml.web.idp.audit.SamlResponseAuditResourceResolverTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    SamlRequestAuditResourceResolverTests.class,
    SamlResponseAuditPrincipalIdProviderTests.class,
    SamlResponseAuditResourceResolverTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
