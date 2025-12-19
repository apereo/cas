package org.apereo.cas.gauth.token;

import module java.base;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link MariaDbGoogleAuthenticatorJpaTokenRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@TestPropertySource(properties = {
    "cas.authn.mfa.gauth.jpa.ddl-auto=create",
    "cas.authn.mfa.gauth.jpa.user=root",
    "cas.authn.mfa.gauth.jpa.password=mypass",
    "cas.authn.mfa.gauth.jpa.driver-class=org.mariadb.jdbc.Driver",
    "cas.authn.mfa.gauth.jpa.url=jdbc:mariadb://localhost:3306/mysql?allowPublicKeyRetrieval=true&characterEncoding=UTF-8&useSSL=FALSE",
    "cas.authn.mfa.gauth.jpa.dialect=org.hibernate.dialect.MariaDBDialect"
})
@EnabledIfListeningOnPort(port = 3306)
@Tag("MariaDb")
class MariaDbGoogleAuthenticatorJpaTokenRepositoryTests extends GoogleAuthenticatorJpaTokenRepositoryTests {
}
