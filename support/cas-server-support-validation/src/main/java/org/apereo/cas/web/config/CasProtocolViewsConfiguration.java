package org.apereo.cas.web.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.validation.CasProtocolViewFactory;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.View;

/**
 * This is {@link CasProtocolViewsConfiguration} that attempts to create Spring-managed beans
 * backed by external configuration.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration(value = "casProtocolViewsConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasProtocolViewsConfiguration {

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("casProtocolViewFactory")
    private ObjectProvider<CasProtocolViewFactory> casProtocolViewFactory;

    /**
     * The CAS protocol views.
     */
    @Configuration("CasProtocolViews")
    public class CasProtocolViews {
        @Bean
        @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public View cas2SuccessView() {
            return casProtocolViewFactory.getObject().create(applicationContext,
                casProperties.getView().getCas2().getSuccess(),
                MediaType.APPLICATION_XML_VALUE);
        }

        @Bean
        @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public View cas2ServiceFailureView() {
            return casProtocolViewFactory.getObject().create(applicationContext,
                casProperties.getView().getCas2().getFailure());
        }

        @ConditionalOnProperty(prefix = "cas.sso", name = "proxyAuthnEnabled", havingValue = "true", matchIfMissing = true)
        @Bean
        @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public View cas2ProxyFailureView() {
            return casProtocolViewFactory.getObject().create(applicationContext,
                casProperties.getView().getCas2().getProxy().getFailure(),
                MediaType.APPLICATION_XML_VALUE);
        }

        @ConditionalOnProperty(prefix = "cas.sso", name = "proxyAuthnEnabled", havingValue = "true", matchIfMissing = true)
        @Bean
        @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public View cas2ProxySuccessView() {
            return casProtocolViewFactory.getObject().create(applicationContext,
                casProperties.getView().getCas2().getProxy().getSuccess(),
                MediaType.APPLICATION_XML_VALUE);
        }

        @Bean
        @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public View cas3SuccessView() {
            return casProtocolViewFactory.getObject().create(applicationContext,
                casProperties.getView().getCas3().getSuccess());
        }


        @Bean
        @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public View cas3ServiceFailureView() {
            return casProtocolViewFactory.getObject().create(applicationContext,
                casProperties.getView().getCas3().getFailure(),
                MediaType.APPLICATION_XML_VALUE);
        }

        @Bean
        @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public View casPostResponseView() {
            return casProtocolViewFactory.getObject().create(applicationContext,
                "protocol/casPostResponseView");
        }
    }

    /**
     * The Oauth protocol views.
     */
    @Configuration("OAuthProtocolViews")
    public class OAuthProtocolViews {
        @Bean
        @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public View oauthConfirmView() {
            return casProtocolViewFactory.getObject().create(applicationContext, "protocol/oauth/confirm");
        }

        @Bean
        @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public View oauthDeviceCodeApprovalView() {
            return casProtocolViewFactory.getObject().create(applicationContext, "protocol/oauth/deviceCodeApproval");
        }

        @Bean
        @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public View oauthDeviceCodeApprovedView() {
            return casProtocolViewFactory.getObject().create(applicationContext, "protocol/oauth/deviceCodeApproved");
        }

        @Bean
        @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public View oauthSessionStaleMismatchErrorView() {
            return casProtocolViewFactory.getObject().create(applicationContext, "protocol/oauth/sessionStaleMismatchError");
        }
    }

    /**
     * The Oidc protocol views.
     */
    @Configuration("OidcProtocolViews")
    public class OidcProtocolViews {
        @Bean
        @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public View oidcConfirmView() {
            return casProtocolViewFactory.getObject().create(applicationContext, "protocol/oidc/confirm");
        }

    }
}
