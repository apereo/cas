package org.apereo.cas.support.oauth.services;

import org.apereo.cas.util.CollectionUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultRegisteredServiceOAuthTokenExchangePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("OAuth")
public class DefaultRegisteredServiceOAuthTokenExchangePolicyTests {
    @Test
    void verifyOperation() throws Exception {
        val policy = new DefaultRegisteredServiceOAuthTokenExchangePolicy()
            .setAllowedAudience(CollectionUtils.wrapSet(".*"))
            .setAllowedTokenTypes(Set.of(".*access-token.*"));
        assertTrue(policy.isTokenExchangeAllowed(new OAuthRegisteredService(), Set.of("resource"),
            Set.of("audience"), "access-token"));
    }
}
