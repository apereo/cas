package org.apereo.cas.support.oauth.validator;

import module java.base;
import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.OAuth20Token;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.dpop.JWKThumbprintConfirmation;
import com.nimbusds.oauth2.sdk.dpop.verifiers.DPoPIssuer;
import com.nimbusds.oauth2.sdk.dpop.verifiers.DPoPProofUse;
import com.nimbusds.oauth2.sdk.dpop.verifiers.DPoPTokenRequestVerifier;
import com.nimbusds.oauth2.sdk.dpop.verifiers.InMemoryDPoPSingleUseChecker;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.util.singleuse.SingleUseChecker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
    private final SingleUseChecker<DPoPProofUse> proofOfPossessionSingleUseChecker
        = new InMemoryDPoPSingleUseChecker(60, 60);

    private final SessionStore sessionStore;
    private final ServicesManager servicesManager;
    private final AuditableExecution registeredServiceAccessStrategyEnforcer;
    private final CasConfigurationProperties casProperties;

    @Override
    public void validate(final WebContext webContext, final OAuth20AccessToken accessToken) throws Throwable {
        val result = webContext.getRequestHeader(OAuth20Constants.DPOP);
        if (result.isPresent()) {
            val clientId = webContext.getRequestParameter(OAuth20Constants.CLIENT_ID)
                .or(() -> Optional.ofNullable(accessToken).map(OAuth20Token::getClientId))
                .orElseThrow();
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

    protected JWKThumbprintConfirmation verifyProofOfPossession(final WebContext webContext,
                                                                final String dPopProof,
                                                                final String clientId) throws Throwable {
        val algorithms = casProperties.getAuthn().getOidc().getDiscovery().getDpopSigningAlgValuesSupported()
            .stream()
            .map(JWSAlgorithm::parse)
            .collect(Collectors.toSet());
        val seconds = Beans.newDuration(casProperties.getAuthn().getOidc().getCore().getSkew()).toSeconds();
        val endpointURI = new URI(webContext.getRequestURL());
        val verifier = new DPoPTokenRequestVerifier(algorithms, endpointURI, seconds, seconds, this.proofOfPossessionSingleUseChecker);
        val signedProof = getSignedProofOfPosessionJwt(dPopProof);
        val dPopIssuer = new DPoPIssuer(new ClientID(clientId));
        return verifier.verify(dPopIssuer, signedProof, Set.of());
    }

    protected void adjustUserProfile(final WebContext webContext,
                                     final String dPopProof,
                                     final String clientId,
                                     final JWKThumbprintConfirmation confirmation) throws Throwable {
        val manager = new ProfileManager(webContext, this.sessionStore);
        manager.getProfile().ifPresent(Unchecked.consumer(profile -> {
            val signedProof = getSignedProofOfPosessionJwt(dPopProof);
            profile.setId(clientId);
            signedProof.getJWTClaimsSet().getClaims().forEach(profile::addAttribute);
            profile.addAttribute(OAuth20Constants.DPOP, dPopProof);
            profile.addAttribute(OAuth20Constants.DPOP_CONFIRMATION, confirmation.getValue().toString());
        }));
    }

    protected SignedJWT getSignedProofOfPosessionJwt(final String dPopProof) throws Throwable {
        return SignedJWT.parse(dPopProof);
    }
}
