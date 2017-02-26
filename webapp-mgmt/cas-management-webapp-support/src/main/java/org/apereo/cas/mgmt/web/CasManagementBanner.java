package org.apereo.cas.mgmt.web;

import org.apereo.cas.util.spring.boot.AbstractCasBanner;

/**
 * This is {@link CasManagementBanner}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class CasManagementBanner extends AbstractCasBanner {
    @Override
    protected String getTitle() {
        return "CAS Management";
    }
}
