package org.apereo.cas.web.controllers;


import com.stormpath.sdk.lang.Assert;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.OidcConstants;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.web.BaseOAuthWrapperController;
import org.jooq.lambda.Unchecked;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

/**
 * This is {@link OidcJwksEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class OidcJwksEndpointController extends BaseOAuthWrapperController {

    /**
     * The Resource loader.
     */
    @Autowired
    protected ResourceLoader resourceLoader;
    
    private Resource jwksFile;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    /**
     * Handle request for jwk set.
     *
     * @param request  the request
     * @param response the response
     * @param model    the model
     * @return the jwk set
     * @throws Exception the exception
     */
    @RequestMapping(value = '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.JWKS_URL,
            method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> handleRequestInternal(final HttpServletRequest request,
                                                        final HttpServletResponse response,
                                                        final Model model) throws Exception {

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
            logger.error(e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public void setJwksFile(final Resource jwksFile) {
        this.jwksFile = jwksFile;
    }
}
