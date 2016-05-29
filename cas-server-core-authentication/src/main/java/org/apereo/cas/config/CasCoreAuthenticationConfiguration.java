package org.apereo.cas.config;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apereo.cas.authentication.AcceptAnyAuthenticationPolicyFactory;
import org.apereo.cas.authentication.AcceptUsersAuthenticationHandler;
import org.apereo.cas.authentication.AllAuthenticationPolicy;
import org.apereo.cas.authentication.AnyAuthenticationPolicy;
import org.apereo.cas.authentication.AuthenticationContextValidator;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.AuthenticationPolicy;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.AuthenticationTransactionManager;
import org.apereo.cas.authentication.CacheCredentialsMetaDataPopulator;
import org.apereo.cas.authentication.ContextualAuthenticationPolicyFactory;
import org.apereo.cas.authentication.DefaultAuthenticationSystemSupport;
import org.apereo.cas.authentication.DefaultAuthenticationTransactionManager;
import org.apereo.cas.authentication.DefaultPrincipalElectionStrategy;
import org.apereo.cas.authentication.FileTrustStoreSslSocketFactory;
import org.apereo.cas.authentication.NotPreventedAuthenticationPolicy;
import org.apereo.cas.authentication.PolicyBasedAuthenticationManager;
import org.apereo.cas.authentication.PrincipalElectionStrategy;
import org.apereo.cas.authentication.RegisteredServiceAuthenticationHandlerResolver;
import org.apereo.cas.authentication.RequiredHandlerAuthenticationPolicy;
import org.apereo.cas.authentication.RequiredHandlerAuthenticationPolicyFactory;
import org.apereo.cas.authentication.SuccessfulHandlerMetaDataPopulator;
import org.apereo.cas.authentication.handler.DefaultPasswordEncoder;
import org.apereo.cas.authentication.handler.NoOpPrincipalNameTransformer;
import org.apereo.cas.authentication.handler.PasswordEncoder;
import org.apereo.cas.authentication.handler.PlainTextPasswordEncoder;
import org.apereo.cas.authentication.handler.PrefixSuffixPrincipalNameTransformer;
import org.apereo.cas.authentication.handler.PrincipalNameTransformer;
import org.apereo.cas.authentication.handler.support.HttpBasedServiceCredentialsAuthenticationHandler;
import org.apereo.cas.authentication.handler.support.JaasAuthenticationHandler;
import org.apereo.cas.authentication.principal.BasicPrincipalResolver;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PersonDirectoryPrincipalResolver;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.RememberMeAuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.support.PasswordPolicyConfiguration;
import org.apereo.cas.web.flow.AuthenticationExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

/**
 * This is {@link CasCoreAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casCoreAuthenticationConfiguration")
public class CasCoreAuthenticationConfiguration {

    @Value("${http.client.truststore.file:classpath:truststore.jks}")
    private Resource trustStoreFile;
    
    @Value("${http.client.truststore.psw:changeit}")
    private String trustStorePassword;
            
    @Bean
    public AuthenticationExceptionHandler authenticationExceptionHandler() {
        return new AuthenticationExceptionHandler();
    }
    
    @RefreshScope
    @Bean
    public AuthenticationPolicy requiredHandlerAuthenticationPolicy() {
        return new RequiredHandlerAuthenticationPolicy();
    }

    @Bean
    public AuthenticationPolicy anyAuthenticationPolicy() {
        return new AnyAuthenticationPolicy();
    }

    @Bean
    public ContextualAuthenticationPolicyFactory acceptAnyAuthenticationPolicyFactory() {
        return new AcceptAnyAuthenticationPolicyFactory();
    }
    
    @Bean
    public AuthenticationHandler acceptUsersAuthenticationHandler() {
        return new AcceptUsersAuthenticationHandler();
    }

    @Bean
    public AuthenticationPolicy allAuthenticationPolicy() {
        return new AllAuthenticationPolicy();
    }

    @RefreshScope
    @Bean
    public AuthenticationContextValidator authenticationContextValidator() {
        return new AuthenticationContextValidator();
    }

    @Bean
    public AuthenticationMetaDataPopulator cacheCredentialsMetaDataPopulator() {
        return new CacheCredentialsMetaDataPopulator();
    }

    @Bean
    public AuthenticationSystemSupport defaultAuthenticationSystemSupport() {
        return new DefaultAuthenticationSystemSupport();
    }
    
    @Bean
    public AuthenticationTransactionManager defaultAuthenticationTransactionManager() {
        return new DefaultAuthenticationTransactionManager();
    }
    
    @Bean
    public PrincipalElectionStrategy defaultPrincipalElectionStrategy() {
        return new DefaultPrincipalElectionStrategy();
    }
    
    @RefreshScope
    @Bean
    public SSLConnectionSocketFactory trustStoreSslSocketFactory() {
        return new FileTrustStoreSslSocketFactory(this.trustStoreFile, this.trustStorePassword);
    }
    
    @Bean
    public AuthenticationPolicy notPreventedAuthenticationPolicy() {
        return new NotPreventedAuthenticationPolicy();
    }
    
    @Bean
    public AuthenticationManager authenticationManager() {
        return new PolicyBasedAuthenticationManager();
    }
    
    @Bean
    public RegisteredServiceAuthenticationHandlerResolver registeredServiceAuthenticationHandlerResolver() {
        return new RegisteredServiceAuthenticationHandlerResolver();
    }

    @Bean
    public ContextualAuthenticationPolicyFactory requiredHandlerAuthenticationPolicyFactory() {
        return new RequiredHandlerAuthenticationPolicyFactory();
    }
    
    @Bean
    public AuthenticationMetaDataPopulator successfulHandlerMetaDataPopulator() {
        return new SuccessfulHandlerMetaDataPopulator();
    }
    
    @Bean 
    public PasswordPolicyConfiguration defaultPasswordPolicyConfiguration() {
        return new PasswordPolicyConfiguration();
    }
    
    @Bean
    public AuthenticationMetaDataPopulator rememberMeAuthenticationMetaDataPopulator() {
        return new RememberMeAuthenticationMetaDataPopulator();
    }
    
    @RefreshScope
    @Bean
    public PrincipalResolver personDirectoryPrincipalResolver() {
        return new PersonDirectoryPrincipalResolver();
    }
    
    @Bean
    public PrincipalFactory defaultPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }
    
    @Bean
    public PrincipalResolver proxyPrincipalResolver() {
        return new BasicPrincipalResolver();
    }
    
    @RefreshScope
    @Bean
    public AuthenticationHandler jaasAuthenticationHandler() {
        return new JaasAuthenticationHandler();
    }
    
    @Bean
    public AuthenticationHandler proxyAuthenticationHandler() {
        return new HttpBasedServiceCredentialsAuthenticationHandler();
    }
    
    @Bean
    public PrincipalNameTransformer prefixSuffixPrincipalNameTransformer() {
        return new PrefixSuffixPrincipalNameTransformer();
    }
    
    @Bean
    public PasswordEncoder plainTextPasswordEncoder() {
        return new PlainTextPasswordEncoder();
    }
    
    @Bean
    public PrincipalNameTransformer noOpPrincipalNameTransformer() {
        return new NoOpPrincipalNameTransformer();
    }
    
    @RefreshScope
    @Bean
    public DefaultPasswordEncoder defaultPasswordEncoder() {
        return new DefaultPasswordEncoder();
    }
}
