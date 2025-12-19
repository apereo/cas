package org.apereo.cas.configuration.model.core.authentication.risk;

import module java.base;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link RiskBasedAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiresModule(name = "cas-server-support-electrofence")
@Getter
@Setter
@Accessors(chain = true)
public class RiskBasedAuthenticationProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 3826749727400569308L;

    /**
     * Handle risky authentication attempts via an IP criteria.
     */
    @NestedConfigurationProperty
    private RiskBasedAuthenticationIpAddressProperties ip = new RiskBasedAuthenticationIpAddressProperties();

    /**
     * Handle risky authentication attempts via a user-agent criteria.
     */
    @NestedConfigurationProperty
    private RiskBasedAuthenticationUserAgentProperties agent = new RiskBasedAuthenticationUserAgentProperties();

    /**
     * Handle risky authentication attempts via a device fingerprint criteria.
     */
    @NestedConfigurationProperty
    private RiskBasedAuthenticationDeviceFingerprintProperties deviceFingerprint = new RiskBasedAuthenticationDeviceFingerprintProperties();

    /**
     * Handle risky authentication attempts via geolocation criteria.
     */
    @NestedConfigurationProperty
    private RiskBasedAuthenticationGeoLocationProperties geoLocation = new RiskBasedAuthenticationGeoLocationProperties();

    /**
     * Handle risky authentication attempts via an date/time criteria.
     */
    @NestedConfigurationProperty
    private RiskBasedAuthenticationDateTimeProperties dateTime = new RiskBasedAuthenticationDateTimeProperties();

    /**
     * Design how responses should be handled, in the event
     * that an authentication event is deemed risky.
     */
    @NestedConfigurationProperty
    private RiskBasedAuthenticationResponseProperties response = new RiskBasedAuthenticationResponseProperties();

    /**
     * Core configuration settings for assessing risky authentication attempts.
     */
    @NestedConfigurationProperty
    private RiskBasedAuthenticationCoreProperties core = new RiskBasedAuthenticationCoreProperties();

}
