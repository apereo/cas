package org.apereo.cas.configuration.model.core.authentication;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link AdaptiveAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class AdaptiveAuthenticationProperties {
    
    private List<String> rejectCountries = new ArrayList<>();
    private List<String> rejectBrowsers = new ArrayList<>();

    public List<String> getRejectCountries() {
        return rejectCountries;
    }

    public void setRejectCountries(final List<String> rejectCountries) {
        this.rejectCountries = rejectCountries;
    }

    public List<String> getRejectBrowsers() {
        return rejectBrowsers;
    }

    public void setRejectBrowsers(final List<String> rejectBrowsers) {
        this.rejectBrowsers = rejectBrowsers;
    }
}
