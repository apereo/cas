package org.apereo.cas.support.rest;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.rest.BadRestRequestException;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.util.function.FunctionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.authentication.www.BasicAuthenticationConverter;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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

    private final ServiceFactory<WebApplicationService> serviceFactory;

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
     * @throws Throwable the throwable
     */
    @PostMapping(value = "/v1/services", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create registered service",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Registered service JSON payload",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = RegisteredService.class)
            )
        ))
    public ResponseEntity<String> createService(@RequestBody final RegisteredService service,
                                                final HttpServletRequest request,
                                                final HttpServletResponse response) throws Throwable {
        try {
            val auth = authenticateRequest(request);
            if (isAuthenticatedPrincipalAuthorized(auth)) {
                this.servicesManager.save(service);
                return new ResponseEntity<>(HttpStatus.OK);
            }
            return new ResponseEntity<>("Request is not authorized", HttpStatus.FORBIDDEN);
        } catch (final AuthenticationException e) {
            return new ResponseEntity<>(StringEscapeUtils.escapeHtml4(e.getMessage()), HttpStatus.UNAUTHORIZED);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
            return new ResponseEntity<>(StringEscapeUtils.escapeHtml4(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    private boolean isAuthenticatedPrincipalAuthorized(final Authentication auth) throws Throwable {
        FunctionUtils.throwIfNull(auth, () -> new AuthenticationException("Unable to determine or verify authentication attempt"));
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

    private Authentication authenticateRequest(final HttpServletRequest request) {
        val converter = new BasicAuthenticationConverter();
        val token = converter.convert(request);
        return FunctionUtils.doIfNotNull(token, () -> {
            LOGGER.debug("Received basic authentication ECP request from credentials [{}]", token.getPrincipal());
            val upc = new UsernamePasswordCredential(token.getPrincipal().toString(), token.getCredentials().toString());
            val serviceRequest = this.serviceFactory.createService(request);
            val result = authenticationSystemSupport.finalizeAuthenticationTransaction(serviceRequest, upc);
            if (result == null) {
                throw new BadRestRequestException("Unable to establish authentication using provided credentials for " + upc.getUsername());
            }
            return result.getAuthentication();
        });
    }
}
