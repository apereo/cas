package org.apereo.cas.support.oauth.web.endpoints;

import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;

import lombok.val;
import org.junit.jupiter.api.Tag;

import java.util.HashSet;
import java.util.Set;

/**
 * This class tests the {@link OAuth20AccessTokenEndpointController} class.
 * <p>
 * It does almost the same as {@link OAuth20AccessTokenControllerTests}, but the registered
 * services always contain empty allowed grant types. These tests are run to ensure that
 * the change that adds proper support for supportedGrantTypes does not break existing CAS
 * setups that does not specify allowedGrantTypes. Briefly, it checks that empty supportedGrantTypes
 * is equivalent to supportedGrantTypes with all valid values.
 *
 * @author Kirill Gagarski
 * @since 5.3.3
 */
@Tag("OAuth")
public class OAuth20AccessTokenControllerNoGrantTypeTests extends OAuth20AccessTokenControllerTests {
    @Override
    protected OAuthRegisteredService getRegisteredService(final String serviceId, final String secret,
                                                          final Set<OAuth20GrantTypes> grantTypes) {
        val service = super.getRegisteredService(serviceId, secret, grantTypes);
        service.setSupportedGrantTypes(new HashSet<>());
        return service;
    }

    /**
     * This test should never fail in this suite, so just doing nothing.
     */
    @Override
    public void verifyClientDisallowedGrantType() {
    }
}
