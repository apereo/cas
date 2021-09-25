package org.apereo.cas.gauth.credential;

import org.apereo.cas.config.CasHibernateJpaConfiguration;
import org.apereo.cas.config.GoogleAuthenticatorJpaConfiguration;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;

import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
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
    GoogleAuthenticatorJpaConfiguration.class,
    CasHibernateJpaConfiguration.class,
    BaseOneTimeTokenCredentialRepositoryTests.SharedTestConfiguration.class
},
    properties = {
        "cas.jdbc.show-sql=false",
        "cas.authn.mfa.gauth.crypto.enabled=false"
    })
@EnableTransactionManagement
@EnableAspectJAutoProxy
@EnableScheduling
@Getter
@Tag("JDBC")
public class JpaGoogleAuthenticatorTokenCredentialRepositoryTests extends BaseOneTimeTokenCredentialRepositoryTests {
    @Autowired(required = false)
    @Qualifier("googleAuthenticatorAccountRegistry")
    private OneTimeTokenCredentialRepository registry;

    @BeforeEach
    public void cleanUp() {
        this.getRegistry().deleteAll();
    }
    
    @Test
    public void verifyCreateUniqueNames() {
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
