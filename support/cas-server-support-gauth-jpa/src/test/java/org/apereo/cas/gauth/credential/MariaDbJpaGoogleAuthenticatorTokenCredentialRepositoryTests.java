package org.apereo.cas.gauth.credential;

import org.apereo.cas.category.MariaDbCategory;
import org.apereo.cas.util.junit.EnabledIfContinuousIntegration;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.junit.experimental.categories.Category;
import org.springframework.test.context.TestPropertySource;

/**
 * Test cases for {@link MariaDbJpaGoogleAuthenticatorTokenCredentialRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@TestPropertySource(properties = {
    "cas.authn.mfa.gauth.jpa.user=root",
    "cas.authn.mfa.gauth.jpa.password=mypass",
    "cas.authn.mfa.gauth.jpa.driverClass=org.mariadb.jdbc.Driver",
    "cas.authn.mfa.gauth.jpa.url=jdbc:mariadb://localhost:3306/mysql?allowPublicKeyRetrieval=true&characterEncoding=UTF-8&useSSL=FALSE",
    "cas.authn.mfa.gauth.jpa.dialect=org.hibernate.dialect.MariaDB103Dialect"
})
@EnabledIfContinuousIntegration
@EnabledIfPortOpen(port = 3306)
@Category(MariaDbCategory.class)
public class MariaDbJpaGoogleAuthenticatorTokenCredentialRepositoryTests extends JpaGoogleAuthenticatorTokenCredentialRepositoryTests {
}
