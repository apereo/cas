package org.apereo.cas.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.CasEmbeddedValueResolver;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.util.StringValueResolver;

/**
 * This is {@link SchedulingUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@UtilityClass
public class SchedulingUtils {
    /**
     * Gets string value resolver.
     *
     * @param applicationContext the application context
     * @return the string value resolver
     */
    public static StringValueResolver prepScheduledAnnotationBeanPostProcessor(final ApplicationContext applicationContext) {
        final StringValueResolver resolver = new CasEmbeddedValueResolver(applicationContext);
        final ScheduledAnnotationBeanPostProcessor sch = applicationContext.getBean(ScheduledAnnotationBeanPostProcessor.class);
        sch.setEmbeddedValueResolver(resolver);
        return resolver;
    }
}
