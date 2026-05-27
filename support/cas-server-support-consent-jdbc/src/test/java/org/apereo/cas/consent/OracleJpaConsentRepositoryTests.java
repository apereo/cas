package org.apereo.cas.consent;

import module java.base;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link OracleJpaConsentRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnabledIfListeningOnPort(port = 1521)
@Tag("Oracle")
@TestPropertySource(properties = {
    "cas.jdbc.show-sql=false",
    "cas.consent.jpa.ddl-auto=create-drop",
    "cas.consent.jpa.user=cas",
    "cas.consent.jpa.password=cas",
    "cas.consent.jpa.driver-class=oracle.jdbc.driver.OracleDriver",
    "cas.consent.jpa.url=jdbc:oracle:thin:@//localhost:1521/FREEPDB1",
    "cas.consent.jpa.dialect=org.hibernate.dialect.OracleDialect"
})
class OracleJpaConsentRepositoryTests extends JpaConsentRepositoryTests {
}
