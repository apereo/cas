package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.authentication.principal.OidcPairwisePersistentIdGenerator;
import org.apereo.cas.authentication.principal.PersistentIdGenerator;
import org.apereo.cas.authentication.principal.ShibbolethCompatiblePersistentIdGenerator;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
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

    /**
     * The setting that defines the salt of the pairwise subject identifiers of clients whose generator has none.
     */
    public static final String PAIRWISE_SUBJECT_SALT_PROPERTY = "cas.authn.oidc.core.pairwise-subject-salt";

    @Serial
    private static final long serialVersionUID = 469929103943101717L;

    private static final AtomicBoolean UNSALTED_WARNING_LOGGED = new AtomicBoolean();

    private static final Set<String> UNCOPYABLE_GENERATORS_LOGGED = ConcurrentHashMap.newKeySet();

    private PersistentIdGenerator persistentIdGenerator = new OidcPairwisePersistentIdGenerator();

    /**
     * The pairwise subject salt of CAS, as defined when it is read, so that clients registered by CAS and the
     * identifiers calculated for them agree on it after the configuration is refreshed.
     *
     * @param environment the environment
     * @return the salt, or blank when none is defined
     */
    public static String getPairwiseSubjectSalt(final Environment environment) {
        return Objects.requireNonNullElse(SpringExpressionLanguageValueResolver.getInstance()
            .resolve(environment.getProperty(PAIRWISE_SUBJECT_SALT_PROPERTY, StringUtils.EMPTY)), StringUtils.EMPTY);
    }

    /**
     * A provider whose identifiers are calculated with the given salt.
     *
     * @param salt the salt
     * @return the provider
     */
    public static PairwiseOidcRegisteredServiceUsernameAttributeProvider withSalt(final String salt) {
        val generator = new OidcPairwisePersistentIdGenerator();
        generator.setSalt(salt);
        val provider = new PairwiseOidcRegisteredServiceUsernameAttributeProvider();
        provider.setPersistentIdGenerator(generator);
        return provider;
    }

    /**
     * Whether the client already calculates pairwise identifiers with a salt of its own, which must then be kept: a
     * generator without a salt makes up one when it is first used, which is not kept with the client, so its
     * identifiers change from one node or restart to the next.
     *
     * @param service the client
     * @return true or false
     */
    public static boolean hasSalt(final RegisteredService service) {
        return service.getUsernameAttributeProvider() instanceof final PairwiseOidcRegisteredServiceUsernameAttributeProvider provider
            && provider.getPersistentIdGenerator() instanceof final ShibbolethCompatiblePersistentIdGenerator generator
            && StringUtils.isNotBlank(generator.getSalt());
    }

    /**
     * Give the generator of the pairwise identifiers of the client the salt, when it is a generator without a salt of its
     * own, keeping the generator, its type and its attribute as they are; a generator that has a salt, or that does not
     * take one, is left alone.
     *
     * @param service the client
     * @param salt    the salt
     */
    public static void assignSaltIfMissing(final RegisteredService service, final String salt) {
        if (service.getUsernameAttributeProvider() instanceof final PairwiseOidcRegisteredServiceUsernameAttributeProvider provider
            && provider.getPersistentIdGenerator() instanceof final ShibbolethCompatiblePersistentIdGenerator generator
            && StringUtils.isBlank(generator.getSalt())) {
            generator.setSalt(salt);
        }
    }

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
        val id = resolvePersistentIdGenerator(oidcSvc, context.getApplicationContext()).generate(context.getPrincipal(), sectorIdentifier);
        LOGGER.debug("Resolved username [{}] for pairwise access", id);
        return id;
    }

    /**
     * The generator of the identifiers. A generator without a salt of its own, as clients that CAS registers are given
     * when the pairwise subject salt of CAS is defined, calculates them with that salt, which is thus never written
     * into client definitions; the generator itself is left as it is, and a copy of it, of the same type, calculates
     * them. A generator that cannot be copied, such as one that holds state that cannot be serialized, is replaced by a
     * default generator with the same attribute, which calculates identifiers as the default type does, without what the
     * type of the generator changes; that is logged as a warning once for each type. Without either salt, the generator
     * makes up one that is not kept, so the identifiers change from one node or restart to the next; that is logged as a
     * warning once, and for each client at debug level.
     *
     * @param client             the client
     * @param applicationContext the application context
     * @return the generator
     */
    private PersistentIdGenerator resolvePersistentIdGenerator(final OidcRegisteredService client, final ApplicationContext applicationContext) {
        if (persistentIdGenerator instanceof final ShibbolethCompatiblePersistentIdGenerator generator && StringUtils.isBlank(generator.getSalt())) {
            val salt = getPairwiseSubjectSalt(applicationContext.getEnvironment());
            if (StringUtils.isNotBlank(salt)) {
                val salted = copyOf(generator);
                salted.setSalt(salt);
                return salted;
            }
            if (UNSALTED_WARNING_LOGGED.compareAndSet(false, true)) {
                LOGGER.warn("Pairwise subject identifiers of some clients, such as [{}], have no salt, so they change from one node or "
                    + "restart to the next; define one for each client or with [{}]", client.getClientId(), PAIRWISE_SUBJECT_SALT_PROPERTY);
            }
            LOGGER.debug("Pairwise subject identifiers of [{}] have no salt", client.getClientId());
        }
        return persistentIdGenerator;
    }

    private static ShibbolethCompatiblePersistentIdGenerator copyOf(final ShibbolethCompatiblePersistentIdGenerator generator) {
        try {
            return SerializationUtils.clone(generator);
        } catch (final SerializationException e) {
            if (UNCOPYABLE_GENERATORS_LOGGED.add(generator.getClass().getName())) {
                LOGGER.warn("Pairwise subject identifier generator [{}] cannot be copied, so a default one with its attribute is used: [{}]",
                    generator.getClass().getName(), e.getMessage());
            }
            val copy = new OidcPairwisePersistentIdGenerator();
            copy.setAttribute(generator.getAttribute());
            return copy;
        }
    }

    /**
     * The sector of the client: the host of its sector identifier URI, or else of its first redirect URI. The redirect
     * URIs of a client registered by CAS are read from its service id, which matches them, and only a web redirect URI, an
     * {@code http} or {@code https} URI with an authority on a host other than a loopback one, gives a host; a service id
     * written by hand in the same quoted form is read the same way. Any other service id, such as one defined by hand or
     * one CAS built for a client it registered before it quoted redirect URIs, is read as it always was, a regular
     * expression included, until CAS builds it again when the client updates its registration. A client whose redirect
     * URIs name several hosts should register a sector identifier URI; a client whose service id or sector identifier URI
     * gives no host, such as a native application, is a sector of its own, its client id, so that it shares identifiers
     * with no other such client.
     *
     * @param client the client
     * @return the sector identifier
     */
    private static String getSectorIdentifier(final OidcRegisteredService client) {
        if (StringUtils.isNotBlank(client.getSectorIdentifierUri())) {
            val uri = UriComponentsBuilder.fromUriString(client.getSectorIdentifierUri()).build();
            return StringUtils.defaultIfBlank(uri.getHost(), client.getClientId());
        }
        val redirectUris = OidcRedirectUriUtils.toRedirectUris(client.getServiceId());
        val hosts = redirectUris.equals(List.of(client.getServiceId()))
            ? readHost(client.getServiceId()).stream().toList()
            : redirectUris.stream().map(OidcRedirectUriUtils::toSectorHost).flatMap(Optional::stream).distinct().toList();
        if (hosts.size() > 1) {
            LOGGER.warn("Redirect URIs of [{}] name several hosts [{}] and no sector identifier URI; [{}] is used as the sector identifier",
                client.getClientId(), hosts, hosts.getFirst());
        }
        return hosts.isEmpty() ? client.getClientId() : hosts.getFirst();
    }

    private static Optional<String> readHost(final String serviceId) {
        try {
            return Optional.ofNullable(UriComponentsBuilder.fromUriString(serviceId).build().getHost()).filter(StringUtils::isNotBlank);
        } catch (final IllegalArgumentException e) {
            LOGGER.trace("Service id [{}] names no host: [{}]", serviceId, e.getMessage());
            return Optional.empty();
        }
    }

}
