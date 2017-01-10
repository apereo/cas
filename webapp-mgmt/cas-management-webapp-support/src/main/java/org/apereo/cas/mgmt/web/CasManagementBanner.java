package org.apereo.cas.mgmt.web;

import org.apereo.cas.util.spring.boot.CasBanner;

/**
 * This is {@link CasManagementBanner}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class CasManagementBanner extends CasBanner {
    @Override
    protected String getTitle() {
        return "CAS Management";
    }
}
