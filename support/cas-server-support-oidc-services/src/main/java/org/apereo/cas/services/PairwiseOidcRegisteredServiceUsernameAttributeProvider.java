package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.authentication.principal.OidcPairwisePersistentIdGenerator;
import org.apereo.cas.authentication.principal.PersistentIdGenerator;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.Nullable;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * This is {@link PairwiseOidcRegisteredServiceUsernameAttributeProvider}.
 * This provides a different sub value to each Client, so as not to enable
 * Clients to correlate the End-User's activities without permission.
 * When pairwise Subject Identifiers are used, the OpenID Provider MUST calculate a unique sub
 * (subject) value for each Sector Identifier. The Subject Identifier value
 * MUST NOT be reversible by any party other than the OpenID Provider.
 * <p>
 * If the client has not provided a value for {@code sector_identifier_uri} in
 * dynamic client Registration, the sector identifier used for
 * pairwise identifier calculation is the host component of the registered {@code redirect_uri}.
 * If there are multiple host names in the registered {@code redirect_uri}s,
 * the Client MUST register a {@code sector_identifier_uri}.
 * When a {@code sector_identifier_uri} is provided, the host component of that URL is used as the sector
 * identifier for the pairwise identifier calculation. The value of the {@code sector_identifier_uri} MUST
 * be a URL using the https scheme that points to a JSON file containing an array of redirect_uri values.
 * The values of the registered {@code redirect_uri}s MUST be included in the elements of the array.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@Getter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Setter
public class PairwiseOidcRegisteredServiceUsernameAttributeProvider extends BaseRegisteredServiceUsernameAttributeProvider {

    @Serial
    private static final long serialVersionUID = 469929103943101717L;

    private PersistentIdGenerator persistentIdGenerator = new OidcPairwisePersistentIdGenerator();

    @Override
    public String resolveUsernameInternal(final RegisteredServiceUsernameProviderContext context) {
        if (context.getRegisteredService() == null || !OidcRegisteredService.class.isAssignableFrom(context.getRegisteredService().getClass())) {
            LOGGER.warn("Service definition [{}] is undefined or it's not an OpenId Connect relying party", context.getRegisteredService());
            return context.getPrincipal().getId();
        }
        val oidcSvc = (OidcRegisteredService) context.getRegisteredService();
        if (StringUtils.isBlank(oidcSvc.getSubjectType())
            || Strings.CI.equals(OidcSubjectTypes.PUBLIC.getType(), oidcSvc.getSubjectType())) {
            LOGGER.warn("Service definition [{}] does not request a pairwise subject type", oidcSvc);
            return context.getPrincipal().getId();
        }
        val sectorIdentifier = getSectorIdentifier(oidcSvc);
        val id = this.persistentIdGenerator.generate(context.getPrincipal(), sectorIdentifier);
        LOGGER.debug("Resolved username [{}] for pairwise access", id);
        return id;
    }

    /**
     * The host of the sector identifier URI of the client, or else of its first redirect URI. The redirect URIs of a
     * client registered by CAS are read from its service id, which matches them; any other service id is read as the
     * redirect URI. A client whose redirect URIs name several hosts should register a sector identifier URI; one whose
     * registered redirect URIs name no host, such as a native application with a private-use scheme, is its own sector.
     *
     * @param client the client
     * @return the sector identifier
     */
    private static @Nullable String getSectorIdentifier(final OidcRegisteredService client) {
        if (StringUtils.isNotBlank(client.getSectorIdentifierUri())) {
            val uri = UriComponentsBuilder.fromUriString(client.getSectorIdentifierUri()).build();
            return uri.getHost();
        }
        val redirectUris = OidcRedirectUriUtils.toRedirectUris(client.getServiceId());
        val hosts = redirectUris.stream()
            .map(redirectUri -> UriComponentsBuilder.fromUriString(redirectUri).build().getHost())
            .filter(StringUtils::isNotBlank)
            .distinct()
            .toList();
        if (hosts.size() > 1) {
            LOGGER.warn("Redirect URIs of [{}] name several hosts [{}] and no sector identifier URI; [{}] is used as the sector identifier",
                client.getClientId(), hosts, hosts.getFirst());
        }
        if (hosts.isEmpty()) {
            return redirectUris.equals(List.of(client.getServiceId())) ? null : client.getClientId();
        }
        return hosts.getFirst();
    }

}
