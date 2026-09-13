package org.apereo.cas.support.oauth.validator;

import module java.base;
import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.OAuth20Token;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.function.FunctionUtils;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.dpop.JWKThumbprintConfirmation;
import com.nimbusds.oauth2.sdk.dpop.verifiers.DPoPIssuer;
import com.nimbusds.oauth2.sdk.dpop.verifiers.DPoPProofUse;
import com.nimbusds.oauth2.sdk.dpop.verifiers.DPoPProtectedResourceRequestVerifier;
import com.nimbusds.oauth2.sdk.dpop.verifiers.DPoPTokenRequestVerifier;
import com.nimbusds.oauth2.sdk.dpop.verifiers.InvalidDPoPProofException;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.DPoPAccessToken;
import com.nimbusds.oauth2.sdk.util.singleuse.AlreadyUsedException;
import com.nimbusds.oauth2.sdk.util.singleuse.SingleUseChecker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.profile.ProfileManager;

/**
 * This is {@link DefaultOAuth20ProofOfPossessionValidator}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
@Slf4j
public class DefaultOAuth20ProofOfPossessionValidator implements OAuth20ProofOfPossessionValidator {

    private final SessionStore sessionStore;
    private final ServicesManager servicesManager;
    private final TicketRegistry ticketRegistry;
    private final TicketFactory ticketFactory;
    private final AuditableExecution registeredServiceAccessStrategyEnforcer;
    private final CasConfigurationProperties casProperties;

    @Override
    public void validate(final WebContext webContext, final OAuth20AccessToken accessToken) throws Throwable {
        val result = webContext.getRequestHeader(OAuth20Constants.DPOP);
        if (result.isPresent()) {
            val clientId = resolveClientId(webContext, accessToken).orElseThrow();
            val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(this.servicesManager, clientId);
            val audit = AuditableContext
                .builder()
                .registeredService(registeredService)
                .build();
            val accessResult = registeredServiceAccessStrategyEnforcer.execute(audit);
            accessResult.throwExceptionIfNeeded();
            val confirmation = verifyProofOfPossession(webContext, result.get(), clientId);
            adjustUserProfile(webContext, result.get(), clientId, confirmation);
        }
    }

    /**
     * The client identifier has to come from an authenticated source. Grants that carry their own
     * proof of authorization rather than client credentials -- the OpenID4VCI pre-authorized code
     * above all -- leave the wallet free to send any {@code client_id} it likes, or none at all,
     * while the real client is the one recorded on the authenticated profile when the credential
     * offer was created. The raw request parameter is therefore consulted last, not first.
     *
     * @param webContext  the web context
     * @param accessToken the access token, when one is already established
     * @return the client id
     */
    protected Optional<String> resolveClientId(final WebContext webContext,
                                               final OAuth20AccessToken accessToken) {
        val manager = new ProfileManager(webContext, this.sessionStore);
        return manager.getProfile()
            .map(OAuth20Utils::getClientIdFromAuthenticatedProfile)
            .filter(StringUtils::isNotBlank)
            .or(() -> Optional.ofNullable(accessToken).map(OAuth20Token::getClientId))
            .or(() -> webContext.getRequestParameter(OAuth20Constants.CLIENT_ID));
    }

    protected JWKThumbprintConfirmation verifyProofOfPossession(final WebContext webContext,
                                                                final String dPopProof,
                                                                final String clientId) throws Throwable {
        val endpointURI = new URI(webContext.getRequestURL());
        val verifier = new DPoPTokenRequestVerifier(getAcceptedSigningAlgorithms(), endpointURI,
            getMaximumAgeInSeconds(), getMaximumAgeInSeconds(), getSingleUseChecker(dPopProof, clientId));
        val signedProof = getSignedProofOfPosessionJwt(dPopProof);
        val dPopIssuer = new DPoPIssuer(new ClientID(clientId));
        return verifier.verify(dPopIssuer, signedProof, Set.of());
    }

    @Override
    public void validateProtectedResourceRequest(final WebContext webContext,
                                                 final String presentedAccessToken,
                                                 final OAuth20AccessToken accessToken) throws Throwable {
        val confirmation = resolveThumbprintConfirmation(accessToken);
        if (confirmation.isEmpty()) {
            LOGGER.trace("Access token is not sender-constrained; no DPoP proof is expected");
            return;
        }
        val dPopProof = webContext.getRequestHeader(OAuth20Constants.DPOP)
            .filter(StringUtils::isNotBlank)
            .orElseThrow(() -> new InvalidDPoPProofException(
                "Access token is DPoP-bound but the request carries no DPoP proof"));
        val clientId = accessToken.getClientId();
        LOGGER.debug("Verifying DPoP proof for client [{}] at [{}]", clientId, webContext.getRequestURL());
        val verifier = new DPoPProtectedResourceRequestVerifier(getAcceptedSigningAlgorithms(),
            getMaximumAgeInSeconds(), getMaximumAgeInSeconds(), getSingleUseChecker(dPopProof, clientId));
        verifier.verify(webContext.getRequestMethod(), new URI(webContext.getRequestURL()),
            new DPoPIssuer(new ClientID(clientId)), getSignedProofOfPosessionJwt(dPopProof),
            new DPoPAccessToken(presentedAccessToken), confirmation.get(), null);
    }

    /**
     * The confirmation recorded on the access token when it was issued. Its absence is what marks a
     * token as an ordinary bearer token, so it doubles as the test for whether a proof is required
     * at all.
     *
     * @param accessToken the access token
     * @return the thumbprint confirmation, empty when the token is not sender-constrained
     */
    protected Optional<JWKThumbprintConfirmation> resolveThumbprintConfirmation(final OAuth20AccessToken accessToken) {
        if (accessToken == null || !accessToken.getAuthentication().containsAttribute(OAuth20Constants.DPOP_CONFIRMATION)) {
            return Optional.empty();
        }
        val attribute = accessToken.getAuthentication().getAttributes().get(OAuth20Constants.DPOP_CONFIRMATION);
        return CollectionUtils.firstElement(attribute)
            .map(value -> new JWKThumbprintConfirmation(new Base64URL(value.toString())));
    }

    protected Set<JWSAlgorithm> getAcceptedSigningAlgorithms() {
        return casProperties.getAuthn().getOidc().getDiscovery().getDpopSigningAlgValuesSupported()
            .stream()
            .map(JWSAlgorithm::parse)
            .collect(Collectors.toSet());
    }

    protected long getMaximumAgeInSeconds() {
        return Beans.newDuration(casProperties.getAuthn().getOidc().getCore().getSkew()).toSeconds();
    }

    /**
     * Replay protection for the proof's {@code jti}, kept in the ticket registry so that the check
     * holds across a cluster rather than within one node's memory.
     *
     * @param dPopProof the proof
     * @param clientId  the client id
     * @return the single use checker
     */
    protected SingleUseChecker<DPoPProofUse> getSingleUseChecker(final String dPopProof, final String clientId) {
        return dPoPProofUse -> {
            val key = dPoPProofUse.getIssuer() + ":" + DigestUtils.sha256(dPoPProofUse.getJWTID().getValue());
            val ticketId = TransientSessionTicketFactory.normalizeTicketId(key);
            try {
                ticketRegistry.getTicket(ticketId, TransientSessionTicket.class);
                throw new AlreadyUsedException("DPoP proof has already been used: " + dPoPProofUse.getJWTID().getValue());
            } catch (final InvalidTicketException e) {
                val factory = (TransientSessionTicketFactory) ticketFactory.get(TransientSessionTicket.class);
                val ticket = factory.create(ticketId, Map.of(OAuth20Constants.DPOP, dPopProof, OAuth20Constants.CLIENT_ID, clientId));
                FunctionUtils.doUnchecked(_ -> ticketRegistry.addTicket(ticket));
            }
        };
    }

    protected void adjustUserProfile(final WebContext webContext,
                                     final String dPopProof,
                                     final String clientId,
                                     final JWKThumbprintConfirmation confirmation) throws Throwable {
        val manager = new ProfileManager(webContext, this.sessionStore);
        manager.getProfile().ifPresent(Unchecked.consumer(profile -> {
            val signedProof = getSignedProofOfPosessionJwt(dPopProof);
            /*
             * A profile that already names this client was authenticated as somebody -- the
             * subject of a pre-authorized code, say -- and its identifier is that subject, not
             * the client. Overwriting it there would mint the token for the wrong principal.
             */
            if (!clientId.equals(OAuth20Utils.getClientIdFromAuthenticatedProfile(profile))) {
                profile.setId(clientId);
            }
            signedProof.getJWTClaimsSet().getClaims().forEach(profile::addAttribute);
            profile.addAttribute(OAuth20Constants.DPOP, dPopProof);
            profile.addAttribute(OAuth20Constants.DPOP_CONFIRMATION, confirmation.getValue().toString());
        }));
    }

    protected SignedJWT getSignedProofOfPosessionJwt(final String dPopProof) throws Throwable {
        return SignedJWT.parse(dPopProof);
    }
}
