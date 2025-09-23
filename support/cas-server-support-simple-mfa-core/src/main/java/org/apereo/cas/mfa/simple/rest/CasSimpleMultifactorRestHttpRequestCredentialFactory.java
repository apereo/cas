package org.apereo.cas.mfa.simple.rest;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorTokenCredential;
import org.apereo.cas.rest.factory.RestHttpRequestCredentialFactory;
import org.apereo.cas.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.MultiValueMap;

import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link CasSimpleMultifactorRestHttpRequestCredentialFactory}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Slf4j
public class CasSimpleMultifactorRestHttpRequestCredentialFactory implements RestHttpRequestCredentialFactory {

    /**
     * Parameter name expected in the request body to contain the token
     * based on which credential will be created.
     */
    public static final String PARAMETER_NAME_CAS_SIMPLE_OTP = "sotp";

    @Override
    public List<Credential> fromRequest(final HttpServletRequest request, final MultiValueMap<String, String> requestBody) {
        if (requestBody == null || requestBody.isEmpty()) {
            LOGGER.debug("Skipping [{}] because the requestBody is null or empty", getClass().getSimpleName());
            return new ArrayList<>();
        }
        val token = requestBody.getFirst(PARAMETER_NAME_CAS_SIMPLE_OTP);
        if (StringUtils.isBlank(token)) {
            return new ArrayList<>();
        }
        val creds = new CasSimpleMultifactorTokenCredential(token);
        return CollectionUtils.wrap(prepareCredential(request, creds));
    }
}
