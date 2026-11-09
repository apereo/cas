package org.apereo.cas.configuration.model.support.oidc.federation;

import module java.base;
import org.apereo.cas.configuration.model.support.oidc.jwks.OidcJsonWebKeyStoreRevocationProperties;
import org.apereo.cas.configuration.model.support.oidc.jwks.OidcJsonWebKeyStoreRotationProperties;
import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link OidcFederationProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@RequiresModule(name = "cas-server-support-oidc")
@Getter
@Setter
@Accessors(chain = true)
public class OidcFederationProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 113328615694269278L;

    /**
     * The role of this server.
     */
    @RequiredProperty
    private OidcFederationRole role;

    /**
     * Organization name to include in the federation metadata.
     */
    @RequiredProperty
    private String organization = "Apereo CAS";

    /**
     * List of contacts to include in the federation metadata.
     */
    @RequiredProperty
    private List<String> contacts = new ArrayList<>();

    /**
     * The List of federation authorities where this entity is enrolled, i.e. its immediate superiors.
     * This is used to generate the federation metadata. When no trust anchors are defined, the entries
     * are also considered trust anchors when validating incoming federation requests from relying parties.
     */
    @RequiredProperty
    private List<String> authorityHints = new ArrayList<>();

    /**
     * The trust anchors whose trust chains an OpenID provider accepts when relying parties register
     * automatically, and that an intermediate resolves entities to at its resolve endpoint, with their keys obtained
     * out of band. When none are defined, the authority hints are used as trust anchors and their keys are taken from
     * their own entity configuration.
     */
    private List<OidcFederationTrustAnchorProperties> trustAnchors = new ArrayList<>();

    /**
     * The maximum number of authority hints followed from any entity configuration while resolving
     * the trust chains of a relying party that registers automatically. An entity that lists more
     * authority hints is refused, which bounds the requests a made-up entity can cause.
     */
    private int maxAuthorityHints = 5;

    /**
     * The maximum number of intermediate entities between a relying party that registers automatically
     * and a trust anchor. A negative value removes the limit.
     */
    private int maxPathLength = 3;

    /**
     * The maximum number of requests a single resolution may send: entity configurations, subordinate statements,
     * historical keys and trust mark status requests, counted together across the trust chains of the entity and those
     * of the issuers of its trust marks. Statements already fetched in the same resolution are reused and not counted
     * again, and neither are the historical keys of trust anchors, which are kept for a while. A resolution that needs
     * more fails, which bounds what a single client id or resolve request can cost.
     */
    private int maxRequestsPerResolution = 50;

    /**
     * The longest time a single resolution may take, all of its requests together; every response is also limited in
     * size. A resolution that takes longer fails, so that slow or unresponsive entities cannot hold the resolutions
     * that may run at the same time.
     */
    @DurationCapable
    private String resolutionTimeout = "PT10S";

    /**
     * The maximum number of resolutions that run at the same time, for automatic registration and the resolve endpoint
     * together. A resolution that cannot start within a second fails without being remembered as unresolved.
     */
    private int maxConcurrentResolutions = 10;

    /**
     * How long an entity identifier whose trust chains could not be resolved is remembered.
     * Within that time, authorization requests that carry it as client id do not cause trust chains
     * to be resolved again, and a registration whose refresh failed is kept as it is. A trust anchor or intermediate
     * also remembers the entities its resolve endpoint resolved, or could not resolve, for at most that time.
     */
    @DurationCapable
    private String unresolvedEntityRetention = "PT5M";

    /**
     * Trust marks issued to this entity, published in its entity configuration. Each entry is a trust mark,
     * a signed JWT as obtained from its trust mark issuer, given as a resource such as a file or a URL, or the JWT itself.
     * Resources are read each time the entity configuration is produced, so a renewed trust mark is published
     * without a restart. Trust marks that are not about this entity or have expired are not published.
     */
    @ExpressionLanguageCapable
    private List<String> trustMarks = new ArrayList<>();

    /**
     * Trust mark types that a relying party must hold to be registered automatically by this OpenID provider.
     * For each type, the relying party's entity configuration must carry a valid trust mark of that type,
     * issued by an issuer that the trust anchor of its trust chain accepts for that type.
     */
    private List<String> requiredTrustMarks = new ArrayList<>();

    /**
     * For a trust anchor, the trust mark types of the federation and the entities accepted as their issuers,
     * published as {@code trust_mark_issuers} in its entity configuration.
     */
    private List<OidcFederationTrustMarkIssuerProperties> trustMarkIssuers = new ArrayList<>();

    /**
     * For a trust anchor, the owners of trust mark types whose issuance is delegated to trust mark issuers,
     * published as {@code trust_mark_owners} in its entity configuration.
     */
    private List<OidcFederationTrustMarkOwnerProperties> trustMarkOwners = new ArrayList<>();

    /**
     * Trust marks this entity issues as a trust mark issuer, each type with the entities it is issued to.
     * When defined, this entity publishes its trust mark, trust mark status and trust marked entities listing
     * endpoints.
     */
    private List<OidcFederationIssuedTrustMarkProperties> issuedTrustMarks = new ArrayList<>();

    /**
     * Client authentication at the trust mark endpoint of this entity as a trust mark issuer. Authenticated requests
     * use {@code POST} and {@code private_key_jwt}, signed with the federation keys of a subordinate of this entity
     * as its subordinate definition holds them, and may only ask for the trust marks issued to the requester itself.
     * Accepted values are {@code NONE}, {@code OPTIONAL} and {@code REQUIRED}.
     */
    private OidcFederationEndpointAuthenticationModes trustMarkEndpointAuthentication = OidcFederationEndpointAuthenticationModes.NONE;

    /**
     * Client authentication at the resolve endpoint of this entity as a trust anchor or intermediate. Authenticated
     * requests use {@code POST} and {@code private_key_jwt}, signed with the federation keys of a subordinate of this
     * entity as its subordinate definition holds them, and their resolve responses are addressed to the requester.
     * Accepted values are {@code NONE}, {@code OPTIONAL} and {@code REQUIRED}.
     */
    private OidcFederationEndpointAuthenticationModes resolveEndpointAuthentication = OidcFederationEndpointAuthenticationModes.NONE;

    /**
     * Rotation of the federation keys: the future key becomes current and starts signing, the current key
     * becomes previous, and a new future key is published.
     */
    @NestedConfigurationProperty
    private OidcJsonWebKeyStoreRotationProperties rotation = new OidcJsonWebKeyStoreRotationProperties();

    /**
     * Revocation of the federation keys: previous keys are no longer published in the entity configuration
     * and are published by the historical keys endpoint instead.
     */
    @NestedConfigurationProperty
    private OidcJsonWebKeyStoreRevocationProperties revocation = new OidcJsonWebKeyStoreRevocationProperties();

    /**
     * Path to the JWKS file resource used to handle signing of federation metadata.
     * Note that if the keystore file does not exist at the specified path, one will be generated for you,
     * with a current and a future key. Keys are rotated and retired in this file, so it must be writable
     * when rotation or revocation is used.
     */
    @RequiredProperty
    @ExpressionLanguageCapable
    private String jwksFile = "file:/etc/cas/config/federation-keystore.jwks";

    /**
     * Sets the time period during which the entity statement is not expired.
     */
    @DurationCapable
    private String entityStatementExpiration = "P90D";

    /**
     * For a TA or intermediate, the file directory where the subordinates are stored.
     */
    private String subordinateDirectory;
}
