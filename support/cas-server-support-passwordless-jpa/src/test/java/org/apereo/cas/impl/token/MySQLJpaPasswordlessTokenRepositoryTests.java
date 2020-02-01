package org.apereo.cas.impl.token;

import org.apereo.cas.util.junit.EnabledIfContinuousIntegration;
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
    "cas.authn.passwordless.tokens.jpa.jdbc.user=root",
    "cas.authn.passwordless.tokens.jpa.jdbc.password=password",
    "cas.authn.passwordless.tokens.jpa.jdbc.driverClass=com.mysql.cj.jdbc.Driver",
    "cas.authn.passwordless.tokens.jpa.jdbc.url=jdbc:mysql://localhost:3306/mysql?allowPublicKeyRetrieval=true&characterEncoding=UTF-8&useSSL=FALSE",
    "cas.authn.passwordless.tokens.jpa.jdbc.dialect=org.hibernate.dialect.MySQL57InnoDBDialect"
})
@EnabledIfPortOpen(port = 3306)
@EnabledIfContinuousIntegration
@Tag("MySQL")
public class MySQLJpaPasswordlessTokenRepositoryTests extends JpaPasswordlessTokenRepositoryTests {
}
