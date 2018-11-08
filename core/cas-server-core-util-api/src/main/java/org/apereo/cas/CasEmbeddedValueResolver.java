package org.apereo.cas;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.config.EmbeddedValueResolver;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.convert.ConversionFailedException;

import java.time.Duration;

/**
 * This is {@link CasEmbeddedValueResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class CasEmbeddedValueResolver extends EmbeddedValueResolver {

    private final ConfigurableApplicationContext applicationContext;

    public CasEmbeddedValueResolver(final ApplicationContext applicationContext) {
        super(((ConfigurableApplicationContext) applicationContext).getBeanFactory());
        this.applicationContext = ConfigurableApplicationContext.class.cast(applicationContext);
    }

    @Override
    public String resolveStringValue(final String strVal) {
        val originalValue = super.resolveStringValue(strVal);

        val value = convertValueToDurationIfPossible(originalValue);
        if (value != null) {
            return value;
        }
        return originalValue;
    }

    private String convertValueToDurationIfPossible(final String value) {
        try {
            val service = applicationContext.getEnvironment().getConversionService();
            val dur = service.convert(value, Duration.class);
            if (dur != null) {
                return String.valueOf(dur.toMillis());
            }
        } catch (final ConversionFailedException e) {
            LOGGER.trace(e.getMessage());
        }
        return null;
    }
}
