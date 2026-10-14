package org.apereo.cas.oidc.vc.issuer.deferred;

import module java.base;
import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.configuration.model.support.oidc.OidcVerifiableCredentialConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.vc.issuer.proof.OidcVerifiableCredentialProofValidator.VerifiableCredentialProofResult;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.nimbusds.jose.jwk.JWK;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.audit.annotation.Audit;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * Keeps deferred transactions as transient session tickets that carry the client, the user, the credential configuration,
 * the status and the validated holder public keys, and expire after the configured time to live. No claims are kept; the
 * credentials are built when the wallet collects them. Their properties are strings, since registries that serialize
 * tickets do not keep other types. A registry that cannot keep the transaction, the stateless ticket registry, makes the
 * credentials issue immediately. Decisions are recorded in the audit log.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
@Slf4j
public class OidcVerifiableCredentialDefaultDeferredIssuanceService implements OidcVerifiableCredentialDeferredIssuanceService {
    private static final String PROPERTY_CREDENTIAL_CONFIGURATION_ID = "deferredCredentialConfigurationId";

    private static final String PROPERTY_PRINCIPAL = "deferredCredentialPrincipal";

    private static final String PROPERTY_STATUS = "deferredCredentialStatus";

    private static final String PROPERTY_PROOFS = "deferredCredentialProofs";

