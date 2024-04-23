package org.apereo.cas.authentication;

import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasLdapAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.spring.beans.BeanContainer;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.jooq.lambda.UncheckedException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Import;
import javax.security.auth.login.FailedLoginException;
import java.util.Arrays;
import static org.apereo.cas.util.junit.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link LdapAuthenticationHandler}.
 *
 * @author Marvin S. Addison
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@SpringBootTest(classes = BaseLdapAuthenticationHandlerTests.SharedTestConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public abstract class BaseLdapAuthenticationHandlerTests {
    @Autowired
    @Qualifier("ldapAuthenticationHandlers")
    protected BeanContainer<AuthenticationHandler> ldapAuthenticationHandlers;

    static String getFailurePassword() {
        return "bad";
    }

    @Test
    void verifyAuthenticateFailure() throws Throwable {
        assertNotEquals(0, ldapAuthenticationHandlers.size());
        assertThrowsWithRootCause(UncheckedException.class, FailedLoginException.class,
            () -> ldapAuthenticationHandlers.toList().forEach(Unchecked.consumer(handler ->
                handler.authenticate(new UsernamePasswordCredential(getUsername(), getFailurePassword()), mock(Service.class)))));
    }

    @Test
    void verifyAuthenticateSuccess() throws Throwable {
        assertNotEquals(0, ldapAuthenticationHandlers.size());
        for (val handler : ldapAuthenticationHandlers.toList()) {
            val credential = new UsernamePasswordCredential(getUsername(), getSuccessPassword());
            val result = handler.authenticate(credential, mock(Service.class));
            assertNotNull(result.getPrincipal());
            assertEquals(credential.getUsername(), result.getPrincipal().getId());
            val attributes = result.getPrincipal().getAttributes();
            Arrays.stream(getPrincipalAttributes()).forEach(s -> assertTrue(attributes.containsKey(s)));
        }
    }

    String[] getPrincipalAttributes() {
        return new String[]{"cn", "description"};
    }

    String getUsername() throws Exception {
        return "admin";
    }

    String getSuccessPassword() {
        return "password";
    }

    @ImportAutoConfiguration({
        MailSenderAutoConfiguration.class,
        AopAutoConfiguration.class,
        WebMvcAutoConfiguration.class,
        SecurityAutoConfiguration.class,
        RefreshAutoConfiguration.class
    })
    @SpringBootConfiguration
    @Import({
        CasCoreAuthenticationAutoConfiguration.class,
        CasCoreServicesAutoConfiguration.class,
        CasCoreUtilAutoConfiguration.class,
        CasCoreTicketsAutoConfiguration.class,
        CasPersonDirectoryAutoConfiguration.class,
        CasCoreWebAutoConfiguration.class,
        CasCoreNotificationsAutoConfiguration.class,
        CasCoreLogoutAutoConfiguration.class,
        CasCoreAutoConfiguration.class,
        CasLdapAuthenticationAutoConfiguration.class
    })
    public static class SharedTestConfiguration {
    }
}
