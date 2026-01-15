package org.apereo.cas.authentication.rest;

import module java.base;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.MutableCredential;
import org.apereo.cas.authentication.SurrogateAuthenticationException;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.authentication.surrogate.SurrogateCredentialTrait;
import org.apereo.cas.configuration.model.support.surrogate.SurrogateAuthenticationProperties;
import org.apereo.cas.rest.factory.UsernamePasswordRestHttpRequestCredentialFactory;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.util.MultiValueMap;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link SurrogateAuthenticationRestHttpRequestCredentialFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class SurrogateAuthenticationRestHttpRequestCredentialFactory extends UsernamePasswordRestHttpRequestCredentialFactory {

    /**
     * Header to be extracted from the request
     * that indicates the surrogate/substitute principal name.
     */
    public static final String REQUEST_HEADER_SURROGATE_PRINCIPAL = "X-Surrogate-Principal";

    private final SurrogateAuthenticationService surrogateAuthenticationService;

    private final SurrogateAuthenticationProperties properties;

    @Override
    public int getOrder() {
        return super.getOrder() - 1;
    }

    @Override
    public List<Credential> fromRequest(final HttpServletRequest request, final MultiValueMap<@NonNull String, String> requestBody) throws Throwable {
        val credentials = super.fromRequest(request, requestBody);
        if (credentials.isEmpty()) {
            return credentials;
        }
        val credential = FunctionUtils.doUnchecked(() -> extractCredential(request, credentials));
        if (credential == null) {
            LOGGER.trace("Not a surrogate authentication attempt, returning parent class credentials");
            return credentials;
        }
        val surrogateAccounts = surrogateAuthenticationService.getImpersonationAccounts(credential.getId(), Optional.empty());
        val surrogateUsername = credential.getCredentialMetadata().getTrait(SurrogateCredentialTrait.class)
            .map(SurrogateCredentialTrait::getSurrogateUsername)
            .orElseThrow();
        if (!surrogateAccounts.contains(surrogateUsername)) {
            throw new SurrogateAuthenticationException(
                "Unable to authorize surrogate authentication request for " + surrogateUsername);
        }
        return CollectionUtils.wrapList(prepareCredential(request, credential));
    }

    protected MutableCredential extractCredential(final HttpServletRequest request,
                                                  final List<Credential> credentials) {
        val credential = (MutableCredential) credentials.getFirst();
        if (credential != null) {
            var surrogateUsername = request.getHeader(REQUEST_HEADER_SURROGATE_PRINCIPAL);
            if (StringUtils.isNotBlank(surrogateUsername)) {
                LOGGER.debug("Request surrogate principal [{}]", surrogateUsername);
                credential.getCredentialMetadata().addTrait(new SurrogateCredentialTrait(surrogateUsername));
                return credential;
            }

            val username = credential.getId();
            val separator = properties.getCore().getSeparator();
            if (username.contains(separator)) {
                surrogateUsername = username.substring(0, username.indexOf(separator));
                val realUsername = username.substring(username.indexOf(separator) + separator.length());
                credential.setId(realUsername);
                credential.getCredentialMetadata().addTrait(new SurrogateCredentialTrait(surrogateUsername));
                return credential;
            }
        }
        return null;
    }
}
