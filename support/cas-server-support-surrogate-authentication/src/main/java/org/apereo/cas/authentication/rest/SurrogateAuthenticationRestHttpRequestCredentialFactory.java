package org.apereo.cas.authentication.rest;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.SurrogateAuthenticationException;
import org.apereo.cas.authentication.SurrogateUsernamePasswordCredential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.configuration.model.support.surrogate.SurrogateAuthenticationProperties;
import org.apereo.cas.rest.factory.UsernamePasswordRestHttpRequestCredentialFactory;
import org.apereo.cas.util.CollectionUtils;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.MultiValueMap;

import javax.servlet.http.HttpServletRequest;

import java.util.List;

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

    /**
     * Extract credential surrogate username password.
     *
     * @param request     the request
     * @param credentials the credentials
     * @return the surrogate username password credential
     * @throws Exception the exception
     */
    protected SurrogateUsernamePasswordCredential extractCredential(final HttpServletRequest request,
                                                                    final List<Credential> credentials) throws Exception {
        val sc = new SurrogateUsernamePasswordCredential();
        val credential = UsernamePasswordCredential.class.cast(credentials.get(0));
        BeanUtils.copyProperties(sc, credential);

        val surrogatePrincipal = request.getHeader(REQUEST_HEADER_SURROGATE_PRINCIPAL);
        if (StringUtils.isNotBlank(surrogatePrincipal)) {
            LOGGER.debug("Request surrogate principal [{}]", surrogatePrincipal);
            sc.setSurrogateUsername(surrogatePrincipal);
            return sc;
        }
        val username = credential.getUsername();
        if (username.contains(properties.getSeparator())) {
            val surrogateUsername = username.substring(0, username.indexOf(properties.getSeparator()));
            val realUsername = username.substring(username.indexOf(properties.getSeparator()) + properties.getSeparator().length());
            sc.setUsername(realUsername);
            sc.setSurrogateUsername(surrogateUsername);
            sc.setPassword(credential.getPassword());
            return sc;
        }
        return null;
    }

    @Override
    public int getOrder() {
        return super.getOrder() - 1;
    }

    @SneakyThrows
    @Override
    public List<Credential> fromRequest(final HttpServletRequest request, final MultiValueMap<String, String> requestBody) {
        val credentials = super.fromRequest(request, requestBody);
        if (credentials.isEmpty()) {
            return credentials;
        }
        val credential = extractCredential(request, credentials);
        if (credential == null) {
            LOGGER.trace("Not a surrogate authentication attempt, returning parent class credentials");
            return credentials;
        }
        val surrogateAccounts = surrogateAuthenticationService.getEligibleAccountsForSurrogateToProxy(credential.getId());
        if (!surrogateAccounts.contains(credential.getSurrogateUsername())) {
            throw new SurrogateAuthenticationException("Unable to authorize surrogate authentication request for " + credential.getSurrogateUsername());
        }
        return CollectionUtils.wrapList(credential);
    }
}
