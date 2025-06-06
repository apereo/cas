package org.apereo.cas.oidc.web.flow;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowLoginContextProvider;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcCasWebflowLoginContextProviderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("OIDCWeb")
@TestPropertySource(properties = "cas.http-client.allow-local-urls=true")
class OidcCasWebflowLoginContextProviderTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("oidcCasWebflowLoginContextProvider")
    private CasWebflowLoginContextProvider oidcCasWebflowLoginContextProvider;

    @Test
    void verifyOperation() throws Throwable {
        val context = MockRequestContext.create();
        assertTrue(oidcCasWebflowLoginContextProvider.getCandidateUsername(context).isEmpty());
        context.setParameter(CasProtocolConstants.PARAMETER_SERVICE, "https://localhost/cas?service=https://example.net&login_hint=casuser");
        assertTrue(oidcCasWebflowLoginContextProvider.getCandidateUsername(context).isPresent());
    }
}
