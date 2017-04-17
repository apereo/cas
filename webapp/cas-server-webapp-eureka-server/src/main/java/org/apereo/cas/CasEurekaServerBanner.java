package org.apereo.cas;

import org.apereo.cas.util.spring.boot.AbstractCasBanner;

/**
 * This is {@link CasEurekaServerBanner}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class CasEurekaServerBanner extends AbstractCasBanner {
    @Override
    protected String getTitle() {
        return "CAS Eureka Server";
    }
}
