package org.apereo.cas.web.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.validation.ValidationServiceSelectionStrategy;
import org.apereo.cas.web.view.Cas10ResponseView;
import org.apereo.cas.web.view.Cas20ResponseView;
import org.apereo.cas.web.view.Cas30JsonResponseView;
import org.apereo.cas.web.view.Cas30ResponseView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.View;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link CasValidationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casValidationConfiguration")
public class CasValidationConfiguration {

    @Resource(name = "defaultValidationServiceSelectionStrategy")
    private ValidationServiceSelectionStrategy defaultStrategy;

    @Autowired
    private CasConfigurationProperties casProperties;

    /**
     * Validation service selection strategies list.
     *
     * @return the list
     */
    @Bean
    public List<ValidationServiceSelectionStrategy> validationServiceSelectionStrategies() {
        final List<ValidationServiceSelectionStrategy> list = new ArrayList<>();
        list.add(this.defaultStrategy);
        return list;
    }

    @Bean
    public View cas1ServiceSuccessView() {
        return new Cas10ResponseView.Success();
    }

    @Bean
    public View cas1ServiceFailureView() {
        return new Cas10ResponseView.Failure();
    }

    @Bean
    public View cas2ServiceSuccessView() {
        return new Cas20ResponseView.Success();
    }

    @Bean
    public View cas3ServiceJsonView() {
        final Cas30JsonResponseView jsonResponseView = new Cas30JsonResponseView();
        jsonResponseView.setAuthenticationContextAttribute(casProperties.getMfa().getAuthenticationContextAttribute());
        jsonResponseView.setReleaseProtocolAttributes(casProperties.getView().getCas3().isReleaseProtocolAttributes());
        return jsonResponseView;
    }

    @Bean
    public View cas3ServiceSuccessView() {
        final Cas30ResponseView.Success s = new Cas30ResponseView.Success();
        s.setAuthenticationContextAttribute(casProperties.getMfa().getAuthenticationContextAttribute());
        s.setReleaseProtocolAttributes(casProperties.getView().getCas3().isReleaseProtocolAttributes());
        return s;
    }
}
