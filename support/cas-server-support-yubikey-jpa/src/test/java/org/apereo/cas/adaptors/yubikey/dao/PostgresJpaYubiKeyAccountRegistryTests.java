package org.apereo.cas.adaptors.yubikey.dao;

import module java.base;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link PostgresJpaYubiKeyAccountRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@EnabledIfListeningOnPort(port = 5432)
@Tag("Postgres")
@TestPropertySource(properties = {
    "cas.jdbc.show-sql=false",
    "cas.authn.mfa.yubikey.jpa.user=postgres",
    "cas.authn.mfa.yubikey.jpa.password=password",
    "cas.authn.mfa.yubikey.jpa.driver-class=org.postgresql.Driver",
    "cas.authn.mfa.yubikey.jpa.url=jdbc:postgresql://localhost:5432/yubikey",
    "cas.authn.mfa.yubikey.jpa.dialect=org.hibernate.dialect.PostgreSQLDialect"
})
class PostgresJpaYubiKeyAccountRegistryTests extends JpaYubiKeyAccountRegistryTests {
}
