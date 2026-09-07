package org.apereo.cas.util.app;

import module java.base;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.metrics.ApplicationStartup;
import org.springframework.core.metrics.jfr.FlightRecorderApplicationStartup;

/**
 * This is {@link ApplicationUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@UtilityClass
public class ApplicationUtils {
    /**
     * System property that selects the startup instrumentation strategy.
     */
    public static final String SYSTEM_PROPERTY_APP_STARTUP = "CAS_APP_STARTUP";

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
     * Startup instrumentation is opt-in: recording every bean instantiation, configuration
     * class parse and condition evaluation costs time and memory on every deployment, while
     * the data it produces is only reachable through the {@code startup} actuator endpoint.
     * Deployments that want to profile the startup sequence must ask for it explicitly via
     * the {@link #SYSTEM_PROPERTY_APP_STARTUP} system property.
     *
     * @return the application startup
     */
    public static ApplicationStartup getApplicationStartup() {
        val type = StringUtils.defaultIfBlank(System.getProperty(SYSTEM_PROPERTY_APP_STARTUP), "default");
        return switch (type.toLowerCase(Locale.ENGLISH)) {
            case "jfr" -> new FlightRecorderApplicationStartup();
            case "buffering" -> new BufferingApplicationStartup(APPLICATION_EVENTS_CAPACITY);
            default -> ApplicationStartup.DEFAULT;
        };
    }

}
