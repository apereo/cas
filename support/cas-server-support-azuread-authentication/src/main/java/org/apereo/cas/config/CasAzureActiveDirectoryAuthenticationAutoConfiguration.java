package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalNameTransformerUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.authentication.support.password.PasswordEncoderUtils;
import org.apereo.cas.azure.ad.authentication.AzureActiveDirectoryAuthenticationHandler;
import org.apereo.cas.azure.ad.authentication.MicrosoftGraphPersonAttributeDao;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryPlanConfigurer;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link CasAzureActiveDirectoryAuthenticationAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Authentication, module = "azuread")
@AutoConfiguration
public class CasAzureActiveDirectoryAuthenticationAutoConfiguration {

    @Configuration(value = "AzureActiveDirectoryAttributeConfiguration", proxyBeanMethods = false)
    static class AzureActiveDirectoryAttributeConfiguration {

        @ConditionalOnMissingBean(name = "microsoftAzureActiveDirectoryAttributeRepositories")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public List<PersonAttributeDao> microsoftAzureActiveDirectoryAttributeRepositories(final CasConfigurationProperties casProperties) {
            val resolver = SpringExpressionLanguageValueResolver.getInstance();
            val list = new ArrayList<PersonAttributeDao>();
            val attrs = casProperties.getAuthn().getAttributeRepository();
            attrs.getAzureActiveDirectory().stream()
                .filter(msft -> StringUtils.isNotBlank(msft.getClientId()) && StringUtils.isNotBlank(msft.getClientSecret()))
                .forEach(msft -> {
                    val dao = new MicrosoftGraphPersonAttributeDao();
                    FunctionUtils.doIfNotNull(msft.getId(), id -> dao.setId(id));
                    FunctionUtils.doIfNotNull(msft.getApiBaseUrl(), dao::setApiBaseUrl);
                    FunctionUtils.doIfNotNull(msft.getGrantType(), dao::setGrantType);
                    FunctionUtils.doIfNotNull(msft.getLoginBaseUrl(), dao::setLoginBaseUrl);
                    FunctionUtils.doIfNotNull(msft.getLoggingLevel(), dao::setLoggingLevel);
                    FunctionUtils.doIfNotNull(msft.getAttributes(), dao::setProperties);
                    FunctionUtils.doIfNotNull(msft.getResource(), dao::setResource);
                    FunctionUtils.doIfNotNull(msft.getScope(), dao::setScope);

                    dao.setTenant(resolver.resolve(msft.getTenant()));
                    dao.setDomain(resolver.resolve(msft.getDomain()));
                    dao.setClientSecret(resolver.resolve(msft.getClientSecret()));
                    dao.setClientId(resolver.resolve(msft.getClientId()));

                    dao.setOrder(msft.getOrder());
                    list.add(dao);
                });
            return list;
        }

        @ConditionalOnMissingBean(name = "microsoftAzureActiveDirectoryAttributeRepositoryPlanConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PersonDirectoryAttributeRepositoryPlanConfigurer microsoftAzureActiveDirectoryAttributeRepositoryPlanConfigurer(
            @Qualifier("microsoftAzureActiveDirectoryAttributeRepositories")
            final List<PersonAttributeDao> repositories) {
            return plan -> repositories.stream().filter(PersonAttributeDao::isEnabled).forEach(plan::registerAttributeRepository);
        }

    }

    @Configuration(value = "AzureActiveDirectoryAuthenticationHandlerConfiguration", proxyBeanMethods = false)
    static class AzureActiveDirectoryAuthenticationHandlerConfiguration {
        private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.azure-active-directory.client-id")
            .and("cas.authn.azure-active-directory.enabled").isTrue().evenIfMissing();

        @ConditionalOnMissingBean(name = "microsoftAzureActiveDirectoryPrincipalFactory")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PrincipalFactory microsoftAzureActiveDirectoryPrincipalFactory() {
            return PrincipalFactoryUtils.newPrincipalFactory();
        }

        @ConditionalOnMissingBean(name = "microsoftAzureActiveDirectoryAuthenticationHandler")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationHandler microsoftAzureActiveDirectoryAuthenticationHandler(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            final CasConfigurationProperties casProperties,
            @Qualifier("microsoftAzureActiveDirectoryPrincipalFactory")
            final PrincipalFactory factory) {
            return BeanSupplier.of(AuthenticationHandler.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val azure = casProperties.getAuthn().getAzureActiveDirectory();
                    val handler = new AzureActiveDirectoryAuthenticationHandler(factory, azure);
                    handler.setState(azure.getState());
                    handler.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(azure.getPrincipalTransformation()));
                    handler.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(azure.getPasswordEncoder(), applicationContext));
                    handler.setCredentialSelectionPredicate(CoreAuthenticationUtils.newCredentialSelectionPredicate(azure.getCredentialCriteria()));
                    return handler;
                })
                .otherwiseProxy()
                .get();
        }

        @ConditionalOnMissingBean(name = "microsoftAzureActiveDirectoryAuthenticationEventExecutionPlanConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationEventExecutionPlanConfigurer microsoftAzureActiveDirectoryAuthenticationEventExecutionPlanConfigurer(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("microsoftAzureActiveDirectoryAuthenticationHandler")
            final AuthenticationHandler handler,
            @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
            final PrincipalResolver defaultPrincipalResolver) {
            return BeanSupplier.of(AuthenticationEventExecutionPlanConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> plan -> plan.registerAuthenticationHandlerWithPrincipalResolver(handler, defaultPrincipalResolver))
                .otherwiseProxy()
                .get();
        }
    }
}
