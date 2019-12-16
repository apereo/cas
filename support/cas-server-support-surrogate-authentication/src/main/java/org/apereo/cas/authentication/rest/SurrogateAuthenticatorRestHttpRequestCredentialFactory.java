package org.apereo.cas.authentication.rest;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.SurrogateAuthenticationException;
import org.apereo.cas.authentication.SurrogateUsernamePasswordCredential;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
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
 * This is {@link SurrogateAuthenticatorRestHttpRequestCredentialFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class SurrogateAuthenticatorRestHttpRequestCredentialFactory extends UsernamePasswordRestHttpRequestCredentialFactory {
    private final SurrogateAuthenticationService surrogateAuthenticationService;

    @Override
    public int getOrder() {
        return super.getOrder() - 1;
    }

    @SneakyThrows
    @Override
    public List<Credential> fromRequest(final HttpServletRequest request, final MultiValueMap<String, String> requestBody) {
        val credentials = super.fromRequest(request, requestBody);
        val surrogatePrincipal = request.getHeader("X-Surrogate-Principal");
        LOGGER.debug("Request surrogate principal [{}]", surrogatePrincipal);
        
        if (credentials.isEmpty() || StringUtils.isBlank(surrogatePrincipal)) {
            return credentials;
        }
        val credential = credentials.get(0);
        val surrogateAccounts = surrogateAuthenticationService.getEligibleAccountsForSurrogateToProxy(credential.getId());
        if (!surrogateAccounts.contains(surrogatePrincipal)) {
            throw new SurrogateAuthenticationException("Unable to authorize surrogate authentication request for " + surrogatePrincipal);
        }
        val sc = new SurrogateUsernamePasswordCredential();
        BeanUtils.copyProperties(sc, credential);
        sc.setSurrogateUsername(surrogatePrincipal);
        return CollectionUtils.wrapList(sc);
    }
}
