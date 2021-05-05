package org.apereo.cas.webauthn;

import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link MySQLJpaWebAuthnCredentialRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@TestPropertySource(properties = {
    "cas.authn.mfa.web-authn.jpa.user=root",
    "cas.authn.mfa.web-authn.jpa.password=password",
    "cas.authn.mfa.web-authn.jpa.driver-class=com.mysql.cj.jdbc.Driver",
    "cas.authn.mfa.web-authn.jpa.url=jdbc:mysql://localhost:3306/mysql?allowPublicKeyRetrieval=true&characterEncoding=UTF-8&useSSL=FALSE",
    "cas.authn.mfa.web-authn.jpa.dialect=org.hibernate.dialect.MySQL8Dialect"
})
@EnabledIfPortOpen(port = 3306)
@Tag("MySQL")
public class MySQLJpaWebAuthnCredentialRepositoryTests extends JpaWebAuthnCredentialRepositoryTests {
}
