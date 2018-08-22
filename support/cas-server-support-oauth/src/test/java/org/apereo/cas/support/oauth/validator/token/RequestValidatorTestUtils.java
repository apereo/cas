package org.apereo.cas.support.oauth.validator.token;

import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Some utils for creating test services for request validator testing.
 *
 * @author Kirill Gagarski
 * @since 5.3.3
 */
public class RequestValidatorTestUtils {
    public static final String SUPPORTING_CLIENT_ID = "supporting";
    public static final String NON_SUPPORTING_CLIENT_ID = "nonsupporting";
    public static final String PROMISCUOUS_CLIENT_ID = "promiscuous";
    public static final String SHARED_SECRET = "secret";

    public static OAuthRegisteredService getService(final String serviceId,
                                                    final String name,
                                                    final String clientId,
                                                    final String clientSecret,
                                                    final Set<OAuth20GrantTypes> grantTypes) {
        final OAuthRegisteredService registeredService = new OAuthRegisteredService();
        registeredService.setName(name);
        registeredService.setClientId(clientId);
        registeredService.setClientSecret(clientSecret);
        registeredService.setServiceId(serviceId);
        registeredService.setSupportedGrantTypes(grantTypes.stream()
                .map(OAuth20GrantTypes::getType)
                .collect(Collectors.toCollection(HashSet::new)));
        return registeredService;
    }

    public static OAuthRegisteredService getPromiscousService(final String serviceId,
                                                              final String name,
                                                              final String clientId,
                                                              final String clientSecret) {
        return getService(serviceId, name, clientId, clientSecret, new HashSet<>());
    }
}
