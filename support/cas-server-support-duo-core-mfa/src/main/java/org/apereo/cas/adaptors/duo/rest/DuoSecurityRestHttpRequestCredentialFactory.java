package org.apereo.cas.adaptors.duo.rest;

import org.apereo.cas.adaptors.duo.authn.DuoSecurityDirectCredential;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityPasscodeCredential;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.metadata.BasicCredentialMetadata;
import org.apereo.cas.configuration.model.support.mfa.duo.DuoSecurityMultifactorAuthenticationProperties;
import org.apereo.cas.multitenancy.TenantDefinition;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.rest.factory.RestHttpRequestCredentialFactory;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.MultiValueMap;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link DuoSecurityRestHttpRequestCredentialFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Slf4j
@RequiredArgsConstructor
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

    protected final TenantExtractor tenantExtractor;
    
    @Override
    public List<Credential> fromRequest(final HttpServletRequest request,
                                        final MultiValueMap<String, String> requestBody) throws Throwable {
        if (requestBody == null || requestBody.isEmpty()) {
            LOGGER.debug("Skipping [{}] because the request body is null or empty", getClass().getSimpleName());
            return new ArrayList<>();
        }
        if (!requestBody.containsKey(RestHttpRequestCredentialFactory.PARAMETER_USERNAME) || !requestBody.containsKey(PARAMETER_NAME_PASSCODE)) {
            LOGGER.debug("No username or passcode provided");
            return new ArrayList<>();
        }
        val username = FunctionUtils.throwIfBlank(requestBody.getFirst(RestHttpRequestCredentialFactory.PARAMETER_USERNAME));
        val token = FunctionUtils.throwIfBlank(requestBody.getFirst(PARAMETER_NAME_PASSCODE));

        val providerId = StringUtils.defaultIfBlank(requestBody.getFirst(PARAMETER_NAME_PROVIDER),
            DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER);
        val credential = new DuoSecurityPasscodeCredential(username, token, providerId);
        val credentialMetadata = new BasicCredentialMetadata(credential);
        credentialMetadata.setTenant(tenantExtractor.extract(request).map(TenantDefinition::getId).orElse(null));
        return CollectionUtils.wrap(prepareCredential(request, credential));
    }

    @Override
    public List<Credential> fromAuthentication(final HttpServletRequest request,
                                               final MultiValueMap<String, String> requestBody,
                                               final Authentication authentication,
                                               final MultifactorAuthenticationProvider provider) {
        val principal = authentication.getPrincipal();
        val credential = new DuoSecurityDirectCredential(principal, provider.getId());
        val credentialMetadata = new BasicCredentialMetadata(credential);
        credentialMetadata.setTenant(tenantExtractor.extract(request).map(TenantDefinition::getId).orElse(null));
        return List.of(prepareCredential(request, credential));
    }
}
