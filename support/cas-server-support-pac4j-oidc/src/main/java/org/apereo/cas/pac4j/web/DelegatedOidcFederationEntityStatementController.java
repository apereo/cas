package org.apereo.cas.pac4j.web;

import org.apereo.cas.pac4j.client.DelegatedIdentityProviders;
import org.apereo.cas.web.AbstractController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.federation.entity.DefaultEntityConfigurationGenerator;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link DelegatedOidcFederationEntityStatementController}.
 *
 * @author Jerome LELEU
 * @since 8.0.0
 */
@Tag(name = "Delegated Authentication")
@RequiredArgsConstructor
public class DelegatedOidcFederationEntityStatementController extends AbstractController {

    /**
     * Base endpoint URL.
     */
    public static final String BASE_ENDPOINT_RELYING_PARTY = "/rp";

    private final DelegatedIdentityProviders identityProviders;

    /**
     * Get the entity statement for the OIDC RP (client).
     *
     * @return the entity statement
     */
    @Operation(summary = "Display the entity statement of the delegated OIDC client (RP)",
        parameters = {
            @Parameter(name = "clientName", description = "The client name", in = ParameterIn.PATH)
        })
    @GetMapping(path = BASE_ENDPOINT_RELYING_PARTY + "/{clientName}/.well-known/openid-federation", consumes = MediaType.ALL_VALUE,
        produces = DefaultEntityConfigurationGenerator.CONTENT_TYPE)
    @ResponseBody
    public String getOpenIdFederationEndpoint(
            @PathVariable final String clientName,
            final HttpServletRequest request,
            final HttpServletResponse response) {
        val webContext = new JEEContext(request, response);
        val optClient = identityProviders.findClient(clientName, webContext);
        if (optClient.isPresent()) {
            val client = optClient.get();
            if (client instanceof final OidcClient oidcClient) {
                val federation = oidcClient.getConfiguration().getFederation();
                if (StringUtils.isNotBlank(federation.getTargetOp())) {
                    return federation.getEntityConfigurationGenerator().generate();
                }
            }
        }
        return StringUtils.EMPTY;
    }
}
