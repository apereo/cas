package org.apereo.cas.adaptors.duo.rest;

import org.apereo.cas.adaptors.duo.authn.DuoSecurityDirectCredential;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityPasscodeCredential;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.configuration.model.support.mfa.DuoSecurityMultifactorAuthenticationProperties;
import org.apereo.cas.rest.factory.RestHttpRequestCredentialFactory;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.MultiValueMap;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link DuoSecurityRestHttpRequestCredentialFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Slf4j
public class DuoSecurityRestHttpRequestCredentialFactory implements RestHttpRequestCredentialFactory {

    /**
     * Parameter name expected in the request body to contain the token
     * based on which credential will be created.
     */
    public static final String PARAMETER_NAME_PASSCODE = "passcode";

    /**
     * Identifier for the duo mfa provider.
     */
    public static final String PARAMETER_NAME_PROVIDER = "provider";

    @Override
    public List<Credential> fromRequest(final HttpServletRequest request, final MultiValueMap<String, String> requestBody) {
        if (requestBody == null || requestBody.isEmpty()) {
            LOGGER.debug("Skipping [{}] because the request body is null or empty", getClass().getSimpleName());
            return new ArrayList<>(0);
        }
        if (!requestBody.containsKey(RestHttpRequestCredentialFactory.PARAMETER_USERNAME) || !requestBody.containsKey(PARAMETER_NAME_PASSCODE)) {
            LOGGER.debug("No username or passcode provided");
            return new ArrayList<>(0);
        }
        val username = FunctionUtils.throwIfBlank(requestBody.getFirst(RestHttpRequestCredentialFactory.PARAMETER_USERNAME));
        val token = FunctionUtils.throwIfBlank(requestBody.getFirst(PARAMETER_NAME_PASSCODE));

        val providerId = StringUtils.defaultString(requestBody.getFirst(PARAMETER_NAME_PROVIDER),
            DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER);
        return CollectionUtils.wrap(new DuoSecurityPasscodeCredential(username, token, providerId));
    }

    @Override
    public List<Credential> fromAuthentication(final HttpServletRequest request,
                                               final MultiValueMap<String, String> requestBody,
                                               final Authentication authentication,
                                               final MultifactorAuthenticationProvider provider) {
        val principal = authentication.getPrincipal();
        return List.of(new DuoSecurityDirectCredential(principal, provider.getId()));
    }
}
