package org.jasig.cas.authentication.handler.support;

import java.net.URL;
import java.security.GeneralSecurityException;

import javax.security.auth.login.FailedLoginException;
import javax.validation.constraints.NotNull;

import org.jasig.cas.authentication.AbstractAuthenticationHandler;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.DefaultHandlerResult;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.HttpBasedServiceCredential;
import org.jasig.cas.util.http.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

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

@Component("proxyAuthenticationHandler")
public final class HttpBasedServiceCredentialsAuthenticationHandler extends AbstractAuthenticationHandler {

    /** Log instance. */
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    /** Instance of Apache Commons HttpClient. */
    @NotNull
    @Autowired
    @Qualifier("supportsTrustStoreSslSocketFactoryHttpClient")
    private HttpClient httpClient;

    @Override
    public HandlerResult authenticate(final Credential credential) throws GeneralSecurityException {
        final HttpBasedServiceCredential httpCredential = (HttpBasedServiceCredential) credential;
        if (!httpCredential.getService().getProxyPolicy().isAllowedProxyCallbackUrl(httpCredential.getCallbackUrl())) {
            logger.warn("Proxy policy for service [{}] cannot authorize the requested callback url [{}].",
                    httpCredential.getService().getServiceId(), httpCredential.getCallbackUrl());
            throw new FailedLoginException(httpCredential.getCallbackUrl() + " cannot be authorized");
        }

        logger.debug("Attempting to authenticate {}", httpCredential);
        final URL callbackUrl = httpCredential.getCallbackUrl();
        if (!this.httpClient.isValidEndPoint(callbackUrl)) {
            throw new FailedLoginException(callbackUrl.toExternalForm() + " sent an unacceptable response status code");
        }
        return new DefaultHandlerResult(this, httpCredential, this.principalFactory.createPrincipal(httpCredential.getId()));
    }

    /**
     * {@inheritDoc}
     * @return true if the credential provided are not null and the credential
     * are a subclass of (or equal to) HttpBasedServiceCredential.
     */
    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof HttpBasedServiceCredential;
    }

    /**
     * Sets the HttpClient which will do all of the connection stuff.
     * @param httpClient http client instance to use
     **/
    public void setHttpClient(final HttpClient httpClient) {
        this.httpClient = httpClient;
    }

}
