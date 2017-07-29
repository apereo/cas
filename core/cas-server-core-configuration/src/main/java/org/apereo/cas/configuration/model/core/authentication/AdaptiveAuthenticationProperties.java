package org.apereo.cas.configuration.model.core.authentication;

import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link AdaptiveAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class AdaptiveAuthenticationProperties implements Serializable {

    private static final long serialVersionUID = -1840174229142982880L;
    /**
     * Comma-separated list of strings representing countries to be rejected from participating in authentication transactions.
     */
    private String rejectCountries;

    /**
     * Comma-separated list of strings representing browser user agents to be rejected from participating in authentication transactions.
     */
    private String rejectBrowsers;

    /**
     * Comma-separated list of strings representing IP addresses to be rejected from participating in authentication transactions.
     */
    private String rejectIpAddresses;

    @NestedConfigurationProperty
    private RiskBasedAuthenticationProperties risk = new RiskBasedAuthenticationProperties();

    /**
     * A map of (mfaProviderId -&gt; adaptiveRegexPattern) that tells CAS when to trigger an MFA authentication transaction.
     *
     * This property binds a valid mfa provider to an adaptive regex pattern representing either IP address, user-agent or geolocation.
     * When either of those collected pieces of adaptive data matches configured regex pattern during authentication event,
     * an MFA authentication transaction is triggered for an MFA provider represented by the map's key.
     *
     * Default value is EMPTY Map.
     */
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
