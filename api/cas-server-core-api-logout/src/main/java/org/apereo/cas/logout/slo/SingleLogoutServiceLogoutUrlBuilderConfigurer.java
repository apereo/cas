package org.apereo.cas.logout.slo;

import org.springframework.core.Ordered;

/**
 * This is {@link SingleLogoutServiceLogoutUrlBuilderConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@FunctionalInterface
public interface SingleLogoutServiceLogoutUrlBuilderConfigurer extends Ordered {

    /**
     * Configure builder.
     *
     * @return the single logout service logout url builder
     */
    SingleLogoutServiceLogoutUrlBuilder configureBuilder();

    @Override
    default int getOrder() {
        return 0;
    }
}
