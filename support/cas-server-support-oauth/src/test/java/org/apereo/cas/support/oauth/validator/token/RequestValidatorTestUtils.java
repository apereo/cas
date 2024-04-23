package org.apereo.cas.support.oauth.validator.token;

import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;

import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Some utils for creating test services for request validator testing.
 *
 * @author Kirill Gagarski
 * @since 5.3.3
 */
@UtilityClass
public class RequestValidatorTestUtils {

    public static final String SHARED_SECRET = "secret";

    public static OAuthRegisteredService getService(final String serviceId,
                                                    final String name,
                                                    final String clientId,
                                                    final String clientSecret,
                                                    final Set<OAuth20GrantTypes> grantTypes) {
        val registeredService = new OAuthRegisteredService();
        registeredService.setName(name);
        registeredService.setClientId(clientId);
        registeredService.setClientSecret(clientSecret);
        registeredService.setServiceId(serviceId);
        registeredService.setSupportedGrantTypes(grantTypes.stream()
            .map(OAuth20GrantTypes::getType)
            .collect(Collectors.toCollection(HashSet::new)));
        return registeredService;
    }

    public static OAuthRegisteredService getPromiscuousService(final String serviceId,
                                                               final String name,
                                                               final String clientId,
                                                               final String clientSecret) {
        return getService(serviceId, name, clientId, clientSecret, new LinkedHashSet<>());
    }
}
