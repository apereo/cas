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

    "cas.authn.mfa.trusted.jpa.user=system",
    "cas.authn.mfa.trusted.jpa.password=Oradoc_db1",
    "cas.authn.mfa.trusted.jpa.driverClass=oracle.jdbc.driver.OracleDriver",
    "cas.authn.mfa.trusted.jpa.url=jdbc:oracle:thin:@localhost:1521:ORCLCDB",
    "cas.authn.mfa.trusted.jpa.dialect=org.hibernate.dialect.Oracle12cDialect"
})
@EnabledIfPortOpen(port = 1521)
@Tag("Oracle")
public class OracleJpaMultifactorAuthenticationTrustStorageTests extends JpaMultifactorAuthenticationTrustStorageTests {
}
