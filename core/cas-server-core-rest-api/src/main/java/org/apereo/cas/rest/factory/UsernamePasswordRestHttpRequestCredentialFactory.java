package org.apereo.cas.rest.factory;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.RememberMeCredential;
import org.apereo.cas.authentication.credential.RememberMeUsernamePasswordCredential;
import org.apereo.cas.util.CollectionUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.MultiValueMap;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link UsernamePasswordRestHttpRequestCredentialFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@Getter
@Setter
public class UsernamePasswordRestHttpRequestCredentialFactory implements RestHttpRequestCredentialFactory {
    private int order = Integer.MIN_VALUE;

    @Override
    public List<Credential> fromRequest(final HttpServletRequest request, final MultiValueMap<String, String> requestBody) throws Throwable {
        if (requestBody == null || requestBody.isEmpty()) {
            LOGGER.debug("Skipping [{}] because the requestBody is null or empty", getClass().getSimpleName());
            return new ArrayList<>();
        }
        val username = requestBody.getFirst(RestHttpRequestCredentialFactory.PARAMETER_USERNAME);
        val password = requestBody.getFirst(RestHttpRequestCredentialFactory.PARAMETER_PASSWORD);
        val rememberMe = requestBody.getFirst(RememberMeCredential.REQUEST_PARAMETER_REMEMBER_ME);
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            LOGGER.debug("Invalid payload; missing required fields.");
            return new ArrayList<>();
        }
        val credential = new RememberMeUsernamePasswordCredential(BooleanUtils.toBoolean(rememberMe));
        credential.setUsername(username);
        credential.assignPassword(password);
        prepareCredential(request, credential);
        return CollectionUtils.wrap(credential);
    }
}
