package org.apereo.cas.webauthn;

import org.apereo.cas.config.CasHibernateJpaConfiguration;
import org.apereo.cas.config.JpaWebAuthnConfiguration;
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
@TestPropertySource(properties = "cas.jdbc.show-sql=true")
@Tag("JDBC")
@Import({JpaWebAuthnConfiguration.class, CasHibernateJpaConfiguration.class})
public class JpaWebAuthnCredentialRepositoryTests extends BaseWebAuthnCredentialRepositoryTests {
}
