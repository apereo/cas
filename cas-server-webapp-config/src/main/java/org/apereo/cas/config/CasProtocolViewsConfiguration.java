package org.apereo.cas.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${view.cas2.success:protocol/2.0/casServiceValidationSuccess}")
    private String cas2SuccessView;

    @Autowired
    private ThymeleafProperties properties;

    /**
     * The Cas 2 failure view.
     */
    @Value("${view.cas2.failure:protocol/2.0/casServiceValidationFailure}")
    private String cas2FailureView;

    /**
     * The Cas 2 proxy success view.
     */
    @Value("${view.cas2.proxy.success:protocol/2.0/casProxySuccessView}")
    private String cas2ProxySuccessView;

    /**
     * The Cas 2 proxy failure view.
     */
    @Value("${view.cas2.proxy.failure:protocol/2.0/casProxyFailureView}")
    private String cas2ProxyFailureView;

    /**
     * The Cas 3 success view.
     */
    @Value("${view.cas3.success:protocol/3.0/casServiceValidationSuccess}")
    private String cas3SuccessView;

    /**
     * The Cas 3 failure view.
     */
    @Value("${view.cas3.failure:protocol/3.0/casServiceValidationFailure}")
    private String cas3FailureView;

    /**
     * Cas 2  success view.
     *
     * @return the  view
     */
    @RefreshScope
    @Bean(name = "cas2SuccessView")
    public View cas2SuccessView() {
        return new CasProtocolView(this.cas2SuccessView, this.applicationContext, this.springTemplateEngine);
    }

    /**
     * Cas 2 service failure view.
     *
     * @return the  view
     */
    @RefreshScope
    @Bean(name = "cas2ServiceFailureView")
    public View cas2ServiceFailureView() {
        return new CasProtocolView(this.cas2FailureView, this.applicationContext, this.springTemplateEngine);
    }

    /**
     * Cas 2 proxy failure view.
     *
     * @return the  view
     */
    @RefreshScope
    @Bean(name = "cas2ProxyFailureView")
    public View cas2ProxyFailureView() {
        return new CasProtocolView(this.cas2ProxyFailureView, this.applicationContext, this.springTemplateEngine);
    }

    /**
     * Cas 2 proxy success view.
     *
     * @return the view
     */
    @RefreshScope
    @Bean(name = "cas2ProxySuccessView")
    public View cas2ProxySuccessView() {
        return new CasProtocolView(this.cas2ProxySuccessView, this.applicationContext, this.springTemplateEngine);
    }

    /**
     * Cas 3 success view.
     *
     * @return the view
     */
    @RefreshScope
    @Bean(name = "cas3SuccessView")
    public View cas3SuccessView() {
        return new CasProtocolView(this.cas3SuccessView, this.applicationContext, this.springTemplateEngine);
    }

    /**
     * Cas 3 service failure view.
     *
     * @return the view
     */
    @RefreshScope
    @Bean(name = "cas3ServiceFailureView")
    public View cas3ServiceFailureView() {
        return new CasProtocolView(this.cas3FailureView, this.applicationContext, this.springTemplateEngine);
    }

    /**
     * Oauth confirm view.
     *
     * @return the view
     */
    @RefreshScope
    @Bean(name = "oauthConfirmView")
    public View oauthConfirmView() {
        return new CasProtocolView("protocol/oauth/confirm", this.applicationContext, this.springTemplateEngine);
    }


    /**
     * Cas open id service failure view.
     *
     * @return the view
     */
    @RefreshScope
    @Bean(name = "casOpenIdServiceFailureView")
    public View casOpenIdServiceFailureView() {
        return new CasProtocolView("protocol/openid/casOpenIdServiceFailureView", this.applicationContext, this.springTemplateEngine);
    }

    /**
     * Cas open id service success view.
     *
     * @return the view
     */
    @RefreshScope
    @Bean(name = "casOpenIdServiceSuccessView")
    public View casOpenIdServiceSuccessView() {
        return new CasProtocolView("protocol/openid/casOpenIdServiceSuccessView", this.applicationContext, this.springTemplateEngine);
    }

    /**
     * Cas open id association failure view.
     *
     * @return the view
     */
    @RefreshScope
    @Bean(name = "casOpenIdAssociationFailureView")
    public View casOpenIdAssociationFailureView() {
        return new CasProtocolView("protocol/openid/casOpenIdAssociationFailureView", this.applicationContext, this.springTemplateEngine);
    }

    /**
     * Cas open id association success view .
     *
     * @return the view
     */
    @RefreshScope
    @Bean(name = "casOpenIdAssociationSuccessView")
    public View casOpenIdAssociationSuccessView() {
        return new CasProtocolView("protocol/openid/casOpenIdAssociationSuccessView", this.applicationContext, this.springTemplateEngine);
    }

    /**
     * Open id provider view.
     *
     * @return the view
     */
    @RefreshScope
    @Bean(name = "openIdProviderView")
    public View openIdProviderView() {
        return new CasProtocolView("protocol/openid/user", this.applicationContext, this.springTemplateEngine);
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
                        final SpringTemplateEngine templateEngine) {
            super(templateName);
            setApplicationContext(applicationContext);
            setTemplateEngine(templateEngine);
            setCharacterEncoding("UTF-8");
            setLocale(Locale.getDefault());
        }
    }
}
