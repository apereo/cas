package org.apereo.cas.gauth.token;

import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link MySQLGoogleAuthenticatorJpaTokenRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@TestPropertySource(properties = {
    "cas.authn.mfa.gauth.jpa.user=root",
    "cas.authn.mfa.gauth.jpa.password=password",
    "cas.authn.mfa.gauth.jpa.driver-class=com.mysql.cj.jdbc.Driver",
    "cas.authn.mfa.gauth.jpa.url=jdbc:mysql://localhost:3306/mysql?allowPublicKeyRetrieval=true&characterEncoding=UTF-8&useSSL=FALSE",
    "cas.authn.mfa.gauth.jpa.dialect=org.hibernate.dialect.MySQLDialect"
})
@EnabledIfListeningOnPort(port = 3306)
@Tag("MySQL")
class MySQLGoogleAuthenticatorJpaTokenRepositoryTests extends GoogleAuthenticatorJpaTokenRepositoryTests {
}
