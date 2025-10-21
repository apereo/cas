package org.apereo.cas.gauth.credential;

import org.apereo.cas.config.CasGoogleAuthenticatorJpaAutoConfiguration;
import org.apereo.cas.config.CasHibernateJpaAutoConfiguration;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.test.CasTestExtension;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for {@link JpaGoogleAuthenticatorTokenCredentialRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@SpringBootTest(classes = {
    CasGoogleAuthenticatorJpaAutoConfiguration.class,
    CasHibernateJpaAutoConfiguration.class,
    BaseOneTimeTokenCredentialRepositoryTests.SharedTestConfiguration.class
},
    properties = {
        "cas.jdbc.show-sql=true",
        "cas.authn.mfa.gauth.core.scratch-codes.encryption.key=12345678901234567890123456789012",
        "cas.authn.mfa.gauth.crypto.enabled=false"
    })
@EnableTransactionManagement(proxyTargetClass = false)
@EnableAspectJAutoProxy(proxyTargetClass = false)
@EnableScheduling
@Getter
@Tag("JDBCMFA")
@ExtendWith(CasTestExtension.class)
class JpaGoogleAuthenticatorTokenCredentialRepositoryTests extends BaseOneTimeTokenCredentialRepositoryTests {
    @Autowired(required = false)
    @Qualifier(BaseGoogleAuthenticatorTokenCredentialRepository.BEAN_NAME)
    private OneTimeTokenCredentialRepository registry;

    @BeforeEach
    void cleanUp() {
        this.getRegistry().deleteAll();
    }
    
    @Test
    void verifyCreateUniqueNames() {
        var acct1 = getAccount("verifyCreateUniqueNames", UUID.randomUUID().toString());
        assertNotNull(acct1);
        val repo = getRegistry("verifyCreate");
        acct1 = repo.save(acct1);
        assertNotNull(acct1);

        var acct2 = getAccount("verifyCreateUniqueNames", UUID.randomUUID().toString());
        acct2.setName(acct1.getName());
        acct2 = repo.save(acct2);
        assertNotNull(acct2);

        acct2.setName("NewAccount");
        acct2 = repo.update(acct2);
        assertNotNull(acct2);

        acct1 = repo.save(acct1);
        assertNotNull(acct1);
    }

}
