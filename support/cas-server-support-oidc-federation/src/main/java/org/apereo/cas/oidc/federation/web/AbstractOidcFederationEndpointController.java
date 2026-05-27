package org.apereo.cas.oidc.federation.web;

import module java.base;
import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.federation.signature.OidcFederationEntityStatementService;
import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.web.AbstractController;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityID;
import com.nimbusds.openid.connect.sdk.federation.entities.FederationEntityMetadata;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.minidev.json.JSONObject;
import org.jspecify.annotations.Nullable;
import org.pac4j.jee.context.JEEContext;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link AbstractOidcFederationEndpointController}.
 *
 * @author Jerome LELEU
 * @since 8.0.0
 */
@Tag(name = "OpenID Connect")
@RequiredArgsConstructor
abstract class AbstractOidcFederationEndpointController extends AbstractController {

    protected final OidcIssuerService oidcIssuerService;
    protected final OidcFederationEntityStatementService federationEntityStatementService;
    protected final OidcProperties oidcProperties;

    protected @Nullable ResponseEntity retrieveInvalidIssuerError(
        final HttpServletRequest request, final HttpServletResponse response, final String path) {
        val webContext = new JEEContext(request, response);
        if (!oidcIssuerService.validateIssuer(webContext, List.of(path))) {
            val body = OAuth20Utils.getErrorResponseBody(OAuth20Constants.INVALID_REQUEST, "Invalid issuer");
            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        }
        return null;
    }

    protected FederationEntityMetadata buildMetadata(final String issuer) throws URISyntaxException {
        val fedMeta = new FederationEntityMetadata();
        fedMeta.setOrganizationName(oidcProperties.getFederation().getOrganization());
        fedMeta.setContacts(oidcProperties.getFederation().getContacts());
        val role = oidcProperties.getFederation().getRole();
        if (role.isTrustAnchorOrIntermediate()) {
            fedMeta.setFederationFetchEndpointURI(new URI(issuer + OidcConstants.FETCH_FEDERATION_URL));
        }
        return fedMeta;
    }

    protected ResponseEntity buildEntityStatement(
        final String issuer, final String subject, final JSONObject metadata,
        @Nullable final JsonNode federationKeys,
        @Nullable final List<EntityID> authorityHints) throws Exception {
        val entityStatement = federationEntityStatementService.createAndSign(
            issuer, subject, metadata, federationKeys, authorityHints);
        return ResponseEntity.ok()
            .cacheControl(CacheControl.noStore().mustRevalidate())
            .header(HttpHeaders.ACCEPT, OidcConstants.ENTITY_STATEMENT_CONTENT_TYPE.toString())
            .contentType(OidcConstants.ENTITY_STATEMENT_CONTENT_TYPE)
            .body(entityStatement.getSignedStatement().serialize());
    }
}
