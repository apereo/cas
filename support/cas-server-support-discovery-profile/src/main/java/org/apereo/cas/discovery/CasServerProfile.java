package org.apereo.cas.discovery;

import java.util.Map;

/**
 * This is {@link CasServerProfile}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class CasServerProfile {
   
    private Map<String, Class> registeredServiceTypes;

    public Map<String, Class> getRegisteredServiceTypes() {
        return registeredServiceTypes;
    }

    public void setRegisteredServiceTypes(final Map<String, Class> registeredServiceTypes) {
        this.registeredServiceTypes = registeredServiceTypes;
    }
}
