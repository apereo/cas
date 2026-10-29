package org.apereo.cas.support.oauth.validator;

import module java.base;
import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
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
import org.apereo.cas.web.CasSessionStore;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.dpop.JWKThumbprintConfirmation;
import com.nimbusds.oauth2.sdk.dpop.verifiers.DPoPIssuer;
import com.nimbusds.oauth2.sdk.dpop.verifiers.DPoPProofUse;
import com.nimbusds.oauth2.sdk.dpop.verifiers.DPoPProtectedResourceRequestVerifier;
import com.nimbusds.oauth2.sdk.dpop.verifiers.DPoPTokenRequestVerifier;
import com.nimbusds.oauth2.sdk.dpop.verifiers.InvalidDPoPNonceException;
import com.nimbusds.oauth2.sdk.dpop.verifiers.InvalidDPoPProofException;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.DPoPAccessToken;
import com.nimbusds.oauth2.sdk.util.singleuse.AlreadyUsedException;
import com.nimbusds.oauth2.sdk.util.singleuse.SingleUseChecker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jooq.lambda.Unchecked;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link DefaultOAuth20ProofOfPossessionValidator}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
@Slf4j
public class DefaultOAuth20ProofOfPossessionValidator implements OAuth20ProofOfPossessionValidator {

    private final CasSessionStore sessionStore;
    private final ServicesManager servicesManager;
    private final TicketRegistry ticketRegistry;
    private final TicketFactory ticketFactory;
    private final AuditableExecution registeredServiceAccessStrategyEnforcer;
    private final CasConfigurationProperties casProperties;
    private final OAuth20DPoPNonceService dpopNonceService;

    @Override
    public void validate(final HttpServletRequest request, final HttpServletResponse response,
                         final @Nullable OAuth20AccessToken accessToken)
            throws Throwable {
        val result = getProofOfPossessionHeader(request);
        if (result.isPresent()) {
            val clientId = resolveClientId(request, response, accessToken).orElseThrow();
            val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(this.servicesManager, clientId);
            val audit = AuditableContext
                .builder()
                .registeredService(registeredService)
                .build();
            val accessResult = registeredServiceAccessStrategyEnforcer.execute(audit);
            accessResult.throwExceptionIfNeeded();
            enforceCertificateBinding(request, response, registeredService);
            val confirmation = verifyProofOfPossession(request, response, result.get(), clientId);
            adjustUserProfile(request, response, result.get(), clientId, confirmation);
        } else {
            val clientId = resolveClientId(request, response, accessToken);
            if (clientId.isPresent()) {
                val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(this.servicesManager, clientId.get());
                enforceCertificateBinding(request, response, registeredService);
                enforceProofOfPossession(registeredService);
            }
        }
    }

    @Override
    public void validateTokenRequest(final HttpServletRequest request, final HttpServletResponse response,
                                     final OAuthRegisteredService registeredService)
            throws Throwable {
        enforceCertificateBinding(request, response, registeredService);
        val dPopProof = getProofOfPossessionHeader(request).filter(StringUtils::isNotBlank);
        if (dPopProof.isPresent()) {
            val confirmation = verifyProofOfPossession(request, response, dPopProof.get(), registeredService.getClientId());
            request.setAttribute(OAuth20Constants.DPOP, dPopProof.get());
            request.setAttribute(OAuth20Constants.DPOP_CONFIRMATION, confirmation.getValue().toString());
        } else {
            enforceProofOfPossession(registeredService);
        }
    }

    /**
     * The {@code DPoP} header of the request. A request may carry only one (RFC 9449, section 4.3), and pac4j would
     * otherwise return the first of several without a word.
     *
     * @param request the request
     * @return the header value, if any
     * @throws InvalidDPoPProofException when the request carries more than one {@code DPoP} header
     */
    protected Optional<String> getProofOfPossessionHeader(final HttpServletRequest request) throws InvalidDPoPProofException {
        val headers = request.getHeaders(OAuth20Constants.DPOP);
        if (headers != null && Collections.list(headers).size() > 1) {
            throw new InvalidDPoPProofException("Only one DPoP proof may be sent");
        }
        return Optional.ofNullable(request.getHeader(OAuth20Constants.DPOP));
    }

