package org.apereo.cas.oidc.vc.issuer.status;

import module java.base;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.vc.issuer.OidcVerifiableCredentialSigningUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.util.CompressionUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.RandomUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.jspecify.annotations.Nullable;

/**
 * Keeps one transient session ticket per status list entry, identified by its status list and index and expiring with
 * the credential, so the ticket registry decides whether an index is taken and holds the entry's status. Indexes are
 * drawn at random from {@code [0, size)} of the first status list with room, and a status list token is rebuilt from the
 * registry with 2 bits per entry and cached for its {@code ttl}. A registry that cannot keep tickets, the stateless ticket
 * registry, gives credentials no status: neither revocation nor a unique index could be guaranteed.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
@Slf4j
public class OidcVerifiableCredentialDefaultStatusListService implements OidcVerifiableCredentialStatusListService {
    private static final int BITS = 2;

    private static final int ALLOCATION_ATTEMPTS = 16;

    private static final long MAXIMUM_STATUS_LISTS = 10_000;

    private static final String TICKET_ID_PREFIX = "vcstatus-";

    private static final String PROPERTY_STATUS_LIST = "statusListId";

    private static final String PROPERTY_INDEX = "statusListIndex";

    private static final String PROPERTY_STATUS = "statusListStatus";

    private static final String PROPERTY_PRINCIPAL = "statusListPrincipal";

    private static final String PROPERTY_CREDENTIAL_CONFIGURATION_ID = "statusListCredentialConfigurationId";

    private static final String PROPERTY_CREDENTIAL_ID = "statusListCredentialId";

    private static final String PROPERTY_EXPIRES_AT = "statusListExpiresAt";

    private final OidcConfigurationContext configurationContext;

    private final Map<String, CachedStatusListToken> statusListTokens = new ConcurrentHashMap<>();

    @Override
    public Optional<StatusReference> allocate(final OAuth20AccessToken accessToken, final String principal,
                                              final String credentialConfigurationId, final String credentialId,
                                              final Duration validity)
            throws Throwable {
        val properties = configurationContext.getCasProperties().getAuthn().getOidc().getVc().getIssuer().getStatusList();
        if (!properties.isEnabled()) {
            return Optional.empty();
        }
        val size = Math.max(1, properties.getSize());
        for (var statusList = 1L; statusList <= MAXIMUM_STATUS_LISTS; statusList++) {
            for (var attempt = 0; attempt < ALLOCATION_ATTEMPTS; attempt++) {
                val index = RandomUtils.nextInt(0, size);
                val ticketId = toTicketId(String.valueOf(statusList), index);
                if (configurationContext.getTicketRegistry().getTicket(ticketId) == null) {
                    return createEntry(ticketId, String.valueOf(statusList), index, accessToken, principal,
                        credentialConfigurationId, credentialId, validity);
                }
            }
        }
        LOGGER.error("No status list has room for another entry; the credential is issued without status");
        return Optional.empty();
    }

    @Override
    public Optional<String> buildStatusListToken(final String statusListId) throws Throwable {
        if (!isValidStatusListId(statusListId)) {
            return Optional.empty();
        }
        val cached = statusListTokens.get(statusListId);
        if (cached != null && cached.expiresAt().isAfter(Instant.now(Clock.systemUTC()))) {
            return Optional.of(cached.token());
        }
        val properties = configurationContext.getCasProperties().getAuthn().getOidc().getVc().getIssuer().getStatusList();
        val size = Math.max(1, properties.getSize());
        val statuses = new byte[(size * BITS + Byte.SIZE - 1) / Byte.SIZE];
        var entries = 0;
        try (val tickets = configurationContext.getTicketRegistry().getTickets(ticket -> isEntryOf(ticket, statusListId))) {
            for (val ticket : tickets.toList()) {
                val entry = (TransientSessionTicket) ticket;
                val index = Long.parseLong(Objects.requireNonNull(entry.getProperty(PROPERTY_INDEX, String.class)));
                val status = StatusType.valueOf(entry.getProperty(PROPERTY_STATUS, String.class)).getValue();
                if (index >= 0 && index < size) {
                    statuses[(int) (index * BITS / Byte.SIZE)] |= (byte) (status << (index * BITS % Byte.SIZE));
                    entries++;
                }
            }
        }
        if (entries == 0) {
            return Optional.empty();
        }
        val timeToLive = Beans.newDuration(properties.getTimeToLive());
        val issuedAt = NumericDate.now();
        val claims = new JwtClaims();
        claims.setSubject(toStatusListUri(statusListId));
        claims.setIssuedAt(issuedAt);
        claims.setExpirationTime(NumericDate.fromSeconds(issuedAt.getValue() + Beans.newDuration(properties.getExpiration()).toSeconds()));
        claims.setClaim("ttl", timeToLive.toSeconds());
        claims.setClaim("status_list", Map.of("bits", BITS, "lst", EncodingUtils.encodeUrlSafeBase64(CompressionUtils.deflate(statuses, false)),
            "aggregation_uri", toStatusListAggregationUri()));
        val token = sign(claims);
        statusListTokens.put(statusListId, new CachedStatusListToken(token, Instant.now(Clock.systemUTC()).plus(timeToLive)));
        return Optional.of(token);
    }

    @Override
    public Optional<StatusType> getStatus(final String uri, final long index) {
        val prefix = toStatusListUri(StringUtils.EMPTY);
        if (!uri.startsWith(prefix) || index < 0) {
            return Optional.empty();
        }
        return findEntry(uri.substring(prefix.length()), index).map(StatusEntry::status);
    }

    @Override
    public List<StatusEntry> getEntries(final String principal) {
        try (val tickets = configurationContext.getTicketRegistry().getTickets(ticket -> isEntryOf(ticket, null)
            && principal.equals(((TransientSessionTicket) ticket).getProperty(PROPERTY_PRINCIPAL, String.class)))) {
            return tickets.map(ticket -> toEntry((TransientSessionTicket) ticket)).toList();
        }
    }

    @Override
    public List<String> getStatusListUris() {
        try (val tickets = configurationContext.getTicketRegistry().getTickets(ticket -> isEntryOf(ticket, null))) {
            return tickets
                .map(ticket -> Objects.requireNonNull(((TransientSessionTicket) ticket).getProperty(PROPERTY_STATUS_LIST, String.class)))
                .distinct()
                .sorted(Comparator.comparingLong(Long::parseLong))
                .map(this::toStatusListUri)
                .toList();
        }
    }

    @Override
    public Optional<StatusEntry> updateStatus(final String statusListId, final long index, final StatusType status) throws Throwable {
        if (!isValidStatusListId(statusListId) || index < 0) {
            return Optional.empty();
        }
        val ticket = configurationContext.getTicketRegistry().getTicket(toTicketId(statusListId, index));
        if (!(ticket instanceof final TransientSessionTicket entry) || entry.isExpired()) {
            return Optional.empty();
        }
        entry.putProperty(PROPERTY_STATUS, status.name());
        configurationContext.getTicketRegistry().updateTicket(entry);
        statusListTokens.remove(statusListId);
        LOGGER.info("Status of credential [{}] at index [{}] of status list [{}] is now [{}]",
            entry.getProperty(PROPERTY_CREDENTIAL_ID, String.class), index, statusListId, status);
        return Optional.of(toEntry(entry));
    }

    protected Optional<StatusReference> createEntry(final String ticketId, final String statusListId, final long index,
                                                    final OAuth20AccessToken accessToken, final String principal,
                                                    final String credentialConfigurationId, final String credentialId,
                                                    final Duration validity)
            throws Throwable {
        val properties = new HashMap<String, Serializable>();
        properties.put(PROPERTY_STATUS_LIST, statusListId);
        properties.put(PROPERTY_INDEX, String.valueOf(index));
        properties.put(PROPERTY_STATUS, StatusType.VALID.name());
        properties.put(PROPERTY_PRINCIPAL, principal);
        properties.put(OAuth20Constants.CLIENT_ID, accessToken.getClientId());
        properties.put(PROPERTY_CREDENTIAL_CONFIGURATION_ID, credentialConfigurationId);
        properties.put(PROPERTY_CREDENTIAL_ID, credentialId);
        properties.put(PROPERTY_EXPIRES_AT, String.valueOf(Instant.now(Clock.systemUTC()).plus(validity).getEpochSecond()));
        properties.put(ExpirationPolicy.class.getName(), new HardTimeoutExpirationPolicy(Math.max(1, validity.toSeconds())));
        val factory = (TransientSessionTicketFactory) configurationContext.getTicketFactory().get(TransientSessionTicket.class);
        val stored = configurationContext.getTicketRegistry().addTicket(factory.create(ticketId, properties));
        if (stored == null || stored.isStateless()) {
            LOGGER.warn("The ticket registry cannot keep status list entries, so credentials are issued without status");
            return Optional.empty();
        }
        return Optional.of(new StatusReference(toStatusListUri(statusListId), index));
    }

    protected String sign(final JwtClaims claims) throws Throwable {
        return OidcVerifiableCredentialSigningUtils.sign(configurationContext, claims, "statuslist+jwt");
    }

    protected Optional<StatusEntry> findEntry(final String statusListId, final long index) {
        if (!isValidStatusListId(statusListId)) {
            return Optional.empty();
        }
        val ticket = configurationContext.getTicketRegistry().getTicket(toTicketId(statusListId, index));
        return ticket instanceof final TransientSessionTicket entry && !entry.isExpired()
            ? Optional.of(toEntry(entry))
            : Optional.empty();
    }

    protected String toStatusListAggregationUri() {
        return configurationContext.getCasProperties().getAuthn().getOidc().getCore().getIssuer()
            + '/' + OidcConstants.VC_STATUS_LIST_AGGREGATION_URL;
    }

    protected String toStatusListUri(final String statusListId) {
        return configurationContext.getCasProperties().getAuthn().getOidc().getCore().getIssuer()
            + '/' + OidcConstants.VC_STATUS_LIST_URL + '/' + statusListId;
    }

    protected StatusEntry toEntry(final TransientSessionTicket ticket) {
        val statusListId = Objects.requireNonNull(ticket.getProperty(PROPERTY_STATUS_LIST, String.class));
        return new StatusEntry(statusListId,
            Long.parseLong(Objects.requireNonNull(ticket.getProperty(PROPERTY_INDEX, String.class))),
            toStatusListUri(statusListId),
            StatusType.valueOf(ticket.getProperty(PROPERTY_STATUS, String.class)),
            Objects.requireNonNull(ticket.getProperty(PROPERTY_PRINCIPAL, String.class)),
            Objects.requireNonNull(ticket.getProperty(OAuth20Constants.CLIENT_ID, String.class)),
            Objects.requireNonNull(ticket.getProperty(PROPERTY_CREDENTIAL_CONFIGURATION_ID, String.class)),
            Objects.requireNonNull(ticket.getProperty(PROPERTY_CREDENTIAL_ID, String.class)),
            Long.parseLong(Objects.requireNonNull(ticket.getProperty(PROPERTY_EXPIRES_AT, String.class))));
    }

    private static boolean isEntryOf(final Ticket ticket, final @Nullable String statusListId) {
        return ticket instanceof final TransientSessionTicket entry && !entry.isExpired()
            && entry.containsProperty(PROPERTY_STATUS_LIST)
            && (statusListId == null || statusListId.equals(entry.getProperty(PROPERTY_STATUS_LIST, String.class)));
    }

    private static boolean isValidStatusListId(final String statusListId) {
        return StringUtils.isNumeric(statusListId) && statusListId.length() <= 5
            && Long.parseLong(statusListId) >= 1 && Long.parseLong(statusListId) <= MAXIMUM_STATUS_LISTS;
    }

    private static String toTicketId(final String statusListId, final long index) {
        return TransientSessionTicketFactory.normalizeTicketId(TICKET_ID_PREFIX + statusListId + '-' + index);
    }

    private record CachedStatusListToken(String token, Instant expiresAt) {
    }
}
