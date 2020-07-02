package org.apereo.cas.gauth.rest;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.gauth.credential.GoogleAuthenticatorTokenCredential;
import org.apereo.cas.rest.factory.RestHttpRequestCredentialFactory;
import org.apereo.cas.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
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

    /**
     * Parameter name expected in the request body to contain the GAuth account id.
     */
    public static final String PARAMETER_NAME_GAUTH_ACCT = "gauthacct";

    @Override
    public List<Credential> fromRequest(final HttpServletRequest request, final MultiValueMap<String, String> requestBody) {
        if (requestBody == null || requestBody.isEmpty()) {
            LOGGER.debug("Skipping [{}] because the requestBody is null or empty", getClass().getSimpleName());
            return new ArrayList<>(0);
        }
        val token = requestBody.getFirst(PARAMETER_NAME_GAUTH_OTP);
        val id = requestBody.getFirst(PARAMETER_NAME_GAUTH_ACCT);
        LOGGER.debug("Google authenticator token [{}] in the request body via account [{}]", token, id);
        if (StringUtils.isBlank(token)) {
            return new ArrayList<>(0);
        }
        val creds = new GoogleAuthenticatorTokenCredential(token,
            StringUtils.isNotBlank(id) ? Long.valueOf(id) : null);
        return CollectionUtils.wrap(creds);
    }
}
