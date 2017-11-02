package org.apereo.cas.discovery;

import java.util.Map;

/**
 * This is {@link CasServerProfile}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
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
    
    public Map<String, Class> getRegisteredServiceTypesSupported() {
        return registeredServiceTypesSupported;
    }

    public void setRegisteredServiceTypesSupported(final Map<String, Class> registeredServiceTypesSupported) {
        this.registeredServiceTypesSupported = registeredServiceTypesSupported;
    }

    public Map<String, String> getMultifactorAuthenticationProviderTypesSupported() {
        return multifactorAuthenticationProviderTypesSupported;
    }

    public void setMultifactorAuthenticationProviderTypesSupported(final Map<String, String> multifactorAuthenticationProviderTypesSupported) {
        this.multifactorAuthenticationProviderTypesSupported = multifactorAuthenticationProviderTypesSupported;
    }

    public Map<String, Class> getRegisteredServiceTypes() {
        return registeredServiceTypes;
    }

    public void setRegisteredServiceTypes(final Map<String, Class> registeredServiceTypes) {
        this.registeredServiceTypes = registeredServiceTypes;
    }

    public Map<String, String> getMultifactorAuthenticationProviderTypes() {
        return multifactorAuthenticationProviderTypes;
    }

    public void setMultifactorAuthenticationProviderTypes(final Map<String, String> multifactorAuthenticationProviderTypes) {
        this.multifactorAuthenticationProviderTypes = multifactorAuthenticationProviderTypes;
    }
}
