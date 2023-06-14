package org.apereo.cas.oidc.token;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcIdTokenExpirationPolicyBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("ExpirationPolicy")
@EnableConfigurationProperties(CasConfigurationProperties.class)
class OidcIdTokenExpirationPolicyBuilderTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("oidcIdTokenExpirationPolicy")
    private ExpirationPolicyBuilder oidcIdTokenExpirationPolicy;

    @Test
    void verifyTicketType() {
        assertNotNull(oidcIdTokenExpirationPolicy.buildTicketExpirationPolicy());
    }
}
