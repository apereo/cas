package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.gauth.credential.LdapGoogleAuthenticatorTokenCredentialRepository;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.warrenstrange.googleauth.IGoogleAuthenticator;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * This is {@link GoogleAuthenticatorLdapConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Configuration(value = "googleAuthenticatorLdapConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableScheduling
public class GoogleAuthenticatorLdapConfiguration {
    
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Bean
    @ConditionalOnMissingBean(name = "googleAuthenticatorAccountRegistry")
    public OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry(
        @Qualifier("googleAuthenticatorInstance") final IGoogleAuthenticator googleAuthenticatorInstance,
        @Qualifier("googleAuthenticatorAccountCipherExecutor") final CipherExecutor cipherExecutor) {

        val ldap = casProperties.getAuthn().getMfa().getGauth().getLdap();
        val connectionFactory = LdapUtils.newLdaptiveConnectionFactory(ldap);
        return new LdapGoogleAuthenticatorTokenCredentialRepository(
            cipherExecutor,
            googleAuthenticatorInstance,
            connectionFactory, ldap);
    }

}
