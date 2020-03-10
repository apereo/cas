package org.apereo.cas.gauth.token;

import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link OracleGoogleAuthenticatorJpaTokenRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@TestPropertySource(properties = {
    "cas.authn.mfa.gauth.jpa.user=system",
    "cas.authn.mfa.gauth.jpa.password=Oradoc_db1",
    "cas.authn.mfa.gauth.jpa.driverClass=oracle.jdbc.driver.OracleDriver",
    "cas.authn.mfa.gauth.jpa.url=jdbc:oracle:thin:@localhost:1521:ORCLCDB",
    "cas.authn.mfa.gauth.jpa.dialect=org.hibernate.dialect.Oracle12cDialect"
})
@EnabledIfPortOpen(port = 1521)
@Tag("Oracle")
public class OracleGoogleAuthenticatorJpaTokenRepositoryTests extends GoogleAuthenticatorJpaTokenRepositoryTests {
}
