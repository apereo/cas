package org.apereo.cas.impl.token;

import module java.base;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link MySQLJpaPasswordlessTokenRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@TestPropertySource(properties = {
    "cas.authn.passwordless.tokens.jpa.user=root",
    "cas.authn.passwordless.tokens.jpa.password=password",
    "cas.authn.passwordless.tokens.jpa.driver-class=com.mysql.cj.jdbc.Driver",
    "cas.authn.passwordless.tokens.jpa.url=jdbc:mysql://localhost:3306/cas?allowPublicKeyRetrieval=true&characterEncoding=UTF-8&useSSL=FALSE",
    "cas.authn.passwordless.tokens.jpa.dialect=org.hibernate.dialect.MySQLDialect"
})
@EnabledIfListeningOnPort(port = 3306)
@Tag("MySQL")
class MySQLJpaPasswordlessTokenRepositoryTests extends JpaPasswordlessTokenRepositoryTests {
}
