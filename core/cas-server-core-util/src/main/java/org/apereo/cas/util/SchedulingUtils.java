package org.apereo.cas.util;

import org.apereo.cas.config.support.CasConfigurationEmbeddedValueResolver;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.util.StringValueResolver;

/**
 * This is {@link SchedulingUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public final class SchedulingUtils {
    private SchedulingUtils() {
    }

    /**
     * Gets string value resolver.
     *
     * @param applicationContext the application context
     * @return the string value resolver
     */
    public static StringValueResolver prepScheduledAnnotationBeanPostProcessor(final ApplicationContext applicationContext) {
        final StringValueResolver resolver = new CasConfigurationEmbeddedValueResolver(applicationContext);
        final ScheduledAnnotationBeanPostProcessor sch = applicationContext.getBean(ScheduledAnnotationBeanPostProcessor.class);
        sch.setEmbeddedValueResolver(resolver);
        return resolver;
    }
}
