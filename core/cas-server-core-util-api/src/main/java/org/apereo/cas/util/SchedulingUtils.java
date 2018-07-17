package org.apereo.cas.util;

import org.apereo.cas.CasEmbeddedValueResolver;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
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
        val resolver = new CasEmbeddedValueResolver(applicationContext);
        try {
            val sch = applicationContext.getBean(ScheduledAnnotationBeanPostProcessor.class);
            sch.setEmbeddedValueResolver(resolver);
        } catch (final NoSuchBeanDefinitionException e) {
            LOGGER.warn("Unable to locate [ScheduledAnnotationBeanPostProcessor] as a bean. Support for duration syntax (i.e. PT2S) may not be available");
            LOGGER.trace(e.getMessage(), e);
        }
        return resolver;
    }
}
