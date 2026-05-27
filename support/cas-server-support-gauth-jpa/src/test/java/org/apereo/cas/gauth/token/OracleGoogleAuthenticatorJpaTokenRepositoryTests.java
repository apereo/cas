package org.apereo.cas.gauth.token;

import module java.base;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link OracleGoogleAuthenticatorJpaTokenRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@TestPropertySource(properties = {
    "cas.authn.mfa.gauth.jpa.user=cas",
    "cas.authn.mfa.gauth.jpa.password=cas",
    "cas.authn.mfa.gauth.jpa.driver-class=oracle.jdbc.driver.OracleDriver",
    "cas.authn.mfa.gauth.jpa.url=jdbc:oracle:thin:@//localhost:1521/FREEPDB1",
    "cas.authn.mfa.gauth.jpa.dialect=org.hibernate.dialect.OracleDialect"
})
@EnabledIfListeningOnPort(port = 1521)
@Tag("Oracle")
class OracleGoogleAuthenticatorJpaTokenRepositoryTests extends GoogleAuthenticatorJpaTokenRepositoryTests {
}
