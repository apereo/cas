package org.apereo.cas.discovery;

import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import lombok.Getter;

/**
 * This is {@link CasServerProfile}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@Getter
public class CasServerProfile {

    /**
     * The type of registered services currently active and configured in the CAS application context.
     */
    private Map<String, Class> registeredServiceTypes;

    /**
     * The type of multifactor authentication providers currently active and configured in the CAS application context.
     */
    private Map<String, String> multifactorAuthenticationProviderTypes;

    /**
     * The type of registered services that <i>could be</i> supported by CAS.
     * This indicates the capability and capacity of the server; NOT that the feature is necessarily currently active.
     */
    private Map<String, Class> registeredServiceTypesSupported;

    /**
     * The type of multifactor authentication providers that <i>could be</i> supported by CAS.
     * This indicates the capability and capacity of the server; NOT that the feature is necessarily currently active.
     */
    private Map<String, String> multifactorAuthenticationProviderTypesSupported;

    public void setRegisteredServiceTypesSupported(final Map<String, Class> registeredServiceTypesSupported) {
        this.registeredServiceTypesSupported = registeredServiceTypesSupported;
    }

    public void setMultifactorAuthenticationProviderTypesSupported(final Map<String, String> multifactorAuthenticationProviderTypesSupported) {
        this.multifactorAuthenticationProviderTypesSupported = multifactorAuthenticationProviderTypesSupported;
    }

    public void setRegisteredServiceTypes(final Map<String, Class> registeredServiceTypes) {
        this.registeredServiceTypes = registeredServiceTypes;
    }

    public void setMultifactorAuthenticationProviderTypes(final Map<String, String> multifactorAuthenticationProviderTypes) {
        this.multifactorAuthenticationProviderTypes = multifactorAuthenticationProviderTypes;
    }
}
