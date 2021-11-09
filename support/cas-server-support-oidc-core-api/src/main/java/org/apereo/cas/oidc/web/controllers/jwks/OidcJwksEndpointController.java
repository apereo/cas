package org.apereo.cas.oidc.web.controllers.jwks;

import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyStoreUtils;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeystoreGeneratorService;
import org.apereo.cas.oidc.web.controllers.BaseOidcController;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.util.LoggingUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.pac4j.core.context.JEEContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

/**
 * This is {@link OidcJwksEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class OidcJwksEndpointController extends BaseOidcController {
    private final OidcJsonWebKeystoreGeneratorService oidcJsonWebKeystoreGeneratorService;

    public OidcJwksEndpointController(final OidcConfigurationContext configurationContext,
                                      final OidcJsonWebKeystoreGeneratorService oidcJsonWebKeystoreGeneratorService) {
        super(configurationContext);
        this.oidcJsonWebKeystoreGeneratorService = oidcJsonWebKeystoreGeneratorService;
    }

    /**
     * Handle request for jwk set.
     *
     * @param request  the request
     * @param response the response
     * @param model    the model
     * @return the jwk set
     */
    @GetMapping(value = {
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.JWKS_URL,
        "/**/" + OidcConstants.JWKS_URL
    }, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> handleRequestInternal(final HttpServletRequest request,
                                                        final HttpServletResponse response,
                                                        final Model model) {
        val webContext = new JEEContext(request, response);
        if (!getConfigurationContext().getOidcRequestSupport().isValidIssuerForEndpoint(webContext, OidcConstants.JWKS_URL)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        try {
            val resource = oidcJsonWebKeystoreGeneratorService.generate();
            val jsonJwks = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
            val jsonWebKeySet = new JsonWebKeySet(jsonJwks);
            
            val servicesManager = getConfigurationContext().getServicesManager();
            servicesManager.getAllServicesOfType(OidcRegisteredService.class)
                .stream()
                .filter(s -> StringUtils.isNotBlank(s.getJwks()))
                .forEach(service -> {
                    val set = OidcJsonWebKeyStoreUtils.getJsonWebKeySet(service, getConfigurationContext().getApplicationContext());
                    set.ifPresent(keys -> keys.getJsonWebKeys().forEach(jsonWebKeySet::addJsonWebKey));
                });
            val body = jsonWebKeySet.toJson(JsonWebKey.OutputControlLevel.PUBLIC_ONLY);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            return new ResponseEntity<>(body, HttpStatus.OK);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
            return new ResponseEntity<>(StringEscapeUtils.escapeHtml4(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }
}
