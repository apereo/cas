package org.jasig.cas.web.config;

import org.jasig.cas.validation.ValidationServiceSelectionStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    
    @Autowired
    @Qualifier("defaultValidationServiceSelectionStrategy")
    private ValidationServiceSelectionStrategy defaultStrategy;

    /**
     * Validation service selection strategies list.
     *
     * @return the list
     */
    @Bean(name="validationServiceSelectionStrategies")
    public List<ValidationServiceSelectionStrategy> validationServiceSelectionStrategies() {
        final List<ValidationServiceSelectionStrategy> list = new ArrayList<>();
        list.add(this.defaultStrategy);
        return list;
    }
}
