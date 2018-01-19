package org.apereo.cas.authentication.handler.support;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.AbstractAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.DefaultAuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.HttpBasedServiceCredential;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.web.UrlValidator;

import javax.security.auth.login.FailedLoginException;
import java.net.URL;

/**
 * Class to validate the credential presented by communicating with the web
 * server and checking the certificate that is returned against the hostname,
 * etc.
 * <p>
 * This class is concerned with ensuring that the protocol is HTTPS and that a
 * response is returned. The SSL handshake that occurs automatically by opening
 * a connection does the heavy process of authenticating.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Slf4j
public class HttpBasedServiceCredentialsAuthenticationHandler extends AbstractAuthenticationHandler {

    private final UrlValidator urlValidator;
    /**
     * Instance of Apache Commons HttpClient.
     */
    private final HttpClient httpClient;

    /**
     * Instantiates a new Abstract authentication handler.
     *
     * @param name             Handler name.
     * @param servicesManager  the services manager.
     * @param principalFactory the principal factory
     * @param order            the order
     * @param httpClient       the http client
     * @param urlValidator     the url validator
     */
    public HttpBasedServiceCredentialsAuthenticationHandler(final String name,
                                                            final ServicesManager servicesManager,
                                                            final PrincipalFactory principalFactory,
                                                            final Integer order, final HttpClient httpClient,
                                                            final UrlValidator urlValidator) {
        super(name, servicesManager, principalFactory, order);
        this.httpClient = httpClient;
        this.urlValidator = urlValidator;
    }

    @Override
    @SneakyThrows
    public AuthenticationHandlerExecutionResult authenticate(final Credential credential) {
        final HttpBasedServiceCredential httpCredential = (HttpBasedServiceCredential) credential;
        final String callbackUrl = httpCredential.getCallbackUrl();
        
        if (!this.urlValidator.isValid(httpCredential.getCallbackUrl())) {
            throw new FailedLoginException(callbackUrl + " is not a valid callback URL");
        }
        
        final URL pgtUrl = new URL(callbackUrl);
        if (!httpCredential.getService().getProxyPolicy().isAllowedProxyCallbackUrl(pgtUrl)) {
            LOGGER.warn("Proxy policy for service [{}] cannot authorize the requested callback url [{}].",
                httpCredential.getService().getServiceId(), httpCredential.getCallbackUrl());
            throw new FailedLoginException(httpCredential.getCallbackUrl() + " cannot be authorized");
        }

        LOGGER.debug("Attempting to authenticate [{}]", httpCredential);
        if (!this.httpClient.isValidEndPoint(callbackUrl)) {
            throw new FailedLoginException(callbackUrl + " sent an unacceptable response status code");
        }
        return new DefaultAuthenticationHandlerExecutionResult(this, httpCredential, this.principalFactory.createPrincipal(httpCredential.getId()));
    }

    /**
     * @return true if the credential provided are not null and the credential
     * are a subclass of (or equal to) HttpBasedServiceCredential.
     */
    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof HttpBasedServiceCredential;
    }
}
