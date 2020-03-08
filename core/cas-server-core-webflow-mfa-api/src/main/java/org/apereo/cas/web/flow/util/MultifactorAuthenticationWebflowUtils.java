package org.apereo.cas.web.flow.util;

import org.apereo.cas.web.flow.configurer.CasMultifactorWebflowCustomizer;

import lombok.experimental.UtilityClass;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This is {@link MultifactorAuthenticationWebflowUtils}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@UtilityClass
public class MultifactorAuthenticationWebflowUtils {
    /**
     * Gets multifactor authentication webflow customizers.
     *
     * @param applicationContext the application context
     * @return the multifactor authentication webflow customizers
     */
    public static List<CasMultifactorWebflowCustomizer> getMultifactorAuthenticationWebflowCustomizers(
        final ConfigurableApplicationContext applicationContext) {
        return applicationContext.getBeansOfType(CasMultifactorWebflowCustomizer.class, false, true)
            .values()
            .stream()
            .sorted(AnnotationAwareOrderComparator.INSTANCE)
            .collect(Collectors.toList());
    }
}
