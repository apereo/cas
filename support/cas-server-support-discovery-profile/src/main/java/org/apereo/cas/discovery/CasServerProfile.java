package org.apereo.cas.discovery;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.Set;

/**
 * This is {@link CasServerProfile}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Getter
@Setter
public class CasServerProfile {

    /**
     * The type of registered services that <i>are</i> supported by this CAS instance.
     */
    private Map<String, Class> registeredServiceTypesSupported;

    /**
     * The type of multifactor authentication providers that <i>are</i> supported by this CAS instance.
     */
    private Map<String, String> multifactorAuthenticationProviderTypesSupported;

    /**
     * The type of delegated clients that <i>are</i> supported by this CAS instance.
     */
    private Set<String> delegatedClientTypesSupported;

    /**
     * The list of available attributes currently active and configured in the CAS application context.
     */
    private Set<String> availableAttributes;
}
