package org.apereo.cas.support.oauth.web.response.accesstoken;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20TokenGeneratedResultTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("OAuth")
public class OAuth20TokenGeneratedResultTests extends AbstractOAuth20Tests {
    @Test
    public void verifyOperation() {
        val results = OAuth20TokenGeneratedResult.builder().accessToken(getAccessToken())
            .grantType(OAuth20GrantTypes.AUTHORIZATION_CODE)
            .registeredService(getRegisteredService(UUID.randomUUID().toString(), "secret"))
            .details(Map.of())
            .build();
        assertTrue(results.getAccessToken().isPresent());
        assertTrue(results.getGrantType().isPresent());
        assertTrue(results.getRegisteredService().isPresent());
        assertTrue(results.getDetails().isEmpty());
        assertNotNull(results.toString());
    }
}
