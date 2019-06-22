package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.Set;

/**
 * This is {@link RegisteredServiceMultifactorPolicy} that describes how a service
 * should handle authentication requests.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface RegisteredServiceMultifactorPolicy extends Serializable {
    /**
     * Gets MFA authentication provider id.
     *
     * @return the authentication provider id
     */
    Set<String> getMultifactorAuthenticationProviders();

    /**
     * Gets failure mode.
     *
     * @return the failure mode
     */
    RegisteredServiceMultifactorPolicyFailureModes getFailureMode();

    /**
     * Gets principal attribute name trigger.
     *
     * @return the principal attribute name trigger
     */
    String getPrincipalAttributeNameTrigger();

    /**
     * Gets principal attribute value to match.
     * Values may be regex patterns.
     *
     * @return the principal attribute value to match
     */
    String getPrincipalAttributeValueToMatch();

    /**
     * Indicates whether authentication should be skipped.
     *
     * @return true/false
     */
    boolean isBypassEnabled();

    /**
     * Whether multifactor authentication should forcefully trigger,
     * even if the existing authentication context can be satisfied without MFA.
     * @return true/false
     */
    boolean isForceExecution();

    /**
     * Whether multifactor authentication should bypass trusted device registration,
     * and check for device records and/or skip prompt for registration.
     * @return true/false
     */
    boolean isBypassTrustedDeviceEnabled();
}
