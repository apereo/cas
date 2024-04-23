package org.apereo.cas.discovery;

import lombok.Getter;
import lombok.Setter;
import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashMap;
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
public class CasServerProfile implements Serializable {

    @Serial
    private static final long serialVersionUID = 1804693559797898008L;

    /**
     * The type of registered services that <i>are</i> supported by this CAS instance.
     */
    private Set<String> registeredServiceTypesSupported;

    /**
     * The type of multifactor authentication providers that <i>are</i> supported by this CAS instance.
     */
    private Map<String, String> multifactorAuthenticationProviderTypesSupported;

    /**
     * The list of available attributes currently active and configured in the CAS application context.
     */
    private Set<String> availableAttributes;

    /**
     * Collection of available authentication handlers.
     */
    private Set<String> availableAuthenticationHandlers;

    /**
     * Map of supported tickets from the catalog.
     */
    private Map<String, Map<String, Object>> ticketTypesSupported = new LinkedHashMap<>();

    /**
     * Profile details that can be augmented by other modules and extensions.
     */
    private Map<String, Object> details = new LinkedHashMap<>();
}
