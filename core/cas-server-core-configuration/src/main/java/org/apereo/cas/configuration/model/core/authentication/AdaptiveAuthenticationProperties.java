package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.support.RequiresModule;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link AdaptiveAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
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

    /**
     * Control settings that handle and calculate risky authentication attempts.
     */
    @NestedConfigurationProperty
    private RiskBasedAuthenticationProperties risk = new RiskBasedAuthenticationProperties();

    /**
     * A map of ({@code mfaProviderId -> adaptiveRegexPattern}) that tells CAS when to trigger an MFA authentication transaction.
     * <p>
     * This property binds a valid mfa provider to an adaptive regex pattern representing either IP address, user-agent or geolocation.
     * When either of those collected pieces of adaptive data matches configured regex pattern during authentication event,
     * an MFA authentication transaction is triggered for an MFA provider represented by the map's key.
     * <p>
     * Default value is EMPTY Map.
     */
    private Map<String, String> requireMultifactor = new HashMap<>();

    /**
     * This property binds a valid mfa provider to a collection of rules that deal with triggering mfa
     * based on that provider based on properties of date/time. One may want to force mfa during weekends,
     * after hours, etc and the ruleset provides a modest configuration set where time can also be treated as trigger.
     */
    private List<TimeBasedAuthenticationProperties> requireTimedMultifactor = new ArrayList<>();

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

    public Map<String, String> getRequireMultifactor() {
        return requireMultifactor;
    }

    public void setRequireMultifactor(final Map<String, String> requireMultifactor) {
        this.requireMultifactor = requireMultifactor;
    }

    public List<TimeBasedAuthenticationProperties> getRequireTimedMultifactor() {
        return requireTimedMultifactor;
    }

    public void setRequireTimedMultifactor(final List<TimeBasedAuthenticationProperties> requireTimedMultifactor) {
        this.requireTimedMultifactor = requireTimedMultifactor;
    }
}
