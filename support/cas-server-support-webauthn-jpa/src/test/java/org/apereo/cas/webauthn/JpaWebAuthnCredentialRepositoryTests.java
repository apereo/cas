package org.apereo.cas.webauthn;

import org.apereo.cas.config.CasHibernateJpaAutoConfiguration;
import org.apereo.cas.config.JpaWebAuthnAutoConfiguration;
import org.apereo.cas.webauthn.storage.BaseWebAuthnCredentialRepositoryTests;
import org.junit.jupiter.api.Tag;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link JpaWebAuthnCredentialRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@TestPropertySource(properties = {
    "cas.authn.mfa.web-authn.crypto.signing.key=xTjUNTiL1kybVd6j0D_vJlIuQ8_1wojpiEUd_daKTvlmQpCOmQ99RRimAXfBi0niX4Z_rduthbLXGnNaeUhqLw",
    "cas.authn.mfa.web-authn.crypto.encryption.key=8W_Z0NGvZd094MJAS-XfepaCtRXnrqTFrlp90GXG8Ok",
    "cas.jdbc.show-sql=false"
})
@Tag("JDBCMFA")
@Import({JpaWebAuthnAutoConfiguration.class, CasHibernateJpaAutoConfiguration.class})
class JpaWebAuthnCredentialRepositoryTests extends BaseWebAuthnCredentialRepositoryTests {
}
