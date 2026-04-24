package org.apereo.cas.oidc.federation.signature;

import module java.base;
import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.util.DateTimeUtils;
import com.nimbusds.openid.connect.sdk.federation.entities.CommonFederationClaimsSet;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityID;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityStatement;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityStatementClaimsSet;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.minidev.json.JSONObject;

/**
 * This is {@link OidcFederationDefaultEntityStatementService}.
 *
 * @author Misagh Moayyed
 * @author Jerome LELEU
 * @since 7.3.0
 */
@RequiredArgsConstructor
public class OidcFederationDefaultEntityStatementService implements OidcFederationEntityStatementService {
    private final OidcFederationJsonWebKeystoreService jsonWebKeystoreService;
    private final OidcProperties oidcProperties;

    @Override
    public EntityStatement createAndSign(final String issuer, final String subject,
                                         final JSONObject metadata, final List<EntityID> authorityHints) throws Exception {
        val iss = new EntityID(issuer);
        val sub = new EntityID(subject);

        val now = LocalDate.now(Clock.systemUTC());
        val iat = DateTimeUtils.dateOf(now);
        val entityStatementExpiration = Beans.newDuration(oidcProperties.getFederation().getEntityStatementExpiration());
        val expDate = now.plusDays(entityStatementExpiration.toDays());
        val exp = DateTimeUtils.dateOf(expDate);
        val claims = new EntityStatementClaimsSet(
            iss,
            sub,
            iat,
            exp,
            jsonWebKeystoreService.toJWKSet()
        );

        if (authorityHints != null) {
            claims.setAuthorityHints(authorityHints);
        }

        claims.setClaim(CommonFederationClaimsSet.METADATA_CLAIM_NAME, metadata);

        return jsonWebKeystoreService.signEntityStatement(claims);
    }
}
