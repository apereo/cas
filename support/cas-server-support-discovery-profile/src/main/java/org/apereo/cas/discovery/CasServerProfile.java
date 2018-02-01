package org.apereo.cas.discovery;

import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link CasServerProfile}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@Getter
@Setter
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

    /**
     * The type of delegated clients that <i>could be</i> supported by CAS.
     * This indicates the capability and capacity of the server; NOT that the feature is necessarily currently active.
     */
    private Set<String> delegatedClientTypesSupported;

    /**
     * The type of delegated clients that <i>are</i> supported by CAS and configured.
     */
    private Set<String> delegatedClientTypes;
}
