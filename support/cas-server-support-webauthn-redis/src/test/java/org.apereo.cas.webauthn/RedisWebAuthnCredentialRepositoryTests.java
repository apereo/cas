package org.apereo.cas.webauthn;

import org.apereo.cas.config.CasRedisWebAuthnAutoConfiguration;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.webauthn.storage.BaseWebAuthnCredentialRepositoryTests;
import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link RedisWebAuthnCredentialRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@TestPropertySource(
    properties = {
        "cas.authn.mfa.web-authn.redis.host=localhost",
        "cas.authn.mfa.web-authn.redis.port=6379"
    })
@Tag("Redis")
@Getter
@EnabledIfListeningOnPort(port = 6379)
@Import(CasRedisWebAuthnAutoConfiguration.class)
class RedisWebAuthnCredentialRepositoryTests extends BaseWebAuthnCredentialRepositoryTests {
}
