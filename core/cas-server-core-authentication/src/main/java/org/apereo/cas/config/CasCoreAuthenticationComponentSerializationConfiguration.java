package org.apereo.cas.config;

import org.apereo.cas.DefaultMessageDescriptor;
import org.apereo.cas.authentication.DefaultAuthentication;
import org.apereo.cas.authentication.DefaultAuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.PrincipalException;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.credential.HttpBasedServiceCredential;
import org.apereo.cas.authentication.credential.OneTimePasswordCredential;
import org.apereo.cas.authentication.credential.RememberMeUsernamePasswordCredential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.InvalidLoginLocationException;
import org.apereo.cas.authentication.exceptions.InvalidLoginTimeException;
import org.apereo.cas.authentication.exceptions.MixedPrincipalException;
import org.apereo.cas.authentication.metadata.BasicCredentialMetaData;
import org.apereo.cas.authentication.principal.SimplePrincipal;
import org.apereo.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.apereo.cas.authentication.support.password.PasswordExpiringWarningMessageDescriptor;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.services.UnauthorizedServiceForPrincipalException;
import org.apereo.cas.services.UnauthorizedSsoServiceException;
import org.apereo.cas.util.crypto.PublicKeyFactoryBean;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;
import org.apereo.cas.validation.ValidationResponseType;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountLockedException;

/**
 * This is {@link CasCoreAuthenticationComponentSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Configuration(value = "casCoreAuthenticationComponentSerializationConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasCoreAuthenticationComponentSerializationConfiguration {
    @Bean
    @ConditionalOnMissingBean(name = "casCoreAuthenticationComponentSerializationPlanConfigurer")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public ComponentSerializationPlanConfigurer casCoreAuthenticationComponentSerializationPlanConfigurer() {
        return plan -> {
            plan.registerSerializableClass(SimpleWebApplicationServiceImpl.class);
            plan.registerSerializableClass(BasicCredentialMetaData.class);
            plan.registerSerializableClass(BasicIdentifiableCredential.class);
            plan.registerSerializableClass(DefaultAuthenticationHandlerExecutionResult.class);
            plan.registerSerializableClass(DefaultAuthentication.class);
            plan.registerSerializableClass(UsernamePasswordCredential.class);
            plan.registerSerializableClass(RememberMeUsernamePasswordCredential.class);
            plan.registerSerializableClass(SimplePrincipal.class);
            plan.registerSerializableClass(HttpBasedServiceCredential.class);
            plan.registerSerializableClass(OneTimePasswordCredential.class);
            plan.registerSerializableClass(PublicKeyFactoryBean.class);
            plan.registerSerializableClass(ValidationResponseType.class);

            plan.registerSerializableClass(PreventedException.class);
            plan.registerSerializableClass(AccountDisabledException.class);
            plan.registerSerializableClass(AccountExpiredException.class);
            plan.registerSerializableClass(AccountLockedException.class);
            plan.registerSerializableClass(InvalidLoginLocationException.class);
            plan.registerSerializableClass(InvalidLoginTimeException.class);
            plan.registerSerializableClass(PrincipalException.class);
            plan.registerSerializableClass(MixedPrincipalException.class);
            plan.registerSerializableClass(UnauthorizedServiceException.class);
            plan.registerSerializableClass(UnauthorizedServiceForPrincipalException.class);
            plan.registerSerializableClass(UnauthorizedSsoServiceException.class);

            plan.registerSerializableClass(DefaultMessageDescriptor.class);
            plan.registerSerializableClass(PasswordExpiringWarningMessageDescriptor.class);
        };
    }
}
