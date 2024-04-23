package org.apereo.cas.util.spring.boot;

import org.springframework.core.env.Environment;

import java.util.Formatter;

/**
 * This is {@link BannerContributor}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@FunctionalInterface
public interface BannerContributor {
    /**
     * Contribute.
     *
     * @param formatter   the formatter
     * @param environment the environment
     */
    void contribute(Formatter formatter, Environment environment);
}
