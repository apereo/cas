package org.apereo.cas.support.rest;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.rest.BadRestRequestException;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RegexUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.credentials.extractor.BasicAuthExtractor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * {@link RestController} implementation of a REST API
 * that allows for registration of CAS services.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@RestController("registeredServiceResourceRestController")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@RequiredArgsConstructor
public class RegisteredServiceResource {
    private final AuthenticationSystemSupport authenticationSystemSupport;
    private final ServiceFactory serviceFactory;
    private final ServicesManager servicesManager;
    private final String attributeName;
    private final String attributeValue;

    /**
     * Create new service.
     *
     * @param service  the service
     * @param request  the request
     * @param response the response
     * @return {@link ResponseEntity} representing RESTful response
     */
    @PostMapping(value = "/v1/services", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> createService(@RequestBody final RegisteredService service,
                                                final HttpServletRequest request, final HttpServletResponse response) {
        try {
            val auth = authenticateRequest(request, response);
            if (isAuthenticatedPrincipalAuthorized(auth)) {
                this.servicesManager.save(service);
                return new ResponseEntity<>(HttpStatus.OK);
            }
            return new ResponseEntity<>("Request is not authorized", HttpStatus.FORBIDDEN);
        } catch (final AuthenticationException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    private boolean isAuthenticatedPrincipalAuthorized(final Authentication auth) {
        val attributes = auth.getPrincipal().getAttributes();
        LOGGER.debug("Evaluating principal attributes [{}]", attributes.keySet());
        if (StringUtils.isBlank(this.attributeName) || StringUtils.isBlank(this.attributeValue)) {
            LOGGER.error("No attribute name or value is defined to authorize this request");
            return false;
        }
        val pattern = RegexUtils.createPattern(this.attributeValue);
        if (attributes.containsKey(this.attributeName)) {
            val values = CollectionUtils.toCollection(attributes.get(this.attributeName));
            return values.stream().anyMatch(t -> RegexUtils.matches(pattern, t.toString()));
        }
        return false;
    }

    private Authentication authenticateRequest(final HttpServletRequest request, final HttpServletResponse response) {
        val extractor = new BasicAuthExtractor();
        val webContext = new JEEContext(request, response);
        val credentialsResult = extractor.extract(webContext);
        if (credentialsResult.isPresent()) {
            val credentials = credentialsResult.get();
            LOGGER.debug("Received basic authentication request from credentials [{}]", credentials);
            val c = new UsernamePasswordCredential(credentials.getUsername(), credentials.getPassword());
            val serviceRequest = this.serviceFactory.createService(request);
            val result = authenticationSystemSupport.handleAndFinalizeSingleAuthenticationTransaction(serviceRequest, c);
            return result.getAuthentication();
        }
        throw new BadRestRequestException("Could not authenticate request");
    }
}
