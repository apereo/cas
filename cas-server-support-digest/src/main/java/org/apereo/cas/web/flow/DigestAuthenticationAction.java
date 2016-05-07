package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.web.support.WebUtils;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.http.credentials.DigestCredentials;
import org.pac4j.http.credentials.extractor.DigestAuthExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link DigestAuthenticationAction} that extracts digest authN credentials from the request.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Component("digestAuthenticationAction")
public class DigestAuthenticationAction extends AbstractNonInteractiveCredentialsAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(DigestAuthenticationAction.class);

    @Override
    protected Credential constructCredentialsFromRequest(final RequestContext requestContext) {
        final HttpServletRequest request = WebUtils.getHttpServletRequest(requestContext);
        final HttpServletResponse response = WebUtils.getHttpServletResponse(requestContext);
        final DigestAuthExtractor extractor = new DigestAuthExtractor(this.getClass().getSimpleName());
        final WebContext webContext = new J2EContext(request, response);
        try {
            final DigestCredentials credentials = extractor.extract(webContext);
            if (credentials != null) {
                LOGGER.debug("Received digest authentication request from credentials {} ", credentials);
                return new UsernamePasswordCredential(credentials.getUsername(), credentials.getToken());
            }
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e);

        }
        return null;
    }
}