    /**
     * A client registered with {@code tls_client_certificate_bound_access_tokens} must authenticate every token request with
     * mutual TLS, so that the token it receives is bound to its certificate; it never receives an unbound token
     * (RFC 8705, section 3.4).
     *
     * @param request           the request
     * @param response          the response
     * @param registeredService the client that makes the token request, if known
     * @throws CertificateBoundAccessTokenRequiredException when the request is not authenticated with mutual TLS
     */
    protected void enforceCertificateBinding(final HttpServletRequest request, final HttpServletResponse response,
                                             final @Nullable OAuthRegisteredService registeredService)
            throws CertificateBoundAccessTokenRequiredException {
        if (registeredService != null && registeredService.isTlsClientCertificateBoundAccessTokens()) {
            val authenticatedWithCertificate = OAuth20Utils.findAuthenticatedUserProfile(request, response, this.sessionStore)
                .filter(profile -> registeredService.getClientId().equals(OAuth20Utils.getClientIdFromAuthenticatedProfile(profile)))
                .map(profile -> profile.getAttribute(OAuth20Constants.X509_CERTIFICATE_DIGEST))
                .isPresent();
            if (!authenticatedWithCertificate) {
                throw new CertificateBoundAccessTokenRequiredException(
                    "Client %s requires certificate-bound access tokens but the token request is not authenticated with mutual TLS"
                        .formatted(registeredService.getClientId()));
            }
        }
    }

    /**
     * A client registered with {@code dpop_bound_access_tokens} must present a DPoP proof with every token request
     * (RFC 9449, section 5.2).
     *
     * @param registeredService the client that makes the token request, if known
     * @throws InvalidDPoPProofException when the client must present a proof
     */
    protected void enforceProofOfPossession(final @Nullable OAuthRegisteredService registeredService) throws InvalidDPoPProofException {
        if (registeredService != null && registeredService.isDpopBoundAccessTokens()) {
            throw new InvalidDPoPProofException("Client %s requires DPoP-bound access tokens but the token request carries no DPoP proof"
                .formatted(registeredService.getClientId()));
        }
    }

    /**
     * The client identifier has to come from an authenticated source. Grants that carry their own
     * proof of authorization rather than client credentials -- the OpenID4VCI pre-authorized code
     * above all -- leave the wallet free to send any {@code client_id} it likes, or none at all,
     * while the real client is the one recorded on the authenticated profile when the credential
     * offer was created. The raw request parameter is therefore consulted last, not first.
     *
     * @param request     the request
     * @param response    the response
     * @param accessToken the access token, when one is already established
     * @return the client id
     */
    protected Optional<String> resolveClientId(final HttpServletRequest request, final HttpServletResponse response,
                                               final @Nullable OAuth20AccessToken accessToken) {
        return OAuth20Utils.findAuthenticatedUserProfile(request, response, this.sessionStore)
            .map(OAuth20Utils::getClientIdFromAuthenticatedProfile)
            .filter(StringUtils::isNotBlank)
            .or(() -> Optional.ofNullable(accessToken).map(OAuth20Token::getClientId))
            .or(() -> Optional.ofNullable(request.getParameter(OAuth20Constants.CLIENT_ID)));
    }

    protected JWKThumbprintConfirmation verifyProofOfPossession(final HttpServletRequest request,
                                                                final HttpServletResponse response,
                                                                final String dPopProof,
                                                                final String clientId)
            throws Throwable {
        val endpointURI = new URI(request.getRequestURL().toString());
        val verifier = new DPoPTokenRequestVerifier(getAcceptedSigningAlgorithms(), endpointURI,
            getMaximumAgeInSeconds(), getMaximumAgeInSeconds(), getSingleUseChecker(dPopProof, clientId));
        val signedProof = getSignedProofOfPosessionJwt(dPopProof);
        val dPopIssuer = new DPoPIssuer(new ClientID(clientId));
        val confirmation = verifier.verify(dPopIssuer, signedProof, dpopNonceService.getAcceptedNonces(signedProof));
        verifyNonce(response, signedProof);
        return confirmation;
    }

    @Override
    public void validateProtectedResourceRequest(final HttpServletRequest request,
                                                 final HttpServletResponse response,
                                                 final String presentedAccessToken,
                                                 final OAuth20AccessToken accessToken)
            throws Throwable {
        val confirmation = resolveThumbprintConfirmation(accessToken);
        if (confirmation.isEmpty()) {
            LOGGER.trace("Access token is not sender-constrained; no DPoP proof is expected");
            return;
        }
        verifyAuthorizationScheme(request, presentedAccessToken);
        val dPopProof = getProofOfPossessionHeader(request)
            .filter(StringUtils::isNotBlank)
            .orElseThrow(() -> new InvalidDPoPProofException(
                "Access token is DPoP-bound but the request carries no DPoP proof"));
        val clientId = accessToken.getClientId();
        LOGGER.debug("Verifying DPoP proof for client [{}] at [{}]", clientId, request.getRequestURL());
        val verifier = new DPoPProtectedResourceRequestVerifier(getAcceptedSigningAlgorithms(),
            getMaximumAgeInSeconds(), getMaximumAgeInSeconds(), getSingleUseChecker(dPopProof, clientId));
        val signedProof = getSignedProofOfPosessionJwt(dPopProof);
        verifier.verify(request.getMethod(), new URI(request.getRequestURL().toString()),
            new DPoPIssuer(new ClientID(clientId)), signedProof,
            new DPoPAccessToken(presentedAccessToken), confirmation.get(), dpopNonceService.getAcceptedNonces(signedProof), null);
        verifyNonce(response, signedProof);
    }

