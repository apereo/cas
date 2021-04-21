package org.apereo.cas.support.rest.resources;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.MultifactorAuthenticationTriggerSelectionStrategy;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.rest.BadRestRequestException;
import org.apereo.cas.rest.factory.RestHttpRequestCredentialFactory;
import org.apereo.cas.rest.factory.UserAuthenticationResourceEntityResponseFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.validation.RequestedAuthenticationContextValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.security.auth.login.FailedLoginException;
import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * {@link RestController} implementation of CAS' REST API.
 * <p>
 * This class implements main CAS RESTful resource for vending/deleting TGTs and vending STs:
 * </p>
 * <ul>
 * <li>{@code POST /v1/tickets}</li>
 * <li>{@code POST /v1/tickets/{TGT-id}}</li>
 * <li>{@code GET /v1/tickets/{TGT-id}}</li>
 * <li>{@code DELETE /v1/tickets/{TGT-id}}</li>
 * </ul>
 *
 * @author Dmitriy Kopylenko
 * @since 4.1.0
 */
@RestController("userAuthenticationResource")
@Slf4j
@RequiredArgsConstructor
public class UserAuthenticationResource {
    private final AuthenticationSystemSupport authenticationSystemSupport;

    private final RestHttpRequestCredentialFactory credentialFactory;

    private final ServiceFactory<WebApplicationService> serviceFactory;

    private final UserAuthenticationResourceEntityResponseFactory userAuthenticationResourceEntityResponseFactory;

    private final ApplicationContext applicationContext;

    private final MultifactorAuthenticationTriggerSelectionStrategy multifactorTriggerSelectionStrategy;

    private final ServicesManager servicesManager;

    private final RequestedAuthenticationContextValidator requestedContextValidator;

    /**
     * Create new ticket granting ticket.
     *
     * @param requestBody username and password application/x-www-form-urlencoded values
     * @param request     raw HttpServletRequest used to call this method
     * @return ResponseEntity representing RESTful response
     */
    @PostMapping(value = RestProtocolConstants.ENDPOINT_USERS, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<String> createTicketGrantingTicket(@RequestBody final MultiValueMap<String, String> requestBody,
                                                             final HttpServletRequest request) {
        try {
            val credentials = this.credentialFactory.fromRequest(request, requestBody);
            if (credentials == null || credentials.isEmpty()) {
                throw new BadRestRequestException("No credentials are provided or extracted to authenticate the REST request");
            }
            val service = this.serviceFactory.createService(request);
            val registeredService = servicesManager.findServiceBy(service);
            val authResult = Optional.ofNullable(
                authenticationSystemSupport.handleInitialAuthenticationTransaction(service, credentials.toArray(Credential[]::new)));

            val authenticationResult = authResult.map(result -> result.getInitialAuthentication()
                .filter(authn -> !requestedContextValidator.validateAuthenticationContext(request, registeredService, authn, service).isSuccess())
                .map(authn ->
                    multifactorTriggerSelectionStrategy.resolve(request, registeredService, authn, service)
                        .map(provider -> {
                            LOGGER.debug("Extracting credentials for multifactor authentication via [{}]", provider);
                            val authnCredentials = credentialFactory.fromAuthentication(request, requestBody, authn, provider);
                            if (authnCredentials == null || authnCredentials.isEmpty()) {
                                throw new AuthenticationException("Unable to extract credentials for multifactor authentication");
                            }
                            return authenticationSystemSupport.finalizeAuthenticationTransaction(service, authnCredentials);
                        })
                        .orElseGet(() -> authenticationSystemSupport.finalizeAllAuthenticationTransactions(result, service)))
                .orElse(authenticationSystemSupport.finalizeAuthenticationTransaction(service, credentials)));
            val result = authenticationResult.orElseThrow(FailedLoginException::new);
            return this.userAuthenticationResourceEntityResponseFactory.build(result, request);
        } catch (final AuthenticationException e) {
            return RestResourceUtils.createResponseEntityForAuthnFailure(e, request, applicationContext);
        } catch (final BadRestRequestException e) {
            LoggingUtils.error(LOGGER, e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
