package org.apereo.cas.rest.authentication;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.MultifactorAuthenticationTriggerSelectionStrategy;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.rest.BadRestRequestException;
import org.apereo.cas.rest.factory.RestHttpRequestCredentialFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.validation.RequestedAuthenticationContextValidator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.util.MultiValueMap;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * This is {@link DefaultRestAuthenticationService}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiredArgsConstructor
@Getter
@Slf4j
public class DefaultRestAuthenticationService implements RestAuthenticationService {
    private final AuthenticationSystemSupport authenticationSystemSupport;

    private final RestHttpRequestCredentialFactory credentialFactory;

    private final ServiceFactory<WebApplicationService> serviceFactory;

    private final MultifactorAuthenticationTriggerSelectionStrategy multifactorTriggerSelectionStrategy;

    private final ServicesManager servicesManager;

    private final RequestedAuthenticationContextValidator requestedContextValidator;

    @Override
    public Optional<AuthenticationResult> authenticate(final MultiValueMap<String, String> requestBody,
                                                       final HttpServletRequest request) {
        val credentials = this.credentialFactory.fromRequest(request, requestBody);
        if (credentials == null || credentials.isEmpty()) {
            throw new BadRestRequestException("No credentials can be extracted to authenticate the REST request");
        }
        val service = this.serviceFactory.createService(request);
        val registeredService = servicesManager.findServiceBy(service);
        val authResult = Optional.ofNullable(
            authenticationSystemSupport.handleInitialAuthenticationTransaction(service, credentials.toArray(Credential[]::new)));

        return authResult.map(result -> result.getInitialAuthentication()
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
    }
}
