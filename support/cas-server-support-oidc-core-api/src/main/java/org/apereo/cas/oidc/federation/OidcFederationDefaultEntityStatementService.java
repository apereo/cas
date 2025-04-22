package org.apereo.cas.oidc.federation;

import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.oidc.discovery.OidcServerDiscoverySettings;
import org.apereo.cas.util.DateTimeUtils;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityID;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityStatement;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityStatementClaimsSet;
import com.nimbusds.openid.connect.sdk.federation.entities.FederationEntityMetadata;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import lombok.RequiredArgsConstructor;
import lombok.val;
import java.time.Clock;
import java.time.LocalDate;

/**
 * This is {@link OidcFederationDefaultEntityStatementService}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@RequiredArgsConstructor
public class OidcFederationDefaultEntityStatementService implements OidcFederationEntityStatementService {
    private final OidcFederationJsonWebKeystoreService jsonWebKeystoreService;
    private final OidcServerDiscoverySettings serverDiscoverySettings;
    private final OidcProperties oidcProperties;

    @Override
    public EntityStatement createAndSign() throws Exception {
        val iss = new EntityID(serverDiscoverySettings.getIssuer());
        val sub = new EntityID(serverDiscoverySettings.getIssuer());

        val iat = DateTimeUtils.dateOf(LocalDate.now(Clock.systemUTC()));
        val exp = DateTimeUtils.dateOf(LocalDate.now(Clock.systemUTC()).plusYears(10));
        val claims = new EntityStatementClaimsSet(
            iss,
            sub,
            iat,
            exp,
            jsonWebKeystoreService.toJWKSet()
        );

        val authorityHints = oidcProperties.getFederation().getAuthorityHints().stream().map(EntityID::new).toList();
        claims.setAuthorityHints(authorityHints);
        
        val discovery = serverDiscoverySettings.toJson();
        val opMetadata = OIDCProviderMetadata.parse(discovery);
        claims.setOPMetadata(opMetadata);
        
        val fedMeta = new FederationEntityMetadata();
        fedMeta.setOrganizationName(oidcProperties.getFederation().getOrganization());
        fedMeta.setContacts(oidcProperties.getFederation().getContacts());
        claims.setFederationEntityMetadata(fedMeta);

        return jsonWebKeystoreService.signEntityStatement(claims);
    }
}
