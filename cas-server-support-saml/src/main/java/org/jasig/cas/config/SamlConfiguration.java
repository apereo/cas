package org.jasig.cas.config;

import org.jasig.cas.authentication.support.CasAttributeEncoder;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.support.saml.web.view.Saml10FailureResponseView;
import org.jasig.cas.support.saml.web.view.Saml10SuccessResponseView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * This is {@link SamlConfiguration} that creates the necessary opensaml context and beans.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("samlConfiguration")
@Lazy(true)
public class SamlConfiguration {

    /**
     * The Issuer.
     */
    @Value("${cas.saml.response.issuer:localhost}")
    private String issuer;

    /**
     * The Skew allowance.
     */
    @Value("${cas.saml.response.skewAllowance:0}")
    private int skewAllowance;

    /**
     * The Services manager.
     */
    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    /**
     * The Cas attribute encoder.
     */
    @Autowired
    @Qualifier("casAttributeEncoder")
    private CasAttributeEncoder casAttributeEncoder;

    /**
     * Cas saml service success view saml 10 success response view.
     *
     * @return the saml 10 success response view
     */
    @RefreshScope
    @Bean(name="casSamlServiceSuccessView")
    public Saml10SuccessResponseView casSamlServiceSuccessView() {
        final Saml10SuccessResponseView view = new Saml10SuccessResponseView();
        view.setServicesManager(this.servicesManager);
        view.setCasAttributeEncoder(this.casAttributeEncoder);
        view.setIssuer(this.issuer);
        view.setSkewAllowance(this.skewAllowance);
        return view;
    }

    /**
     * Cas saml service failure view saml 10 failure response view.
     *
     * @return the saml 10 failure response view
     */
    @RefreshScope
    @Bean(name="casSamlServiceFailureView")
    public Saml10FailureResponseView casSamlServiceFailureView() {
        final Saml10FailureResponseView view = new Saml10FailureResponseView();
        view.setServicesManager(this.servicesManager);
        view.setCasAttributeEncoder(this.casAttributeEncoder);
        return view;
    }
    
}
