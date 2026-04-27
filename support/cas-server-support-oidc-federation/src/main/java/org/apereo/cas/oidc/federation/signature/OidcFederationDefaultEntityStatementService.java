package org.apereo.cas.oidc.federation.signature;

import module java.base;
import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.util.DateTimeUtils;
import com.nimbusds.openid.connect.sdk.federation.entities.CommonFederationClaimsSet;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityID;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityStatement;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityStatementClaimsSet;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityType;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import tools.jackson.databind.JsonNode;

import static org.apereo.cas.oidc.OidcConstants.*;

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
                                         final JSONObject metadata, final JsonNode federationKeys,
                                         final List<EntityID> authorityHints) throws Exception {
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

        val jwks = (JSONObject) claims.getClaim(JWKS);
        if (federationKeys != null) {
            val keys = (List) jwks.get(KEYS);
            val addKeys = (JSONArray) JSONValue.parse(federationKeys.toString());
            keys.addAll(addKeys);
        }

        if (authorityHints != null) {
            claims.setAuthorityHints(authorityHints);
        }

        claims.setClaim(CommonFederationClaimsSet.METADATA_CLAIM_NAME, metadata);

        val federationEntity = (JSONObject) metadata.get(EntityType.FEDERATION_ENTITY.getValue());
        if (federationEntity != null) {
            val fetchEndpoint = federationEntity.get("federation_fetch_endpoint");
            if (fetchEndpoint != null) {
                val map = new HashMap<String, Object>();
                map.put("max_path_length", 1);
                claims.setClaim("constraints", map);
            }
        }

        return jsonWebKeystoreService.signEntityStatement(claims);
    }
}
