package org.apereo.cas.gauth.credential;

import module java.base;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.test.context.TestPropertySource;

/**
 * Test cases for {@link MySQLJpaGoogleAuthenticatorTokenCredentialRepositoryTests}.
 *
 * This class shares one physical database and one persistence unit with its sibling test for the
 * token repository, and its schema generation drops and recreates the tables at startup. The
 * shared resource keeps the two from running at the same time and pulling the schema out from
 * under each other.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@TestPropertySource(properties = {
    "cas.authn.mfa.gauth.jpa.ddl-auto=create-drop",
    "cas.authn.mfa.gauth.jpa.user=root",
    "cas.authn.mfa.gauth.jpa.password=password",
    "cas.authn.mfa.gauth.jpa.driver-class=com.mysql.cj.jdbc.Driver",
    "cas.authn.mfa.gauth.jpa.url=jdbc:mysql://localhost:3306/cas?allowPublicKeyRetrieval=true&characterEncoding=UTF-8&useSSL=FALSE",
    "cas.authn.mfa.gauth.jpa.dialect=org.hibernate.dialect.MySQLDialect"
})
@EnabledIfListeningOnPort(port = 3306)
@ResourceLock("googleAuthenticatorJpaSchema")
@Tag("MySQL")
class MySQLJpaGoogleAuthenticatorTokenCredentialRepositoryTests extends JpaGoogleAuthenticatorTokenCredentialRepositoryTests {
}
