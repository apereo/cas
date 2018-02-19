package org.apereo.cas.util.spring;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
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
     * @param attributeFilter    the attribute filter
     * @return the beans with annotation
     */
    public static List<String> getBeansWithAnnotation(final ConfigurableApplicationContext applicationContext,
                                                      final Class<? extends Annotation> type, final Predicate<Map<String, Object>> attributeFilter) {
        final List<String> result = new ArrayList<>();
        final ConfigurableListableBeanFactory factory = applicationContext.getBeanFactory();
        for (final String name : factory.getBeanDefinitionNames()) {
            final BeanDefinition bd = factory.getBeanDefinition(name);

            if (bd.getSource() instanceof StandardMethodMetadata) {
                final StandardMethodMetadata metadata = (StandardMethodMetadata) bd.getSource();

                final Map<String, Object> attributes = metadata.getAnnotationAttributes(type.getName());
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
