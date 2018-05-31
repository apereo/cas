package org.apereo.cas.util.spring;

import com.google.common.base.Predicates;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.type.StandardMethodMetadata;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * This is {@link AnnotationUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@UtilityClass
public class AnnotationUtils {

    /**
     * Gets beans with annotation.
     *
     * @param applicationContext the application context
     * @param type               the type
     * @return the beans with annotation
     */
    public static List<String> getBeansWithAnnotation(final ConfigurableApplicationContext applicationContext,
                                                      final Class<? extends Annotation> type) {
        return getBeansWithAnnotation(applicationContext, type, Predicates.alwaysTrue());
    }

    /**
     * Gets beans with annotation.
     *
     * @param applicationContext the application context
     * @param type               the type
     * @param attributeFilter    the attribute filter
     * @return the beans with annotation
     */
    public static List<String> getBeansWithAnnotation(final ConfigurableApplicationContext applicationContext,
                                                      final Class<? extends Annotation> type, final Predicate<Map<String, Object>> attributeFilter) {
        final List<String> result = new ArrayList<>();
        final var factory = applicationContext.getBeanFactory();
        for (final var name : factory.getBeanDefinitionNames()) {
            final var bd = factory.getBeanDefinition(name);

            if (bd.getSource() instanceof StandardMethodMetadata) {
                final var metadata = (StandardMethodMetadata) bd.getSource();

                final var attributes = metadata.getAnnotationAttributes(type.getName());
                if (null == attributes) {
                    continue;
                }

                if (attributeFilter.test(attributes)) {
                    result.add(name);
                }
            }
        }

        return result;
    }
}
