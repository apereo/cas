package org.apereo.cas;

import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.logging.LoggingInitialization;
import org.apereo.cas.util.spring.boot.AbstractCasBanner;
import org.apereo.cas.util.spring.boot.DefaultCasBanner;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.springframework.boot.Banner;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;
import org.springframework.core.metrics.ApplicationStartup;
import org.springframework.core.metrics.jfr.FlightRecorderApplicationStartup;

import java.util.Optional;

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
    @SneakyThrows
    public static Optional<LoggingInitialization> getLoggingInitialization() {
        val packageName = CasEmbeddedContainerUtils.class.getPackage().getName();
        val reflections = new Reflections(new ConfigurationBuilder()
            .filterInputsBy(new FilterBuilder().includePackage(packageName))
            .setUrls(ClasspathHelper.forPackage(packageName)));
        
        val subTypes = reflections.getSubTypesOf(LoggingInitialization.class);
        return subTypes.isEmpty()
            ? Optional.empty()
            : Optional.of(subTypes.iterator().next().getDeclaredConstructor().newInstance());
    }

    /**
     * Gets cas banner instance.
     *
     * @return the cas banner instance
     */
    public static Banner getCasBannerInstance() {
        val packageName = CasEmbeddedContainerUtils.class.getPackage().getName();
        val reflections = new Reflections(new ConfigurationBuilder()
            .filterInputsBy(new FilterBuilder().includePackage(packageName))
            .setExpandSuperTypes(true)
            .setUrls(ClasspathHelper.forPackage(packageName)));
        
        val subTypes = reflections.getSubTypesOf(AbstractCasBanner.class);
        subTypes.remove(DefaultCasBanner.class);

        if (subTypes.isEmpty()) {
            return new DefaultCasBanner();
        }
        try {
            val clz = subTypes.iterator().next();
            return clz.getDeclaredConstructor().newInstance();
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return new DefaultCasBanner();
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
