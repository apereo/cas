package org.apereo.cas.support.saml.services;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlIdPTestUtils;
import org.apereo.cas.util.CollectionUtils;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * This is {@link GroovySamlRegisteredServiceAttributeReleasePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class GroovySamlRegisteredServiceAttributeReleasePolicyTests extends BaseSamlIdPConfigurationTests {

    @Test
    public void verifyScriptReleasesSamlAttributes() {
        final GroovySamlRegisteredServiceAttributeReleasePolicy filter = new GroovySamlRegisteredServiceAttributeReleasePolicy();
        filter.setGroovyScript("classpath:saml-groovy-attrs.groovy");
        filter.setAllowedAttributes(CollectionUtils.wrapList("uid", "givenName", "displayName"));
        final SamlRegisteredService registeredService = SamlIdPTestUtils.getSamlRegisteredService();
        registeredService.setAttributeReleasePolicy(filter);
        final Map attributes = filter.getAttributes(CoreAuthenticationTestUtils.getPrincipal(),
            CoreAuthenticationTestUtils.getService(), registeredService);
        assertFalse(attributes.isEmpty());
    }

}
