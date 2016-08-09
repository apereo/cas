package org.apereo.cas.configuration.model.core.authentication;

/**
 * This is {@link AdaptiveAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class AdaptiveAuthenticationProperties {
    
    private String rejectCountries;
    private String rejectBrowsers;
    private String rejectIpAddresses;

    public String getRejectIpAddresses() {
        return rejectIpAddresses;
    }

    public void setRejectIpAddresses(final String rejectIpAddresses) {
        this.rejectIpAddresses = rejectIpAddresses;
    }

    public String getRejectCountries() {
        return rejectCountries;
    }

    public void setRejectCountries(final String rejectCountries) {
        this.rejectCountries = rejectCountries;
    }

    public String getRejectBrowsers() {
        return rejectBrowsers;
    }

    public void setRejectBrowsers(final String rejectBrowsers) {
        this.rejectBrowsers = rejectBrowsers;
    }
}