    private static final String PROPERTY_CREATED_AT = "deferredCredentialCreatedAt";

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false)
        .minimal(true)
        .build()
        .toObjectMapper();

    private static final TypeReference<List<Map<String, Object>>> PROOFS_TYPE = new TypeReference<>() {
    };

    protected final OidcConfigurationContext configurationContext;

    @Override
    public boolean isDeferred(final String credentialConfigurationId) {
        val configuration = configurationContext.getCasProperties().getAuthn().getOidc().getVc()
            .getIssuer().getCredentialConfigurations().get(credentialConfigurationId);
        return configuration != null && configuration.isDeferredIssuance();
    }

    @Override
    public boolean isDeferredIssuanceSupported() {
        return configurationContext.getCasProperties().getAuthn().getOidc().getVc().getIssuer()
            .getCredentialConfigurations().values().stream().anyMatch(OidcVerifiableCredentialConfigurationProperties::isDeferredIssuance);
    }

    @Override
    public Optional<DeferredTransaction> defer(final OAuth20AccessToken accessToken, final String credentialConfigurationId,
                                               final List<VerifiableCredentialProofResult> proofs)
            throws Throwable {
        val timeToLive = Beans.newDuration(configurationContext.getCasProperties().getAuthn().getOidc()
            .getVc().getIssuer().getDeferredIssuance().getTimeToLive());
        val properties = new HashMap<String, Serializable>();
        properties.put(OAuth20Constants.CLIENT_ID, accessToken.getClientId());
        properties.put(PROPERTY_PRINCIPAL, accessToken.getAuthentication().getPrincipal().getId());
        properties.put(PROPERTY_CREDENTIAL_CONFIGURATION_ID, credentialConfigurationId);
        properties.put(PROPERTY_STATUS, TransactionStatus.PENDING.name());
        properties.put(PROPERTY_PROOFS, MAPPER.writeValueAsString(proofs.stream().map(this::toMap).toList()));
        properties.put(PROPERTY_CREATED_AT, String.valueOf(Instant.now(Clock.systemUTC()).getEpochSecond()));
        properties.put(ExpirationPolicy.class.getName(), new HardTimeoutExpirationPolicy(Math.max(1, timeToLive.toSeconds())));
        val factory = (TransientSessionTicketFactory) configurationContext.getTicketFactory().get(TransientSessionTicket.class);
        val stored = configurationContext.getTicketRegistry().addTicket(factory.create(properties));
        if (!(stored instanceof final TransientSessionTicket transaction) || transaction.isStateless()) {
            LOGGER.warn("The ticket registry cannot keep deferred transactions, so credentials of [{}] are issued immediately",
                credentialConfigurationId);
            return Optional.empty();
        }
        LOGGER.info("Credentials of [{}] for [{}] are deferred as transaction [{}]", credentialConfigurationId,
            accessToken.getClientId(), transaction.getId());
        return Optional.of(toTransaction(transaction));
    }

    @Override
    public Optional<DeferredTransaction> find(final OAuth20AccessToken accessToken, final String transactionId) {
        val ticket = FunctionUtils.doAndHandle(() -> configurationContext.getTicketRegistry().getTicket(transactionId));
        if (ticket != null && isTransaction(ticket)) {
            val transaction = (TransientSessionTicket) ticket;
            if (accessToken.getClientId().equals(transaction.getProperty(OAuth20Constants.CLIENT_ID, String.class))
                && accessToken.getAuthentication().getPrincipal().getId().equals(transaction.getProperty(PROPERTY_PRINCIPAL, String.class))) {
                return Optional.of(toTransaction(transaction));
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean complete(final String transactionId) throws Throwable {
        return configurationContext.getTicketRegistry().deleteTicket(transactionId) > 0;
    }

    @Override
    public List<DeferredTransaction> getTransactions(final @Nullable String principal) {
        try (val tickets = configurationContext.getTicketRegistry().getTickets(ticket -> isTransaction(ticket)
            && (principal == null || principal.equals(((TransientSessionTicket) ticket).getProperty(PROPERTY_PRINCIPAL, String.class))))) {
            return tickets.map(ticket -> toTransaction((TransientSessionTicket) ticket)).toList();
        }
    }

    @Audit(action = AuditableActions.OIDC_VERIFIABLE_CREDENTIAL_DEFERRED_ISSUANCE,
        actionResolverName = AuditActionResolvers.OIDC_VERIFIABLE_CREDENTIAL_DEFERRED_ISSUANCE_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.OIDC_VERIFIABLE_CREDENTIAL_DEFERRED_ISSUANCE_RESOURCE_RESOLVER)
    @Override
    public Optional<DeferredTransaction> decide(final String transactionId, final TransactionStatus status) throws Throwable {
        val ticket = configurationContext.getTicketRegistry().getTicket(transactionId);
        if (ticket == null || !isTransaction(ticket)) {
            return Optional.empty();
        }
        val transaction = (TransientSessionTicket) ticket;
        transaction.putProperty(PROPERTY_STATUS, status.name());
        configurationContext.getTicketRegistry().updateTicket(transaction);
        LOGGER.info("Deferred transaction [{}] for [{}] is now [{}]", transactionId,
            transaction.getProperty(PROPERTY_PRINCIPAL, String.class), status);
        return Optional.of(toTransaction(transaction));
    }

    protected DeferredTransaction toTransaction(final TransientSessionTicket ticket) {
        val proofs = MAPPER.readValue(Objects.requireNonNull(ticket.getProperty(PROPERTY_PROOFS, String.class)), PROOFS_TYPE);
        return new DeferredTransaction(ticket.getId(),
            Objects.requireNonNull(ticket.getProperty(OAuth20Constants.CLIENT_ID, String.class)),
            Objects.requireNonNull(ticket.getProperty(PROPERTY_PRINCIPAL, String.class)),
            Objects.requireNonNull(ticket.getProperty(PROPERTY_CREDENTIAL_CONFIGURATION_ID, String.class)),
            TransactionStatus.valueOf(ticket.getProperty(PROPERTY_STATUS, String.class)),
            Long.parseLong(Objects.requireNonNull(ticket.getProperty(PROPERTY_CREATED_AT, String.class))),
            proofs.stream().map(OidcVerifiableCredentialDefaultDeferredIssuanceService::toProof).toList());
    }

    protected Map<String, Object> toMap(final VerifiableCredentialProofResult proof) {
        val map = new LinkedHashMap<String, Object>();
        map.put("proofType", proof.proofType());
        map.put("jwtId", proof.jwtId());
        map.put("subject", proof.subject());
        map.put("holderJwk", proof.holderJwk().toPublicJWK().toJSONObject());
        map.put("nonce", proof.nonce());
        return map;
    }

    private static VerifiableCredentialProofResult toProof(final Map<String, Object> proof) {
        return new VerifiableCredentialProofResult(
            Objects.toString(proof.get("proofType")),
            Objects.toString(proof.get("jwtId")),
            Objects.toString(proof.get("subject")),
            FunctionUtils.doUnchecked(() -> JWK.parse(toJsonObject(proof.get("holderJwk")))),
            Objects.toString(proof.get("nonce"), null));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> toJsonObject(final @Nullable Object value) {
        return (Map<String, Object>) Objects.requireNonNull(value, "Deferred transaction carries no holder key");
    }

    private static boolean isTransaction(final Ticket ticket) {
        return ticket instanceof final TransientSessionTicket transaction && !transaction.isExpired()
            && transaction.containsProperty(PROPERTY_CREDENTIAL_CONFIGURATION_ID)
            && transaction.containsProperty(PROPERTY_PROOFS);
    }
}
