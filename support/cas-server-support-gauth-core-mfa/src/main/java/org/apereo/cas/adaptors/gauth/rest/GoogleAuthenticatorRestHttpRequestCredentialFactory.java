package org.apereo.cas.adaptors.gauth.rest;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.adaptors.gauth.GoogleAuthenticatorTokenCredential;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.rest.RestHttpRequestCredentialFactory;
import org.apereo.cas.util.CollectionUtils;
import org.springframework.util.MultiValueMap;

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


    @Override
    public List<Credential> fromRequestBody(final MultiValueMap<String, String> requestBody) {
        final String gauthotp = requestBody.getFirst("gauthotp");
        LOGGER.debug("Google authenticator token in the request body: [{}]", gauthotp);
        if (StringUtils.isBlank(gauthotp)) {
            return new ArrayList<>(0);
        }
        return CollectionUtils.wrap(new GoogleAuthenticatorTokenCredential(gauthotp));
    }
}
