package org.apereo.cas.oidc;

import org.apereo.cas.oidc.claims.OidcAttributeToScopeClaimMapper;
import org.apereo.cas.oidc.claims.OidcIdTokenClaimCollector;
import org.apereo.cas.oidc.discovery.OidcServerDiscoverySettings;
import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.oidc.nativesso.OidcDeviceSecretGenerator;
import org.apereo.cas.oidc.util.OidcRequestSupport;
import org.apereo.cas.oidc.web.controllers.dynareg.OidcClientRegistrationRequestTranslator;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.OAuth20TokenSigningAndEncryptionService;
import org.apereo.cas.ticket.idtoken.IdTokenGeneratorService;
import org.apereo.cas.token.JwtBuilder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import java.util.List;

/**
 * This is {@link OidcConfigurationContext}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Getter
@SuperBuilder
public class OidcConfigurationContext extends OAuth20ConfigurationContext {
    /**
     * Bean name.
     */
    public static final String BEAN_NAME = "oidcConfigurationContext";

    private final OidcServerDiscoverySettings discoverySettings;

    private final OidcAttributeToScopeClaimMapper attributeToScopeClaimMapper;

    private final OidcIssuerService issuerService;

    private final OidcRequestSupport oidcRequestSupport;

    private final List<OidcIdTokenClaimCollector> idTokenClaimCollectors;

    private final IdTokenGeneratorService idTokenGeneratorService;
    
    private final ExpirationPolicyBuilder idTokenExpirationPolicy;

    private final JwtBuilder responseModeJwtBuilder;

    private final OidcClientRegistrationRequestTranslator clientRegistrationRequestTranslator;

    private final OAuth20TokenSigningAndEncryptionService introspectionSigningAndEncryptionService;

    private final OidcDeviceSecretGenerator deviceSecretGenerator;
}
