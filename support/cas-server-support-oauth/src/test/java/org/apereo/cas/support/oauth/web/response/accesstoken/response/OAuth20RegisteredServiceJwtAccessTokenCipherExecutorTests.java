package org.apereo.cas.support.oauth.web.response.accesstoken.response;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.services.RegisteredServiceTestUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20RegisteredServiceJwtAccessTokenCipherExecutorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("OAuth")
class OAuth20RegisteredServiceJwtAccessTokenCipherExecutorTests extends AbstractOAuth20Tests {

    @Test
    void verifyOperation() {
        val cipher = new OAuth20RegisteredServiceJwtAccessTokenCipherExecutor();
        assertTrue(cipher.getSigningKey(RegisteredServiceTestUtils.getRegisteredService()).isEmpty());
        assertTrue(cipher.getEncryptionKey(RegisteredServiceTestUtils.getRegisteredService()).isEmpty());
    }
}
