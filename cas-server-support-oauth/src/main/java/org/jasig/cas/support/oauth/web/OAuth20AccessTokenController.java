package org.jasig.cas.support.oauth.web;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.OAuthUtils;
import org.jasig.cas.support.oauth.services.OAuthRegisteredService;
import org.jasig.cas.support.oauth.ticket.accesstoken.AccessToken;
import org.jasig.cas.support.oauth.ticket.accesstoken.AccessTokenFactory;
import org.jasig.cas.support.oauth.ticket.code.OAuthCode;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.http.credentials.UsernamePasswordCredentials;
import org.pac4j.http.credentials.extractor.BasicAuthExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

/**
 * This controller returns an access token according to the given OAuth code.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
@Component("accessTokenController")
public final class OAuth20AccessTokenController extends BaseOAuthWrapperController {

    @Autowired
    @Qualifier("defaultAccessTokenFactory")
    private AccessTokenFactory accessTokenFactory;

    @NotNull
    @Value("${tgt.timeToKillInSeconds:7200}")
    private long timeout;

    private BasicAuthExtractor basicAuthExtractor = new BasicAuthExtractor(null);

    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        if (!verifyAccessTokenRequest(request)) {
            return OAuthUtils.writeTextError(response, OAuthConstants.INVALID_REQUEST, HttpStatus.SC_BAD_REQUEST);
        }

        final String codeParameter = request.getParameter(OAuthConstants.CODE);
        final OAuthCode code = ticketRegistry.getTicket(codeParameter, OAuthCode.class);
        // code should not be expired
        if (code == null || code.isExpired()) {
            logger.error("Code expired: {}", code);
            return OAuthUtils.writeTextError(response, OAuthConstants.INVALID_GRANT, HttpStatus.SC_BAD_REQUEST);
        }
        ticketRegistry.deleteTicket(code.getId());

        final Service service = code.getService();
        final Authentication authentication = code.getAuthentication();
        final AccessToken accessToken = accessTokenFactory.create(service, authentication);
        ticketRegistry.addTicket(accessToken);

        final String text = String.format("%s=%s&%s=%s", OAuthConstants.ACCESS_TOKEN, accessToken.getId(), OAuthConstants.EXPIRES, timeout);
        logger.debug("OAuth access token response: {}", text);
        response.setContentType("text/plain");
        return OAuthUtils.writeText(response, text, HttpStatus.SC_OK);
    }

    /**
     * Verify the access token request.
     *
     * @param request the HTTP request
     * @return true, if successful
     */
    private boolean verifyAccessTokenRequest(final HttpServletRequest request) {

        final boolean checkParameterExist = checkCredentialsExist(request)
                && checkParameterExist(request, OAuthConstants.REDIRECT_URI)
                && checkParameterExist(request, OAuthConstants.CODE);

        return checkParameterExist
                && checkServiceValid(request)
                && checkCallbackValid(request)
                && checkClientSecret(request);
    }

    /**
     * Check if the credentials exist.
     *
     * @param request the HTTP request
     * @return whether the credentials exist
     */
    private boolean checkCredentialsExist(final HttpServletRequest request) {
        final UsernamePasswordCredentials credential = getCredentials(request);
        if (StringUtils.isBlank(credential.getUsername())) {
            logger.error("Missing clientId");
            return false;
        } else if (StringUtils.isBlank(credential.getPassword())) {
            logger.error("Missing secret");
            return false;
        }
        return true;
    }

    /**
     * Check the client secret.
     *
     * @param request the HTTP request
     * @return whether the secret is valid
     */
    private boolean checkClientSecret(final HttpServletRequest request) {
        final UsernamePasswordCredentials credential = getCredentials(request);
        final String clientId = credential.getUsername();
        final String clientSecret = credential.getPassword();
        final OAuthRegisteredService service = OAuthUtils.getRegisteredOAuthService(this.servicesManager, clientId);
        logger.debug("Found: {} for: {} in secret check", service, clientId);

        if (!StringUtils.equals(service.getClientSecret(), clientSecret)) {
            logger.error("Wrong client secret for service: {}", service);
            return false;
        }
        return true;
    }

    @Override
    protected String getClientId(final HttpServletRequest request) {
        return getCredentials(request).getUsername();
    }

    /**
     * Get the client credentials.
     *
     * @param request the HTTP request
     * @return the client credentials
     */
    private UsernamePasswordCredentials getCredentials(final HttpServletRequest request) {
        final String id = request.getParameter(OAuthConstants.CLIENT_ID);
        final String secret = request.getParameter(OAuthConstants.CLIENT_SECRET);
        UsernamePasswordCredentials credentials = null;
        if (StringUtils.isNotBlank(id) && StringUtils.isNotBlank(secret)) {
            credentials = new UsernamePasswordCredentials(id, secret, null);
        } else {
            try {
                credentials = basicAuthExtractor.extract(new J2EContext(request, null));
            } catch (final TechnicalException e) {
                logger.error("Cannot get clientId / secret from header", e);
            }
        }
        if (credentials != null) {
            return credentials;
        } else {
            return new UsernamePasswordCredentials(null, null, null);
        }
    }

    public AccessTokenFactory getAccessTokenFactory() {
        return accessTokenFactory;
    }

    public void setAccessTokenFactory(final AccessTokenFactory accessTokenFactory) {
        this.accessTokenFactory = accessTokenFactory;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(final long timeout) {
        this.timeout = timeout;
    }
}
