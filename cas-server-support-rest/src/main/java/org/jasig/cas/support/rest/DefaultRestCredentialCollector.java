package org.jasig.cas.support.rest;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

/**
 * The {@link DefaultRestCredentialCollector} is the default
 * collector which extracts the username/password from the request body.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Component("defaultRestCredentialCollector")
public class DefaultRestCredentialCollector implements RestCredentialCollector {

    private static final long serialVersionUID = -8843635354983695870L;

    @Override
    public Credential collect(final MultiValueMap<String, String> requestBody) {
        final String uid = requestBody.getFirst("username");
        final String psw = requestBody.getFirst("password");

        if (StringUtils.isBlank(uid) || StringUtils.isBlank(psw)) {
            throw new IllegalArgumentException("Username/Password must be defined in the REST request");
        }
        return new UsernamePasswordCredential(uid, psw);
    }
}
