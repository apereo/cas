package org.apereo.cas.web;

import org.apereo.cas.configuration.CasConfigurationPropertiesValidator;

import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.val;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;

/**
 * This is {@link CasWebApplicationContext}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@ToString
@NoArgsConstructor
public class CasWebApplicationContext extends AnnotationConfigServletWebServerApplicationContext {

    @Override
    protected void onRefresh() {
        if (!Boolean.getBoolean("SKIP_CONFIG_VALIDATION")) {
            val validator = new CasConfigurationPropertiesValidator(this);
            validator.validate();
        }
        super.onRefresh();
    }
}
