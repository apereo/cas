package org.apereo.cas.services;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apereo.cas.authentication.principal.OidcPairwisePersistentIdGenerator;
import org.apereo.cas.authentication.principal.PersistentIdGenerator;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriComponents;
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
public class PairwiseOidcRegisteredServiceUsernameAttributeProvider extends BaseRegisteredServiceUsernameAttributeProvider {
    private static final long serialVersionUID = 469929103943101717L;

    private static final Logger LOGGER = LoggerFactory.getLogger(PairwiseOidcRegisteredServiceUsernameAttributeProvider.class);

    private PersistentIdGenerator persistentIdGenerator = new OidcPairwisePersistentIdGenerator();

    public PairwiseOidcRegisteredServiceUsernameAttributeProvider() {
    }

    @Override
    public String resolveUsernameInternal(final Principal principal, final Service service, final RegisteredService registeredService) {
        if (registeredService == null || !OidcRegisteredService.class.isAssignableFrom(registeredService.getClass())) {
            LOGGER.warn("Service definition [{}] is undefined or it's not an OpenId Connect relying party", registeredService);
            return principal.getId();
        }

        final OidcRegisteredService oidcSvc = OidcRegisteredService.class.cast(registeredService);
        if (StringUtils.isBlank(oidcSvc.getSubjectType())
                || StringUtils.equalsIgnoreCase(OidcSubjectTypes.PUBLIC.getType(), oidcSvc.getSubjectType())) {
            LOGGER.warn("Service definition [{}] does not request a pairwise subject type", oidcSvc);
            return principal.getId();
        }


        final String sectorIdentifier = getSectorIdentifier(oidcSvc);
        if (StringUtils.isBlank(sectorIdentifier)) {
            LOGGER.debug("Service definition [{}] does not provide a sector identifier", oidcSvc);
            return principal.getId();
        }

        if (this.persistentIdGenerator == null) {
            throw new IllegalArgumentException("No pairwise persistent id generator is defined");
        }
        final String id = this.persistentIdGenerator.generate(principal, new PairwiseService(sectorIdentifier));
        LOGGER.debug("Resolved username [{}] for pairwise access", id);
        return id;
    }


    private String getSectorIdentifier(final OidcRegisteredService client) {
        if (!StringUtils.isBlank(client.getSectorIdentifierUri())) {
            final UriComponents uri = UriComponentsBuilder.fromUriString(client.getSectorIdentifierUri()).build();
            return uri.getHost();
        }
        final UriComponents uri = UriComponentsBuilder.fromUriString(client.getServiceId()).build();
        return uri.getHost();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final PairwiseOidcRegisteredServiceUsernameAttributeProvider rhs = (PairwiseOidcRegisteredServiceUsernameAttributeProvider) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        return builder
                .appendSuper(super.equals(obj))
                .append(this.persistentIdGenerator, rhs.persistentIdGenerator)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(persistentIdGenerator)
                .toHashCode();
    }

    private static class PairwiseService implements Service {
        private static final long serialVersionUID = -6154643329901712381L;
        private final String id;

        PairwiseService(final String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return this.id;
        }
    }
}
