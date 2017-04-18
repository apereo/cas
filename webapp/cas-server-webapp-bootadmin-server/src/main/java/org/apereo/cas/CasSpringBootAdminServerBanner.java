package org.apereo.cas;

import org.apereo.cas.util.spring.boot.AbstractCasBanner;

/**
 * This is {@link CasSpringBootAdminServerBanner}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class CasSpringBootAdminServerBanner extends AbstractCasBanner {
    @Override
    protected String getTitle() {
        return "CAS Admin Server";
    }
}
