package org.apereo.cas.oidc.ticket;

import module java.base;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcPushedAuthorizationRequestExpirationPolicyBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("OIDC")
@TestPropertySource(properties = "cas.authn.oidc.par.max-time-to-live-in-seconds=PT45S")
class OidcPushedAuthorizationRequestExpirationPolicyBuilderTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("pushedAuthorizationUriExpirationPolicy")
    private ExpirationPolicyBuilder pushedAuthorizationUriExpirationPolicy;

    @Test
    void verifyOperation() {
        val expiration = Beans.newDuration(casProperties.getAuthn().getOidc().getPar().getMaxTimeToLiveInSeconds()).toSeconds();
        assertEquals(expiration, pushedAuthorizationUriExpirationPolicy.buildTicketExpirationPolicy().getTimeToLive());
    }
}
