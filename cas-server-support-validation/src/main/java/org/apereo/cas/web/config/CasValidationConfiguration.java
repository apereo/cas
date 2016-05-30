package org.apereo.cas.web.config;

import org.apereo.cas.validation.ValidationServiceSelectionStrategy;
import org.apereo.cas.web.view.Cas10ResponseView;
import org.apereo.cas.web.view.Cas20ResponseView;
import org.apereo.cas.web.view.Cas30JsonResponseView;
import org.apereo.cas.web.view.Cas30ResponseView;
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
    
    @Resource(name="defaultValidationServiceSelectionStrategy")
    private ValidationServiceSelectionStrategy defaultStrategy;

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
        return new Cas30JsonResponseView();
    }
    
    @Bean
    public View cas3ServiceSuccessView() {
        return new Cas30ResponseView.Success();
    }
}
