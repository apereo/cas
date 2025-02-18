package org.apereo.cas.oidc.issuer;

import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.WebContext;

import java.util.Optional;

/**
 * This is {@link OidcDefaultIssuerService}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiredArgsConstructor
@Slf4j
public class OidcDefaultIssuerService implements OidcIssuerService {
    protected final OidcProperties properties;
    protected final TenantExtractor tenantExtractor;

    @Override
    public String determineIssuer(final Optional<OidcRegisteredService> registeredService) {
        val issuer = registeredService
            .filter(svc -> StringUtils.isNotBlank(svc.getIdTokenIssuer()))
            .map(OidcRegisteredService::getIdTokenIssuer)
            .orElseGet(() -> properties.getCore().getIssuer());
        LOGGER.trace("Determined issuer as [{}] for [{}]", issuer,
            registeredService.map(RegisteredService::getName).orElse("CAS"));
        return StringUtils.removeEnd(issuer, "/");
    }

    @Override
    public boolean validateIssuer(final WebContext webContext, final String endpoint) {
        val requestUrl = webContext.getRequestURL();
        val issuerFromRequestUrl = StringUtils.removeEnd(StringUtils.remove(requestUrl, '/' + endpoint), "/");
        val definedIssuer = determineIssuer(Optional.empty());
        val definedIssuerWithSlash = StringUtils.appendIfMissing(definedIssuer, "/");
        val result = definedIssuer.equalsIgnoreCase(issuerFromRequestUrl)
                     || issuerFromRequestUrl.startsWith(definedIssuerWithSlash)
                     || RegexUtils.find(properties.getCore().getAcceptedIssuersPattern(), issuerFromRequestUrl);
        FunctionUtils.doIf(!result, o -> LOGGER.trace("Configured issuer [{}] defined does not match the request issuer [{}]",
            o, issuerFromRequestUrl)).accept(definedIssuer);
        return result;
    }
}
