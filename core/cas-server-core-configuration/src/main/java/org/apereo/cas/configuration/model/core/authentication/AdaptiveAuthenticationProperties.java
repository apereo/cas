package org.apereo.cas.configuration.model.core.authentication;

import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.HashMap;
import java.util.Map;

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

    @NestedConfigurationProperty
    private RiskBasedAuthenticationProperties risk = new RiskBasedAuthenticationProperties();

    private Map requireMultifactor = new HashMap<>();

    public RiskBasedAuthenticationProperties getRisk() {
        return risk;
    }

    public void setRisk(final RiskBasedAuthenticationProperties risk) {
        this.risk = risk;
    }

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

    public Map getRequireMultifactor() {
        return requireMultifactor;
    }

    public void setRequireMultifactor(final Map requireMultifactor) {
        this.requireMultifactor = requireMultifactor;
    }
}
