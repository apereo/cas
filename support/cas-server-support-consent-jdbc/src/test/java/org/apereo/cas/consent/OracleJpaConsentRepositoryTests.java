package org.apereo.cas.consent;

import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link OracleJpaConsentRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnabledIfPortOpen(port = 1521)
@Tag("Oracle")
@TestPropertySource(properties = {
    "cas.jdbc.show-sql=false",
    "cas.consent.jpa.ddl-auto=create-drop",
    "cas.consent.jpa.user=system",
    "cas.consent.jpa.password=Oradoc_db1",
    "cas.consent.jpa.driver-class=oracle.jdbc.driver.OracleDriver",
    "cas.consent.jpa.url=jdbc:oracle:thin:@localhost:1521:ORCLCDB",
    "cas.consent.jpa.dialect=org.hibernate.dialect.Oracle12cDialect"
})
public class OracleJpaConsentRepositoryTests extends JpaConsentRepositoryTests {
}
