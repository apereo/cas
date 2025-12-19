package org.apereo.cas.util.spring.boot;

import module java.base;
import lombok.val;
import org.springframework.boot.Banner;
import org.springframework.core.env.Environment;

/**
 * This is {@link CasBanner}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
public interface CasBanner extends Banner {
    /**
     * Gets title.
     *
     * @return the title
     */
    String getTitle();

    /**
     * Inject environment info.
     *
     * @param formatter   the formatter
     * @param environment the environment
     * @param sourceClass the source class
     */
    default void injectEnvironmentInfo(final Formatter formatter,
                                       final Environment environment,
                                       final Class<?> sourceClass) {
        if (environment.getActiveProfiles().length > 0) {
            formatter.format("Active Profiles: %s%n", String.join(",", environment.getActiveProfiles()));
        }
    }

    /**
     * Gets cas banner instance.
     *
     * @return the cas banner instance
     */
    static CasBanner getInstance() {
        val subTypes = ServiceLoader.load(CasBanner.class)
            .stream()
            .map(ServiceLoader.Provider::get)
            .toList();
        return subTypes.isEmpty() ? new DefaultCasBanner() : subTypes.getFirst();
    }
}
