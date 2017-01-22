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
    private ThymeleafProperties properties;

    /**
     * Cas 2  success view.
     *
     * @return the  view
     */
    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public CasProtocolView cas2SuccessView() {
        return new CasProtocolView(casProperties.getView().getCas2().getSuccess(), applicationContext, springTemplateEngine, properties);
    }

    /**
     * Cas 2 service failure view.
     *
     * @return the  view
     */
    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public CasProtocolView cas2ServiceFailureView() {
        return new CasProtocolView(casProperties.getView().getCas2().getFailure(), applicationContext, springTemplateEngine, properties);
    }

    /**
     * Cas 2 proxy failure view.
     *
     * @return the  view
     */
    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public CasProtocolView cas2ProxyFailureView() {
        return new CasProtocolView(casProperties.getView().getCas2().getProxy().getFailure(), applicationContext, springTemplateEngine, properties);
    }

    /**
     * Cas 2 proxy success view.
     *
     * @return the view
     */
    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public CasProtocolView cas2ProxySuccessView() {
        return new CasProtocolView(casProperties.getView().getCas2().getProxy().getSuccess(), applicationContext, springTemplateEngine, properties);
    }

    /**
     * Cas 3 success view.
     *
     * @return the view
     */
    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public CasProtocolView cas3SuccessView() {
        return new CasProtocolView(casProperties.getView().getCas3().getSuccess(), applicationContext, springTemplateEngine, properties);
    }

    /**
     * Cas 3 service failure view.
     *
     * @return the view
     */
    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public CasProtocolView cas3ServiceFailureView() {
        return new CasProtocolView(casProperties.getView().getCas3().getFailure(), applicationContext, springTemplateEngine, properties);
    }

    /**
     * Oauth confirm view.
     *
     * @return the view
     */
    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public CasProtocolView oauthConfirmView() {
        return new CasProtocolView("protocol/oauth/confirm", applicationContext, springTemplateEngine, properties);
    }

    /**
     * Cas open id service failure view.
     *
     * @return the view
     */
    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public CasProtocolView casOpenIdServiceFailureView() {
        return new CasProtocolView("protocol/openid/casOpenIdServiceFailureView", applicationContext, springTemplateEngine, properties);
    }

    /**
     * Cas open id service success view.
     *
     * @return the view
     */
    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public CasProtocolView casOpenIdServiceSuccessView() {
        return new CasProtocolView("protocol/openid/casOpenIdServiceSuccessView", applicationContext, springTemplateEngine, properties);
    }

    /**
     * Cas open id association success view .
     *
     * @return the view
     */
    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public CasProtocolView casOpenIdAssociationSuccessView() {
        return new CasProtocolView("protocol/openid/casOpenIdAssociationSuccessView", applicationContext, springTemplateEngine, properties);
    }

    /**
     * Open id provider view.
     *
     * @return the view
     */
    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public CasProtocolView openIdProviderView() {
        return new CasProtocolView("protocol/openid/user", applicationContext, springTemplateEngine, properties);
    }
}
