package org.apereo.cas.oidc.web.controllers;


import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.validator.OAuth20Validator;
import org.apereo.cas.support.oauth.web.endpoints.BaseOAuth20Controller;
import org.apereo.cas.ticket.accesstoken.AccessTokenFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.jooq.lambda.Unchecked;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
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
public class OidcJwksEndpointController extends BaseOAuth20Controller {
    private static final Logger LOGGER = LoggerFactory.getLogger(OidcJwksEndpointController.class);

    @Autowired
    private ResourceLoader resourceLoader;

    private final Resource jwksFile;

    public OidcJwksEndpointController(final ServicesManager servicesManager,
                                      final TicketRegistry ticketRegistry,
                                      final OAuth20Validator validator,
                                      final AccessTokenFactory accessTokenFactory,
                                      final PrincipalFactory principalFactory,
                                      final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory,
                                      final OAuth20ProfileScopeToAttributesFilter scopeToAttributesFilter,
                                      final CasConfigurationProperties casProperties,
                                      final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator) {
        super(servicesManager, ticketRegistry, validator, accessTokenFactory, principalFactory,
                webApplicationServiceServiceFactory, scopeToAttributesFilter, 
                casProperties, ticketGrantingTicketCookieGenerator);
        this.jwksFile = casProperties.getAuthn().getOidc().getJwksFile();
    }

    /**
     * Handle request for jwk set.
     *
     * @param request  the request
     * @param response the response
     * @param model    the model
     * @return the jwk set
     */
    @GetMapping(value = '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.JWKS_URL, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> handleRequestInternal(final HttpServletRequest request,
                                                        final HttpServletResponse response,
                                                        final Model model) {

        Assert.notNull(this.jwksFile, "JWKS file cannot be undefined or null.");

        try {
            final String jsonJwks = IOUtils.toString(this.jwksFile.getInputStream(), StandardCharsets.UTF_8);
            final JsonWebKeySet jsonWebKeySet = new JsonWebKeySet(jsonJwks);

            this.servicesManager.getAllServices()
                    .stream()
                    .filter(s -> s instanceof OidcRegisteredService && StringUtils.isNotBlank(((OidcRegisteredService) s).getJwks()))
                    .forEach(
                            Unchecked.consumer(s -> {
                                final OidcRegisteredService service = (OidcRegisteredService) s;
                                final Resource resource = this.resourceLoader.getResource(service.getJwks());
                                final JsonWebKeySet set = new JsonWebKeySet(IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8));
                                set.getJsonWebKeys().forEach(jsonWebKeySet::addJsonWebKey);
                            }));
            final String body = jsonWebKeySet.toJson(JsonWebKey.OutputControlLevel.PUBLIC_ONLY);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            return new ResponseEntity<>(body, HttpStatus.OK);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
