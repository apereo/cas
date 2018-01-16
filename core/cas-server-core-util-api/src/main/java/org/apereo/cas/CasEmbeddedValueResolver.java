package org.apereo.cas;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.EmbeddedValueResolver;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.ConversionService;

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
        final String originalValue = super.resolveStringValue(strVal);

        final String value = convertValueToDurationIfPossible(originalValue);
        if (value != null) {
            return value;
        }
        return originalValue;
    }

    private String convertValueToDurationIfPossible(final String value) {
        try {
            final ConversionService service = applicationContext.getEnvironment().getConversionService();
            final Duration dur = service.convert(value, Duration.class);
            if (dur != null) {
                return String.valueOf(dur.toMillis());
            }
        } catch (final ConversionFailedException e) {
            LOGGER.trace(e.getMessage());
        }
        return null;
    }
}
