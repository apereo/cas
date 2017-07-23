package org.apereo.cas.web.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.view.CasProtocolView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.thymeleaf.spring4.SpringTemplateEngine;

/**
 * This is {@link CasProtocolViewsConfiguration} that attempts to create Spring-managed beans
 * backed by external configuration.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casProtocolViewsConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasProtocolViewsConfiguration {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private SpringTemplateEngine springTemplateEngine;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ThymeleafProperties thymeleafProperties;

    /**
     * The Cas protocol views.
     */
    @Configuration("CasProtocolViews")
    public class CasProtocolViews {
        @Bean
        @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public CasProtocolView cas2SuccessView() {
            return new CasProtocolView(casProperties.getView().getCas2().getSuccess(),
                    applicationContext,
                    springTemplateEngine, thymeleafProperties,
                    MediaType.APPLICATION_XML_VALUE);
        }

        @Bean
        @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public CasProtocolView cas2ServiceFailureView() {
            return new CasProtocolView(casProperties.getView().getCas2().getFailure(), applicationContext,
                    springTemplateEngine, thymeleafProperties);
        }

        @Bean
        @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public CasProtocolView cas2ProxyFailureView() {
            return new CasProtocolView(casProperties.getView().getCas2().getProxy().getFailure(), applicationContext,
                    springTemplateEngine, thymeleafProperties,
                    MediaType.APPLICATION_XML_VALUE);
        }

        @Bean
        @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public CasProtocolView cas2ProxySuccessView() {
            return new CasProtocolView(casProperties.getView().getCas2().getProxy().getSuccess(),
                    applicationContext, springTemplateEngine, thymeleafProperties,
                    MediaType.APPLICATION_XML_VALUE);
        }

        @Bean
        @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public CasProtocolView cas3SuccessView() {
            return new CasProtocolView(casProperties.getView().getCas3().getSuccess(),
                    applicationContext, springTemplateEngine, thymeleafProperties);
        }


        @Bean
        @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public CasProtocolView cas3ServiceFailureView() {
            return new CasProtocolView(casProperties.getView().getCas3().getFailure(),
                    applicationContext, springTemplateEngine, thymeleafProperties,
                    MediaType.APPLICATION_XML_VALUE);
        }

        @Bean
        @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public CasProtocolView casPostResponseView() {
            return new CasProtocolView("protocol/casPostResponseView",
                    applicationContext, springTemplateEngine, thymeleafProperties);
        }
    }

    /**
     * The Oauth protocol views.
     */
    @Configuration("OAuthProtocolViews")
    public class OAuthProtocolViews {
        @Bean
        @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public CasProtocolView oauthConfirmView() {
            return new CasProtocolView("protocol/oauth/confirm", applicationContext, springTemplateEngine, thymeleafProperties);
        }

    }

    /**
     * The Oidc protocol views.
     */
    @Configuration("OidcProtocolViews")
    public class OidcProtocolViews {
        @Bean
        @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public CasProtocolView oidcConfirmView() {
            return new CasProtocolView("protocol/oidc/confirm", applicationContext, springTemplateEngine, thymeleafProperties);
        }

    }

    /**
     * The openid protocol views.
     */
    @Configuration("OpenIdProtocolViews")
    public class OpenIdProtocolViews {
        @Bean
        @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public CasProtocolView casOpenIdServiceFailureView() {
            return new CasProtocolView("protocol/openid/casOpenIdServiceFailureView",
                    applicationContext, springTemplateEngine, thymeleafProperties);
        }

        @Bean
        @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public CasProtocolView casOpenIdServiceSuccessView() {
            return new CasProtocolView("protocol/openid/casOpenIdServiceSuccessView", applicationContext,
                    springTemplateEngine, thymeleafProperties);
        }

        @Bean
        @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public CasProtocolView casOpenIdAssociationSuccessView() {
            return new CasProtocolView("protocol/openid/casOpenIdAssociationSuccessView", applicationContext,
                    springTemplateEngine, thymeleafProperties);
        }

        @Bean
        @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public CasProtocolView openIdProviderView() {
            return new CasProtocolView("protocol/openid/user", applicationContext,
                    springTemplateEngine, thymeleafProperties);
        }

    }

}
