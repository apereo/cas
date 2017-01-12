package org.apereo.cas.adaptors.gauth;

import com.warrenstrange.googleauth.ICredentialRepository;
import org.apereo.cas.config.GoogleAuthenticatorJpaConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Test cases for {@link JpaGoogleAuthenticatorCredentialRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {GoogleAuthenticatorJpaConfiguration.class, AopAutoConfiguration.class, RefreshAutoConfiguration.class})
@EnableTransactionManagement
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class JpaGoogleAuthenticatorAccountRegistryTests {
    
    @Autowired
    @Qualifier("googleAuthenticatorAccountRegistry")
    private ICredentialRepository registry;
    
    @Test
    public void verifySave() {
        registry.saveUserCredentials("uid", "secret", 143211, Arrays.asList(1, 2, 3, 4, 5, 6));
        final String s = registry.getSecretKey("uid");
        assertEquals(s, "secret");
    }
}
