package org.apereo.cas.impl.token;

import org.apereo.cas.util.junit.EnabledIfPortOpen;

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
    "cas.authn.passwordless.tokens.jpa.url=jdbc:mysql://localhost:3306/mysql?allowPublicKeyRetrieval=true&characterEncoding=UTF-8&useSSL=FALSE",
    "cas.authn.passwordless.tokens.jpa.dialect=org.hibernate.dialect.MySQL57InnoDBDialect"
})
@EnabledIfPortOpen(port = 3306)
@Tag("MySQL")
public class MySQLJpaPasswordlessTokenRepositoryTests extends JpaPasswordlessTokenRepositoryTests {
}
