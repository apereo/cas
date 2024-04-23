package org.apereo.cas.support.oauth.validator;

import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * This is {@link DefaultOAuth20ClientSecretValidator}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public class DefaultOAuth20ClientSecretValidator implements OAuth20ClientSecretValidator {
    private final CipherExecutor<Serializable, String> cipherExecutor;

    @Override
    public boolean validate(final OAuthRegisteredService registeredService, final String clientSecret) {
        if (isClientSecretUndefined(registeredService)) {
            LOGGER.debug("The client secret is not defined for the registered service [{}]", registeredService.getName());
            return true;
        }
        val clientSecretDecoded = EncodingUtils.urlDecode(clientSecret);
        val definedSecret = cipherExecutor.decode(registeredService.getClientSecret(), new Object[]{registeredService});
        if (!StringUtils.equals(definedSecret, clientSecretDecoded)) {
            LOGGER.error("Wrong client secret for service: [{}]. Using PKCE does not require a client secret and "
                       + "requests generally must not specify a client secret to CAS.\nFurthermore, you must make sure "
                       + "no client secret is assigned to this registered service in the CAS service registry.",
                        registeredService.getServiceId());
            return false;
        }
        return true;
    }

    @Override
    public boolean isClientSecretExpired(final OAuthRegisteredService registeredService) {
        return false;
    }
    
    protected boolean isClientSecretUndefined(final OAuthRegisteredService registeredService) {
        return registeredService != null && StringUtils.isBlank(registeredService.getClientSecret());
    }
}
