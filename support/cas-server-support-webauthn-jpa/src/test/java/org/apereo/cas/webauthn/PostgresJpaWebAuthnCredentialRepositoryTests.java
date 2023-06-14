package org.apereo.cas.webauthn;

import org.apereo.cas.util.junit.EnabledIfListeningOnPort;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link PostgresJpaWebAuthnCredentialRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@TestPropertySource(properties = {
    "cas.authn.mfa.web-authn.jpa.user=postgres",
    "cas.authn.mfa.web-authn.jpa.password=password",
    "cas.authn.mfa.web-authn.jpa.driver-class=org.postgresql.Driver",
    "cas.authn.mfa.web-authn.jpa.url=jdbc:postgresql://localhost:5432/mfa",
    "cas.authn.mfa.web-authn.jpa.dialect=org.hibernate.dialect.PostgreSQLDialect"
})
@EnabledIfListeningOnPort(port = 5432)
@Tag("Postgres")
class PostgresJpaWebAuthnCredentialRepositoryTests extends JpaWebAuthnCredentialRepositoryTests {
}
