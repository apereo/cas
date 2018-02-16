package org.apereo.cas.services;

import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;

import java.util.Map;

/**
 * This is {@link BaseSurrogateRegisteredServiceAccessStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public abstract class BaseSurrogateRegisteredServiceAccessStrategy extends DefaultRegisteredServiceAccessStrategy {
    private static final long serialVersionUID = -3975861635454453130L;

    /**
     * Is surrogate authentication session?.
     *
     * @param attributes the attributes
     * @return true /false
     */
    protected boolean isSurrogateAuthenticationSession(final Map<String, Object> attributes) {
        return attributes.containsKey(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_ENABLED);
    }
}
