package org.apereo.cas.oidc.issuer;

import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.RegexUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.pac4j.core.context.WebContext;
import java.util.List;
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
    public boolean validateIssuer(final WebContext webContext, final List<String> endpoints,
                                  final OidcRegisteredService registeredService) {
        val requestUrl = webContext.getRequestURL();
        val definedIssuer = determineIssuer(Optional.ofNullable(registeredService));
        val definedIssuerWithSlash = Strings.CI.appendIfMissing(definedIssuer, "/");
        
        val foundMatch = endpoints.stream().anyMatch(endpoint -> {
            val issuerFromRequestUrl = StringUtils.removeEnd(StringUtils.remove(requestUrl, '/' + endpoint), "/");
            return definedIssuer.equalsIgnoreCase(issuerFromRequestUrl)
                || issuerFromRequestUrl.startsWith(definedIssuerWithSlash)
                || definedIssuer.startsWith(issuerFromRequestUrl)
                || RegexUtils.find(properties.getCore().getAcceptedIssuersPattern(), issuerFromRequestUrl);
        });
        if (!foundMatch) {
            LOGGER.debug("Cannot accept issuer [{}] at [{}] for any of the endpoints [{}]",
                webContext.getRequestURL(), endpoints, definedIssuer);
        }
        return foundMatch;
    }
}
