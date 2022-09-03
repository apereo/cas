package org.apereo.cas;

import org.apereo.cas.util.ReflectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.logging.LoggingInitialization;
import org.apereo.cas.util.spring.boot.CasBanner;
import org.apereo.cas.util.spring.boot.DefaultCasBanner;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;
import org.springframework.core.metrics.ApplicationStartup;
import org.springframework.core.metrics.jfr.FlightRecorderApplicationStartup;

import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

/**
 * This is {@link CasEmbeddedContainerUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@UtilityClass
public class CasEmbeddedContainerUtils {
    private static final int APPLICATION_EVENTS_CAPACITY = 5_000;

    /**
     * Gets logging initialization.
     *
     * @return the logging initialization
     */
    public static Optional<LoggingInitialization> getLoggingInitialization() {
        return FunctionUtils.doUnchecked(() -> {
            val packageName = CasEmbeddedContainerUtils.class.getPackage().getName();

            val subTypes = ReflectionUtils.findSubclassesInPackage(LoggingInitialization.class, packageName);
            return subTypes.isEmpty()
                ? Optional.empty()
                : Optional.of(subTypes.iterator().next().getDeclaredConstructor().newInstance());
        });
    }

    /**
     * Gets cas banner instance.
     *
     * @return the cas banner instance
     */
    public static CasBanner getCasBannerInstance() {
        val subTypes = ServiceLoader.load(CasBanner.class).stream()
            .map(ServiceLoader.Provider::get)
            .collect(Collectors.toList());
        return subTypes.isEmpty() ? new DefaultCasBanner() : subTypes.get(0);
    }

    /**
     * Gets application startup.
     *
     * @return the application startup
     */
    public static ApplicationStartup getApplicationStartup() {
        val type = StringUtils.defaultIfBlank(System.getProperty("CAS_APP_STARTUP"), "default");
        if (StringUtils.equalsIgnoreCase("jfr", type)) {
            return new FlightRecorderApplicationStartup();
        }
        if (StringUtils.equalsIgnoreCase("buffering", type)) {
            return new BufferingApplicationStartup(APPLICATION_EVENTS_CAPACITY);
        }
        return ApplicationStartup.DEFAULT;
    }

}
