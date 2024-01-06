package org.apereo.cas.gauth.credential;

import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasCoreWebflowAutoConfiguration;
import org.apereo.cas.config.CasGoogleAuthenticatorAutoConfiguration;
import org.apereo.cas.config.CasGoogleAuthenticatorLdapAutoConfiguration;
import org.apereo.cas.config.CasOneTimeTokenAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * This is {@link BaseLdapGoogleAuthenticatorTokenCredentialRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Getter
public abstract class BaseLdapGoogleAuthenticatorTokenCredentialRepositoryTests extends BaseOneTimeTokenCredentialRepositoryTests {
    @Autowired(required = false)
    @Qualifier("googleAuthenticatorAccountRegistry")
    private OneTimeTokenCredentialRepository registry;

    @Autowired
    private CasConfigurationProperties casProperties;

    @BeforeEach
    public void cleanUp() {
        getRegistry().deleteAll();
    }

    @ImportAutoConfiguration({
        RefreshAutoConfiguration.class,
        WebMvcAutoConfiguration.class,
        AopAutoConfiguration.class
    })
    @SpringBootConfiguration
    @Import({
        CasGoogleAuthenticatorLdapAutoConfiguration.class,
        CasOneTimeTokenAuthenticationAutoConfiguration.class,
        CasGoogleAuthenticatorAutoConfiguration.class,
        CasCoreTicketsAutoConfiguration.class,
        CasCoreLogoutAutoConfiguration.class,
        CasCoreCookieAutoConfiguration.class,
        CasCoreAuthenticationAutoConfiguration.class,
        CasPersonDirectoryAutoConfiguration.class,
        CasCoreServicesAutoConfiguration.class,
        CasCoreUtilAutoConfiguration.class,
        CasCoreNotificationsAutoConfiguration.class,
        CasCoreAutoConfiguration.class,
        CasCoreWebAutoConfiguration.class,
        CasCoreWebflowAutoConfiguration.class,
        CasCoreMultifactorAuthenticationAutoConfiguration.class,
        CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class

    })
    public static class SharedTestConfiguration {
    }
}
