package org.apereo.cas.util.app;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.metrics.ApplicationStartup;
import org.springframework.core.metrics.jfr.FlightRecorderApplicationStartup;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

/**
 * This is {@link ApplicationUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@UtilityClass
public class ApplicationUtils {
    private static final int APPLICATION_EVENTS_CAPACITY = 5_000;

    /**
     * Gets application initialization components.
     *
     * @return the initialization components
     */
    public static List<ApplicationEntrypointInitializer> getApplicationEntrypointInitializers() {
        return ServiceLoader.load(ApplicationEntrypointInitializer.class)
            .stream()
            .map(ServiceLoader.Provider::get)
            .filter(Objects::nonNull)
            .sorted(AnnotationAwareOrderComparator.INSTANCE)
            .collect(Collectors.toList());
    }

    /**
     * Gets application startup.
     *
     * @return the application startup
     */
    public static ApplicationStartup getApplicationStartup() {
        val type = StringUtils.defaultIfBlank(System.getProperty("CAS_APP_STARTUP"), "default");
        if (Strings.CI.equals("jfr", type)) {
            return new FlightRecorderApplicationStartup();
        }
        if (Strings.CI.equals("buffering", type)) {
            return new BufferingApplicationStartup(APPLICATION_EVENTS_CAPACITY);
        }
        return ApplicationStartup.DEFAULT;
    }

}
