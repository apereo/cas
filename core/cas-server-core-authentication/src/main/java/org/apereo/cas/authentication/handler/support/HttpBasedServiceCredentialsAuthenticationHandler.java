package org.apereo.cas.authentication.handler.support;

import org.apereo.cas.authentication.AbstractAuthenticationHandler;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.DefaultHandlerResult;
import org.apereo.cas.authentication.HandlerResult;
import org.apereo.cas.authentication.HttpBasedServiceCredential;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.http.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.FailedLoginException;
import java.net.URL;
import java.security.GeneralSecurityException;

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
public class HttpBasedServiceCredentialsAuthenticationHandler extends AbstractAuthenticationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpBasedServiceCredentialsAuthenticationHandler.class);
    
    /** Instance of Apache Commons HttpClient. */
    private final HttpClient httpClient;

    /**
     * Instantiates a new Abstract authentication handler.
     *
     * @param name             Handler name.
     * @param servicesManager  the services manager.
     * @param principalFactory the principal factory
     * @param order            the order
     * @param httpClient       the http client
     */
    public HttpBasedServiceCredentialsAuthenticationHandler(final String name, final ServicesManager servicesManager, 
                                                            final PrincipalFactory principalFactory,
                                                            final Integer order, final HttpClient httpClient) {
        super(name, servicesManager, principalFactory, order);
        this.httpClient = httpClient;
    }

    @Override
    public HandlerResult authenticate(final Credential credential) throws GeneralSecurityException {
        final HttpBasedServiceCredential httpCredential = (HttpBasedServiceCredential) credential;
        if (!httpCredential.getService().getProxyPolicy().isAllowedProxyCallbackUrl(httpCredential.getCallbackUrl())) {
            LOGGER.warn("Proxy policy for service [{}] cannot authorize the requested callback url [{}].",
                    httpCredential.getService().getServiceId(), httpCredential.getCallbackUrl());
            throw new FailedLoginException(httpCredential.getCallbackUrl() + " cannot be authorized");
        }

        LOGGER.debug("Attempting to authenticate [{}]", httpCredential);
        final URL callbackUrl = httpCredential.getCallbackUrl();
        if (!this.httpClient.isValidEndPoint(callbackUrl)) {
            throw new FailedLoginException(callbackUrl.toExternalForm() + " sent an unacceptable response status code");
        }
        return new DefaultHandlerResult(this, httpCredential, this.principalFactory.createPrincipal(httpCredential.getId()));
    }

    /**
     *
     * @return true if the credential provided are not null and the credential
     * are a subclass of (or equal to) HttpBasedServiceCredential.
     */
    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof HttpBasedServiceCredential;
    }
}
