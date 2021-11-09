package org.apereo.cas.oidc.scopes;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultOidcAttributeReleasePolicyFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("OIDC")
public class DefaultOidcAttributeReleasePolicyFactoryTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("oidcAttributeReleasePolicyFactory")
    private OidcAttributeReleasePolicyFactory oidcAttributeReleasePolicyFactory;

    @Test
    public void verifyOperation() {
        assertNotNull(oidcAttributeReleasePolicyFactory.get(OidcConstants.StandardScopes.EMAIL));
        assertNotNull(oidcAttributeReleasePolicyFactory.get(OidcConstants.StandardScopes.ADDRESS));
        assertNotNull(oidcAttributeReleasePolicyFactory.get(OidcConstants.StandardScopes.PHONE));
        assertNotNull(oidcAttributeReleasePolicyFactory.get(OidcConstants.StandardScopes.ADDRESS));
        assertNotNull(oidcAttributeReleasePolicyFactory.get(OidcConstants.StandardScopes.OPENID));

        assertNull(oidcAttributeReleasePolicyFactory.get(OidcConstants.StandardScopes.OFFLINE_ACCESS));
    }
}
