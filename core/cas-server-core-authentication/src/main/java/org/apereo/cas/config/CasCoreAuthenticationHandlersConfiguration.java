package org.apereo.cas.config;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.AcceptUsersAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.handler.support.HttpBasedServiceCredentialsAuthenticationHandler;
import org.apereo.cas.authentication.handler.support.JaasAuthenticationHandler;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalNameTransformerUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.resolvers.ProxyingPrincipalResolver;
import org.apereo.cas.authentication.support.password.PasswordEncoderUtils;
import org.apereo.cas.authentication.support.password.PasswordPolicyConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.generic.AcceptAuthenticationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.http.HttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link CasCoreAuthenticationHandlersConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.1.0
 */
@Configuration("casCoreAuthenticationHandlersConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasCoreAuthenticationHandlersConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("supportsTrustStoreSslSocketFactoryHttpClient")
    private HttpClient supportsTrustStoreSslSocketFactoryHttpClient;

    @Autowired(required = false)
    @Qualifier("acceptPasswordPolicyConfiguration")
    private PasswordPolicyConfiguration acceptPasswordPolicyConfiguration;

    @Autowired(required = false)
    @Qualifier("jaasPasswordPolicyConfiguration")
    private PasswordPolicyConfiguration jaasPasswordPolicyConfiguration;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @ConditionalOnMissingBean(name = "jaasPrincipalFactory")
    @Bean
    public PrincipalFactory jaasPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @Bean
    public AuthenticationHandler proxyAuthenticationHandler() {
        return new HttpBasedServiceCredentialsAuthenticationHandler(null, servicesManager,
                proxyPrincipalFactory(), Integer.MIN_VALUE,
                supportsTrustStoreSslSocketFactoryHttpClient);
    }

    @ConditionalOnMissingBean(name = "proxyPrincipalFactory")
    @Bean
    public PrincipalFactory proxyPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @ConditionalOnMissingBean(name = "proxyPrincipalResolver")
    @Bean
    public PrincipalResolver proxyPrincipalResolver() {
        return new ProxyingPrincipalResolver(proxyPrincipalFactory());
    }

    @RefreshScope
    @Bean
    public AuthenticationHandler acceptUsersAuthenticationHandler() {
        final AcceptAuthenticationProperties props = casProperties.getAuthn().getAccept();
        final AcceptUsersAuthenticationHandler h = new AcceptUsersAuthenticationHandler(props.getName(), servicesManager,
                acceptUsersPrincipalFactory(), null, getParsedUsers());
        h.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(props.getPasswordEncoder()));
        if (acceptPasswordPolicyConfiguration != null) {
            h.setPasswordPolicyConfiguration(acceptPasswordPolicyConfiguration);
        }
        h.setCredentialSelectionPredicate(CoreAuthenticationUtils.newCredentialSelectionPredicate(props.getCredentialCriteria()));
        h.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(props.getPrincipalTransformation()));
        return h;
    }

    @ConditionalOnMissingBean(name = "acceptUsersPrincipalFactory")
    @Bean
    public PrincipalFactory acceptUsersPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    private Map<String, String> getParsedUsers() {
        final Pattern pattern = Pattern.compile("::");
        final String usersProperty = casProperties.getAuthn().getAccept().getUsers();

        if (StringUtils.isNotBlank(usersProperty) && usersProperty.contains(pattern.pattern())) {
            return Stream.of(usersProperty.split(","))
                    .map(pattern::split)
                    .collect(Collectors.toMap(userAndPassword -> userAndPassword[0], userAndPassword -> userAndPassword[1]));
        }
        return new HashMap<>(0);
    }

    @ConditionalOnMissingBean(name = "proxyAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer proxyAuthenticationEventExecutionPlanConfigurer() {
        return plan -> plan.registerAuthenticationHandlerWithPrincipalResolver(proxyAuthenticationHandler(), proxyPrincipalResolver());
    }

    /**
     * The Jaas authentication configuration.
     */
    @Configuration("jaasAuthenticationConfiguration")
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public class JaasAuthenticationConfiguration {
        @Autowired
        @Qualifier("personDirectoryPrincipalResolver")
        private PrincipalResolver personDirectoryPrincipalResolver;

        @ConditionalOnMissingBean(name = "jaasAuthenticationHandlers")
        @RefreshScope
        @Bean
        public List<AuthenticationHandler> jaasAuthenticationHandlers() {
            return casProperties.getAuthn().getJaas()
                    .stream()
                    .filter(jaas -> StringUtils.isNotBlank(jaas.getRealm()))
                    .map(jaas -> {
                        final JaasAuthenticationHandler h = new JaasAuthenticationHandler(jaas.getName(),
                                servicesManager, jaasPrincipalFactory(), null);

                        h.setKerberosKdcSystemProperty(jaas.getKerberosKdcSystemProperty());
                        h.setKerberosRealmSystemProperty(jaas.getKerberosRealmSystemProperty());
                        h.setRealm(jaas.getRealm());
                        h.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(jaas.getPasswordEncoder()));

                        if (jaasPasswordPolicyConfiguration != null) {
                            h.setPasswordPolicyConfiguration(jaasPasswordPolicyConfiguration);
                        }
                        h.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(jaas.getPrincipalTransformation()));
                        h.setCredentialSelectionPredicate(CoreAuthenticationUtils.newCredentialSelectionPredicate(jaas.getCredentialCriteria()));
                        return h;
                    })
                    .collect(Collectors.toList());
        }

        @ConditionalOnMissingBean(name = "jaasAuthenticationEventExecutionPlanConfigurer")
        @Bean
        public AuthenticationEventExecutionPlanConfigurer jaasAuthenticationEventExecutionPlanConfigurer() {
            return plan -> plan.registerAuthenticationHandlerWithPrincipalResolvers(jaasAuthenticationHandlers(), personDirectoryPrincipalResolver);
        }
    }
}
