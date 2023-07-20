package org.apereo.cas.gauth.credential;

import org.apereo.cas.util.junit.EnabledIfListeningOnPort;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * Test cases for {@link PostgresJpaGoogleAuthenticatorTokenCredentialRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@TestPropertySource(properties = {
    "cas.authn.mfa.gauth.jpa.ddl-auto=create-drop",
    "cas.authn.mfa.gauth.jpa.user=postgres",
    "cas.authn.mfa.gauth.jpa.password=password",
    "cas.authn.mfa.gauth.jpa.driver-class=org.postgresql.Driver",
    "cas.authn.mfa.gauth.jpa.url=jdbc:postgresql://localhost:5432/mfa",
    "cas.authn.mfa.gauth.jpa.dialect=org.hibernate.dialect.PostgreSQLDialect"
})
@EnabledIfListeningOnPort(port = 5432)
@Tag("Postgres")
class PostgresJpaGoogleAuthenticatorTokenCredentialRepositoryTests extends JpaGoogleAuthenticatorTokenCredentialRepositoryTests {
}
