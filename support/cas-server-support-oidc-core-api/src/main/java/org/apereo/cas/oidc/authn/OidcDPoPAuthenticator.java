package org.apereo.cas.oidc.authn;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.oidc.discovery.OidcServerDiscoverySettings;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.dpop.verifiers.DPoPIssuer;
import com.nimbusds.oauth2.sdk.dpop.verifiers.DPoPTokenRequestVerifier;
import com.nimbusds.oauth2.sdk.id.ClientID;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.profile.CommonProfile;

import java.net.URI;
import java.util.stream.Collectors;

/**
 * This is {@link OidcDPoPAuthenticator}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@RequiredArgsConstructor
public class OidcDPoPAuthenticator implements Authenticator {
    protected final OidcServerDiscoverySettings oidcServerDiscoverySettings;

    protected final ServicesManager servicesManager;

    protected final AuditableExecution registeredServiceAccessStrategyEnforcer;

    protected final CasConfigurationProperties casProperties;

    @Override
    public void validate(final Credentials credentials, final WebContext webContext,
                         final SessionStore sessionStore) {
        webContext.getRequestHeader(OAuth20Constants.DPOP)
            .ifPresent(Unchecked.consumer(dPopProof -> {
                val clientId = webContext.getRequestParameter(OAuth20Constants.CLIENT_ID).orElseThrow();
                val registeredService = (OidcRegisteredService)
                    OAuth20Utils.getRegisteredOAuthServiceByClientId(servicesManager, clientId);
                val audit = AuditableContext.builder()
                    .registeredService(registeredService)
                    .build();
                val accessResult = registeredServiceAccessStrategyEnforcer.execute(audit);
                if (!accessResult.isExecutionFailure()) {
                    val algorithms = oidcServerDiscoverySettings.getDPopSigningAlgValuesSupported()
                        .stream()
                        .map(JWSAlgorithm::parse)
                        .collect(Collectors.toSet());

                    val seconds = Beans.newDuration(casProperties.getAuthn().getOidc().getCore().getSkew()).toSeconds();
                    val verifier = new DPoPTokenRequestVerifier(algorithms,
                        new URI(webContext.getRequestURL()), seconds, null);
                    val signedProof = SignedJWT.parse(dPopProof);
                    val dPopIssuer = new DPoPIssuer(new ClientID(clientId));
                    val confirmation = verifier.verify(dPopIssuer, signedProof);

                    val userProfile = new CommonProfile(true);
                    userProfile.setId(clientId);
                    userProfile.addAttributes(signedProof.getJWTClaimsSet().getClaims());
                    userProfile.addAttribute(OAuth20Constants.DPOP, dPopProof);
                    userProfile.addAttribute(OAuth20Constants.DPOP_CONFIRMATION, confirmation.getValue().toString());
                    credentials.setUserProfile(userProfile);
                }
            }));
    }
}