    /**
     * A DPoP-bound access token is accepted only in the {@code Authorization} header under the {@code DPoP} scheme. Taking it
     * as a bearer token, whether in the header, the form body or the query, would let it be downgraded (RFC 9449, section 7.2).
     *
     * @param request              the request
     * @param presentedAccessToken the access token exactly as the client presented it
     * @throws DPoPBoundAccessTokenDowngradeException when the token is not presented under the {@code DPoP} scheme
     */
    protected void verifyAuthorizationScheme(final HttpServletRequest request, final String presentedAccessToken)
        throws DPoPBoundAccessTokenDowngradeException {
        val scheme = OAuth20Constants.TOKEN_TYPE_DPOP + ' ';
        val authorization = Objects.requireNonNullElse(request.getHeader(HttpHeaders.AUTHORIZATION), StringUtils.EMPTY);
        if (!Strings.CI.startsWith(authorization, scheme)
            || !presentedAccessToken.equals(authorization.substring(scheme.length()).trim())) {
            throw new DPoPBoundAccessTokenDowngradeException(
                "DPoP-bound access token must be presented in the Authorization header with the DPoP scheme");
        }
    }

    @Override
    public Optional<String> validateKeyBinding(final HttpServletRequest request, final HttpServletResponse response) throws Throwable {
        val dPopProof = getProofOfPossessionHeader(request).filter(StringUtils::isNotBlank);
        if (dPopProof.isEmpty()) {
            return Optional.empty();
        }
        val clientId = resolveClientId(request, response, null)
            .orElseThrow(() -> new InvalidDPoPProofException("Unable to determine the client that presented the DPoP proof"));
        return Optional.of(verifyProofOfPossession(request, response, dPopProof.get(), clientId).getValue().toString());
    }

    /**
     * Once nonces are turned on, the proof must carry, in its {@code nonce} claim, a nonce that CAS handed out and that has not
     * expired (RFC 9449, sections 4.3 and 11.3). Otherwise a fresh nonce is put in the {@code DPoP-Nonce} header of the
     * response, and the endpoint answers with {@code use_dpop_nonce}.
     *
     * @param response    the response
     * @param signedProof the verified proof
     * @throws Throwable the throwable, {@link InvalidDPoPNonceException} when the nonce is missing or not valid
     */
    protected void verifyNonce(final HttpServletResponse response, final SignedJWT signedProof) throws Throwable {
        if (!dpopNonceService.isAccepted(signedProof)) {
            dpopNonceService.provide(response);
            throw new InvalidDPoPNonceException("DPoP proof carries no valid server-provided nonce", null);
        }
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

    @Override
    public Set<JWSAlgorithm> getAcceptedSigningAlgorithms() {
        return casProperties.getAuthn().getOidc().getDiscovery().getDpopSigningAlgValuesSupported()
            .stream()
            .map(JWSAlgorithm::parse)
            .collect(Collectors.toCollection(LinkedHashSet::new));
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

    protected void adjustUserProfile(final HttpServletRequest request,
                                     final HttpServletResponse response,
                                     final String dPopProof,
                                     final String clientId,
                                     final JWKThumbprintConfirmation confirmation)
            throws Throwable {
        OAuth20Utils.findAuthenticatedUserProfile(request, response, this.sessionStore).ifPresent(Unchecked.consumer(profile -> {
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

    /**
     * The proof must be a single well-formed JWS (RFC 9449, section 4.3). A value that does not parse is an invalid proof,
     * answered with {@code invalid_dpop_proof} (RFC 9449, sections 5 and 7.1), not a malformed request.
     *
     * @param dPopProof the proof
     * @return the signed proof
     * @throws InvalidDPoPProofException when the proof is not a JWS
     */
    protected SignedJWT getSignedProofOfPosessionJwt(final String dPopProof) throws InvalidDPoPProofException {
        try {
            return SignedJWT.parse(dPopProof);
        } catch (final ParseException e) {
            throw new InvalidDPoPProofException("DPoP proof is not a well-formed JWS: " + e.getMessage());
        }
    }
}
