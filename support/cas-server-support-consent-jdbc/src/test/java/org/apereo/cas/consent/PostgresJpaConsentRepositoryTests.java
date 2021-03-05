package org.apereo.cas.consent;

import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link PostgresJpaConsentRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnabledIfPortOpen(port = 5432)
@Tag("Postgres")
@TestPropertySource(properties = {
    "cas.jdbc.show-sql=true",
    "cas.consent.jpa.ddl-auto=create-drop",

    "cas.consent.jpa.user=postgres",
    "cas.consent.jpa.password=password",
    "cas.consent.jpa.driver-class=org.postgresql.Driver",
    "cas.consent.jpa.url=jdbc:postgresql://localhost:5432/audit",
    "cas.consent.jpa.dialect=org.hibernate.dialect.PostgreSQL95Dialect"
})
public class PostgresJpaConsentRepositoryTests extends JpaConsentRepositoryTests {
}
