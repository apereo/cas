package org.apereo.cas.adaptors.gauth.rest;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.adaptors.gauth.GoogleAuthenticatorTokenCredential;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.rest.factory.RestHttpRequestCredentialFactory;
import org.apereo.cas.util.CollectionUtils;
import org.springframework.util.MultiValueMap;

import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link GoogleAuthenticatorRestHttpRequestCredentialFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class GoogleAuthenticatorRestHttpRequestCredentialFactory implements RestHttpRequestCredentialFactory {

    /**
     * Parameter name expected in the request body to contain the GAuth token
     * based on which credential will be created.
     */
    public static final String PARAMETER_NAME_GAUTH_OTP = "gauthotp";
    
    @Override
    public List<Credential> fromRequest(final HttpServletRequest request, final MultiValueMap<String, String> requestBody) {
        if (requestBody == null || requestBody.isEmpty()) {
            LOGGER.debug("Skipping {} because the requestBody is null or empty", getClass().getSimpleName());
            return new ArrayList<>(0);
        }
        final String token = requestBody.getFirst(PARAMETER_NAME_GAUTH_OTP);
        LOGGER.debug("Google authenticator token in the request body: [{}]", token);
        if (StringUtils.isBlank(token)) {
            return new ArrayList<>(0);
        }
        return CollectionUtils.wrap(new GoogleAuthenticatorTokenCredential(token));
    }
}
