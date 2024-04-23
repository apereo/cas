package org.apereo.cas.services;

import org.apereo.cas.configuration.model.support.mfa.BaseMultifactorAuthenticationProviderProperties;
import org.apereo.cas.configuration.support.ExpressionLanguageCapable;

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
    BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes getFailureMode();

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
     * Indicates whether multifactor authentication should be skipped.
     *
     * @return true/false
     */
    boolean isBypassEnabled();

    /**
     * Whether multifactor authentication should forcefully trigger,
     * even if the existing authentication context can be satisfied without MFA.
     *
     * @return true/false
     */
    boolean isForceExecution();

    /**
     * Whether multifactor authentication should bypass trusted device registration,
     * and check for device records and/or skip prompt for registration.
     *
     * @return true/false
     */
    boolean isBypassTrustedDeviceEnabled();

    /**
     * Gets principal attribute name trigger to enable bypass.
     *
     * @return the principal attribute name trigger
     */
    String getBypassPrincipalAttributeName();

    /**
     * Gets principal attribute value to match to enable bypass.
     * Values may be regex patterns.
     *
     * @return the principal attribute value to match
     */
    String getBypassPrincipalAttributeValue();

    /**
     * Indicates whether bypass should be enabled and activated
     * if the defined bypass principal attribute is not found.
     *
     * @return true/false
     */
    boolean isBypassIfMissingPrincipalAttribute();

    /**
     * Path to an external/embedded script
     * that allows for triggering of MFA.
     *
     * @return MFA trigger as a script path or embedded script.
     */
    @ExpressionLanguageCapable
    String getScript();
}
