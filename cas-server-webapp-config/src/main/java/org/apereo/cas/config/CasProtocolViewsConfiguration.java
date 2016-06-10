package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.View;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.view.ThymeleafView;

import java.util.Locale;

/**
 * This is {@link CasProtocolViewsConfiguration} that attempts to create Spring-managed beans
 * backed by external configuration.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casProtocolViewsConfiguration")
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
    @RefreshScope
    @Bean
    public View cas2SuccessView() {
        return new CasProtocolView(casProperties.getView().getCas2().getSuccess(),
                this.applicationContext, this.springTemplateEngine, this.properties);
    }

    /**
     * Cas 2 service failure view.
     *
     * @return the  view
     */
    @RefreshScope
    @Bean
    public View cas2ServiceFailureView() {
        return new CasProtocolView(casProperties.getView().getCas2().getFailure(),
                this.applicationContext, this.springTemplateEngine, this.properties);
    }

    /**
     * Cas 2 proxy failure view.
     *
     * @return the  view
     */
    @RefreshScope
    @Bean
    public View cas2ProxyFailureView() {
        return new CasProtocolView(casProperties.getView().getCas2().getProxy().getFailure(),
                this.applicationContext, this.springTemplateEngine, this.properties);
    }

    /**
     * Cas 2 proxy success view.
     *
     * @return the view
     */
    @RefreshScope
    @Bean
    public View cas2ProxySuccessView() {
        return new CasProtocolView(casProperties.getView().getCas2().getProxy().getSuccess(),
                this.applicationContext, this.springTemplateEngine, this.properties);
    }

    /**
     * Cas 3 success view.
     *
     * @return the view
     */
    @RefreshScope
    @Bean
    public View cas3SuccessView() {
        return new CasProtocolView(casProperties.getView().getCas3().getSuccess(),
                this.applicationContext, this.springTemplateEngine, this.properties);
    }

    /**
     * Cas 3 service failure view.
     *
     * @return the view
     */
    @RefreshScope
    @Bean
    public View cas3ServiceFailureView() {
        return new CasProtocolView(casProperties.getView().getCas3().getFailure(),
                this.applicationContext, this.springTemplateEngine, this.properties);
    }

    /**
     * Oauth confirm view.
     *
     * @return the view
     */
    @RefreshScope
    @Bean
    public View oauthConfirmView() {
        return new CasProtocolView("protocol/oauth/confirm",
                this.applicationContext, this.springTemplateEngine, this.properties);
    }


    /**
     * Cas open id service failure view.
     *
     * @return the view
     */
    @RefreshScope
    @Bean
    public View casOpenIdServiceFailureView() {
        return new CasProtocolView("protocol/openid/casOpenIdServiceFailureView",
                this.applicationContext, this.springTemplateEngine, this.properties);
    }

    /**
     * Cas open id service success view.
     *
     * @return the view
     */
    @RefreshScope
    @Bean
    public View casOpenIdServiceSuccessView() {
        return new CasProtocolView("protocol/openid/casOpenIdServiceSuccessView",
                this.applicationContext, this.springTemplateEngine, this.properties);
    }

    /**
     * Cas open id association failure view.
     *
     * @return the view
     */
    @RefreshScope
    @Bean
    public View casOpenIdAssociationFailureView() {
        return new CasProtocolView("protocol/openid/casOpenIdAssociationFailureView",
                this.applicationContext, this.springTemplateEngine, this.properties);
    }

    /**
     * Cas open id association success view .
     *
     * @return the view
     */
    @RefreshScope
    @Bean
    public View casOpenIdAssociationSuccessView() {
        return new CasProtocolView("protocol/openid/casOpenIdAssociationSuccessView",
                this.applicationContext, this.springTemplateEngine, this.properties);
    }

    /**
     * Open id provider view.
     *
     * @return the view
     */
    @RefreshScope
    @Bean
    public View openIdProviderView() {
        return new CasProtocolView("protocol/openid/user", this.applicationContext, this.springTemplateEngine,
                this.properties);
    }


    private static class CasProtocolView extends ThymeleafView {
        /**
         * Instantiates a new Cas protocol view.
         *
         * @param templateName       the template name
         * @param applicationContext the application context
         * @param templateEngine     the template engine
         */
        CasProtocolView(final String templateName, final ApplicationContext applicationContext,
                        final SpringTemplateEngine templateEngine, final ThymeleafProperties properties) {
            super(templateName);
            setApplicationContext(applicationContext);
            setTemplateEngine(templateEngine);
            setCharacterEncoding(properties.getEncoding().displayName());
            setLocale(Locale.getDefault());
        }
    }
}
