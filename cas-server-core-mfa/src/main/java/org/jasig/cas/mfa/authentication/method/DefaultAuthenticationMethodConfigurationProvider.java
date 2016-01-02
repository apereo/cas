package org.jasig.cas.mfa.authentication.method;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * The {@link DefaultAuthenticationMethodConfigurationProvider} is the default
 * implementation of the {@link AuthenticationMethodConfigurationProvider}.
 * Methods are mapped in memory via a static map.
 *
 * @author Misagh Moayyed
 * @since 4.3
 */
@Component("defaultAuthenticationMethodProvider")
public final class DefaultAuthenticationMethodConfigurationProvider implements AuthenticationMethodConfigurationProvider {

    private final Map<String, Integer> authenticationMethodsMap;

    /**
     * Instantiates a new Default authentication method configuration provider.
     *
     * @param authenticationMethodsMap the authentication methods map
     */
    @Autowired(required=false)
    public DefaultAuthenticationMethodConfigurationProvider(@Qualifier("authenticationMethodsMap") final Map authenticationMethodsMap) {
        this.authenticationMethodsMap = authenticationMethodsMap;
    }

    @Override
    public boolean contains(final String name) {
        return get(name) != null;
    }

    @Override
    public AuthenticationMethod get(final String name) {
        if (this.authenticationMethodsMap.containsKey(name)) {
            final Integer rank = this.authenticationMethodsMap.get(name);
            return new AuthenticationMethod(name, rank);
        }
        return null;
    }
}

