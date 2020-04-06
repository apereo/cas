package org.apereo.cas.trusted.authentication.storage;

import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * Test cases for {@link JpaMultifactorAuthenticationTrustStorage}.
 *
 * @author Daniel Frett
 * @since 5.3.0
 */
@TestPropertySource(properties = {
    "cas.jdbc.showSql=true",
    "cas.authn.mfa.trusted.jpa.ddlAuto=create-drop",
    "cas.authn.mfa.trusted.jpa.user=postgres",
    "cas.authn.mfa.trusted.jpa.password=password",
    "cas.authn.mfa.trusted.jpa.driverClass=org.postgresql.Driver",
    "cas.authn.mfa.trusted.jpa.url=jdbc:postgresql://localhost:5432/mfa",
    "cas.authn.mfa.trusted.jpa.dialect=org.hibernate.dialect.PostgreSQL95Dialect"
})
@EnabledIfPortOpen(port = 5432)
@Tag("Postgres")
public class PostgresJpaMultifactorAuthenticationTrustStorageTests extends JpaMultifactorAuthenticationTrustStorageTests {
}
