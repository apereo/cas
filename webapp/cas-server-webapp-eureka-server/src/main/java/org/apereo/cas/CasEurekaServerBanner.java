package org.apereo.cas;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.util.spring.boot.AbstractCasBanner;

/**
 * This is {@link CasEurekaServerBanner}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class CasEurekaServerBanner extends AbstractCasBanner {
    @Override
    protected String getTitle() {
        return "CAS Eureka Server";
    }
}
